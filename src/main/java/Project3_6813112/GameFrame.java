// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/** Second frame: the lake + tackle box + catch log + buttons. Starts the game threads. */
// [PRESENT] REQUIRED FRAME 3 (JFrame): the game window
// ไทย: Frame ที่ 3 (JFrame): หน้าต่างเกมหลัก
public class GameFrame extends JFrame implements ActionListener, KeyListener, ItemListener {

    private final LakeGame game;
    private final LakePanel lake;
    private final GameLoop loop;
    private final FishSpawner spawner;
    private final WeatherThread weather;
    private final Timer uiTimer;
    private boolean resultShown, spaceDown, updatingBait;

    private final CatchModel catchModel = new CatchModel();
    // [PRESENT] EXTRA COMPONENT JTable + custom AbstractTableModel: today's catch (see CatchModel below)
    // ไทย: extra component JTable + AbstractTableModel ที่เขียนเอง: ปลาที่จับได้วันนี้ (ดู CatchModel ด้านล่าง)
    private final JTable catchTable = new JTable(catchModel);
    private final EnumMap<Species, Icon> fishIcons = new EnumMap<>(Species.class);
    private final JComboBox<Object> baitBox = new JComboBox<>();       // normal baits + fish kept as live bait
    private int baitVersion = -1;
    private final JLabel moneyLabel = new JLabel();
    private final JLabel countLabel = new JLabel();
    private final JLabel gearLabel = new JLabel();
    // [PRESENT] EXTRA COMPONENT JTextPane: coloured notes / log lines
    // ไทย: extra component JTextPane: ข้อความบันทึกเกมมีสี
    private final javax.swing.JTextPane logArea = new javax.swing.JTextPane();      // the notes, with coloured lines
    private final FancyButton shopBtn = new FancyButton("Shop", Icons.bag(18), FancyButton.Style.GOLD);
    private final FancyButton dexBtn = new FancyButton("Fishdex", Icons.book(18), FancyButton.Style.BLUE);
    private final FancyButton helpBtn = new FancyButton("Help", Icons.question(18), FancyButton.Style.WOOD);
    private final FancyButton pauseBtn = new FancyButton("Pause", Icons.pause(16), FancyButton.Style.WOOD);
    private final FancyButton endBtn = new FancyButton("End trip", Icons.door(18), FancyButton.Style.RED);

