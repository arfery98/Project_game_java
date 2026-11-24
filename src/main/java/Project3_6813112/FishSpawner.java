// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Background thread that keeps new fish swimming into the lake. */
// [PRESENT] THREAD 2 fish-spawner: every 0.8 s calls LakeGame.spawnTick()
// ไทย: thread ที่ 2 fish-spawner: ทุก 0.8 วินาทีเรียก LakeGame.spawnTick() เพื่อสร้างปลา
public class FishSpawner extends Thread {

    private final LakeGame game;
    private final GameLoop loop;
    private volatile boolean running = true;

    public FishSpawner(LakeGame game, GameLoop loop) {
        super("fish-spawner");
        setDaemon(true);
        this.game = game;
        this.loop = loop;
    }

    @Override
    public void run() {
        while (running && !game.isOver()) {
            try {
                Thread.sleep(800);
            // [PRESENT] EXCEPTION: interrupted while sleeping = the thread stops cleanly
            // ไทย: จัดการ exception: ถูกขัดจังหวะตอนหลับ = thread หยุดทำงานอย่างเรียบร้อย
            } catch (InterruptedException e) {
                return;
            }
            if (!loop.isPaused()) game.spawnTick();
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}
