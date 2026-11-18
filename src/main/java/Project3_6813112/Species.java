// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Every fish species: where it lives, how big, how strong. Bigger fish need a bigger lure fish.
 * strength 0..1 = how hard it pulls; weight = how common (legendary = 2-3).
 */
public enum Species {
    // ---------- Village Pond ----------
    TILAPIA("Tilapia", Location.POND, 15, 35, 12, 0.20, 40, 20, 160, 40, Time.ANY, Shape.FISH,
            new Color(120, 130, 140), new Color(200, 200, 190), true),
    CARP("Common Carp", Location.POND, 55, 95, 80, 0.40, 30, 80, 300, 25, Time.ANY, Shape.FISH,
            new Color(180, 140, 60), new Color(230, 200, 120), false),
    GOURAMI("Giant Gourami", Location.POND, 30, 60, 45, 0.35, 35, 40, 220, 15, Time.DAY, Shape.ROUND,
            new Color(150, 120, 160), new Color(210, 190, 200), false),
    KOI("Golden Koi", Location.POND, 40, 90, 300, 0.55, 45, 20, 200, 3, Time.ANY, Shape.FISH,
            new Color(250, 170, 30), new Color(255, 240, 200), false),
    // ---------- Willow River ----------
    BARB("Silver Barb", Location.RIVER, 15, 35, 15, 0.20, 55, 20, 150, 40, Time.DAY, Shape.ROUND,
            new Color(190, 195, 200), new Color(240, 240, 240), false),
    CATFISH("Catfish", Location.RIVER, 55, 95, 80, 0.45, 30, 280, 420, 30, Time.ANY, Shape.CATFISH,
            new Color(90, 90, 80), new Color(170, 170, 150), false),
    SNAKEHEAD("Snakehead", Location.RIVER, 60, 90, 90, 0.50, 50, 40, 260, 20, Time.ANY, Shape.LONG,
            new Color(80, 100, 70), new Color(160, 170, 120), true),
    AROWANA("Silver Arowana", Location.RIVER, 60, 110, 380, 0.65, 60, 20, 120, 3, Time.ANY, Shape.LONG,
            new Color(210, 215, 225), new Color(255, 230, 180), false),
    // ---------- Pine Lake ----------
    PERCH("Yellow Perch", Location.LAKE, 15, 35, 14, 0.20, 45, 30, 240, 35, Time.ANY, Shape.FISH,
            new Color(200, 180, 60), new Color(240, 230, 160), true),
    TROUT("Rainbow Trout", Location.LAKE, 30, 60, 35, 0.35, 55, 20, 320, 35, Time.DAY, Shape.TORPEDO,
            new Color(120, 150, 110), new Color(235, 140, 150), false),
    PIKE("Northern Pike", Location.LAKE, 90, 130, 160, 0.60, 65, 120, 540, 15, Time.ANY, Shape.LONG,
            new Color(90, 120, 70), new Color(200, 210, 150), true),
    GHOST_TROUT("Ghost Trout", Location.LAKE, 50, 90, 420, 0.60, 70, 320, 580, 3, Time.NIGHT, Shape.FANTAIL,
            new Color(220, 230, 255), new Color(160, 200, 255), false),
    // ---------- Old Pier ----------
    MACKEREL("Mackerel", Location.SEA, 30, 45, 32, 0.30, 80, 20, 260, 40, Time.ANY, Shape.FISH,
            new Color(50, 110, 160), new Color(220, 230, 240), true),
    SNAPPER("Red Snapper", Location.SEA, 60, 90, 90, 0.45, 50, 160, 560, 25, Time.ANY, Shape.FISH,
            new Color(220, 70, 60), new Color(250, 190, 170), false),
    SQUID("Squid", Location.SEA, 30, 50, 40, 0.30, 40, 100, 400, 18, Time.NIGHT, Shape.SQUID,
            new Color(230, 200, 210), new Color(250, 160, 180), false),
    TUNA("Bluefin Tuna", Location.SEA, 100, 220, 600, 0.80, 90, 320, 620, 3, Time.ANY, Shape.TORPEDO,
            new Color(30, 50, 110), new Color(200, 210, 220), false),
    // ---------- Moonlit Lagoon ----------
    LANTERN("Lantern Fish", Location.LAGOON, 8, 20, 20, 0.15, 60, 20, 260, 40, Time.ANY, Shape.PUFFER,
            new Color(80, 200, 255), new Color(200, 255, 255), false),
    EEL("Glow Eel", Location.LAGOON, 60, 110, 90, 0.50, 40, 220, 480, 22, Time.ANY, Shape.EEL,
            new Color(90, 255, 160), new Color(40, 120, 90), false),
    RAY("Moon Ray", Location.LAGOON, 100, 140, 180, 0.55, 35, 360, 490, 15, Time.ANY, Shape.RAY,
            new Color(150, 160, 230), new Color(230, 230, 255), false),
    STARFIN("Starfin Leviathan", Location.LAGOON, 150, 300, 900, 0.90, 55, 400, 500, 2, Time.ANY, Shape.ANGLER,
            new Color(60, 40, 140), new Color(255, 230, 120), true),
    // ---------- more fish for every spot (small fry up to big hunters) ----------
    GUPPY("Guppy", Location.POND, 4, 9, 6, 0.10, 55, 20, 140, 45, Time.ANY, Shape.FANTAIL,
            new Color(255, 140, 90), new Color(255, 230, 160), false),
    GOLDFISH("Goldfish", Location.POND, 10, 25, 14, 0.15, 40, 20, 200, 38, Time.ANY, Shape.FANTAIL,
            new Color(255, 150, 40), new Color(255, 220, 150), false),
    BLUEGILL("Bluegill", Location.POND, 12, 28, 16, 0.22, 50, 40, 240, 34, Time.ANY, Shape.ROUND,
            new Color(70, 110, 150), new Color(230, 170, 70), true),
    POND_BASS("Pond Bass", Location.POND, 35, 65, 55, 0.50, 55, 120, 330, 18, Time.ANY, Shape.BULKY,
            new Color(80, 110, 70), new Color(210, 220, 170), true),
    WALKING_CATFISH("Walking Catfish", Location.POND, 30, 55, 40, 0.40, 35, 200, 380, 16, Time.NIGHT, Shape.CATFISH,
            new Color(60, 60, 70), new Color(150, 150, 160), false),
    GRASS_CARP("Grass Carp", Location.POND, 90, 140, 150, 0.60, 40, 160, 440, 8, Time.ANY, Shape.TORPEDO,
            new Color(110, 130, 90), new Color(220, 225, 180), false),
    CHUB("River Chub", Location.RIVER, 10, 22, 12, 0.18, 55, 20, 160, 40, Time.DAY, Shape.TORPEDO,
            new Color(150, 170, 140), new Color(230, 235, 220), false),
    LOACH("Stone Loach", Location.RIVER, 8, 16, 10, 0.15, 30, 220, 440, 30, Time.ANY, Shape.CATFISH,
            new Color(150, 120, 80), new Color(210, 190, 150), true),
    BROWN_TROUT("Brown Trout", Location.RIVER, 30, 55, 40, 0.40, 60, 60, 280, 24, Time.DAY, Shape.FISH,
            new Color(150, 110, 70), new Color(230, 200, 150), false),
    RIVER_BASS("River Bass", Location.RIVER, 35, 70, 60, 0.50, 55, 120, 360, 16, Time.ANY, Shape.BULKY,
            new Color(70, 120, 90), new Color(200, 225, 180), true),
    PANGASIUS("Giant Pangasius", Location.RIVER, 100, 150, 180, 0.65, 35, 250, 450, 7, Time.ANY, Shape.CATFISH,
            new Color(100, 105, 120), new Color(200, 205, 215), false),
    STURGEON("River Sturgeon", Location.RIVER, 100, 180, 260, 0.70, 30, 300, 460, 5, Time.NIGHT, Shape.NEEDLE,
            new Color(95, 95, 85), new Color(190, 180, 160), false),
    BLEAK("Bleak", Location.LAKE, 8, 15, 8, 0.12, 65, 20, 160, 45, Time.DAY, Shape.TORPEDO,
            new Color(190, 205, 215), new Color(245, 250, 255), false),
    ROACH("Roach", Location.LAKE, 12, 28, 14, 0.20, 55, 30, 230, 36, Time.ANY, Shape.ROUND,
            new Color(170, 150, 130), new Color(240, 90, 80), false),
    WHITEFISH("Whitefish", Location.LAKE, 30, 50, 35, 0.35, 60, 90, 340, 24, Time.ANY, Shape.FISH,
            new Color(210, 215, 205), new Color(250, 250, 240), false),
    SALMON("Lake Salmon", Location.LAKE, 55, 95, 110, 0.55, 75, 120, 430, 12, Time.DAY, Shape.TORPEDO,
            new Color(150, 160, 170), new Color(240, 150, 130), false),
    BURBOT("Burbot", Location.LAKE, 55, 85, 80, 0.45, 35, 260, 460, 14, Time.NIGHT, Shape.CATFISH,
            new Color(110, 100, 60), new Color(190, 180, 120), true),
    CHAR("Great Lake Char", Location.LAKE, 100, 150, 200, 0.65, 60, 280, 470, 6, Time.ANY, Shape.BULKY,
            new Color(70, 90, 90), new Color(240, 180, 90), false),
    ANCHOVY("Anchovy", Location.SEA, 6, 12, 7, 0.10, 90, 20, 180, 45, Time.ANY, Shape.LONG,
            new Color(170, 190, 200), new Color(235, 240, 245), false),
    SARDINE_FISH("Sardine", Location.SEA, 12, 22, 12, 0.18, 85, 20, 220, 40, Time.DAY, Shape.TORPEDO,
            new Color(90, 130, 170), new Color(230, 235, 240), true),
    SEA_BREAM("Sea Bream", Location.SEA, 30, 50, 38, 0.40, 60, 120, 380, 24, Time.ANY, Shape.ROUND,
            new Color(220, 140, 150), new Color(250, 210, 215), false),
    GROUPER("Giant Grouper", Location.SEA, 70, 105, 140, 0.65, 40, 240, 460, 12, Time.ANY, Shape.BULKY,
            new Color(120, 100, 70), new Color(210, 190, 150), true),
    BARRACUDA("Barracuda", Location.SEA, 95, 150, 230, 0.75, 85, 150, 400, 8, Time.ANY, Shape.NEEDLE,
            new Color(120, 140, 150), new Color(220, 230, 235), true),
    SWORDFISH("Swordfish", Location.SEA, 150, 300, 420, 0.85, 95, 280, 470, 4, Time.ANY, Shape.NEEDLE,
            new Color(60, 80, 120), new Color(190, 200, 215), false),
    FIREFLY("Firefly Tetra", Location.LAGOON, 4, 9, 8, 0.10, 65, 20, 200, 45, Time.ANY, Shape.FISH,
            new Color(255, 235, 100), new Color(255, 255, 200), false),
    PEARL("Pearl Guppy", Location.LAGOON, 6, 12, 12, 0.12, 55, 30, 240, 40, Time.ANY, Shape.FANTAIL,
            new Color(240, 230, 250), new Color(200, 190, 240), false),
    NEON_DART("Neon Dart", Location.LAGOON, 10, 20, 18, 0.20, 80, 60, 300, 34, Time.ANY, Shape.TORPEDO,
            new Color(60, 255, 220), new Color(250, 100, 220), true),
    DREAM_ANGLER("Dream Angler", Location.LAGOON, 30, 60, 55, 0.50, 40, 200, 440, 16, Time.ANY, Shape.ANGLER,
            new Color(110, 80, 170), new Color(255, 210, 120), false),
    SPIRIT_KOI("Spirit Koi", Location.LAGOON, 60, 90, 130, 0.60, 45, 150, 420, 9, Time.ANY, Shape.FANTAIL,
            new Color(200, 230, 255), new Color(120, 160, 255), false),
    CRYSTAL_PIKE("Crystal Pike", Location.LAGOON, 95, 150, 280, 0.70, 70, 220, 460, 6, Time.ANY, Shape.LONG,
            new Color(180, 240, 250), new Color(100, 200, 230), false);

