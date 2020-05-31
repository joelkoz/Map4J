package org.map4j.render;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.map4j.coordinates.PBox;
import org.map4j.coordinates.PCoordinate;
import org.map4j.coordinates.TBox;
import org.map4j.coordinates.TCoordinate;
import org.map4j.loaders.Tile;
import org.map4j.loaders.TileLoaderController;
import org.map4j.loaders.Tile.TileTopicListener;
import org.map4j.tinymq.TinyMQ;


/**
 * A TileGridImage represents an image composed of a square section of 
 * adjacent tiles somewhere in pixel space.  The grid will be loaded asynchronously,
 * publishing an "UPDATED" topic to the broker whenever the image has
 * changed (usually as each tile is successfully loaded).
 * 
 * @author Joel Kozikowski
 */
public class TileGridImage  implements TileTopicListener {

    public static TinyMQ<TileGridImage> broker = new TinyMQ<TileGridImage>();
    
    public interface TileGridImageTopicListener extends TinyMQ.ITopicSubscriber<TileGridImage> {};

    public static final int pixelTileSize = 256;
    
    
    /**
     * The topic published to broker whenever this tile grid image has changed. Call getImage()
     * to get the most up to date version of the image. 
     */
    public static final String TOPIC_UPDATED = "update";
    
    private PCoordinate pCenter;
    private TCoordinate tCenter;
    private Tile[][] tileGrid;
    private int tilesPerSide;
    private TCoordinate tileGridUL;
    private BufferedImage baseImage;
    private TileLoaderController tileController;
    private int loaded;
    
    /**
     * Create a TileGridImage with tiles loaded from the specified loader controller. The grid
     * will be located in such a way that the pCenter coordinate is somewhere in the center
     * tile of the tile box.  The grid will be a square of tiles with dimensions of
     * tilesPerSide x tilesPerSide.
     */
    public TileGridImage(TileLoaderController tileController, PCoordinate pCenter, int tilesPerSide) {
        
        this.tileController = tileController;
        this.tilesPerSide = tilesPerSide;
        
        this.pCenter = new PCoordinate(pCenter);
        this.tCenter = pCenter.asT(true); 
        
        tileGrid = new Tile[tilesPerSide][tilesPerSide];
        
        int masterImageSize = tilesPerSide * pixelTileSize;
        baseImage = new BufferedImage(masterImageSize, masterImageSize, BufferedImage.TYPE_INT_ARGB);

        Tile.broker.subscribe(Tile.TOPIC_LOADED, this);
        
        recalcTileGrid();
    }

    
    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }


    
    /**
     * Stops the loading process (if it is still in progress) and unsubscribes the grid image
     * from the loader. This should be called whenever you know for sure you are discarding
     * the tile loader to help the tile broker out.
     */
    public synchronized void stop() {
        if (tileController != null) {
            tileController.cancelOutstandingJobs();
            Tile.broker.unsubscribe(Tile.TOPIC_LOADED, this);
            tileController = null;
        }
    }
    
    

    /**
     * Returns the tile grid image as it currently stands. It may or may not yet be 
     * completed as all tiles may not have been fully loaded yet.
     */
    public synchronized BufferedImage getImage() {
        return baseImage;
    }
    
    
    /**
     * Returns TRUE if the tile load has completed (that is, no more tiles
     * in the grid are in the "unloaded" or "loading" state.
     */
    public synchronized boolean isLoadCompleted() {
        if (loaded == -1) {
            // Traverse the grid and return FALSE if any tiles are found to be
            // in the "loaded" state.
            TCoordinate tc = new TCoordinate(tileGridUL);
            for (int col = 0; col < tilesPerSide; col++) {
               tc.setCol(tileGridUL.getCol());
               for (int row = 0; row < tilesPerSide; row++) {
                   if (tileGrid[col][row].isLoading()) {
                       // It only takes one to indicate we are not done...
                       return false;
                   }
                   tc.adjustCol(1);
               } // col
               tc.adjustRow(1);
            } // row
            
            // If we get here, then no tiles are marked "isLoading()"...
            loaded = 1;
        }
        
        return (loaded == 1);
    }

    
    
    /**
     * Forces the image to be re-rendered.
     */
    public synchronized void refresh() {
        this.loadTileGrid();
        broker.publish(TOPIC_UPDATED, this);
    }
    
    
    
    /**
     * Returns the pixel box that represents the area covered by this image.
     */
    public PBox getPixelBox() {
        TBox tbox = this.getTileBox();
        PCoordinate ul = tbox.t1.asP();
        PCoordinate lr = tbox.t2.asP();
        lr.adjustX(pixelTileSize-1);
        lr.adjustY(pixelTileSize-1);
        return new PBox(ul, lr);
    }
    
    

    /**
     * Returns the tile box that represents the area covered by this image.
     */
    public TBox getTileBox() {
        TCoordinate tileGridLR = new TCoordinate(tileGridUL);
        tileGridLR.adjustCol(tilesPerSide-1);
        tileGridLR.adjustRow(tilesPerSide-1);
        return new TBox(tileGridUL, tileGridLR);
    }

    
    /**
     * Returns the pixel coordinate for the center of this image.
     */
    public PCoordinate getPCenter() {
        return pCenter;
    }

    
    /**
     * Returns the tile coordinate that is at the center of the grid image.
     */
    public TCoordinate getTCenter() { 
        return tCenter; 
    }


    
    /**
     * Respond to messages from the tile message broker about their loading status...
     */
    @Override
    public void onPublish(String topic, Tile tile) {
        if (topic.equals(Tile.TOPIC_LOADED)) {
            synchronized(this) {
               renderTile(tile);
            }
            broker.publish(TOPIC_UPDATED, this);
        }
    }
    
    
    private void recalcTileGrid() {
        // We are about to re-calculate the tile grid, so stop any pending load jobs...
        tileController.cancelOutstandingJobs(); 

        // Calculate the upper left of the tile grid (in XYZ tile space)...
        tileGridUL = new TCoordinate(tCenter);
        tileGridUL.adjustCol(-tilesPerSide / 2);
        tileGridUL.adjustRow(-tilesPerSide / 2);
        
        loadTileGrid();
    }
    
    
    private void loadTileGrid() {
        loaded = -1;
        TCoordinate tc = new TCoordinate(tileGridUL);
        for (int col = 0; col < tilesPerSide; col++) {
           tc.setCol(tileGridUL.getCol());
           for (int row = 0; row < tilesPerSide; row++) {
               tileGrid[col][row] = tileController.getTile(tc);
               this.renderTile(tileGrid[col][row]);
               tc.adjustCol(1);
           } // col
           tc.adjustRow(1);
        } // row
    }

    
    /**
     * Draws the specified tile onto the base image in its current state
     * at its appropriate location.
     */
    private void renderTile(Tile tile) {
        int colOffset = tile.coord.getCol() - tileGridUL.getCol();
        int rowOffset = tile.coord.getRowAsXYZ() - tileGridUL.getRowAsXYZ();
        
        int x = colOffset * pixelTileSize;
        int y = rowOffset * pixelTileSize;
        
        Graphics g = baseImage.getGraphics();
        g.clearRect(x, y, pixelTileSize, pixelTileSize);
        g.drawImage(tile.getImage(), x, y, null);
        g.dispose();
    }

}
