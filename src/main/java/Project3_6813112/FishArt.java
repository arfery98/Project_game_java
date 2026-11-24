// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/** Draws every species with Java2D shapes (no image files). Tail wags with "anim". */
public final class FishArt {

    private FishArt() { }

    public static void draw(Graphics2D g0, Species s, double cx, double cy, double len, boolean right, double anim) {
        draw(g0, s, cx, cy, len, right, anim, 0);
    }

    private static final Color BONE = new Color(240, 234, 214);

    // [GRAPHICS] ENTRY: draws one fish. Picks a body method by shape, wraps it in Shading, and cuts away parts when the fish was eaten (eaten = 0..3) | SCREEN: Start (title fish) + Setup (fish preview) + Game (lake, How to Play)
    // ไทย: จุดเข้า: วาดปลา 1 ตัว เลือกเมธอดตัวปลาตามรูปร่าง ห่อด้วย Shading และตัดชิ้นส่วนทิ้งเมื่อโดนกัด (eaten = 0..3) | อยู่ที่: หน้า Start (ปลาที่โลโก้) + Setup (รูปปลาตัวอย่าง) + Game (ทะเลสาบ, หน้าวิธีเล่น)
    /**
     * eaten = 0..3: a bigger fish bit the tail (1), then the back half (2), then the rest: only bones (3).
     * The eaten part is drawn as a skeleton.
     */
    public static void draw(Graphics2D g0, Species s, double cx, double cy, double len, boolean right, double anim, int eaten) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(cx, cy);
        if (!right) g.scale(-1, 1);      // all shapes are drawn facing right

