# OfflineMaps  
 Generate offline maps using data from online maps or paper maps  
  
### How to work  
Properties of tiled web maps that require convention or standards include the size of tiles, the numbering of zoom levels, the projection to use, the way individual tiles are numbered or otherwise identified, and the method for requesting them.  
  
Most tiled web maps follow certain Google Maps conventions:  
1. Tiles are 256x256 pixels.  
2. At the outer most zoom level, 0, the entire world can be rendered in a single map tile.  
3. Each zoom level doubles in both dimensions, so a single tile is replaced by 4 tiles when zooming in. This means that about 22 zoom levels are sufficient for most practical purposes.  
4. The Web Mercator projection is used, with latitude limits of around 85 degrees.  
  
The de facto OpenStreetMap standard, known as Slippy Map Tilenames[2] or XYZ,[3] follows these and adds more:  
1. An X and Y numbering scheme.  
2. PNG images for tiles.  
3. Images are served through a Web server, with a URL like *http://.../Z/X/Y.png*, where Z is the zoom level, and X and Y identify the tile.  
  
## Example  
### Get offline maps from the internet  
Select a rectangular area on the world map, with its four corners marked by four latitude and longitude points on the world map.  
  
Convert the coordinates of the top-left corner of a rectangular area from latitude and longitude to the coordinates on a flat map.  
```bash  
Coordinate.latLon2WorldCoord(latLonTL)
```  
  
Convert coordinates of a flat map to tile coordinates based on zoom level.  
```bash  
Coordinate.worldCoord2TileCoord(zoom, WorldCoordTL)
```  
  
The bottom-right corner of the rectangular area is also determined in the same way.  
  
The values corresponding to the top-left and bottom-right coordinates of the rectangular area in relation to the tile coordinates have been obtained. Now iterate through these tile coordinates, retrieve their respective data, and save them.  
```bash  
for (tileCoordTL.x : tileCoordBR.x) {
    for (tileCoordTL.y : tileCoordBR.y) {
        OfflineMap.downloadTiles(zoom, x, y)
    }
}
```  
  
### Get offline maps from a paper map  
Convert the scale of a paper map to the zoom level of a digital map.  
```bash  
int zoom = (int)Math.round(Math.log(591657527.591555 / scale) / Math.log(2));
```  
  
The image is relationship between *zoom* and *scale*.  
![image](https://github.com/kachuu/OfflineMaps/blob/main/ZoomLevel.png)  
  
Convert a paper map into a digital map. Since the top left corner of the paper map may not coincide with the tile, it is necessary to slightly shift the paper map for correction. The calculation here requires the use of the *zoom* variable.  
```bash  
OfflineMap.paperMap2DigitalMap(mapFile, zoom, latLonTL)
```  
  
Now, having obtained the coordinates corresponding to the top-left corner of the paper map in relation to the tiles, as well as the width and height of the paper map in pixels, it is possible to iteratively retrieve tile data and save them.  
```bash  
OfflineMap.createTiles(newMapFile, zoom, latLonTL)
```  
  
## Run  
### Command line  
```bash  
Download map tiles for network  
  -web z lat1,lon1 lat2,lon2  
            z ........ zoom level  
            lat1 ..... Latitude of the top-left corner of the rectangle  
            lon1 ..... Longitude of the top-left corner of the rectangle  
            lat2 ..... Latitude of the bottom-right corner of the rectangle  
            lon2 ..... Longitude of the bottom-right corner of the rectangle  
  
Create map tiles for paper map  
  -paper f s lat,lon  
            f ........ paper map file. e.g. full file path  
            s ........ paper map scale  
            lat ...... Latitude of the top-left side of paper map  
            lon ...... Longitude of the top-left side of paper map  
```  
  
### Get offline maps from the internet  
```bash  
java App -web 17 -33.959591878069034,151.09710802548128 -33.970459461303896,151.11512819536605
```  
  
The image is from zoom level = 17, tileX = 120549, tileY = 78695. Save format is 17/120549/78695.png  
![image](https://github.com/kachuu/OfflineMaps/blob/main/bin/17/120549/78695.png)  
  
### Get offline maps from a paper map  
```bash  
java App -paper PaperMap.png 110000 -33.361437897206116,150.56579437671328
```  
  
The image is original paper map  
![image](https://github.com/kachuu/OfflineMaps/blob/main/PaperMap.jpeg)  
  
The image is adjusted paper map. The red area indicates the areas that need adjustment. The paper map needs to be shifted to the right and down in order to align perfectly with the digital map, and the map needs to be slightly enlarged for further processing.  
![image](https://github.com/kachuu/OfflineMaps/blob/main/bin/Test.png)  
  
The image is from paper map, zoom level = 12, tileX = 3771, tileY = 2460. Save format is 12/3771/2460.png  
![image](https://github.com/kachuu/OfflineMaps/blob/main/bin/12/3771/2460.png)  
  
