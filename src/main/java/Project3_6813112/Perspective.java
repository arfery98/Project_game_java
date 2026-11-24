// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.geom.Point2D;

// [GRAPHICS] turns a position in the flat world into a screen position with depth (2.5-D) before casting | SCREEN: Game (view before casting)
// ไทย: แปลงตำแหน่งในโลกแบนเป็นตำแหน่งบนจอแบบมีความลึก (2.5 มิติ) ก่อนโยนเบ็ด | อยู่ที่: หน้า Game (มุมมองก่อนโยนเบ็ด)
/**
 * Maps the game's 2-D world (x = distance from the dock, y = depth) into the
 * 3-D "behind the angler" camera view.
 *
 * Pinhole camera: a point on the water at distance z appears at
 *     screenY = HORIZON + FOCAL * CAM_HEIGHT / z
 * so far things crowd toward the horizon and everything shrinks by 1 / z.
 * We choose where the near and far edges of the fishing area land on screen and
 * solve for z, so the playable area fills the screen nicely.
 * Underwater points are pushed down by their depth (x 0.42 = "apparent depth",
 * water makes things look shallower), also scaled by 1 / z.
 */
public final class Perspective {

    public static final double HORIZON = 168;      // screen y of the horizon
    public static final double NEAR_Y = 384;       // where the water by the dock appears
    public static final double FAR_Y = 178;        // where the far end of the lake appears
    public static final double CENTER_X = 430;     // screen x straight ahead of the angler
    public static final double FOCAL_X_HEIGHT = 520;   // FOCAL * CAM_HEIGHT (pixels x metres)
    public static final double LANE_WIDTH = 420;   // sideways spread of lanes at scale 1
    public static final double DEPTH_K = 0.42;     // apparent-depth factor

    private Perspective() { }

    /** 0 at the dock, 1 at the far end of the lake. */
    public static double u(double worldX) {
        double v = (worldX - LakeGame.SHORE_X) / (LakeGame.WORLD_W - LakeGame.SHORE_X);
        return Math.max(0, Math.min(1, v));
    }

    /** Screen y of the water surface for a world x. */
    public static double surfaceY(double worldX) {
        return NEAR_Y - u(worldX) * (NEAR_Y - FAR_Y);
    }

    /** Distance from the camera in metres (from screenY = HORIZON + F*H / z). */
    public static double distance(double worldX) {
        return FOCAL_X_HEIGHT / (surfaceY(worldX) - HORIZON);
    }

    /** Size multiplier: things twice as far look half as big. */
    public static double scale(double worldX) {
        return Math.max(0.12, Math.min(1.15, 2.6 / distance(worldX)));
    }

    /**
     * Projects a world point to the screen.
     * @param lane sideways position, -1 (left) .. +1 (right), 0 = straight ahead
     */
    public static Point2D.Double project(double worldX, double worldY, double lane) {
        double s = scale(worldX);
        double sx = CENTER_X + lane * LANE_WIDTH * s;
        // under water: apparent depth (x DEPTH_K); above water (a flying bobber): full height
        double k = worldY < LakeGame.SURFACE ? 0.9 : DEPTH_K;
        double sy = surfaceY(worldX) + (worldY - LakeGame.SURFACE) * k * s;
        return new Point2D.Double(sx, sy);
    }

    /** Screen y of a surface row at fraction u (0 near .. 1 far) - used to draw wave rows. */
    public static double rowY(double uu) {
        return NEAR_Y - uu * (NEAR_Y - FAR_Y);
    }

    /** Scale at fraction u. */
    public static double rowScale(double uu) {
        double sy = rowY(uu);
        return Math.max(0.12, Math.min(1.15, 2.6 / (FOCAL_X_HEIGHT / (sy - HORIZON))));
    }
}
