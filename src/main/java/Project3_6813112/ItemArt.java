// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;

/** Pictures of the shop items: every rod, reel, lure and part has its own model, drawn with Java2D. */
public final class ItemArt {

    private ItemArt() { }

    /** Draws the item's model inside the box (0, 0, w, h). */
    public static void draw(Graphics2D g0, ShopItem item, int w, int h, double t) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (item instanceof Rod r) rod(g, r, w, h, t);
        else if (item instanceof Reel r) reel(g, r, w, h, t);
        else if (item instanceof Bait b) lure(g, b, w, h, t);
        else if (item instanceof Part p) part(g, p, w, h, t);
        g.dispose();
    }

    // ------------------------------------------------------------------ rods
    /** A rod as a tapered curve from the handle (p0) to the tip (p1). */
    private static void taper(Graphics2D g, Point2D p0, Point2D c, Point2D p1, double w0, double w1, double from, double to, Color col) {
        int n = 36;
        double[] lx = new double[n + 1], ly = new double[n + 1], rx = new double[n + 1], ry = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            double u = from + (to - from) * i / n;
            double x = (1 - u) * (1 - u) * p0.getX() + 2 * (1 - u) * u * c.getX() + u * u * p1.getX();
            double y = (1 - u) * (1 - u) * p0.getY() + 2 * (1 - u) * u * c.getY() + u * u * p1.getY();
            double dx = 2 * (1 - u) * (c.getX() - p0.getX()) + 2 * u * (p1.getX() - c.getX());
            double dy = 2 * (1 - u) * (c.getY() - p0.getY()) + 2 * u * (p1.getY() - c.getY());
            double len = Math.max(1e-6, Math.hypot(dx, dy)), nx = -dy / len, ny = dx / len;
            double wd = (w0 + (w1 - w0) * u) / 2;
            lx[i] = x + nx * wd;
            ly[i] = y + ny * wd;
            rx[i] = x - nx * wd;
            ry[i] = y - ny * wd;
        }
        Path2D.Double path = new Path2D.Double();
        path.moveTo(lx[0], ly[0]);
        for (int i = 1; i <= n; i++) path.lineTo(lx[i], ly[i]);
        for (int i = n; i >= 0; i--) path.lineTo(rx[i], ry[i]);
        path.closePath();
        g.setColor(col);
        g.fill(path);
    }

    private static Point2D at(Point2D p0, Point2D c, Point2D p1, double u) {
        return new Point2D.Double((1 - u) * (1 - u) * p0.getX() + 2 * (1 - u) * u * c.getX() + u * u * p1.getX(),
                (1 - u) * (1 - u) * p0.getY() + 2 * (1 - u) * u * c.getY() + u * u * p1.getY());
    }

    private static void rod(Graphics2D g, Rod rod, int w, int h, double t) {
        Point2D p0 = new Point2D.Double(w * 0.16, h * 0.90), p1 = new Point2D.Double(w * 0.90, h * 0.16);
        Point2D c = new Point2D.Double(w * 0.55, h * 0.62);
        switch (rod) {
            case BAMBOO -> {
                taper(g, p0, c, p1, 8, 2.2, 0, 1, new Color(222, 196, 112));                         // light bamboo
                for (int i = 1; i < 6; i++) {                                                          // the knots of the cane
                    Point2D k = at(p0, c, p1, 0.2 + i * 0.13);
                    g.setColor(new Color(150, 120, 55));
                    g.fill(new Ellipse2D.Double(k.getX() - 3.2, k.getY() - 3.2, 6.4, 6.4));
                }
                taper(g, p0, c, p1, 12, 8, 0, 0.2, new Color(140, 92, 55));                          // cork grip
            }
            case CARBON -> {
                taper(g, p0, c, p1, 6.5, 1.6, 0, 1, new Color(36, 42, 54));                          // black carbon
                taper(g, p0, c, p1, 1.6, 0.6, 0.05, 1, new Color(90, 150, 230));                      // blue gloss line
                taper(g, p0, c, p1, 11, 7, 0, 0.22, new Color(25, 25, 30));
                Point2D ring = at(p0, c, p1, 0.24);
                g.setColor(new Color(220, 60, 60));
                g.fill(new Ellipse2D.Double(ring.getX() - 4, ring.getY() - 4, 8, 8));
            }
            case PRO -> {
                taper(g, p0, c, p1, 7.5, 1.8, 0, 1, new Color(60, 130, 205));                         // blue blank
                for (int i = 0; i < 4; i++) {                                                          // silver wraps
                    Point2D k = at(p0, c, p1, 0.3 + i * 0.17);
                    g.setColor(new Color(215, 222, 232));
                    g.fill(new Ellipse2D.Double(k.getX() - 3.4, k.getY() - 3.4, 6.8, 6.8));
                }
                taper(g, p0, c, p1, 12.5, 8, 0, 0.22, new Color(240, 200, 60));                       // foam grip
                taper(g, p0, c, p1, 12.5, 8, 0.08, 0.14, new Color(40, 40, 45));
            }
            default -> {
                double glow = 0.5 + 0.5 * Math.sin(t * 3);
                g.setColor(new Color(255, 215, 90, (int) (50 + 50 * glow)));                           // a golden glow behind it
                g.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new QuadCurve2D.Double(p0.getX(), p0.getY(), c.getX(), c.getY(), p1.getX(), p1.getY()));
                g.setStroke(new BasicStroke(1f));
                taper(g, p0, c, p1, 9, 2.4, 0, 1, new Color(235, 185, 50));
                taper(g, p0, c, p1, 2.2, 0.7, 0.05, 1, new Color(255, 245, 190));
                taper(g, p0, c, p1, 13.5, 9, 0, 0.22, new Color(110, 50, 140));
                Point2D gem = at(p0, c, p1, 0.27);
                g.setColor(new Color(190, 90, 240));
                g.fill(new Ellipse2D.Double(gem.getX() - 5, gem.getY() - 5, 10, 10));
                g.setColor(new Color(255, 255, 255, 190));
                g.fill(new Ellipse2D.Double(gem.getX() - 2.5, gem.getY() - 3.5, 3.5, 3.5));
                sparkle(g, w * 0.78, h * 0.34, 5 + 2 * glow);
                sparkle(g, w * 0.30, h * 0.52, 4 + 2 * (1 - glow));
            }
        }
        // line guides and the line hanging from the tip
        g.setColor(new Color(200, 200, 205));
        for (int i = 1; i <= 4; i++) {
            Point2D k = at(p0, c, p1, 0.3 + i * 0.16);
            g.draw(new Ellipse2D.Double(k.getX() - 1.8, k.getY() - 1.8, 3.6, 3.6));
        }
        g.setColor(new Color(235, 235, 235, 200));
        g.draw(new Line2D.Double(p1.getX(), p1.getY(), p1.getX() + 4, p1.getY() + h * 0.28));
    }

    private static void sparkle(Graphics2D g, double x, double y, double r) {
        g.setColor(new Color(255, 245, 170, 230));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x - r, y, x + r, y));
        g.draw(new Line2D.Double(x, y - r, x, y + r));
        g.setStroke(new BasicStroke(1f));
    }

    // ------------------------------------------------------------------ reels
    private static void reel(Graphics2D g, Reel reel, int w, int h, double t) {
        double cx = w * 0.5, cy = h * 0.52, r = Math.min(w, h) * 0.30;
        Color body, trim, handle;
        switch (reel) {
            case BASIC -> { body = new Color(150, 155, 165); trim = new Color(95, 100, 110); handle = new Color(110, 75, 50); }
            case QUICK -> { body = new Color(70, 140, 220); trim = new Color(40, 85, 150); handle = new Color(250, 160, 50); }
            default -> { body = new Color(215, 60, 60); trim = new Color(40, 40, 48); handle = new Color(230, 235, 245); }
        }
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(cx - r, cy - r + 5, r * 2, r * 2));
        g.setColor(trim);                                                    // foot that clamps onto the rod
        g.fill(new RoundRectangle2D.Double(cx - r * 0.35, cy - r - 10, r * 0.7, 14, 6, 6));
        g.setColor(body);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setColor(trim);
        g.setStroke(new BasicStroke(3f));
        g.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(235, 235, 225));                                // the spool with the line
        g.fill(new Ellipse2D.Double(cx - r * 0.62, cy - r * 0.62, r * 1.24, r * 1.24));
        g.setColor(new Color(190, 190, 180));
        for (int i = 0; i < 3; i++) g.draw(new Ellipse2D.Double(cx - r * (0.55 - i * 0.12), cy - r * (0.55 - i * 0.12), r * (1.1 - i * 0.24), r * (1.1 - i * 0.24)));
        g.setColor(trim);
        g.fill(new Ellipse2D.Double(cx - r * 0.18, cy - r * 0.18, r * 0.36, r * 0.36));
        double spin = t * (reel == Reel.BASIC ? 1.2 : reel == Reel.QUICK ? 2.4 : 4);   // the crank turns, faster for a better reel
        double hx = cx + Math.cos(spin) * r * 1.05, hy = cy + Math.sin(spin) * r * 1.05;
        g.setColor(trim);
        g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(cx, cy, hx, hy));
        g.setStroke(new BasicStroke(1f));
        g.setColor(handle);
        g.fill(new Ellipse2D.Double(hx - 6, hy - 6, 12, 12));
        if (reel == Reel.TURBO) {                                            // flame stripes
            g.setColor(new Color(255, 190, 60));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Arc2D.Double(cx - r * 0.9, cy - r * 0.9, r * 1.8, r * 1.8, 200, 60, Arc2D.OPEN));
            g.setStroke(new BasicStroke(1f));
        }
    }

    // ------------------------------------------------------------------ lures
    private static void lure(Graphics2D g, Bait b, int w, int h, double t) {
        double cx = w * 0.5, cy = h * 0.46, r = Math.min(w, h) * 0.24 + (b == Bait.GOLDEN ? 4 : 0);
        Color c = b.getColor();
        g.setColor(new Color(235, 235, 235, 200));
        g.draw(new Line2D.Double(cx, 2, cx, cy - r * 0.4));
        g.setColor(c.darker());
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = -2; i <= 2; i++) {
            g.draw(new QuadCurve2D.Double(i * r * 0.3 + cx, cy + r * 0.5, cx + i * r * 0.6 + Math.sin(t * 5 + i) * 4, cy + r * 1.3, cx + i * r * 0.45, cy + r * 2.0));
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(c);
        g.fill(new Ellipse2D.Double(cx - r, cy - r * 0.55, r * 2, r * 1.7));
        g.setColor(new Color(255, 255, 255, 150));
        g.fill(new Ellipse2D.Double(cx - r * 0.6, cy - r * 0.35, r * 0.8, r * 0.5));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx + r * 0.1, cy - r * 0.05, r * 0.55, r * 0.55));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(cx + r * 0.27, cy + r * 0.1, r * 0.25, r * 0.25));
        if (b == Bait.GOLDEN) {
            sparkle(g, cx - r * 1.1, cy - r * 0.5, 5 + 2 * Math.sin(t * 4));
            sparkle(g, cx + r * 1.1, cy + r * 0.2, 5 + 2 * Math.cos(t * 4));
        }
    }

    // ------------------------------------------------------------------ parts
    private static void part(Graphics2D g, Part p, int w, int h, double t) {
        double cx = w * 0.5, cy = h * 0.5, s = Math.min(w, h) * 0.34;
        Color ink = new Color(60, 45, 35);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (p) {
            case REINFORCED_LINE -> {                                        // a spool of thick braided line
                g.setColor(new Color(60, 120, 200));
                g.fill(new RoundRectangle2D.Double(cx - s, cy - s * 0.8, s * 2, s * 1.6, 12, 12));
                g.setColor(new Color(230, 235, 245));
                for (int i = 0; i < 5; i++) g.draw(new Line2D.Double(cx - s * 0.8, cy - s * 0.55 + i * s * 0.28, cx + s * 0.8, cy - s * 0.55 + i * s * 0.28 + 3));
                g.setColor(ink);
                g.draw(new RoundRectangle2D.Double(cx - s, cy - s * 0.8, s * 2, s * 1.6, 12, 12));
            }
            case SHOCK_ABSORBER -> {                                         // a spring between two plates
                g.setColor(new Color(120, 125, 135));
                g.fill(new RoundRectangle2D.Double(cx - s * 0.7, cy - s * 1.0, s * 1.4, 8, 4, 4));
                g.fill(new RoundRectangle2D.Double(cx - s * 0.7, cy + s * 1.0 - 8, s * 1.4, 8, 4, 4));
                g.setColor(new Color(230, 120, 40));
                Path2D.Double spring = new Path2D.Double();
                spring.moveTo(cx, cy - s * 0.9);
                for (int i = 0; i < 6; i++) {
                    double y = cy - s * 0.9 + (i + 0.5) * s * 1.8 / 6;
                    spring.lineTo(cx + (i % 2 == 0 ? s * 0.55 : -s * 0.55), y);
                }
                spring.lineTo(cx, cy + s * 0.9);
                g.draw(spring);
            }
            case LONG_CAST -> {                                              // an arrow flying far
                g.setColor(new Color(60, 160, 110));
                g.draw(new Line2D.Double(cx - s, cy + s * 0.5, cx + s * 0.8, cy - s * 0.5));
                Path2D.Double head = new Path2D.Double();
                head.moveTo(cx + s * 1.1, cy - s * 0.7);
                head.lineTo(cx + s * 0.3, cy - s * 0.55);
                head.lineTo(cx + s * 0.85, cy + s * 0.1);
                head.closePath();
                g.fill(head);
                g.setColor(new Color(60, 160, 110, 110));
                g.draw(new Line2D.Double(cx - s * 1.0, cy + s * 0.05, cx - s * 0.2, cy - s * 0.35));
                g.draw(new Line2D.Double(cx - s * 0.7, cy + s * 0.9, cx + s * 0.1, cy + s * 0.5));
            }
            case LURE_CHARM -> {                                             // a heart that pulls fish in
                double pulse = 1 + 0.08 * Math.sin(t * 5);
                g.setColor(new Color(235, 80, 120));
                Path2D.Double heart = new Path2D.Double();
                heart.moveTo(cx, cy + s * 0.9 * pulse);
                heart.curveTo(cx - s * 1.5 * pulse, cy - s * 0.1, cx - s * 0.7 * pulse, cy - s * 1.1 * pulse, cx, cy - s * 0.35);
                heart.curveTo(cx + s * 0.7 * pulse, cy - s * 1.1 * pulse, cx + s * 1.5 * pulse, cy - s * 0.1, cx, cy + s * 0.9 * pulse);
                g.fill(heart);
                g.setColor(new Color(255, 255, 255, 170));
                g.fill(new Ellipse2D.Double(cx - s * 0.7, cy - s * 0.55, s * 0.4, s * 0.3));
            }
            case BAIT_SAVER -> {                                             // a small fish inside a bubble
                g.setColor(new Color(170, 225, 250, 150));
                g.fill(new Ellipse2D.Double(cx - s, cy - s, s * 2, s * 2));
                g.setColor(new Color(90, 170, 220));
                g.draw(new Ellipse2D.Double(cx - s, cy - s, s * 2, s * 2));
                g.setColor(new Color(250, 150, 60));
                g.fill(new Ellipse2D.Double(cx - s * 0.5, cy - s * 0.25, s * 0.95, s * 0.55));
                Path2D.Double tail = new Path2D.Double();
                tail.moveTo(cx - s * 0.45, cy);
                tail.lineTo(cx - s * 0.85, cy - s * 0.3);
                tail.lineTo(cx - s * 0.85, cy + s * 0.3);
                tail.closePath();
                g.fill(tail);
                g.setColor(Color.BLACK);
                g.fill(new Ellipse2D.Double(cx + s * 0.2, cy - s * 0.12, 4, 4));
            }
            case FISH_GUARD -> {                                             // a shield with a fish bone
                Path2D.Double shield = new Path2D.Double();
                shield.moveTo(cx - s * 0.9, cy - s * 0.9);
                shield.lineTo(cx + s * 0.9, cy - s * 0.9);
                shield.lineTo(cx + s * 0.9, cy + s * 0.1);
                shield.curveTo(cx + s * 0.9, cy + s * 0.8, cx + s * 0.3, cy + s * 1.0, cx, cy + s * 1.15);
                shield.curveTo(cx - s * 0.3, cy + s * 1.0, cx - s * 0.9, cy + s * 0.8, cx - s * 0.9, cy + s * 0.1);
                shield.closePath();
                g.setColor(new Color(70, 110, 190));
                g.fill(shield);
                g.setColor(ink);
                g.draw(shield);
                g.setColor(new Color(240, 234, 214));
                g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(cx - s * 0.6, cy - s * 0.1, cx + s * 0.4, cy - s * 0.1));
                for (int i = 0; i < 3; i++) g.draw(new Line2D.Double(cx - s * 0.3 + i * s * 0.3, cy - s * 0.1, cx - s * 0.3 + i * s * 0.3, cy - s * 0.1 + (i % 2 == 0 ? -s * 0.35 : s * 0.35)));
            }
            case LUCKY_CHARM -> {                                            // a four-leaf clover
                g.setColor(new Color(60, 170, 80));
                for (int i = 0; i < 4; i++) {
                    double a = Math.PI / 4 + i * Math.PI / 2;
                    g.fill(new Ellipse2D.Double(cx + Math.cos(a) * s * 0.5 - s * 0.45, cy + Math.sin(a) * s * 0.5 - s * 0.45, s * 0.9, s * 0.9));
                }
                g.setColor(new Color(40, 120, 55));
                g.draw(new Line2D.Double(cx, cy, cx + s * 0.5, cy + s * 1.1));
                sparkle(g, cx + s * 0.9, cy - s * 0.8, 5);
            }
            default -> {                                                     // heavy core: a steel hex bolt
                Path2D.Double hex = new Path2D.Double();
                for (int i = 0; i < 6; i++) {
                    double a = Math.PI / 6 + i * Math.PI / 3;
                    if (i == 0) hex.moveTo(cx + Math.cos(a) * s, cy + Math.sin(a) * s);
                    else hex.lineTo(cx + Math.cos(a) * s, cy + Math.sin(a) * s);
                }
                hex.closePath();
                g.setColor(new Color(130, 138, 150));
                g.fill(hex);
                g.setColor(ink);
                g.draw(hex);
                g.setColor(new Color(60, 66, 76));
                g.fill(new Ellipse2D.Double(cx - s * 0.4, cy - s * 0.4, s * 0.8, s * 0.8));
                g.setColor(new Color(200, 205, 215));
                g.draw(new Ellipse2D.Double(cx - s * 0.4, cy - s * 0.4, s * 0.8, s * 0.8));
            }
        }
        g.setStroke(new BasicStroke(1f));
    }
}
