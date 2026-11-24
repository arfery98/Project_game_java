// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** End-of-trip summary and a leaderboard of every trip since the program started. */
public class ResultDialog extends JDialog {

    private record Entry(String name, String spot, int earned, int seconds, int fish, String best) {
        double perMinute() { return earned * 60.0 / Math.max(60, seconds); }
    }

    private static final List<Entry> BOARD = new ArrayList<>();

    // [PRESENT] RULE LEADERBOARD: kept in memory (lost when the program closes), ranked by money per minute, admin trips skipped
    // ไทย: กฎ leaderboard: เก็บในหน่วยความจำ (หายเมื่อปิดโปรแกรม) เรียงตามเงินต่อนาที ทริป admin ไม่นับ
    public static void record(LakeGame g) {
        Angler a = g.getAngler();
        if (a.isUnlimited()) return;                   // admin trips (unlimited money) are not ranked
        CatchRecord best = a.biggestCatch();
        BOARD.add(new Entry(a.getName(), g.getSettings().location().getTitle(), a.getEarned(), (int) g.getElapsed(),
                a.getCatches().size(), best == null ? "-" : best.species().getTitle()));
        BOARD.sort(Comparator.comparingDouble(Entry::perMinute).reversed());     // fair for trips of any length
    }

    public ResultDialog(JFrame owner, LakeGame game) {
        super(owner, "Trip Summary", ModalityType.APPLICATION_MODAL);
        Logo.applyIcon(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Angler a = game.getAngler();
        CatchRecord best = a.biggestCatch();

        JLabel title = Theme.heading(a.getCatches().isEmpty() ? "No luck today... try again!" : "What a trip, " + a.getName() + "!",
                28, Theme.PAPER);

        JPanel stats = new JPanel(new GridLayout(1, 6, 8, 0));
        stats.setBorder(new RibbonBorder("This trip", Theme.WATER));
        String[][] cells = {
            {"Time fishing", game.getElapsed() < 1 ? "0:00" : String.format("%d:%02d", (int) game.getElapsed() / 60, (int) game.getElapsed() % 60)},
            {"Money earned", "$" + a.getEarned()},
            {"Fish caught", String.valueOf(a.getCatches().size())},
            {"Species", String.valueOf(a.speciesCount())},
            {"Got away", String.valueOf(game.getEscaped())},
            {"Best catch", best == null ? "-" : best.species().getTitle() + " (" + best.sizeCm() + " cm)"}
        };
        for (String[] c : cells) {
            JLabel l = new JLabel("<html><center><small>" + c[0] + "</small><br><b>" + c[1] + "</b></center></html>",
                    SwingConstants.CENTER);
            l.setFont(Theme.body(Font.PLAIN, 14));
            if (c[0].equals("Best catch") && best != null) {
                l.setIcon(Icons.fish(best.species(), 50, 28, false));
                l.setVerticalTextPosition(SwingConstants.BOTTOM);
                l.setHorizontalTextPosition(SwingConstants.CENTER);
            }
            stats.add(l);
        }

        String[] cols = {"#", "Angler", "Spot", "Earned", "Time", "$ / min", "Fish", "Best catch"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int i = 0; i < BOARD.size(); i++) {
            Entry e = BOARD.get(i);
            model.addRow(new Object[]{i + 1, e.name(), e.spot(), "$" + e.earned(), String.format("%d:%02d", e.seconds() / 60, e.seconds() % 60),
                String.format("%.0f", e.perMinute()), e.fish(), e.best()});
        }
        // [PRESENT] EXTRA COMPONENT JTable: leaderboard of the best trips
        // ไทย: extra component JTable: ตารางอันดับทริปที่ดีที่สุด
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        JPanel board = new JPanel(new BorderLayout());
        board.setBorder(new RibbonBorder("Leaderboard (ranked by $ per minute)", new Color(200, 120, 60)));
        board.add(Theme.scroll(table));

        FancyButton again = new FancyButton("Fish again", Icons.rod(18), FancyButton.Style.GREEN);
        FancyButton menu = new FancyButton("Main menu", Icons.cog(18), FancyButton.Style.WOOD);
        FancyButton exit = new FancyButton("Exit", Icons.door(18), FancyButton.Style.RED);
        // [PRESENT] EVENT ActionListener (lambda): Fish again
        // ไทย: event ActionListener (lambda): ปุ่ม Fish again (เล่นอีกรอบ)
        again.addActionListener(e -> {
            dispose();
            owner.dispose();
            new SetupFrame().setVisible(true);
        });
        // [PRESENT] EVENT ActionListener (lambda): Main menu
        // ไทย: event ActionListener (lambda): ปุ่ม Main menu (กลับหน้าแรก)
        menu.addActionListener(e -> {
            dispose();
            owner.dispose();
            new TitleFrame().setVisible(true);
        });
        // [PRESENT] EVENT ActionListener (lambda): Exit
        // ไทย: event ActionListener (lambda): ปุ่ม Exit (ออกจากโปรแกรม)
        exit.addActionListener(e -> System.exit(0));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(again);
        buttons.add(menu);
        buttons.add(exit);

        JPanel center = new JPanel(new BorderLayout(8, 10));
        center.add(stats, BorderLayout.NORTH);
        center.add(board, BorderLayout.CENTER);
        WoodPanel root = new WoodPanel(new BorderLayout(10, 10), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 14, 16));
        root.add(title, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        title.setForeground(Theme.PAPER);
        setSize(760, 470);
        Theme.fitToScreen(this);
        setLocationRelativeTo(owner);
    }
}
