// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.util.Random;

/**
 * One fish swimming in the lake. Small state machine: wander -> approach -> nibble -> hooked, or flee.
 *
 * Free swimming looks like a real fish: it steers toward a waypoint with a limited turn rate (so it
 * swims on diagonals and makes wide turns), glides between tail-beat bursts, slows down in a turn,
 * and its body tilts to match the direction it is moving.
 */
public class Fish {

    public enum State { WANDER, APPROACH, NIBBLE, HOOKED, FLEE, HUNTER }

    private static final double MAX_CLIMB = 0.50;     // steepest dive/climb angle while cruising (about 29 degrees)

    private final Species species;
    private final int sizeCm;
    private final double length;     // drawn size in pixels
    double x, y, vx, vy;             // position and velocity (pixels, pixels per second)
    private double life;
    State state = State.WANDER;
    int nibblesLeft;
    double nibbleTimer;
    double anim;
    double ignoreTimer;              // after losing interest, ignore the hook for a while
    double lane, laneTarget, laneVel;   // sideways position for the 2.5D view (-1 left .. +1 right)
    double bottom = LakeGame.BOTTOM; // lowest y this fish may swim (set by the game for the current spot)

    // ---- swimming
    private double heading;          // radians: 0 = right, PI = left, positive = diving
    private double speed;
    private double wpX, wpY, wpTimer;
    private double glidePhase;
    private double boostT;           // startled fish swim faster for a moment
    // ---- drawing
    private double pitch;            // body tilt (radians, positive = nose down)
    private double flip = 1;         // +1 faces right, -1 faces left, in between = turning around
    private double lastX, lastY, dvx, dvy;
    private boolean started;

    public Fish(Species s, double x, double y, Random rnd) {
        this(s, x, y, rnd, 0, Integer.MAX_VALUE);
    }

    /** Like the normal constructor, but the size is forced into [minCm, maxCm] when that overlaps the species range. */
    public Fish(Species s, double x, double y, Random rnd, int forceMinCm, int forceMaxCm) {
        this.species = s;
        double r = Math.pow(rnd.nextDouble(), 1.6);   // small fish are more common
        int lo = Math.max(s.getMinCm(), forceMinCm), hi = Math.min(s.getMaxCm(), forceMaxCm);
        if (lo > hi || (forceMinCm == 0 && forceMaxCm == Integer.MAX_VALUE)) {
            lo = s.getMinCm();
            hi = s.getMaxCm();
        } else {
            r = rnd.nextDouble();                       // a forced size range: any size in it
        }
        this.sizeCm = (int) Math.round(lo + (hi - lo) * r);
        this.length = Math.max(18, Math.min(130, 18 + sizeCm * 0.45));
        this.x = x;
        this.y = y;
        boolean right = rnd.nextBoolean();
        this.heading = right ? 0 : Math.PI;
        this.flip = right ? 1 : -1;
        this.speed = s.getSpeed() * (0.4 + rnd.nextDouble() * 0.2);
        this.vx = Math.cos(heading) * speed;
        this.life = 30 + rnd.nextDouble() * 40;
        this.anim = rnd.nextDouble() * 10;
        this.glidePhase = rnd.nextDouble() * 6;
        this.lane = rnd.nextDouble() * 2 - 1;
        this.laneTarget = rnd.nextDouble() * 2 - 1;
        this.wpTimer = 0;
    }

    public double getLane() { return lane; }
    public boolean facingRight3D() { return laneVel >= 0; }

    public Species getSpecies() { return species; }
    public int getSizeCm() { return sizeCm; }
    public double getLength() { return length; }
    public double getX() { return x; }
    public double getY() { return y; }
    public State getState() { return state; }
    /** Size class 1..4 of this fish (Small .. Giant). */
    public int getTier() { return species.getTier(); }
    public boolean facingRight() { return flip >= 0; }

    /** 1.0 when facing fully sideways, near 0 in the middle of turning around (used to squash the picture). */
    public double getTurnScale() { return Math.max(0.14, Math.abs(flip)); }

    /** Rotation to draw the picture with: body tilts along the swimming direction, either way it faces. */
    public double getTilt() { return flip >= 0 ? pitch : -pitch; }

    /** Horizontal speed in px/s as drawn (for the 2.5D view). */
    public double getDisplayVx() { return dvx; }
    public double getLaneVel() { return laneVel; }

    /** Value grows with size: 60% of base at minimum size, 140% at maximum. */
    /** Share of the price that is left after 0, 1, 2, 3 bites by bigger fish (3 = only bones). */
    public static final double[] VALUE_LEFT = {1.0, 0.7, 0.4, 0.1};
    int eaten;                       // bites taken out of this fish while it was being reeled in
    public int getEaten() { return eaten; }

