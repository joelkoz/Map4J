package org.map4j.coordinates;

import java.awt.Point;

/**
 * A PBox represents two points in pixel space that form a rectangle.
 * The rectangle is always normalized so coordinate number one is
 * upper left, and coordinate 2 is lower right (i.e. p1.px < p2.px
 * and p1.py < p2.py).
 * 
 * @author Joel Kozikowski
 */
public class PBox {

    public PCoordinate p1;
    public PCoordinate p2;
    
    public PBox(int px1, int py1, int px2, int py2, int zoom, MapProjections proj) {

        if (px1 > px2) {
            int temp = px1;
            px1 = px2;
            px2 = temp;
        }
        
        if (py1 > py2) {
            int temp = py1;
            py1 = py2;
            py2 = temp;
        }
        
        p1 = new PCoordinate(px1, py1, zoom, proj);
        p2 = new PCoordinate(px2, py2, zoom, proj);
    }

    
    public PBox(PCoordinate arg1, PCoordinate arg2) {
        this(arg1.px, arg1.py, arg2.px, arg2.py, arg1.zoom, arg1.proj);
        assert(arg1.zoom == arg2.zoom);
        assert(arg1.proj.getTileSize() == arg2.proj.getTileSize());
    }
    
    
    public PBox(PBox other) {
        p1 = new PCoordinate(other.p1);
        p2 = new PCoordinate(other.p2);
    }
    
    
    public TBox asTBox() {
        return asTBox(true);
    }
    
    
    public TBox asTBox(boolean useXYZ) {
        TCoordinate t1 = p1.asT(useXYZ);
        TCoordinate t2 = p2.asT(useXYZ);
        return new TBox(t1, t2);
    }
    
    
    public int getWidth() {
        return (p2.px - p1.px) + 1;
    }
    
    
    public int getHeight() {
        return (p2.py - p1.py) + 1;
    }
    

    /**
     * Returns a point within this Pixel Box for the specified
     * coordinate. It will be the offset of wCoord from this
     * box's upper left (p1).
     */
    public Point toPoint(IWorldCoordinate wCoord) {
        PCoordinate pCoord = wCoord.asP(p1.zoom);
        return toPoint(pCoord);
    }
    
    
    
    /**
     * Returns a point within this Pixel Box for the specified
     * coordinate. It will be the offset of pCoord from this
     * box's upper left (p1).
     */
    public Point toPoint(IPixelCoordinate pCoord) {
        return new Point(pCoord.getPixelX() - p1.px, pCoord.getPixelY() - p1.py);
    }
    
    
    /**
     * Returns TRUE if the specified coordinate lies inside this box
     */
    public boolean contains(IPixelCoordinate coord) {
        return (coord.getPixelX() >= p1.px) &&
               (coord.getPixelX() <= p2.px) &&
               (coord.getPixelY() >= p1.py) &&
               (coord.getPixelY() <= p2.py);
    }


    /**
     * Returns TRUE if this pixel box and the specified other pixel box
     * represent the same rectangle in pixel space.
     */
    public boolean equals(PBox other) {
        if (other != null) {
            return p1.equals(other.p1) && p2.equals(other.p2);
        }
        else {
            return false;
        }
    }

    
    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof PBox) {
            return this.equals((PBox)other);
        }
        else {
            return false;
        }
    }


    /**
     * Returns the zoom level of this pixel box.
     */
    public int getZoom() {
        return p1.zoom;
    }


    /**
     * Returns TRUE if this box overlaps anywhere with the specified
     * other box.
     */
    public boolean overlaps(PBox other) {
        if (this.p1.px > other.p2.px ||
            this.p2.px < other.p1.px ||
            this.p1.py > other.p2.py ||
            this.p2.py < other.p1.py) {
          return false;
        }
        else {
          return true;  
        }
    }

    
}
