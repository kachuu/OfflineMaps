package com.java.local;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.awt.Color;

public class OfflineMap {

    static public void createTiles(BufferedImage map, int zoom, LatLon latLon) {
        try {
            if (null == map) return;

            BufferedImage tile = new BufferedImage(256, 256, BufferedImage.TYPE_USHORT_565_RGB);//create tile canvas
            Graphics g = tile.getGraphics();

            Point tileCoord = Coordinate.worldCoord2TileCoord(zoom, Coordinate.latLon2WorldCoord(latLon));
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

    static public BufferedImage paperMap2DigitalMap(String mapFile, int zoom, LatLon latLon) {
        try {
            LatLon latLonOrg = Coordinate.tileCoord2LatLon(zoom, Coordinate.worldCoord2TileCoord(zoom, Coordinate.latLon2WorldCoord(latLon)));
            Point pxCoordTL = Coordinate.worldCoord2PixelCoord(zoom, Coordinate.latLon2WorldCoord(latLon));
            Point pxCoordOrg = Coordinate.worldCoord2PixelCoord(zoom, Coordinate.latLon2WorldCoord(latLonOrg));
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

    // public final String MAP_URL = "http://mt1.google.com/vt/lyrs=m&x=%s&y=%s&z=%s";
    static public final String MAP_URL = "https://tile.openstreetmap.org/%d/%d/%d.png";
    static public void downloadTiles(int z, int x, int y) {
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
