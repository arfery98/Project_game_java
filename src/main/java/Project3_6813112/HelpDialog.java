// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** "How to play" instructions on a paper card. */
public class HelpDialog extends JDialog {

    private static final String TEXT = """
            Catch as many (and as valuable) fish as you can.
            There is no time limit - press END TRIP when you want to stop.

            1.  CAST
                CLICK once (or press SPACE): the camera zooms in and a POWER bar
                appears. A marker sweeps up and down the green-to-red bar.
                CLICK again (or SPACE) to stop it - the higher the marker,
                the further the line flies. Once the line is in the water the
                view changes to the side view.

            2.  WATCH THE HOOK
                The lake is wide: the camera zooms in and follows the hook.
                The hook swings on a circle around the tip of your rod,
                braked by the water, until the line hangs straight
                down from the rod.
                Hold the DOWN arrow to slow the swing so fish can catch up.
                A longer cast makes a longer swing, so the hook hangs deeper:
                a short cast for shallow fish, a long cast for deep ones.
                You only see the part of the lake around the hook - you
                cannot tell where the other fish are!
                Until a fish bites you can only HOLD the UP arrow to pull
                the hook back up to a shallower depth.

            3.  WAIT FOR THE BITE
                A fish swims up to the lure (it glows). The moment it
                touches the lure it is HOOKED AUTOMATICALLY - there is
                no strike button. Be ready to hold the reel!

            4.  REEL IT IN
                HOLD the mouse button or SPACE to reel. Watch the
                tension gauge:
                    green  = perfect
                    red    = the line is about to SNAP - let go for a moment!
                    grey   = too slack - keep reeling or the fish slips off
                When the fish fights hard, release a little.
                BEWARE: a bigger fish (red glow) may rush in and eat
                your catch on the way up! Each bite takes a piece
                (tail first) and lowers the price; after 3 bites
                only bones are left.

            5.  SELL OR KEEP
                A caught fish is shown on a card. Choose:
                    SELL (Space)       - get its money
                    KEEP AS BAIT (B)   - it goes on your hook as LIVE BAIT
                A kept fish is LIVE BAIT: it is used up when a fish takes it.
                Legendary fish cannot be bait. The bait box holds 5 fish.
                Pick bait with the Bait box or the B key.

            6.  UPGRADE
                Spend your money in the SHOP:
                    Lures - only the golden lure is sold; other bait comes from fish
                    Rods  - stronger rods tame big fish
                    Reels - reel in faster
                Legendary fish only bite the GOLDEN LURE.

            OTHER KEYS:  B next bait (or keep the fish on the card)
                         UP pull the lure up   DOWN slow the swing   P pause   F1 help

            SIZE CLASSES: every kind of fish is Small, Medium,
            Large or Giant (see the Fishdex menu). You never see the
            fish on the line, only a coloured lure.
              Purple lure (free)   catches Small fish
              a kept Small fish    catches Medium fish
              a kept Medium fish   catches Large fish
              a kept Large fish    catches Giant fish
              Golden lure (shop)   legends only
            So catch a small fish, press B to keep it as bait, and
            use it to hunt the next size. Same size or bigger fish
            will not bite ("Need bigger bait!" = bait too small).

            DEPTH LAYERS: Small and Medium fish live in the upper water
            (down to about 400 cm). Large fish live from 400 cm down,
            Giant fish from 1000 cm, legends from 1500 cm. Cast farther
            so the lure hangs deeper for the bigger fish!

            RODS: each rod is made for fish up to a size class and
            casts farther, so the lure sinks deeper (Shop > Rods):
              Bamboo (free)   up to Medium    ~900 cm deep    1 slot
              Carbon          up to Large     ~1800 cm        2 slots
              Pro             up to Giant     ~3000 cm        3 slots
              Legend          legends too     ~5000 cm        4 slots
            A bigger fish CAN bite, but with a weak rod the tension
            jumps into the red and the line snaps - and the bait is
            lost. A snap can also bite off the golden lure.
            PARTS (Shop > Parts) fit into the rod's slots and give it
            skills: stronger line, shock absorber, longer cast,
            lure charm, bait saver, fish guard, lucky charm, heavy core.

            Rain makes fish hungrier. Some fish only come out at night.
            Fish are a surprise until you land them. Turn on "Fish finder" in Options
            if you want to see a fish's name when you hover over it.
            """;

