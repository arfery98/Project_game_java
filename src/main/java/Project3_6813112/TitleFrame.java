// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The first screen: logo over a calm lake, with Start / Options / Exit.
 * (A "Continue" button can be added under Start when saving is implemented.)
 */
// [PRESENT] REQUIRED FRAME 1 (JFrame): title screen with the menu buttons
// ไทย: Frame ที่ 1 (JFrame): หน้าจอ title มีปุ่มเมนู
public class TitleFrame extends JFrame implements ActionListener {

    private final FancyButton startButton = new FancyButton("Start", Icons.play(24), FancyButton.Style.GREEN);
    private final FancyButton optionsButton = new FancyButton("Options", Icons.cog(22), FancyButton.Style.WOOD);
    private final FancyButton exitButton = new FancyButton("Exit", Icons.door(22), FancyButton.Style.RED);
    private final Timer animation;

    public TitleFrame() {
        super("Fish Goes Fishing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Logo.applyIcon(this);

        Scene scene = new Scene();
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        scene.add(Box.createVerticalStrut(376), gc);          // room for the logo that Scene paints

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 14));
        buttons.setOpaque(false);
        for (FancyButton b : new FancyButton[]{startButton, optionsButton, exitButton}) {
            b.setFont(Theme.body(Font.BOLD, 21));
            b.setPreferredSize(new Dimension(290, 56));
            b.addActionListener(this);
            buttons.add(b);
        }
        gc.gridy = 1;
        scene.add(buttons, gc);

        // an empty block under the buttons: it keeps the buttons where they were (the member names are painted in the scene)
        gc.gridy = 2;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.SOUTH;
        scene.add(javax.swing.Box.createVerticalStrut(70), gc);

        setContentPane(scene);
        getRootPane().setDefaultButton(startButton);           // Enter = Start

