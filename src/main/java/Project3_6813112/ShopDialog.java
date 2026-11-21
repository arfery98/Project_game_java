// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BasicStroke;
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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;

/**
 * Tackle shop. A tab for each kind of gear (Rods, Reels, Lures, Parts); every item is a card with its own
 * picture, three cards in a row. Click a card to select it, then Buy (or double-click the card).
 */
public class ShopDialog extends JDialog implements ActionListener {

    private static final int CARD_W = 196, CARD_H = 226;

    private final LakeGame game;
    // [PRESENT] EXTRA COMPONENT JTabbedPane: Rods / Reels / Lures / Parts
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<List<Card>> cards = new ArrayList<>();
    private final List<ShopItem> selected = new ArrayList<>();
    private final JLabel moneyLabel = new JLabel();
    private final JLabel slotLabel = new JLabel(" ");
    private final JLabel statusLabel = new JLabel(" ");
    private final FancyButton buyButton = new FancyButton("Buy", Icons.bag(18), FancyButton.Style.GOLD);
    private final FancyButton removeButton = new FancyButton("Remove part", Icons.door(18), FancyButton.Style.RED);
    private final FancyButton closeButton = new FancyButton("Back to fishing", Icons.rod(18), FancyButton.Style.WOOD);
    private final Timer animation;

    public ShopDialog(Window owner, LakeGame game) {
        super(owner, "Tackle Shop", ModalityType.APPLICATION_MODAL);
        Logo.applyIcon(this);
        this.game = game;

        addTab("Rods", Rod.values());
        addTab("Reels", Reel.values());
        addTab("Lures", Bait.values());
        addTab("Parts", Part.values());
        tabs.setFont(Theme.body(Font.BOLD, 14));
        tabs.addChangeListener(e -> refresh());

        moneyLabel.setIcon(Icons.coin(22));
        moneyLabel.setFont(Theme.title(18));
        moneyLabel.setForeground(Theme.PAPER);
        JLabel tip = new JLabel("Click a card, then Buy (or double-click)");
        tip.setForeground(Theme.PAPER_DARK);
        JPanel north = new JPanel(new BorderLayout());
        north.add(moneyLabel, BorderLayout.WEST);
        north.add(tip, BorderLayout.EAST);

        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBorder(new RibbonBorder("Tackle Shop", new Color(200, 120, 60)));
        card.add(tabs, BorderLayout.CENTER);
        JPanel info = new JPanel(new BorderLayout());
        slotLabel.setFont(Theme.body(Font.BOLD, 13));
        statusLabel.setFont(Theme.body(Font.BOLD, 13));
        info.add(slotLabel, BorderLayout.NORTH);
        info.add(statusLabel, BorderLayout.SOUTH);
        card.add(info, BorderLayout.SOUTH);

        buyButton.addActionListener(this);
        removeButton.addActionListener(this);
        closeButton.addActionListener(this);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(removeButton);
        buttons.add(buyButton);
        buttons.add(closeButton);

        WoodPanel root = new WoodPanel(new BorderLayout(8, 8), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.add(north, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        moneyLabel.setForeground(Theme.PAPER);
        tip.setForeground(Theme.PAPER_DARK);
        refresh();
        setSize(CARD_W * 3 + 2 * 12 + 140, 680);
        Theme.fitToScreen(this);
        setLocationRelativeTo(owner);

        animation = new Timer(60, e -> tabs.repaint());          // the pictures move a little (spinning crank, sparkles)
        animation.start();
    }

    @Override
    public void dispose() {
        animation.stop();
        super.dispose();
    }

    private void addTab(String name, ShopItem[] items) {
        List<Card> row = new ArrayList<>();
        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));          // three cards in a row
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (ShopItem it : items) {
            Card c = new Card(it);
            row.add(c);
            grid.add(c);
        }
        cards.add(row);
        selected.add(items[0]);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(grid, BorderLayout.NORTH);
        javax.swing.JScrollPane sp = Theme.scroll(holder);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabs.addTab(name, sp);
    }

    private ShopItem current() {
        return selected.get(tabs.getSelectedIndex());
    }

    private boolean onParts() {
        return tabs.getSelectedIndex() == 3;
    }

    private void refresh() {
        if (cards.size() < tabs.getTabCount() || tabs.getSelectedIndex() < 0) return;      // still being built
        Angler a = game.getAngler();
        moneyLabel.setText(" $" + a.getMoney());
        slotLabel.setText("<html>" + a.getRod().getTitle() + ": part slots " + a.slotsUsed() + "/" + a.slots()
                + (a.slotsUsed() > 0 ? " &nbsp;(" + partNames(a) + ")" : "") + "</html>");
        removeButton.setVisible(onParts());
        for (List<Card> row : cards) for (Card c : row) c.repaint();
    }

