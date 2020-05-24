package org.map4j.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import org.map4j.coordinates.PBox;
import org.map4j.coordinates.PCoordinate;
import org.map4j.coordinates.WCoordinate;

public class MapRectangle extends AbstractMapObject implements IMapRectangle {

    private WCoordinate topLeft;
    private WCoordinate bottomRight;

    public MapRectangle(WCoordinate topLeft, WCoordinate bottomRight) {
        this(null, null, topLeft, bottomRight);
    }

    public MapRectangle(String name, WCoordinate topLeft, WCoordinate bottomRight) {
        this(null, name, topLeft, bottomRight);
    }

    public MapRectangle(MapLayer layer, WCoordinate topLeft, WCoordinate bottomRight) {
        this(layer, null, topLeft, bottomRight);
    }

    public MapRectangle(MapLayer layer, String name, WCoordinate topLeft, WCoordinate bottomRight) {
        this(layer, name, topLeft, bottomRight, getDefaultStyle());
    }

    public MapRectangle(MapLayer layer, String name, WCoordinate topLeft, WCoordinate bottomRight, Style style) {
        super(layer, name, style);
        this.topLeft = new WCoordinate(topLeft);
        this.bottomRight = new WCoordinate(bottomRight);
    }

    
    @Override
    public WCoordinate getTopLeft() {
        return topLeft;
    }

    @Override
    public WCoordinate getBottomRight() {
        return bottomRight;
    }

    
    protected void paint(Graphics g, Point topLeft, Point bottomRight) {
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
        g.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
        // Restore graphics
        g.setColor(oldColor);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(oldStroke);
        }
        int width = bottomRight.x-topLeft.x;
        int height = bottomRight.y-topLeft.y;
        Point p = new Point(topLeft.x+(width/2), topLeft.y+(height/2));
        if (getLayer() == null || getLayer().isVisibleTexts()) paintName(g, p);
    }

    
    public static Style getDefaultStyle() {
        return new Style(Color.BLUE, null, new BasicStroke(2), getDefaultFont());
    }

    @Override
    public String toString() {
        return "MapRectangle from " + getTopLeft() + " to " + getBottomRight();
    }

    @Override
    public void paint(Graphics g, PBox pImageBox) {
        Point topLeft = pImageBox.toPoint(this.getTopLeft());
        Point bottomRight = pImageBox.toPoint(this.getBottomRight());
        this.paint(g,  topLeft, bottomRight);
    }


    private PBox pBox = null;
    
    @Override
    public boolean isContained(PBox pImageBox) {
        if (pBox == null || pBox.getZoom() != pImageBox.getZoom()) {
            PCoordinate pTopLeft = this.topLeft.asP(pImageBox.getZoom());
            PCoordinate pBottomRight = this.bottomRight.asP(pImageBox.getZoom());
            pBox = new PBox(pTopLeft, pBottomRight);
        }
        
        return pImageBox.overlaps(pBox);
    }

    
    
}
