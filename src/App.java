import java.text.NumberFormat;
import com.java.local.Coordinate;
import com.java.local.LatLon;
import com.java.local.OfflineMap;
import com.java.local.Point;

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
                         new LatLon(NumberFormat.getInstance().parse(latlon1[0]).doubleValue(), 
                                    NumberFormat.getInstance().parse(latlon1[1]).doubleValue()),
                         new LatLon(NumberFormat.getInstance().parse(latlon2[0]).doubleValue(), 
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
                         new LatLon(NumberFormat.getInstance().parse(latlon[0]).doubleValue(),
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

        Point tileCoordTL = Coordinate.worldCoord2TileCoord(zoom, Coordinate.latLon2WorldCoord(latLonTL));
        System.out.println(String.format("zoom = %d, t/l tile coordinate(%d, %d)", zoom, tileCoordTL.x, tileCoordTL.y));

        Point tileCoordBR = Coordinate.worldCoord2TileCoord(zoom, Coordinate.latLon2WorldCoord(latLonBR));
        System.out.println(String.format("zoom = %d, b/r tile coordinate(%d, %d)", zoom, tileCoordBR.x, tileCoordBR.y));

        for (int x = tileCoordTL.x; x <= tileCoordBR.x; ++x) {
            for (int y = tileCoordTL.y; y <= tileCoordBR.y; ++y) {
                OfflineMap.downloadTiles(zoom, x, y);//download according to storage rules of digital map
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

        OfflineMap.createTiles(OfflineMap.paperMap2DigitalMap(mapFile, zoom, latLonTL), 
                                zoom, latLonTL);

        System.out.println("<<< end");
    }
}
