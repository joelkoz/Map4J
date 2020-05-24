package org.map4j.coordinates;

/**
 * A class that represents a coordinate in Tile Space.
 * <p>Similar to pixel space, Tile Space represents the actual tile column and tile
 * row number.  The "origin" of Tile Space is (0,0). where the column zero is the left most tile column.
 * For the ROW, however, there are two standards: 
 * <ol><li>"XYZ", used by Google, Bing, OpenStreetMap, etc. places
 * row zero at the TOP of the map with increasing numbers going downward.</li>
 * <li>"TMS" places the zero row at the BOTTOM of the map with increasing numbers going upward.  TMS is used
 * in the popular storage file format MBTiles.</li>
 * </ol>
 * @author Joel Kozikowski
 */
public class TCoordinate implements ITileCoordinate {

    protected int col;
    protected int row;
    protected byte zoom;
    protected boolean xyz;
    protected MapProjections proj;
    

    /**
     * Creates a tile coordinate for the specified zoom level. A tile size
     * of 256 is assumed, as well as an XYZ tile coordinate system.
     */
    public TCoordinate(int col, int row, int zoom) {
        this(col, row, zoom, true, MapProjections.Merc256);
    }

    
    
    /**
     * @param useXYZ True if an XYZ coordinate system should be used.  False if this is a TMS system.
     * @param proj The MapProjections object to use for conversions.
     */
    public TCoordinate(int col, int row, int zoom, boolean useXYZ, MapProjections proj) {
        
        int max = 1 << zoom;
        assert(col >= 0 && col < max && row >= 0 && row < max);
        
        this.col = col;
        this.row = row;
        this.zoom = (byte)zoom;
        this.xyz = useXYZ;
        this.proj = proj;
    }


    public TCoordinate(TCoordinate other) {
        this.col = other.col;
        this.row = other.row;
        this.zoom = other.zoom;
        this.xyz = other.xyz;
        this.proj = other.proj;
    }

    
    /**
     * Constructs a new TCoordinate for the specified column and
     * row, taking all the other settings from the "other" coordinate.
     */
    public TCoordinate(int col, int row, TCoordinate other) {
        this.col = col;
        this.row = row;
        this.zoom = other.zoom;
        this.xyz = other.xyz;
        this.proj = other.proj;
    }
    
    
    
    /**
     * Returns the zoom level that this tile coordinate is part of
     */
    public int getZoom() {
        return this.zoom;
    }
    
    /**
     * Returns the tile size used by the coordinate system this
     * tile is in.
     */
    public int getTileSize() {
        return proj.getTileSize();
    }

    
    /**
     * Returns the row in XYZ coordinate space (regardless of what space it is actually in
     */
    public int getRowAsXYZ() {
        if (this.xyz) {
            return row;
        }
        else {
            return flipY(row, zoom);
        }
    }


    /**
     * Returns the row in TMS coordinate space (regardless of what space it is actually in
     */
    public int getRowAsTMS() {
        if (!this.xyz) {
            return row;
        }
        else {
            return flipY(row, zoom);
        }
    }


    /**
     * Returns the row in the coordinate space specified by the parameter. This 
     * allows the row to be retrieved in a data driven manner.
     * @param useXYZ if TRUE, the row will be returned in XYZ space. If False,
     *   it will be returned in TMS space.
     */
    public int getRow(boolean useXYZ) {
        if (this.xyz == useXYZ) {
            return row;
        }
        else {
            return flipY(row, zoom);
        }
    }

    
    
    /**
     * Flips the specified Y coordinate from its current coordinate system to the
     * other system.  Thus, if its XYZ, it will be returned as TMS and vice versa.
     */
    public static int flipY(int oldY, int zoom) {
        return (1 << zoom) - oldY - 1;
    }
    
   
    /**
     * Returns the coordinate as a request path like "z/x/y", where (x,y)
     * is in XYZ coordinate space (regardless of what space the coordinate
     * is actually in.
     */
    public String getXYZRequestPath() {
        return zoom + "/" + col + "/" + getRowAsXYZ();
    }

    
    /**
     * Returns the coordinate as a request path like "z/x/y", where (x,y)
     * is in TMS coordinate space (regardless of what space the coordinate
     * is actually in.
     */
    public String getTMSRequestPath() {
        return zoom + "/" + col + "/" + getRowAsTMS();
    }


    
    /**
     * Returns the coordinate as a request path like "z/x/y", where (x,y)
     * is in the coordinate space specified by the useXYZ parameter
     * (regardless of what space the coordinate is actually in).
     * @param useXYZ if TRUE, the row will be returned in XYZ space. If False,
     *   it will be returned in TMS space.
     */
    public String getRequestPath(boolean useXYZ) {
        return zoom + "/" + col + "/" + getRow(useXYZ);
    }

    
    /**
     * Returns the coordinate as a request path like "z/x/y", where (x,y)
     * is in the native coordinate space of this tile.
     */
    public String getRequestPath() {
        return zoom + "/" + col + "/" + getRow(this.xyz);
    }
    
    
    
    /**
     * Creates a new TCoordinate with the specified col and row in the same coordinate space
     * as this object.
     */
    public TCoordinate getT(int col, int row) {
         TCoordinate newT = new TCoordinate(col, row, this.zoom, this.xyz, this.proj);
         return newT;
    }
    
    
    /**
     * Returns the pixel coordinate of the upper left hand corner of this tile
     * (i.e. the pixel coordinate of pixel at (0,0) on the tile image
     */
    public PCoordinate asP() {
        int px = col * proj.getTileSize();
        int py = getRowAsXYZ() * proj.getTileSize();
        return new PCoordinate(px, py, zoom, proj);
    }

    
    /**
     * Returns the pixel coordinate of the pixel that is at the center
     * of this tile.
     */
    public PCoordinate centerAsP() {
        int offset = proj.getTileSize() / 2;
        int px = col * proj.getTileSize() + offset;
        int py = getRowAsXYZ() * proj.getTileSize() + offset;
        return new PCoordinate(px, py, zoom, proj);
    }
    
    
    public boolean equals(TCoordinate other) {
        return (other != null) &&
               (this.col == other.col) &&
               (this.row == other.row) &&
               (this.zoom == other.zoom) &&
               (this.xyz == other.xyz) &&
               (this.proj.equals(other.proj));
    }


    @Override
    public boolean equals(Object other) {
        return (other != null) &&
               (other instanceof TCoordinate) &&
               this.equals((TCoordinate)other);
    }
    

    @Override
    public int getCol() {
        return this.col;
    }


    @Override
    public void setCol(int newCol) {
       this.col = newCol;
    }

    
    public void adjustCol(int delta) {
        this.col += delta;
    }
    
    @Override
    public int getRow() {
        return this.row;
    }


    @Override
    public void setRow(int newRow) {
        this.row = newRow;
    }

    public void adjustRow(int delta) {
        this.row += delta;
    }
    
}
