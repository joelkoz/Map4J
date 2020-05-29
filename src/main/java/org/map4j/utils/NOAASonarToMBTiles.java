package org.map4j.utils;

import org.map4j.coordinates.WBox;
import org.map4j.loaders.MapService;
import org.map4j.loaders.Tile;


/**
 * Creates an MBTiles file out of NOAA bathymetric data found on
 * https://maps.ngdc.noaa.gov/viewers/bathymetry 
 * 
 * @author Joel Kozikowski
 */
public class NOAASonarToMBTiles extends MapServiceToMBTiles {

    public static final String baseUrl = "https://gis.ngdc.noaa.gov/arcgis/rest/services/bag_hillshades/ImageServer/tile";
    public static final String tilesetName = "NOAA_BAG_SFL";
    public static final String tilesetDescription = "South Florida Sonar images";
    public static final String mbtFileName = "NOAA Raster South Florida Sonar.mbtiles";
    
    public static final boolean useXYZ = true;
    
    public static final WBox southFloridaReefs = new WBox(25.595, -80.199, 26.349, -80.049);

    
    /**
     * Map service that downloads tiles from the NOAA tile server. That server 
     * uses a non-standard coordinate order in the url (zoom/row/col)
     * 
     * @author Joel Kozikowski
     */
    public static class NOAAImageService extends MapService {
        
        public NOAAImageService() {
            super(tilesetName, baseUrl, null, useXYZ);
        }

        /**
         * NOAA uses the ArcGIS map server. The REST API has a tile retrieval URL different from
         * XYZ and TMS.
         * @see https://developers.arcgis.com/rest/services-reference/map-tile.htm
         */
        @Override
        public String getTileUrl(Tile tile) {
            return baseUrl + "/" + tile.coord.getZoom() + "/" + tile.coord.getRow() + "/" + tile.coord.getCol();
        }
        
    }
    
    
    public NOAASonarToMBTiles(int zoom) {
        super(new NOAAImageService(), southFloridaReefs, zoom, useXYZ,
              tilesetName, tilesetDescription, mbtFileName);
    }
    
    public static void main(String[] args) {
        NOAASonarToMBTiles tileMaker = new NOAASonarToMBTiles(14);
        tileMaker.run();
        System.exit(0);
    }
}
