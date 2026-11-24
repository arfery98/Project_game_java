// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/** A panel whose background is painted as wood planks or as paper. */
// [PRESENT] OOP INHERITANCE: custom JPanel with a wood look
// ไทย: OOP inheritance: JPanel แบบกำหนดเอง ลายไม้
public class WoodPanel extends JPanel {

    public enum Kind { WOOD, PAPER }

    private final Kind kind;

    public WoodPanel(LayoutManager layout, Kind kind) {
        super(layout);
        this.kind = kind;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (kind == Kind.WOOD) Theme.paintWood(g2, 0, 0, getWidth(), getHeight(), 0);
        else Theme.paintPaper(g2, 0, 0, getWidth(), getHeight(), 0);
    }
}
