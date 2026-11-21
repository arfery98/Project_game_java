// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * The game logo, drawn with Java2D (no image file needed).
 *
 * Emblem: the mascot fish in a straw hat fishing from the lake, with the line dropping into the water
 * and ripples around it.
 * The same drawing is used for the title screen (animated) and for the window icon.
 */
public final class Logo {

    public static final Color DEEP = new Color(0x1D, 0x5A, 0x74);     // sky / lake edge
    public static final Color WATER = new Color(0x3A, 0x97, 0xB9);    // lake surface
    public static final Color CREAM = new Color(0xFF, 0xF8, 0xEA);
    public static final Color RED = new Color(0xD9, 0x55, 0x3F);
    public static final Color INK = new Color(0x2B, 0x3A, 0x42);

    private static final double WATERLINE = 0.10;                     // y of the surface, in units of size from centre

    private Logo() { }

    // [GRAPHICS] LOGO: the round fish-with-straw-hat emblem, all Java2D shapes
    /**
     * Paints the round emblem centred at (cx, cy).
     * @param size diameter in pixels
     * @param t    animation time in seconds (use 0 for a still picture)
     */
    public static void paintEmblem(Graphics2D g0, double cx, double cy, double size, double t) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(cx, cy);
        g.scale(size, size);                           // from here on 1.0 = the full diameter

        Shape disc = new Ellipse2D.Double(-0.5, -0.5, 1, 1);
        g.setColor(DEEP);
        g.fill(disc);
        g.clip(disc);

        // lake: flat water with a very gentle wave on top
        double phase = t * 1.6;
        Path2D.Double lake = new Path2D.Double();
        lake.moveTo(-0.5, 0.5);
        lake.lineTo(-0.5, WATERLINE);
        for (double x = -0.5; x <= 0.5001; x += 0.02) {
            lake.lineTo(x, WATERLINE + 0.008 * Math.sin(x * 22 + phase));
        }
        lake.lineTo(0.5, 0.5);
        lake.closePath();
        g.setColor(WATER);
        g.fill(lake);

