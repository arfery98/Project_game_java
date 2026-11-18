// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.util.Random;

/** Background thread that turns rain on and off every 20-50 seconds (rain = more, hungrier fish). */
// [PRESENT] THREAD 3 weather: switches rain on and off every 20-50 s
public class WeatherThread extends Thread {

    private final LakeGame game;
    private final GameLoop loop;
    private final Random rnd = new Random();
    private volatile boolean running = true;

    public WeatherThread(LakeGame game, GameLoop loop) {
        super("weather");
        setDaemon(true);
        this.game = game;
        this.loop = loop;
    }

    @Override
    public void run() {
        boolean rain = false;
        while (running && !game.isOver()) {
            long wait = (rain ? 20 : 30) * 1000L + rnd.nextInt(20_000);
            try {
                // sleep in small steps so pausing the game also pauses the weather
                long slept = 0;
                while (slept < wait && running) {
                    Thread.sleep(200);
                    if (!loop.isPaused()) slept += 200;
                }
            } catch (InterruptedException e) {
                return;
            }
            rain = !rain;
            game.setRaining(rain);
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}