    private static final String TEXT_TH = """
            จับปลาให้ได้มากและมีมูลค่าสูงที่สุด
            ไม่มีเวลาจำกัด กดปุ่ม END TRIP เมื่ออยากหยุด

            1.  โยนเบ็ด (CAST)
                คลิก (หรือกด SPACE) หนึ่งครั้ง: กล้องจะซูมเข้าและแถบพลัง (POWER)
                จะปรากฏขึ้น ตัวชี้จะวิ่งขึ้นลงบนแถบสีเขียวถึงแดง
                คลิกอีกครั้ง (หรือกด SPACE) เพื่อหยุด ยิ่งตัวชี้อยู่สูง
                สายเบ็ดยิ่งพุ่งไกล เมื่อสายตกถึงน้ำ ภาพจะเปลี่ยนเป็นมุมมองด้านข้าง

            2.  ดูเบ็ด
                ทะเลสาบกว้าง กล้องจะซูมและตามเบ็ดไป
                เบ็ดจะแกว่งเป็นวงกลมรอบปลายคัน ช้าลงเพราะแรงต้านของน้ำ
                จนสายห้อยลงตรงๆ จากคัน
                กดลูกศรลงค้างไว้เพื่อหน่วงการแกว่ง ให้ปลาตามทัน
                ยิ่งโยนไกล เบ็ดยิ่งแกว่งไกลและห้อยลึกขึ้น:
                โยนสั้นสำหรับปลาน้ำตื้น โยนไกลสำหรับปลาน้ำลึก
                คุณเห็นแค่ส่วนของทะเลสาบรอบๆ เบ็ด
                จึงไม่รู้ว่าปลาตัวอื่นอยู่ที่ไหน!
                ระหว่างรอปลากิน คุณทำได้แค่กดลูกศรขึ้นค้างไว้
                เพื่อดึงเบ็ดขึ้นมาให้ตื้นขึ้น

            3.  รอปลากิน
                ปลาจะว่ายมาที่เหยื่อ (ตัวมันเรืองแสง) พอแตะเหยื่อ
                มันจะติดเบ็ดเองทันที — ไม่มีปุ่มกระตุกเบ็ด
                เตรียมกดดึงให้พร้อม!

            4.  ดึงปลาขึ้น
                กดเมาส์หรือ SPACE ค้างไว้เพื่อดึง และดูมาตรวัดความตึง:
                    สีเขียว = พอดี
                    สีแดง  = สายใกล้ขาด ปล่อยสักครู่!
                    สีเทา  = หย่อนเกินไป ดึงต่อ ไม่อย่างนั้นปลาจะหลุด
                เมื่อปลาสู้แรง ให้ผ่อนปล่อยเล็กน้อย
                ระวัง: ปลาที่ใหญ่กว่า (เรืองแสงสีแดง) อาจพุ่งมากินปลาของคุณ
                ระหว่างที่ดึงขึ้นมา! แต่ละครั้งที่โดนงับจะเสียไปหนึ่งชิ้น
                (เริ่มจากหาง) และราคาลดลง ถ้าโดนงับครบ 3 ครั้ง
                จะเหลือแต่ก้าง

            5.  ขายหรือเก็บ
                ปลาที่จับได้จะแสดงบนการ์ด เลือกได้ว่า:
                    SELL (Space)       - ขายเอาเงิน
                    KEEP AS BAIT (B)   - เก็บไว้เป็นเหยื่อเป็น (LIVE BAIT)
                                         ติดที่ตะขอ
                เหยื่อเป็นจะถูกใช้หมดเมื่อมีปลามากิน
                ปลาในตำนานใช้เป็นเหยื่อไม่ได้ กล่องเหยื่อเก็บได้ 5 ตัว
                เลือกเหยื่อจากช่อง Bait หรือกดปุ่ม B

            6.  อัปเกรด
                ใช้เงินในร้านค้า (SHOP):
                    Lures - ขายแค่เหยื่อทอง เหยื่ออื่นได้จากปลาที่จับได้
                    Rods  - คันที่แข็งแรงกว่าคุมปลาใหญ่ได้
                    Reels - ดึงสายเร็วขึ้น
                ปลาในตำนานกินเฉพาะเหยื่อทอง (GOLDEN LURE)

            ปุ่มอื่นๆ:  B เปลี่ยนเหยื่อ (หรือเก็บปลาบนการ์ดเป็นเหยื่อ)
                        ลูกศรขึ้น ดึงเหยื่อขึ้น   ลูกศรลง หน่วงการแกว่ง
                        P หยุดเกม   F1 เปิดวิธีเล่น

            ไซส์ของปลา: ปลาทุกชนิดเป็น Small, Medium, Large หรือ Giant
            (ดูได้ในเมนู Fishdex) คุณจะไม่เห็นตัวปลาที่ติดเบ็ด
            เห็นแค่สีของเหยื่อ
              เหยื่อสีม่วง (ฟรี)         จับปลา Small
              ปลา Small ที่เก็บไว้       จับปลา Medium
              ปลา Medium ที่เก็บไว้      จับปลา Large
              ปลา Large ที่เก็บไว้       จับปลา Giant
              เหยื่อทอง (ร้านค้า)        เฉพาะปลาในตำนาน
            ดังนั้นให้จับปลาเล็กๆ แล้วกด B เก็บไว้เป็นเหยื่อ
            เพื่อไปล่าปลาไซส์ถัดไป ปลาไซส์เท่ากันหรือใหญ่กว่า
            จะไม่กินเหยื่อ ("Need bigger bait!" = เหยื่อเล็กเกินไป)

            ชั้นความลึก: ปลา Small และ Medium อยู่น้ำตื้น
            (ลึกไม่เกินประมาณ 400 ซม.) ปลา Large อยู่ตั้งแต่ 400 ซม. ลงไป
            ปลา Giant ตั้งแต่ 1000 ซม. และปลาในตำนานตั้งแต่ 1500 ซม.
            โยนให้ไกลขึ้นเพื่อให้เหยื่อห้อยลึกลงไปหาปลาตัวใหญ่!

            คันเบ็ด: แต่ละคันทำมาสำหรับปลาถึงไซส์หนึ่ง และโยนได้ไกลขึ้น
            เหยื่อจึงจมลึกขึ้น (Shop > Rods):
              Bamboo (ฟรี)   ถึง Medium       ลึก ~900 ซม.     1 ช่อง
              Carbon         ถึง Large        ลึก ~1800 ซม.    2 ช่อง
              Pro            ถึง Giant        ลึก ~3000 ซม.    3 ช่อง
              Legend         รวมปลาในตำนาน   ลึก ~5000 ซม.    4 ช่อง
            ปลาที่ใหญ่เกินคันก็ยังกินเบ็ดได้ แต่ความตึงจะพุ่งเข้าโซนแดง
            สายจะขาด และเหยื่อหายไป สายขาดอาจทำให้เหยื่อทองหายด้วย
            PARTS (Shop > Parts) ใส่ในช่องของคันเพื่อเพิ่มความสามารถ:
            สายแข็งแรง ตัวซับแรงกระชาก ตัวช่วยโยนไกล เสน่ห์เหยื่อ
            ตัวประหยัดเหยื่อ ตัวกันปลานักล่า เครื่องรางนำโชค และแกนหนัก

            ฝนทำให้ปลาหิวมากขึ้น ปลาบางชนิดออกมาเฉพาะกลางคืน
            ปลาแต่ละตัวเป็นปริศนาจนกว่าจะจับขึ้นมาได้
            เปิด "Fish finder" ใน Options ถ้าอยากเห็นชื่อปลาเมื่อเอาเมาส์ไปชี้
            """;

