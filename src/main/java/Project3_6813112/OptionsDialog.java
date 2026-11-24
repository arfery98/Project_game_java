// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Options: sound, camera and gameplay settings. Every change is applied at once to GameOptions. */
public class OptionsDialog extends JDialog implements ActionListener, ChangeListener {

    // [PRESENT] REQUIRED COMPONENT JCheckBox x5: sound, fish finder, easy reeling, rain, day/night
    // ไทย: component บังคับ JCheckBox 5 ตัว: เสียง, fish finder, reel ง่าย, ฝน, กลางวันกลางคืน
    private final JCheckBox soundBox = new JCheckBox("Sound effects");
    // [PRESENT] EXTRA COMPONENT JSlider: sound volume
    // ไทย: extra component JSlider: ปรับระดับเสียง
    private final JSlider volumeSlider = new JSlider(0, 100, 60);
    private final JLabel volumeLabel = new JLabel();
    private final JCheckBox finderBox = new JCheckBox("Fish finder - show names when you hover (off = surprise)");
    private final JCheckBox easyBox = new JCheckBox("Easy reeling - more forgiving line");
    private final JCheckBox rainBox = new JCheckBox("Rain showers");
    private final JCheckBox dayNightBox = new JCheckBox("Day and night cycle");
    private final FancyButton resetButton = new FancyButton("Reset", Icons.cog(18), FancyButton.Style.WOOD);
    private final FancyButton doneButton = new FancyButton("Done", Icons.play(18), FancyButton.Style.GREEN);
    private boolean loading;

    public OptionsDialog(Window owner) {
        super(owner, "Options", ModalityType.APPLICATION_MODAL);
        setIconImage(Logo.icon(32));

        JPanel sound = new JPanel(new GridLayout(2, 1, 0, 6));
        sound.setBorder(new RibbonBorder("Sound", Theme.WATER));
        JPanel volRow = new JPanel(new BorderLayout(8, 0));
        JLabel volName = new JLabel("Volume");
        volName.setFont(Theme.body(Font.BOLD, 13));
        volName.setPreferredSize(new Dimension(70, 24));
        volumeLabel.setPreferredSize(new Dimension(44, 24));
        volRow.add(volName, BorderLayout.WEST);
        volRow.add(volumeSlider, BorderLayout.CENTER);
        volRow.add(volumeLabel, BorderLayout.EAST);
        sound.add(soundBox);
        sound.add(volRow);

        JPanel play = new JPanel(new GridLayout(4, 1, 0, 6));
        play.setBorder(new RibbonBorder("Gameplay", new java.awt.Color(200, 120, 60)));
        play.add(finderBox);
        play.add(easyBox);
        play.add(rainBox);
        play.add(dayNightBox);

        JPanel cards = new JPanel(new GridLayout(0, 1, 0, 8));
        cards.add(sound);
        cards.add(play);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(resetButton);
        buttons.add(doneButton);
        JLabel tip = new JLabel("Changes apply to the next trip.");
        tip.setFont(Theme.body(Font.ITALIC, 11));
        JPanel south = new JPanel(new BorderLayout());
        south.add(tip, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.EAST);

        WoodPanel root = new WoodPanel(new BorderLayout(8, 8), WoodPanel.Kind.WOOD);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        root.add(cards, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        Theme.transparent(root);
        tip.setForeground(Theme.PAPER);

        for (JCheckBox b : new JCheckBox[]{soundBox, finderBox, easyBox, rainBox, dayNightBox}) b.addActionListener(this);
        volumeSlider.addChangeListener(this);
        resetButton.addActionListener(this);
        doneButton.addActionListener(this);
        load();

        setSize(520, 430);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /** Copies GameOptions into the controls. */
    private void load() {
        loading = true;
        soundBox.setSelected(GameOptions.sound);
        volumeSlider.setValue(GameOptions.volume);
        volumeSlider.setEnabled(GameOptions.sound);
        volumeLabel.setText(GameOptions.volume + "%");
        finderBox.setSelected(GameOptions.fishFinder);
        easyBox.setSelected(GameOptions.easyReel);
        rainBox.setSelected(GameOptions.rain);
        dayNightBox.setSelected(GameOptions.dayNight);
        loading = false;
    }

    /** Copies the controls into GameOptions. */
    private void store() {
        GameOptions.sound = soundBox.isSelected();
        GameOptions.volume = volumeSlider.getValue();
        GameOptions.fishFinder = finderBox.isSelected();
        GameOptions.easyReel = easyBox.isSelected();
        GameOptions.rain = rainBox.isSelected();
        GameOptions.dayNight = dayNightBox.isSelected();
        GameOptions.apply();
    }

    // [PRESENT] EVENT ActionListener: the 5 JCheckBox options, Reset, Done
    // ไทย: event ActionListener: ตัวเลือก JCheckBox 5 ตัว, ปุ่ม Reset, ปุ่ม Done
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == doneButton) {
            dispose();
        } else if (e.getSource() == resetButton) {
            GameOptions.reset();
            load();
        } else if (!loading) {
            volumeSlider.setEnabled(soundBox.isSelected());
            store();
        }
    }

    /** Volume slider: update the label at once and play a test beep when the slider is released. */
    // [PRESENT] EVENT ChangeListener (JSlider): volume changes while dragging
    // ไทย: event ChangeListener (JSlider): ระดับเสียงเปลี่ยนขณะลาก
    @Override
    public void stateChanged(ChangeEvent e) {
        if (loading) return;
        volumeLabel.setText(volumeSlider.getValue() + "%");
        store();
        if (!volumeSlider.getValueIsAdjusting()) SoundFX.coin();
    }
}