        // the mascot fish, bobbing a little, in a straw hat, holding a fishing rod
        Color body = new Color(70, 165, 205), fin = new Color(35, 115, 160), gold = new Color(235, 195, 70);
        double bob = Math.sin(t * 2.2) * 0.010;
        double fx = -0.10, fy = WATERLINE - 0.15 + bob;
        g.setColor(fin);                                                    // tail
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(fx - 0.10, fy + 0.02);
        tail.lineTo(fx - 0.26, fy - 0.10);
        tail.lineTo(fx - 0.22, fy + 0.03);
        tail.lineTo(fx - 0.26, fy + 0.15);
        tail.closePath();
        g.fill(tail);
        g.setColor(body);                                                   // body
        g.fill(new Ellipse2D.Double(fx - 0.14, fy - 0.19, 0.30, 0.37));
        g.setColor(CREAM);                                                  // belly
        g.fill(new Ellipse2D.Double(fx - 0.03, fy + 0.04, 0.17, 0.11));
        g.setColor(Color.WHITE);                                            // eye
        g.fill(new Ellipse2D.Double(fx + 0.02, fy - 0.12, 0.10, 0.10));
        g.setColor(INK);
        g.fill(new Ellipse2D.Double(fx + 0.065, fy - 0.093, 0.040, 0.040));
        g.setStroke(new BasicStroke(0.010f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Arc2D.Double(fx + 0.05, fy - 0.02, 0.10, 0.07, 200, 140, java.awt.geom.Arc2D.OPEN));   // smile
        g.setColor(gold);                                                   // straw hat
        g.fill(new Ellipse2D.Double(fx - 0.15, fy - 0.215, 0.34, 0.06));
        g.fill(new java.awt.geom.RoundRectangle2D.Double(fx - 0.075, fy - 0.31, 0.19, 0.12, 0.06, 0.06));
        g.setColor(RED);
        g.fill(new Rectangle2D.Double(fx - 0.075, fy - 0.215, 0.19, 0.022));

        // rod held by a fin, line hanging into the water with a hook and ripples
        double rtx = 0.40, rty = -0.36;
        double hx = 0.30, hy = WATERLINE + 0.14;
        g.setColor(new Color(110, 70, 38));
        g.setStroke(new BasicStroke(0.018f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(fx + 0.12, fy + 0.02, rtx, rty));
        g.setColor(fin);
        g.fill(new Ellipse2D.Double(fx + 0.09, fy - 0.01, 0.075, 0.065));  // fin gripping the rod
        g.setColor(CREAM);
        g.setStroke(new BasicStroke(0.008f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(rtx, rty, hx, hy - 0.04));
        g.draw(new java.awt.geom.Arc2D.Double(hx - 0.022, hy - 0.04, 0.044, 0.05, 180, 200, java.awt.geom.Arc2D.OPEN));   // hook
        g.setColor(RED);
        g.fill(new Ellipse2D.Double(hx - 0.016, hy + 0.003, 0.032, 0.032));                                              // bait

        // ripples where the line enters the water, spreading and fading when animated
        double p = (t * 0.45) % 1.0;
        for (int i = 0; i < 2; i++) {
            double k = t == 0 ? (i == 0 ? 0.12 : 0.62) : (p + i * 0.5) % 1.0;
            double rx = 0.05 + 0.16 * k, ry = rx * 0.22;
            g.setColor(new Color(CREAM.getRed(), CREAM.getGreen(), CREAM.getBlue(), (int) (235 * (1 - k))));
            g.setStroke(new BasicStroke(0.009f));
            g.draw(new Ellipse2D.Double(hx - rx, WATERLINE + 0.014 - ry, rx * 2, ry * 2));
        }

        g.setClip(null);
        g.dispose();

        // thin cream ring around the badge
        Graphics2D rg = (Graphics2D) g0.create();
        rg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rg.translate(cx, cy);
        rg.scale(size, size);
        rg.setColor(CREAM);
        rg.setStroke(new BasicStroke(0.014f));
        rg.draw(new Ellipse2D.Double(-0.465, -0.465, 0.93, 0.93));
        rg.dispose();
    }

    // [GRAPHICS] the "Fish Goes Fishing" lettering
    /** Wordmark: lowercase, wide-tracked, clean sans. Returns the width drawn. */
    public static double paintWordmark(Graphics2D g0, double x, double baselineY, double fontSize, Color color, boolean centred) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font f = new Font("Segoe UI Semibold", Font.PLAIN, Math.round((float) fontSize));
        if (!f.getFamily().toLowerCase().contains("segoe")) f = new Font(Font.SANS_SERIF, Font.BOLD, Math.round((float) fontSize));
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String text = "fish goes fishing";
        double track = fontSize * 0.10;
        double total = 0;
        for (int i = 0; i < text.length(); i++) total += fm.charWidth(text.charAt(i)) + track;
        total -= track;
        double px = centred ? x - total / 2 : x;
        g.setColor(color);
        for (int i = 0; i < text.length(); i++) {
            g.drawString(String.valueOf(text.charAt(i)), (float) px, (float) baselineY);
            px += fm.charWidth(text.charAt(i)) + track;
        }
        g.dispose();
        return total;
    }

    /** Gives a window the logo as its title-bar / taskbar icon. */
    public static void applyIcon(java.awt.Window w) {
        w.setIconImages(java.util.List.of(icon(16), icon(32), icon(64), icon(128)));
    }

    /** Square icon image (emblem only) for window title bars. */
    public static BufferedImage icon(int px) {
        BufferedImage img = new BufferedImage(px, px, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        paintEmblem(g, px / 2.0, px / 2.0, px - 1, 0);
        g.dispose();
        return img;
    }
}
