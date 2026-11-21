// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The picture part of "How to Play": nine small animated drawings (power bar, swinging hook, tension gauge,
 * the bait chain ...) with a short caption each, in English or Thai. Everything is drawn with Java2D.
 */
public class HelpGuide extends JPanel {

    private static final String[][] EN = {
        {"1. CAST", "CLICK (or SPACE) once: the POWER bar starts. CLICK again to stop it - the higher the marker, the longer the cast."},
        {"2. WATCH THE HOOK", "The hook swings around the tip of the rod. Hold DOWN to slow the swing, hold UP to pull the hook up."},
        {"3. WAIT FOR THE BITE", "A glowing fish swims to the lure. The moment it touches the lure it is hooked automatically."},
        {"4. REEL IT IN", "HOLD SPACE or the mouse button to reel. Keep the needle in the GREEN. Red = the line is about to snap, let go for a moment."},
        {"5. BEWARE OF HUNTERS", "A bigger fish (red glow) may bite your catch. Each bite lowers the price, 3 bites leave only bones."},
        {"6. SELL OR KEEP", "SELL (Space) for money, or KEEP AS BAIT (B) to catch a bigger fish later."},
        {"7. THE BAIT CHAIN", "The purple lure catches Small fish. A kept Small fish catches Medium, then Large, then Giant. The golden lure is for legends."},
        {"8. DEPTH", "Cast farther to hang the lure deeper: Large fish live from 400 cm, Giant from 1000 cm, legends from 1500 cm."},
        {"9. UPGRADE", "Buy better rods in the SHOP: they catch bigger fish and cast deeper (reach in cm)."},
    };
    private static final String[][] TH = {
        {"1. โยนเบ็ด", "คลิก (หรือกด SPACE) หนึ่งครั้ง แถบพลังจะวิ่ง คลิกอีกครั้งเพื่อหยุด ยิ่งตัวชี้อยู่สูง ยิ่งโยนไกล"},
        {"2. ดูเบ็ด", "เบ็ดแกว่งรอบปลายคัน กดลูกศรลงค้างเพื่อหน่วงการแกว่ง กดลูกศรขึ้นค้างเพื่อดึงเบ็ดขึ้น"},
        {"3. รอปลากิน", "ปลาเรืองแสงว่ายมาที่เหยื่อ พอมันแตะเหยื่อ ก็ติดเบ็ดเองทันที"},
        {"4. ดึงปลาขึ้น", "กด SPACE หรือเมาส์ค้างเพื่อดึง ให้เข็มอยู่ในสีเขียว สีแดงคือสายใกล้ขาด ให้ปล่อยสักครู่"},
        {"5. ระวังปลานักล่า", "ปลาตัวใหญ่ (เรืองแสงสีแดง) อาจมางับปลาที่คุณดึงอยู่ ยิ่งโดนงับ ราคายิ่งลด ครบ 3 ครั้งเหลือแต่ก้าง"},
        {"6. ขายหรือเก็บ", "SELL (Space) ขายเอาเงิน หรือ KEEP AS BAIT (B) เก็บไว้เป็นเหยื่อ เพื่อจับปลาตัวใหญ่ขึ้น"},
        {"7. ห่วงโซ่เหยื่อ", "เหยื่อม่วงจับปลา Small ปลา Small ที่เก็บไว้จับ Medium แล้ว Large แล้ว Giant เหยื่อทองสำหรับปลาในตำนาน"},
        {"8. ความลึก", "โยนไกลขึ้นเพื่อให้เหยื่อลึกขึ้น ปลา Large อยู่ตั้งแต่ 400 ซม. Giant ตั้งแต่ 1000 ซม. ตำนานตั้งแต่ 1500 ซม."},
        {"9. อัปเกรด", "ซื้อคันที่ดีขึ้นใน SHOP จับปลาใหญ่ขึ้นและโยนลึกขึ้น (ระยะเป็นซม.)"},
    };

    private final Diagram[] diagrams = new Diagram[EN.length];
    private final JLabel[] texts = new JLabel[EN.length];
    private final Timer timer;
    private boolean thai;

