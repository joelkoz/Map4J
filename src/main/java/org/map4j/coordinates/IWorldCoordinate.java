package org.map4j.coordinates;


/**
 * An interface implemented by any object that can be expressed
 * as a point in the World coordinate system.
 * @author Joel Kozikowski
 */
public interface IWorldCoordinate {

    /**
     * Returns the Latitude value of this coordinate in world space.
     */
    public double getLat();
    
    
    /**
     * Sets the Latitude value of this coordinate in world space to newLat.
     */
    public void setLat(double newLat);
    
    
    /**
     * Returns the Longitude value of this coordinate in world space.
     */
    public double getLon();
    
    
    /**
     * Sets the Longitude value of this coordinate in world space to newLon.
     */
    public void setLon(double newLon);

    
    /**
     * Return this world coordinate as a Pixel Coordinate in the specified
     * zoom level. 
     */
    public PCoordinate asP(int zoom);
}
