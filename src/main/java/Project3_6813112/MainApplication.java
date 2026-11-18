// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import javax.swing.SwingUtilities;

/** Program entry point: applies the theme, then opens the title screen (first frame). */
public class MainApplication {

    public static void main(String[] args) {
        Theme.installLookAndFeel();
        SwingUtilities.invokeLater(() -> new TitleFrame().setVisible(true));
    }
}
