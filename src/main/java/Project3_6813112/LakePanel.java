// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JPanel;

/** Side view of the fishing spot. Draws everything and turns mouse input into game commands. */
public class LakePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int W = LakeGame.W, H = LakeGame.H, SURFACE = LakeGame.SURFACE, BOTTOM = LakeGame.BOTTOM;
    private static final int WW = LakeGame.WORLD_W;     // the lake is wider than the screen
    private static final Font SIGN = Theme.title(17);
    private static final Font SMALL = Theme.body(Font.BOLD, 12);
    private static final Font HINT = Theme.body(Font.BOLD, 14);
    private static final double JUMP_TIME = 0.75;     // fish leap before the catch card

    private final LakeGame game;
    private GameLoop loop;
    private Fish hover;
    private final int[][] stars = new int[60][2];
    private final double[] weeds = new double[Math.max(44, LakeGame.WORLD_W / 55)];

    public LakePanel(LakeGame game) {
        this.game = game;
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        Random r = new Random(7);
        for (int[] s : stars) {
            s[0] = r.nextInt(W);
            s[1] = r.nextInt(SURFACE - 40);
        }
        for (int i = 0; i < weeds.length; i++) weeds[i] = LakeGame.SHORE_X + 30 + r.nextDouble() * (WW - LakeGame.SHORE_X - 40);
    }

    public void setLoop(GameLoop loop) { this.loop = loop; }

    // Optional fixed clock: lets a video renderer draw frame by frame with exact, repeatable animation time.
    private double fixedTime = -1;

    public void setFixedTime(double seconds) {
        if (fixedTime < 0) lastNanos = (long) (seconds * 1e9);     // start the virtual clock without a jump
        fixedTime = seconds;
    }

    private double clock() { return fixedTime >= 0 ? fixedTime : System.currentTimeMillis() / 1000.0; }

    private static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // =====================================================================
    // painting
    // =====================================================================
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double t = clock();

        synchronized (game) {     // read a consistent picture of the game
            double day = game.daylight();
            boolean rain = game.isRaining();
            Graphics2D vg = (Graphics2D) g.create();
            vg.clipRect(0, 0, W, H);                // the zoomed picture must not spill over the side panel
            double shx = (Math.sin(t * 71) * punch * 7 + Math.sin(t * 53) * shake * 10);
            double shy = (Math.cos(t * 67) * punch * 7 + Math.cos(t * 59) * shake * 10);
            vg.translate(shx, shy);                 // camera shake on the throw and on the splash
            paintViews(vg, day, rain, t);
            vg.translate(-shx, -shy);
            drawCastEffects(vg, t);
            vg.dispose();
            if (rain) drawRain(g, t);
            drawHud(g, t);
            drawPhaseOverlay(g, t);
            java.util.List<int[]> placed = new java.util.ArrayList<>();         // boxes of the toasts drawn so far
            for (LakeGame.Toast toast : game.getToasts()) {
                g.setFont(toast.text.length() <= 2 ? Theme.title(34) : Theme.title(17));
                FontMetrics fm = g.getFontMetrics();
                int alpha = (int) Math.max(0, 255 * (1 - toast.age / 1.8));
                int tw = fm.stringWidth(toast.text);
                int x = (int) Math.max(tw / 2.0 + 8, Math.min(W - tw / 2.0 - 8, screenX(toast.x))) - tw / 2;
                int y = (int) Math.max(40, Math.min(H - 24, screenY(toast.y)));
                for (int tries = 0; tries < 6; tries++) {                       // do not draw on top of another toast
                    boolean clash = false;
                    for (int[] pl : placed) if (Math.abs(pl[0] - (x + tw / 2)) < (pl[2] + tw) / 2 + 6 && Math.abs(pl[1] - y) < 22) clash = true;
                    if (!clash) break;
                    y += 24;
                }
                placed.add(new int[]{x + tw / 2, y, tw});
                g.setColor(new Color(40, 25, 10, alpha / 2));
                g.drawString(toast.text, x + 2, y + 2);
                Color c = toast.color;
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
                g.drawString(toast.text, x, y);
            }
        }
        drawPauseOrEnd(g);
        g.dispose();
    }

    // =====================================================================
    // two camera views: 2.5-D (idle / casting) and 2-D side view (line is in the water)
    // =====================================================================
    private static final double SWITCH_TIME = 0.7;      // seconds for the animated change of view
    private double blend;                               // 0 = 2.5-D view, 1 = side view
    private double camY;                                // side view scrolls down with the hook in deep water
    private double zoom = 1;                            // extra zoom of the 2.5-D view while charging / casting
    private double castFollow;                          // 0..1: the 2.5-D camera centres on the flying bobber
    private double followX = W / 2.0, followY = H / 2.0; // where the 2.5-D camera looks while following the bobber
    private static final double FOCUS_ZOOM = 1.9;       // side view zoom while the hook is in the water
    private double viewZoom = 1;                        // current zoom of the side view
    private LakeGame.Phase lastPhase = LakeGame.Phase.IDLE;
    private int seenChomps;
    private boolean mouseDown;                          // the button is held: it reels as soon as a fish is hooked
    private double punch, shake, frameDt;               // cast punch-in, screen shake, last frame time
    private final java.util.List<double[]> drops = new java.util.ArrayList<>();   // splash droplets: x, y, vx, vy, life
    private double camCX = W / 2.0, camCY = H / 2.0;    // world point at the centre of the side view
    private long lastNanos = System.nanoTime();
    private boolean render3D;                           // which scene is being drawn right now
    private BufferedImage imgA, imgB;                   // scratch images used only during the transition

    /** The side view is used once the line is in the water; before that the 2.5-D view is used. */
    private boolean wantSide() {
        return switch (game.getPhase()) {
            case WAITING, BITE, REELING, CAUGHT -> true;
            default -> false;
        };
    }

    /** Moves "blend" and "zoom" toward where they should be (time based, so it is smooth at any frame rate). */
    private void advanceViews() {
        long now = fixedTime >= 0 ? (long) (fixedTime * 1e9) : System.nanoTime();
        double dt = Math.max(0, Math.min(0.1, (now - lastNanos) / 1e9));
        lastNanos = now;
        double target = wantSide() ? 1 : 0;
        double step = dt / SWITCH_TIME;
        blend = blend < target ? Math.min(target, blend + step) : Math.max(target, blend - step);
        LakeGame.Phase ph = game.getPhase();
        frameDt = dt;
        if (ph != lastPhase) {
            if (ph == LakeGame.Phase.CASTING) punch = 1;                          // the throw: the camera slams in
            if (lastPhase == LakeGame.Phase.CASTING && ph == LakeGame.Phase.WAITING) {   // the splash
                shake = 1;
                viewZoom += 0.55;                                                 // the side view crashes in, then settles
                Random sr = new Random();
                for (int i = 0; i < 26; i++) {
                    double ang = -Math.PI / 2 + (sr.nextDouble() - 0.5) * 1.8;
                    double sp = 120 + sr.nextDouble() * 260;
                    drops.add(new double[]{game.getBobberX(), SURFACE, Math.cos(ang) * sp, Math.sin(ang) * sp, 0.9 + sr.nextDouble() * 0.4});
                }
            }
            lastPhase = ph;
        }
        if (game.getChomps() != seenChomps) {                                 // a bigger fish bit the catch
            seenChomps = game.getChomps();
            shake = 1;
        }
        if (mouseDown && ph == LakeGame.Phase.REELING && !game.isReeling()) game.setReelMouse(true);
        punch *= Math.exp(-dt * 4.5);
        shake *= Math.exp(-dt * 3.5);
        for (java.util.Iterator<double[]> it = drops.iterator(); it.hasNext(); ) {
            double[] d = it.next();
            d[3] += 700 * dt;
            d[0] += d[2] * dt;
            d[1] += d[3] * dt;
            d[4] -= dt;
            if (d[4] <= 0 || d[1] > SURFACE + 6) it.remove();
        }
        double maxScroll = Math.max(0, game.bottomY() + 24 - H);
        boolean following = ph == LakeGame.Phase.WAITING || ph == LakeGame.Phase.BITE || ph == LakeGame.Phase.REELING;
        double want = following ? Math.max(0, Math.min(maxScroll, game.getHookY() - 330)) : 0;
        camY += (want - camY) * Math.min(1, dt * 4.5);          // eased, so going deeper never jerks the picture
        double zt = ph == LakeGame.Phase.CASTING ? 1.8 : ph == LakeGame.Phase.CHARGING ? 1.14 : 1.0;
        zoom += (zt - zoom) * Math.min(1, dt * 7);
        // casting: the 2.5-D camera locks on the flying bobber
        double cf = ph == LakeGame.Phase.CASTING ? 1 : 0;
        castFollow += (cf - castFollow) * Math.min(1, dt * 8);
        Point2D.Double bp = Perspective.project(game.getBobberX(), game.getBobberY(), 0);
        followX += (bp.x - followX) * Math.min(1, dt * 12);
        followY += (bp.y - followY) * Math.min(1, dt * 12);
        // hook in the water (or still flying): the side view zooms in and the picture follows the hook
        boolean focus = ph == LakeGame.Phase.WAITING || ph == LakeGame.Phase.BITE || ph == LakeGame.Phase.REELING;
        viewZoom += ((focus ? FOCUS_ZOOM : 1.0) - viewZoom) * Math.min(1, dt * 4);
        double halfW = W / (2 * viewZoom), halfH = H / (2 * viewZoom);
        double tx = W / 2.0, ty = camY + H / 2.0;
        if (focus) {
            tx = Math.max(halfW, Math.min(WW - halfW, game.getHookX()));
            ty = Math.max(halfH, Math.min(game.bottomY() + 24 - halfH, game.getHookY()));
        }
        double ck = Math.min(1, dt * 5);
        camCX += (tx - camCX) * ck;
        camCY += (ty - camCY) * ck;
    }

    private static double smooth(double x) { return x * x * (3 - 2 * x); }

    /** Speed lines, a dark vignette and a WHOOSH while the lure flies: a dramatic throw. */
    private void drawCastEffects(Graphics2D g, double t) {
        if (game.getPhase() != LakeGame.Phase.CASTING || blend > 0.6) return;
        double p = game.getCastProgress();
        double fade = Math.min(1, Math.min(p * 6, (1 - p) * 4 + 0.35));
        double cx = W / 2.0, cy = H / 2.0;
        g.setPaint(new java.awt.RadialGradientPaint(new Point2D.Double(cx, cy), 460f, new float[]{0.45f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 10, 20, (int) (140 * fade))}));
        g.fillRect(0, 0, W, H);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 34; i++) {
            double ang = i * Math.PI * 2 / 34 + Math.sin(i * 12.9) * 0.1;
            double r0 = 120 + 90 * Math.abs(Math.sin(i * 7.3 + t * 25));
            double r1 = r0 + 110 + 120 * Math.abs(Math.sin(i * 3.1 + t * 18));
            g.setColor(new Color(255, 255, 255, (int) (150 * fade)));
            g.draw(new java.awt.geom.Line2D.Double(cx + Math.cos(ang) * r0, cy + Math.sin(ang) * r0 * 0.8, cx + Math.cos(ang) * r1, cy + Math.sin(ang) * r1 * 0.8));
        }
        g.setStroke(new BasicStroke(1f));
        if (p < 0.55) {                                   // WHOOSH, big at first, shrinking
            double s = 1 + 0.9 * Math.max(0, 1 - p * 6);
            g.setFont(Theme.title((float) (44 * s)));
            FontMetrics fm = g.getFontMetrics();
            String w = "WHOOSH!";
            int x = (int) (cx - fm.stringWidth(w) / 2.0), y = 110;
            g.setColor(new Color(20, 40, 60, (int) (150 * (1 - p / 0.55))));
            g.drawString(w, x + 3, y + 3);
            g.setColor(new Color(255, 245, 200, (int) (255 * (1 - p / 0.55))));
            g.drawString(w, x, y);
        }
    }

    /** Screen position of a world point in the side view (the camera zooms in and scrolls with the hook). */
    private double screenX(double worldX) { return W / 2.0 + (worldX - camCX) * viewZoom; }
    private double screenY(double worldY) { return H / 2.0 + (worldY - camCY) * viewZoom; }

    private BufferedImage layer(BufferedImage old) {
        if (old != null && old.getWidth() == W && old.getHeight() == H) return old;
        return new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
    }

    private void paintViews(Graphics2D g, double day, boolean rain, double t) {
        advanceViews();
        if (blend <= 0.001) {
            paintScene(g, true, day, rain, t);
        } else if (blend >= 0.999) {
            paintScene(g, false, day, rain, t);
        } else {
            // transition: draw both views, push the 2.5-D picture toward the viewer while the side view fades in
            double e = smooth(blend);
            imgA = layer(imgA);
            imgB = layer(imgB);
            for (int i = 0; i < 2; i++) {
                BufferedImage img = i == 0 ? imgA : imgB;
                Graphics2D ig = img.createGraphics();
                ig.setComposite(AlphaComposite.Clear);
                ig.fillRect(0, 0, W, H);
                ig.setComposite(AlphaComposite.SrcOver);
                ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ig.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                paintScene(ig, i == 0, day, rain, t);
                ig.dispose();
            }
            double s3 = 1 + 0.16 * e, s2 = 1.10 - 0.10 * e;
            Graphics2D a = (Graphics2D) g.create();
            a.translate(W / 2.0, H * 0.62);
            a.scale(s3, s3);
            a.translate(-W / 2.0, -H * 0.62);
            a.drawImage(imgA, 0, 0, null);
            a.dispose();
            Graphics2D b = (Graphics2D) g.create();
            b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) e));
            b.translate(W / 2.0, H * 0.62);
            b.scale(s2, s2);
            b.translate(-W / 2.0, -H * 0.62);
            b.drawImage(imgB, 0, 0, null);
            b.dispose();
        }
    }

    /** One complete scene without any HUD: either the 2.5-D view or the side view. */
    private void paintScene(Graphics2D g, boolean as3D, double day, boolean rain, double t) {
        render3D = as3D;
        if (as3D) {
            paintScene3D(g, day, rain, t);
            return;
        }
        hz = SURFACE;
        Graphics2D w = (Graphics2D) g.create();
        w.translate(W / 2.0, H / 2.0);          // the camera zooms in on the hook and follows it
        w.scale(viewZoom, viewZoom);
        w.translate(-camCX, -camCY);
        drawSky(w, day, rain, t);
        drawWater(w, day, t);
        drawBottom(w, t);
        for (double[] b : game.getBubbles()) {
            w.setColor(new Color(255, 255, 255, 90));
            w.draw(new Ellipse2D.Double(b[0] - 3, b[1] - 3, 6, 6));
        }
        drawFish(w);
        drawDock(w, t);
        drawLine(w);
        for (double[] d : drops) {
            w.setColor(new Color(255, 255, 255, (int) Math.max(0, Math.min(230, d[4] * 260))));
            w.fill(new Ellipse2D.Double(d[0] - 2.5, d[1] - 2.5, 5, 5));
        }
        for (double[] r : game.getRipples()) {
            double rad = 8 + r[1] * 40;
            w.setColor(new Color(255, 255, 255, (int) Math.max(0, 180 * (1 - r[1] / 1.2))));
            w.draw(new Ellipse2D.Double(r[0] - rad, SURFACE - rad * 0.2, rad * 2, rad * 0.4));
        }
        w.dispose();
        if (viewZoom < 1.3) drawForeground(g, t);   // plants in front stay on the screen (not while zoomed in)
    }

    // =====================================================================
    // camera + parallax
    // =====================================================================

    /** Parallax factors: far layers move less than near ones when the camera pans. */
    private static final double P_SKY = 0.05, P_MOUNTAIN = 0.15, P_CLOUD = 0.25, P_HILL = 0.35, P_NEAR = 0.6;
    private double camX;            // virtual camera offset in pixels (eased every frame)
    private Color skyTop = Color.BLACK;
    private int hz = SURFACE;       // horizon line: SURFACE in side view, Perspective.HORIZON in 3-D
    /** Shadow colour: darker (and more see-through) the deeper the fish swims. */
    private Color shadowTint(double worldY) {
        double d = Math.max(0, Math.min(1, (worldY - SURFACE) / (double) Math.max(1, game.maxDepth())));
        if (game.daylight() < 0.3) {     // dark water at night: a dark shape would vanish, so use a pale one
            return new Color(150, 205, 235, (int) (200 - 90 * d));
        }
        return new Color(8, 22, 36, (int) (235 - 70 * d));
    }

    /** The camera sways slowly, leans back while aiming and follows the hook once the line is out. */
    private void updateCamera(double t) {
        double target = Math.sin(t * 0.15) * 18;
        switch (game.getPhase()) {
            case CHARGING -> target = -22;
            case CASTING, WAITING, BITE, REELING -> {
                if (!render3D) target += (game.getBobberX() - W / 2.0) * 0.12;   // in 2.5-D the line goes straight ahead
            }
            default -> { }
        }
        camX += (target - camX) * 0.04;     // ease so the view glides instead of jumping
    }

    /** Screen x of a point on a layer: x - camX * factor. */
    private double par(double x, double factor) {
        if (!render3D) return x + (1 - factor) * (camCX - W / 2.0);   // side view: far layers lag behind the scrolling world
        return x - camX * factor;
    }

    /** Width of the scenery to draw: the whole lake in the side view, the screen in the 2.5-D view. */
    private int span() { return render3D ? W : WW; }

    private double sunX() { return par(W * 0.12 + W * 0.55 * sunProgress(), P_SKY); }
    private double moonX() { return par(W * 0.45 + 19, P_SKY); }

    private void drawSky(Graphics2D g, double day, boolean rain, double t) {
        updateCamera(t);
        Color top = mix(new Color(12, 16, 48), new Color(100, 170, 235), day);
        Color bottom = mix(new Color(40, 45, 95), new Color(195, 225, 250), day);
        double warm = day > 0 && day < 1 ? 1 - Math.abs(day - 0.5) * 2 : 0;
        bottom = mix(bottom, new Color(250, 160, 100), warm * 0.7);
        if (rain) {
            top = mix(top, new Color(90, 95, 105), 0.6);
            bottom = mix(bottom, new Color(130, 135, 140), 0.6);
        }
        skyTop = top;
        g.setPaint(new GradientPaint(0, 0, top, 0, hz, bottom));
        g.fillRect(0, 0, span(), hz);

        // layer 0: stars, sun, moon (almost fixed - they are very far away)
        if (day < 0.5) {
            g.setColor(new Color(255, 255, 230, (int) (200 * (1 - day * 2))));
            for (int[] s : stars) g.fillRect((int) par(s[0], P_SKY), s[1], 2, 2);
        }
        double cx = sunX();
        if (day > 0.05 && !rain) {
            g.setColor(new Color(255, 240, 160, (int) (90 * Math.min(1, day * 1.5))));
            g.fill(new Ellipse2D.Double(cx - 34, 40 - day * 20, 68, 68));
            g.setColor(new Color(255, 225, 110, (int) (255 * Math.min(1, day * 1.5))));
            g.fill(new Ellipse2D.Double(cx - 24, 50 - day * 20, 48, 48));
        } else if (day <= 0.3) {
            double mx = moonX() - 19;
            g.setColor(new Color(240, 240, 220));
            g.fill(new Ellipse2D.Double(mx, 34, 38, 38));
            g.setColor(top);
            g.fill(new Ellipse2D.Double(mx + 10, 28, 38, 38));
        }

        Location loc = game.getSettings().location();
        // layer 1: far mountains, faded into the sky colour (atmospheric haze)
        Color far = mix(mix(loc.getLand(), new Color(120, 140, 175), 0.55), bottom, 0.45);
        far = mix(new Color(25, 30, 60), far, 0.35 + 0.65 * day);
        g.setColor(far);
        Polygon mountains = new Polygon();
        mountains.addPoint(-120, hz);
        for (int x = -120; x <= span() + 120; x += 24) {
            double y = hz - 62 - 48 * Math.abs(Math.sin(x * 0.0085 + loc.ordinal() * 1.7)) - 16 * Math.sin(x * 0.027 + 1);
            mountains.addPoint((int) par(x, P_MOUNTAIN), (int) y);
        }
        mountains.addPoint((int) par(span() + 120, P_MOUNTAIN), hz);
        g.fillPolygon(mountains);

        // layer 2: clouds - drift on their own and also move with the camera
        g.setColor(rain ? new Color(110, 115, 125, 230) : new Color(255, 255, 255, (int) (110 + 120 * day)));
        int clouds = render3D ? 4 : Math.max(11, WW / 220);
        for (int i = 0; i < clouds; i++) {
            double x = ((t * (8 + i * 3) + i * 240) % (span() + 260)) - 130;
            x = par(x, P_CLOUD);
            double y = 30 + i * 22;
            g.fill(new Ellipse2D.Double(x, y, 90, 26));
            g.fill(new Ellipse2D.Double(x + 25, y - 12, 55, 30));
        }

        // layer 3: rolling hills
        Color land = mix(loc.getLand().darker(), loc.getLand(), day);
        g.setColor(mix(land, far, 0.25));
        Polygon hills = new Polygon();
        hills.addPoint((int) par(-120, P_HILL), hz);
        for (int x = -120; x <= span() + 120; x += 40) {
            hills.addPoint((int) par(x, P_HILL), (int) (hz - 30 - 18 * Math.sin(x * 0.012 + loc.ordinal()) - 10 * Math.sin(x * 0.037)));
        }
        hills.addPoint((int) par(span() + 120, P_HILL), hz);
        g.fillPolygon(hills);

        // layer 4: near shore details - move the most
        drawNearShore(g, loc, land.darker(), t);
    }

    /** Front layer on the far bank: trees, a lighthouse or palms depending on the spot. */
    private void drawNearShore(Graphics2D g, Location loc, Color c, double t) {
        g.setColor(c);
        switch (loc) {
            case SEA -> {
                for (int lk = 0; lk < (render3D ? 1 : WW / 850); lk++) {
                double lx = par(W * 0.72 + lk * 850, P_NEAR);
                g.setColor(new Color(235, 230, 220));
                g.fillPolygon(new int[]{(int) lx - 9, (int) lx + 9, (int) lx + 6, (int) lx - 6},
                        new int[]{hz - 4, hz - 4, hz - 58, hz - 58}, 4);
                g.setColor(new Color(200, 70, 60));
                g.fillRect((int) lx - 8, hz - 30, 16, 8);
                g.fillRect((int) lx - 8, hz - 66, 16, 9);
                g.setColor(new Color(255, 240, 150, 160 + (int) (90 * Math.sin(t * 3))));
                g.fill(new Ellipse2D.Double(lx - 5, hz - 64, 10, 6));
                double bx = par(W * 0.42 + lk * 950 + Math.sin(t * 0.2) * 30, P_NEAR);   // a small sailboat
                g.setColor(new Color(90, 60, 40));
                g.fillPolygon(new int[]{(int) bx - 16, (int) bx + 16, (int) bx + 11, (int) bx - 11},
                        new int[]{hz - 8, hz - 8, hz - 2, hz - 2}, 4);
                g.setColor(new Color(245, 245, 240));
                g.fillPolygon(new int[]{(int) bx, (int) bx, (int) bx + 13}, new int[]{hz - 36, hz - 9, hz - 9}, 3);
                }
            }
            case LAGOON -> {
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < (render3D ? 4 : WW / 150); i++) {
                    double x = par(260 + i * 150 + (i % 2) * 30, P_NEAR);
                    int h = 34 + (i * 13) % 18;
                    g.draw(new QuadCurve2D.Double(x, hz - 2, x + 6, hz - h / 2.0, x + 2, hz - h));
                    for (int k = -2; k <= 2; k++) {
                        g.draw(new QuadCurve2D.Double(x + 2, hz - h, x + 2 + k * 9, hz - h - 8, x + 2 + k * 16, hz - h + 6));
                    }
                }
                g.setStroke(new BasicStroke(1f));
            }
            default -> {
                for (int x = 180; x < span() + 60; x += 46) {
                    double sx = par(x + (x * 7) % 19, P_NEAR);
                    int h = 22 + (x * 13) % 20;
                    if ((x / 46) % 3 == 0) {        // round tree
                        g.fill(new Ellipse2D.Double(sx - 11, hz - h - 6, 22, h));
                    } else {                        // pine
                        g.fillPolygon(new int[]{(int) sx - 10, (int) sx + 10, (int) sx},
                                new int[]{hz - 2, hz - 2, hz - h - 10}, 3);
                    }
                }
            }
        }
    }

    private double sunProgress() {
        String c = game.clockText();
        double h = Integer.parseInt(c.substring(0, 2)) + Integer.parseInt(c.substring(3)) / 60.0;
        return Math.max(0, Math.min(1, (h - 6) / 13));
    }

    /**
     * Height of the water surface at x: two sine waves travelling in opposite directions
     * add up to an irregular, natural-looking swell. Rain makes the waves bigger.
     */
    private double waveY(double x, double t) {
        return SURFACE + waveH(x, t);
    }

    /** Wave height (pixels, side view) at x. Also reused by the 3-D view, scaled by distance. */
    private double waveH(double x, double t) {
        double amp = game.isRaining() ? 1.7 : 1.0;
        return (Math.sin(x * 0.045 + t * 1.8) * 2.2 + Math.sin(x * 0.11 - t * 2.6) * 1.1) * amp;
    }

    private void drawWater(Graphics2D g, double day, double t) {
        Location loc = game.getSettings().location();
        Color top = mix(loc.getWaterTop().darker().darker(), loc.getWaterTop(), 0.35 + 0.65 * day);
        Color bottom = mix(Color.BLACK, loc.getWaterBottom(), 0.5 + 0.5 * day);

        // water body with a wavy top edge
        Polygon water = new Polygon();
        for (int x = 0; x <= WW; x += 8) water.addPoint(x, (int) Math.round(waveY(x, t)));
        int floor = game.bottomY() + 90;
        water.addPoint(WW, floor);
        water.addPoint(0, floor);
        g.setPaint(new GradientPaint(0, SURFACE, top, 0, game.bottomY(), bottom));
        g.fillPolygon(water);

        // faint mirror of the sky right under the surface
        g.setPaint(new GradientPaint(0, SURFACE, new Color(skyTop.getRed(), skyTop.getGreen(), skyTop.getBlue(), 90),
                0, SURFACE + 26, new Color(skyTop.getRed(), skyTop.getGreen(), skyTop.getBlue(), 0)));
        g.fillRect(0, SURFACE, WW, 26);

        if (day > 0.3) {   // sun rays under water
            for (int i = 0; i < WW / 130; i++) {
                double x = 200 + i * 130 + Math.sin(t * 0.3 + i) * 20;
                g.setColor(new Color(255, 255, 255, (int) (18 * day)));
                g.fillPolygon(new int[]{(int) x, (int) x + 30, (int) x - 40, (int) x - 90},
                        new int[]{SURFACE, SURFACE, game.bottomY(), game.bottomY()}, 4);
            }
        }

        // glittering reflection of the sun (day) or moon (night): short dashes that shimmer
        boolean rain = game.isRaining();
        if (!rain) {
            boolean sun = day > 0.05;
            double rx = sun ? sunX() : moonX();
            Color glint = sun ? new Color(255, 232, 150) : new Color(225, 230, 255);
            double strength = sun ? Math.min(1, day * 1.5) : 0.7;
            for (int k = 0; k < 16; k++) {
                double y = SURFACE + 4 + k * 6.5;
                double w = (46 - k * 2.2) * (0.6 + 0.4 * Math.abs(Math.sin(t * 2.7 + k * 1.9)));
                double sx = rx + Math.sin(t * 1.6 + k * 1.3) * (3 + k * 0.6);
                int alpha = (int) (strength * Math.max(0, 210 - k * 12));
                g.setColor(new Color(glint.getRed(), glint.getGreen(), glint.getBlue(), alpha));
                g.fillRoundRect((int) (sx - w / 2), (int) y, (int) Math.max(4, w), 3, 3, 3);
            }
        }

        // bright crest line + a softer second line just below it
        g.setStroke(new BasicStroke(1.6f));
        g.setColor(new Color(255, 255, 255, 140));
        drawWaveLine(g, t, 0);
        g.setColor(new Color(255, 255, 255, 45));
        drawWaveLine(g, t, 5);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawWaveLine(Graphics2D g, double t, int offset) {
        int prevX = 0, prevY = (int) Math.round(waveY(0, t)) + offset;
        for (int x = 8; x <= WW; x += 8) {
            int y = (int) Math.round(waveY(x, t + offset * 0.2)) + offset;
            g.drawLine(prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }
    }

    private void drawBottom(Graphics2D g, double t) {
        Location loc = game.getSettings().location();
        g.setColor(loc == Location.SEA ? new Color(170, 150, 100) : new Color(70, 60, 40));
        int floorY = game.bottomY();
        g.fillRect(0, floorY, WW, 120);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < weeds.length; i++) {
            double x = weeds[i], hgt = 40 + (i * 37) % 60;
            g.setColor(loc == Location.LAGOON ? new Color(60, 160, 140, 180) : new Color(40, 120, 60, 200));
            double sway = Math.sin(t * 1.3 + i) * 10;
            g.draw(new QuadCurve2D.Double(x, floorY, x + sway, floorY - hgt / 2, x + sway * 1.6, floorY - hgt));
        }
        g.setStroke(new BasicStroke(1f));
    }

    /** Draws a fish tilted along its swimming direction; squashed while it turns around. */
    private void drawFishSprite(Graphics2D g, Fish f, double cx, double cy, double len) {
        Graphics2D t = (Graphics2D) g.create();
        t.translate(cx, cy);
        t.rotate(f.getTilt());
        t.scale(f.getTurnScale(), 1);
        FishArt.draw(t, f.getSpecies(), 0, 0, len, f.facingRight(), f.anim, f.getEaten());
        t.dispose();
    }

    /** Pulsing golden glow around the fish that is about to bite (no name: you only see WHICH fish it is). */
    private void drawBiteGlow(Graphics2D g, Fish f) {
        boolean nibble = f.getState() == Fish.State.NIBBLE;
        double t = clock();
        double pulse = 0.5 + 0.5 * Math.sin(t * (nibble ? 16 : 8));
        float r = (float) (f.getLength() * (0.80 + 0.22 * pulse) * (nibble ? 1.12 : 1.0));
        int a = (int) ((nibble ? 150 : 95) + 70 * pulse);
        g.setPaint(new java.awt.RadialGradientPaint(new Point2D.Double(f.getX(), f.getY()), r,
                new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(255, 236, 140, Math.min(255, a)), new Color(255, 214, 90, Math.min(255, a / 2)), new Color(255, 200, 80, 0)}));
        g.fill(new Ellipse2D.Double(f.getX() - r, f.getY() - r, r * 2, r * 2));
        // small sparkles circling the fish
        g.setColor(new Color(255, 250, 200, 200));
        g.setStroke(new BasicStroke(1.4f));
        for (int i = 0; i < 3; i++) {
            double ang = t * 2.2 + i * 2.1;
            double sx = f.getX() + Math.cos(ang) * f.getLength() * 0.62, sy = f.getY() + Math.sin(ang) * f.getLength() * 0.40;
            double k = 2.5 + 1.5 * Math.sin(t * 9 + i);
            g.draw(new java.awt.geom.Line2D.Double(sx - k, sy, sx + k, sy));
            g.draw(new java.awt.geom.Line2D.Double(sx, sy - k, sx, sy + k));
        }
        g.setStroke(new BasicStroke(1f));
    }

    /** Pulsing red glow: this bigger fish is coming for the fish you are reeling in. */
    private void drawHunterGlow(Graphics2D g, Fish f) {
        double pulse = 0.5 + 0.5 * Math.sin(clock() * 18);
        float r = (float) (f.getLength() * (0.85 + 0.25 * pulse));
        int a = (int) (110 + 90 * pulse);
        g.setPaint(new java.awt.RadialGradientPaint(new Point2D.Double(f.getX(), f.getY()), r, new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(255, 70, 50, a), new Color(255, 60, 40, a / 2), new Color(255, 60, 40, 0)}));
        g.fill(new Ellipse2D.Double(f.getX() - r, f.getY() - r, r * 2, r * 2));
    }

    private void drawFish(Graphics2D g) {
        boolean finder = game.getSettings().fishFinder();
        for (Fish f : game.fishSnapshot()) {
            double sy = H / 2.0 + (f.getY() - camCY) * viewZoom;
            if (sy < -150 || sy > H + 150) continue;      // not on screen
            if (f.getState() == Fish.State.APPROACH || f.getState() == Fish.State.NIBBLE) drawBiteGlow(g, f);
            if (f.getState() == Fish.State.HUNTER) drawHunterGlow(g, f);
            drawFishSprite(g, f, f.getX(), f.getY(), f.getLength());
            boolean label = finder && f == hover;       // never names the fish that is about to bite: the catch is a surprise
            if (label) {
                String s = f.getSpecies().getTitle() + " " + f.getSizeCm() + "cm";
                g.setFont(SMALL);
                FontMetrics fm = g.getFontMetrics();
                int x = (int) f.getX() - fm.stringWidth(s) / 2, y = (int) (f.getY() - f.getLength() * 0.4 - 12);
                Theme.paintPaper(g, x - 6, y - 15, fm.stringWidth(s) + 12, 20, 10);
                g.setColor(f.getSpecies().isLegendary() ? new Color(190, 120, 0) : Theme.INK);
                g.drawString(s, x, y);
            }
        }
    }

    // =====================================================================
    // 3-D view: camera behind the angler looking out over the lake
    // =====================================================================
    private final java.util.IdentityHashMap<Fish, Point2D.Double> projected = new java.util.IdentityHashMap<>();
    private double rodTipX, rodTipY;

    private void paintScene3D(Graphics2D g0, double day, boolean rain, double t) {
        Graphics2D g = (Graphics2D) g0.create();
        double z3 = zoom + 0.6 * punch;               // a hard zoom punch the moment the lure is thrown
        if (z3 > 1.001) {          // click 1: zoom toward the rod; while casting the camera centres on the bobber
            double ax = W * 0.54 + (followX - W * 0.54) * castFollow, ay = H * 0.62 + (followY - H * 0.62) * castFollow;
            g.translate(ax + (W / 2.0 - ax) * castFollow, ay + (H / 2.0 - ay) * castFollow);
            g.scale(z3, z3);
            g.translate(-ax, -ay);
        }
        hz = (int) Perspective.HORIZON;
        drawSky(g, day, rain, t);
        drawWater3D(g, day, t);
        drawFish3D(g);
        for (double[] r : game.getRipples()) {
            Point2D.Double p = Perspective.project(r[0], SURFACE, 0);
            double s = Perspective.scale(r[0]);
            double rad = (8 + r[1] * 40) * s * 1.4;
            g.setColor(new Color(255, 255, 255, (int) Math.max(0, 200 * (1 - r[1] / 1.2))));
            g.draw(new Ellipse2D.Double(p.x - rad, p.y - rad * 0.22, rad * 2, rad * 0.44));
        }
        drawDock3D(g, t);
        drawLine3D(g, t);
        drawForeground(g, t);
        g.dispose();
    }

    private void drawWater3D(Graphics2D g, double day, double t) {
        Location loc = game.getSettings().location();
        Color near = mix(Color.BLACK, mix(loc.getWaterTop(), loc.getWaterBottom(), 0.45), 0.45 + 0.55 * day);
        Color far = mix(mix(loc.getWaterTop(), skyTop, 0.45), Color.WHITE, 0.12 * day);
        int horizon = (int) Perspective.HORIZON;
        g.setPaint(new GradientPaint(0, horizon, far, 0, H, near));
        g.fillRect(0, horizon, W, H - horizon);

        // wave rows: equal steps in DISTANCE, so rows crowd together toward the horizon
        g.setStroke(new BasicStroke(1.2f));
        for (double z = 1.0; z < 60; z *= 1.16) {
            double sy = Perspective.HORIZON + Perspective.FOCAL_X_HEIGHT / z;
            if (sy > H + 5) continue;
            double s = Math.max(0.12, Math.min(1.15, 2.6 / z));
            g.setColor(new Color(255, 255, 255, (int) Math.min(120, 25 + 90 * s)));
            int prevX = -10, prevY = (int) sy;
            for (int x = 0; x <= W + 10; x += 10) {
                double y = sy + waveH(x / Math.max(0.25, s) * 0.6 + z * 37, t) * s * 1.6;
                g.drawLine(prevX, prevY, x, (int) y);
                prevX = x;
                prevY = (int) y;
            }
        }
        // sun / moon glitter path: narrow at the horizon, wide near the camera
        if (!game.isRaining()) {
            boolean sun = day > 0.05;
            double rx = sun ? sunX() : moonX();
            Color glint = sun ? new Color(255, 232, 150) : new Color(225, 230, 255);
            double strength = sun ? Math.min(1, day * 1.5) : 0.7;
            for (double z = 1.4; z < 55; z *= 1.09) {
                double sy = Perspective.HORIZON + Perspective.FOCAL_X_HEIGHT / z;
                if (sy > H) continue;
                double s = Math.max(0.12, Math.min(1.15, 2.6 / z));
                double shimmer = Math.abs(Math.sin(t * 2.4 + z * 3.1));
                double w = (14 + 70 * s) * (0.4 + 0.6 * shimmer);
                double sx = rx + Math.sin(t * 1.5 + z) * 6 * s;
                g.setColor(new Color(glint.getRed(), glint.getGreen(), glint.getBlue(), (int) (strength * (90 + 120 * shimmer) * Math.min(1, s * 2))));
                g.fillRoundRect((int) (sx - w / 2), (int) sy, (int) Math.max(3, w), (int) Math.max(2, 3 * s), 3, 3);
            }
        }
    }

    /** One plain soft shadow for every species: only the size tells a small fish from a big one. */
    private void drawShadowBlob(Graphics2D g, double cx, double cy, double len, boolean right, double angle, Color tint) {
        Graphics2D b = (Graphics2D) g.create();
        b.translate(cx, cy);
        b.rotate(angle);
        if (!right) b.scale(-1, 1);
        for (int i = 0; i < 3; i++) {                         // three layers = a soft edge
            double k = 1 + (2 - i) * 0.12;
            int a = (int) (tint.getAlpha() * (0.30 + 0.25 * i));
            b.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), Math.min(255, a)));
            b.fill(new Ellipse2D.Double(-len * 0.5 * k, -len * 0.19 * k, len * k, len * 0.38 * k));
        }
        Color solid = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), (int) (tint.getAlpha() * 0.8));
        b.setColor(solid);
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(-len * 0.40, 0);
        tail.lineTo(-len * 0.74, -len * 0.18);
        tail.lineTo(-len * 0.66, 0);
        tail.lineTo(-len * 0.74, len * 0.18);
        tail.closePath();
        b.fill(tail);
        b.dispose();
    }

    private void drawFish3D(Graphics2D g) {
        projected.clear();
        java.util.List<Fish> list = game.fishSnapshot();
        list.sort((a, b) -> Double.compare(b.getX(), a.getX()));     // far first, near last
        for (Fish f : list) {
            Point2D.Double p = Perspective.project(f.getX(), f.getY(), f.getLane());
            double s = Perspective.scale(f.getX());
            projected.put(f, p);
            // direction on the screen: sideways drift to the left/right, and swimming away = up the screen
            double sx = f.getLaneVel() * Perspective.LANE_WIDTH * s, sy = -f.getDisplayVx() * 0.317;
            boolean right = Math.abs(sx) < 0.5 ? f.facingRight() : sx > 0;
            double angle = Math.max(-1.2, Math.min(1.2, Math.atan2(sy, Math.max(Math.abs(sx), 1e-3))));
            if (!right) angle = -angle;
            double depth = (f.getY() - SURFACE) / (double) Math.max(1, game.maxDepth());
            float alpha = (float) Math.max(0.35, Math.min(1, 1.0 - depth * 0.45 - (1 - s) * 0.25));
            Graphics2D fg = (Graphics2D) g.create();
            fg.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            drawShadowBlob(fg, p.x, p.y, f.getLength() * s * 1.5, right, angle, shadowTint(f.getY()));
            fg.dispose();
        }
    }

    /** Foreground: a wooden dock running toward the vanishing point, the angler seen from behind. */
    private void drawDock3D(Graphics2D g, double t) {
        double vx = Perspective.CENTER_X, vy = Perspective.HORIZON;
        int top = 430;
        double k = (H - top) / (H - vy);                 // how far up toward the vanishing point
        double bl = 150, br = 710;
        double tl = bl + (vx - bl) * k, tr = br + (vx - br) * k;
        // posts going into the water under the dock end
        g.setColor(new Color(85, 55, 30));
        g.fillRect((int) tl + 4, top, 12, 46);
        g.fillRect((int) tr - 16, top, 12, 46);
        Polygon deck = new Polygon(new int[]{(int) bl, (int) br, (int) tr, (int) tl}, new int[]{H, H, top, top}, 4);
        g.setPaint(new GradientPaint(0, top, Theme.WOOD_LIGHT, 0, H, Theme.WOOD));
        g.fillPolygon(deck);
        // boards: seams that converge to the vanishing point + cross joints spaced by perspective
        g.setColor(new Color(90, 55, 28, 150));
        for (int i = 1; i < 7; i++) {
            double fx = bl + (br - bl) * i / 7.0;
            double tx = tl + (tr - tl) * i / 7.0;
            g.drawLine((int) fx, H, (int) tx, top);
        }
        for (int i = 1; i < 4; i++) {
            double y = top + (H - top) * Math.pow(i / 4.0, 1.6);
            double kk = (H - y) / (H - vy);
            g.drawLine((int) (bl + (vx - bl) * kk), (int) y, (int) (br + (vx - br) * kk), (int) y);
        }
        g.setColor(Theme.WOOD_DARK);
        g.setStroke(new BasicStroke(2.5f));
        g.drawPolygon(deck);
        g.setStroke(new BasicStroke(1f));

        // angler from behind, sitting at the end of the dock
        LakeGame.Phase ph = game.getPhase();
        LakeGame.Mood mood = game.getMood();
        double tension = ph == LakeGame.Phase.REELING ? game.getTension() : 0;
        int bounce = mood == LakeGame.Mood.HAPPY ? (int) (Math.abs(Math.sin(t * 8)) * 8) : 0;
        int slump = mood == LakeGame.Mood.SAD ? 6 : 0;
        int ax = (int) vx - 40 - (int) (tension * 6), ay = top + 34 - bounce;
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(ax - 40, top + 30, 80, 16));
        // the angler is a fish, seen from behind: tail fin fanned out at the bottom, dorsal ridge down the back
        g.setColor(FISH_FIN);
        g.fillPolygon(new int[]{ax - 9, ax + 9, ax + 34, ax + 14, ax, ax - 14, ax - 34},
                new int[]{ay - 34, ay - 34, ay - 2, ay + 4, ay - 4, ay + 4, ay - 2}, 7);   // tail fin
        g.setColor(FISH_BODY);
        g.fill(new Ellipse2D.Double(ax - 26, ay - 74, 52, 72));       // body
        g.setColor(FISH_FIN);
        g.fillRoundRect(ax - 4, ay - 98 + slump, 8, 64, 6, 6);        // dorsal ridge
        g.setColor(new Color(225, 160, 60));
        g.fillRoundRect(ax - 20, ay - 52, 40, 26, 12, 12);            // vest
        g.setColor(new Color(200, 135, 45));
        g.drawLine(ax, ay - 50, ax, ay - 28);
        g.setColor(FISH_BODY);
        g.fill(new Ellipse2D.Double(ax - 19, ay - 98 + slump, 38, 36));   // head (back)
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(ax - 25, ay - 90 + slump, 11, 11));   // side eyes bulge out
        g.fill(new Ellipse2D.Double(ax + 14, ay - 90 + slump, 11, 11));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(ax - 22, ay - 87 + slump, 5, 5));
        g.fill(new Ellipse2D.Double(ax + 17, ay - 87 + slump, 5, 5));
        g.setColor(new Color(235, 195, 70));
        g.fill(new Ellipse2D.Double(ax - 30, ay - 92 + slump, 60, 16));   // hat brim
        g.fillRoundRect(ax - 15, ay - 112 + slump, 30, 24, 12, 12);      // hat crown
        g.setColor(new Color(200, 70, 60));
        g.fillRect(ax - 15, ay - 93 + slump, 30, 4);
        // bucket with today's catch count
        int bx = ax + 62, by = top + 28;
        g.setColor(new Color(110, 130, 150));
        g.fillPolygon(new int[]{bx - 15, bx + 15, bx + 12, bx - 12}, new int[]{by - 22, by - 22, by + 6, by + 6}, 4);
        g.setColor(new Color(70, 90, 110));
        g.drawArc(bx - 15, by - 34, 30, 22, 0, 180);
        g.setFont(Theme.title(13));
        g.setColor(Color.WHITE);
        String n = String.valueOf(game.getAngler().getCatches().size());
        g.drawString(n, bx - g.getFontMetrics().stringWidth(n) / 2, by - 4);

        // rod: hands at the right shoulder
        double hx = ax + 22, hy = ay - 40;
        double tipX = vx + 70, tipY = 236 + tension * 50;
        if (ph == LakeGame.Phase.CHARGING) {
            double p = game.getAimPower();
            tipX = hx - 30 - p * 60;
            tipY = hy - 150 + p * 20;
        } else if (ph == LakeGame.Phase.CASTING) {
            double s = Math.min(1, game.getCastProgress() * 2.5);
            tipX = (hx - 70) + (vx + 70 - (hx - 70)) * s;
            tipY = (hy - 140) + (236 - (hy - 140)) * s;
        } else if (mood == LakeGame.Mood.HAPPY) {
            tipX = hx + 40;
            tipY = hy - 170;
        }
        g.setColor(new Color(55, 35, 18));
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(hx, hy, (hx + tipX) / 2, (hy + tipY) / 2 - 12 + tension * 40, tipX, tipY));
        g.setStroke(new BasicStroke(1f));
        g.setColor(FISH_BODY);
        g.fill(new Ellipse2D.Double(hx - 7, hy - 7, 14, 14));         // fin gripping the rod
        rodTipX = tipX;
        rodTipY = tipY;
    }

    private void drawLine3D(Graphics2D g, double t) {
        LakeGame.Phase ph = game.getPhase();
        if (ph == LakeGame.Phase.IDLE || ph == LakeGame.Phase.CHARGING
                || ph == LakeGame.Phase.CAUGHT) return;
        double tension = ph == LakeGame.Phase.REELING ? game.getTension() : 0;
        double s;
        Point2D.Double h;
        if (ph == LakeGame.Phase.CASTING) {                              // the hook itself flies through the air
            h = Perspective.project(game.getBobberX(), game.getBobberY(), 0);
            s = Perspective.scale(game.getBobberX());
        } else {
            h = Perspective.project(game.getHookX(), game.getHookY(), 0);
            s = Perspective.scale(game.getHookX());
        }
        g.setColor(new Color(245, 245, 245, 210));
        g.setStroke(new BasicStroke(1.3f));
        double sag = ph == LakeGame.Phase.CASTING ? 30 : 40 * (1 - Math.min(1, tension));
        g.draw(new QuadCurve2D.Double(rodTipX, rodTipY, (rodTipX + h.x) / 2, Math.max(rodTipY, h.y) + sag - 20, h.x, h.y));
        if (ph != LakeGame.Phase.REELING) drawHookSprite(g, h.x, h.y, Math.max(0.6, s * 1.5));
        g.setStroke(new BasicStroke(1f));
    }

    /** The lure at the end of the line: a coloured jig. The colour is the lure class; you never see a fish here. */
    private void drawHookSprite(Graphics2D g, double hx, double hy, double k) {
        Angler a = game.getAngler();
        Color c = a.isLive() ? Bait.colorOfTier(a.getLive().species().getTier()) : a.getBait().getColor();
        int tier = a.isLive() ? a.getLive().species().getTier() : Math.min(a.getBait().getTier(), 4);
        double r = (6 + (tier == 9 ? 2 : tier) * 1.6) * k, tc = clock();
        Graphics2D f = (Graphics2D) g.create();
        f.translate(hx, hy + r * 0.2);
        f.rotate(Math.sin(tc * 5) * 0.10);
        f.setColor(c.darker());                                             // little skirt of strands
        f.setStroke(new BasicStroke((float) Math.max(1.2, r * 0.18), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -1; i <= 1; i++) {
            f.draw(new java.awt.geom.QuadCurve2D.Double(i * r * 0.35, r * 0.55, i * r * 0.75 + Math.sin(tc * 8 + i) * r * 0.2, r * 1.2, i * r * 0.55, r * 1.9));
        }
        f.setColor(c);                                                      // glossy body
        f.fill(new Ellipse2D.Double(-r * 0.8, -r * 0.4, r * 1.6, r * 1.5));
        f.setColor(new Color(255, 255, 255, 150));
        f.fill(new Ellipse2D.Double(-r * 0.45, -r * 0.2, r * 0.55, r * 0.4));
        f.setColor(Color.WHITE);                                            // eye bead: a lure, not a fish
        f.fill(new Ellipse2D.Double(r * 0.05, r * 0.15, r * 0.5, r * 0.5));
        f.setColor(Color.BLACK);
        f.fill(new Ellipse2D.Double(r * 0.2, r * 0.3, r * 0.22, r * 0.22));
        f.dispose();
    }

    // =====================================================================
    // foreground layer (Dave-the-Diver style): big blurry plants drawn IN FRONT of the fish
    // =====================================================================
    private static final double P_FRONT = 1.3;     // moves more than every other layer -> feels closest

    private void drawForeground(Graphics2D g, double t) {
        Location loc = game.getSettings().location();
        boolean night = game.daylight() < 0.3;
        Color leaf = loc == Location.LAGOON ? new Color(45, 150, 140) : loc == Location.SEA ? new Color(40, 120, 90) : new Color(35, 105, 55);
        if (night) leaf = mix(leaf, Color.BLACK, 0.45);
        Graphics2D f = (Graphics2D) g.create();
        f.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!render3D) {
            // soft bubbles of light that drift up (out of focus = close to the camera)
            for (int i = 0; i < 7; i++) {
                double x = par((i * 137 + t * (4 + i)) % (W + 60) - 30, P_FRONT);
                double y = H - ((t * (14 + i * 4) + i * 90) % (H - SURFACE)) ;
                double r = 12 + (i * 7) % 18;
                f.setColor(new Color(255, 255, 255, 16));
                f.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
                f.setColor(new Color(255, 255, 255, 30));
                f.setStroke(new BasicStroke(1.2f));
                f.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            }
            // fat kelp fronds growing from the bottom, swaying; two shades so they overlap with depth
            int[] xs = {-10, 62, 150, 330, 620, 705, 790};
            for (int i = 0; i < xs.length; i++) {
                double x = par(xs[i], P_FRONT);
                double h = 130 + (i * 53) % 110;
                double sway = Math.sin(t * 0.9 + i * 1.7) * (14 + i % 3 * 5);
                f.setColor(mix(leaf, Color.BLACK, (i % 2) * 0.25));
                f.setStroke(new BasicStroke(10f + (i % 3) * 3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                java.awt.geom.Path2D.Double frond = new java.awt.geom.Path2D.Double();
                frond.moveTo(x, H + 6);
                frond.curveTo(x + sway * 0.4, H - h * 0.35, x - sway, H - h * 0.7, x + sway * 0.8, H - h);
                f.draw(frond);
                // leaves
                f.setStroke(new BasicStroke(1f));
                for (int k = 1; k <= 3; k++) {
                    double ly = H - h * k / 4.0, lx = x + sway * k / 4.0 * (k % 2 == 0 ? -1 : 1);
                    f.fill(new Ellipse2D.Double(lx - 14 * (k % 2 == 0 ? 1.4 : 0.6), ly - 6, 28, 12));
                }
            }
            // depth fog at the bottom edge
            f.setPaint(new GradientPaint(0, BOTTOM - 150, new Color(0, 0, 0, 0), 0, H, new Color(0, 10, 20, night ? 130 : 90)));
            f.fillRect(0, BOTTOM - 150, W, H - BOTTOM + 150);
        } else {
            // 3-D view: reeds at both screen edges, taller and thicker than anything else
            int[] xs = {8, 38, 72, 112, 700, 740, 774, 800};
            for (int i = 0; i < xs.length; i++) {
                double x = par(xs[i], P_FRONT);
                double h = 170 + (i * 47) % 90;
                double sway = Math.sin(t * 1.1 + i * 2.1) * 10;
                f.setColor(mix(leaf, new Color(190, 200, 110), 0.12 * (i % 3)));
                f.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                f.draw(new QuadCurve2D.Double(x, H + 4, x + sway * 0.3, H - h * 0.55, x + sway + (xs[i] < W / 2 ? 14 : -14), H - h));
                if (i % 3 == 1) {   // a cattail on top
                    f.setColor(new Color(110, 70, 40));
                    f.fill(new java.awt.geom.RoundRectangle2D.Double(x + sway + (xs[i] < W / 2 ? 14 : -14) - 4, H - h - 18, 9, 26, 6, 6));
                }
            }
            f.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, H, new Color(0, 15, 25, 70)));
            f.fillRect(0, H / 2, W, H / 2);
        }
        f.dispose();
    }

    // ---------------- dock and angler (the angler is a fish) ----------------
    private static final Color FISH_BODY = new Color(70, 165, 205);
    private static final Color FISH_BELLY = new Color(205, 238, 243);
    private static final Color FISH_FIN = new Color(35, 115, 160);

    private void drawDock(Graphics2D g, double t) {
        int floorY = game.bottomY();                       // the posts stand on the lake floor
        g.setPaint(new GradientPaint(0, SURFACE - 22, new Color(90, 60, 35), 0, floorY, new Color(34, 26, 20)));
        for (int x = 20; x <= LakeGame.SHORE_X; x += 55) g.fillRect(x, SURFACE - 22, 10, floorY - (SURFACE - 22));
        Theme.paintWood(g, -4, SURFACE - 32, (int) LakeGame.SHORE_X + 18, 14, 6);

        LakeGame.Phase ph = game.getPhase();
        LakeGame.Mood mood = game.getMood();
        double tension = ph == LakeGame.Phase.REELING ? game.getTension() : 0;
        int bounce = mood == LakeGame.Mood.HAPPY ? (int) (Math.abs(Math.sin(t * 8)) * 6) : 0;
        int lean = (int) (-tension * 6);
        int ax = 105 + lean, ay = SURFACE - 32 - bounce;

        // a fish standing upright, facing the lake: tail fin behind, flippers for feet
        g.setColor(FISH_FIN);
        g.fillPolygon(new int[]{ax - 12, ax - 34, ax - 28, ax - 36, ax - 12},
                new int[]{ay - 34, ay - 50, ay - 34, ay - 16, ay - 28}, 5);          // tail fin
        g.fill(new Ellipse2D.Double(ax - 10, ay - 12 + bounce, 16, 8));                // flippers
        g.fill(new Ellipse2D.Double(ax + 6, ay - 12 + bounce, 18, 8));
        g.setColor(FISH_BODY);
        g.fill(new Ellipse2D.Double(ax - 17, ay - 68, 40, 62));                        // body
        g.setColor(FISH_BELLY);
        g.fill(new java.awt.geom.Arc2D.Double(ax - 11, ay - 56, 32, 48, -80, 160, java.awt.geom.Arc2D.CHORD));
        g.setColor(FISH_FIN);
        g.fillPolygon(new int[]{ax - 8, ax - 2, ax + 6}, new int[]{ay - 64, ay - 80, ay - 64}, 3);   // dorsal fin
        // vest
        g.setColor(new Color(225, 160, 60));
        g.fillRoundRect(ax - 11, ay - 36, 24, 18, 8, 8);
        // hat
        g.setColor(new Color(235, 195, 70));
        g.fillRoundRect(ax - 19, ay - 66, 38, 7, 6, 6);
        g.fillRoundRect(ax - 10, ay - 79, 20, 15, 8, 8);
        g.setColor(new Color(200, 70, 60));
        g.fillRect(ax - 10, ay - 68, 20, 3);
        drawFace(g, ax, ay, mood, t);

        // rod: pulled back while aiming, swings forward while casting, bends under tension
        double handX = ax + 16, handY = ay - 26;
        double tipX = LakeGame.ROD_X, tipY = LakeGame.ROD_Y + tension * 35;
        if (ph == LakeGame.Phase.CHARGING) {
            double p = game.getAimPower();
            tipX = handX - 10 - p * 50;
            tipY = handY - 70 + p * 10;
        } else if (ph == LakeGame.Phase.CASTING) {
            double s = Math.min(1, game.getCastProgress() * 2.5);   // fast swing at the start of the throw
            tipX = (handX - 40) + (LakeGame.ROD_X - (handX - 40)) * s;
            tipY = (handY - 65) + (LakeGame.ROD_Y - (handY - 65)) * s;
        } else if (mood == LakeGame.Mood.HAPPY) {
            tipX = handX + 20;
            tipY = handY - 75;
        }
        g.setColor(new Color(60, 40, 20));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(handX, handY, (handX + tipX) / 2, (handY + tipY) / 2 - 10 + tension * 25, tipX, tipY));
        // reel with a crank that spins while reeling
        double crank = game.isReeling() ? t * 14 : 0.6;
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(150, 150, 160));
        g.fill(new Ellipse2D.Double(handX - 7, handY - 2, 10, 10));
        g.setColor(new Color(60, 60, 70));
        g.drawLine((int) handX - 2, (int) handY + 3, (int) (handX - 2 + Math.cos(crank) * 8), (int) (handY + 3 + Math.sin(crank) * 8));
        g.setColor(FISH_FIN);
        g.fill(new Ellipse2D.Double(handX - 2 + Math.cos(crank) * 8 - 3, handY + 3 + Math.sin(crank) * 8 - 3, 8, 8));   // fin on the crank
        g.setStroke(new BasicStroke(1f));
    }

    private void drawFace(Graphics2D g, int ax, int ay, LakeGame.Mood mood, double t) {
        int ex = ax + 2, ey = ay - 56;
        // fish eye: white ball with a pupil that looks at the lake (shifts with the mood)
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(ex + 2, ey - 3, 13, 13));
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Double(ex + 2, ey - 3, 13, 13));
        int py = mood == LakeGame.Mood.SAD ? 4 : 2;
        g.fill(new Ellipse2D.Double(ex + 8, ey + py - 1, 5, 5));
        ex += 6; ey += 6;                                  // the mouth and brows sit at the fish's snout
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (mood) {
            case HAPPY -> {
                g.drawArc(ex, ey - 1, 5, 5, 0, 180);          // ^ ^ eyes
                g.drawArc(ex + 6, ey - 1, 5, 5, 0, 180);
                g.drawArc(ex + 1, ey + 3, 9, 7, 180, 180);    // big smile
                g.setColor(new Color(255, 140, 140, 160));
                g.fillOval(ex - 4, ey + 4, 5, 3);
            }
            case SAD -> {
                g.fillOval(ex + 1, ey, 3, 3);
                g.fillOval(ex + 7, ey, 3, 3);
                g.drawArc(ex + 2, ey + 7, 7, 5, 0, 180);      // frown
                g.setColor(new Color(120, 180, 255));
                g.fillOval(ex + 10, ey + 3 + (int) ((t * 20) % 8), 3, 4);   // tear
            }
            case FOCUS -> {
                g.drawLine(ex, ey + 1, ex + 4, ey + 2);       // determined eyes
                g.drawLine(ex + 7, ey + 2, ex + 11, ey + 1);
                g.drawLine(ex + 3, ey + 8, ex + 9, ey + 8);
                g.setColor(new Color(150, 200, 255));
                g.fill(new Ellipse2D.Double(ax - 13, ay - 62, 4, 6));      // sweat drop
            }
            default -> {
                g.fillOval(ex + 1, ey, 3, 3);
                g.fillOval(ex + 7, ey, 3, 3);
                g.drawArc(ex + 2, ey + 4, 7, 5, 200, 140);
            }
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawLine(Graphics2D g) {
        LakeGame.Phase ph = game.getPhase();
        if (ph == LakeGame.Phase.IDLE || ph == LakeGame.Phase.CHARGING
                || ph == LakeGame.Phase.CAUGHT) return;
        double tension = ph == LakeGame.Phase.REELING ? game.getTension() : 0;
        double tipX = LakeGame.ROD_X, tipY = LakeGame.ROD_Y + tension * 35;
        if (ph == LakeGame.Phase.CASTING) {
            double s = Math.min(1, game.getCastProgress() * 2.5);
            double handX = 121, handY = SURFACE - 58;
            tipX = (handX - 40) + (LakeGame.ROD_X - (handX - 40)) * s;
            tipY = (handY - 65) + (LakeGame.ROD_Y - (handY - 65)) * s;
        }
        // the line runs straight from the rod to the hook; a nibble or bite makes the hook twitch
        double hx = game.getHookX(), hy = game.getHookY();
        if (ph == LakeGame.Phase.CASTING) {
            hx = game.getBobberX();
            hy = game.getBobberY();                                         // the hook flies through the air
        } else if (ph == LakeGame.Phase.WAITING || ph == LakeGame.Phase.BITE) {
            hy += (game.getBobberY() - (SURFACE - 3)) * 1.5;
        }
        g.setColor(new Color(240, 240, 240, 200));
        g.setStroke(new BasicStroke(1.2f));
        double sag = ph == LakeGame.Phase.REELING ? 30 * (1 - Math.min(1, tension))
                : ph == LakeGame.Phase.CASTING ? 35 : 8;
        g.draw(new QuadCurve2D.Double(tipX, tipY, (tipX + hx) / 2, (tipY + hy) / 2 + sag, hx, hy));
        g.setStroke(new BasicStroke(1f));
        if (ph != LakeGame.Phase.REELING) drawHookSprite(g, hx, hy, 1);
    }

    private void drawRain(Graphics2D g, double t) {
        g.setColor(new Color(200, 210, 230, 140));
        Random r = new Random(3);
        for (int i = 0; i < 120; i++) {
            double x = (r.nextInt(W + 100) + t * 60) % (W + 100) - 50;
            double y = (r.nextInt(H) + t * (400 + r.nextInt(200))) % H;
            g.drawLine((int) x, (int) y, (int) (x - 4), (int) (y + 12));
        }
    }

    // ---------------- HUD: hanging wooden sign + depth stick ----------------
    private void drawHud(Graphics2D g, double t) {
        Angler a = game.getAngler();
        int played = (int) game.getElapsed();
        int sx = W - 222, sy = 18, sw = 208, sh = 86;
        double swing = Math.sin(t * 1.2) * 1.5;      // the sign sways a little
        Graphics2D s = (Graphics2D) g.create();
        s.rotate(Math.toRadians(swing), sx + sw / 2.0, 0);
        s.setColor(new Color(80, 55, 30));
        s.setStroke(new BasicStroke(2f));
        s.drawLine(sx + 30, 0, sx + 30, sy + 6);
        s.drawLine(sx + sw - 30, 0, sx + sw - 30, sy + 6);
        Theme.paintWood(s, sx, sy, sw, sh, 14);
        s.setColor(new Color(70, 40, 20));
        s.fillOval(sx + 27, sy + 4, 6, 6);
        s.fillOval(sx + sw - 33, sy + 4, 6, 6);
        s.setFont(Theme.title(20));
        s.setColor(new Color(255, 225, 120));
        s.drawString("$" + a.getMoney(), sx + 14, sy + 38);
        s.setColor(Theme.PAPER);
        String time = String.format("%d:%02d", played / 60, played % 60);
        FontMetrics fm = s.getFontMetrics();
        s.drawString(time, sx + sw - 14 - fm.stringWidth(time), sy + 38);
        s.setFont(Theme.body(Font.BOLD, 12));
        String weather = game.isRaining() ? "Rain" : (game.isNight() ? "Night" : "Sunny");
        s.drawString(game.clockText() + "  -  " + weather, sx + 14, sy + 58);
        String baitText = "Bait: " + a.baitShort();
        FontMetrics bfm = s.getFontMetrics();
        while (bfm.stringWidth(baitText) > sw - 62 && baitText.length() > 8) baitText = baitText.substring(0, baitText.length() - 1);
        s.drawString(baitText, sx + 14, sy + 76);
        s.setColor(a.baitColor());
        s.fill(new Ellipse2D.Double(sx + sw - 30, sy + 64, 14, 14));
        s.dispose();

        LakeGame.Phase ph = game.getPhase();
        if (ph != LakeGame.Phase.REELING && ph != LakeGame.Phase.CAUGHT) {
            int gx = 24, y0 = SURFACE + 22, y1 = BOTTOM - 14;
            Theme.paintWood(g, gx - 7, y0 - 6, 14, y1 - y0 + 12, 6);
            g.setColor(Theme.PAPER);
            int maxD = game.maxDepth();
            int tick = maxD > 2000 ? 500 : maxD > 1000 ? 200 : 50;                  // a deep spot gets coarser marks on the depth stick
            for (int d = 0; d <= maxD; d += tick) {
                int y = (int) (y0 + (y1 - y0) * (double) d / maxD);
                g.drawLine(gx - 5, y, gx + 5, y);
            }
            int depth = game.getHookDepth();
            int my = (int) Math.round(y0 + (y1 - y0) * game.getHookDepthF() / maxD);
            Theme.paintPaper(g, gx + 12, my - 11, 62, 22, 10);
            g.setColor(Theme.INK);
            g.setFont(SMALL);
            g.drawString(depth + " cm", gx + 20, my + 5);
            g.setColor(new Color(230, 60, 50));
            g.fillPolygon(new int[]{gx + 4, gx + 13, gx + 13}, new int[]{my, my - 6, my + 6}, 3);
            g.setFont(Theme.body(Font.BOLD, 11));
            g.setColor(Color.WHITE);
            g.drawString("DEPTH", gx - 14, y0 - 12);
            g.drawString("Up pull", gx - 14, y1 + 22);
            g.drawString("Down slow", gx - 14, y1 + 36);
        }
    }

    /** Paper speech bubble with a hint. */
    private void hint(Graphics2D g, String text, Color textColor, int y) {
        g.setFont(HINT);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text) + 28;
        int x = (W - w) / 2 + 40;
        Theme.paintPaper(g, x, y - 20, w, 30, 16);
        g.setColor(textColor);
        g.drawString(text, x + 14, y);
    }

    private void drawPhaseOverlay(Graphics2D g, double t) {
        switch (game.getPhase()) {
            case IDLE -> {
                int alpha = (int) (200 + 55 * Math.sin(t * 3));
                hint(g, "Click (or press SPACE) to start casting", new Color(72, 46, 26, alpha), 130);
            }
            case CHARGING, CASTING -> drawPower(g, t);
            case WAITING -> hint(g, "Wait for a bite - the fish hooks itself. HOLD SPACE / mouse to reel! (Down = slow swing)", Theme.INK, 130);
            case BITE -> {
                if ((int) (t * 8) % 2 == 0) {
                    g.setFont(Theme.title(40));
                    g.setColor(new Color(240, 60, 40));
                    g.drawString("!", (int) Math.max(10, Math.min(W - 24, screenX(game.getHookX()) - 6)),
                            (int) Math.max(50, Math.min(H - 20, screenY(game.getHookY()) - 30)));
                }
                hint(g, "BITE!  CLICK  /  SPACE  NOW!", Theme.DANGER, 130);
            }
            case REELING -> drawReeling(g, t);
            case CAUGHT -> drawCatch(g, t);
        }
    }

    /**
     * Cast power bar: a vertical green -> red bar with a marker that sweeps up and down.
     * Click 2 (or Space) stops it; the power at that moment decides how far the line goes.
     */
    private void drawPower(Graphics2D g, double t) {
        boolean casting = game.getPhase() == LakeGame.Phase.CASTING;
        double fade = Math.min(1, game.getChargeT() / LakeGame.ZOOM_TIME);      // bar fades in while the camera zooms
        double p = game.getAimPower();
        int bx = W - 74, by = 150, bw = 34, bh = 262;
        Graphics2D k = (Graphics2D) g.create();
        k.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (casting ? 1 : fade)));
        Theme.paintWood(k, bx - 18, by - 38, bw + 36, bh + 96, 16);
        k.setFont(Theme.title(15));
        k.setColor(Theme.PAPER);
        FontMetrics fm = k.getFontMetrics();
        k.drawString("POWER", bx + bw / 2 - fm.stringWidth("POWER") / 2, by - 14);
        // the bar: green (weak) at the bottom -> yellow -> red (strong) at the top
        k.setColor(new Color(50, 35, 20));
        k.fillRoundRect(bx - 4, by - 4, bw + 8, bh + 8, 12, 12);
        k.setPaint(new GradientPaint(0, by + bh, new Color(70, 200, 95), 0, by, new Color(225, 60, 50)));
        k.fillRoundRect(bx, by, bw, bh, 8, 8);
        k.setColor(new Color(255, 255, 255, 70));
        for (int i = 1; i < 10; i++) {            // scale ticks
            int ty = by + bh - bh * i / 10;
            k.drawLine(bx, ty, bx + (i % 5 == 0 ? 14 : 8), ty);
        }
        // marker line with arrows on both sides
        double my = by + bh - bh * p;
        k.setColor(Color.WHITE);
        k.setStroke(new BasicStroke(3.5f));
        k.draw(new Line2D.Double(bx - 8, my, bx + bw + 8, my));
        k.setStroke(new BasicStroke(1f));
        k.fillPolygon(new int[]{bx - 15, bx - 15, bx - 5}, new int[]{(int) my - 7, (int) my + 7, (int) my}, 3);
        k.fillPolygon(new int[]{bx + bw + 15, bx + bw + 15, bx + bw + 5}, new int[]{(int) my - 7, (int) my + 7, (int) my}, 3);
        // percentage
        k.setFont(Theme.title(20));
        fm = k.getFontMetrics();
        String pct = Math.round(p * 100) + "%";
        k.setColor(casting ? new Color(255, 225, 120) : Theme.PAPER);
        k.drawString(pct, bx + bw / 2 - fm.stringWidth(pct) / 2, by + bh + 32);
        k.dispose();
        if (casting) hint(g, "Power " + Math.round(p * 100) + "%!", Theme.INK, 130);
        else if (game.isChargeReady()) hint(g, "Click again (or SPACE) to stop the bar!", Theme.INK, 130);
    }

    // ---------------- reel gauge (tension dial) ----------------
    private void drawReeling(Graphics2D g, double t) {
        double ten = game.getTension(), limit = game.getTensionLimit();
        double max = 1.25;                       // dial shows 0 .. 1.25
        int cx = W - 92, cy = 250, r = 70;
        Theme.paintWood(g, cx - r - 18, cy - r - 30, 2 * r + 36, r + 104, 20);
        g.setFont(Theme.title(14));
        g.setColor(Theme.PAPER);
        FontMetrics fm = g.getFontMetrics();
        g.drawString("TENSION", cx - fm.stringWidth("TENSION") / 2, cy - r - 10);
        // coloured zones: angle 180 (left) = 0 tension, angle 0 (right) = max
        double[] stops = {0, 0.12, limit - 0.15, limit, max};
        Color[] zone = {new Color(150, 150, 155), new Color(80, 190, 95), new Color(240, 195, 60), new Color(215, 70, 55)};
        g.setStroke(new BasicStroke(16f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < zone.length; i++) {
            double a0 = 180 - stops[i] / max * 180, a1 = 180 - stops[i + 1] / max * 180;
            g.setColor(zone[i]);
            g.draw(new Arc2D.Double(cx - r + 8, cy - r + 8, 2 * r - 16, 2 * r - 16, a1, a0 - a1, Arc2D.OPEN));
        }
        // needle
        double ang = Math.toRadians(180 - Math.min(max, ten) / max * 180);
        double shake = ten > limit * 0.85 ? Math.sin(t * 60) * 0.04 : 0;
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(40, 30, 20));
        g.draw(new Line2D.Double(cx, cy, cx + Math.cos(ang + shake) * (r - 14), cy - Math.sin(ang + shake) * (r - 14)));
        // reel hub with spinning handle
        g.setColor(new Color(170, 170, 180));
        g.fill(new Ellipse2D.Double(cx - 16, cy - 16, 32, 32));
        g.setColor(new Color(90, 90, 100));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Ellipse2D.Double(cx - 16, cy - 16, 32, 32));
        double spin = game.isReeling() ? t * 12 : 0;
        g.drawLine(cx, cy, (int) (cx + Math.cos(spin) * 22), (int) (cy + Math.sin(spin) * 22));
        g.setColor(Theme.WOOD_DARK);
        g.fill(new Ellipse2D.Double(cx + Math.cos(spin) * 22 - 5, cy + Math.sin(spin) * 22 - 5, 10, 10));
        g.setFont(Theme.body(Font.BOLD, 12));
        g.setColor(Theme.PAPER);
        String state = game.isReeling() ? "REELING!" : "hold to reel";
        fm = g.getFontMetrics();
        g.drawString(state, cx - fm.stringWidth(state) / 2, cy + 44);

        // distance: rope from the fish to the dock
        double prog = Math.max(0, game.getLineProgress());
        int px = 210, py = BOTTOM - 10, pw = 380;
        Theme.paintPaper(g, px - 14, py - 24, pw + 28, 36, 14);
        g.setColor(new Color(150, 110, 70));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10, new float[]{6, 4}, 0));
        g.drawLine(px, py - 6, px + pw, py - 6);
        g.setStroke(new BasicStroke(1f));
        Fish f = game.getReelFish();
        if (f != null) FishArt.draw(g, f.getSpecies(), px + pw - pw * prog, py - 6, 30, false, t * 10);
        g.setColor(Theme.WOOD_DARK);
        g.fillRect(px - 6, py - 14, 6, 16);
        g.setFont(Theme.body(Font.BOLD, 11));
        g.setColor(Theme.INK);
        g.drawString("dock", px - 4, py - 16);

        boolean flash = (int) (t * 6) % 2 == 0;
        if (ten > limit * 0.85) {
            if (flash) hint(g, "TOO TIGHT!  LET GO!", Theme.DANGER, 130);
        } else if (game.getSlackDanger() > 0.35) {
            if (flash) hint(g, "Line is slack - HOLD to reel!", new Color(190, 120, 0), 130);
        } else if (game.isBurst()) {
            hint(g, "The fish is fighting hard!", new Color(190, 90, 30), 130);
        } else {
            hint(g, "Hold SPACE / mouse to reel. Keep the needle in the GREEN.", Theme.INK, 130);
        }
    }

    // ---------------- catch: fish leaps out, then the card pops ----------------
    private void drawCatch(Graphics2D g, double t) {
        CatchRecord c = game.getLastCatch();
        if (c == null) return;
        double ct = game.getCardT();
        if (ct < JUMP_TIME) {
            double p = ct / JUMP_TIME;
            double len = Math.min(120, 30 + c.sizeCm() * 0.5);
            double x, y;
            {   // the catch always happens in the side view: the fish leaps toward the angler
                double sx = game.getJumpX(), ex = 150;
                x = sx + (ex - sx) * p;
                y = SURFACE + (SURFACE - 70 - SURFACE) * p - Math.sin(Math.PI * p) * 140;
            }
            Graphics2D f = (Graphics2D) g.create();
            f.rotate(-Math.PI * 2 * p * 0.5 + Math.PI * 0.25, x, y);   // tumbling leap
            FishArt.draw(f, c.species(), x, y, len, false, t * 14);
            f.dispose();
            for (int i = 0; i < 6; i++) {       // water drops flying off
                double dx = Math.cos(i * 1.1 + p * 5) * 20 * p, dy = Math.sin(i * 1.7) * 14 * p;
                g.setColor(new Color(200, 230, 255, (int) (200 * (1 - p))));
                g.fill(new Ellipse2D.Double(x + dx - 3, y + dy + 10, 5, 7));
            }
            return;
        }
        double pop = Math.min(1, (ct - JUMP_TIME) * 4);
        double scale = 0.6 + 0.4 * pop + (pop < 1 ? Math.sin(pop * Math.PI) * 0.08 : 0);
        int cw = 400, ch = 322;
        int x = (W - cw) / 2 + 40, y = 140;
        Graphics2D k = (Graphics2D) g.create();
        k.translate(x + cw / 2.0, y + ch / 2.0);
        k.scale(scale, scale);
        k.translate(-(x + cw / 2.0), -(y + ch / 2.0));
        boolean special = c.species().isLegendary() || c.newSpecies();
        if (special) drawGlitter(k, x + cw / 2.0, y + ch / 2.0, t, c.species().isLegendary());
        Theme.paintWood(k, x - 10, y - 10, cw + 20, ch + 20, 28);
        Theme.paintPaper(k, x, y, cw, ch, 22);
        // ribbon with the fish name
        k.setFont(Theme.title(22));
        FontMetrics fm = k.getFontMetrics();
        String title = c.species().getTitle();
        int rw = fm.stringWidth(title) + 40;
        Color rib = c.species().isLegendary() ? new Color(220, 160, 30) : Theme.WATER;
        k.setColor(rib);
        k.fillRoundRect(x + (cw - rw) / 2, y - 18, rw, 38, 14, 14);
        k.setColor(rib.darker());
        k.drawRoundRect(x + (cw - rw) / 2, y - 18, rw, 38, 14, 14);
        k.setColor(Theme.PAPER);
        k.drawString(title, x + (cw - fm.stringWidth(title)) / 2, y + 9);
        // fish photo
        k.setPaint(new GradientPaint(0, y + 34, new Color(185, 225, 245), 0, y + 160, new Color(120, 180, 215)));
        k.fillRoundRect(x + 24, y + 34, cw - 48, 126, 18, 18);
        double len = Math.min(200, 70 + c.sizeCm() * 0.9);
        FishArt.draw(k, c.species(), x + cw / 2.0, y + 97, len, true, t * 5, c.eaten());
        k.setFont(Theme.title(20));
        fm = k.getFontMetrics();
        String info = c.sizeCm() + " cm      $" + c.value();
        k.setColor(Theme.INK);
        k.drawString(info, x + (cw - fm.stringWidth(info)) / 2, y + 194);
        k.setFont(Theme.body(Font.BOLD, 13));
        fm = k.getFontMetrics();
        int cls = c.species().getTier();
        String clsText = "Class " + cls + " - " + Species.tierName(cls)
                + (c.eaten() >= 3 ? "   (only bones!)" : c.eaten() > 0 ? "   (bitten x" + c.eaten() + ")" : "");
        k.setColor(new Color(90, 110, 125));
        k.drawString(clsText, x + (cw - fm.stringWidth(clsText)) / 2, y + 210);
        String badge = c.species().isLegendary() ? "LEGENDARY CATCH!" : c.newSpecies() ? "NEW SPECIES!" : c.record() ? "NEW RECORD!" : "";
        if (!badge.isEmpty()) {
            k.setFont(Theme.title(17));
            fm = k.getFontMetrics();
            k.setColor(Theme.DANGER);
            k.drawString(badge, x + (cw - fm.stringWidth(badge)) / 2, y + 238);
        }
        // two choices: sell the fish, or keep it as live bait
        if (pop >= 1) {
            cardRect = new java.awt.Rectangle(x, y, cw, ch);
            cardSell = new java.awt.Rectangle(x + 24, y + 252, 172, 42);
            cardKeep = new java.awt.Rectangle(x + cw - 24 - 172, y + 252, 172, 42);
        } else {
            cardRect = cardSell = cardKeep = null;
        }
        String why = game.keepBlockReason();
        java.awt.Rectangle sellR = new java.awt.Rectangle(x + 24, y + 252, 172, 42);
        java.awt.Rectangle keepR = new java.awt.Rectangle(x + cw - 24 - 172, y + 252, 172, 42);
        drawCardButton(k, sellR, "Sell  $" + c.value() + "  [Space]", Theme.LEAF, cardSell != null && cardSell.contains(mouseX, mouseY), true);
        drawCardButton(k, keepR, "Keep as bait  [B]", new Color(60, 140, 190), cardKeep != null && cardKeep.contains(mouseX, mouseY), why == null);
        k.setFont(Theme.body(Font.ITALIC, 12));
        fm = k.getFontMetrics();
        String hintText = why != null ? why + "." : "Keep it as bait to catch the next size up!";
        k.setColor(why != null ? Theme.DANGER : Theme.INK_SOFT);
        k.drawString(hintText, x + (cw - fm.stringWidth(hintText)) / 2, y + 314);
        k.dispose();
    }

    private java.awt.Rectangle cardRect, cardSell, cardKeep;      // screen rectangles of the catch card (null while it pops up)
    private int mouseX = -1, mouseY = -1;

    private void drawCardButton(Graphics2D g, java.awt.Rectangle r, String text, Color base, boolean hover, boolean enabled) {
        Color c = enabled ? (hover ? base.brighter() : base) : new Color(170, 165, 155);
        g.setColor(c.darker().darker());
        g.fillRoundRect(r.x, r.y + 3, r.width, r.height - 2, 14, 14);
        g.setPaint(new GradientPaint(0, r.y, c.brighter(), 0, r.y + r.height, c));
        g.fillRoundRect(r.x, r.y, r.width, r.height - 3, 14, 14);
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRoundRect(r.x + 4, r.y + 3, r.width - 8, (r.height - 6) / 3, 10, 10);
        g.setFont(Theme.body(Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(enabled ? Theme.PAPER : new Color(235, 230, 220));
        g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2, r.y + r.height / 2 + fm.getAscent() / 2 - 3);
    }

    /** A live bait fish hanging from the hook (its head on the hook, tail down), swaying a little. */
    /** Rotating rays and twinkling stars behind a special catch card. */
    private void drawGlitter(Graphics2D g, double cx, double cy, double t, boolean gold) {
        Color ray = gold ? new Color(255, 220, 90, 70) : new Color(255, 255, 255, 60);
        g.setColor(ray);
        for (int i = 0; i < 12; i++) {
            double a = t * 0.6 + i * Math.PI / 6;
            Polygon p = new Polygon();
            p.addPoint((int) cx, (int) cy);
            p.addPoint((int) (cx + Math.cos(a - 0.08) * 330), (int) (cy + Math.sin(a - 0.08) * 330));
            p.addPoint((int) (cx + Math.cos(a + 0.08) * 330), (int) (cy + Math.sin(a + 0.08) * 330));
            g.fillPolygon(p);
        }
        Random r = new Random(11);
        for (int i = 0; i < 26; i++) {
            double px = cx + (r.nextDouble() - 0.5) * 520, py = cy + (r.nextDouble() - 0.5) * 360;
            double tw = Math.abs(Math.sin(t * 3 + i));
            double s = 3 + tw * 5;
            g.setColor(gold ? new Color(255, 230, 120, (int) (120 + 135 * tw)) : new Color(255, 255, 255, (int) (100 + 155 * tw)));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Line2D.Double(px - s, py, px + s, py));
            g.draw(new Line2D.Double(px, py - s, px, py + s));
        }
        g.setStroke(new BasicStroke(1f));
    }

    private void drawPauseOrEnd(Graphics2D g) {
        String s = null;
        if (game.isOver()) s = "Trip ended";
        else if (loop != null && loop.isPaused()) s = "Paused";
        if (s == null) return;
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRect(0, 0, W, H);
        int bw = 320, bh = 90, bx = (W - bw) / 2, by = H / 2 - 60;
        Theme.paintWood(g, bx, by, bw, bh, 22);
        g.setFont(Theme.title(38));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.PAPER);
        g.drawString(s, (W - fm.stringWidth(s)) / 2, by + 58);
        if (!game.isOver()) {
            g.setFont(Theme.body(Font.BOLD, 13));
            fm = g.getFontMetrics();
            String sub = "press P to keep fishing";
            g.drawString(sub, (W - fm.stringWidth(sub)) / 2, by + bh + 22);
        }
    }

    // =====================================================================
    // mouse handlers
    // =====================================================================

    /** Press: start aiming, strike, start reeling or close the catch card - depending on the phase. */
    // [PRESENT] EVENT MouseListener: click = start power bar / throw / reel / sell or keep the catch
    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        requestFocusInWindow();
        if (game.isOver() || (loop != null && loop.isPaused())) return;
        switch (game.getPhase()) {
            case IDLE -> game.startCharge();          // click 1: zoom in, the power bar starts
            case CHARGING -> game.release();          // click 2: stop the bar and throw
            case REELING -> game.setReelMouse(true);
            case CAUGHT -> {
                if (game.getCardT() < JUMP_TIME + 0.35) return;                    // the card is still popping up
                if (cardKeep != null && cardKeep.contains(e.getPoint())) game.keepCatchAsBait();
                else if (cardRect != null && cardRect.contains(e.getPoint()) && !cardSell.contains(e.getPoint())) return;
                else game.sellCatch();                                             // Sell button, or a click outside the card
            }
            default -> { }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) { }

    /** Releasing the button stops reeling. */
    // [PRESENT] EVENT MouseListener (release): stop reeling
    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        game.setReelMouse(false);
    }

    /** Hover over a fish to see its name (when Fish Finder is on). */
    // [PRESENT] EVENT MouseMotionListener: hover over a fish to see its name (fish finder)
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        Fish best = null;
        double bd = Double.MAX_VALUE;
        for (Fish f : game.fishSnapshot()) {
            double fx = f.getX(), fy = f.getY(), size = f.getLength();
            if (blend >= 0.5) {               // side view: zoomed and centred on the hook
                fx = W / 2.0 + (fx - camCX) * viewZoom;
                fy = H / 2.0 + (fy - camCY) * viewZoom;
                size *= viewZoom;
            }
            if (blend < 0.5) {   // 2.5-D view: compare with where the fish is drawn on screen
                Point2D.Double p = projected.get(f);
                if (p == null) continue;
                fx = p.x;
                fy = p.y;
                size *= Perspective.scale(f.getX()) * 1.5;
            }
            double d = Math.hypot(fx - e.getX(), fy - e.getY());
            if (d < size * 0.5 + 8 && d < bd) {
                bd = d;
                best = f;
            }
        }
        hover = best;
        setCursor(Cursor.getPredefinedCursor(game.getPhase() == LakeGame.Phase.IDLE ? Cursor.CROSSHAIR_CURSOR : Cursor.HAND_CURSOR));
    }

    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { hover = null; }
}
