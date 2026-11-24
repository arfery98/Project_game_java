// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Soft, rounded sound effects synthesized in code (sine tones and filtered noise) - no audio files.
 * Plays on its own background thread so the game never waits for audio.
 */
public final class SoundFX {

    private static final float RATE = 22050f;
    private static volatile boolean enabled = true;
    private static volatile int volume = 60;   // 0..100
    private static final java.util.concurrent.atomic.AtomicInteger PENDING = new java.util.concurrent.atomic.AtomicInteger();
    // [PRESENT] THREAD 4 sound-fx: one background thread plays the generated sounds (max 3 waiting)
    // ไทย: thread ที่ 4 sound-fx: thread เบื้องหลังเดียวเล่นเสียงที่สร้างจากโค้ด (รอคิวได้สูงสุด 3)
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

    // [PRESENT] RULE SOUND FROM CODE: no audio files; builds 16-bit samples (sine tones and filtered noise) and plays them through javax.sound.sampled SourceDataLine
    // ไทย: กฎเสียงจากโค้ด: ไม่มีไฟล์เสียง สร้างตัวอย่างเสียง 16 บิต (คลื่นไซน์และ noise กรอง) แล้วเล่นผ่าน SourceDataLine ของ javax.sound.sampled
    private static void synth(int[] seq) {
        try {
            AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt, 4096);
                line.start();
                double gain = 9000.0 * volume / 100.0;           // soft: about 27% of full scale at volume 100
                java.util.Random noise = new java.util.Random();
                double lowpass = 0, crunchLo = 0, crunchHi = 0, gate = 1;     // filters for the noise sounds
                for (int i = 0; i + 1 < seq.length; i += 2) {
                    int freq = seq[i], ms = seq[i + 1];
                    int n = (int) (RATE * ms / 1000);
                    boolean crunch = freq == -2;                            // -2 = a crunchy bite (the "eating" sound)
                    int fadeIn = Math.min(n / 2, (int) (RATE * (crunch ? 0.001 : 0.008)));     // soft start: no click
                    int fadeOut = Math.min(n, (int) (RATE * (crunch ? 0.030 : 0.070)));        // the note dies away gently
                    byte[] buf = new byte[n * 2];
                    double phase = 0;
                    for (int j = 0; j < n; j++) {
                        double env = 1.0;
                        if (j < fadeIn) env = Math.sin(Math.PI / 2 * j / fadeIn);
                        int left = n - j;
                        if (left < fadeOut) env *= Math.sin(Math.PI / 2 * left / fadeOut);
                        double v = 0;
                        if (freq > 0) {
                            phase += 2 * Math.PI * freq / RATE;
                            v = 0.85 * Math.sin(phase) + 0.15 * Math.sin(2 * phase);   // round, warm tone (a square wave is harsh)
                        } else if (crunch) {
                            double w = noise.nextDouble() * 2 - 1;
                            crunchLo += (w - crunchLo) * 0.06;
                            crunchHi += (w - crunchHi) * 0.40;
                            if (j % 33 == 0) gate = noise.nextDouble() < 0.55 ? 1.0 : 0.25;     // crackle: breaks the noise into small crunches
                            double t = j / RATE;
                            v = (crunchHi - crunchLo) * gate * 2.8 * Math.exp(-t / 0.024)           // dry crunch (about 400 Hz - 3 kHz)
                                    + 0.55 * Math.sin(2 * Math.PI * (150 - 380 * t) * t) * Math.exp(-t / 0.03);   // low "thump" of the bite
                        } else if (freq < 0) {
                            lowpass += (noise.nextDouble() * 2 - 1 - lowpass) * 0.10;
                            v = lowpass * 2.4;
                        }
                        int sample = (int) (v * env * gain);
                        buf[2 * j] = (byte) sample;
                        buf[2 * j + 1] = (byte) (sample >> 8);
                    }
                    line.write(buf, 0, buf.length);
                }
                line.drain();
            }
        // [PRESENT] EXCEPTION: no sound device = the game runs on without sound
        // ไทย: จัดการ exception: ไม่มีอุปกรณ์เสียง = เกมเล่นต่อได้โดยไม่มีเสียง
        } catch (LineUnavailableException | IllegalArgumentException e) {
            // no audio device (or no 16-bit line): the game is still fully playable, so switch the sound off
            if (enabled) System.err.println("Sound is not available on this computer: " + e.getMessage());
            enabled = false;
        } catch (RuntimeException e) {
            enabled = false;          // any other audio problem: play silently
        }
    }

    public static void cast()    { play(330, 45, 392, 45, 494, 45, 659, 70); }
    public static void splash()  { play(-1, 140); }
    public static void nibble()  { play(880, 45); }
    public static void bite()    { eat(); }

    /** Crunchy "nom nom nom" - a fish eating the bait, or a big fish biting your catch. */
    public static void eat()     { play(-2, 90, 0, 45, -2, 85, 0, 50, -2, 100, 0, 45, -2, 75); }
    public static void hooked()  { play(440, 60, 587, 60, 784, 100); }

    /** One reel click (kept for completeness); the game uses the continuous reel sound, see setReeling(). */
    public static void reel()    { play(520, 24, 0, 8, 700, 20); }

    // ------------------------------------------------------------------------------------------
    // Continuous sounds: the reel while the button is held, and the power bar while it sweeps.
    // One line stays open and a background thread keeps writing the sound, so there is no gap,
    // and it stops by itself when the game stops asking for it (button released, bar stopped, game paused).
    // ------------------------------------------------------------------------------------------
    private static volatile long reelAskedAt = -1_000_000_000L;
    private static volatile long powerAskedAt = -1_000_000_000L;
    private static volatile double powerLevel;
    private static Thread loopThread;
    private static volatile boolean loopBroken;

    /** Called by the game every frame while the reel button is held down. Silence follows by itself when it stops being called. */
    public static void setReeling(boolean on) {
        if (!on) { reelAskedAt = -1_000_000_000L; return; }
        reelAskedAt = System.nanoTime();
        startLoopThread();
    }

    /** Called every frame while the power bar sweeps: level 0 = green end, 1 = red end. The pitch follows the marker. */
    public static void setPower(double level) {
        if (level < 0) { powerAskedAt = -1_000_000_000L; return; }
        powerLevel = Math.min(1, level);
        powerAskedAt = System.nanoTime();
        startLoopThread();
    }

    private static synchronized void startLoopThread() {
        if (loopThread != null || loopBroken) return;
        loopThread = new Thread(SoundFX::loopSounds, "sound-loop");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    private static boolean recent(long askedAt) {
        return System.nanoTime() - askedAt < 130_000_000L;          // asked within the last 130 ms
    }

    private static void loopSounds() {
        try {
            AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt, 2048);
                line.start();
                final int chunk = 441;                                   // 20 ms per write
                byte[] buf = new byte[chunk * 2];
                java.util.Random noise = new java.util.Random();
                double reelGain = 0, powerGain = 0;                      // smooth fade in / out: no clicks
                double clickAge = 1, clickClock = 0, hum = 0, tone = 0, freq = 262, band = 0, bandLo = 0;
                int clickNo = 0;
                while (true) {
                    boolean on = enabled && volume > 0;
                    boolean reel = on && recent(reelAskedAt);
                    boolean power = on && recent(powerAskedAt);
                    if (!reel && !power && reelGain < 0.002 && powerGain < 0.002) {
                        line.flush();
                        Thread.sleep(20);
                        continue;
                    }
                    double master = 9000.0 * volume / 100.0;
                    double targetFreq = 262 * Math.pow(4, powerLevel);   // green end = low note, red end = high note (2 octaves)
                    for (int i = 0; i < chunk; i++) {
                        reelGain += ((reel ? 1 : 0) - reelGain) * 0.004;
                        powerGain += ((power ? 1 : 0) - powerGain) * 0.004;
                        double v = 0;
                        if (reelGain > 0.0005) {
                            // a ratchet: 15 clicks per second, each a short "tok" that alternates between two pitches,
                            // over a soft low whirr so the sound never stops while the button is held
                            clickClock += 1.0 / RATE;
                            if (clickClock >= 1.0 / 15) { clickClock -= 1.0 / 15; clickAge = 0; clickNo++; }
                            double f = (clickNo % 2 == 0) ? 560 : 760;
                            double env = Math.exp(-clickAge / 0.014);
                            clickAge += 1.0 / RATE;
                            double w = noise.nextDouble() * 2 - 1;
                            bandLo += (w - bandLo) * 0.08;
                            band += (w - band) * 0.45;
                            double click = env * (0.75 * Math.sin(2 * Math.PI * f * clickAge) + 0.9 * (band - bandLo));
                            hum += 2 * Math.PI * (118 + 10 * Math.sin(clickAge * 40)) / RATE;
                            double whirr = 0.10 * Math.sin(hum) + 0.05 * Math.sin(2.01 * hum);
                            v += reelGain * (0.9 * click + whirr) * 1.25;
                        }
                        if (powerGain > 0.0005) {
                            freq += (targetFreq - freq) * 0.002;         // glide smoothly instead of jumping between notes
                            tone += 2 * Math.PI * freq / RATE;
                            v += powerGain * 0.55 * (0.85 * Math.sin(tone) + 0.15 * Math.sin(2 * tone));
                        }
                        int sample = (int) Math.max(-32000, Math.min(32000, v * master));
                        buf[2 * i] = (byte) sample;
                        buf[2 * i + 1] = (byte) (sample >> 8);
                    }
                    line.write(buf, 0, buf.length);
                }
            }
        } catch (InterruptedException e) {
            // the program is closing
        // [PRESENT] EXCEPTION: no sound device = the game runs on without sound
        // ไทย: จัดการ exception: ไม่มีอุปกรณ์เสียง = เกมเล่นต่อได้โดยไม่มีเสียง
        } catch (LineUnavailableException | IllegalArgumentException e) {
            loopBroken = true;               // no audio device: the game is still fully playable
        } catch (RuntimeException e) {
            loopBroken = true;
        }
    }

    public static void caught()  { play(659, 90, 784, 90, 988, 170); }
    public static void fanfare() { play(523, 110, 659, 110, 784, 110, 1046, 320, 0, 70, 988, 110, 1046, 420); }
    public static void snap()    { play(-1, 70, 262, 90, 196, 220); }
    public static void lose()    { play(392, 110, 294, 170); }
    public static void coin()    { play(988, 60, 1319, 130); }
}
