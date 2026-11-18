// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Parts you can fit on a rod. Each rod has a limited number of slots; every part gives the rod a skill. */
public enum Part implements ShopItem {
    REINFORCED_LINE("Reinforced Line", 150, "Stronger line: it snaps at 20% more tension."),
    SHOCK_ABSORBER("Shock Absorber", 200, "A fish's bursts pull 25% less."),
    LONG_CAST("Long-Cast Tuner", 250, "Casts 35% farther, so the lure sinks deeper."),
    LURE_CHARM("Lure Charm", 180, "Fish notice the lure from farther and bite sooner."),
    BAIT_SAVER("Bait Saver", 300, "40% of the time a live bait fish survives the bite."),
    FISH_GUARD("Fish Guard", 250, "Bigger fish go for your catch 60% less often."),
    LUCKY_CHARM("Lucky Charm", 350, "Every fish sells for 20% more."),
    HEAVY_CORE("Heavy Core", 450, "The rod counts as one class stronger: fewer snaps on big fish.");

    private final String title;
    private final int price;
    private final String desc;

    Part(String title, int price, String desc) {
        this.title = title;
        this.price = price;
        this.desc = desc;
    }

    @Override public String getTitle() { return title; }
    @Override public int getPrice() { return price; }
    @Override public String getDesc() { return desc; }
    @Override public String getCategory() { return "Part"; }
}
