// License: GPL. For details, see Readme.txt file.
package org.map4j.layers;

import java.awt.Color;

import org.map4j.coordinates.WCoordinate;


/**
 * A simple implementation of the {@link IMapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 *
 * @author Jan Peter Stotz
 * @Joel Kozikowski
 */
public class MapMarkerDot extends MapMarkerCircle {

    public static final int DOT_DRAW_RADIUS = 5;

    public MapMarkerDot(WCoordinate coord) {
        this(null, null, coord);
    }

    public MapMarkerDot(String name, WCoordinate coord) {
        this(null, name, coord);
    }

    public MapMarkerDot(MapLayer layer, WCoordinate coord) {
        this(layer, null, coord);
    }

    public MapMarkerDot(MapLayer layer, String name, WCoordinate coord) {
        this(layer, name, coord, getDefaultStyle());
    }

    public MapMarkerDot(Color color, double lat, double lon) {
        this(null, null, lat, lon);
        setColor(color);
    }

    public MapMarkerDot(double lat, double lon) {
        this(null, null, lat, lon);
    }

    public MapMarkerDot(String name, double lat, double lon) {
        this(null, name, new WCoordinate(lat, lon));
    }

    public MapMarkerDot(MapLayer layer, String name, double lat, double lon) {
        this(layer, name, new WCoordinate(lat, lon), getDefaultStyle());
    }

    public MapMarkerDot(MapLayer layer, String name, WCoordinate coord, Style style) {
        super(layer, name, coord, DOT_DRAW_RADIUS, STYLE.FIXED, style);
    }

    public static Style getDefaultStyle() {
        return new Style(Color.BLACK, Color.YELLOW, null, getDefaultFont());
    }
}
