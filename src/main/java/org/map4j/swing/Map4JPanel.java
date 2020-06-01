package org.map4j.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.map4j.coordinates.WCoordinate;
import org.map4j.layers.MapLayer;
import org.map4j.loaders.TileLoaderController;
import org.map4j.render.DisplayImage;
import org.map4j.render.MapRenderer;
import org.map4j.render.MapRenderer.MapRendererTopicListener;


/**
 * A Swing panel that displays a moving map composed of the map data supplied by
 * the specified tile loader controller
 * 
 * @author Joel Kozikowski
 */
public class Map4JPanel extends JPanel implements MapRendererTopicListener {

    private MapRenderer render;
    private int heading;
    
    
    public Map4JPanel() {
        this(16);
    }
    
    public Map4JPanel(int initialZoom) {
        this(initialZoom, null);
    }
    
    public Map4JPanel(int initialZoom, TileLoaderController tileController) {
        this.render = new MapRenderer(initialZoom, tileController);
        MapRenderer.broker.subscribe(MapRenderer.TOPIC_CHANGED, this);
    }

    
    /**
     * Sets the tile loader controller that supplies map tiles to this map display
     */
    public void setTileLoaderController(TileLoaderController newController) {
        this.render.setTileLoaderController(newController);
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        if (this.getPosition() != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                DisplayImage img = this.render.getDisplayImage();
                img.drawImageHeadsUp(heading, g2d);
            }
            catch (Exception ex) {
               ex.printStackTrace(System.err);
               super.paintComponent(g);
            }
            finally {
               g2d.dispose();
            }
        }
        else {
            super.paintComponent(g);
        }
    }

    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height); 
        this.render.setDisplayDimensions(width, height);
    }
    
    
    /**
     * Returns the current display heading (in degrees 0..359) where 0 is "North up"
     */
    public int getHeading() {
        return heading;
    }

    
    /**
     * Sets the current display heading (in degrees 0..359) where 0 is "North up"
     */
    public void setHeading(int heading) {
        if (this.heading != heading) {
            this.heading = heading;
            repaint();
        }
    }



    /**
     * Returns the coordinates of the center of the displayed map
     */
    public WCoordinate getPosition() {
        return this.render.getDisplayLocation();
    }



    /**
     * Sets the position of the displayed map. The center of the display
     * will be at location
     * @param location
     */
    public void setPosition(WCoordinate location) {
        if (!location.equals(this.render.getDisplayLocation())) {
            this.render.setDisplayLocation(location);
        }
    }

    
    /**
     * Returns the current zoom level of the map.
     */
    public int getZoom() {
        return this.render.getZoomLevel();
    }


    /**
     * Sets the current zoom level to the specified level
     */
    public void setZoom(int zoom) {
        this.render.setZoomLevel(zoom);
    }
    
    
    /**
     * Changes the zoom level by the specified amount. The actual
     * zoom level will be kept to the available levels.
     * @param delta
     */
    public void adjustZoom(int delta) {
        this.render.adjustZoom(delta);
    }

    
    
    /**
     * Marks the current display image as invalid and causes the map panel
     * to be redrawn and repainted. If reloadTiles is TRUE, the
     * tile grid will also be invalidated and reloaded from the tile controller.
     * Whether or not this causes a full reload depends on the state of the 
     * tile controllers cache.
     */
    public void refresh(boolean reloadTiles) {
        this.render.refresh(reloadTiles);
    }
    
   
    public void clearRenderCache() {
        this.render.clearRenderCache();
    }

    
    @Override
    public void onPublish(String topic, MapRenderer payload) {
        repaint();
    }


    /**
     * Returns TRUE if map layers are visible and will be rendered
     * on the map.
     */
    public boolean isMapLayersVisible() {
        return this.render.isMapLayersVisible();
    }


    /**
     * Sets the visibility of the map layer set. If visible is FALSE
     * non of the markers will be rendered.
     * @param visible
     */
    public void setMapLayersVisible(boolean visible) {
        this.render.setMapLayersVisible(visible);
    }
    
    
    /**
     * Sets an optional collection of map objects that will be rendered on the map, provided
     * isMapLayersVisibile() is true.
     * @param root
     */
    public void setLayerRoot(MapLayer root) {
        render.setLayerRoot(root);
    }
    
    
    /**
     * Returns the root layer for the map markers that will be drawn on this map.
     */
    public MapLayer getLayerRoot() {
        return render.getLayerRoot();
    }
    
    
    /**
     * If set to TRUE, the drawing routine will wait until the image has completed its loading
     * Painting the background. This makes for smoother painting (i.e. no image blink)
     * when used with offline tile sources which have little delay for retrieving tiles. The
     * default value is FALSE. 
     * @param waitForCompleteImage
     */
    public void setWaitForCompleteImage(boolean waitForCompleteImage) {
        render.setWaitForCompleteImage(waitForCompleteImage);
    }

    
    public boolean isWaitForCompleteImage() {
        return render.isWaitForCompleteImage();
    }    
    
}
