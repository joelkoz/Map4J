package org.map4j.coordinates;


/**
 * An interface implemented by any object that can be expressed
 * as a point in the Pixel coordinate system.
 * <p>Map Tiling divides a Web Mercator projected map up into "tiles" that
 * are exactly square (usually 256 x 256). The number of tiles needed to cover the world map depends
 * on the "Zoom" level.  A zoom level divides the map up with an equal number of horizontal and
 * vertical tiles, with that number being represented with the formula 2 ^ Zoom. Thus, a Zoom level 
 * of "zero" has a single 256 x 256 tile that represents the entire earth. A zoom level of "one" 
 * is a "2 x 2" matrix of tiles.  Level two is 4 x 4, etc. Assuming a 256 x 256 tile is used, a
 * level two zoom would be able to display the entire world map as a 4 x 256 = 1024 x 1024 pixel
 * image. This entire grid of pixels taken as a single image represents "pixel space". Pixel 
 * space is usually written as (x, y, z), which represents the (x,y) coordinate of a position 
 * in pixel space at a zoom level of z.
 *  
 * @author Joel Kozikowski
 */
public interface IPixelCoordinate {

    /**
     * Returns the X value of this coordinate in pixel space.
     */
    public int getPixelX();
    
    
    /**
     * Sets the X value of this coordinate in pixel space to newX.
     */
    public void setPixelX(int newX);
    
    
    /**
     * Returns the Y value of this coordinate in pixel space.
     */
    public int getPixelY();
    
    
    /**
     * Sets the Y value of this coordinate in pixel space to newY.
     */
    public void setPixelY(int newY);
 
    
    /**
     * Returns the zoom level of the pixel space this coordinate lies within
     */
    public int getZoom();
}
