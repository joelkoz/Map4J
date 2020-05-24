package org.map4j.layers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import org.map4j.coordinates.IWorldCoordinate;
import org.map4j.coordinates.PBox;
import org.map4j.coordinates.PCoordinate;
import org.map4j.coordinates.WCoordinate;


/**
 * A simple implementation of the {@link IMapMarker} interface. Each map marker
 * is painted as a circle with a black border line and filled with a specified
 * color.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public class MapMarkerCircle extends AbstractMapObject implements IMapMarker {

    protected IWorldCoordinate coord;
    protected int drawRadius;
    protected STYLE markerStyle;

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param coord Coordinates of the map marker
     * @param drawRadius Radius of the map marker position
     */
    public MapMarkerCircle(IWorldCoordinate coord, int drawRadius) {
        this(null, null, coord, drawRadius);
    }

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param name Name of the map marker
     * @param coord Coordinates of the map marker
     * @param drawRadius Radius of the map marker position
     */
    public MapMarkerCircle(String name, IWorldCoordinate coord, int drawRadius) {
        this(null, name, coord, drawRadius);
    }

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param layer Layer of the map marker
     * @param coord Coordinates of the map marker
     * @param drawRadius Radius of the map marker position
     */
    public MapMarkerCircle(MapLayer layer, IWorldCoordinate coord, int drawRadius) {
        this(layer, null, coord, drawRadius);
    }

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param lat Latitude of the map marker
     * @param lon Longitude of the map marker
     * @param drawRadius Radius of the map marker position
     */
    public MapMarkerCircle(String name, double lat, double lon, int drawRadius) {
        this(null, name, new WCoordinate(lat, lon), drawRadius);
    }

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param layer Layer of the map marker
     * @param lat Latitude of the map marker
     * @param lon Longitude of the map marker
     * @param drawRadius Radius of the map marker position
     */
    public MapMarkerCircle(MapLayer layer, double lat, double lon, int drawRadius) {
        this(layer, null, new WCoordinate(lat, lon), drawRadius);
    }

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param layer Layer of the map marker
     * @param name Name of the map marker
     * @param coord Coordinates of the map marker
     * @param drawRadius Radius of the map marker position
     */
    public MapMarkerCircle(MapLayer layer, String name, IWorldCoordinate coord, int drawRadius) {
        this(layer, name, coord, drawRadius, STYLE.VARIABLE, getDefaultStyle());
    }

    /**
     * Constructs a new {@code MapMarkerCircle}.
     * @param layer Layer of the map marker
     * @param name Name of the map marker
     * @param coord Coordinates of the map marker
     * @param drawRadius Radius of the map marker position
     * @param markerStyle Marker style (fixed or variable)
     * @param style Graphical style
     */
    public MapMarkerCircle(MapLayer layer, String name, IWorldCoordinate coord, int drawRadius, STYLE markerStyle, Style style) {
        super(layer, name, style);
        this.markerStyle = markerStyle;
        this.coord = coord;
        this.drawRadius = drawRadius;
    }

    @Override
    public IWorldCoordinate getCoordinate() {
        return coord;
    }

    @Override
    public double getLat() {
        return coord.getLat();
    }

    @Override
    public double getLon() {
        return coord.getLon();
    }

    @Override
    public int getDrawRadius() {
        return drawRadius;
    }

    @Override
    public STYLE getMarkerStyle() {
        return markerStyle;
    }

    
    protected void paint(Graphics g, Point position, int drawRadius) {
        int sizeH = drawRadius;
        int size = sizeH * 2;

        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(getColor());
        g.drawOval(position.x - sizeH, position.y - sizeH, size, size);

        if (getLayer() == null || getLayer().isVisibleTexts()) paintName(g, position);
    }

    
    public static Style getDefaultStyle() {
        return new Style(Color.ORANGE, new Color(200, 200, 200, 200), null, getDefaultFont());
    }

    
    @Override
    public String toString() {
        return "MapMarker at " + getLat() + ' ' + getLon();
    }

    
    @Override
    public void setLat(double lat) {
        pCache = null;
        if (coord == null) coord = new WCoordinate(lat, 0);
        else coord.setLat(lat);
    }

    @Override
    public void setLon(double lon) {
        pCache = null;
        if (coord == null) coord = new WCoordinate(0, lon);
        else coord.setLon(lon);
    }

    
    private PCoordinate pCache = null;
    
    @Override
    public PCoordinate asP(int zoom) {
        if (pCache == null || pCache.getZoom() != zoom) {
            pCache = coord.asP(zoom);
        }
        return pCache;
    }

    
    @Override
    public void paint(Graphics g, PBox pImageBox) {
        this.paint(g, pImageBox.toPoint(this), this.getDrawRadius());
    }

    
    @Override
    public boolean isContained(PBox pImageBox) {
        return pImageBox.contains(this.asP(pImageBox.getZoom()));
    }
    
}
