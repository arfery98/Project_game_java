// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Collection book: species of this spot, shown as silhouettes until caught. */
public class FishdexDialog extends JDialog implements ActionListener {

    private final Angler angler;
    private final List<Species> all;
    private List<Species> shown = new ArrayList<>();
    private Icon[] icons = new Icon[0];
    private final DefaultTableModel model;
    private final JComboBox<String> classBox = new JComboBox<>(new String[]{"All sizes", "Small", "Medium", "Large", "Giant"});
    private final JLabel title = new JLabel();

    public FishdexDialog(Window owner, LakeGame game) {
        super(owner, "Fishdex - " + game.getSettings().location().getTitle(), ModalityType.APPLICATION_MODAL);
        Logo.applyIcon(this);
        angler = game.getAngler();
        all = Species.at(game.getSettings().location());

        String[] cols = {"Fish", "Best size", "Class", "Bait", "Best rod", "Lives at", "When", "Price"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        // [PRESENT] EXTRA COMPONENT JTable: every species, price and what you caught
        // ไทย: extra component JTable: ตารางปลาทุกชนิด ราคา และที่จับได้
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.setRowHeight(38);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setIcon(r < icons.length ? icons[r] : null);
                setFont(Theme.body(Font.BOLD, 13));
                return this;
            }
        });

        // menu: choose which size class to look at
        classBox.setFont(Theme.body(Font.BOLD, 13));
        classBox.setFocusable(false);
        classBox.addActionListener(this);
        JLabel pick = new JLabel("Show size class:");
        pick.setFont(Theme.body(Font.BOLD, 13));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        top.add(pick);
        top.add(classBox);
        title.setFont(Theme.body(Font.BOLD, 13));
        title.setPreferredSize(new java.awt.Dimension(260, 22));      // room for the whole text
        top.add(title);

        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBorder(new RibbonBorder("Fishdex - " + game.getSettings().location().getTitle(), Theme.LEAF));
        card.add(top, BorderLayout.NORTH);
        card.add(Theme.scroll(table), BorderLayout.CENTER);

        FancyButton close = new FancyButton("Close", Icons.rod(18), FancyButton.Style.WOOD);
        close.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(close);

        WoodPanel root = new WoodPanel(new BorderLayout(8, 8), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.add(card, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        reload();
        setSize(1010, 440);
        Theme.fitToScreen(this);
        setLocationRelativeTo(owner);
    }

    // [PRESENT] EVENT ActionListener (JComboBox): choose a size class, the table reloads
    // ไทย: event ActionListener (JComboBox): เลือกขนาดปลา แล้วตารางโหลดใหม่
    @Override
    public void actionPerformed(ActionEvent e) {
        reload();
    }

    /** Fills the table with the species of the chosen size class (0 = all). */
    private void reload() {
        int cls = classBox.getSelectedIndex();
        shown = new ArrayList<>();
        int caught = 0, caughtShown = 0;
        for (Species s : all) {
            boolean got = angler.bestSize(s) != null;
            if (got) caught++;
            if (cls == 0 || s.getTier() == cls) {
                shown.add(s);
                if (got) caughtShown++;
            }
        }
        model.setRowCount(0);
        icons = new Icon[shown.size()];
        for (int i = 0; i < shown.size(); i++) {
            Species s = shown.get(i);
            Integer best = angler.bestSize(s);
            icons[i] = Icons.fish(s, 56, 30, best == null);
            String when = switch (s.getTime()) {
                case DAY -> "Day";
                case NIGHT -> "Night";
                default -> "Any time";
            };
            model.addRow(new Object[]{
                best == null ? (s.isLegendary() ? "??? (legend)" : "???") : s.getTitle(),
                best == null ? "-" : best + " cm",
                s.tierName() + (s.isLegendary() ? " (legend)" : ""),
                s.baitHint(),
                s.rodHint(),
                s.getMinDepth() + "-" + s.getMaxDepth() + " cm",
                when,
                "$" + Math.round(s.getBaseValue() * 0.6) + "-" + Math.round(s.getBaseValue() * 1.4)
            });
        }
        title.setText(cls == 0 ? "Collected " + caught + " / " + all.size()
                : shown.isEmpty() ? "No " + classBox.getSelectedItem() + " fish here"
                : "Collected " + caughtShown + " / " + shown.size());
    }
}
