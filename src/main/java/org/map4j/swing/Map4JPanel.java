package org.map4j.swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

import org.map4j.coordinates.WCoordinate;
import org.map4j.layers.MapLayer;
import org.map4j.loaders.TileLoaderController;
import org.map4j.render.MapImage;
import org.map4j.render.MapRenderer;
import org.map4j.render.MapRenderer.MapRendererTopicListener;


/**
 * A Swing panel that displays a moving map composed of the map data supplied by
 * the specified tile loader controller
 * 
 * @author Joel Kozikowski
 */
public class Map4JPanel extends JPanel implements ComponentListener, MapRendererTopicListener {

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
        this.addComponentListener(this);
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
            MapImage img = this.render.getDisplayImage();
            img.drawImageHeadsUp(heading, g2d);
            g2d.dispose();
        }
        else {
            super.paintComponent(g);
        }
    }

    
    @Override
    public void componentResized(ComponentEvent e) {
        this.render.setDisplayDimensions(this.getWidth(), this.getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
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
        this.heading = heading;
        repaint();
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
        this.render.setDisplayLocation(location);;
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

    
    
    public void refresh() {
        this.render.refresh();
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
    
    
}
