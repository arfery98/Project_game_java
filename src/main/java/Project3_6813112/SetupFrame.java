// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

/** Second screen (after Start): angler name, fishing spot and trip length. Other settings live in Options. */
public class SetupFrame extends JFrame implements ActionListener, ItemListener {

    private final JTextField nameField = new JTextField("Angler", 14);
    private final JComboBox<Location> spotBox = new JComboBox<>(Location.values());
    private final JLabel spotDesc = new JLabel();
    private final JLabel optionsSummary = new JLabel();
    private final SpotPreview preview = new SpotPreview();
    private final TitleBanner banner = new TitleBanner();
    private final FancyButton startButton = new FancyButton("Go Fishing!", Icons.rod(20), FancyButton.Style.GREEN);
    private final FancyButton helpButton = new FancyButton("How to Play", Icons.book(18), FancyButton.Style.WOOD);
    private final FancyButton backButton = new FancyButton("Back", Icons.door(18), FancyButton.Style.WOOD);
    private final FancyButton optionsButton = new FancyButton("Options...", Icons.cog(18), FancyButton.Style.BLUE);

    public SetupFrame() {
        super("Fish Goes Fishing - New Trip");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Logo.applyIcon(this);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 4));
        form.add(row("Your name:", nameField));
        form.add(row("Fishing spot:", spotBox));
        form.add(row("", spotDesc));
        form.add(row("", new JLabel("<html><i>No time limit - press End trip when you want to stop.</i></html>")));
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setBorder(new RibbonBorder("Trip", Theme.WATER));
        formWrap.add(form);

        // current options (changed in the Options dialog)
        optionsSummary.setFont(Theme.body(Font.PLAIN, 12));
        JPanel optRow = new JPanel(new BorderLayout(10, 0));
        optRow.add(optionsSummary, BorderLayout.CENTER);
        optRow.add(optionsButton, BorderLayout.EAST);
        JPanel optsWrap = new JPanel(new BorderLayout());
        optsWrap.setBorder(new RibbonBorder("Options", new Color(200, 120, 60)));
        optsWrap.add(optRow);

        JLabel tips = new JLabel("<html><b>How it works</b><br>"
                + "1. Click to start casting, click again to stop the power bar.<br>"
                + "2. Wait: when a fish bites it hooks itself.<br>"
                + "3. Hold SPACE / mouse to reel, needle in the green.<br>"
                + "<i>Keys: hold Up to pull the lure up, B bait, F1 help.</i></html>");
        tips.setFont(Theme.body(Font.PLAIN, 13));
        JPanel tipsWrap = new JPanel(new BorderLayout());
        tipsWrap.setBorder(new RibbonBorder("Quick tips", Theme.LEAF));
        tipsWrap.add(tips, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout(8, 10));
        JPanel upper = new JPanel(new BorderLayout(8, 10));
        upper.add(formWrap, BorderLayout.NORTH);
        upper.add(optsWrap, BorderLayout.CENTER);
        left.add(upper, BorderLayout.NORTH);
        left.add(tipsWrap, BorderLayout.CENTER);

        // The ribbon card is painted by the wrapper, so the animated preview draws on top of it.
        JPanel previewWrap = new JPanel(new BorderLayout());
        previewWrap.setBorder(new RibbonBorder("Who lives here?", Theme.LEAF));
        previewWrap.add(preview);
        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.add(left, BorderLayout.CENTER);
        center.add(previewWrap, BorderLayout.EAST);

        JLabel members = new JLabel("<html><b>Group:</b> " + Credits.twoLines() + "</html>");
        members.setFont(Theme.body(Font.PLAIN, 11));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(backButton);
        buttons.add(helpButton);
        buttons.add(startButton);
        startButton.setFont(Theme.body(Font.BOLD, 16));
        JPanel south = new JPanel(new BorderLayout());
        south.add(members, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.EAST);

        WoodPanel root = new WoodPanel(new BorderLayout(10, 10), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        root.add(banner, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        members.setForeground(Theme.PAPER);        // light text on the wood

        spotBox.addItemListener(this);
        startButton.addActionListener(this);
        helpButton.addActionListener(this);
        backButton.addActionListener(this);
        optionsButton.addActionListener(this);
        getRootPane().setDefaultButton(startButton);
        updateSpot();
        updateSummary();

        setSize(1090, 640);
        setMinimumSize(new Dimension(1020, 600));
        setResizable(false);                  // keep the window within 1200x700 (no maximize)
        setLocationRelativeTo(null);
        new Timer(50, e -> {           // animate the fish in the banner and preview
            preview.repaint();
            banner.repaint();
        }).start();
    }

    private static JPanel row(String label, java.awt.Component c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.add(fixed(label));
        p.add(c);
        return p;
    }

    private static JLabel fixed(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.body(Font.BOLD, 13));
        l.setPreferredSize(new Dimension(100, 26));
        return l;
    }

    private void updateSpot() {
        Location loc = (Location) spotBox.getSelectedItem();
        spotDesc.setText("<html><i>" + loc.getDesc() + "</i></html>");
        preview.repaint();
    }

    private void updateSummary() {
        optionsSummary.setText("<html>" + GameOptions.summary() + "</html>");
    }

    // ---------- event handlers ----------

    /** Spot changed: show which fish live there. */
    // [PRESENT] EVENT ItemListener (JComboBox): choosing a fishing spot refreshes the fish list
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) updateSpot();
    }

    // [PRESENT] EVENT ActionListener: Options / How to Play / Back / Go Fishing (checks the JTextField name first)
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == helpButton) {
            new HelpDialog(this).setVisible(true);
        } else if (src == optionsButton) {
            new OptionsDialog(this).setVisible(true);
            updateSummary();
        } else if (src == backButton) {
            new TitleFrame().setVisible(true);
            dispose();
        } else if (src == startButton) {
            String name = nameField.getText().trim();
            if (name.isEmpty() || name.length() > 16) {
                JOptionPane.showMessageDialog(this, "Please enter your name (1-16 characters).",
                        "Check setup", JOptionPane.WARNING_MESSAGE);
                nameField.requestFocus();
                return;
            }
            Settings s = new Settings(name, (Location) spotBox.getSelectedItem(), GameOptions.sound,
                    GameOptions.rain, GameOptions.dayNight, GameOptions.easyReel, GameOptions.fishFinder,
                    GameOptions.volume);
            GameOptions.apply();
            new GameFrame(s).setVisible(true);
            dispose();
        }
    }

    /** Paper sign with the game title and two little fish swimming around it. */
    private static class TitleBanner extends JPanel {
        TitleBanner() {
            setOpaque(false);
            setPreferredSize(new Dimension(800, 100));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), bw = 560, bx = (w - bw) / 2;
            // ropes
            g.setColor(new Color(90, 60, 30));
            g.fillRect(bx + 60, 0, 4, 12);
            g.fillRect(bx + bw - 64, 0, 4, 12);
            Theme.paintPaper(g, bx, 8, bw, 88, 26);
            g.setColor(Theme.WOOD_DARK);
            g.drawRoundRect(bx + 4, 12, bw - 8, 80, 22, 22);
            // logo emblem + wordmark
            double t2 = System.currentTimeMillis() / 1000.0;
            Logo.paintEmblem(g, bx + 70, 52, 68, t2);
            Logo.paintWordmark(g, bx + 126, 62, 42, Logo.INK, false);
            g.setFont(Theme.body(Font.ITALIC, 12));
            g.setColor(Theme.INK_SOFT);
            g.drawString("Set up your trip, then cast away!", bx + 128, 84);
            FishArt.draw(g, Species.KOI, bx - 50 + Math.sin(t2) * 8, 52, 70, true, t2 * 6);
            FishArt.draw(g, Species.TROUT, bx + bw + 50 - Math.sin(t2) * 8, 52, 70, false, t2 * 6 + 2);
            g.dispose();
        }
    }

    /** Animated list of the fish at the selected spot. */
    private class SpotPreview extends JPanel {
        SpotPreview() {
            setOpaque(false);
            setPreferredSize(new Dimension(310, 330));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Location loc = (Location) spotBox.getSelectedItem();
            int x = 0, y = 0, w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, y, loc.getWaterTop(), 0, y + h, loc.getWaterBottom()));
            g.fillRoundRect(x, y, w, h, 16, 16);
            double t = System.currentTimeMillis() / 1000.0;
            List<Species> list = Species.at(loc);
            for (int i = 0; i < list.size(); i++) {
                Species s = list.get(i);
                int rowY = y + 48 + i * (h - 40) / list.size();
                FishArt.draw(g, s, x + 52 + Math.sin(t + i) * 6, rowY, 52, true, t * 6 + i);
                g.setFont(Theme.body(Font.BOLD, 14));
                g.setColor(s.isLegendary() ? Theme.GOLD : Color.WHITE);
                g.drawString(s.isLegendary() ? s.getTitle() + "  (legend)" : s.getTitle(), x + 100, rowY - 2);
                g.setFont(Theme.body(Font.PLAIN, 11));
                g.setColor(new Color(225, 240, 245));
                String when = s.getTime() == Species.Time.ANY ? "" : s.getTime() == Species.Time.DAY ? ", day only" : ", night only";
                g.drawString(s.getMinCm() + "-" + s.getMaxCm() + " cm, ~$" + s.getBaseValue() + when, x + 100, rowY + 14);
            }
            g.setFont(Theme.body(Font.ITALIC, 11));
            FontMetrics fm = g.getFontMetrics();
            String tip = "Bigger fish need a smaller fish as bait!";
            g.setColor(new Color(255, 255, 255, 190));
            g.drawString(tip, x + (w - fm.stringWidth(tip)) / 2, y + 18);
            g.dispose();
        }
    }
}
