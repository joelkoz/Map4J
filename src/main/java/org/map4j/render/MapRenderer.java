package org.map4j.render;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.map4j.coordinates.PBox;
import org.map4j.coordinates.PCoordinate;
import org.map4j.coordinates.TCoordinate;
import org.map4j.coordinates.WCoordinate;
import org.map4j.layers.IMapObject;
import org.map4j.layers.MapLayer;
import org.map4j.layers.VisibleMapObjectVisitor;
import org.map4j.loaders.Tile;
import org.map4j.loaders.TileLoaderController;
import org.map4j.loaders.Tile.TileTopicListener;
import org.map4j.tinymq.TinyMQ;

/**
 * A class that can render a map image from a tile set and return
 * a copy of the image with a specific coordinate at the image 
 * center ready to be displayed to the end user. This class
 * also contains methods for manipulating the position
 * and zoom level of the rendered map. Other classes may
 * subscribe to changes to the moving map by registering with
 * the topic broker.
 * 
 * @author Joel Kozikowski
 */
public class MapRenderer implements TileTopicListener {

    public static TinyMQ<MapRenderer> broker = new TinyMQ<MapRenderer>();
    
    public interface MapRendererTopicListener extends TinyMQ.ITopicSubscriber<MapRenderer> {};

    /**
     * The topic published whenever there has been a change to the moving map
     */
    public static final String TOPIC_CHANGED = "changed";
    
    private int displayWidth;
    private int displayHeight;
    private int pixelDisplayDiameter;

    private BufferedImage baseImage;
    private MapImage displayImage;
    
    private TileLoaderController tileController;
    private int pixelTileSize;
    private int zoomLevel;
    private Tile[][] tileGrid;
    private int tileGridSize;
    private WCoordinate wCenter;
    private PCoordinate pCenter;
    private TCoordinate tCenter;
    private TCoordinate tileGridUL;
   
    
    public MapRenderer() {
        this(16);
    }
    

    public MapRenderer(int initialZoom) {
        this(initialZoom, null);
    }
    
    
    public MapRenderer(int initialZoom, TileLoaderController tileController) {
        this.pixelTileSize = 256;
        this.zoomLevel = initialZoom;
        if (tileController != null) {
            setTileLoaderController(tileController);
        }
        Tile.broker.subscribe(Tile.TOPIC_LOADED, this);
    }    

    
    public MapRenderer(int displayWidth, int displayHeight, int initialZoom, TileLoaderController tileController, WCoordinate initialLocation) {
        this(initialZoom, tileController);
        wCenter = initialLocation;
        setDisplayDimensions(displayWidth, displayHeight);
    }

    
    /**
     * Sets a new tile controller to use for generating the displayed map.
     */
    public synchronized void setTileLoaderController(TileLoaderController newController) {

        if (this.tileController != null) {
            this.tileController.cancelOutstandingJobs();
        }

        if (this.zoomLevel < newController.getMinZoom()) {
            this.zoomLevel = newController.getMinZoom();
        }

        if (this.zoomLevel > newController.getMaxZoom()) {
            this.zoomLevel = newController.getMaxZoom();
        }
        
        this.tileController = newController;

        clearRenderCache();
    }
    
    
    /**
     * Clears the rendering cache and forces a re-render of the entire image.
     */
    public synchronized void clearRenderCache() {
        pCenter = null;
        tCenter = null;
        tileGridUL = null;
        tileGrid = null;
        
        if (this.displayWidth > 0 && this.displayHeight > 0) {
            this.setDisplayDimensions(this.displayWidth, this.displayHeight);
        }
        
        this.displayImage = null;
    }
    
    
    public synchronized void setDisplayDimensions(int displayWidth, int displayHeight) {

        // Cancel any loader jobs as we are about to re-calculate the entire tile grid...
        tileController.cancelOutstandingJobs();
        
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        
        
        // Figure out the size in pixels of a square image that can be rotated and shifted by
        // one tile yet still fill the entire display.
        pixelDisplayDiameter = 2 * enclosingRadius(displayWidth, displayHeight);
        
        // How many tiles are required to compose that image box?
        tileGridSize = (int)Math.ceil(pixelDisplayDiameter / this.pixelTileSize) + 2;
        
        tileGrid = new Tile[tileGridSize][tileGridSize];
        
        int masterImageSize = tileGridSize * this.pixelTileSize;
        baseImage = new BufferedImage(masterImageSize, masterImageSize, BufferedImage.TYPE_INT_ARGB);
        
        displayImage = null;
        
        if (wCenter != null) {
            recalcLocation(wCenter);
        }
        
        broker.publish(TOPIC_CHANGED, this);
    }
    
    
    /**
     * Sets the "display location" (i.e. the center of the screen) to be the world
     * coordinates specified in displayLocation
     */
    public synchronized void setDisplayLocation(WCoordinate displayLocation) {
        if (this.wCenter == null || !this.wCenter.equals(displayLocation)) {
            recalcLocation(displayLocation);
            broker.publish(TOPIC_CHANGED, this);
        }
    }
     

