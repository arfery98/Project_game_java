// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/**
 * Animation/physics thread, about 60 frames per second. Uses measured real time (dt)
 * so the game runs at the same speed on every computer.
 */
// [PRESENT] THREAD 1 game-loop: physics + repaint about 60 times a second (uses real dt)
public class GameLoop extends Thread {

    private final LakeGame game;
    private final Runnable repaint;
    private volatile boolean running = true;
    private volatile boolean paused;

    public GameLoop(LakeGame game, Runnable repaint) {
        super("game-loop");
        setDaemon(true);
        this.game = game;
        this.repaint = repaint;
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double dt = Math.min(0.1, (now - last) / 1e9);
            last = now;
            if (!paused) game.update(dt);
            repaint.run();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    public boolean isPaused() { return paused; }
    public void setPaused(boolean p) { paused = p; }
}
