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
 * Base class for utility that that downloads tiles from an online map
 * service and saves them to an MBTiles file.
 * 
 * @author Joel Kozikowski
 */
public class MapServiceToMBTiles implements Runnable, Tile.TileTopicListener {

    protected MapService mapService;
    protected MBTilesFile mbTiles;
    protected int zoom;
    protected boolean useXYZ;
    protected int tileCount;
    protected WBox downloadArea;
    protected String mbtFileName;
    protected String tilesetName;
    protected String tilesetDescription;
    protected long startTime;
    
    public MapServiceToMBTiles(MapService mapService, WBox downloadArea, int zoom,
                               boolean useXYZ,
                               String tilesetName, String tilesetDescription, 
                               String mbtFileName) {
        this.mapService = mapService;
        this.zoom = zoom;
        this.useXYZ = useXYZ;
        this.tileCount = 0;
        this.downloadArea = downloadArea;
        this.mbtFileName = mbtFileName;
        this.tilesetName = tilesetName;
        this.tilesetDescription = tilesetDescription;
    }
    
    public void run() {
        System.out.println("Starting tile saver of " + tilesetDescription + " for zoom level " + zoom);
        startTime = System.currentTimeMillis();
        
        Tile.broker.subscribe(Tile.TOPIC_LOADED, this);
        
        try {
            // Get a tile loader to load individual tiles
            ITileLoader tileLoader = mapService.getTileLoader();
            int tileJobs = 0;
            
            // Prepare an MBTiles file to write to
            mbTiles = new MBTilesFile(mbtFileName);
            if (mbTiles.isNew()) {
                System.out.println("Adding metadata to new MBTiles file");
                MBMetadata.CoordinateBox bounds = new MBMetadata.CoordinateBox(downloadArea.w1.getLon(), downloadArea.w1.getLat(), downloadArea.w2.getLon(), downloadArea.w2.getLat());
                MBMetadata meta = new MBMetadata(tilesetName, TileSetType.OVERLAY, "1", tilesetDescription, TileMimeType.PNG, bounds);
                mbTiles.updateMetadata(meta);
            }

            TBox tileset = downloadArea.asTBox(zoom, useXYZ);
            for (int row = tileset.t1.getRow(); row <= tileset.t2.getRow(); row++) {
                for (int col = tileset.t1.getCol(); col <= tileset.t2.getCol(); col++) {

                    // Make sure there is room to make more requests...
                    while (tileLoader.jobQueueFull()) {
                        System.out.println("Job queue is full - pausing...");
                        Thread.sleep(1500);
                    }
                    
                    TCoordinate tc = tileset.t1.getT(col, row);
                    if (mbTiles.getTile(zoom, col, tc.getRowAsTMS()) == null) {
                        Tile tile = new Tile(tilesetName, tc);
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
            System.out.println("Runtime: " + ((System.currentTimeMillis() - startTime) / 1000) / 60.0 + " minutes.");
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
    
}
