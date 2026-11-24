// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

/** Chunky rounded button painted by hand: lighter on hover, pushed down when pressed. */
// [PRESENT] OOP INHERITANCE: custom JButton drawn with Java2D
// ไทย: OOP inheritance: ปุ่ม JButton แบบกำหนดเอง วาดด้วย Java2D
public class FancyButton extends JButton {

    public enum Style {
        WOOD(Theme.WOOD), GREEN(Theme.LEAF), BLUE(Theme.WATER), RED(Theme.DANGER), GOLD(new Color(225, 160, 40));

        final Color base;

        Style(Color base) { this.base = base; }
    }

    private final Style style;

    public FancyButton(String text, Icon icon, Style style) {
        super(text, icon);
        this.style = style;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFocusable(false);
        setOpaque(false);
        setFont(Theme.body(Font.BOLD, 13));
        setForeground(Theme.PAPER);
        setIconTextGap(5);
        setMargin(new Insets(7, 8, 9, 8));
        // our own padding instead of the look-and-feel border (which would squeeze the text)
        setBorder(BorderFactory.createEmptyBorder(7, 8, 9, 8));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setRolloverEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ButtonModel m = getModel();
        Color base = isEnabled() ? style.base : new Color(170, 160, 150);
        if (isEnabled() && m.isRollover()) base = base.brighter();
        boolean down = m.isPressed() && m.isArmed();
        int w = getWidth(), h = getHeight();
        int push = down ? 2 : 0;
        // shadow / bottom edge
        g.setColor(base.darker().darker());
        g.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 4, 14, 14));
        // face
        RoundRectangle2D face = new RoundRectangle2D.Double(1, 1 + push, w - 2, h - 5, 14, 14);
        g.setPaint(new GradientPaint(0, 0, base.brighter(), 0, h, base));
        g.fill(face);
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRoundRect(5, 3 + push, w - 10, (h - 6) / 3, 10, 10);
        g.setColor(base.darker().darker());
        g.setStroke(new BasicStroke(1.5f));
        g.draw(face);
        g.dispose();
        // text + icon (shifted when pressed)
        Graphics2D t = (Graphics2D) g0.create();
        t.translate(0, push - 1);
        t.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(t);
        t.dispose();
    }
}
