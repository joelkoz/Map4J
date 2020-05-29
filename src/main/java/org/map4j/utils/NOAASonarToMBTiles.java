package org.map4j.utils;

import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.map4j.coordinates.TBox;
import org.map4j.coordinates.TCoordinate;
import org.map4j.coordinates.WBox;
import org.map4j.loaders.ITileLoader;
import org.map4j.loaders.ITileLoaderJob;
import org.map4j.loaders.MapService;
import org.map4j.loaders.Tile;
import org.map4j.utils.mbtiles.MBMetadata;
import org.map4j.utils.mbtiles.MBTilesFile;
import org.map4j.utils.mbtiles.MBMetadata.TileMimeType;
import org.map4j.utils.mbtiles.MBMetadata.TileSetType;


/**
 * Creates an MBTiles file out of NOAA bathymetric data found on
 * https://maps.ngdc.noaa.gov/viewers/bathymetry 
 * 
 * @author Joel Kozikowski
 */
public class NOAASonarToMBTiles implements Runnable, Tile.TileTopicListener {

    public static final String sourceId = "NOAA-BAG";
    public static final String baseUrl = "https://gis.ngdc.noaa.gov/arcgis/rest/services/bag_hillshades/ImageServer/tile";
    public static final String tilesetName = "NOAA_BAG_SFL";
    public static final String tilesetDescription = "South Florida Sonar images";
    public static final boolean useXYZ = true;
    
    public static final WBox southFloridaReefs = new WBox(25.595, -80.199, 26.349, -80.049);

    public static class NOAAImageService extends MapService {
        
        public NOAAImageService() {
            super(sourceId, baseUrl, null, useXYZ);
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
    
    
    private NOAAImageService mapService;
    private MBTilesFile mbTiles;
    private int zoom;
    private int tileCount;
    
    public NOAASonarToMBTiles(int zoom) {
        mapService = new NOAAImageService();
        this.zoom = zoom;
        this.tileCount = 0;
    }
    
    public void run() {
        System.out.println("Starting NOAA tile saver for zoom level " + zoom);
        
        Tile.broker.subscribe(Tile.TOPIC_LOADED, this);
        
        final String mbTilesName = "NOAA Raster South Florida Sonar.mbtiles";
        try {
            // Get a tile loader to load individual tiles
            ITileLoader tileLoader = mapService.getTileLoader();
            int tileJobs = 0;
            
            // Prepare an MBTiles file to write to
            mbTiles = new MBTilesFile(mbTilesName);
            if (mbTiles.isNew()) {
                System.out.println("Adding metadata to new MBTiles file");
                MBMetadata.CoordinateBox bounds = new MBMetadata.CoordinateBox(southFloridaReefs.w1.getLon(), southFloridaReefs.w1.getLat(), southFloridaReefs.w2.getLon(), southFloridaReefs.w2.getLat());
                MBMetadata meta = new MBMetadata(tilesetName, TileSetType.OVERLAY, "1", tilesetDescription, TileMimeType.PNG, bounds);
                mbTiles.updateMetadata(meta);
            }

            TBox tileset = southFloridaReefs.asTBox(zoom, useXYZ);
            for (int row = tileset.t1.getRow(); row <= tileset.t2.getRow(); row++) {
                for (int col = tileset.t1.getCol(); col <= tileset.t2.getCol(); col++) {

                    // Make sure there is room to make more requests...
                    while (tileLoader.jobQueueFull()) {
                        System.out.println("Job queue is full - pausing...");
                        Thread.sleep(1500);
                    }
                    
                    TCoordinate tc = tileset.t1.getT(col, row);
                    if (mbTiles.getTile(zoom, col, tc.getRowAsTMS()) == null) {
                        Tile tile = new Tile(sourceId, tc);
                        System.out.println("Requesting tile " + tc.getRequestPath());
                        ITileLoaderJob job = tileLoader.createTileLoaderJob(tile);
                        job.startTileLoad();
                        tileJobs++;
                    }
                    else {
                        System.out.println("Skipping pre-existing tile " + zoom + "/" + col + " /" + row);
                    }
                    
                } // for col
            } // for row
            
            
            while (tileLoader.hasOutstandingJobs()) {
                System.out.println("Waiting for tile loader to complete job...");
                Thread.sleep(2000);
            }
            
            System.out.println("Download of zoom level " + zoom + " complete. Saved " + this.tileCount + " of " + tileJobs + " tiles.");
            mbTiles.updateMinZoom();
            mbTiles.updateMaxZoom();
            mbTiles.close();
        }
        catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    
    @Override
    public void onPublish(String topic, Tile tile) {
        if (topic.equals(Tile.TOPIC_LOADED)) {
            TCoordinate coord = tile.coord;
            try {
                if (!tile.hasError() ) {
                    System.out.println("Retrieved tile " + tile.coord.getRequestPath());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(tile.getImage(), "png", bos);
                    mbTiles.addTile(bos.toByteArray(), coord.getZoom(), coord.getCol(), coord.getRowAsTMS());
                    System.out.println("Saved MBTile " + tile.getCacheKey());
                    this.tileCount++;
                }
                else {
                    System.err.println("Error retrieving tile " + tile.coord.getRequestPath() + ": " + tile.getErrorMessage());
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

  
    
    public static void main(String[] args) {
        NOAASonarToMBTiles tileMaker = new NOAASonarToMBTiles(15);
        tileMaker.run();
        System.exit(0);
    }
}
