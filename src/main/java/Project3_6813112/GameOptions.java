// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/**
 * Settings shared by every screen (changed in the Options dialog, read when a trip starts).
 * Kept in one place so saving them to a file later only needs load() / save() here.
 */
public final class GameOptions {

    public static boolean sound;
    public static int volume;
    public static boolean fishFinder;   // show fish names on hover
    public static boolean easyReel;     // more forgiving reeling
    public static boolean rain;         // rain showers
    public static boolean dayNight;     // day and night cycle

    static {
        reset();
    }

    private GameOptions() { }

    /** Back to the factory settings. */
    public static void reset() {
        sound = true;
        volume = 60;
        fishFinder = false;
        easyReel = false;
        rain = true;
        dayNight = true;
        apply();
    }

    /** Pushes the sound settings to the sound engine. */
    public static void apply() {
        SoundFX.setEnabled(sound);
        SoundFX.setVolume(volume);
    }

    /** One-line text for the "current options" summary. */
    public static String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(sound ? "Sound " + volume + "%" : "Sound off");
        if (easyReel) sb.append("  |  Easy reeling");
        if (fishFinder) sb.append("  |  Fish finder on");
        if (!rain) sb.append("  |  No rain");
        if (!dayNight) sb.append("  |  Always daytime");
        return sb.toString();
    }

    // Later: public static void load() / save() using java.util.Properties for "Continue" and saved options.
}
