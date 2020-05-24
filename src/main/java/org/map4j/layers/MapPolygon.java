package org.map4j.layers;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.List;

import org.map4j.coordinates.IWorldCoordinate;
import org.map4j.coordinates.PBox;


public class MapPolygon extends AbstractMapObject implements IMapPolygon {

    protected List<? extends IWorldCoordinate> points;

    public MapPolygon(IWorldCoordinate... points) {
        this(null, null, points);
    }

    public MapPolygon(List<? extends IWorldCoordinate> points) {
        this(null, null, points);
    }

    public MapPolygon(String name, List<? extends IWorldCoordinate> points) {
        this(null, name, points);
    }

    public MapPolygon(String name, IWorldCoordinate... points) {
        this(null, name, points);
    }

    public MapPolygon(MapLayer layer, List<? extends IWorldCoordinate> points) {
        this(layer, null, points);
    }

    public MapPolygon(MapLayer layer, String name, List<? extends IWorldCoordinate> points) {
        this(layer, name, points, getDefaultStyle());
    }

    public MapPolygon(MapLayer layer, String name, IWorldCoordinate... points) {
        this(layer, name, Arrays.asList(points), getDefaultStyle());
    }

    public MapPolygon(MapLayer layer, String name, List<? extends IWorldCoordinate> points, Style style) {
        super(layer, name, style);
        this.points = points;
    }

    @Override
    public List<? extends IWorldCoordinate> getPoints() {
        return this.points;
    }


    protected Polygon polyCache = null;
    protected PBox polyCacheBox = null;
    
    /**
     * Returns the all of the coordinate points of
     * this polygon translated as a Polygon
     * of Point objects relative to pBox
     */
    protected Polygon asPolygon(PBox pBox) {

        if (polyCacheBox == null || !polyCacheBox.equals(pBox)) {
            polyCache = new Polygon();
            for (IWorldCoordinate wc : this.getPoints()) {
                Point pt = pBox.toPoint(wc);
                polyCache.addPoint(pt.x, pt.y);
            } // for
            polyCacheBox = new PBox(pBox);
        }
        
        return polyCache;
    }
    
    
    @Override
    public void paint(Graphics g, PBox pBox) {
        
        Polygon polygon = this.asPolygon(pBox);
        
        // Prepare graphics
        Color oldColor = g.getColor();
        g.setColor(getColor());

        Stroke oldStroke = null;
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            oldStroke = g2.getStroke();
            g2.setStroke(getStroke());
        }
        // Draw
        g.drawPolygon(polygon);
        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g2.fillPolygon(polygon);
            g2.setComposite(oldComposite);
        }
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        Rectangle rec = polygon.getBounds();
        Point corner = rec.getLocation();
        Point p = new Point(corner.x+(rec.width/2), corner.y+(rec.height/2));
        if (getLayer() == null || getLayer().isVisibleTexts()) paintName(g, p);
    }

    
    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, new Color(100, 100, 100, 50), new BasicStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapPolygon [points=" + points + ']';
    }

    
    private int containedCacheVal = -1;
    
    @Override
    public boolean isContained(PBox pImageBox) {
        if (containedCacheVal < 0 || polyCacheBox == null ||
            !polyCacheBox.equals(pImageBox)) {
            Polygon poly = this.asPolygon(pImageBox);
            Rectangle rect = new Rectangle(0, 0, pImageBox.getWidth(), pImageBox.getHeight());
            if (poly.intersects(rect)) {
                containedCacheVal = 1;
            }
            else {
                containedCacheVal = 0;
            }
        }
        return (containedCacheVal == 1);
    }
    
}