    public GameFrame(Settings settings) {
        super("Fish Goes Fishing - " + settings.location().getTitle());
        Logo.applyIcon(this);
        game = new LakeGame(settings);
        lake = new LakePanel(game);
        lake.addKeyListener(this);
        lake.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { game.setDepthMove(0); game.setBrake(false); }
        });
        lake.setBorder(BorderFactory.createLineBorder(Theme.WOOD_DARK, 3));

        WoodPanel root = new WoodPanel(new BorderLayout(10, 0), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(lake, BorderLayout.CENTER);
        root.add(buildSide(), BorderLayout.EAST);
        setContentPane(root);
        Theme.transparent(root);
        moneyLabel.setForeground(new Color(150, 95, 10));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            // [PRESENT] EVENT WindowListener (WindowAdapter): closing the window stops the threads and exits
            // ไทย: event WindowListener (WindowAdapter): ปิดหน้าต่าง = หยุด thread แล้วออกจากโปรแกรม
            @Override
            public void windowClosing(WindowEvent e) {
                stopThreads();
                System.exit(0);
            }
        });
        setSize(1170, 630);
        Theme.makeResizable(this, 1010, 610);        // resizable (the lake picture scales), never bigger than 1200x700
        Theme.fitToScreen(this);
        setLocationRelativeTo(null);

        loop = new GameLoop(game, lake::repaint);
        lake.setLoop(loop);
        spawner = new FishSpawner(game, loop);
        weather = new WeatherThread(game, loop);
        uiTimer = new Timer(200, e -> refreshUI());

        log("Welcome to " + settings.location().getTitle() + ", " + settings.name() + "!");
        log("Click to cast, click again to set the power. F1 = help.");
        loop.start();
        spawner.start();
        if (settings.rain()) weather.start();
        uiTimer.start();
        refreshUI();
        SwingUtilities.invokeLater(lake::requestFocusInWindow);
    }

    private JPanel buildSide() {
        JPanel side = new JPanel(new BorderLayout(6, 8));
        side.setPreferredSize(new Dimension(320, 560));

        // ---- tackle box ----
        moneyLabel.setIcon(Icons.coin(24));
        moneyLabel.setFont(Theme.title(20));
        countLabel.setFont(Theme.body(Font.BOLD, 12));
        gearLabel.setFont(Theme.body(Font.PLAIN, 12));
        baitBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean f) {
                if (v instanceof CatchRecord c) {                        // a fish kept as live bait
                    super.getListCellRendererComponent(l, "Live: " + c.species().getTitle() + " " + c.sizeCm() + " cm", i, sel, f);
                    setIcon(Icons.fish(c.species(), 30, 18, false));
                    return this;
                }
                Bait b = (Bait) v;
                boolean owned = game.getAngler().owns(b);
                super.getListCellRendererComponent(l, b.getTitle() + (owned ? "" : "  ($" + b.getPrice() + " in shop)"), i, sel, f);
                setIcon(Icons.bait(b, 18));
                if (!owned && !sel) setForeground(Color.GRAY);
                return this;
            }
        });
        baitBox.setFocusable(false);
        baitBox.addItemListener(this);
        JPanel moneyRow = new JPanel(new BorderLayout());
        moneyRow.add(moneyLabel, BorderLayout.WEST);
        moneyRow.add(countLabel, BorderLayout.EAST);
        JPanel baitRow = new JPanel(new BorderLayout(6, 0));
        JLabel baitLbl = new JLabel("Bait [B]");
        baitLbl.setFont(Theme.body(Font.BOLD, 12));
        baitRow.add(baitLbl, BorderLayout.WEST);
        baitRow.add(baitBox, BorderLayout.CENTER);
        JPanel tackle = new JPanel(new GridLayout(3, 1, 0, 4));
        tackle.setBorder(new RibbonBorder("Tackle box", new Color(200, 120, 60)));
        tackle.add(moneyRow);
        tackle.add(gearLabel);
        tackle.add(baitRow);
        side.add(tackle, BorderLayout.NORTH);

        // ---- catch table with little fish pictures ----
        Theme.styleTable(catchTable);
        catchTable.setFocusable(false);
        catchTable.setFillsViewportHeight(true);
        catchTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        catchTable.getColumnModel().getColumn(1).setPreferredWidth(52);
        catchTable.getColumnModel().getColumn(2).setPreferredWidth(56);
        catchTable.getColumnModel().getColumn(3).setPreferredWidth(48);
        catchTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                Species sp = catchModel.speciesAt(r);
                setIcon(sp == null ? null : fishIcons.computeIfAbsent(sp, k -> Icons.fish(k, 40, 22, false)));
                setFont(Theme.body(Font.BOLD, 12));
                return this;
            }
        });
        JPanel catchBox = new JPanel(new BorderLayout());
        catchBox.setBorder(new RibbonBorder("Today's catch", Theme.WATER));
        catchBox.add(Theme.scroll(catchTable));

        logArea.setEditable(false);
        logArea.setFocusable(false);
        logArea.setBackground(Theme.PAPER);
        logArea.setForeground(Theme.INK);
        logArea.setFont(Theme.body(Font.PLAIN, 13));
        logArea.setMargin(new java.awt.Insets(6, 8, 6, 8));
        JPanel notes = new JPanel(new BorderLayout());
        notes.setBorder(new RibbonBorder("Notes", Theme.LEAF));
        notes.add(Theme.scroll(logArea));
        notes.setPreferredSize(new Dimension(320, 196));

        JPanel middle = new JPanel(new BorderLayout(4, 8));
        middle.add(catchBox, BorderLayout.CENTER);
        middle.add(notes, BorderLayout.SOUTH);
        side.add(middle, BorderLayout.CENTER);

        // ---- buttons ----
        JPanel buttons = new JPanel(new GridLayout(2, 3, 6, 6));
        for (FancyButton b : new FancyButton[]{shopBtn, dexBtn, helpBtn, pauseBtn, endBtn}) {
            b.addActionListener(this);
            buttons.add(b);
        }
        side.add(buttons, BorderLayout.SOUTH);
        return side;
    }

    // ================= periodic refresh (EDT) =================
    private void refreshUI() {
        String m;
        while ((m = game.pollLog()) != null) log(m);
        Angler a = game.getAngler();
        moneyLabel.setText(" " + a.moneyText());
        countLabel.setText(a.getCatches().size() + " fish  /  " + a.speciesCount() + " species");
        gearLabel.setText(a.getRod().getTitle() + " [" + a.slotsUsed() + "/" + a.slots() + "]  +  " + a.getReel().getTitle());
        catchModel.setRows(a.getCatches(), a.getVersion());
        if (baitVersion != a.getVersion()) {
            baitVersion = a.getVersion();
            rebuildBaitBox();
        }
        if (spaceDown && game.getPhase() == LakeGame.Phase.REELING) game.setReelKey(true);   // Space already held when the fish bit
        boolean paused = loop.isPaused();
        pauseBtn.setText(paused ? "Resume" : "Pause");
        pauseBtn.setIcon(paused ? Icons.play(16) : Icons.pause(16));

        if (game.isOver() && !resultShown) {
            resultShown = true;
            stopThreads();
            Timer t = new Timer(1500, e -> {
                ResultDialog.record(game);
                new ResultDialog(this, game).setVisible(true);
            });
            t.setRepeats(false);
            t.start();
        }
    }

    /** Adds a line to the notes. Important lines have a colour, and the view always scrolls to the newest one. */
    private void log(String s) {
        Color c = Theme.INK;
        String l = s.toLowerCase();
        if (l.contains("snap") || l.contains("bones") || l.contains("bit your") || l.contains("too big") || l.contains("ran away")
                || l.contains("slipped") || l.contains("bitten off")) {
            c = new Color(175, 45, 35);                                                   // trouble
        } else if (l.startsWith("caught") || l.startsWith("sold") || l.startsWith("kept") || l.startsWith("hooked") || l.contains("bought")) {
            c = new Color(35, 120, 50);                                                   // good news
        } else if (l.contains("huge") || l.contains("legend") || l.contains("rain")) {
            c = new Color(40, 95, 160);                                                   // events
        }
        javax.swing.text.StyledDocument doc = logArea.getStyledDocument();
        javax.swing.text.SimpleAttributeSet a = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setForeground(a, c);
        javax.swing.text.StyleConstants.setBold(a, c != Theme.INK);
        javax.swing.text.StyleConstants.setSpaceBelow(a, 5f);
        try {
            if (doc.getLength() > 6000) doc.remove(0, 2000);                               // forget the oldest notes
            int start = doc.getLength();
            doc.insertString(start, s + "\n", a);
            doc.setParagraphAttributes(start, s.length() + 1, a, false);
        } catch (javax.swing.text.BadLocationException e) {
            return;
        }
        SwingUtilities.invokeLater(() -> logArea.setCaretPosition(doc.getLength()));
    }

    private void stopThreads() {
        loop.shutdown();
        spawner.shutdown();
        weather.shutdown();
        lake.repaint();
    }

    /** Opens a modal dialog with the game paused, then resumes. */
    private void pausedDialog(Runnable showDialog) {
        boolean was = loop.isPaused();
        loop.setPaused(true);
        showDialog.run();
        game.setDepthMove(0);              // a key released while the dialog was open would otherwise stay "held"
        game.setBrake(false);
        loop.setPaused(was);
        refreshUI();
        lake.requestFocusInWindow();
    }

    // [PRESENT] RULE PAUSE: only sets a volatile flag; the game-loop thread keeps running but skips game.update(dt); the weather thread also waits
    // ไทย: กฎหยุดเกม: แค่ตั้งธง volatile, game-loop thread ยังวิ่งแต่ข้าม game.update(dt), weather thread รอด้วย
    private void togglePause() {
        if (game.isOver()) return;
        loop.setPaused(!loop.isPaused());
        refreshUI();
    }

    /** Fills the bait box with the normal baits and the fish kept as live bait, and selects what is on the hook. */
    private void rebuildBaitBox() {
        Angler a = game.getAngler();
        updatingBait = true;
        baitBox.removeAllItems();
        for (Bait b : Bait.values()) baitBox.addItem(b);
        for (CatchRecord c : a.getLiveBox()) baitBox.addItem(c);
        baitBox.setSelectedItem(a.isLive() ? a.getLive() : a.getBait());
        updatingBait = false;
    }

    private void nextBait() {
        Angler a = game.getAngler();
        java.util.List<Object> options = new java.util.ArrayList<>();
        for (Bait b : Bait.values()) if (a.owns(b)) options.add(b);
        options.addAll(a.getLiveBox());
        Object current = a.isLive() ? a.getLive() : a.getBait();
        int i = options.indexOf(current);
        Object next = options.get((i + 1) % options.size());
        if (next instanceof CatchRecord c) log(game.chooseLive(c));
        else log(game.chooseBait((Bait) next));
        refreshUI();
    }

    // ================= event handlers =================

    // [PRESENT] EVENT ActionListener (JButton): Shop / Fishdex / Help / Pause / End trip buttons
    // ไทย: event ActionListener (JButton): ปุ่ม Shop / Fishdex / Help / Pause / End trip
    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        // [PRESENT] REQUIRED COMPONENT JButton that opens another window: Shop / Fishdex / Help dialogs
        // ไทย: component บังคับ JButton ที่เปิดหน้าต่างอื่น: Shop / Fishdex / Help
        if (s == shopBtn) pausedDialog(() -> new ShopDialog(this, game).setVisible(true));
        else if (s == dexBtn) pausedDialog(() -> new FishdexDialog(this, game).setVisible(true));
        else if (s == helpBtn) pausedDialog(() -> new HelpDialog(this).setVisible(true));
        else if (s == pauseBtn) togglePause();
        else if (s == endBtn) {
            int[] ans = {JOptionPane.NO_OPTION};
            pausedDialog(() -> ans[0] = JOptionPane.showConfirmDialog(this, "Pack up and end this fishing trip now?",
                    "End trip", JOptionPane.YES_NO_OPTION));                 // the game waits while you decide
            if (ans[0] == JOptionPane.YES_OPTION) game.finish();
        }
        lake.requestFocusInWindow();
    }

    /** Bait combo box: switch bait (only baits you own). */
    // [PRESENT] EVENT ItemListener (JComboBox): choose the bait to use
    // ไทย: event ItemListener (JComboBox): เลือกเหยื่อที่จะใช้
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (updatingBait || e.getStateChange() != ItemEvent.SELECTED) return;
        Object item = e.getItem();
        String msg = item instanceof CatchRecord c ? game.chooseLive(c) : game.chooseBait((Bait) item);
        log(msg);
        refreshUI();
        lake.requestFocusInWindow();
    }

    /** SPACE: cast (press, press again) / strike / reel. Up arrow: pull the hook up. B: bait. R: pull line out. P: pause. */
    // [PRESENT] EVENT KeyListener: SPACE cast+reel, UP pull hook up, DOWN slow swing, B bait, R, P pause, F1 help
    // ไทย: event KeyListener (กดปุ่ม): SPACE โยน+ม้วน, UP ดึงเบ็ดขึ้น, DOWN หน่วงการแกว่ง, B เหยื่อ, P หยุดเกม, F1 วิธีเล่น
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_F1) {
            pausedDialog(() -> new HelpDialog(this).setVisible(true));
            return;
        }
        if (k == KeyEvent.VK_P) {
            togglePause();
            return;
        }
        if (game.isOver() || loop.isPaused()) return;
        switch (k) {
            case KeyEvent.VK_SPACE -> {
                if (spaceDown) return;          // ignore key auto-repeat while held
                spaceDown = true;
                switch (game.getPhase()) {
                    case IDLE -> game.startCharge();          // 1st press: zoom in + power bar
                    case CHARGING -> game.release();          // 2nd press: stop the bar and throw
                    case REELING -> game.setReelKey(true);
                    case CAUGHT -> game.continueAfterCatch();
                    default -> { }
                }
            }
            case KeyEvent.VK_UP -> game.setDepthMove(-1);       // hold: pull the hook up
            case KeyEvent.VK_DOWN -> game.setBrake(true);        // hold: slow the swing of the lure
            case KeyEvent.VK_B -> {
                if (game.getPhase() == LakeGame.Phase.CAUGHT) game.keepCatchAsBait();      // keep the fish as live bait
                else nextBait();
            }
            case KeyEvent.VK_R -> game.reelIn();
            default -> { }
        }
    }

    // [PRESENT] EVENT KeyListener (release): stop reeling / pulling / braking
    // ไทย: event KeyListener (ปล่อยปุ่ม): หยุดม้วน / หยุดดึง / หยุดหน่วง
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spaceDown = false;
            if (game.getPhase() == LakeGame.Phase.REELING) game.setReelKey(false);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            game.setBrake(false);
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            game.setDepthMove(0);                                // key released: the hook sinks again
        }
    }

    @Override public void keyTyped(KeyEvent e) { }

    // ================= catch table =================
    private class CatchModel extends AbstractTableModel {
        private final String[] cols = {"Fish", "Size", "Value", "Time"};
        private List<CatchRecord> rows = List.of();
        private int lastSize = -1, lastVersion = -1;

        /** Reloads the table when a fish was added or sold/kept. */
        void setRows(List<CatchRecord> r, int version) {
            if (r.size() == lastSize && version == lastVersion) return;
            rows = List.copyOf(r);
            lastSize = r.size();
            lastVersion = version;
            fireTableDataChanged();
        }

        Species speciesAt(int r) { return r >= 0 && r < rows.size() ? rows.get(r).species() : null; }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            CatchRecord cr = rows.get(r);
            return switch (c) {
                case 0 -> cr.species().getTitle() + (cr.newSpecies() ? " *" : "");
                case 1 -> cr.sizeCm() + " cm";
                case 2 -> game.getAngler().fateText(cr);
                default -> cr.clock();
            };
        }
    }
}
