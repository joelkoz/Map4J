package org.map4j.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.map4j.coordinates.PBox;
import org.map4j.coordinates.PCoordinate;
import org.map4j.coordinates.TCoordinate;
import org.map4j.coordinates.WCoordinate;
import org.map4j.layers.IMapObject;
import org.map4j.layers.MapLayer;
import org.map4j.layers.VisibleMapObjectVisitor;
import org.map4j.loaders.TileLoaderController;
import org.map4j.render.TileGridImage.TileGridImageTopicListener;
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
public class MapRenderer implements TileGridImageTopicListener {

    public static TinyMQ<MapRenderer> broker = new TinyMQ<MapRenderer>();
    
    public interface MapRendererTopicListener extends TinyMQ.ITopicSubscriber<MapRenderer> {};

    /**
     * The topic published whenever there has been a change to the moving map
     */
    public static final String TOPIC_CHANGED = "changed";
    
    private int displayWidth;
    private int displayHeight;
    private int pixelDisplayDiameter;
    
    private TileGridImage tileGridImage;
    private DisplayImage displayImage;
    
    private TileLoaderController tileController;
    private int zoomLevel;
    private int tileGridSize;    
    private WCoordinate wCenter;
    private PCoordinate pCenter;
    private boolean waitForCompleteImage;
    
    public MapRenderer() {
        this(16);
    }
    

    public MapRenderer(int initialZoom) {
        this(initialZoom, null);
    }
    
    
    public MapRenderer(int initialZoom, TileLoaderController tileController) {
        this.zoomLevel = initialZoom;
        if (tileController != null) {
            setTileLoaderController(tileController);
        }
        TileGridImage.broker.subscribe(TileGridImage.TOPIC_UPDATED, this);
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

        if (this.displayWidth > 0 && this.displayHeight > 0) {
            this.setDisplayDimensions(this.displayWidth, this.displayHeight);
        }

    }
    
    
    /**
     * Clears the rendering cache and forces a re-render of the entire image.
     */
    public synchronized void clearRenderCache() {
        pCenter = null;
        this.discardTileGrid();
        
        this.displayImage = null;
    }
    
    
    private void discardTileGrid() {
        if (tileGridImage != null) {
            tileGridImage.stop();
        }
        tileGridImage = null;
    }
    
    
    /**
     * Sets the dimensions of the display so the renderer knows how large of an image
     * to render when getDisplayImage() is called.
     * @param displayWidth The width, in pixels, of the image needed to fill the display
     * @param displayHeight The height, in pixels, of the image needed ot fill the display
     */
    public void setDisplayDimensions(int displayWidth, int displayHeight) {

        synchronized(this) {
            this.discardTileGrid();
            
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
    
            clearRenderCache();
            
            // Figure out the size in pixels of a square image that can be rotated and shifted by
            // one tile yet still fill the entire display.
            pixelDisplayDiameter = 2 * enclosingRadius(displayWidth, displayHeight);
            
            // How many tiles are required to compose that image box?
            tileGridSize = (int)Math.ceil(pixelDisplayDiameter / TileGridImage.pixelTileSize) + 2;
            
            displayImage = null;
            
            if (wCenter != null) {
                recalcLocation(wCenter);
            }
        }
        
        broker.publish(TOPIC_CHANGED, this);
    }
    
    
    /**
     * Sets the "display location" (i.e. the center of the screen) to be the world
     * coordinates specified in displayLocation
     */
    public void setDisplayLocation(WCoordinate displayLocation) {
        boolean notify = false;
        synchronized(this) {
            if (this.wCenter == null || !this.wCenter.equals(displayLocation)) {
                recalcLocation(displayLocation);
                notify = true;
            }
        }
        if (notify) {
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
    public void setZoomLevel(int zoomLevel) {
        boolean notify = false;
        synchronized(this) {
            this.zoomLevel = zoomLevel;
            if (this.wCenter != null) {
               recalcLocation(this.wCenter);
               notify = true;
            }
        }

        if (notify) {
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
    public void refresh() {
        synchronized(this) {
            if (tileGridImage != null) {
                tileGridImage.refresh();
            }
            this.displayImage = null;
        }
        broker.publish(TOPIC_CHANGED, this);
    }

    
    /**
     * Respond to messages from the tile grid image about its loading status...
     */    
    @Override
    public void onPublish(String topic, TileGridImage payload) {
        synchronized (this) {
            this.displayImage = null;
        }
        broker.publish(TOPIC_CHANGED, this);
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
           TCoordinate tCenter = pCenter.asT(true);
           if (tileGridImage == null || !tileGridImage.getTCenter().equals(tCenter)) {
               // We have a new center tile!
               this.discardTileGrid();
               tileGridImage = new TileGridImage(tileController, pCenter, tileGridSize);
           }
       }
       displayImage = null;
    }
    
    
    /**
     * Retrieves the current display image that will be large
     * enough to rotate around its center, yet still fill the
     * displayWidth and displayHeight
     */
    public synchronized DisplayImage getDisplayImage() {

        if (displayImage == null) {

            if (this.waitForCompleteImage) {
                while(!tileGridImage.isLoadCompleted()) {
                    try { Thread.sleep(400); } catch (InterruptedException ex) {}
                } // while
            }
            
            
            PCoordinate pTileGridUL = tileGridImage.getTileBox().t1.asP();
            
            int subImageCenterX = pCenter.getPixelX() - pTileGridUL.getPixelX();
            int subImageCenterY = pCenter.getPixelY() - pTileGridUL.getPixelY();
            
            int pixelDisplayRadius = pixelDisplayDiameter / 2;
            int displayCornerX = subImageCenterX - pixelDisplayRadius;
            int displayCornerY = subImageCenterY - pixelDisplayRadius;
            
            // Make a new image...
            BufferedImage newImage = new BufferedImage(pixelDisplayDiameter, pixelDisplayDiameter, tileGridImage.getImage().getType());
            Graphics2D target2D = newImage.createGraphics();
            
            target2D.drawImage(tileGridImage.getImage(), 0, 0, pixelDisplayDiameter-1, pixelDisplayDiameter-1,
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
            
            displayImage = new DisplayImage(pImageBox, newImage, this.displayWidth, this.displayHeight);
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
        displayImage = null;
        broker.publish(TOPIC_CHANGED, this);
    }


    /**
     * If set to TRUE, the drawing routine will wait until the image has completed its loading
     * before getDisplayImage() returns. This makes for smoother painting (i.e. no image blink)
     * when used with offline tile sources which have little delay for retrieving tiles.  The
     * default value is FALSE. 
     * @param waitForCompleteImage
     */
    public void setWaitForCompleteImage(boolean waitForCompleteImage) {
        this.waitForCompleteImage = waitForCompleteImage;
    }

    
    public boolean isWaitForCompleteImage() {
        return waitForCompleteImage;
    }

    
}
