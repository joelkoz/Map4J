package org.map4j.coordinates;


/**
 * An interface implemented by any object that can be expressed
 * as a point in the Pixel coordinate system.
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