        setSize(1000, 660);
        Theme.makeResizable(this, 1000, 660);
        Theme.fitToScreen(this);
        setLocationRelativeTo(null);
        animation = new Timer(33, e -> scene.repaint());
        animation.start();
    }

    // [PRESENT] EVENT ActionListener (JButton): Start / Options / Exit
    // ไทย: event ActionListener (JButton): ปุ่ม Start / Options / Exit
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            animation.stop();
            // [PRESENT] REQUIRED COMPONENT JButton that opens another window: Play -> setup screen
            // ไทย: component บังคับ JButton ที่เปิดหน้าต่างอื่น: Play เปิดหน้าตั้งค่า
            new SetupFrame().setVisible(true);
            dispose();
        } else if (e.getSource() == optionsButton) {
            new OptionsDialog(this).setVisible(true);
        } else if (e.getSource() == exitButton) {
            System.exit(0);
        }
    }

    /** Evening lake with drifting clouds and fish, and the logo on top. */
    private static class Scene extends JPanel {
        private static final Species[] FISH = {Species.TILAPIA, Species.CARP, Species.TROUT, Species.PERCH, Species.KOI,
            Species.MACKEREL, Species.CATFISH, Species.GOURAMI};
        private final long start = System.currentTimeMillis();

        Scene() {
            super(new GridBagLayout());
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            double t = (System.currentTimeMillis() - start) / 1000.0;
            int horizon = (int) (h * 0.60);

            // sky: deep teal at the top, warm light at the horizon
            g.setPaint(new GradientPaint(0, 0, new Color(26, 66, 96), 0, horizon, new Color(236, 190, 140)));
            g.fillRect(0, 0, w, horizon);
            // low sun with a soft glow
            double sx = w * 0.74, sy = horizon - 60;
            for (int i = 6; i >= 1; i--) {
                g.setColor(new Color(255, 226, 160, 14 + i * 4));
                g.fill(new Ellipse2D.Double(sx - 34 - i * 22, sy - 34 - i * 22, 68 + i * 44, 68 + i * 44));
            }
            g.setColor(new Color(255, 238, 190));
            g.fill(new Ellipse2D.Double(sx - 34, sy - 34, 68, 68));
            // clouds
            g.setColor(new Color(255, 255, 255, 70));
            for (int i = 0; i < 4; i++) {
                double x = ((t * (7 + i * 3) + i * 300) % (w + 260)) - 130;
                double y = 60 + i * 38;
                g.fill(new Ellipse2D.Double(x, y, 120, 30));
                g.fill(new Ellipse2D.Double(x + 30, y - 14, 70, 34));
            }
            // two layers of hills
            drawHills(g, w, horizon, new Color(52, 100, 118), 70, 0.007, 1.3);
            drawHills(g, w, horizon, new Color(30, 78, 92), 38, 0.012, 4.1);

            // lake
            g.setPaint(new GradientPaint(0, horizon, new Color(70, 150, 176), 0, h, new Color(14, 52, 72)));
            g.fillRect(0, horizon, w, h - horizon);
            // sun reflection
            for (int k = 0; k < 14; k++) {
                double y = horizon + 6 + k * 9;
                double ww = (60 - k * 3) * (0.5 + 0.5 * Math.abs(Math.sin(t * 2 + k)));
                g.setColor(new Color(255, 230, 170, Math.max(0, 170 - k * 11)));
                g.fillRoundRect((int) (sx - ww / 2 + Math.sin(t + k) * 4), (int) y, (int) Math.max(4, ww), 3, 3, 3);
            }
            // waves
            g.setColor(new Color(255, 255, 255, 40));
            for (int r = 0; r < 7; r++) {
                double y = horizon + 24 + r * r * 7;
                Path2D.Double p = new Path2D.Double();
                for (int x = 0; x <= w; x += 12) {
                    double yy = y + Math.sin(x * 0.03 + t * 1.4 + r) * (1 + r * 0.5);
                    if (x == 0) p.moveTo(x, yy);
                    else p.lineTo(x, yy);
                }
                g.draw(p);
            }
            // fish swimming below the surface
            for (int i = 0; i < FISH.length; i++) {
                boolean right = i % 2 == 0;
                double speed = 22 + (i * 11) % 23;
                double x = ((t * speed * (right ? 1 : -1) + i * 173) % (w + 240) + (w + 240)) % (w + 240) - 120;
                double y = horizon + 70 + (i * 53) % (h - horizon - 120) + Math.sin(t * 0.8 + i) * 7;
                double len = 54 + (i * 17) % 40;
                java.awt.Graphics2D fg = (Graphics2D) g.create();
                fg.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.55f));
                FishArt.draw(fg, FISH[i], x, y, len, right, t * 5 + i);
                fg.dispose();
            }
            // darken the bottom edge slightly
            g.setPaint(new GradientPaint(0, h - 130, new Color(0, 0, 0, 0), 0, h, new Color(0, 14, 24, 130)));
            g.fillRect(0, h - 130, w, 130);

            // logo: animated emblem + wordmark + tagline
            Logo.paintEmblem(g, w / 2.0, 142, 190, t);
            g.setColor(new Color(0, 0, 0, 70));
            Logo.paintWordmark(g, w / 2.0 + 2, 318 + 3, 62, new Color(0, 0, 0, 70), true);
            Logo.paintWordmark(g, w / 2.0, 318, 62, Logo.CREAM, true);
            g.setFont(Theme.body(Font.PLAIN, 15));
            FontMetrics fm = g.getFontMetrics();
            String tag = "cast  -  wait  -  reel";
            g.setColor(new Color(255, 248, 234, 210));
            g.drawString(tag, (w - fm.stringWidth(tag)) / 2, 346);

            // group members, drawn centred (two lines) near the bottom, away from the border
            g.setFont(Theme.body(Font.PLAIN, 12));
            fm = g.getFontMetrics();
            g.setColor(new Color(235, 245, 250));
            String[] lines = {Credits.line1(), Credits.line2()};
            for (int i = 0; i < lines.length; i++) {
                g.drawString(lines[i], (w - fm.stringWidth(lines[i])) / 2, h - 46 + i * 18);
            }
            g.dispose();
        }

        private void drawHills(Graphics2D g, int w, int horizon, Color c, int amp, double freq, double phase) {
            Path2D.Double p = new Path2D.Double();
            p.moveTo(0, horizon);
            for (int x = 0; x <= w; x += 10) {
                p.lineTo(x, horizon - 20 - amp * (0.55 + 0.45 * Math.sin(x * freq + phase)) * (0.6 + 0.4 * Math.sin(x * freq * 2.7 + phase)));
            }
            p.lineTo(w, horizon);
            p.closePath();
            g.setColor(c);
            g.fill(p);
        }
    }
}
