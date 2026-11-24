// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Program entry point: applies the theme, then opens the title screen (first frame). */
// [PRESENT] REQUIRED: start class MainApplication (own file)
// ไทย: คลาสเริ่มต้นชื่อ MainApplication อยู่ในไฟล์ของตัวเอง (ตามที่โจทย์กำหนด)
public class MainApplication {

    public static void main(String[] args) {
        // Last safety net: an unexpected error in any thread is reported once in a friendly window
        // instead of the program silently stopping.
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Something unexpected happened in the game (" + error.getClass().getSimpleName() + ").\n"
                            + "You can keep playing; if the game behaves strangely, restart it.",
                    "Fish Goes Fishing", JOptionPane.WARNING_MESSAGE));
        });
        Theme.installLookAndFeel();
        SwingUtilities.invokeLater(() -> new TitleFrame().setVisible(true));
    }
}
