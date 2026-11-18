// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Cute 8-bit sound effects synthesized in code - no audio files.
 * Plays on its own background thread so the game never waits for audio.
 */
public final class SoundFX {

    private static final float RATE = 22050f;
    private static volatile boolean enabled = true;
    private static volatile int volume = 60;   // 0..100
    private static final java.util.concurrent.atomic.AtomicInteger PENDING = new java.util.concurrent.atomic.AtomicInteger();
    // [PRESENT] THREAD 4 sound-fx: one background thread plays the generated sounds (max 3 waiting)
    private static final ExecutorService AUDIO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sound-fx");
        t.setDaemon(true);
        return t;
    });

    private SoundFX() { }

    public static void setEnabled(boolean on) { enabled = on; }
    public static void setVolume(int v) { volume = Math.max(0, Math.min(100, v)); }

    /** Plays pairs of (frequency Hz, duration ms). Frequency 0 = silence, negative = noise. */
    public static void play(int... freqMs) {
        if (!enabled || volume == 0) return;
        if (PENDING.get() >= 3) return;          // too many sounds waiting: skip this one instead of playing it late
        PENDING.incrementAndGet();
        AUDIO.submit(() -> {
            try {
                synth(freqMs);
            } finally {
                PENDING.decrementAndGet();
            }
        });
    }

    private static void synth(int[] seq) {
        try {
            AudioFormat fmt = new AudioFormat(RATE, 8, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt, 2048);
                line.start();
                int amp = Math.max(1, 30 * volume / 100);
                java.util.Random noise = new java.util.Random();
                for (int i = 0; i + 1 < seq.length; i += 2) {
                    int freq = seq[i], ms = seq[i + 1];
                    byte[] buf = new byte[(int) (RATE * ms / 1000)];
                    for (int j = 0; j < buf.length; j++) {
                        int a = buf.length - j < 200 ? amp * (buf.length - j) / 200 : amp;
                        if (freq > 0) {
                            double half = RATE / (2.0 * freq);
                            buf[j] = (byte) (((int) (j / half) % 2 == 0) ? a : -a);
                        } else if (freq < 0) {
                            buf[j] = (byte) (noise.nextInt(2 * a + 1) - a);
                        }
                    }
                    line.write(buf, 0, buf.length);
                }
                line.drain();
            }
        } catch (Exception ignored) {
            // no audio device - play silently
        }
    }

    public static void cast()    { play(500, 30, 700, 30, 900, 30, 1200, 40); }
    public static void splash()  { play(-1, 120); }
    public static void nibble()  { play(1500, 18); }
    public static void bite()    { play(1000, 60, 1400, 90); }
    public static void hooked()  { play(600, 50, 900, 50, 1200, 80); }
    public static void reel()    { play(2200, 8); }
    public static void caught()  { play(784, 80, 988, 80, 1175, 160); }
    public static void fanfare() { play(523, 100, 659, 100, 784, 100, 1046, 300, 0, 60, 988, 100, 1046, 400); }
    public static void snap()    { play(-1, 60, 300, 80, 200, 200); }
    public static void lose()    { play(500, 90, 350, 140); }
    public static void coin()    { play(1320, 50, 1760, 100); }
}
