// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Game model and rules. Shared by the game-loop thread, the fish-spawner thread,
 * the weather thread and the Swing EDT, so every state-changing method is synchronized.
 *
 * Phases: IDLE -> CHARGING (click 1: zoom + power bar) -> CASTING (click 2 stops the bar) -> WAITING -> BITE -> REELING -> CAUGHT -> IDLE
 */
public class LakeGame {

    public static final int W = 800, H = 560, SURFACE = 190, BOTTOM = 540;   // W x H = the screen
    public static final int WORLD_W = 5400;       // the lake is much wider than the screen; the camera follows the hook
    public static final double SHORE_X = 150, ROD_X = 172, ROD_Y = 92;
    public static final int MIN_DEPTH = 20;
    public static final double MIN_CAST_X = 346;   // the shortest cast hangs ~100 cm deep
    private static final double CHASE_MAX = 330;       // px: a fish chasing the hook gives up beyond this distance
    private static final double PULL_SPEED = 170;     // cm of line taken in per second while Up is held
    private static final double SWING_SPEED = 130;    // px/s average speed of the lure along its arc (the arc length decides the time)
    private static final double BRAKE = 0.35;         // the swing runs at this share of its speed while Down is held
    public static final double ZOOM_TIME = 0.45;   // seconds of camera zoom before the power bar starts
    public static final double SWEEP_TIME = 0.8;   // seconds for the marker to sweep from one end to the other

    public enum Phase { IDLE, CHARGING, CASTING, WAITING, BITE, REELING, CAUGHT }

    /** Floating text such as "+$45" or "Too early!". */
    public static class Toast {
        final String text;
        final double x;
        double y, age;
        final Color color;

