# OfflineMaps  
 Download map tiles from internet, create map tiles using paper map  
  
### Command line  
```bash  
Download map tiles for network  
&nbsp;&nbsp;-web z lat1,lon1 lat2,lon2  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;z ........ zoom level  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lat1 ..... Latitude of the top-left corner of the rectangle  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lon1 ..... Longitude of the top-left corner of the rectangle  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lat2 ..... Latitude of the bottom-right corner of the rectangle  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lon2 ..... Longitude of the bottom-right corner of the rectangle  
  
Create map tiles for paper map  
&nbsp;&nbsp;-paper f s lat,lon  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;f ........ paper map file. e.g. full file path  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;s ........ paper map scale  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lat ...... Latitude of the top-left side of paper map  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;lon ...... Longitude of the top-left side of paper map  
```  
  
**Download cmd**  
```bash  
java App -web 17 -33.959591878069034,151.09710802548128 -33.970459461303896,151.11512819536605  
```  
  
**Create cmd**  
```bash  
java App -paper PaperMap.png 110000 -33.361437897206116,150.56579437671328  
```  
  
### Download map tiles from the internet:  
1. Determine the latitude and longitude of the top-left and bottom-right corners of the map rectangular area.  
2. Convert the latitude and longitude into map tile indices based on the map's zoom level.  
3. Download the map tiles according to tile indices.  
4. Save the map tiles in the format z/x/y.png. This is the data source format for offline map tools.  
  
The following image is from zoom level = **17**, tileX = **120549**, tileY = **78695**.  
Save format is **17/120549/78695.png**  
![image](https://github.com/kachuu/OfflineMaps/blob/main/bin/17/120549/78695.png)  
  
### Create map tiles using paper map  
1. Convert the scale of a paper map to a zoom level.  
2. Obtain the latitude and longitude corresponding to the top-left corner of the paper map.  
3. Calculate the pixel coordinates of the top-left corner origin of the paper map on the digital map. Because the paper map may not perfectly align with the digital map, its origin may be located at some point inside a digital map tile.  
4. Adjust the top-left corner origin of the paper map to align it perfectly with the digital map.  
5. Convert latitude and longitude to map tile indices based on the map's zoom level.  
6. Cut the map according to the map tile indices.  
7. Save the map tiles in the format z/x/y.png. This is the data source format for offline map tools.  
  
The following image is zoom level  
![image](https://github.com/kachuu/OfflineMaps/blob/main/ZoomLevel.png)  
  
The following image is original paper map  
![image](https://github.com/kachuu/OfflineMaps/blob/main/PaperMap.jpeg)  
  
The following image is adjusted paper map  
The red area indicates the areas that need adjustment. The paper map needs to be shifted to the right and down in order to align perfectly with the digital map, and the map needs to be slightly enlarged for further processing.  
![image](https://github.com/kachuu/OfflineMaps/blob/main/bin/Test.png)  
  
The following image is from paper map, zoom level = **12**, tileX = **3771**, tileY = **2460**.  
Save format is **12/3771/2460.png**  
![image](https://github.com/kachuu/OfflineMaps/blob/main/bin/12/3771/2460.png)  
  
