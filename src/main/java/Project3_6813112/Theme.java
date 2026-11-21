// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The "fishing cabin" look shared by every screen: wood, paper, ink colours,
 * fonts and painting helpers. Changing a colour here changes the whole game.
 */
public final class Theme {

    public static final Color WOOD_LIGHT = new Color(205, 150, 95);
    public static final Color WOOD = new Color(165, 108, 62);
    public static final Color WOOD_DARK = new Color(100, 62, 34);
    public static final Color PAPER = new Color(252, 244, 224);
    public static final Color PAPER_DARK = new Color(236, 221, 190);
    public static final Color INK = new Color(72, 46, 26);
    public static final Color INK_SOFT = new Color(125, 95, 65);
    public static final Color WATER = new Color(58, 140, 182);
    public static final Color LEAF = new Color(88, 160, 78);
    public static final Color GOLD = new Color(242, 190, 60);
    public static final Color DANGER = new Color(205, 85, 70);

    private static final String TITLE_FAMILY = pick("Segoe UI Variable Display", "Segoe UI Semibold", "Segoe UI", "Helvetica Neue", "Arial", Font.SANS_SERIF);
    private static final String BODY_FAMILY = pick("Segoe UI Variable Text", "Segoe UI", "Helvetica Neue", "Arial", Font.SANS_SERIF);

    private Theme() { }

    private static String pick(String... names) {
        List<String> have = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String n : names) if (have.contains(n)) return n;
        return Font.SANS_SERIF;
    }

    /** Playful font for titles and signs (falls back to SansSerif if not installed). */
    public static Font title(float size) { return new Font(TITLE_FAMILY, Font.BOLD, Math.round(size)); }

    public static Font body(int style, float size) { return new Font(BODY_FAMILY, style, Math.round(size)); }

    /** Tints Nimbus so standard components (check boxes, combos, tables...) match the theme. */
    public static void installLookAndFeel() {
        UIManager.put("control", PAPER);
        UIManager.put("nimbusBase", new Color(120, 78, 40));
        UIManager.put("nimbusBlueGrey", new Color(185, 150, 110));
        UIManager.put("nimbusFocus", GOLD);
        UIManager.put("nimbusSelectionBackground", WATER);
        UIManager.put("nimbusLightBackground", new Color(255, 251, 240));
        UIManager.put("text", INK);
        UIManager.put("defaultFont", body(Font.PLAIN, 13));
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // keep default look and feel
        }
    }

    /** The task says no window may be bigger than 1200 x 700. */
    public static final int MAX_W = 1200, MAX_H = 700;

    /**
     * Lets a frame be resized, but only between its minimum size and 1200 x 700 (the maximize button is turned back).
     */
    public static void makeResizable(JFrame f, int minW, int minH) {
        f.setResizable(true);
        f.setMinimumSize(new java.awt.Dimension(minW, minH));
        f.addComponentListener(new ComponentAdapter() {
            // [PRESENT] EVENT ComponentListener (ComponentAdapter): the window can be resized, never bigger than 1200 x 700
            @Override
            public void componentResized(ComponentEvent e) {
                if ((f.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
                    f.setExtendedState(JFrame.NORMAL);
                    return;
                }
                int w = Math.min(f.getWidth(), MAX_W), h = Math.min(f.getHeight(), MAX_H);
                if (w != f.getWidth() || h != f.getHeight()) f.setSize(w, h);
            }
        });
    }

    /** Makes a window smaller if the screen is small (the taskbar is counted), so nothing is cut off. */
    public static void fitToScreen(Window w) {
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int nw = Math.min(w.getWidth(), usable.width - 20), nh = Math.min(w.getHeight(), usable.height - 20);
        if (nw != w.getWidth() || nh != w.getHeight()) w.setSize(nw, nh);
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Wooden board: gradient planks with grain lines and a dark rim. */
    public static void paintWood(Graphics2D g0, int x, int y, int w, int h, int arc) {
        Graphics2D g = (Graphics2D) g0.create();
        smooth(g);
        Shape s = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        g.setPaint(new GradientPaint(x, y, WOOD_LIGHT, x, y + h, WOOD));
        g.fill(s);
        g.clip(s);
        // grain: wavy thin lines, deterministic so they do not flicker
        g.setStroke(new BasicStroke(1f));
        for (int line = y + 6; line < y + h; line += 9) {
            g.setColor(new Color(110, 70, 38, 45 + (line * 13) % 30));
            int prevX = x, prevY = line;
            for (int px = x; px <= x + w; px += 12) {
                int py = (int) (line + Math.sin((px + line * 3) * 0.035) * 2.2);
                g.drawLine(prevX, prevY, px, py);
                prevX = px;
                prevY = py;
            }
        }
        // plank seams
        g.setColor(new Color(90, 55, 28, 90));
        for (int sy = y + 46; sy < y + h - 10; sy += 46) g.drawLine(x, sy, x + w, sy);
        g.setClip(null);
        g.setColor(WOOD_DARK);
        g.setStroke(new BasicStroke(2.5f));
        g.draw(new RoundRectangle2D.Double(x + 1, y + 1, w - 2, h - 2, arc, arc));
        g.dispose();
    }

    /** Old paper card with a soft edge. */
    public static void paintPaper(Graphics2D g0, int x, int y, int w, int h, int arc) {
        Graphics2D g = (Graphics2D) g0.create();
        smooth(g);
        Shape s = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
        g.setPaint(new GradientPaint(x, y, PAPER, x, y + h, PAPER_DARK));
        g.fill(s);
        g.setColor(new Color(160, 120, 80, 120));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new RoundRectangle2D.Double(x + 1, y + 1, w - 2, h - 2, arc, arc));
        g.dispose();
    }

    /** Makes plain panels/check boxes see-through so the wood or paper behind them shows. */
    public static void transparent(Container c) {
        for (Component k : c.getComponents()) {
            if (k instanceof JScrollPane || k instanceof JTable) continue;
            if (k instanceof JPanel p && !(p instanceof WoodPanel)) p.setOpaque(false);
            if (k instanceof AbstractButton b && !(b instanceof FancyButton)) {
                b.setOpaque(false);
                b.setForeground(INK);
            }
            if (k instanceof JLabel l && l.getForeground().equals(UIManager.getColor("Label.foreground"))) {
                l.setForeground(INK);
            }
            if (k instanceof Container inner) transparent(inner);
        }
    }

    /** Scroll pane with a thin wooden frame and paper background. */
    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(WOOD_DARK, 2));
        sp.getViewport().setBackground(new Color(255, 251, 240));
        return sp;
    }

    /** Paper-coloured table with a wooden header. */
    public static void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setFont(body(Font.PLAIN, 13));
        t.setForeground(INK);
        t.setBackground(new Color(255, 251, 240));
        t.setGridColor(PAPER_DARK);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(190, 225, 240));
        t.setSelectionForeground(INK);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int col) {
                super.getTableCellRendererComponent(tb, v, false, false, r, col);
                setBackground(WOOD);
                setForeground(PAPER);
                setFont(body(Font.BOLD, 12));
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, WOOD_DARK));
                setOpaque(true);
                return this;
            }
        });
    }

    /** Big playful heading label. */
    public static JLabel heading(String text, float size, Color color) {
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(title(size));
        l.setForeground(color);
        return l;
    }
}
