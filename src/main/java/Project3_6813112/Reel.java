// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Reels: higher speed = the fish comes in faster. */
public enum Reel implements ShopItem {
    BASIC("Basic Reel", 0, 70),
    QUICK("Quick Reel", 180, 95),
    TURBO("Turbo Reel", 450, 125);

    private final String title;
    private final int price;
    private final double speed;   // px per second of line pulled in

    Reel(String title, int price, double speed) {
        this.title = title;
        this.price = price;
        this.speed = speed;
    }

    @Override public String getTitle() { return title; }
    @Override public int getPrice() { return price; }
    @Override public String getDesc() { return "Reels in " + (int) speed + " cm of line per second."; }
    @Override public String getCategory() { return "Reel"; }
    public double getSpeed() { return speed; }
}
