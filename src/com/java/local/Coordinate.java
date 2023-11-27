package com.java.local;

public class Coordinate {

    static public WorldCoord latLon2WorldCoord(LatLon latLon) {
        double siny = Math.sin((latLon.lat * Math.PI) / 180);
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);
        return new WorldCoord(256 * (0.5 + latLon.lon / 360), 
                              256 * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI)));
    }

    static public Point worldCoord2TileCoord(int zoom, WorldCoord p) {
        return new Point((int)Math.floor((p.x * (1 << zoom)) / 256),
                         (int)Math.floor((p.y * (1 << zoom)) / 256));
    }

    static public Point worldCoord2PixelCoord(int zoom, WorldCoord p) {
        return new Point((int)Math.floor((p.x * (1 << zoom))),
                         (int)Math.floor((p.y * (1 << zoom))));
    }

    static public LatLon tileCoord2LatLon(int zoom, Point p) {
        double n = Math.PI - (2.0 * Math.PI * p.y) / Math.pow(2.0, zoom);
        return new LatLon(Math.toDegrees(Math.atan(Math.sinh(n))),
                          p.x / Math.pow(2.0, zoom) * 360.0 - 180);
    }
}
