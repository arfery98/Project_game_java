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

    public HelpDialog(Window owner) {
        super(owner, "How to Play", ModalityType.APPLICATION_MODAL);
        Logo.applyIcon(this);
        JTextArea area = new JTextArea(TEXT + "\nGROUP MEMBERS\n    " + String.join("\n    ", Credits.MEMBERS));
        area.setEditable(false);
        area.setLineWrap(true);                 // long lines wrap instead of adding a sideways scroll bar
        area.setWrapStyleWord(true);
        area.setFont(Theme.body(Font.PLAIN, 14));
        area.setForeground(Theme.INK);
        area.setBackground(Theme.PAPER);
        area.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        area.setCaretPosition(0);
        JScrollPane scroll = Theme.scroll(area);
        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new RibbonBorder("How to Play", Theme.WATER));
        card.add(scroll);

        FancyButton close = new FancyButton("Let's fish!", Icons.rod(18), FancyButton.Style.GREEN);
        close.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(close);

        WoodPanel root = new WoodPanel(new BorderLayout(8, 8), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(card, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        setSize(680, 640);
        Theme.fitToScreen(this);
        setLocationRelativeTo(owner);
    }
}
