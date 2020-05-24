package org.map4j.layers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Stroke;

import org.map4j.coordinates.PBox;

/**
 * IMapObjects are the most generic form of items that can be drawn on a map.  They are members of 
 * a particular map layer, and they can also be optionally styled.
 */
public interface IMapObject {

    MapLayer getLayer();

    void setLayer(MapLayer layer);

    Style getStyle();

    Style getStyleAssigned();

    Color getColor();

    Color getBackColor();

    Stroke getStroke();

    Font getFont();

    String getName();

    
    /**
     * Returns TRUE if this map object is visible somewhere on the
     * world map. This can be based on the layer that contains it,
     * or based on individual visibility settings.
     */
    boolean isVisible();

    
    /**
     * Returns TRUE if this map object is visible within the specified
     * pImageBox.  FALSE should be returned if this map object does
     * lie inside the image box, but its visibility is turned off.
     * @see #isContained(PBox)
     */
    boolean isVisible(PBox pImageBox);
    
    
    /**
     * Returns TRUE if this map object is contained inside of the
     * specified pImageBox, regardless of the visibility setting.
     */
    boolean isContained(PBox pImageBox);

    
    
    /**
     * Paints the map marker on the map. pImageBox represents the
     * Pixel box that surrounds the specified graphics context.
     *
     * @param g The graphics context to draw the marker on
     * @param pImageBox the imageBox that surrounds the pixels in the specified
     *        graphics context.
     */
    void paint(Graphics g, PBox pImageBox);
    
}
