package org.map4j.layers;


import org.map4j.coordinates.IWorldCoordinate;


/**
 * IMapMarker is an item draw on a map that is positioned at a single point in
 * space.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public interface IMapMarker extends IMapObject, IWorldCoordinate {

    enum STYLE {
        FIXED,
        VARIABLE
    }

    /**
     * @return Latitude and Longitude of the map marker position
     */
    IWorldCoordinate getCoordinate();

    
    /**
     * @return Radius, in pixels, required to draw the marker on the map.
     */
    int getDrawRadius();

    /**
     * @return Style of the map marker
     */
    STYLE getMarkerStyle();
}
