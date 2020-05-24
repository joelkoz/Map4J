// License: GPL. For details, see Readme.txt file.
package org.map4j.layers;

import java.util.List;

import org.map4j.coordinates.IWorldCoordinate;

/**
 * IMapPolygon is a map object that is defined by a set of points on the map. They
 * represent a closed region on the map defined by three or more points. The first
 * and final points in the polygon are joined by a line segment that closes the polygon.
 *
 * @author Vincent Privat
 * @author Joel Kozikowski
 */
public interface IMapPolygon extends IMapObject {

    /**
     * @return Latitude/Longitude of each point of polygon
     */
    List<? extends IWorldCoordinate> getPoints();

}
