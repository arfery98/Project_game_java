// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Color;

/** The five fishing spots. Each has its own colours and fish. */
public enum Location {
    POND("Village Pond", "Calm and friendly. Perfect for beginners.",
            new Color(70, 150, 120), new Color(20, 70, 60), new Color(110, 170, 90), false, 4000),
    RIVER("Willow River", "Murky water, strong fighters near the bottom.",
            new Color(110, 140, 90), new Color(50, 60, 35), new Color(120, 160, 80), false, 5000),
    LAKE("Pine Lake", "Clear mountain water. Fast trout and fierce pike.",
            new Color(60, 140, 200), new Color(15, 50, 100), new Color(70, 120, 90), false, 4500),
    SEA("Old Pier", "Salt water. Big money, big fish.",
            new Color(40, 120, 190), new Color(10, 30, 80), new Color(200, 190, 140), false, 5000),
    LAGOON("Moonlit Lagoon", "Always night. Glowing creatures live here.",
            new Color(40, 60, 130), new Color(5, 10, 40), new Color(40, 50, 80), true, 4000);

    private final String title, desc;
    private final Color waterTop, waterBottom, land;
    private final boolean alwaysNight;
    private final int depth;                 // water depth in cm: how far the hook can sink at this spot

    Location(String title, String desc, Color waterTop, Color waterBottom, Color land, boolean alwaysNight, int depth) {
        this.title = title;
        this.desc = desc;
        this.waterTop = waterTop;
        this.waterBottom = waterBottom;
        this.land = land;
        this.alwaysNight = alwaysNight;
        this.depth = depth;
    }

    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public Color getWaterTop() { return waterTop; }
    public Color getWaterBottom() { return waterBottom; }
    public Color getLand() { return land; }
    public boolean isAlwaysNight() { return alwaysNight; }
    public int getDepth() { return depth; }

    @Override
    public String toString() { return title; }
}
