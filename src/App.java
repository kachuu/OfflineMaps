import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

public class App {
    public static void main(String[] args) throws Exception {
        App app = new App();
        // app.test1();
        app.test2();
    }

    public void test1() {
        System.out.println(">>> download map tiles for network");

        LatLon latLonLT = new LatLon(-33.959591878069034, 151.09710802548128);//left top coordinate of rectangle on digital map
        LatLon latLonRB = new LatLon(-33.970459461303896, 151.11512819536605);//right bottom coordinate of rectangle on digital map
        int zoom = 17;//zoom level of digital map

        Point tileCoordLT = worldCoord2TileCoord(zoom, latLon2WorldCoord(latLonLT));
        System.out.println(String.format("zoom = %d, l/t tile coordinate(%d, %d)", zoom, tileCoordLT.x, tileCoordLT.y));

        Point tileCoordRB = worldCoord2TileCoord(zoom, latLon2WorldCoord(latLonRB));
        System.out.println(String.format("zoom = %d, r/b tile coordinate(%d, %d)", zoom, tileCoordRB.x, tileCoordRB.y));

        for (int x = tileCoordLT.x; x <= tileCoordRB.x; ++x) {
            for (int y = tileCoordLT.y; y <= tileCoordRB.y; ++y) {
                downloadTiles(zoom, x, y);//download according to storage rules of digital map
            } 
        }

        System.out.println("<<< end");
    }

    public void test2() {
        System.out.println(">>> create map tiles for paper map");

        double scale = 110000;//paper map scale
        int zoom = (int)Math.round(Math.log(591657527.591555 / scale) / Math.log(2));
        System.out.println(String.format("zoom = %d", zoom));

        LatLon latLonLT = new LatLon(-33.361437897206116, 150.56579437671328);//coordinate on left top side of paper map
        createTiles(paperMap2DigitalMap(zoom, latLonLT, "PaperMap.png"), 
                    zoom, latLonLT);

        System.out.println("<<< end");
    }

    public void createTiles(BufferedImage map, int zoom, LatLon latLon) {
        try {
            if (null == map) return;

            BufferedImage tile = new BufferedImage(256, 256, BufferedImage.TYPE_USHORT_565_RGB);//create tile canvas
            Graphics g = tile.getGraphics();

            Point tileCoord =worldCoord2TileCoord(zoom, latLon2WorldCoord(latLon));
            System.out.println(String.format("tile coord x = %d, y = %d", tileCoord.x, tileCoord.y));
            for (int x = 0; x < map.getWidth() / 256; ++x) {
                for (int y = 0; y < map.getHeight() / 256; ++y) {
                    File dir = new File(String.format("%s/%s", zoom, tileCoord.x + x));
                    if (!dir.exists()) dir.mkdirs();            

                    String tileFile = String.format("%s/%s/%s.png", zoom, tileCoord.x + x, tileCoord.y + y);
                    System.out.println(tileFile);

                    g.drawImage(map, -256 * x, -256 * y, null);//cut digital map according to specified tile size
                    ImageIO.write(tile, "png", new File(tileFile));//save tile
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public BufferedImage paperMap2DigitalMap(int zoom, LatLon latLon, String mapFile) {
        try {
            LatLon latLonOrg = tileCoord2LatLon(zoom, worldCoord2TileCoord(zoom, latLon2WorldCoord(latLon)));
            Point pxCoordLT = worldCoord2PixelCoord(zoom, latLon2WorldCoord(latLon));
            Point pxCoordOrg = worldCoord2PixelCoord(zoom, latLon2WorldCoord(latLonOrg));
            Point pxDiff = new Point(pxCoordLT.x - pxCoordOrg.x, pxCoordLT.y - pxCoordOrg.y);
            System.out.println(String.format("diff px x = %d, y = %d", pxDiff.x, pxDiff.y));//offset of paper map on digital map
            
            BufferedImage orgMap = ImageIO.read(new File(mapFile));
            int orgMapW = orgMap.getWidth(null);
            int orgMapH = orgMap.getHeight(null);
            System.out.println(String.format("org map w = %d, h = %d", orgMapW, orgMapH));

            int newMapW = (orgMapW + pxDiff.x) + 256 - ((orgMapW + pxDiff.x) % 256);
            int newMapH = (orgMapH + pxDiff.y) + 256 - ((orgMapH + pxDiff.y) % 256);
            System.out.println(String.format("new map w = %d, h = %d", newMapW, newMapH));//new digital map size, divisible by 256

            BufferedImage newMap = new BufferedImage(newMapW, newMapH, BufferedImage.TYPE_USHORT_565_RGB);
            Graphics g = newMap.getGraphics();
            g.setColor(Color.red);//highlight the differences between old and new maps, only test
            g.fillRect(0, 0, newMapW, newMapH);//only test
            g.drawImage(orgMap, pxDiff.x, pxDiff.y, null);
            ImageIO.write(newMap, "png", new File("Test.png"));//only test
            return newMap;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public class LatLon {
        public double lat;
        public double lon;
        public LatLon(double lat, double lon) {this.lat = lat; this.lon = lon;}
    }

    public class WorldCoord {
        public double x;
        public double y;
        public WorldCoord(double x, double y) {this.x = x; this.y = y;}
    }

    public class Point {
        public int x;
        public int y;
        public Point(int x, int y) {this.x = x; this.y = y;}
    }

    public WorldCoord latLon2WorldCoord(LatLon latLon) {
        double siny = Math.sin((latLon.lat * Math.PI) / 180);
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);
        return new WorldCoord(256 * (0.5 + latLon.lon / 360), 
                              256 * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI)));
    }

    public Point worldCoord2TileCoord(int zoom, WorldCoord p) {
        return new Point((int)Math.floor((p.x * (1 << zoom)) / 256),
                         (int)Math.floor((p.y * (1 << zoom)) / 256));
    }

    public Point worldCoord2PixelCoord(int zoom, WorldCoord p) {
        return new Point((int)Math.floor((p.x * (1 << zoom))),
                         (int)Math.floor((p.y * (1 << zoom))));
    }

    public LatLon tileCoord2LatLon(int zoom, Point p) {
        double n = Math.PI - (2.0 * Math.PI * p.y) / Math.pow(2.0, zoom);
        return new LatLon(Math.toDegrees(Math.atan(Math.sinh(n))),
                          p.x / Math.pow(2.0, zoom) * 360.0 - 180);
    }

    // public final String MAP_URL = "http://mt1.google.com/vt/lyrs=m&x=%s&y=%s&z=%s";
    public final String MAP_URL = "https://tile.openstreetmap.org/%d/%d/%d.png";
    public void downloadTiles(int z, int x, int y) {
        try {
            URL url = new URL(String.format(MAP_URL, z, x, y));
            System.out.println(url);

            File dir = new File(String.format("%s/%s", z, x));
            if (!dir.exists()) dir.mkdirs();            

            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            FileOutputStream fos = new FileOutputStream(String.format("%s/%s/%s.png", z, x, y), false);
            byte buf[] = new byte[1024];
            for (int i = 0; -1 != (i = bis.read(buf, 0, 1024));) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    static {
        //issue: tile.openstreetmap.org Server returned HTTP response code: 403
        //https://stackoverflow.com/questions/56397206/gluon-maps-doesnt-load-the-map-and-throws-an-exception
        System.setProperty("http.agent", "Gluon Mobile/1.0.3");
    }
}