    public enum Time { ANY, DAY, NIGHT }
    /** Body models: classic, long (pike), eel, ray, squid, disc (bream), torpedo (tuna), fantail (goldfish),
     *  catfish (whiskers), needle (bill), puffer (round with spikes), angler (lure on the head), bulky (grouper). */
    public enum Shape { FISH, LONG, EEL, RAY, SQUID, ROUND, TORPEDO, FANTAIL, CATFISH, NEEDLE, PUFFER, ANGLER, BULKY }

    private final String title;
    private final Location location;
    private final int minCm, maxCm, baseValue;
    private final double strength, speed;
    private final int minDepth, maxDepth, weight;
    private final Time time;
    private final Shape shape;
    private final Color body, accent;
    private final boolean stripes;

    Species(String title, Location location, int minCm, int maxCm, int baseValue, double strength, double speed,
            int minDepth, int maxDepth, int weight, Time time, Shape shape, Color body, Color accent,
            boolean stripes) {
        this.title = title;
        this.location = location;
        this.minCm = minCm;
        this.maxCm = maxCm;
        this.baseValue = baseValue;
        this.strength = strength;
        this.speed = speed;
        this.weight = weight;
        this.time = time;
        this.shape = shape;
        this.body = body;
        this.accent = accent;
        this.stripes = stripes;
        int[] b = band();                         // depth from the size class, never below the floor of the spot
        int floor = location.getDepth() - 45;
        this.minDepth = Math.max(20, Math.min(b[0], floor - 150));
        this.maxDepth = Math.max(this.minDepth + 100, Math.min(b[1], floor));
    }