    private static String partNames(Angler a) {
        StringBuilder sb = new StringBuilder();
        for (Part p : a.getParts()) sb.append(sb.length() > 0 ? ", " : "").append(p.getTitle());
        return sb.toString();
    }

    private void say(String msg, boolean ok) {
        statusLabel.setForeground(ok ? new Color(40, 130, 50) : Theme.DANGER);
        statusLabel.setText(msg);
        refresh();
    }

    private void buy() {
        String msg = game.buy(current());
        say(msg, msg.startsWith("Bought") || msg.startsWith("Equipped"));
    }

    private void remove() {
        if (!(current() instanceof Part p)) return;
        String msg = game.uninstall(p);
        say(msg, msg.startsWith("Removed"));
    }

    // [PRESENT] EVENT ActionListener: Buy / Remove part / Back to fishing
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buyButton) buy();
        else if (e.getSource() == removeButton) remove();
        else dispose();
    }

    /** One item of the shop: its model, name, price or state, and a short description. */
    private final class Card extends JPanel {
        private final ShopItem item;
        private boolean hover;

        Card(ShopItem item) {
            this.item = item;
            setPreferredSize(new Dimension(CARD_W, CARD_H));
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                // [PRESENT] EVENT MouseAdapter on a shop card: click selects, double-click buys
                @Override
                public void mouseClicked(MouseEvent e) {
                    selected.set(tabs.getSelectedIndex(), Card.this.item);
                    refresh();
                    if (e.getClickCount() == 2) buy();
                }

                // [PRESENT] EVENT MouseAdapter on a shop card: hover highlight on
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                // [PRESENT] EVENT MouseAdapter on a shop card: hover highlight off
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        private String state() {
            Angler a = game.getAngler();
            if (item instanceof Rod r) return a.getRod() == r ? "equipped" : a.owns(r) ? "owned" : null;
            if (item instanceof Reel r) return a.getReel() == r ? "equipped" : a.owns(r) ? "owned" : null;
            if (item instanceof Bait b) return !a.isLive() && a.getBait() == b ? "in use" : a.owns(b) ? "owned" : null;
            if (item instanceof Part p) return a.has(p) ? "fitted" : null;
            return null;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            boolean sel = item == current();
            // card
            g.setColor(new Color(0, 0, 0, 40));
            g.fill(new RoundRectangle2D.Double(3, 5, w - 6, h - 6, 18, 18));
            g.setColor(hover ? new Color(255, 252, 240) : new Color(252, 246, 228));
            g.fill(new RoundRectangle2D.Double(0, 0, w - 4, h - 4, 18, 18));
            g.setColor(sel ? new Color(225, 160, 40) : Theme.PAPER_DARK);
            g.setStroke(new BasicStroke(sel ? 4f : 2f));
            g.draw(new RoundRectangle2D.Double(1, 1, w - 6, h - 6, 18, 18));
            // picture
            int ax = 10, ay = 10, aw = w - 24, ah = 98;
            g.setPaint(new GradientPaint(0, ay, new Color(196, 228, 240), 0, ay + ah, new Color(140, 196, 222)));
            g.fill(new RoundRectangle2D.Double(ax, ay, aw, ah, 14, 14));
            Graphics2D a = (Graphics2D) g.create(ax, ay, aw, ah);
            ItemArt.draw(a, item, aw, ah, System.currentTimeMillis() / 1000.0);
            a.dispose();
            // name
            g.setFont(Theme.title(15));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Theme.INK);
            g.drawString(item.getTitle(), (w - 4 - fm.stringWidth(item.getTitle())) / 2, ay + ah + 22);
            // price or state
            String st = state();
            Angler an = game.getAngler();
            boolean afford = an.getMoney() >= item.getPrice();
            String line = st != null ? st : "$" + item.getPrice();
            Color col = st != null ? new Color(50, 140, 60) : afford ? new Color(150, 95, 20) : new Color(180, 60, 45);
            g.setFont(Theme.body(Font.BOLD, 14));
            fm = g.getFontMetrics();
            g.setColor(col);
            g.drawString(line, (w - 4 - fm.stringWidth(line)) / 2, ay + ah + 40);
            // description
            g.setFont(Theme.body(Font.PLAIN, 12));
            fm = g.getFontMetrics();
            g.setColor(new Color(85, 70, 55));
            int y = ay + ah + 58;
            for (String s : wrap(item.getDesc(), fm, w - 32)) {
                g.drawString(s, 14, y);
                y += 15;
                if (y > h - 10) break;
            }
            g.dispose();
        }
    }

    private static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(test) > maxWidth && cur.length() > 0) {
                lines.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }
}