        Toast(String text, double x, double y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private final Settings settings;
    private final Angler angler;
    private final Random rnd = new Random();
    private final List<Fish> fish = new ArrayList<>();
    private final List<Toast> toasts = new ArrayList<>();
    private final List<double[]> ripples = new ArrayList<>();   // x, age
    private final List<double[]> bubbles = new ArrayList<>();   // x, y, speed
    private final ConcurrentLinkedQueue<String> log = new ConcurrentLinkedQueue<>();

    private Phase phase = Phase.IDLE;
    private double aimPower, chargeT, castT, castTargetX;
    // "bobber" = the cast point (where the hook lands, x) and the twitch of the hook on a nibble (y); no float is drawn
    private double bobberX = ROD_X, bobberY = ROD_Y, bobberDip, bobT;
    // The hook is a pendulum whose pivot is the rod tip and whose line has a fixed length (the distance from the
    // rod to where the cast landed). It only swings along that circle, slowed by the water, until the line
    // hangs straight down under the rod. The line is never paid out: a farther cast simply reaches deeper.
    private double swingAngle0;                // rad from vertical at the splash (the start of the swing)
    private double lineLen0;                   // cm of line from the rod tip (the radius of the circle)
    private double pullSpeed = PULL_SPEED;     // faster for a long line, so pulling it up never takes ages
    private double castDur = 0.75;             // seconds the lure flies
    private double sinkT;                      // swing time since the splash (runs slower while the brake is held)
    private double swingDur = 8;               // seconds the whole swing takes
    private volatile boolean brake;            // Down held: the swing is slowed
    private double pulled;                     // cm of line taken in by holding Up
    private boolean pullUp;                    // Up arrow held: pull the hook back up
    private final int bottomY;                 // y of the lake floor at this spot
    private double hookX = ROD_X, hookY = ROD_Y;

    private Fish nibbleFish;
    private double biteTimer;

    private Fish reelFish;
    private double hookV;                      // how fast the lure moves (px/s)
    private double weakMul = 1;                // > 1 when the rod is too weak for the hooked fish
    private double lineD, lineD0, fightX0, depth0, tension, overTime, slackTime, burstTimer, fightT, reelSoundT;
    private boolean burst, reelKey, reelMouse;

    private CatchRecord lastCatch;
    private double cardT, jumpX;
    private double lastLoseAt = -99;

    /** Angler's face, used only for drawing. */
    public enum Mood { NORMAL, FOCUS, HAPPY, SAD }

    private double elapsed, clock;
    private boolean raining;
    private volatile boolean over;
    private int casts, escaped;

    public LakeGame(Settings settings) {
        this.settings = settings;
        this.bottomY = SURFACE + settings.location().getDepth();
        this.angler = new Angler(settings.name());
        if (settings.name().trim().equalsIgnoreCase("admin")) angler.setUnlimited(true);      // admin: unlimited money
        this.clock = settings.dayNight() ? 15 * 60 : (settings.location().isAlwaysNight() ? 22 * 60 : 10 * 60);
        populate(40);
    }

    // =====================================================================
    // simulation step (game-loop thread)
    // =====================================================================
    // [PRESENT] SYNCHRONIZED: the model is shared by game-loop, fish-spawner, weather and the Swing thread, so its methods are synchronized
    public synchronized void update(double dt) {
        if (over) return;
        elapsed += dt;
        if (settings.dayNight()) clock = (clock + dt * 4) % (24 * 60);   // 4 game minutes per real second
        bobT += dt;
        bobberDip *= Math.pow(0.02, dt);

        for (Fish f : fish) f.update(dt, rnd);
        switch (phase) {
            case CHARGING -> {
                chargeT += dt;
                // the bar waits while the camera zooms in, then the marker sweeps 0 -> 1 -> 0 -> 1 ... (triangle wave)
                double ct = Math.max(0, chargeT - ZOOM_TIME);
                double p = (ct / SWEEP_TIME) % 2;
                aimPower = ct <= 0 ? 0 : (p < 1 ? p : 2 - p);
                SoundFX.setPower(ct <= 0 ? -1 : aimPower);        // the note rises and falls with the marker (green -> red -> green ...)
            }
            case CASTING -> updateCast(dt);
            case WAITING -> updateWaiting(dt);
            case BITE -> updateBite(dt);
            case REELING -> updateReeling(dt);
            case CAUGHT -> {
                cardT += dt;
                if (cardT > 8) sellCatch();                  // no decision: the fish is sold
            }
            default -> { }
        }
        if (phase == Phase.WAITING || phase == Phase.BITE) {
            bobberY = SURFACE - 3 + Math.sin(bobT * 2) * 1.5 + bobberDip;
        }
        updateEffects(dt);
    }

    private void updateCast(double dt) {
        castT += dt / castDur;
        double t = Math.min(1, castT);
        bobberX = ROD_X + (castTargetX - ROD_X) * t;
        bobberY = ROD_Y + (SURFACE - ROD_Y) * t - Math.sin(Math.PI * t) * (60 + 120 * aimPower);
        hookX = bobberX;
        hookY = bobberY;
        if (castT >= 1) {
            phase = Phase.WAITING;
            bobberY = SURFACE - 3;
            hookY = SURFACE;
            double dx = bobberX - ROD_X, dy = SURFACE - ROD_Y;
            swingAngle0 = Math.atan2(dx, dy);          // the hook starts at the splash point ...
            lineLen0 = Math.hypot(dx, dy);
            swingDur = Math.max(6, Math.min(26, Math.abs(swingAngle0) * lineLen0 / SWING_SPEED));
            pullSpeed = Math.max(PULL_SPEED, lineLen0 / 4.0);
            sinkT = 0;
            pulled = 0;
            quietT = 0;
            ripples.add(new double[]{bobberX, 0});
            SoundFX.splash();
        }
    }

    /** 1 at the start of the swing, 0 at the end: a smooth ease with zero speed at both ends (no creeping, no jump). */
    private double swingShare(double time) {
        double x = Math.max(0, Math.min(1, time / swingDur));
        return 1 - x * x * x * (x * (6 * x - 15) + 10);
    }

    /** Hook position on a circle around the rod tip, swinging in over swingDur seconds. */
    private void placeHook() {
        double angle = swingAngle0 * swingShare(sinkT);
        double len = lineLen0 - pulled;                      // constant radius: a pure circular swing
        hookX = Math.max(SHORE_X + 20, Math.min(WORLD_W - 20, ROD_X + len * Math.sin(angle)));
        hookY = Math.min(bottomY - 12, ROD_Y + len * Math.cos(angle));
    }

    /** Where the hook will be in `ahead` seconds if nothing changes (the swing is a known curve). */
    private double[] hookAt(double ahead) {
        double angle = swingAngle0 * swingShare(sinkT + ahead), len = lineLen0 - pulled;
        return new double[]{Math.max(SHORE_X + 20, Math.min(WORLD_W - 20, ROD_X + len * Math.sin(angle))),
                Math.min(bottomY - 12, ROD_Y + len * Math.cos(angle))};
    }

    /** y of the lake floor and the deepest the hook may go at this fishing spot. */
    public int bottomY() { return bottomY; }

    public int maxDepth() { return bottomY - SURFACE - 30; }

    /** The farthest a full-power cast lands: the line is then just long enough to hang above the lake floor. */
    public double maxCastX() {
        double dy = SURFACE - ROD_Y;
        double reachDepth = Math.max(100, Math.min(angler.getRod().getReach() * (angler.has(Part.LONG_CAST) ? 1.35 : 1.0), maxDepth()));   // the rod and the lake floor limit the cast
        double reach = reachDepth + dy;                          // line length that hangs that deep
        return Math.min(WORLD_W - 120, ROD_X + Math.sqrt(Math.max(1, reach * reach - dy * dy)));
    }

    private void updateWaiting(double dt) {
        // the hook swings in by itself; before a fish bites the only control is pulling it up
        // the hook keeps swinging while a fish swims toward it, but holds still once a fish is eating
        sinkT += dt * (brake ? BRAKE : 1);
        double px = hookX, py = hookY;
        if (pullUp && sinkT > 0.6) {                   // Up held through the cast does not undo it
            pulled += pullSpeed * dt;
            placeHook();
            if (hookY <= SURFACE + MIN_DEPTH) {        // pulled all the way up: the cast is over
                reelIn();
                return;
            }
        } else {
            placeHook();
        }
        hookV = dt > 0 ? Math.hypot(hookX - px, hookY - py) / dt : 0;
        boolean settled = hookY - SURFACE > 60;
        // nothing happening for a long time: tell the player how to look for fish elsewhere
        quietT = nibbleFish == null ? quietT + dt : 0;
        if (quietT > 18) {
            quietT = 0;
            toast("Quiet here: no fish for this bait at this depth. Pull up (Up) or cast another distance.", new Color(200, 230, 255));
        }

        if (nibbleFish != null && nibbleFish.state == Fish.State.FLEE) nibbleFish = null;
        if (nibbleFish == null && settled) {
            boolean charm = angler.has(Part.LURE_CHARM);
            double sense = (raining ? 250 : 200) * (charm ? 1.25 : 1.0);   // how far a fish notices the lure
            double rainBoost = raining ? 1.4 : 1.0;
            for (Fish f : fish) {
                if (f.state != Fish.State.WANDER || f.ignoreTimer > 0) continue;
                // fish only notice a hook inside (or near) the depth band they live in
                if (hookY < f.minY() - 40 || hookY > f.maxY() + 40) continue;
                if (Math.hypot(f.x - hookX, f.y - hookY) > sense) continue;
                double attr = lureAppeal(f);
                double chance = attr * 2.6 * (charm ? 1.8 : 1.0) * rainBoost * dt;            // a fish locks onto the lure quickly
                if (rnd.nextDouble() < chance) {
                    f.state = Fish.State.APPROACH;
                    f.startApproach(hookX);
                    nibbleFish = f;
                    break;
                }
            }
        }
        if (nibbleFish != null) {
            Fish f = nibbleFish;
            if (f.state == Fish.State.APPROACH) {
                f.approachT += dt;
                if (Math.hypot(f.x - hookX, f.y - hookY) > CHASE_MAX || f.approachT > 5) {   // too far: let another fish try
                    f.giveUp();
                    nibbleFish = null;
                    return;
                }
                double left = f.approachHook(hookX, hookY, dt, hookV);
                if (left < 3) {                                    // it touches the lure: hooked at once, start reeling
                    SoundFX.bite();
                    hookIt();
                    return;
                }
            } else if (f.state == Fish.State.NIBBLE) {
                f.nibbleTimer -= dt;
                if (f.nibbleTimer <= 0) {
                    if (f.nibblesLeft > 0) {
                        f.nibblesLeft--;
                        bobberDip = 4;
                        f.nibbleTimer = 0.5 + rnd.nextDouble() * 0.6;
                        SoundFX.nibble();
                    } else {
                        SoundFX.bite();
                        hookIt();                              // the fish takes the lure: it is hooked right away
                        return;
                    }
                }
            }
        }
    }

    private double lastNeedBigger = -99, lastWaitToast = -99, quietT;

    /**
     * How tempting the lure is for this fish. A fish of size class F bites only a lure of class F-1: the free
     * purple lure for Small fish, a kept Small fish for Medium, a Medium fish for Large, a Large fish for Giant.
     * Nothing of its own size or bigger (it cannot swallow it) and nothing smaller ("Need bigger bait!").
     * Legends only want the golden lure.
     */
    private double lureAppeal(Fish f) {
        int lure = lureTier();
        Species sp = f.getSpecies();
        if (sp.isLegendary()) return lure == 9 ? 0.9 : 0;
        if (lure == 9) return 0;
        int need = f.getTier() - 1;
        if (lure < need) {
            f.ignoreTimer = 12;                               // do not nag about the same fish
            if (elapsed - lastNeedBigger > 9 && Math.hypot(f.x - hookX, f.y - hookY) < 130) {
                lastNeedBigger = elapsed;
                toasts.add(new Toast("Need bigger bait!", f.x, f.y - 28, new Color(255, 190, 110)));
            }
            return 0;
        }
        return lure == need ? 1.0 : 0;
    }

    /** How many size classes this fish is above what the rod (with a Heavy Core) is made for; 0 = no problem. */
    private int rodShortfall(Species s) {
        int rodMax = angler.getRod().getMaxClass() + (angler.has(Part.HEAVY_CORE) ? 1 : 0);
        int cls = s.isLegendary() ? 5 : s.getTier();
        return Math.max(0, cls - rodMax);
    }

    private void updateBite(double dt) {
        biteTimer -= dt;
        bobberDip = 14;
        if (biteTimer <= 0) {
            scare(nibbleFish);
            nibbleFish = null;
            phase = Phase.WAITING;
            if (angler.isLive()) {
                log.add("The " + angler.getLive().species().getTitle() + " bait was eaten!");
                angler.consumeLive();
            }
            toast("Too slow... it stole a nibble.", new Color(255, 200, 120));
        }
    }

    private void updateReeling(double dt) {
        Fish f = reelFish;
        Species sp = f.getSpecies();
        fightT += dt;
        burstTimer -= dt;
        if (burstTimer <= 0) {
            burst = !burst;
            burstTimer = burst ? 0.8 + rnd.nextDouble() * 0.8 : 1.4 + rnd.nextDouble() * 1.6;
            if (burst) SoundFX.splash();
        }
        // How hard the fish pulls right now (stronger rod = less effective pull).
        double tired = Math.max(0.3, 1 - fightT / 70.0);          // the fish tires out: its pull fades over about a minute
        double pull = tired * sp.getStrength() * (burst ? (angler.has(Part.SHOCK_ABSORBER) ? 1.42 : 1.9) : 1.0) * (0.85 + 0.15 * Math.sin(fightT * 3.1))
                / angler.getRod().getPower();
        boolean reeling = reelKey || reelMouse;
        // Reeling adds your pull on top of the fish's pull; letting go only partly relaxes the line,
        // so a strong fish in a burst can still snap a weak rod.
        double target = (reeling ? 0.30 + pull : pull * 0.6) * weakMul;      // a rod too weak for this fish: the line goes red fast
        tension += (target - tension) * Math.min(1, 3.5 * dt);

        double limit = tensionLimit();
        if (tension > limit) {
            overTime += dt;
            if (overTime > 0.45 / weakMul) {
                if (angler.dropGolden()) toasts.add(new Toast("The golden lure was bitten off!", hookX, hookY - 40, new Color(255, 200, 60)));
                lose("SNAP! The line broke.");
                SoundFX.snap();
                return;
            }
        } else {
            overTime = Math.max(0, overTime - dt);
        }
        if (tension < 0.12) {
            slackTime += dt;
            if (slackTime > (settings.easyReel() ? 4.0 : 2.5)) {
                lose("Too slack - the fish slipped off the hook.");
                SoundFX.lose();
                return;
            }
        } else {
            slackTime = 0;
        }

        if (reeling) {
            lineD -= angler.getReel().getSpeed() * Math.max(0.15, 1 - pull * 0.7) * dt;
            SoundFX.setReeling(true);                  // the reel sound plays only while the button is held
        } else {
            lineD += pull * 40 * Math.max(1, Math.sqrt(lineD0 / 100)) * dt;      // the fish takes line while you do not reel
        }
        if (lineD > lineD0 * 1.6 + 120) {
            lose("It ran away with all your line!");
            SoundFX.lose();
            return;
        }

        double wobble = burst ? 2 : 1;
        double prog = Math.min(1.3, lineD / lineD0);             // 1 = where it was hooked, 0 = at the dock
        hookX = SHORE_X + fightX0 * prog + Math.sin(fightT * 2.3) * 8 * wobble;
        double depth = Math.max(8, depth0 * prog);
        hookY = Math.min(bottomY - 15, SURFACE + depth + Math.sin(fightT * 3.7) * 6 * wobble);
        f.x = hookX;
        f.y = hookY;
        bobberX = hookX;
        bobberY = SURFACE - 3 + (burst ? 6 : 2);

        if (hunter != null) {                                  // the bigger fish rushes at the catch
            double dx = f.x - hunter.x, dy = f.y - hunter.y, d = Math.max(1, Math.hypot(dx, dy));
            if (d <= f.getLength() * 0.5 + hunter.getLength() * 0.3) {
                chomp(f);
            } else {
                double step = Math.min(d, HUNTER_SPEED[Math.max(1, Math.min(4, hunter.getTier()))] * dt);   // a bigger hunter swims faster
                hunter.x += dx / d * step;
                hunter.y += dy / d * step;
            }
        } else if (huntLeft > 0 && lineD > lineD0 * 0.2) {
            huntTimer -= dt;
            if (huntTimer <= 0) spawnHunter(f);
        }
        if (lineD <= 25) land(f);
    }

    private double tensionLimit() { return (settings.easyReel() ? 1.15 : 1.0) * (angler.has(Part.REINFORCED_LINE) ? 1.2 : 1.0); }

    private void land(Fish f) {
        fish.remove(f);
        reelFish = null;
        reelKey = reelMouse = false;
        endHunt();
        CatchRecord r = angler.addCatch(f.getSpecies(), f.getSizeCm(), f.value(), clockText(), f.getEaten());   // sold or kept later
        lastCatch = r;
        cardT = 0;
        jumpX = f.x;                                   // the fish leaps out of the water from here
        ripples.add(new double[]{f.x, 0});
        phase = Phase.CAUGHT;
        String extra = r.newSpecies() ? " NEW SPECIES!" : r.record() ? " New record!" : "";
        log.add("Caught a " + f.getSizeCm() + " cm " + f.getSpecies().getTitle() + " (worth $" + r.value() + ")." + extra);
        if (f.getSpecies().isLegendary() || r.newSpecies()) SoundFX.fanfare();
        else SoundFX.caught();
    }

    private void lose(String why) {
        endHunt();
        escaped++;
        lastLoseAt = elapsed;
        scare(reelFish);
        reelFish = null;
        reelKey = reelMouse = false;
        phase = Phase.IDLE;
        toast(why, new Color(255, 120, 110));
        log.add(why);
    }

    private void scare(Fish f) {
        if (f == null) return;
        f.startled(hookX);
    }

    private void updateEffects(double dt) {
        for (Iterator<Toast> it = toasts.iterator(); it.hasNext(); ) {
            Toast t = it.next();
            t.age += dt;
            t.y -= 22 * dt;
            if (t.age > 1.8) it.remove();
        }
        for (Iterator<double[]> it = ripples.iterator(); it.hasNext(); ) {
            double[] r = it.next();
            r[1] += dt;
            if (r[1] > 1.2) it.remove();
        }
        if (rnd.nextDouble() < dt * 1.5) {
            bubbles.add(new double[]{SHORE_X + 30 + rnd.nextDouble() * (WORLD_W - SHORE_X - 40), bottomY - 5, 20 + rnd.nextDouble() * 30});
        }
        for (Iterator<double[]> it = bubbles.iterator(); it.hasNext(); ) {
            double[] b = it.next();
            b[1] -= b[2] * dt;
            b[0] += Math.sin(b[1] * 0.1) * 0.3;
            if (b[1] < SURFACE + 4) it.remove();
        }
        fish.removeIf(Fish::isGone);
    }

    private void toast(String text, Color c) {
        toasts.add(new Toast(text, Math.min(W - 200, Math.max(200, bobberX)), SURFACE - 40, c));
    }

    // =====================================================================
    // fish population (spawner thread) and weather (weather thread)
    // =====================================================================
    private void populate(int n) {
        for (int i = 0; i < n; i++) spawnOne(SHORE_X + 80 + rnd.nextDouble() * (WORLD_W - SHORE_X - 130));
    }

    public synchronized void spawnTick() {
        if (over) return;
        int max = 60 + (raining ? 10 : 0);
        // too many fish (the ones swimming around the lure add up): remove the wanderers farthest from the lure
        while (fish.size() > max + 10) {
            Fish far = null;
            double best = -1;
            for (Fish f : fish) {
                if (f.getState() != Fish.State.WANDER || f.getSpecies().isLegendary()) continue;
                double d = Math.abs(f.x - hookX);
                if (d > best) { best = d; far = f; }
            }
            if (far == null) break;
            fish.remove(far);
        }
        for (int k = 0; k < (fish.size() < max / 2 ? 3 : 1); k++) {          // the lake is full of fish
            if (fish.size() < max && rnd.nextDouble() < 0.85) spawnOne(SHORE_X + 80 + rnd.nextDouble() * 2200);
        }
        // fish swim by around the lure all the time, so the screen is never empty: around where it is now and
        // around where the swing will take it in a moment
        if (phase == Phase.WAITING && nibbleFish == null) {
            double[][] spots = {{hookX, hookY}, hookAt(1.6), hookAt(3.2)};
            for (double[] sp : spots) {
                int around = 0;
                for (Fish f : fish) if (Math.abs(f.x - sp[0]) < 330 && Math.abs(f.y - sp[1]) < 230) around++;
                for (int k = 0; k < 2 && around < 7; k++, around++) {
                    double dir = rnd.nextBoolean() ? 1 : -1;
                    double x = sp[0] + dir * (200 + rnd.nextDouble() * 120);
                    if (x < SHORE_X + 80 || x > WORLD_W - 40) x = sp[0] - dir * (200 + rnd.nextDouble() * 120);
                    int lure = lureTier();
                    spawnOne(Math.max(SHORE_X + 80, Math.min(WORLD_W - 40, x)), sp[0], sp[1] + (rnd.nextDouble() - 0.5) * 160,
                            rnd.nextDouble() < 0.6 && lure != 9 ? lure : -1);
                }
            }
        }
        // while the lure waits, keep fish coming. They gather where the lure will hang when it stops swinging
        // (under the rod tip), so they are already there when it arrives.
        double tx = ROD_X, ty = Math.min(bottomY - 12, ROD_Y + lineLen0 - pulled);
        if (phase == Phase.WAITING && nibbleFish == null && ty - SURFACE > 60) {
            boolean near = false;
            int lure = lureTier();
            for (Fish f : fish) {
                if (Math.abs(f.x - tx) < 260 && Math.abs(f.y - ty) < 110 && canBite(lure, f)) { near = true; break; }
            }
            if ((!near || quietT > 8) && rnd.nextDouble() < 0.85) {      // nobody suitable near, or it has been quiet a while
                double x = tx + 190 + rnd.nextDouble() * 60;               // just outside the distance a fish notices a lure
                spawnOne(Math.min(WORLD_W - 40, x), tx, ty, lure);
            }
            if (sinkT < swingDur * 0.8 && rnd.nextDouble() < 0.7) {      // the lure is still swinging: meet it on the way
                double[] on = hookAt(2.2);
                boolean met = false;
                for (Fish f : fish) if (Math.abs(f.x - on[0]) < 260 && Math.abs(f.y - on[1]) < 150 && canBite(lure, f)) { met = true; break; }
                if (!met) {
                    double dir = rnd.nextBoolean() ? 1 : -1;
                    double x = on[0] + dir * (190 + rnd.nextDouble() * 60);
                    if (x < SHORE_X + 80 || x > WORLD_W - 40) x = on[0] - dir * (190 + rnd.nextDouble() * 60);
                    spawnOne(Math.max(SHORE_X + 80, Math.min(WORLD_W - 40, x)), on[0], on[1], lure);
                }
            }
        }
    }

    /** Lure class: 0..4 for the coloured lures, the size class (1..5) for a kept fish, 9 = golden. */
    private int lureTier() {
        if (!angler.isLive()) return angler.getBait().getTier();
        return angler.isLive() ? angler.getLive().species().getTier() : angler.getBait().getTier();
    }

    /** True if a fish (not a legend) of this size class could bite a lure of class B: only B = F-1. */
    private static boolean canBiteClass(int lure, int fishTier) {
        return lure != 9 && lure == fishTier - 1;
    }

    private boolean canBite(int lure, Fish f) {
        if (f.getSpecies().isLegendary()) return lure == 9;
        return canBiteClass(lure, f.getTier());
    }

    private void spawnOne(double x) { spawnOne(x, Double.NaN, Double.NaN, -1); }

    /**
     * focusY (if given) limits the choice to fish that live around that y. lure (>= 0) also limits it to fish that
     * could bite that lure, and gives them a size the lure can catch.
     */
    private void spawnOne(double x, double focusX, double focusY, int lure) {
        List<Species> pool = new ArrayList<>();
        int total = 0;
        boolean night = isNight();
        boolean timed = settings.dayNight() || settings.location().isAlwaysNight();   // with the day/night cycle off, all fish may appear
        boolean golden = lure == 9;
        boolean relaxed = false;                    // true: no fish of this depth fits the lure, so one comes from elsewhere
        for (int pass = 0; pass < 2 && pool.isEmpty(); pass++) {
            if (pass == 1) {
                if (Double.isNaN(focusY) || !golden) break;                // only legends come from another layer
                relaxed = true;
            }
            total = 0;
            for (Species s : Species.at(settings.location())) {
                if (timed && s.getTime() == Species.Time.DAY && night) continue;
                if (timed && s.getTime() == Species.Time.NIGHT && !night) continue;
                if (!relaxed && !Double.isNaN(focusY) && (focusY < SURFACE + s.getMinDepth() - 40 || focusY > SURFACE + s.getMaxDepth() + 40)) continue;
                if (lure >= 0) {
                    if (golden != s.isLegendary()) continue;                       // the golden lure calls legends, nothing else
                    if (!golden && !canBiteSomeSize(s, lure)) continue;
                }
                pool.add(s);
                total += s.getWeight();
            }
        }
        if (pool.isEmpty()) return;
        int r = rnd.nextInt(total);
        Species pick = pool.get(0);
        for (Species s : pool) {
            r -= s.getWeight();
            if (r < 0) {
                pick = s;
                break;
            }
        }
        if (pick.isLegendary()) {                                                   // only one of each legend at a time
            for (Fish other : fish) {
                if (other.getSpecies() != pick) continue;
                if (lure == 9 && other.getState() == Fish.State.WANDER) {              // it comes to the golden lure
                    other.x = x;
                    other.y = Math.max(SURFACE + 20, Math.min(bottomY - 15, focusY));
                    other.allowDepth(other.y);
                    other.headTo(focusX, focusY);
                }
                return;
            }
        }
        double minY = SURFACE + pick.getMinDepth();
        double maxY = Math.min(bottomY - 15, SURFACE + pick.getMaxDepth());
        double y = minY + rnd.nextDouble() * (maxY - minY);
        if (!Double.isNaN(focusY)) y = Math.max(minY, Math.min(maxY, focusY + (rnd.nextDouble() - 0.5) * 60));
        if (relaxed) y = Math.max(SURFACE + 20, Math.min(bottomY - 15, focusY + (rnd.nextDouble() - 0.5) * 60));
        Fish f;
        f = new Fish(pick, x, y, rnd);
        f.bottom = bottomY;
        if (x > WORLD_W) f.vx = -Math.abs(f.vx);
        fish.add(f);
        if (relaxed) f.allowDepth(y);                           // it may leave its usual depth to reach the lure
        if (lure >= 0) f.headTo(focusX, focusY);                // it came to look at the lure
        if (pick.isLegendary()) log.add("A legend swims by: " + pick.getTitle() + "!");
    }

    /** True if this species grows into a size class that the lure can catch. */
    private boolean canBiteSomeSize(Species s, int lure) {
        return s.getTier() == lure + 1;
    }

    public synchronized void setRaining(boolean r) {
        if (raining == r || over) return;
        raining = r;
        log.add(r ? "It started to rain - fish are hungrier!" : "The rain stopped.");
    }

    // =====================================================================
    // player commands (EDT)
    // =====================================================================
    /** Click 1 (or Space): the camera zooms in and the power bar starts sweeping. */
    public synchronized void startCharge() {
        if (phase == Phase.IDLE && !over) {
            phase = Phase.CHARGING;
            chargeT = 0;
            aimPower = 0;
            SoundFX.nibble();
        }
    }

    /** True once the zoom is finished and the bar is moving (a click before that is ignored). */
    public synchronized boolean isChargeReady() { return phase == Phase.CHARGING && chargeT >= ZOOM_TIME; }

    /** Click 2 (or Space again): stop the bar and throw. Power = where the marker stopped. */
    public synchronized void release() {
        if (phase != Phase.CHARGING || chargeT < ZOOM_TIME) return;
        SoundFX.setPower(-1);                          // the bar stops, so its sound stops
        aimPower = Math.max(0.08, aimPower);          // even a very weak throw lands a little way out
        castTargetX = MIN_CAST_X + aimPower * (maxCastX() - MIN_CAST_X);
        castDur = 0.75 + (castTargetX - ROD_X) / 2500.0;      // a long cast flies a little longer
        castT = 0;
        casts++;
        phase = Phase.CASTING;
        nibbleFish = null;
        SoundFX.cast();
    }

    /** The fish has swallowed the lure: it is hooked at once and the fight starts (hold SPACE / the mouse to reel). */
    private void hookIt() {
        reelFish = nibbleFish;
        nibbleFish = null;
        reelFish.state = Fish.State.HOOKED;
        if (angler.isLive() && angler.has(Part.BAIT_SAVER) && rnd.nextDouble() < 0.4) {
            toast("Bait Saver! The bait fish got away unhurt.", new Color(150, 230, 255));
        } else {
            angler.consumeLive();                      // the fish took the live bait
        }
        int shortfall = rodShortfall(reelFish.getSpecies());
        weakMul = 1 + 0.9 * shortfall;
        fightX0 = Math.max(60, hookX - SHORE_X);
        depth0 = hookY - SURFACE;
        lineD0 = Math.max(60, fightX0 * 0.5 + depth0 * 0.15);      // a deep fish takes longer to bring up
        lineD = lineD0;
        tension = 0.35;
        overTime = 0;
        slackTime = -2;                                // a little time to grab the reel before the line can go slack
        burst = false;
        burstTimer = 1.2;
        fightT = 0;
        phase = Phase.REELING;
        // a bigger fish may try to eat the catch on its way up
        huntLeft = 0;
        hunter = null;
        if (!reelFish.getSpecies().isLegendary() && reelFish.getTier() < 4 && rnd.nextDouble() < (angler.has(Part.FISH_GUARD) ? 0.16 : 0.4)) {
            huntLeft = 1 + (rnd.nextDouble() < 0.4 ? 1 : 0) + (rnd.nextDouble() < 0.15 ? 1 : 0);
            huntTimer = 1.0 + rnd.nextDouble() * 1.2;
        }
        if (shortfall > 0) {
            toast("TOO HEAVY for this rod! It will snap!", new Color(255, 120, 100));
            log.add("Too big for your " + angler.getRod().getTitle() + "! The line will snap.");
        } else {
            toast("HOOKED! Hold to reel", new Color(120, 255, 150));
        }
        log.add("Hooked! Hold SPACE / mouse to reel.");
        SoundFX.hooked();
    }

    // ---------- a bigger fish eats the catch ----------
    private Fish hunter;
    private int huntLeft, chomps;
    private double huntTimer;
    private static final double[] HUNTER_SPEED = {0, 130, 190, 280, 380};      // px/s by size class of the hunter

    public synchronized int getChomps() { return chomps; }
    public synchronized boolean hunterActive() { return hunter != null; }

    private void endHunt() {
        if (hunter != null) hunter.startled(hookX);
        hunter = null;
        huntLeft = 0;
    }

    private void spawnHunter(Fish prey) {
        List<Species> pool = new ArrayList<>();
        int total = 0;
        boolean timed = settings.dayNight() || settings.location().isAlwaysNight();
        boolean night = isNight();
        for (Species s : Species.at(settings.location())) {
            if (s.isLegendary() || s.getTier() <= prey.getTier()) continue;
            double preyDepth = prey.y - SURFACE;
            if (preyDepth < s.getMinDepth() - 150 || preyDepth > s.getMaxDepth() + 150) continue;      // a hunter lives near the prey's layer
            if (timed && s.getTime() == Species.Time.DAY && night) continue;
            if (timed && s.getTime() == Species.Time.NIGHT && !night) continue;
            pool.add(s);
            total += s.getWeight();
        }
        if (pool.isEmpty()) {
            huntLeft = 0;
            return;
        }
        int r = rnd.nextInt(total);
        Species pick = pool.get(0);
        for (Species s : pool) {
            r -= s.getWeight();
            if (r < 0) {
                pick = s;
                break;
            }
        }
        double dir = rnd.nextBoolean() ? 1 : -1;
        double x = Math.max(SHORE_X + 60, Math.min(WORLD_W - 40, prey.x + dir * (250 + rnd.nextDouble() * 80)));
        double y = Math.max(SURFACE + 20, Math.min(bottomY - 15, prey.y + (rnd.nextDouble() - 0.5) * 70));
        Fish h = new Fish(pick, x, y, rnd);
        h.bottom = bottomY;
        h.allowDepth(y);
        h.state = Fish.State.HUNTER;
        h.side = x >= prey.x ? 1 : -1;
        fish.add(h);
        hunter = h;
        toasts.add(new Toast("Watch out! A bigger fish!", prey.x, prey.y - 34, new Color(255, 110, 90)));
        SoundFX.splash();
    }

    private void chomp(Fish prey) {
        prey.eaten++;
        chomps++;
        SoundFX.eat();
        tension = Math.min(tensionLimit() * 0.95, tension + 0.25);
        boolean bones = prey.eaten >= 3;
        toasts.add(new Toast(bones ? "Only bones are left!" : "CHOMP!", prey.x, prey.y + 40, new Color(255, 90, 70)));
        log.add(bones ? "A " + hunter.getSpecies().getTitle() + " ate the whole fish - only bones are left!"
                : "A " + hunter.getSpecies().getTitle() + " bit your catch! (" + prey.eaten + "/3)");
        hunter.startled(prey.x);
        hunter = null;
        huntLeft = bones ? 0 : huntLeft - 1;
        huntTimer = 1.0 + rnd.nextDouble() * 1.2;
    }

    public synchronized void setReelKey(boolean on) { reelKey = on && phase == Phase.REELING; }
    public synchronized void setReelMouse(boolean on) { reelMouse = on && phase == Phase.REELING; }

    /** One-off depth change (used by tests). */
    public synchronized void changeDepth(int delta) {
        if (phase != Phase.WAITING) return;
        pulled = Math.max(0, pulled - delta);
        placeHook();
    }

    /** Up arrow held (dir < 0): pull the hook up. Anything else releases it; the hook sinks by itself. */
    public synchronized void setDepthMove(int dir) { pullUp = dir < 0; }

    /** Down arrow held: the swing of the lure is slowed so that fish can catch up with it. */
    public void setBrake(boolean on) { brake = on; }

    /** Pull the line out of the water without a fish. */
    public synchronized void reelIn() {
        if (phase == Phase.WAITING || phase == Phase.BITE) {
            if (nibbleFish != null) scare(nibbleFish);
            nibbleFish = null;
            phase = Phase.IDLE;
        }
    }

    /** Sell the fish on the card (Space / the Sell button / waiting too long). */
    public synchronized void sellCatch() {
        if (phase != Phase.CAUGHT || lastCatch == null) return;
        angler.sell(lastCatch);
        int got = angler.sellValue(lastCatch);
        toasts.add(new Toast("+$" + got, ROD_X + 40, SURFACE - 40, new Color(255, 230, 90)));
        log.add("Sold the " + lastCatch.sizeCm() + " cm " + lastCatch.species().getTitle() + " for $" + got + ".");
        SoundFX.coin();
        phase = Phase.IDLE;
    }

    /** Keep the fish on the card as live bait (B / the Keep button). Not allowed for legends or a full box. */
    public synchronized void keepCatchAsBait() {
        if (phase != Phase.CAUGHT || lastCatch == null) return;
        String why = angler.keepBlockReason(lastCatch);
        if (why != null) {
            toast(why + ".", new Color(255, 200, 120));
            SoundFX.lose();
            return;
        }
        angler.keepAsBait(lastCatch);
        toast("Bait fish on the hook!", new Color(150, 230, 255));
        log.add("Kept the " + lastCatch.sizeCm() + " cm " + lastCatch.species().getTitle()
                + " as live bait. Big hunters will want it!");
        SoundFX.hooked();
        phase = Phase.IDLE;
    }

    /** Why the card's Keep button is disabled (null = it works). */
    public synchronized String keepBlockReason() {
        return lastCatch == null ? "Nothing to keep" : angler.keepBlockReason(lastCatch);
    }

    /** Default answer to "continue": sell. */
    public synchronized void continueAfterCatch() {
        sellCatch();
    }

    /** Put a kept fish on the hook. */
    public synchronized String chooseLive(CatchRecord r) {
        if (!angler.selectLive(r)) return "That bait fish is gone.";
        if (phase == Phase.WAITING && nibbleFish != null) {
            scare(nibbleFish);
            nibbleFish = null;
        }
        return "Live bait on the hook: " + r.species().getTitle() + " " + r.sizeCm() + " cm.";
    }

    public synchronized String chooseBait(Bait b) {
        if (!angler.owns(b)) return b.getTitle() + " is not bought yet - visit the shop.";
        angler.setBait(b);
        if (phase == Phase.WAITING && nibbleFish != null) {
            scare(nibbleFish);
            nibbleFish = null;
        }
        return "Bait changed to " + b.getTitle() + ".";
    }

    public synchronized String uninstall(Part p) {
        return angler.uninstall(p);
    }

    public synchronized String buy(ShopItem item) {
        String r = angler.buy(item);
        if (r.startsWith("Bought") || r.startsWith("Equipped")) SoundFX.coin();
        return r;
    }

    // =====================================================================
    // queries
    // =====================================================================
    public boolean isNight() {
        if (settings.location().isAlwaysNight()) return true;
        int h = (int) (clock / 60);
        return h >= 19 || h < 6;
    }

    public synchronized String clockText() {
        int m = (int) clock;
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    /** 0 = midnight ... 1 = noon, used for sky colour. */
    public synchronized double daylight() {
        if (settings.location().isAlwaysNight()) return 0;
        double h = clock / 60.0;
        if (h < 5 || h >= 20) return 0;
        if (h < 7) return (h - 5) / 2;
        if (h < 17) return 1;
        return 1 - (h - 17) / 3;
    }


    public String pollLog() { return log.poll(); }

    public synchronized List<Fish> fishSnapshot() { return new ArrayList<>(fish); }
    public synchronized List<Toast> getToasts() { return toasts; }
    public synchronized List<double[]> getRipples() { return ripples; }
    public synchronized List<double[]> getBubbles() { return bubbles; }

    public Settings getSettings() { return settings; }
    public Angler getAngler() { return angler; }
    public synchronized Phase getPhase() { return phase; }
    public synchronized double getAimPower() { return aimPower; }
    public synchronized double getChargeT() { return chargeT; }
    public synchronized double getCastTargetPreview() { return MIN_CAST_X + aimPower * (maxCastX() - MIN_CAST_X); }
    public synchronized double getBobberX() { return bobberX; }
    public synchronized double getBobberY() { return bobberY; }
    public synchronized double getHookX() { return hookX; }
    public synchronized double getHookY() { return hookY; }
    public synchronized int getHookDepth() { return (int) Math.round(getHookDepthF()); }
    public synchronized double getHookDepthF() { return Math.max(0, hookY - SURFACE); }
    public synchronized double getTension() { return tension; }
    public synchronized double getTensionLimit() { return tensionLimit(); }
    public synchronized boolean isBurst() { return burst; }
    public synchronized boolean isReeling() { return reelKey || reelMouse; }
    public synchronized double getLineProgress() { return lineD0 <= 0 ? 0 : 1 - Math.min(1, lineD / Math.max(lineD0, 1)); }
    public synchronized double getSlackDanger() { return slackTime / (settings.easyReel() ? 4.0 : 2.5); }
    public synchronized double getBiteTimer() { return biteTimer; }
    public synchronized Fish getReelFish() { return reelFish; }
    public synchronized CatchRecord getLastCatch() { return lastCatch; }
    public synchronized double getCardT() { return cardT; }
    public synchronized double getJumpX() { return jumpX; }
    public synchronized double getCastProgress() { return Math.min(1, castT); }

    public synchronized Mood getMood() {
        if (phase == Phase.CAUGHT) return Mood.HAPPY;
        if (phase == Phase.REELING) return Mood.FOCUS;
        if (elapsed - lastLoseAt < 2.0) return Mood.SAD;
        return Mood.NORMAL;
    }
    public synchronized boolean isRaining() { return raining; }
    public synchronized double getElapsed() { return elapsed; }
    public synchronized int getCasts() { return casts; }
    public synchronized int getEscaped() { return escaped; }
    public boolean isOver() { return over; }

    /** Ends the session early (End button). */
    public synchronized void finish() {
        if (!over) {
            if (phase == Phase.CAUGHT && lastCatch != null) sellCatch();      // do not lose the fish that is on the card
            over = true;
            log.add("You packed up your rod. See you next time!");
        }
    }
}