    public String getTitle() { return title; }
    public Location getLocation() { return location; }
    public int getMinCm() { return minCm; }
    public int getMaxCm() { return maxCm; }
    public int getBaseValue() { return baseValue; }
    public double getStrength() { return strength; }
    public double getSpeed() { return speed; }
    /**
     * Where the species lives (cm below the surface), by size class: Small and Medium fish in the upper water
     * (down to about 400 cm), Large fish from 400 cm down, Giants from 1000 cm, legends from 1500 cm.
     * (The depth columns in the table above are not used.)
     */
    private int[] band() {
        return switch (isLegendary() ? 5 : getTier()) {
            case 1 -> new int[]{20, 350};
            case 2 -> new int[]{50, 400};
            case 3 -> new int[]{400, 1600};
            case 4 -> new int[]{1000, 100000};
            default -> new int[]{1500, 100000};
        };
    }

    public int getMinDepth() { return minDepth; }
    public int getMaxDepth() { return maxDepth; }
    public int getWeight() { return weight; }
    public Time getTime() { return time; }
    public Shape getShape() { return shape; }
    public Color getBody() { return body; }
    public Color getAccent() { return accent; }
    public boolean hasStripes() { return stripes; }
    public boolean isLegendary() { return weight <= 3; }
    public boolean glows() { return location == Location.LAGOON; }

