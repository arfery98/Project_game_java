// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/**
 * Rods. A better rod is meant for bigger fish (maxClass: 2 = up to Medium, 5 = legends too): a bigger fish can
 * still bite, but the line shoots into the red and snaps. It also casts farther so the lure sinks deeper
 * (reach, in cm of depth), makes strong fish pull the line less (power) and has more slots for parts.
 */
public enum Rod implements ShopItem {
    BAMBOO("Bamboo Rod", 0, 1.0, 2, 900, 1),
    CARBON("Carbon Rod", 200, 1.3, 3, 1800, 2),
    PRO("Pro Rod", 500, 1.7, 4, 3000, 3),
    LEGEND("Legend Rod", 1200, 2.2, 5, 5000, 4);

    private final String title;
    private final int price, maxClass, reach, slots;
    private final double power;

    Rod(String title, int price, double power, int maxClass, int reach, int slots) {
        this.title = title;
        this.price = price;
        this.power = power;
        this.maxClass = maxClass;
        this.reach = reach;
        this.slots = slots;
    }

    @Override public String getTitle() { return title; }
    @Override public int getPrice() { return price; }
    @Override
    public String getDesc() {
        String fish = maxClass >= 5 ? "legends too" : "up to " + Species.tierName(maxClass) + " fish";
        return "Made for " + fish + ". Casts ~" + reach + " cm deep. " + slots + " part slot" + (slots > 1 ? "s" : "") + ". Power x" + power + ".";
    }
    @Override public String getCategory() { return "Rod"; }
    public double getPower() { return power; }
    /** Number of parts this rod can carry. */
    public int getSlots() { return slots; }
    /** Biggest size class this rod is made for: 1 Small .. 4 Giant, 5 = legendary fish. Bigger fish snap the line. */
    public int getMaxClass() { return maxClass; }
    /** How deep (cm) a full-power cast can put the lure. */
    public int getReach() { return reach; }
}
