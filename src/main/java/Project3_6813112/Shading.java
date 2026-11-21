// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Gives flat drawings a more finished look: soft light from above (bright top, darker bottom) and a thin dark outline.
 * The drawing is first painted into a small image, then lit, then drawn back. Used by the fish and the shop items.
 * Finished pictures are kept in a small cache, so a fish that is drawn 60 times a second is lit only once per pose.
 */
public final class Shading {

    private Shading() { }

    private static final int CACHE_SIZE = 400;
    private static final Map<String, BufferedImage> CACHE = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > CACHE_SIZE;                      // forget the picture that was used longest ago
        }
    };

    // [GRAPHICS] rounds the tail phase to 8 poses so the cache can reuse pictures
    /** The same pose of the same fish: the animation phase rounded to 8 steps of one wag. */
    public static double steps(double anim) {
        double turn = 2 * Math.PI;
        double a = ((anim % turn) + turn) % turn;
        return Math.round(a / turn * 8) * turn / 8;
    }

    // [GRAPHICS] LIGHT + OUTLINE: paints flat shapes into a small image, adds a top-light gradient and a dark outline, caches the result
    /**
     * @param key a text that is the same for the same picture (null = do not keep it)
     * @param x0,y0 the top-left corner of the box (in the coordinates of g) that contains the whole drawing
     * @param w,h the size of that box
     * @param shadeTop,shadeBottom where the light gradient starts and ends (y, in the coordinates of g)
     * @param outline outline width in the coordinates of g
     * @param painter paints the drawing in the same coordinates as g (called only when the picture is not in the cache)
     */
    public static void paint(String key, Graphics2D g, double x0, double y0, double w, double h, double shadeTop, double shadeBottom,
                             double outline, Consumer<Graphics2D> painter) {
        AffineTransform tx = g.getTransform();
        double sc = Math.max(1.0, Math.hypot(tx.getScaleX(), tx.getShearY()));         // image pixels per unit
        int iw = (int) Math.ceil(w * sc), ih = (int) Math.ceil(h * sc);
        if (iw < 2 || ih < 2 || iw > 3000 || ih > 3000) {                               // too small or too big: paint plainly
            painter.accept(g);
            return;
        }
        String full = key == null ? null : key + "|" + Math.round(sc * 4);
        BufferedImage done = null;
        if (full != null) {
            synchronized (CACHE) {
                done = CACHE.get(full);
            }
        }
        if (done == null) {
            done = build(sc, iw, ih, x0, y0, shadeTop, shadeBottom, outline, painter);
            if (full != null) {
                synchronized (CACHE) {
                    CACHE.put(full, done);
                }
            }
        }
        Graphics2D out = (Graphics2D) g.create();
        out.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate(x0, y0);
        at.scale(1 / sc, 1 / sc);
        out.drawImage(done, at, null);
        out.dispose();
    }

    private static BufferedImage build(double sc, int iw, int ih, double x0, double y0, double shadeTop, double shadeBottom,
                                       double outline, Consumer<Graphics2D> painter) {
        BufferedImage img = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig = img.createGraphics();
        ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ig.scale(sc, sc);
        ig.translate(-x0, -y0);
        painter.accept(ig);
        ig.dispose();

        // light from above, only where the drawing has paint
        Graphics2D lg = img.createGraphics();
        lg.setComposite(AlphaComposite.SrcAtop);
        float top = (float) ((shadeTop - y0) * sc), bottom = (float) ((shadeBottom - y0) * sc);
        if (bottom - top < 1) bottom = top + 1;
        lg.setPaint(new LinearGradientPaint(0, top, 0, bottom, new float[]{0f, 0.35f, 1f},
                new Color[]{new Color(255, 255, 255, 85), new Color(255, 255, 255, 0), new Color(0, 25, 45, 100)}));
        lg.fillRect(0, 0, iw, ih);
        lg.dispose();

        // the same picture in one dark colour, drawn a little to each side: this makes the outline
        BufferedImage edge = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        Graphics2D eg = edge.createGraphics();
        eg.drawImage(img, 0, 0, null);
        eg.setComposite(AlphaComposite.SrcAtop);
        eg.setColor(new Color(18, 32, 46, 215));
        eg.fillRect(0, 0, iw, ih);
        eg.dispose();

        BufferedImage done = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dg = done.createGraphics();
        double d = outline * 1.2 * sc;
        for (int k = 0; k < 4; k++) {
            double a = k * Math.PI / 2 + Math.PI / 4;
            dg.drawImage(edge, (int) Math.round(Math.cos(a) * d), (int) Math.round(Math.sin(a) * d), null);
        }
        dg.drawImage(img, 0, 0, null);
        dg.dispose();
        return done;
    }
}
