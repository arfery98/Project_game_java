// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;

/** Section frame: a paper card with a coloured ribbon carrying the title. */
public class RibbonBorder extends AbstractBorder {

    private final String title;
    private final Color ribbon;

    public RibbonBorder(String title, Color ribbon) {
        this.title = title;
        this.ribbon = ribbon;
    }

    public RibbonBorder(String title) {
        this(title, Theme.WATER);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(title == null ? 12 : 36, 14, 12, 14);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets in) {
        Insets i = getBorderInsets(c);
        in.set(i.top, i.left, i.bottom, i.right);
        return in;
    }

    @Override
    public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int top = title == null ? 2 : 14;
        RoundRectangle2D card = new RoundRectangle2D.Double(x + 2, y + top, w - 5, h - top - 3, 18, 18);
        g.setColor(new Color(60, 35, 15, 60));
        g.fill(new RoundRectangle2D.Double(x + 4, y + top + 3, w - 5, h - top - 3, 18, 18));
        g.setColor(new Color(252, 244, 224, 240));
        g.fill(card);
        g.setColor(Theme.WOOD_DARK);
        g.setStroke(new BasicStroke(2f));
        g.draw(card);
        if (title != null) {
            g.setFont(Theme.title(15));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(title);
            int rx = x + 14, ry = y + 2, rw = tw + 30, rh = 26;
            Polygon tails = new Polygon(new int[]{rx - 6, rx + 6, rx + 6, rx - 6, rx},
                    new int[]{ry + 6, ry + 6, ry + rh + 4, ry + rh + 4, ry + rh / 2 + 5}, 5);
            g.setColor(ribbon.darker());
            g.fillPolygon(tails);
            g.setColor(ribbon);
            g.fillRoundRect(rx, ry, rw, rh, 10, 10);
            g.setColor(ribbon.darker());
            g.drawRoundRect(rx, ry, rw, rh, 10, 10);
            g.setColor(Theme.PAPER);
            g.drawString(title, rx + 15, ry + rh / 2 + fm.getAscent() / 2 - 3);
        }
        g.dispose();
    }

    @Override
    public boolean isBorderOpaque() { return false; }
}
