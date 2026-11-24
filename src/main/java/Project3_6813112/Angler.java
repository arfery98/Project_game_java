// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The player: money, gear, baits and every fish caught.
 * Each caught fish is either sold for money or kept in the bait box and used as LIVE BAIT.
 */
public class Angler {

    /** How many fish the bait box can hold. */
    public static final int MAX_LIVE = 5;

    private final String name;
    private int money = 60;
    private int earned;
    private Rod rod = Rod.BAMBOO;
    private Reel reel = Reel.BASIC;
    private Bait bait = Bait.PURPLE;
    private final EnumSet<Bait> baits = EnumSet.of(Bait.PURPLE);
    private final EnumSet<Part> parts = EnumSet.noneOf(Part.class);     // parts fitted on the rod
    private final List<CatchRecord> catches = new ArrayList<>();
    private final EnumMap<Species, Integer> best = new EnumMap<>(Species.class);

    private CatchRecord live;                                          // live bait on the hook (null = normal bait)
    private final List<CatchRecord> liveBox = new ArrayList<>();       // fish kept as bait
    private final Map<CatchRecord, String> fate = new IdentityHashMap<>();   // sold / bait / used
    private int version;                                               // changes whenever bait or a fish's fate changes

    public Angler(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    /** Admin mode (the player is called "admin"): the money never runs out. */
    private boolean unlimited;

    public void setUnlimited(boolean on) { unlimited = on; }
    public boolean isUnlimited() { return unlimited; }
    public int getMoney() { return unlimited ? 999_999_999 : money; }

    /** Money for the screen: "$60", or "$∞" in admin mode. */
    public String moneyText() { return unlimited ? "$\u221E" : "$" + money; }
    public int getEarned() { return earned; }
    public Rod getRod() { return rod; }
    public Reel getReel() { return reel; }
    public Bait getBait() { return bait; }
    public List<CatchRecord> getCatches() { return catches; }
    public boolean owns(Bait b) { return baits.contains(b); }
    public boolean has(Part p) { return parts.contains(p); }
    public int slotsUsed() { return parts.size(); }
    public int slots() { return rod.getSlots(); }
    public List<Part> getParts() { return new ArrayList<>(parts); }

    /** Takes a part off the rod (no refund) to free its slot. */
    public String uninstall(Part p) {
        if (!parts.remove(p)) return p.getTitle() + " is not on the rod.";
        version++;
        return "Removed " + p.getTitle() + " from the rod.";
    }

    /** The golden lure is lost (a legend bit it off). Back to the starter lure. */
    public boolean dropGolden() {
        if (live != null || bait != Bait.GOLDEN) return false;
        baits.remove(Bait.GOLDEN);
        bait = Bait.PURPLE;
        version++;
        return true;
    }
    public Integer bestSize(Species s) { return best.get(s); }
    public int speciesCount() { return best.size(); }
    public int getVersion() { return version; }

    // ---------------- bait ----------------

    /** Chooses a normal bait (and takes any live bait off the hook). */
    public boolean setBait(Bait b) {
        if (!owns(b)) return false;
        bait = b;
        live = null;
        version++;
        return true;
    }

    public boolean isLive() { return live != null; }
    public CatchRecord getLive() { return live; }
    public List<CatchRecord> getLiveBox() { return liveBox; }

    /** Puts a fish from the bait box on the hook. */
    public boolean selectLive(CatchRecord r) {
        if (!liveBox.contains(r)) return false;
        live = r;
        version++;
        return true;
    }

    /** The live bait is gone (a fish ate it). Back to the normal bait. */
    public void consumeLive() {
        if (live == null) return;
        liveBox.remove(live);
        fate.put(live, "used");
        live = null;
        version++;
    }

    /** Short bait name for the small sign on the screen. */
    public String baitShort() {
        return live != null ? "Live " + live.species().getTitle() : bait.getTitle();
    }

    public String baitLabel() {
        return live != null ? "Live: " + live.species().getTitle() + " " + live.sizeCm() + " cm" : bait.getTitle();
    }

    public Color baitColor() {
        return live != null ? Bait.colorOfTier(live.species().getTier()) : bait.getColor();
    }

    // ---------------- shop ----------------

    public boolean owns(ShopItem item) {
        if (item instanceof Bait b) return baits.contains(b);
        if (item instanceof Part p) return parts.contains(p);
        if (item instanceof Rod r) return rod.ordinal() >= r.ordinal();
        if (item instanceof Reel r) return reel.ordinal() >= r.ordinal();
        return false;
    }

    /** @return message describing what happened */
    public String buy(ShopItem item) {
        if (owns(item)) return "You already have " + item.getTitle() + ".";
        if (item instanceof Part && parts.size() >= rod.getSlots()) {
            return "No free slot on the " + rod.getTitle() + " (" + parts.size() + "/" + rod.getSlots()
                    + "). Remove a part or buy a better rod.";
        }
        if (getMoney() < item.getPrice()) return "Not enough money for " + item.getTitle() + " ($" + item.getPrice() + ").";
        if (!unlimited) money -= item.getPrice();
        if (item instanceof Bait b) {
            baits.add(b);
            bait = b;
            live = null;
            version++;
            return "Bought " + b.getTitle() + " and tied it to the line!";
        } else if (item instanceof Part p) {
            parts.add(p);
            version++;
            return "Bought " + p.getTitle() + " and fitted it on the rod!";
        } else if (item instanceof Rod r) {
            rod = r;
            version++;
        } else if (item instanceof Reel r) {
            reel = r;
        }
        return "Equipped " + item.getTitle() + "!";
    }

    // ---------------- catches ----------------

    /** Records a catch (NEW / RECORD flags). The fish is neither sold nor kept yet. */
    public CatchRecord addCatch(Species s, int sizeCm, int value, String clock) {
        return addCatch(s, sizeCm, value, clock, 0);
    }

    public CatchRecord addCatch(Species s, int sizeCm, int value, String clock, int eaten) {
        Integer prev = best.get(s);
        boolean isNew = prev == null;
        boolean isRecord = !isNew && sizeCm > prev;
        if (isNew || isRecord) best.put(s, sizeCm);
        CatchRecord r = new CatchRecord(s, sizeCm, value, isNew, isRecord, clock, eaten);
        catches.add(0, r);
        version++;
        return r;
    }

    /** What the fish sells for (a Lucky Charm adds 20%). */
    public int sellValue(CatchRecord r) {
        return has(Part.LUCKY_CHARM) ? (int) Math.round(r.value() * 1.2) : r.value();
    }

    /** Sells a fish for its value. */
    public void sell(CatchRecord r) {
        int v = sellValue(r);
        if (!unlimited) money += v;
        earned += v;
        fate.put(r, "sold");
        version++;
    }

    /** @return null if the fish can be kept, otherwise the reason it cannot */
    public String keepBlockReason(CatchRecord r) {
        if (r.species().isLegendary()) return "Legendary fish are too precious for bait";
        if (r.eaten() >= 3) return "Only bones are left";
        if (liveBox.size() >= MAX_LIVE) return "Bait box is full (" + MAX_LIVE + ")";
        return null;
    }

    /** Keeps a fish in the bait box and puts it on the hook. */
    public void keepAsBait(CatchRecord r) {
        liveBox.add(r);
        fate.put(r, "bait");
        live = r;
        version++;
    }

    /** What happened to a fish, for the catch table. */
    public String fateText(CatchRecord r) {
        String f = fate.get(r);
        if (f == null) return "...";
        return switch (f) {
            case "sold" -> "$" + r.value();
            case "bait" -> "bait";
            default -> "used";
        };
    }

    public CatchRecord biggestCatch() {
        CatchRecord b = null;
        for (CatchRecord c : catches) if (b == null || c.value() > b.value()) b = c;
        return b;
    }
}