    private static boolean thai;           // the language is remembered while the program runs
    private HelpGuide guide;               // the animated pictures (stopped when the window closes)

    public HelpDialog(Window owner) {
        super(owner, thai ? "วิธีเล่น" : "How to Play", ModalityType.APPLICATION_MODAL);
        Logo.applyIcon(this);
        JTextArea area = new JTextArea(fullText());
        area.setEditable(false);
        area.setLineWrap(true);                 // long lines wrap instead of adding a sideways scroll bar
        area.setWrapStyleWord(true);
        area.setFont(textFont());
        area.setForeground(Theme.INK);
        area.setBackground(Theme.PAPER);
        area.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        area.setCaretPosition(0);
        JScrollPane scroll = Theme.scroll(area);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        guide = new HelpGuide(thai);
        JScrollPane pictures = Theme.scroll(guide);
        pictures.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pictures.getVerticalScrollBar().setUnitIncrement(18);
        final javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        tabs.addTab(thai ? "ภาพประกอบ" : "Pictures", pictures);
        tabs.addTab(thai ? "คำอธิบายเต็ม" : "Full guide", scroll);
        tabs.setFont(thai ? thaiFont(Font.BOLD, 13) : Theme.body(Font.BOLD, 13));

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new RibbonBorder("How to Play", Theme.WATER));
        card.add(tabs);