    public HelpGuide(boolean thai) {
        this.thai = thai;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        for (int i = 0; i < EN.length; i++) {
            diagrams[i] = new Diagram(i);
            texts[i] = new JLabel();
            texts[i].setVerticalAlignment(JLabel.CENTER);
            texts[i].setForeground(Theme.INK);
            JPanel card = new JPanel(new BorderLayout(14, 0));
            card.setOpaque(false);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.PAPER_DARK),
                    BorderFactory.createEmptyBorder(8, 2, 8, 2)));
            card.add(diagrams[i], BorderLayout.WEST);
            card.add(texts[i], BorderLayout.CENTER);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            add(card);
        }
        setThai(thai);
        timer = new Timer(40, e -> repaint());          // the little pictures move
        timer.start();
    }

    /** Stops the animation (call when the window closes). */
    public void stop() { timer.stop(); }

    /** Switches the captions between English and Thai. */
    public void setThai(boolean thai) {
        this.thai = thai;
        String[][] src = thai ? TH : EN;
        Font f = thai ? HelpDialog.thaiFont(Font.PLAIN, 14) : Theme.body(Font.PLAIN, 14);
        for (int i = 0; i < texts.length; i++) {
            texts[i].setFont(f);
            texts[i].setText("<html><body style='width:272px'><b>" + src[i][0] + "</b><br>" + src[i][1] + "</body></html>");
        }
        revalidate();
        repaint();
    }

    // =====================================================================================================
    //  the drawings
    // =====================================================================================================
    private static final int DW = 270, DH = 136;

    private static Species first(boolean legend, int tier) {
        for (Species s : Species.values()) {
            if (legend ? s.isLegendary() : (!s.isLegendary() && s.getTier() == tier)) return s;
        }
        return Species.values()[0];
    }

    // [GRAPHICS] the 9 small animated pictures in How to Play; a Swing Timer (40 ms) changes t and repaints
    private static final class Diagram extends JComponent {
        private final int kind;

        Diagram(int kind) {
            this.kind = kind;
            setPreferredSize(new Dimension(DW, DH));
            setMinimumSize(new Dimension(DW, DH));
            setMaximumSize(new Dimension(DW, DH));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.translate((getWidth() - DW) / 2, (getHeight() - DH) / 2);
            RoundRectangle2D.Double frame = new RoundRectangle2D.Double(0, 0, DW, DH, 18, 18);
            g.clip(frame);
            double t = System.nanoTime() / 1e9;
            switch (kind) {
                case 0 -> cast(g, t);
                case 1 -> hook(g, t);
                case 2 -> bite(g, t);
                case 3 -> reel(g, t);
                case 4 -> hunter(g, t);
                case 5 -> sellKeep(g);
                case 6 -> chain(g);
                case 7 -> depth(g, t);
                default -> rods(g);
            }
            g.setClip(null);
            g.setColor(Theme.WOOD_DARK);
            g.setStroke(new BasicStroke(2.5f));
            g.draw(frame);
            g.dispose();
        }

        // ---------- small helpers ----------
        private static void water(Graphics2D g, Color top, Color bottom) {
            g.setPaint(new GradientPaint(0, 0, top, 0, DH, bottom));
            g.fillRect(0, 0, DW, DH);
        }

        private static void label(Graphics2D g, String s, float size, Color c, double x, double y, boolean center) {
            g.setFont(Theme.body(Font.BOLD, size));
            FontMetrics fm = g.getFontMetrics();
            double px = center ? x - fm.stringWidth(s) / 2.0 : x;
            g.setColor(new Color(0, 0, 0, 70));
            g.drawString(s, (float) px + 1, (float) y + 1);
            g.setColor(c);
            g.drawString(s, (float) px, (float) y);
        }

        private static void keyCap(Graphics2D g, String s, double x, double y, double w) {
            g.setColor(new Color(0, 0, 0, 60));
            g.fill(new RoundRectangle2D.Double(x + 2, y + 3, w, 22, 8, 8));
            g.setPaint(new GradientPaint(0, (float) y, new Color(250, 250, 245), 0, (float) y + 22, new Color(205, 205, 195)));
            g.fill(new RoundRectangle2D.Double(x, y, w, 22, 8, 8));
            g.setColor(Theme.INK);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Double(x, y, w, 22, 8, 8));
            g.setFont(Theme.body(Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, (float) (x + (w - fm.stringWidth(s)) / 2), (float) y + 15);
        }

        private static void arrow(Graphics2D g, double cx, double cy, boolean up, Color c) {
            Path2D.Double p = new Path2D.Double();
            double d = up ? -1 : 1;
            p.moveTo(cx, cy + d * 8);
            p.lineTo(cx - 7, cy - d * 2);
            p.lineTo(cx - 2.5, cy - d * 2);
            p.lineTo(cx - 2.5, cy - d * 8);
            p.lineTo(cx + 2.5, cy - d * 8);
            p.lineTo(cx + 2.5, cy - d * 2);
            p.lineTo(cx + 7, cy - d * 2);
            p.closePath();
            g.setColor(c);
            g.fill(p);
        }

        private static void lure(Graphics2D g, double x, double y, double s, Color c) {
            g.setColor(c.darker());
            g.setStroke(new BasicStroke(2f));
            for (int i = -1; i <= 1; i++) g.draw(new Line2D.Double(x + i * 3 * s, y + 3 * s, x + i * 5 * s, y + 12 * s));
            g.setColor(c);
            g.fill(new Ellipse2D.Double(x - 8 * s, y - 9 * s, 16 * s, 14 * s));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(x - 1 * s, y - 5 * s, 5 * s, 5 * s));
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(x + 0.5 * s, y - 3.8 * s, 2.4 * s, 2.4 * s));
        }

        private static void fishShape(Graphics2D g, double cx, double cy, double w, Color c) {
            g.setColor(c);
            g.fill(new Ellipse2D.Double(cx - w / 2, cy - w * 0.27, w, w * 0.54));
            Path2D.Double tail = new Path2D.Double();
            tail.moveTo(cx - w * 0.42, cy);
            tail.lineTo(cx - w * 0.72, cy - w * 0.26);
            tail.lineTo(cx - w * 0.72, cy + w * 0.26);
            tail.closePath();
            g.fill(tail);
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx + w * 0.2, cy - w * 0.12, w * 0.16, w * 0.16));
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(cx + w * 0.26, cy - w * 0.08, w * 0.08, w * 0.08));
        }

        // ---------- 1. cast: the power bar and the flying line ----------
        private void cast(Graphics2D g, double t) {
            g.setPaint(new GradientPaint(0, 0, new Color(150, 210, 240), 0, 84, new Color(215, 238, 246)));
            g.fillRect(0, 0, DW, 84);
            g.setPaint(new GradientPaint(0, 84, new Color(90, 170, 205), 0, DH, new Color(45, 115, 155)));
            g.fillRect(0, 84, DW, DH - 84);
            g.setColor(Theme.WOOD);
            g.fillRect(0, 82, 74, 10);
            g.setColor(Theme.WOOD_DARK);
            g.fillRect(8, 92, 6, 44);
            g.fillRect(58, 92, 6, 44);
            Logo.paintEmblem(g, 38, 58, 42, 0);
            g.setColor(new Color(110, 70, 38));
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(52, 66, 102, 26));

            double p = (t * 0.5) % 2;
            double level = p < 1 ? p : 2 - p;
            double landX = 118 + level * 84;
            QuadCurve2D.Double arc = new QuadCurve2D.Double(102, 26, (102 + landX) / 2, -8, landX, 90);
            g.setColor(new Color(255, 255, 255, 220));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5, 4}, 0));
            g.draw(arc);
            g.setColor(new Color(215, 85, 63));
            g.fill(new Ellipse2D.Double(landX - 5, 85, 10, 10));
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(landX - 5, 90, 10, 5));

            double bx = 226, by0 = 16, bh = 104;
            g.setPaint(new LinearGradientPaint(new Point2D.Double(0, by0 + bh), new Point2D.Double(0, by0),
                    new float[]{0f, 0.55f, 0.8f, 1f}, new Color[]{new Color(70, 190, 90), new Color(150, 210, 70), new Color(240, 200, 50), new Color(225, 60, 50)}));
            g.fill(new RoundRectangle2D.Double(bx, by0, 20, bh, 8, 8));
            g.setColor(Theme.INK);
            g.setStroke(new BasicStroke(2f));
            g.draw(new RoundRectangle2D.Double(bx, by0, 20, bh, 8, 8));
            double my = by0 + bh - level * bh;
            Path2D.Double mk = new Path2D.Double();
            mk.moveTo(bx - 2, my);
            mk.lineTo(bx - 12, my - 6);
            mk.lineTo(bx - 12, my + 6);
            mk.closePath();
            g.setColor(Color.WHITE);
            g.fill(mk);
            g.setColor(Theme.INK);
            g.draw(mk);
            label(g, "POWER", 10, Color.WHITE, bx + 10, by0 - 4, true);
            double pulse = 0.5 + 0.5 * Math.sin(t * 6);
            g.setColor(new Color(255, 255, 255, (int) (150 + 100 * pulse)));
            g.fill(new RoundRectangle2D.Double(8, 8, 62, 20, 10, 10));
            label(g, "CLICK!", 11, Theme.DANGER, 39, 22, true);
        }

        // ---------- 2. the hook swings on a circle around the rod tip ----------
        private void hook(Graphics2D g, double t) {
            water(g, new Color(110, 190, 220), new Color(25, 85, 125));
            g.setColor(new Color(255, 255, 255, 120));
            g.fillRect(0, 18, DW, 4);
            double tx = 62, ty = 14, r = 96;
            g.setColor(new Color(110, 70, 38));
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(10, 4, tx, ty));
            g.setColor(new Color(255, 255, 255, 90));
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{3, 4}, 0));
            g.draw(new Arc2D.Double(tx - r, ty - r, 2 * r, 2 * r, 205, 130, Arc2D.OPEN));
            double tt = t % 5.5;
            double phi = Math.toRadians(62) * Math.exp(-tt * 0.5) * Math.cos(tt * 2.6);
            double hx = tx + r * Math.sin(phi), hy = ty + r * Math.cos(phi);
            g.setColor(new Color(255, 255, 255, 230));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new Line2D.Double(tx, ty, hx, hy - 8));
            lure(g, hx, hy, 1.0, Bait.PURPLE.getColor());
            // the two keys
            keyCap(g, "DOWN", 156, 28, 52);
            arrow(g, 224, 40, false, new Color(255, 230, 120));
            label(g, "slow the swing", 10, Color.WHITE, 156, 66, false);
            keyCap(g, "UP", 156, 82, 52);
            arrow(g, 224, 94, true, new Color(255, 230, 120));
            label(g, "pull the hook up", 10, Color.WHITE, 156, 120, false);
        }

        // ---------- 3. a fish swims to the lure and is hooked ----------
        private void bite(Graphics2D g, double t) {
            water(g, new Color(95, 175, 210), new Color(20, 70, 110));
            double cyc = (t % 3.6) / 3.6;
            double lx = 170, ly = 62;
            g.setColor(new Color(255, 255, 255, 230));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new Line2D.Double(lx, 0, lx, ly - 9));
            lure(g, lx, ly, 1.3, Bait.PURPLE.getColor());
            Species sp = first(false, 1);
            double k = Math.min(1, cyc / 0.55);
            double fx = 40 + (lx - 42 - 40) * (1 - Math.pow(1 - k, 3));
            if (cyc < 0.62) {
                g.setPaint(new RadialGradientPaint(new Point2D.Double(fx, ly + 2), 38, new float[]{0f, 1f},
                        new Color[]{new Color(255, 245, 160, 150), new Color(255, 245, 160, 0)}));
                g.fill(new Ellipse2D.Double(fx - 38, ly - 36, 76, 76));
            }
            FishArt.draw(g, sp, fx, ly + 2, 56, true, t);
            if (cyc >= 0.55) {
                double a = Math.min(1, (cyc - 0.55) / 0.12);
                g.setColor(new Color(255, 255, 255, (int) (200 * (1 - a * 0.4))));
                g.setStroke(new BasicStroke(2.5f));
                g.draw(new Ellipse2D.Double(lx - 14 - 22 * a, ly - 14 - 22 * a, 28 + 44 * a, 28 + 44 * a));
                label(g, "HOOKED!", 15, new Color(130, 255, 160), 196, 28, false);
                g.setColor(new Color(255, 80, 60));
                g.fill(new RoundRectangle2D.Double(131, 20, 8, 22, 4, 4));
                g.fill(new Ellipse2D.Double(131, 46, 8, 8));
            }
        }

        // ---------- 4. the tension gauge ----------
        private void reel(Graphics2D g, double t) {
            Theme.paintWood(g, 0, 0, DW, DH, 18);
            double cx = 135, cy = 104, r = 74;
            g.setColor(new Color(40, 25, 12, 110));
            g.fill(new Arc2D.Double(cx - r - 10, cy - r - 10, 2 * r + 20, 2 * r + 20, 0, 180, Arc2D.PIE));
            g.setStroke(new BasicStroke(14f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            Color[] zone = {new Color(150, 150, 150), new Color(90, 200, 100), new Color(240, 205, 60), new Color(225, 70, 60)};
            double[][] span = {{180, -30}, {150, -80}, {70, -35}, {35, -35}};
            for (int i = 0; i < 4; i++) {
                g.setColor(zone[i]);
                g.draw(new Arc2D.Double(cx - r, cy - r, 2 * r, 2 * r, span[i][0], span[i][1], Arc2D.OPEN));
            }
            double tl = 0.5 + 0.30 * Math.sin(t * 1.4) + 0.12 * Math.sin(t * 6.1);
            tl = Math.max(0.03, Math.min(0.97, tl));
            double a = Math.PI * (1 - tl);
            g.setColor(Theme.INK);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(cx, cy, cx + (r - 12) * Math.cos(a), cy - (r - 12) * Math.sin(a)));
            g.setColor(new Color(235, 220, 190));
            g.fill(new Ellipse2D.Double(cx - 7, cy - 7, 14, 14));
            label(g, "TENSION", 11, new Color(255, 240, 210), cx, 22, true);
            String state = tl > 0.8 ? "SNAP!" : tl < 0.2 ? "too slack" : "good";
            Color sc = tl > 0.8 ? new Color(255, 110, 90) : tl < 0.2 ? new Color(220, 220, 220) : new Color(140, 255, 160);
            label(g, state, 12, sc, cx, 124, true);
            keyCap(g, "SPACE", 10, 100, 54);
            label(g, "HOLD", 10, new Color(255, 240, 210), 18, 96, false);
        }

        // ---------- 5. a hunter bites the catch ----------
        private void hunter(Graphics2D g, double t) {
            water(g, new Color(85, 160, 195), new Color(18, 62, 100));
            Species big = first(false, 4), small = first(false, 1);
            double bx = 74 + 8 * Math.sin(t * 2.2);
            g.setPaint(new RadialGradientPaint(new Point2D.Double(bx, 76), 78, new float[]{0f, 1f},
                    new Color[]{new Color(255, 60, 40, 120), new Color(255, 60, 40, 0)}));
            g.fill(new Ellipse2D.Double(bx - 78, -2, 156, 156));
            FishArt.draw(g, big, bx, 76, 118, true, t);
            int eaten = (int) ((t / 1.4) % 4);
            g.setColor(new Color(255, 255, 255, 230));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new Line2D.Double(200, 0, 200, 58));
            FishArt.draw(g, small, 200, 76, 58, true, t, eaten);
            String s = eaten == 3 ? "only bones!" : eaten == 0 ? "a hunter comes!" : "CHOMP!";
            double wob = 2 * Math.sin(t * 14);
            label(g, s, 13, eaten == 0 ? new Color(255, 235, 150) : new Color(255, 95, 75), 132, 24 + (eaten > 0 ? wob : 0), true);
            label(g, "bites: " + eaten + " / 3", 11, Color.WHITE, 200, 122, true);
        }

        // ---------- 6. sell or keep ----------
        private void sellKeep(Graphics2D g) {
            Theme.paintWood(g, 0, 0, DW, DH, 18);
            Theme.paintPaper(g, 78, 8, 114, 66, 14);
            FishArt.draw(g, first(false, 1), 135, 36, 56, true, 0);
            label(g, "13 cm   $10", 11, Theme.INK, 135, 66, true);
            g.setPaint(new GradientPaint(0, 82, new Color(110, 200, 100), 0, 112, new Color(60, 150, 70)));
            g.fill(new RoundRectangle2D.Double(16, 82, 112, 32, 10, 10));
            g.setColor(Theme.INK);
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new RoundRectangle2D.Double(16, 82, 112, 32, 10, 10));
            label(g, "SELL  $  [Space]", 11, Color.WHITE, 72, 103, true);
            g.setPaint(new GradientPaint(0, 82, new Color(90, 170, 225), 0, 112, new Color(50, 120, 185)));
            g.fill(new RoundRectangle2D.Double(138, 82, 118, 32, 10, 10));
            g.setColor(Theme.INK);
            g.draw(new RoundRectangle2D.Double(138, 82, 118, 32, 10, 10));
            label(g, "KEEP AS BAIT  [B]", 11, Color.WHITE, 197, 103, true);
        }

        // ---------- 7. the bait chain ----------
        private void chain(Graphics2D g) {
            water(g, new Color(125, 200, 225), new Color(40, 105, 150));
            double y = 54;
            double[] xs = {24, 76, 124, 176, 234};
            double[] ws = {0, 24, 32, 42, 52};
            String[] names = {"purple lure", "Small", "Medium", "Large", "Giant"};
            for (int i = 0; i < 5; i++) {
                if (i == 0) lure(g, xs[0], y + 2, 1.5, Bait.colorOfTier(0));
                else fishShape(g, xs[i], y, ws[i], Bait.colorOfTier(i));
                label(g, names[i], 10, Color.WHITE, xs[i], 98, true);
            }
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 4; i++) {
                double x1 = xs[i] + (i == 0 ? 18 : ws[i] / 2 + 4), x2 = xs[i + 1] - ws[i + 1] / 2 - 10;
                g.setColor(new Color(255, 255, 255, 230));
                g.draw(new Line2D.Double(x1, y - 26, x2, y - 26));
                Path2D.Double head = new Path2D.Double();
                head.moveTo(x2 + 6, y - 26);
                head.lineTo(x2, y - 31);
                head.lineTo(x2, y - 21);
                head.closePath();
                g.fill(head);
            }
            label(g, "catches", 9, Color.WHITE, 54, y - 31, true);
            label(g, "kept as bait catches", 9, Color.WHITE, 168, y - 38, true);
            lure(g, 20, 118, 0.9, Bait.GOLDEN.getColor());
            label(g, "golden lure = legends only", 10, new Color(255, 225, 120), 36, 122, false);
        }

        // ---------- 8. depth layers ----------
        private void depth(Graphics2D g, double t) {
            g.setPaint(new LinearGradientPaint(new Point2D.Double(0, 0), new Point2D.Double(0, DH), new float[]{0f, 0.35f, 0.7f, 1f},
                    new Color[]{new Color(135, 210, 232), new Color(60, 140, 185), new Color(25, 70, 125), new Color(8, 25, 60)}));
            g.fillRect(0, 0, DW, DH);
            double[] ly = {34, 72, 104};
            String[] dep = {"400 cm", "1000 cm", "1500 cm"};
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{4, 4}, 0));
            for (int i = 0; i < 3; i++) {
                g.setColor(new Color(255, 255, 255, 150));
                g.draw(new Line2D.Double(40, ly[i], DW - 8, ly[i]));
                label(g, dep[i], 9, Color.WHITE, 44, ly[i] - 3, false);
            }
            Species small = first(false, 1), mid = first(false, 2), large = first(false, 3), giant = first(false, 4), legend = first(true, 0);
            FishArt.draw(g, small, 120, 16, 28, true, t);
            FishArt.draw(g, mid, 190, 22, 34, false, t);
            FishArt.draw(g, large, 160, 54, 46, true, t);
            FishArt.draw(g, giant, 205, 90, 60, false, t);
            FishArt.draw(g, legend, 140, 122, 64, true, t);
            label(g, "Small, Medium", 9, Color.WHITE, 224, 12, true);
            label(g, "Large", 9, Color.WHITE, 232, 50, true);
            label(g, "Giant", 9, Color.WHITE, 105, 88, true);
            label(g, "Legend", 9, new Color(255, 225, 120), 218, 124, true);
            double d = 0.5 + 0.5 * Math.sin(t * 0.8);
            double hy = 8 + d * 112;
            g.setColor(new Color(255, 255, 255, 230));
            g.setStroke(new BasicStroke(1.8f));
            g.draw(new Line2D.Double(22, 0, 22, hy - 8));
            lure(g, 22, hy, 0.9, Bait.PURPLE.getColor());
        }

        // ---------- 9. rods ----------
        private void rods(Graphics2D g) {
            Theme.paintPaper(g, 0, 0, DW, DH, 18);
            String[] names = {"Bamboo", "Carbon", "Pro", "Legend"};
            int[] reach = {900, 1800, 3000, 5000};
            Color[] cols = {new Color(205, 165, 95), new Color(80, 90, 105), new Color(60, 120, 200), new Color(235, 190, 50)};
            for (int i = 0; i < 4; i++) {
                double y = 14 + i * 30;
                label(g, names[i], 11, Theme.INK, 10, y + 15, false);
                double len = 24 + reach[i] / 5000.0 * 118;
                g.setPaint(new GradientPaint(66, (float) y, cols[i].brighter(), 66, (float) y + 18, cols[i].darker()));
                g.fill(new RoundRectangle2D.Double(66, y, len, 18, 9, 9));
                g.setColor(Theme.INK);
                g.setStroke(new BasicStroke(1.3f));
                g.draw(new RoundRectangle2D.Double(66, y, len, 18, 9, 9));
                label(g, reach[i] + " cm", 10, Color.WHITE, 74, y + 13, false);
                for (int s = 0; s <= i; s++) {                      // part slots
                    g.setColor(Theme.WOOD_DARK);
                    g.fill(new Ellipse2D.Double(66 + len + 8 + s * 12, y + 4, 10, 10));
                }
            }
            label(g, "slots for Parts", 9, Theme.INK_SOFT, 188, 14, false);
        }
    }
}
