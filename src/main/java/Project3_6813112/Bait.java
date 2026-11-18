// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Color;

/**
 * Lures. There is no hook and you never see the fish on the line, only a coloured lure.
 * The free PURPLE lure catches Small fish. To catch a bigger fish you must use a fish of the size class
 * just below it as bait (keep a caught fish, B key): Small fish for Medium, Medium for Large, Large for Giant.
 * Only the GOLDEN lure tempts a legendary fish.
 */
public enum Bait implements ShopItem {
    PURPLE("Purple Lure", 0, 0, "Free starter lure. Only Small fish bite it.", new Color(160, 95, 215)),
    GOLDEN("Golden Lure", 800, 9, "The only lure a legendary fish will bite.", new Color(255, 200, 40));

    /** Lure colour by class: 0 = the starter lure, 1..4 = a kept fish of size class Small .. Giant. */
    private static final Color[] CLASS_COLORS = {
        new Color(160, 95, 215), new Color(70, 145, 235), new Color(75, 200, 120), new Color(255, 150, 50), new Color(235, 70, 70)};

    private final String title;
    private final int price, tier;
    private final String desc;
    private final Color color;

    Bait(String title, int price, int tier, String desc, Color color) {
        this.title = title;
        this.price = price;
        this.tier = tier;
        this.desc = desc;
        this.color = color;
    }

    @Override public String getTitle() { return title; }
    @Override public int getPrice() { return price; }
    @Override public String getDesc() { return desc; }
    @Override public String getCategory() { return "Lure"; }
    /** Lure class: 0 = starter, 9 = golden. A fish of class F bites a lure of class F-1. */
    public int getTier() { return tier; }
    public Color getColor() { return color; }

    /** Colour of the lure made by a kept fish of this size class (1..4). */
    public static Color colorOfTier(int tier) {
        return CLASS_COLORS[Math.max(0, Math.min(CLASS_COLORS.length - 1, tier))];
    }

    @Override
    public String toString() { return title; }
}