        if (s.glows()) {
            Color c = s.getBody();
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            g.fill(new Ellipse2D.Double(-len * 0.7, -len * 0.4, len * 1.4, len * 0.8));
        }
        double cut = eaten <= 0 ? -1e9 : eaten == 1 ? -len * 0.20 : eaten == 2 ? len * 0.12 : 1e9;   // x < cut is eaten
        if (eaten < 3) {
            final double cutX = cut;
            final double an = Shading.steps(anim);          // 8 poses of the tail: each is lit once and then reused
            // the body is painted flat, then Shading adds soft light from above and a thin dark outline
            String key = s.name() + ":" + Math.round(len * 2) + ":" + eaten + ":" + Math.round(an * 100);
            Shading.paint(key, g, -len * 1.05 - 12, -len * 0.55 - 12, len * 2.1 + 24, len * 1.1 + 24, -len * 0.38, len * 0.38,
                    Math.max(1.0, len / 60), ig -> {
                Graphics2D b = (Graphics2D) ig.create();
                if (eaten > 0) b.clip(new java.awt.geom.Rectangle2D.Double(cutX, -len, len * 1.4 - cutX, len * 2));
                switch (s.getShape()) {
                    case FISH -> body(b, s, len, 0.42, an);
                    case LONG -> body(b, s, len, 0.24, an);
                    case EEL -> eel(b, s, len, an);
                    case RAY -> ray(b, s, len, an);
                    case SQUID -> squid(b, s, len, an);
                    case ROUND -> round(b, s, len, an);
                    case TORPEDO -> torpedo(b, s, len, an);
                    case FANTAIL -> fantail(b, s, len, an);
                    case CATFISH -> catfish(b, s, len, an);
                    case NEEDLE -> needle(b, s, len, an);
                    case PUFFER -> puffer(b, s, len, an);
                    case ANGLER -> angler(b, s, len, an);
                    case BULKY -> bulky(b, s, len, an);
                }
                b.dispose();
            });
        }
        if (eaten > 0) bones(g, s, len, cut, anim, eaten >= 3);
        if (s.isLegendary() && eaten == 0) sparkles(g, len, anim);
        g.dispose();
    }

    /**
     * Draws the fish as a flat dark shadow (shape and size only, no colours). Used by "Shadow mode":
     * the fish is first painted into a small image, then everything that was painted is filled with one colour.
     */
    public static void drawSilhouette(Graphics2D g, Species s, double cx, double cy, double len, boolean right,
                                      double anim, Color tint) {
        int w = (int) Math.ceil(len * 2.1) + 24, h = (int) Math.ceil(len * 1.1) + 24;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = img.createGraphics();
        draw(ig, s, w / 2.0, h / 2.0, len, right, anim);
        ig.setComposite(AlphaComposite.SrcAtop);
        ig.setColor(tint);
        ig.fillRect(0, 0, w, h);
        ig.dispose();
        g.drawImage(img, (int) Math.round(cx - w / 2.0), (int) Math.round(cy - h / 2.0), null);
    }

    /** Skeleton of the part of the fish left of x = cut (all of it when whole = true, with a skull). */
    private static void bones(Graphics2D g, Species s, double len, double cut, double anim, boolean whole) {
        double ratio = switch (s.getShape()) {
            case LONG, NEEDLE -> 0.22;
            case FISH, TORPEDO, CATFISH, FANTAIL -> 0.40;
            case ROUND, PUFFER, BULKY, ANGLER -> 0.56;
            default -> 0.30;
        };
        double h = len * ratio, wag = Math.sin(anim) * len * 0.05;
        float w = (float) Math.max(1.3, len / 48);
        double headX = whole ? len * 0.30 : Math.min(cut, len * 0.45);
        g.setColor(BONE);
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(-len * 0.45, wag * 0.3, headX, 0));                          // spine
        g.draw(new java.awt.geom.Line2D.Double(-len * 0.45, wag * 0.3, -len * 0.72, -h * 0.6 + wag));      // tail bones
        g.draw(new java.awt.geom.Line2D.Double(-len * 0.45, wag * 0.3, -len * 0.74, wag));
        g.draw(new java.awt.geom.Line2D.Double(-len * 0.45, wag * 0.3, -len * 0.72, h * 0.6 + wag));
        g.setStroke(new BasicStroke(Math.max(1f, w * 0.75f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (double x = -len * 0.38; x < headX - len * 0.03; x += len * 0.07) {                             // ribs
            double rib = h * 0.40 * (1 - Math.abs(x) / (len * 1.1));
            g.draw(new java.awt.geom.Line2D.Double(x, 0, x - len * 0.03, -rib));
            g.draw(new java.awt.geom.Line2D.Double(x, 0, x - len * 0.03, rib));
        }
        if (whole) {                                                                                         // skull
            g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(len * 0.28, -h * 0.30, len * 0.22, h * 0.60));
            g.setColor(new Color(60, 50, 45));
            g.fill(new Ellipse2D.Double(len * 0.37, -h * 0.17, len * 0.07, len * 0.07));
        }
    }

    // ------------------------------------------------------------------ more body models
    private static Color dark(Color c, double k) {
        return new Color((int) (c.getRed() * k), (int) (c.getGreen() * k), (int) (c.getBlue() * k));
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    /** Markings that make single species look different: koi patches, trout speckles, a bluegill ear spot. */
    private static void marks(Graphics2D g, Species s, double len, double h) {
        switch (s) {
            case KOI, SPIRIT_KOI -> {
                g.setColor(alpha(s == Species.KOI ? new Color(235, 70, 40) : new Color(80, 120, 230), 210));
                g.fill(new Ellipse2D.Double(-len * 0.3, -h * 0.5, len * 0.28, h * 0.55));
                g.fill(new Ellipse2D.Double(len * 0.02, -h * 0.35, len * 0.2, h * 0.4));
            }
            case BROWN_TROUT, TROUT, SALMON -> {
                g.setColor(new Color(0, 0, 0, 90));
                for (int i = 0; i < 9; i++) g.fill(new Ellipse2D.Double(-len * 0.35 + (i * 0.083) * len, -h * 0.35 + (i * 37 % 5) * h * 0.12, len * 0.035, len * 0.035));
            }
            case BLUEGILL -> {
                g.setColor(new Color(20, 20, 40, 190));
                g.fill(new Ellipse2D.Double(len * 0.1, -h * 0.06, len * 0.12, len * 0.1));
            }
            case POND_BASS, RIVER_BASS, GROUPER, CHAR -> {
                g.setColor(new Color(0, 0, 0, 70));
                for (int i = 0; i < 7; i++) g.fill(new Ellipse2D.Double(-len * 0.3 + i * len * 0.1, -h * 0.25 + (i % 3) * h * 0.16, len * 0.05, len * 0.05));
            }
            default -> { }
        }
    }

    private static void sideEye(Graphics2D g, double x, double y, double r) {
        eye(g, x, y, r);
    }

    /** A tall disc-shaped fish (bream, bluegill, gourami): big dorsal and anal fins, small tail. */
    private static void round(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double w = len * 0.74, h = len * 0.86, wag = Math.sin(anim) * len * 0.05;
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(-w * 0.42, 0);
        tail.lineTo(-len * 0.62, -h * 0.26 + wag);
        tail.lineTo(-len * 0.55, wag);
        tail.lineTo(-len * 0.62, h * 0.26 + wag);
        tail.closePath();
        g.setColor(dark(body, 0.8));
        g.fill(tail);
        Path2D.Double dorsal = new Path2D.Double();                       // a long fin over the whole back
        dorsal.moveTo(-w * 0.35, -h * 0.38);
        dorsal.curveTo(-w * 0.2, -h * 0.72, w * 0.15, -h * 0.74, w * 0.38, -h * 0.3);
        dorsal.closePath();
        g.fill(dorsal);
        Path2D.Double anal = new Path2D.Double();
        anal.moveTo(-w * 0.3, h * 0.4);
        anal.curveTo(-w * 0.1, h * 0.7, w * 0.15, h * 0.68, w * 0.25, h * 0.38);
        anal.closePath();
        g.fill(anal);
        Ellipse2D bodyShape = new Ellipse2D.Double(-w / 2, -h / 2, w, h);
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 200));
        gc.fill(new Ellipse2D.Double(-w / 2, h * 0.08, w, h));
        if (s.hasStripes()) {
            gc.setColor(new Color(0, 0, 0, 55));
            for (int i = 0; i < 4; i++) gc.fill(new Ellipse2D.Double(-w * 0.3 + i * w * 0.2, -h / 2, w * 0.07, h));
        }
        marks(gc, s, len, h);
        gc.dispose();
        g.setColor(alpha(dark(body, 0.8), 230));
        g.fill(new Ellipse2D.Double(-w * 0.05, h * 0.02, w * 0.3, h * 0.16 + Math.sin(anim * 2) * 3));    // side fin
        eye(g, w * 0.22, -h * 0.12, Math.max(2.8, len * 0.075));
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new Ellipse2D.Double(w * 0.44, h * 0.04, len * 0.04, len * 0.03));                          // small mouth
    }

    /** A streamlined fast fish (tuna, mackerel, salmon): pointed nose, finlets, deeply forked tail. */
    private static void torpedo(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double h = len * 0.30, wag = Math.sin(anim) * len * 0.08;
        Path2D.Double tail = new Path2D.Double();                         // crescent tail
        tail.moveTo(-len * 0.40, -h * 0.08);
        tail.lineTo(-len * 0.74, -h * 1.0 + wag);
        tail.lineTo(-len * 0.60, wag);
        tail.lineTo(-len * 0.74, h * 1.0 + wag);
        tail.lineTo(-len * 0.40, h * 0.08);
        tail.closePath();
        g.setColor(dark(body, 0.75));
        g.fill(tail);
        Polygon dorsal = new Polygon();
        dorsal.addPoint((int) (-len * 0.02), (int) (-h * 0.42));
        dorsal.addPoint((int) (len * 0.08), (int) (-h * 0.98));
        dorsal.addPoint((int) (len * 0.2), (int) (-h * 0.38));
        g.fill(dorsal);
        for (int i = 0; i < 4; i++) {                                     // finlets
            double x = -len * 0.12 - i * len * 0.06;
            Polygon f = new Polygon();
            f.addPoint((int) x, (int) (-h * 0.42));
            f.addPoint((int) (x - len * 0.03), (int) (-h * 0.66));
            f.addPoint((int) (x - len * 0.05), (int) (-h * 0.38));
            g.fill(f);
        }
        Path2D.Double bodyShape = new Path2D.Double();
        bodyShape.moveTo(len * 0.52, 0);
        bodyShape.curveTo(len * 0.32, -h * 0.75, -len * 0.18, -h * 0.62, -len * 0.42, -h * 0.1);
        bodyShape.lineTo(-len * 0.42, h * 0.1);
        bodyShape.curveTo(-len * 0.18, h * 0.62, len * 0.32, h * 0.75, len * 0.52, 0);
        bodyShape.closePath();
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 215));
        gc.fill(new Ellipse2D.Double(-len * 0.5, h * 0.1, len * 1.1, h));
        if (s.hasStripes()) {
            gc.setColor(new Color(0, 0, 0, 60));
            for (int i = 0; i < 5; i++) gc.fill(new Ellipse2D.Double(-len * 0.3 + i * len * 0.12, -h * 0.7, len * 0.04, h * 0.9));
        }
        marks(gc, s, len, h);
        gc.dispose();
        g.setColor(alpha(dark(body, 0.8), 230));
        g.fill(new Ellipse2D.Double(len * 0.12, h * 0.1, len * 0.14, h * 0.28));
        eye(g, len * 0.36, -h * 0.1, Math.max(2.4, len * 0.055));
    }

    /** A small body with a huge flowing tail (goldfish, guppy). */
    private static void fantail(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double h = len * 0.46, bl = len * 0.56, wag = Math.sin(anim) * len * 0.07;
        for (int i = 0; i < 2; i++) {
            Path2D.Double lobe = new Path2D.Double();                     // two big, see-through tail lobes
            double dy = i == 0 ? -1 : 1;
            lobe.moveTo(-bl * 0.35, 0);
            lobe.curveTo(-len * 0.55, dy * h * 0.2 + wag, -len * 0.85, dy * h * 1.0 + wag, -len * 0.7, dy * h * 0.35 + wag);
            lobe.curveTo(-len * 0.8, wag, -len * 0.6, wag, -bl * 0.35, 0);
            lobe.closePath();
            g.setColor(alpha(i == 0 ? acc : dark(acc, 0.9), 190));
            g.fill(lobe);
        }
        Path2D.Double dorsal = new Path2D.Double();
        dorsal.moveTo(-bl * 0.3, -h * 0.4);
        dorsal.curveTo(-bl * 0.1, -h * 0.9, bl * 0.2, -h * 0.85, bl * 0.25, -h * 0.35);
        dorsal.closePath();
        g.setColor(alpha(acc, 210));
        g.fill(dorsal);
        Ellipse2D bodyShape = new Ellipse2D.Double(-bl * 0.5, -h / 2, bl * 1.1, h);
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 190));
        gc.fill(new Ellipse2D.Double(-bl * 0.5, h * 0.05, bl * 1.1, h));
        marks(gc, s, len, h);
        gc.dispose();
        eye(g, bl * 0.35, -h * 0.1, Math.max(2.6, len * 0.065));
    }

    /** A flat wide head with whiskers (catfish, loach). */
    private static void catfish(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double h = len * 0.30, wag = Math.sin(anim) * len * 0.06;
        Path2D.Double tail = new Path2D.Double();                         // rounded tail
        tail.moveTo(-len * 0.38, 0);
        tail.curveTo(-len * 0.52, -h * 0.9 + wag, -len * 0.66, -h * 0.6 + wag, -len * 0.62, wag);
        tail.curveTo(-len * 0.66, h * 0.6 + wag, -len * 0.52, h * 0.9 + wag, -len * 0.38, 0);
        g.setColor(dark(body, 0.8));
        g.fill(tail);
        Polygon dorsal = new Polygon();
        dorsal.addPoint((int) (-len * 0.02), (int) (-h * 0.4));
        dorsal.addPoint((int) (len * 0.08), (int) (-h * 0.9));
        dorsal.addPoint((int) (len * 0.16), (int) (-h * 0.36));
        g.fill(dorsal);
        Path2D.Double bodyShape = new Path2D.Double();                    // tapering body, wide flat head
        bodyShape.moveTo(len * 0.5, h * 0.12);
        bodyShape.curveTo(len * 0.5, -h * 0.6, len * 0.3, -h * 0.62, len * 0.12, -h * 0.5);
        bodyShape.curveTo(-len * 0.1, -h * 0.42, -len * 0.3, -h * 0.28, -len * 0.4, -h * 0.08);
        bodyShape.lineTo(-len * 0.4, h * 0.08);
        bodyShape.curveTo(-len * 0.3, h * 0.28, -len * 0.1, h * 0.42, len * 0.12, h * 0.5);
        bodyShape.curveTo(len * 0.3, h * 0.62, len * 0.5, h * 0.5, len * 0.5, h * 0.12);
        bodyShape.closePath();
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 170));
        gc.fill(new Ellipse2D.Double(-len * 0.5, h * 0.12, len, h));
        if (s.hasStripes()) {
            gc.setColor(new Color(0, 0, 0, 70));
            for (int i = 0; i < 5; i++) gc.fill(new Ellipse2D.Double(-len * 0.3 + i * len * 0.14, -h * 0.5, len * 0.05, h));
        }
        gc.dispose();
        g.setColor(dark(body, 0.65));                                      // whiskers
        g.setStroke(new BasicStroke(Math.max(1f, (float) len / 55f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 2; i++) {
            double dy = i == 0 ? -1 : 1;
            g.draw(new java.awt.geom.QuadCurve2D.Double(len * 0.5, dy * h * 0.12, len * 0.62, dy * h * 0.5 + Math.sin(anim * 2) * 2, len * 0.7, dy * h * 0.85));
            g.draw(new java.awt.geom.QuadCurve2D.Double(len * 0.5, dy * h * 0.04, len * 0.68, dy * h * 0.15, len * 0.78, dy * h * 0.35));
        }
        g.setStroke(new BasicStroke(1f));
        eye(g, len * 0.34, -h * 0.28, Math.max(2.2, len * 0.045));
    }

    /** A very slim fish with a long bill (swordfish, barracuda, sturgeon). */
    private static void needle(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double h = len * 0.17, wag = Math.sin(anim) * len * 0.07;
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(-len * 0.4, 0);
        tail.lineTo(-len * 0.66, -h * 1.6 + wag);
        tail.lineTo(-len * 0.56, wag);
        tail.lineTo(-len * 0.66, h * 1.6 + wag);
        tail.closePath();
        g.setColor(dark(body, 0.75));
        g.fill(tail);
        Path2D.Double sail = new Path2D.Double();                         // tall dorsal fin
        sail.moveTo(-len * 0.12, -h * 0.5);
        sail.curveTo(-len * 0.02, -h * 2.6, len * 0.1, -h * 2.4, len * 0.2, -h * 0.5);
        sail.closePath();
        g.fill(sail);
        Polygon bill = new Polygon();                                      // the bill
        bill.addPoint((int) (len * 0.3), (int) (-h * 0.22));
        bill.addPoint((int) (len * 0.74), 0);
        bill.addPoint((int) (len * 0.3), (int) (h * 0.22));
        g.setColor(dark(body, 0.6));
        g.fill(bill);
        Ellipse2D bodyShape = new Ellipse2D.Double(-len * 0.45, -h / 2, len * 0.8, h);
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 215));
        gc.fill(new Ellipse2D.Double(-len * 0.45, h * 0.05, len * 0.8, h));
        if (s.hasStripes()) {
            gc.setColor(new Color(0, 0, 0, 70));
            for (int i = 0; i < 6; i++) gc.fill(new Ellipse2D.Double(-len * 0.3 + i * len * 0.1, -h / 2, len * 0.035, h * 0.7));
        }
        gc.dispose();
        eye(g, len * 0.24, -h * 0.05, Math.max(2.2, len * 0.04));
    }

    /** A round fish covered in little spikes (puffer, lantern). */
    private static void puffer(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double d = len * 0.74, r = d / 2, wag = Math.sin(anim) * len * 0.05;
        Polygon tail = new Polygon();
        tail.addPoint((int) (-r * 0.8), 0);
        tail.addPoint((int) (-len * 0.62), (int) (-r * 0.45 + wag));
        tail.addPoint((int) (-len * 0.62), (int) (r * 0.45 + wag));
        g.setColor(dark(body, 0.8));
        g.fill(tail);
        g.setColor(alpha(dark(body, 0.75), 230));                          // spikes
        g.setStroke(new BasicStroke(Math.max(1.2f, (float) len / 40f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI * 2 / 16;
            g.draw(new java.awt.geom.Line2D.Double(Math.cos(a) * r * 0.92, Math.sin(a) * r * 0.92, Math.cos(a) * r * 1.16, Math.sin(a) * r * 1.16));
        }
        g.setStroke(new BasicStroke(1f));
        Ellipse2D bodyShape = new Ellipse2D.Double(-r, -r, d, d);
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 215));
        gc.fill(new Ellipse2D.Double(-r, r * 0.15, d, d));
        gc.setColor(alpha(acc, 140));
        for (int i = 0; i < 6; i++) gc.fill(new Ellipse2D.Double(-r * 0.6 + i * r * 0.22, -r * 0.5 + (i % 2) * r * 0.25, r * 0.12, r * 0.12));
        gc.dispose();
        g.setColor(alpha(dark(body, 0.8), 230));
        g.fill(new Ellipse2D.Double(-r * 0.1, r * 0.05, r * 0.5, r * 0.3 + Math.sin(anim * 3) * 3));          // flapping fin
        eye(g, r * 0.4, -r * 0.2, Math.max(3.2, len * 0.09));
        g.setColor(new Color(0, 0, 0, 110));
        g.fill(new Ellipse2D.Double(r * 0.86, r * 0.02, len * 0.05, len * 0.05));
    }

    /** A deep-sea angler: big head with teeth, and a glowing lure hanging from a stalk. */
    private static void angler(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double w = len * 0.78, h = len * 0.56, wag = Math.sin(anim) * len * 0.06;
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(-w * 0.4, 0);
        tail.lineTo(-len * 0.66, -h * 0.4 + wag);
        tail.lineTo(-len * 0.58, wag);
        tail.lineTo(-len * 0.66, h * 0.4 + wag);
        tail.closePath();
        g.setColor(dark(body, 0.75));
        g.fill(tail);
        // the stalk and the glowing bulb
        double bx = len * 0.5, by = -h * 0.95 + Math.sin(anim * 1.5) * len * 0.03;
        g.setColor(dark(body, 0.7));
        g.setStroke(new BasicStroke(Math.max(1.4f, (float) len / 38f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(w * 0.12, -h * 0.45, w * 0.3, -h * 1.15, bx, by));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 240, 150, 70));
        g.fill(new Ellipse2D.Double(bx - len * 0.12, by - len * 0.12, len * 0.24, len * 0.24));
        g.setColor(new Color(255, 245, 170));
        g.fill(new Ellipse2D.Double(bx - len * 0.045, by - len * 0.045, len * 0.09, len * 0.09));
        Ellipse2D bodyShape = new Ellipse2D.Double(-w / 2, -h / 2, w, h);
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 150));
        gc.fill(new Ellipse2D.Double(-w / 2, h * 0.15, w, h));
        gc.dispose();
        Path2D.Double mouth = new Path2D.Double();                        // wide mouth full of teeth
        mouth.moveTo(w * 0.5, -h * 0.05);
        mouth.lineTo(w * 0.05, h * 0.3);
        mouth.lineTo(w * 0.1, h * 0.12);
        mouth.closePath();
        g.setColor(new Color(40, 20, 30));
        g.fill(mouth);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            double x = w * 0.44 - i * w * 0.075, y = -h * 0.02 + i * h * 0.06;
            Polygon tooth = new Polygon();
            tooth.addPoint((int) x, (int) y);
            tooth.addPoint((int) (x - w * 0.025), (int) (y + h * 0.1));
            tooth.addPoint((int) (x - w * 0.05), (int) y);
            g.fill(tooth);
        }
        eye(g, w * 0.2, -h * 0.2, Math.max(3, len * 0.075));
    }

    /** A fat fish with a big head and mouth (grouper, bass, char). */
    private static void bulky(Graphics2D g, Species s, double len, double anim) {
        Color body = s.getBody(), acc = s.getAccent();
        double h = len * 0.56, wag = Math.sin(anim) * len * 0.06;
        Path2D.Double tail = new Path2D.Double();                         // broad rounded tail
        tail.moveTo(-len * 0.36, 0);
        tail.curveTo(-len * 0.5, -h * 0.7 + wag, -len * 0.68, -h * 0.55 + wag, -len * 0.64, wag);
        tail.curveTo(-len * 0.68, h * 0.55 + wag, -len * 0.5, h * 0.7 + wag, -len * 0.36, 0);
        g.setColor(dark(body, 0.8));
        g.fill(tail);
        Path2D.Double spiny = new Path2D.Double();                        // spiny dorsal fin
        spiny.moveTo(-len * 0.24, -h * 0.44);
        for (int i = 0; i < 6; i++) {
            double x = -len * 0.24 + i * len * 0.075;
            spiny.lineTo(x + len * 0.03, -h * (0.66 + 0.1 * Math.sin(i * 1.3)));
            spiny.lineTo(x + len * 0.075, -h * 0.44);
        }
        spiny.closePath();
        g.fill(spiny);
        Ellipse2D bodyShape = new Ellipse2D.Double(-len / 2, -h / 2, len, h);
        g.setColor(body);
        g.fill(bodyShape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(bodyShape);
        gc.setColor(alpha(acc, 200));
        gc.fill(new Ellipse2D.Double(-len / 2, h * 0.1, len, h));
        gc.setColor(alpha(dark(body, 0.7), 150));                          // bigger, darker head
        gc.fill(new Ellipse2D.Double(len * 0.14, -h * 0.5, len * 0.45, h));
        if (s.hasStripes()) {
            gc.setColor(new Color(0, 0, 0, 55));
            for (int i = 0; i < 4; i++) gc.fill(new Ellipse2D.Double(-len * 0.3 + i * len * 0.12, -h / 2, len * 0.05, h));
        }
        marks(gc, s, len, h);
        gc.dispose();
        g.setColor(new Color(0, 0, 0, 120));                               // wide mouth line
        g.setStroke(new BasicStroke(Math.max(1.2f, (float) len / 45f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.QuadCurve2D.Double(len * 0.5, h * 0.02, len * 0.36, h * 0.2, len * 0.22, h * 0.12));
        g.setStroke(new BasicStroke(1f));
        eye(g, len * 0.3, -h * 0.16, Math.max(3, len * 0.07));
    }

    // [GRAPHICS] normal fish body: Path2D outline + tail that wags with sin(anim) + fins + stripes + eye | SCREEN: Start (title fish) + Setup (fish preview) + Game (lake, How to Play)
    // ไทย: ตัวปลาปกติ: เส้นขอบ Path2D + หางที่กระดิกด้วย sin(anim) + ครีบ + ลายขีด + ตา | อยู่ที่: หน้า Start (ปลาที่โลโก้) + Setup (รูปปลาตัวอย่าง) + Game (ทะเลสาบ, หน้าวิธีเล่น)
    private static void body(Graphics2D g, Species s, double len, double ratio, double anim) {
        double h = len * ratio;
        double wag = Math.sin(anim) * len * 0.07;
        Color body = s.getBody(), acc = s.getAccent();
        // tail
        Polygon tail = new Polygon();
        tail.addPoint((int) (-len * 0.42), 0);
        tail.addPoint((int) (-len * 0.72), (int) (-h * 0.6 + wag));
        tail.addPoint((int) (-len * 0.66), (int) wag);
        tail.addPoint((int) (-len * 0.72), (int) (h * 0.6 + wag));
        g.setColor(body.darker());
        g.fillPolygon(tail);
        // dorsal fin
        Polygon fin = new Polygon();
        fin.addPoint((int) (-len * 0.15), (int) (-h * 0.42));
        fin.addPoint((int) (len * 0.05), (int) (-h * 0.85));
        fin.addPoint((int) (len * 0.18), (int) (-h * 0.4));
        g.fill(fin);
        // body + belly
        Ellipse2D shape = new Ellipse2D.Double(-len / 2, -h / 2, len, h);
        g.setColor(body);
        g.fill(shape);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(shape);
        gc.setColor(new Color(acc.getRed(), acc.getGreen(), acc.getBlue(), 200));
        gc.fill(new Ellipse2D.Double(-len / 2, 0, len, h));
        if (s.hasStripes()) {
            gc.setColor(new Color(0, 0, 0, 60));
            for (int i = 0; i < 4; i++) {
                double x = -len * 0.25 + i * len * 0.14;
                gc.fill(new Ellipse2D.Double(x, -h / 2, len * 0.05, h));
            }
        }
        marks(gc, s, len, h);
        gc.dispose();                          // flat shapes only: no outline around the body
        g.setColor(new Color(0, 0, 0, 38));    // a soft gill shade instead of a line
        g.fill(new java.awt.geom.Arc2D.Double(len * 0.14, -h * 0.3, len * 0.14, h * 0.6, 90, -180, java.awt.geom.Arc2D.CHORD));
        eye(g, len * 0.33, -h * 0.12, Math.max(2.5, len * 0.07));
    }

    private static void eel(Graphics2D g, Species s, double len, double anim) {
        Path2D p = new Path2D.Double();
        int n = 16;
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            double x = len / 2 - t * len;
            double y = Math.sin(anim + t * 6) * len * 0.07 * t;
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        float w = (float) Math.max(4, len * 0.12);
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(s.getBody());
        g.draw(p);
        g.setStroke(new BasicStroke(w * 0.28f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));   // a thin belly stripe, no border
        g.setColor(s.getAccent());
        g.draw(p);
        eye(g, len * 0.44, -w * 0.15, Math.max(2.5, w * 0.35));
    }

    private static void ray(Graphics2D g, Species s, double len, double anim) {
        double flap = 0.75 + Math.sin(anim * 0.7) * 0.25;
        double h = len * 0.7 * flap;
        g.setColor(s.getBody().darker());
        g.setStroke(new BasicStroke(Math.max(1.5f, (float) len / 50f)));
        g.drawLine((int) (-len * 0.3), 0, (int) (-len * 0.95), (int) (Math.sin(anim) * len * 0.05));
        Polygon p = new Polygon();
        p.addPoint((int) (len * 0.45), 0);
        p.addPoint(0, (int) (-h / 2));
        p.addPoint((int) (-len * 0.35), 0);
        p.addPoint(0, (int) (h / 2));
        g.setColor(s.getBody());
        g.fillPolygon(p);
        g.setColor(new Color(255, 255, 255, 70));
        g.fill(new Ellipse2D.Double(-len * 0.15, -h * 0.15, len * 0.4, h * 0.3));
        eye(g, len * 0.25, -len * 0.06, Math.max(2.5, len * 0.04));
        eye(g, len * 0.25, len * 0.06, Math.max(2.5, len * 0.04));
    }

    private static void squid(Graphics2D g, Species s, double len, double anim) {
        double h = len * 0.34;
        g.setColor(s.getAccent());
        g.setStroke(new BasicStroke(Math.max(1.5f, (float) len / 30f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 5; i++) {
            double oy = (i - 2) * h * 0.18;
            Path2D t = new Path2D.Double();
            t.moveTo(len * 0.2, oy);
            t.quadTo(len * 0.38, oy + Math.sin(anim + i) * h * 0.4, len * 0.55, oy + Math.sin(anim * 1.3 + i) * h * 0.3);
            g.draw(t);
        }
        Polygon fin = new Polygon();
        fin.addPoint((int) (-len * 0.5), 0);
        fin.addPoint((int) (-len * 0.32), (int) (-h * 0.75));
        fin.addPoint((int) (-len * 0.32), (int) (h * 0.75));
        g.setColor(s.getBody().darker());
        g.fillPolygon(fin);
        Ellipse2D mantle = new Ellipse2D.Double(-len * 0.45, -h / 2, len * 0.7, h);
        g.setColor(s.getBody());
        g.fill(mantle);
        eye(g, len * 0.18, -h * 0.1, Math.max(2.5, len * 0.07));
    }

    // [GRAPHICS] the eye: white, black pupil and a small white shine | SCREEN: Start (title fish) + Setup (fish preview) + Game (lake, How to Play)
    // ไทย: ตาปลา: ขาว รูม่านตาดำ และประกายขาวเล็กๆ | อยู่ที่: หน้า Start (ปลาที่โลโก้) + Setup (รูปปลาตัวอย่าง) + Game (ทะเลสาบ, หน้าวิธีเล่น)
    private static void eye(Graphics2D g, double x, double y, double r) {
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(x - r * 0.3, y - r * 0.55, r * 1.1, r * 1.1));
        g.setColor(new Color(255, 255, 255, 235));                                   // a little shine: the eye looks alive
        g.fill(new Ellipse2D.Double(x + r * 0.0, y - r * 0.45, r * 0.42, r * 0.42));
    }

    private static void sparkles(Graphics2D g, double len, double anim) {
        g.setColor(new Color(255, 245, 160, 220));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 4; i++) {
            double a = anim * 0.4 + i * Math.PI / 2;
            double x = Math.cos(a) * len * 0.6, y = Math.sin(a) * len * 0.35;
            double r = 3 + 2 * Math.sin(anim * 2 + i);
            g.drawLine((int) (x - r), (int) y, (int) (x + r), (int) y);
            g.drawLine((int) x, (int) (y - r), (int) x, (int) (y + r));
        }
    }
}
