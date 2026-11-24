// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/** Small vector icons drawn in code (no image files), used on buttons, lists and tables. */
public final class Icons {

    /** Draws an icon into a size x size box. */
    public interface Painter {
        // [GRAPHICS] every icon is a small painter: shapes drawn at size s, no image files | SCREEN: Start + Setup + Game (icons on every button)
        // ไทย: ทุกไอคอนคือ painter เล็กๆ วาดรูปทรงตามขนาด s ไม่มีไฟล์รูป | อยู่ที่: หน้า Start + Setup + Game (ไอคอนบนปุ่มทุกปุ่ม)
        void paint(Graphics2D g, int s);
    }

    private Icons() { }

    public static Icon of(int size, Painter p) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g0, int x, int y) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.translate(x, y);
                p.paint(g, size);
                g.dispose();
            }

            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private static final Color LIGHT = Theme.PAPER;

    public static Icon rod(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.setStroke(new BasicStroke(n / 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(n / 6, n - n / 6, n - n / 6, n / 6);
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(n - n / 6, n / 6, n - n / 6, n - n / 4);
            g.fillOval(n - n / 6 - 3, n - n / 4, 6, 6);
            g.fillOval(n / 6, n - n / 3, n / 4, n / 4);
        });
    }

    public static Icon bag(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.fillOval(n / 8, n / 3, n * 3 / 4, n * 2 / 3 - 1);
            g.fillRect(n * 3 / 8, n / 6, n / 4, n / 5);
            g.setColor(Theme.WOOD_DARK);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, n / 2));
            g.drawString("$", n * 3 / 8, n - n / 6);
        });
    }

    public static Icon book(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.fillRoundRect(n / 8, n / 8, n * 3 / 4, n * 3 / 4, 4, 4);
            g.setColor(Theme.WATER.darker());
            g.fillRect(n / 8, n / 8, n / 7, n * 3 / 4);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(n / 3 + 2, n / 3, n * 3 / 4, n / 3);
            g.drawLine(n / 3 + 2, n / 2, n * 3 / 4, n / 2);
        });
    }

    public static Icon question(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.fill(new Ellipse2D.Double(1, 1, n - 2, n - 2));
            g.setColor(Theme.WOOD_DARK);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, n * 3 / 4));
            g.drawString("?", n / 3 - 1, n - n / 5);
        });
    }

    public static Icon pause(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.fillRoundRect(n / 5, n / 6, n / 5, n * 2 / 3, 3, 3);
            g.fillRoundRect(n * 3 / 5, n / 6, n / 5, n * 2 / 3, 3, 3);
        });
    }

    public static Icon door(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(n / 5, n / 8, n / 2, n * 3 / 4, 3, 3);
            g.fillPolygon(new int[]{n / 2, n - 2, n / 2}, new int[]{n / 4, n / 2, n * 3 / 4}, 3);
        });
    }

    public static Icon play(int s) {
        return of(s, (g, n) -> {
            g.setColor(LIGHT);
            g.fillPolygon(new int[]{n / 4, n - n / 6, n / 4}, new int[]{n / 6, n / 2, n - n / 6}, 3);
        });
    }

    /** Gear: used for Options / Reset. */
    public static Icon cog(int s) {
        return of(s, (g, n) -> {
            double c = n / 2.0;
            g.setColor(LIGHT);
            for (int i = 0; i < 8; i++) {
                java.awt.geom.AffineTransform old = g.getTransform();
                g.rotate(Math.PI * i / 4, c, c);
                g.fillRoundRect((int) (c - n * 0.09), 1, (int) (n * 0.18), (int) (n * 0.30), 2, 2);
                g.setTransform(old);
            }
            g.fill(new Ellipse2D.Double(n * 0.18, n * 0.18, n * 0.64, n * 0.64));
            g.setColor(Theme.WOOD_DARK);
            g.fill(new Ellipse2D.Double(n * 0.38, n * 0.38, n * 0.24, n * 0.24));
        });
    }

    public static Icon coin(int s) {
        return of(s, (g, n) -> {
            g.setColor(Theme.GOLD);
            g.fill(new Ellipse2D.Double(1, 1, n - 2, n - 2));
            g.setColor(Theme.WOOD_DARK);
            g.draw(new Ellipse2D.Double(1, 1, n - 2, n - 2));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, n * 2 / 3));
            g.drawString("$", n / 3, n - n / 4);
        });
    }

    /** A little coloured lure. */
    public static Icon bait(Bait b, int s) {
        return of(s, (g, n) -> {
            g.setColor(b.getColor().darker());
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(n / 3, n * 3 / 5, n / 4, n - 2);
            g.drawLine(n / 2, n * 3 / 5, n / 2, n - 1);
            g.drawLine(n * 2 / 3, n * 3 / 5, n * 3 / 4, n - 2);
            g.setColor(b.getColor());
            g.fill(new Ellipse2D.Double(n / 6.0, n / 8.0, n * 0.66, n * 0.6));
            g.setColor(new Color(255, 255, 255, 150));
            g.fill(new Ellipse2D.Double(n * 0.25, n * 0.2, n * 0.22, n * 0.16));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(n * 0.5, n * 0.33, n * 0.2, n * 0.2));
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(n * 0.57, n * 0.38, n * 0.09, n * 0.09));
        });
    }

    /** Rod or reel icon in wood colour for shop rows. */
    public static Icon gear(ShopItem item, int s) {
        if (item instanceof Bait b) return bait(b, s);
        if (item instanceof Part) return cog(s);
        boolean isRod = item instanceof Rod;
        return of(s, (g, n) -> {
            g.setColor(Theme.WOOD_DARK);
            if (isRod) {
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(3, n - 3, n - 3, 3);
                g.setStroke(new BasicStroke(1f));
                g.drawLine(n - 3, 3, n - 3, n - 5);
            } else {
                g.setStroke(new BasicStroke(2f));
                g.draw(new Ellipse2D.Double(3, 3, n - 6, n - 6));
                g.fill(new Ellipse2D.Double(n / 2.0 - 3, n / 2.0 - 3, 6, 6));
                g.drawLine(n / 2, n / 2, n - 3, n / 4);
            }
        });
    }

    /** Fish picture for tables; silhouette = not caught yet. */
    public static Icon fish(Species sp, int w, int h, boolean silhouette) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        FishArt.draw(g, sp, w * 0.55, h / 2.0, w * 0.75, true, 1.0);
        if (silhouette) {
            g.setComposite(AlphaComposite.SrcAtop);
            g.setColor(new Color(60, 50, 45));
            g.fillRect(0, 0, w, h);
        }
        g.dispose();
        return new Icon() {
            @Override public void paintIcon(Component c, Graphics g2, int x, int y) { g2.drawImage(img, x, y, null); }
            @Override public int getIconWidth() { return w; }
            @Override public int getIconHeight() { return h; }
        };
    }
}
