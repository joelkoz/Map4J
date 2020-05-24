package org.map4j.layers;

import org.map4j.coordinates.WCoordinate;

/**
 * IMapRectangle is any map object that occupies a rectangular area of space on the
 * map.
 *
 * @author Stefan Zeller
 * @author Joel Kozikowski
 */
public interface IMapRectangle extends IMapObject {

    /**
     * @return Latitude/Longitude of top left of rectangle
     */
    WCoordinate getTopLeft();

    /**
     * @return Latitude/Longitude of bottom right of rectangle
     */
    WCoordinate getBottomRight();

}