    // [PRESENT] RULE PRICE: size x species price, reduced by how many times a hunter bit it
    // ไทย: กฎราคา: ราคาพื้นฐานของชนิดปลา คูณตามขนาดตัว คูณสัดส่วนที่เหลือหลังโดนนักล่ากัด
    public int value() {
        double t = (double) (sizeCm - species.getMinCm()) / Math.max(1, species.getMaxCm() - species.getMinCm());
        return (int) Math.round(species.getBaseValue() * (0.6 + 0.8 * t) * VALUE_LEFT[Math.min(3, eaten)]);
    }

    private double lowY = Double.NaN, highY = Double.NaN;      // a fish called to the lure may leave its usual depth band

    /** Lets this fish swim at depth y even if it normally lives shallower or deeper (it came to look at the lure). */
    void allowDepth(double y) {
        double lo = Math.max(LakeGame.SURFACE + 15, y - 60), hi = Math.min(bottom - 15, y + 60);      // stay inside the water
        lowY = Double.isNaN(lowY) ? lo : Math.min(lowY, lo);
        highY = Double.isNaN(highY) ? hi : Math.max(highY, hi);
    }

    public double minY() {
        double m = Math.max(LakeGame.SURFACE + 15, LakeGame.SURFACE + species.getMinDepth());
        return Double.isNaN(lowY) ? m : Math.max(LakeGame.SURFACE + 15, Math.min(m, lowY));
    }

    public double maxY() {
        double m = Math.min(bottom - 15, LakeGame.SURFACE + species.getMaxDepth());
        return Double.isNaN(highY) ? m : Math.max(m, Math.min(bottom - 15, highY));
    }

    /** Picks a new place to swim to: usually a long diagonal, always inside the fish's depth band. */
    private void pickWaypoint(Random rnd) {
        double lo = LakeGame.SHORE_X + 60, hi = LakeGame.WORLD_W - 40;
        double dir = rnd.nextDouble() < 0.7 ? Math.signum(Math.cos(heading) == 0 ? 1 : Math.cos(heading)) : -Math.signum(Math.cos(heading));
        double reach = 140 + rnd.nextDouble() * 360;
        wpX = Math.max(lo, Math.min(hi, x + dir * reach));
        if (Math.abs(wpX - x) < 90) wpX = dir > 0 ? Math.max(lo, x - 200) : Math.min(hi, x + 200);   // too close to a wall: go the other way
        // mostly gentle slopes, now and then a long climb or dive to another depth
        double top = minY(), bot = maxY();
        double r = rnd.nextDouble();
        double range = 30 + r * r * 220;
        wpY = Math.max(top, Math.min(bot, y + (rnd.nextDouble() * 2 - 1) * range));
        wpTimer = 3 + rnd.nextDouble() * 5;
    }

