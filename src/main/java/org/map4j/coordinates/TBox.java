package org.map4j.coordinates;

import java.awt.Point;

/**
 * A TBox represents two points in tile space that form a rectangle.
 * The rectangle is always normalized so coordinate number one is
 * upper left, and coordinate 2 is lower right (i.e. col1 < col2
 * and row1 < row2).
 * 
 * @author Joel Kozikowski
 */
public class TBox {

    public TCoordinate t1;
    public TCoordinate t2;
    
    public TBox(int col1, int row1, int col2, int row2, int zoom, boolean useXYZ, MapProjections proj) {
        if (col1 > col2) {
            int temp = col1;
            col1 = col2;
            col2 = temp;
        }
        
        if (row1 > row2) {
            int temp = row1;
            row1 = row2;
            row2 = temp;
        }
        
        t1 = new TCoordinate(col1, row1, zoom, useXYZ, proj);
        t2 = new TCoordinate(col2, row2, zoom, useXYZ, proj);
    }

    
    public TBox(TCoordinate arg1, TCoordinate arg2) {
        this(arg1.col, arg1.row, arg2.col, arg2.row, arg1.zoom, arg1.xyz, arg1.proj);
        assert(arg1.zoom == arg2.zoom);
        assert(arg1.xyz == arg2.xyz);
        assert(arg1.proj.getTileSize() == arg2.proj.getTileSize());
    }
    
    
    
    public TBox(TBox other) {
        this.t1 = new TCoordinate(other.t1);
        this.t2 = new TCoordinate(other.t2);
    }
    
    
    public int getWidth() {
        return (t2.col - t1.col) + 1;
    }
    
    
    public int getHeight() {
        return (t2.row - t1.row) + 1;
    }
    
    
    
    /**
     * Returns a point within this Tile Box for the specified
     * coordinate. It will be the offset of pCoord from this
     * box's upper left (p1).  if pCoord is outside of this
     * pixel box, NULL is returned.
     */
    public Point toPoint(ITileCoordinate tCoord) {
        if (this.contains(tCoord)) {
            return new Point(tCoord.getCol() - t1.col, tCoord.getRow(true) - t1.getRow(true));
        }
        else {
            return null;
        }
    }
    
    
    
    /**
     * Returns TRUE if the specified coordinate lies inside this box
     */
    public boolean contains(ITileCoordinate coord) {
        return (coord.getCol() >= t1.col) &&
               (coord.getCol() <= t2.col) &&
               (coord.getRow(t1.xyz) >= t1.row) &&
               (coord.getRow(t1.xyz) <= t2.row);
    }
   
    
    /**
     * Returns the zoom level of this pixel box.
     */
    public int getZoom() {
        return t1.zoom;
    }
    
}