    /**
     * Returns the current location being displayed at the center of the map.
     * null is returned if it has not been specified yet.
     */
    public synchronized WCoordinate getDisplayLocation() {
        return this.wCenter;
    }
    
        
    /**
     * Sets the zoom level of the display to be the specified zoom level. This
     * will result in the display being redrawn.
     */
    public synchronized void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
        if (this.wCenter != null) {
           recalcLocation(this.wCenter);
           broker.publish(TOPIC_CHANGED, this);
        }
    }
    
    
    /**
     * Returns the current zoom level...
     */
    public int getZoomLevel() {
        return this.zoomLevel;
    }

    
    /**
     * Adjusts the current zoom level by the specified delta
     * @param delta A positive or negative number to add to the current zoom level
     */
    public synchronized void adjustZoom(int delta) {
        int newZoom = this.zoomLevel + delta;
        if (newZoom < this.getMinZoom()) {
            newZoom = this.getMinZoom();
        }
        if (newZoom > this.getMaxZoom()) {
            newZoom = this.getMaxZoom();
        }
        this.setZoomLevel(newZoom);
    }
    
    
    public int getMinZoom() {
        return this.tileController.getMinZoom();
    }
    
    
    public int getMaxZoom() {
        return this.tileController.getMaxZoom();
    }
    
    
    /**
     * Marks the current display image as invalid, then publishes a "changed"
     * notification to any listeners, indicating the current display image needs 
     * be retrieved via call to getDisplayImage()
     */
    public synchronized void refresh() {
        renderGrid();
        this.displayImage = null;
        broker.publish(TOPIC_CHANGED, this);
    }
    
    
    
    /**
     * Respond to messages from the tile message broker about their loading status...
     */
    @Override
    public synchronized void onPublish(String topic, Tile tile) {
        if (topic.equals(Tile.TOPIC_LOADED)) {
            renderTile(tile);
            broker.publish(TOPIC_CHANGED, this);
        }
    }

    
    /**
     * Returns the radius of the smallest circle that will enclose 
     * a square that has a side width of a and a height of b.
     */
    private int enclosingRadius(int a, int b) {
        double r = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)) / 2.0;
        return (int)Math.ceil(r);
    }

    
    private void recalcLocation(WCoordinate displayLocation) {
       // The location has changed...
       this.wCenter = new WCoordinate(displayLocation);
       
       // Convert the location to pixel space...
       pCenter = this.wCenter.asP(this.zoomLevel);
       
       if (this.displayWidth > 0 && this.displayHeight > 0) {
           // What should be the center tile (in XYZ tile space)...
           TCoordinate centerTile = pCenter.asT(true);
           if (tCenter == null || !centerTile.equals(tCenter)) {
               // We have a new center tile!
               tCenter = centerTile;
               recalcTileGrid();
           }
       }
       displayImage = null;
    }
    
    
    private void recalcTileGrid() {
        // We are about to re-calculate the tile grid, so stop any pending load jobs...
        tileController.cancelOutstandingJobs(); 

        // Calculate the upper left of the tile grid (in XYZ tile space)...
        tileGridUL = new TCoordinate(tCenter);
        tileGridUL.adjustCol(-tileGridSize / 2);
        tileGridUL.adjustRow(-tileGridSize / 2);
        
        renderGrid();
        
        displayImage = null;
    }
    
    
    private void renderGrid() {
        TCoordinate tc = new TCoordinate(tileGridUL);
        for (int col = 0; col < tileGridSize; col++) {
           tc.setCol(tileGridUL.getCol());
           for (int row = 0; row < tileGridSize; row++) {
               tileGrid[col][row] = tileController.getTile(tc);
               this.renderTile(tileGrid[col][row]);
               tc.adjustCol(1);
           } // col
           tc.adjustRow(1);
        } // row
    }

    private void renderTile(Tile tile) {
        int colOffset = tile.coord.getCol() - tileGridUL.getCol();
        int rowOffset = tile.coord.getRowAsXYZ() - tileGridUL.getRowAsXYZ();
        
        int x = colOffset * this.pixelTileSize;
        int y = rowOffset * this.pixelTileSize;
        
        Graphics g = baseImage.getGraphics();
        g.clearRect(x, y, this.pixelTileSize, this.pixelTileSize);
        g.drawImage(tile.getImage(), x, y, null);
        g.dispose();
        
        // Base image has changed - mark it dirty...
        displayImage = null;
    }
    
    
    
    /**
     * Retrieves the current display image that will be large
     * enough to rotate around its center, yet still fill the
     * displayWidth and displayHeight
     */
    public synchronized MapImage getDisplayImage() {

        if (displayImage == null) {
            PCoordinate pTileGridUL = tileGridUL.asP();
            
            int subImageCenterX = pCenter.getPixelX() - pTileGridUL.getPixelX();
            int subImageCenterY = pCenter.getPixelY() - pTileGridUL.getPixelY();
            
            int pixelDisplayRadius = pixelDisplayDiameter / 2;
            int displayCornerX = subImageCenterX - pixelDisplayRadius;
            int displayCornerY = subImageCenterY - pixelDisplayRadius;
            
            // Make a new image...
            BufferedImage newImage = new BufferedImage(pixelDisplayDiameter, pixelDisplayDiameter, baseImage.getType());
            Graphics2D target2D = newImage.createGraphics();
            
            target2D.drawImage(baseImage, 0, 0, pixelDisplayDiameter-1, pixelDisplayDiameter-1,
                               displayCornerX, displayCornerY, displayCornerX + pixelDisplayDiameter-1, displayCornerY + pixelDisplayDiameter-1, null);
            
            // Calculate a pixel box to represent the new image...
            PCoordinate pNewUL = new PCoordinate(pCenter);
            pNewUL.adjustX(-pixelDisplayRadius);
            pNewUL.adjustY(-pixelDisplayRadius); 

            PCoordinate pNewLR = new PCoordinate(pNewUL);
            pNewLR.adjustX(pixelDisplayDiameter - 1);
            pNewLR.adjustY(pixelDisplayDiameter - 1);
            
            PBox pImageBox = new PBox(pNewUL, pNewLR);
            
            // If markers are visible and exist, draw them on the map...
            if (this.isMapLayersVisible() && this.getLayerRoot() != null) {
                // Draw visible map markers
                VisibleMapObjectVisitor visitor = new VisibleMapObjectVisitor(this.getLayerRoot()) {
                    @Override
                    public void visit(IMapObject mapObject) {
                        if (mapObject.isContained(pImageBox)) {
                            mapObject.paint(target2D, pImageBox);
                        }
                    }
                };
                visitor.run();
            }
            
            target2D.dispose();
            
            displayImage = new MapImage(pImageBox, newImage, this.displayWidth, this.displayHeight);
        }
        
        return displayImage;
    }
    
    
    private MapLayer layerRoot;
    
    /**
     * Sets an optional set of map objects layers that will be rendered on the map, provided
     * isMapMarkersVisibile() is true.
     * @param root
     */
    public void setLayerRoot(MapLayer root) {
        this.layerRoot = root;
    }
    
    
    /**
     * Returns the root layer group for the map markers that will be drawn on this map.
     */
    public MapLayer getLayerRoot() {
        return this.layerRoot;
    }
    

    
    private boolean layersVisible = true;
        
    public boolean isMapLayersVisible() {
        return layersVisible;
    }

    
    public void setMapLayersVisible(boolean visible) {
        layersVisible = visible;
    }
    
}
