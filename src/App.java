import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.NumberFormat;
import javax.imageio.ImageIO;

public class App {
    public static void main(String[] args) throws Exception {
        App app = new App();
        if (0 < args.length && args[0].equals("-web") && 4 == args.length) {
            String[] latlon1 = args[2].split(",");
            String[] latlon2 = args[3].split(",");
            if (2 != latlon1.length || 2 != latlon2.length) {
                app.cmdLineParam();
                return;
            }

            app.cmdLine1(NumberFormat.getInstance().parse(args[1]).intValue(), 
                         app.new LatLon(NumberFormat.getInstance().parse(latlon1[0]).doubleValue(), 
                                        NumberFormat.getInstance().parse(latlon1[1]).doubleValue()),
                         app.new LatLon(NumberFormat.getInstance().parse(latlon2[0]).doubleValue(), 
                                        NumberFormat.getInstance().parse(latlon2[1]).doubleValue()));
        } else
        if (0 < args.length && args[0].equals("-paper") && 4 == args.length) {
            String[] latlon = args[3].split(",");
            if (2 != latlon.length) {
                app.cmdLineParam();
                return;
            }

            app.cmdLine2(args[1],
                         NumberFormat.getInstance().parse(args[2]).intValue(), 
                         app.new LatLon(NumberFormat.getInstance().parse(latlon[0]).doubleValue(), 
                                        NumberFormat.getInstance().parse(latlon[1]).doubleValue()));
        } else app.cmdLineParam();
    }

    public void cmdLineParam() {
        System.out.println(
        "\n" + 
        "Command line parameters\n" +
        "\n" +
        "Download map tiles for network\n" +
        "  -web z lat1,lon1 lat2,lon2\n" +
        "            z ........ zoom level\n" +
        "            lat1 ..... Latitude of the top-left corner of the rectangle\n" +
        "            lon1 ..... Longitude of the top-left corner of the rectangle\n" +
        "            lat2 ..... Latitude of the bottom-right corner of the rectangle\n" +
        "            lon2 ..... Longitude of the bottom-right corner of the rectangle\n" +
        "\n" +
        "Create map tiles for paper map\n" +
        "  -paper f s lat,lon\n" +
        "            f ........ paper map file. e.g. full file path\n" +
        "            s ........ paper map scale\n" +
        "            lat ...... Latitude of the top-left side of paper map\n" +
        "            lon ...... Longitude of the top-left side of paper map\n" +
        "\n");
    }

    //zoom: zoom level of digital map
    //latLonTL: top-left coordinate of rectangle on digital map
    //latLonBR: bottom-right coordinate of rectangle on digital map
    public void cmdLine1(int zoom, LatLon latLonTL, LatLon latLonBR) {
        System.out.println(">>> download map tiles for network");

        Point tileCoordTL = worldCoord2TileCoord(zoom, latLon2WorldCoord(latLonTL));
        System.out.println(String.format("zoom = %d, t/l tile coordinate(%d, %d)", zoom, tileCoordTL.x, tileCoordTL.y));

        Point tileCoordBR = worldCoord2TileCoord(zoom, latLon2WorldCoord(latLonBR));
        System.out.println(String.format("zoom = %d, b/r tile coordinate(%d, %d)", zoom, tileCoordBR.x, tileCoordBR.y));

        for (int x = tileCoordTL.x; x <= tileCoordBR.x; ++x) {
            for (int y = tileCoordTL.y; y <= tileCoordBR.y; ++y) {
                downloadTiles(zoom, x, y);//download according to storage rules of digital map
            } 
        }

        System.out.println("<<< end");
    }

    //mapFile: paper map file. e.g. full file path
    //scale: paper map scale
    //latLonTL: coordinate on top-left side of paper map
    public void cmdLine2(String mapFile, int scale, LatLon latLonTL) {
        System.out.println(">>> create map tiles for paper map");

        int zoom = (int)Math.round(Math.log(591657527.591555 / scale) / Math.log(2));
        System.out.println(String.format("zoom = %d", zoom));

        createTiles(paperMap2DigitalMap(mapFile, zoom, latLonTL), 
                    zoom, latLonTL);

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

    public BufferedImage paperMap2DigitalMap(String mapFile, int zoom, LatLon latLon) {
        try {
            LatLon latLonOrg = tileCoord2LatLon(zoom, worldCoord2TileCoord(zoom, latLon2WorldCoord(latLon)));
            Point pxCoordTL = worldCoord2PixelCoord(zoom, latLon2WorldCoord(latLon));
            Point pxCoordOrg = worldCoord2PixelCoord(zoom, latLon2WorldCoord(latLonOrg));
            Point pxDiff = new Point(pxCoordTL.x - pxCoordOrg.x, pxCoordTL.y - pxCoordOrg.y);
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