        FancyButton close = new FancyButton(thai ? "ไปตกปลากัน!" : "Let's fish!", Icons.rod(18), FancyButton.Style.GREEN);
        close.addActionListener(e -> dispose());
        FancyButton language = new FancyButton(thai ? "English" : "ภาษาไทย", Icons.book(18), FancyButton.Style.BLUE);
        final Font latinFont = close.getFont();                 // the button font that has the English letters
        applyButtonFont(close, latinFont);
        applyButtonFont(language, latinFont);
        // [PRESENT] EVENT ActionListener (lambda): switch the How to Play text between English and Thai
        language.addActionListener(e -> {
            thai = !thai;
            area.setText(fullText());
            area.setFont(textFont());
            area.setCaretPosition(0);
            guide.setThai(thai);
            tabs.setFont(thai ? thaiFont(Font.BOLD, 13) : Theme.body(Font.BOLD, 13));
            tabs.setTitleAt(0, thai ? "ภาพประกอบ" : "Pictures");
            tabs.setTitleAt(1, thai ? "คำอธิบายเต็ม" : "Full guide");
            language.setText(thai ? "English" : "ภาษาไทย");
            close.setText(thai ? "ไปตกปลากัน!" : "Let's fish!");
            applyButtonFont(close, latinFont);
            applyButtonFont(language, latinFont);
            setTitle(thai ? "วิธีเล่น" : "How to Play");
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(close);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(language);
        JPanel south = new JPanel(new BorderLayout());
        south.add(left, BorderLayout.WEST);
        south.add(right, BorderLayout.EAST);

        WoodPanel root = new WoodPanel(new BorderLayout(8, 8), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(card, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        setSize(720, 660);
        Theme.fitToScreen(this);
        setLocationRelativeTo(owner);
    }

    /** The instructions in the chosen language, followed by the group members. */
    private static String fullText() {
        return (thai ? TEXT_TH + "\nสมาชิกกลุ่ม\n    " : TEXT + "\nGROUP MEMBERS\n    ") + String.join("\n    ", Credits.MEMBERS);
    }

    /** The text font: the game font for English, a font that really has Thai letters for Thai. */
    private static Font textFont() {
        return thai ? thaiFont(Font.PLAIN, 14) : Theme.body(Font.PLAIN, 14);
    }

    /** Gives a button a Thai-capable font while its text is Thai, and its normal font otherwise. */
    private static void applyButtonFont(javax.swing.AbstractButton b, Font latin) {
        boolean hasThai = b.getText().chars().anyMatch(c -> c >= 0x0E00 && c <= 0x0E7F);
        b.setFont(hasThai ? thaiFont(latin.getStyle(), latin.getSize()) : latin);
    }

    /** The first installed font that can draw Thai letters (the game's own font, Segoe UI, cannot). */
    static Font thaiFont(int style, int size) {
        for (String name : new String[]{"Leelawadee UI", "Tahoma", "Noto Sans Thai", "Thonburi", "Sarabun", "Dialog"}) {
            Font f = new Font(name, style, size);
            if (f.canDisplay('ก')) return f;          // the letter "ก"
        }
        return new Font(Font.DIALOG, style, size);
    }

    @Override
    public void dispose() {
        if (guide != null) guide.stop();
        super.dispose();
    }
}
