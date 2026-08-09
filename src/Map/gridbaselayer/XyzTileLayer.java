package Map.gridbaselayer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.*;

import javax.imageio.ImageIO;

/**
 * Very small XYZ tile layer provider and drawer.
 * - caches tiles to a simple local disk cache under user.home/.pamguard/tilecache
 * - fetches tiles in a fixed thread pool
 * - exposes drawTiles(Graphics2D g, MapRectProjector rectProj, int width, int height)
 *
 * This is intentionally minimal to make integration and review easier. It can be
 * extended with tile expiration, provider lists, attribution, and better error handling.
 */
public class XyzTileLayer {

    private final String template; // e.g. https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png
    private final ExecutorService fetchPool = Executors.newFixedThreadPool(4);
    private final File cacheDir;

    public XyzTileLayer(String template) {
        this.template = template;
        cacheDir = new File(System.getProperty("user.home"), ".pamguard/tilecache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    /**
     * Draw tiles onto the given Graphics2D using the projector to compute
     * geographic bounds. This is synchronous for already-cached tiles and
     * will start background fetches for missing tiles (they will appear after
     * the next repaint).
     */
    public void drawTiles(Graphics2D g, MapRectProjector rectProj, int width, int height) {
        // Determine visible lat/lon bounds
        double minLat = rectProj.panel2LL(0, height).getLatitude();
        double maxLat = rectProj.panel2LL(0, 0).getLatitude();
        double minLon = rectProj.panel2LL(0, 0).getLongitude();
        double maxLon = rectProj.panel2LL(width, 0).getLongitude();

        // Use WebMercator tile scheme. Choose zoom from map scale (approx)
        int z = zoomForScale(rectProj.getPixelsPerMetre());

        int xMin = lon2tileX(minLon, z);
        int xMax = lon2tileX(maxLon, z);
        int yMin = lat2tileY(maxLat, z);
        int yMax = lat2tileY(minLat, z);

        // clamp
        xMin = Math.max(0, xMin); yMin = Math.max(0, yMin);

        int tileSize = 256;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                final int fx = x;
                final int fy = y;
                File cached = cacheFile(z, fx, fy);
                BufferedImage tile = null;
                if (cached.exists()) {
                    try {
                        tile = ImageIO.read(cached);
                    } catch (IOException e) {
                        // ignore and try fetch
                        tile = null;
                    }
                }
                if (tile != null) {
                    // compute where to draw tile
                    int px = tileXToPixelX(fx, z, width, rectProj);
                    int py = tileYToPixelY(fy, z, height, rectProj);
                    g.drawImage(tile, px, py, null);
                } else {
                    // schedule fetch and optionally draw placeholder
                    fetchPool.submit(() -> {
                        try {
                            BufferedImage t = fetchTile(z, fx, fy);
                            if (t != null) {
                                ImageIO.write(t, "png", cached);
                                // Request a repaint by setting the repaint flag on the Swing thread
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    // attempt to find map panel and repaint - simplest is global repaint
                                    java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow().repaint();
                                });
                            }
                        } catch (Exception e) {
                            // ignore fetch errors for now
                        }
                    });
                }
            }
        }
    }

    private File cacheFile(int z, int x, int y) {
        File zDir = new File(cacheDir, Integer.toString(z));
        File xDir = new File(zDir, Integer.toString(x));
        xDir.mkdirs();
        return new File(xDir, Integer.toString(y) + ".png");
    }

    private BufferedImage fetchTile(int z, int x, int y) {
        try {
            String url = template.replace("{z}", Integer.toString(z))
                    .replace("{x}", Integer.toString(x))
                    .replace("{y}", Integer.toString(y))
                    .replace("{s}", "a");
            URL u = new URL(url);
            return ImageIO.read(u);
        } catch (IOException e) {
            return null;
        }
    }

    // Very simple heuristics to pick a zoom from pixel scale.
    private int zoomForScale(double pixelsPerMetre) {
        // WebMercator: at equator, scale for one tile pixel in metres varies with zoom.
        // Use a rough mapping for typical screen densities. This can be tuned.
        double metresPerPixel = 1.0 / pixelsPerMetre;
        // approximate metres per pixel at zoom 0 is ~156543.03
        double zf = Math.log(156543.03 / metresPerPixel) / Math.log(2);
        int z = (int) Math.round(zf);
        if (z < 0) z = 0;
        if (z > 19) z = 19;
        return z;
    }

    // Slippy map helper functions
    private int lon2tileX(double lon, int z) {
        return (int) Math.floor((lon + 180.0) / 360.0 * (1 << z));
    }

    private int lat2tileY(double lat, int z) {
        double latRad = Math.toRadians(lat);
        double n = Math.PI - Math.log(Math.tan(Math.PI / 4.0 + latRad / 2.0));
        return (int) Math.floor(n / (2.0 * Math.PI) * (1 << z));
    }

    // Convert tile x to pixel coordinate within panel using rectProj
    private int tileXToPixelX(int tileX, int z, int panelWidth, MapRectProjector rectProj) {
        double lon = tileXToLon(tileX, z);
        // use centre latitude for approximation
        double lat = rectProj.getMapCentreDegrees().getLatitude();
        java.awt.Point p = rectProj.latLongToPanel(new PamUtils.LatLong(lat, lon));
        return p.x;
    }

    private int tileYToPixelY(int tileY, int z, int panelHeight, MapRectProjector rectProj) {
        double lat = tileYToLat(tileY, z);
        double lon = rectProj.getMapCentreDegrees().getLongitude();
        java.awt.Point p = rectProj.latLongToPanel(new PamUtils.LatLong(lat, lon));
        return p.y;
    }

    private double tileXToLon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    private double tileYToLat(int y, int z) {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

}