    private static double wrap(double a) {
        while (a > Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    /** The fish bolts away from a point (a scare). */
    void startled(double fromX) {
        state = State.WANDER;
        ignoreTimer = 8;
        heading = x >= fromX ? 0.25 : Math.PI - 0.25;
        boostT = 1.3;
        wpTimer = 0;
    }

    /** Free swimming (not when hooked - the game moves a hooked fish). */
    void update(double dt, Random rnd) {
        if (!started) {
            started = true;
            lastX = x;
            lastY = y;
        }
        if (ignoreTimer > 0) ignoreTimer -= dt;
        if (boostT > 0) boostT -= dt;

        // velocity that the game applied to the fish since the last frame (approach, hooked...)
        double mvx = dt > 0 ? (x - lastX) / dt : 0, mvy = dt > 0 ? (y - lastY) / dt : 0;

        // sideways drift (only visible in the 2.5D view). Fish going for the hook swim to lane 0,
        // because the line is cast straight ahead.
        double before = lane;
        if (state == State.WANDER || state == State.FLEE) {
            if (rnd.nextDouble() < dt * 0.2) laneTarget = rnd.nextDouble() * 2 - 1;
            lane += (laneTarget - lane) * Math.min(1, dt * 0.35);
        } else {
            lane += (0 - lane) * Math.min(1, dt * 1.6);
        }
        if (dt > 0 && Math.abs(lane - before) > 1e-6) laneVel = (lane - before) / dt;

        double targetFlip;
        switch (state) {
            case WANDER -> {
                life -= dt;
                wpTimer -= dt;
                double dx = wpX - x, dy = wpY - y;
                if (wpTimer <= 0 || Math.hypot(dx, dy) < 28) {
                    pickWaypoint(rnd);
                    dx = wpX - x;
                    dy = wpY - y;
                }
                // aim at the waypoint, but never steeper than MAX_CLIMB (fish do not swim straight up or down)
                double off = Math.min(MAX_CLIMB, Math.atan2(Math.abs(dy), Math.max(1e-3, Math.abs(dx))));
                double desired = dx >= 0 ? Math.signum(dy) * off : Math.PI - Math.signum(dy) * off;
                double diff = wrap(desired - heading);
                double turnRate = Math.max(1.0, Math.min(2.6, 2.6 - length / 90.0));   // small fish turn quicker
                heading = wrap(heading + Math.max(-turnRate * dt, Math.min(turnRate * dt, diff)));
                // tail-beat bursts and glides, slower in a sharp turn
                glidePhase += dt * (1.1 + 40.0 / (length + 40));
                double cruise = species.getSpeed() * 0.55;
                speed = cruise * (0.62 + 0.62 * Math.max(0, Math.sin(glidePhase)));
                speed *= 1 - 0.55 * Math.min(1, Math.abs(diff) / 1.6);
                if (boostT > 0) speed = Math.max(speed, species.getSpeed() * 1.9);
                vx = Math.cos(heading) * speed;
                vy = Math.sin(heading) * speed;
                x += vx * dt;
                y += vy * dt;
                // soft walls: the water band, and the two ends of the lake
                if (y < minY()) { y += (minY() - y) * Math.min(1, dt * 4); if (vy < 0) wpY = Math.max(wpY, minY() + 40); }
                if (y > maxY()) { y += (maxY() - y) * Math.min(1, dt * 4); if (vy > 0) wpY = Math.min(wpY, maxY() - 40); }
                if (x < LakeGame.SHORE_X + 40 && Math.cos(heading) < 0) wpTimer = 0;
                if (x > LakeGame.WORLD_W - 30 && Math.cos(heading) > 0) wpTimer = 0;
                if (life <= 0) state = State.FLEE;
                dvx = vx;
                dvy = vy;
                targetFlip = Math.abs(vx) > 3 ? Math.signum(vx) : flip >= 0 ? 1 : -1;
            }
            case FLEE -> {
                heading = wrap(heading + (0.22 - heading) * Math.min(1, dt * 3));
                speed = species.getSpeed() * 1.7;
                vx = Math.cos(heading) * speed;
                vy = Math.sin(heading) * speed;
                x += vx * dt;
                y += vy * dt;
                y = Math.max(LakeGame.SURFACE + 15, Math.min(bottom - 15, y));      // never through the floor or out of the water
                fleeT += dt;
                dvx = vx;
                dvy = vy;
                targetFlip = 1;
            }
            default -> {
                // approach / nibble / hooked: the game moves the fish, we only follow what it did
                dvx += (mvx - dvx) * Math.min(1, dt * 8);
                dvy += (mvy - dvy) * Math.min(1, dt * 8);
                vx = dvx;
                vy = dvy;
                targetFlip = state == State.HOOKED ? 1 : -side;
            }
        }
        flip += (targetFlip - flip) * Math.min(1, dt * 6.5);

        // body tilt follows the climb / dive angle
        double targetPitch = Math.max(-0.60, Math.min(0.60, Math.atan2(dvy, Math.max(10, Math.abs(dvx)))));
        if (state == State.NIBBLE) targetPitch = 0.16;
        if (state == State.HOOKED) targetPitch += Math.sin(anim * 1.3) * 0.28;
        pitch += (targetPitch - pitch) * Math.min(1, dt * 6);

        // tail beats faster when swimming faster
        double sp = Math.hypot(dvx, dvy);
        anim += dt * (state == State.HOOKED ? 14 : 3.5 + sp * 0.16);

        lastX = x;
        lastY = y;
    }

    /** Side of the hook the fish approaches from: +1 = it is right of the hook, -1 = left. Fixed when it starts. */
    int side = 1;

    /** Remembers which side the fish is on, so its "mouth point" never jumps to the other side of the hook. */
    void startApproach(double hookX) {
        side = x >= hookX ? 1 : -1;
        approachT = 0;
    }

    double approachT;                // seconds spent chasing the hook

    /** Makes a wandering fish swim toward a point (used for a fish that arrives to look at the lure). */
    void headTo(double tx, double ty) {
        wpX = Math.max(LakeGame.SHORE_X + 60, Math.min(LakeGame.WORLD_W - 40, tx));
        wpY = Math.max(minY(), Math.min(maxY(), ty));
        wpTimer = 10;
    }

    /** The fish stops chasing (the hook got too far away): it goes back to swimming and ignores the hook for a while. */
    void giveUp() {
        state = State.WANDER;
        ignoreTimer = 6;
        wpTimer = 0;
    }

    /**
     * Swims to the fish's mouth point beside the hook (len*0.45 away on the side it came from).
     * The target does not depend on where the fish faces, so the fish cannot flip left/right forever.
     * @return distance left
     */
    double approachHook(double hookX, double hookY, double dt, double hookSpeed) {
        double tx = hookX + side * length * 0.45, ty = hookY;
        double dx = tx - x, dy = ty - y;
        double d = Math.hypot(dx, dy);
        if (d < 0.5) return 0;
        double step = Math.min(d, Math.max(Math.max(130, species.getSpeed() * 1.8), hookSpeed * 1.6) * dt);   // a fish that has locked on keeps up with a moving lure      // a fish that has spotted the hook swims straight to it
        x += dx / d * step;
        y += dy / d * step;
        return d - step;
    }

    private double fleeT;            // seconds spent fleeing: a fleeing fish disappears after a while
    public boolean isGone() { return state == State.FLEE && (x > LakeGame.WORLD_W + 80 || fleeT > 10); }
}