    /** Size class 1..4 of a fish of this length: Small, Medium, Large, Giant (there is no Tiny class). */
    public static int tierOfCm(int cm) {
        return cm <= 40 ? 1 : cm <= 70 ? 2 : cm <= 110 ? 3 : 4;
    }

    public static String tierName(int tier) {
        return switch (tier) {
            case 1 -> "Small";
            case 2 -> "Medium";
            case 3 -> "Large";
            default -> "Giant";
        };
    }

    /** The size class of this species: Small, Medium, Large or Giant (decided by its biggest size). */
    public int getTier() { return tierOfCm(maxCm); }

    public String tierName() { return tierName(getTier()); }

    /** The weakest rod that can land this species. */
    public String rodHint() {
        if (isLegendary()) return "Legend Rod";
        return getTier() <= 2 ? "Bamboo Rod" : getTier() == 3 ? "Carbon Rod" : "Pro Rod";
    }

    /** What this species bites: the starter lure (Small fish), a fish of the class below, or the golden lure (legends). */
    public String baitHint() {
        if (isLegendary()) return "Golden Lure";
        return getTier() == 1 ? "Purple Lure" : tierName(getTier() - 1) + " fish";
    }

    public static List<Species> at(Location loc) {
        List<Species> out = new ArrayList<>();
        for (Species s : values()) if (s.location == loc) out.add(s);
        out.sort(java.util.Comparator.comparing(Species::isLegendary).thenComparingInt(Species::getMinCm));   // small fry first, legend last
        return out;
    }
}
