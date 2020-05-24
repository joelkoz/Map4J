package org.map4j.coordinates;


/**
 * A class that represents a coordinate in pixel space.
 * <p>Map Tiling divides a Web Mercator projected map up into "tiles" that
 * are exactly square (usually 256 x 256). The number of tiles needed to cover the world map depends
 * on the "Zoom" level.  A zoom level divides the map up with an equal number of horizontal and
 * vertical tiles, with that number being represented with the formula 2 ^ Zoom. Thus, a Zoom level 
 * of "zero" has a single 256 x 256 tile that represents the entire earth. A zoom level of "one" 
 * is a "2 x 2" matrix of tiles.  Level two is 4 x 4, etc. Assuming a 256 x 256 tile is used, a
 * level two zoom would be able to display the entire world map as a 4 x 256 = 1024 x 1024 pixel
 * image. This entire grid of pixels taken as a single image represents "pixel space". Pixel 
 * space is usually written as (x, y, z), which represents the (x,y) coordinate of a position 
 * in pixel space at a zoom level of z.
 * 
 * @author Joel Kozikowski
 */
public class PCoordinate implements IPixelCoordinate {

    protected int px;
    protected int py;
    protected byte zoom;
    protected MapProjections proj;

    public PCoordinate() {
        this(0,0,0);
    }
    
    /**
     * Constructs a PCoordinate object using a 256 pixel tile size
     * @param px x in pixel space
     * @param py y in pixel space
     * @param zoom the zoom level of the pixel space
     */
    public PCoordinate(int px, int py, int zoom) {
        this(px, py, zoom, MapProjections.Merc256);
    }
    
    
    public PCoordinate(int px, int py, int zoom, MapProjections proj) {
        this.px = px;
        this.py = py;
        this.zoom = (byte)zoom;
        this.proj = proj;
    }


    
    public PCoordinate(PCoordinate other) {
        this.px = other.px;
        this.py = other.py;
        this.zoom = other.zoom;
        this.proj = other.proj;
    }
    
    
    /**
     * A PCoordinate at px, py, using the same zoom level and
     * projection as other.
     */
    public PCoordinate(int px, int py, PCoordinate other) {
        this.px = px;
        this.py = py;
        this.zoom = other.zoom;
        this.proj = other.proj;
    }
    
        
    /**
     * Returns the maximum number of pixels that are on a map
     * image that contains this coordinate.
     */
    public int getMaxPixels() {
        return getMaxPixels(proj.getTileSize(), this.zoom);
    }
    
        
    /**
     * Returns the maximum number of pixels that are on a map
     * image with the specified tileSize at the specified zoom
     * level. Since images are always square, this number
     * represents both the max number of horizontal and vertical
     * pixels.
     */
    public static int getMaxPixels(int tileSize, int zoom) {
        return tileSize * (1 << zoom);
    }


    /**
     * Returns this coordinate converted to world space.
     */
    public WCoordinate asW() {
        double lat = proj.pyToLat(py, zoom);
        double lon = proj.pxToLon(px, zoom);
        return new WCoordinate(lat, lon, proj);
    }

    
    
    /**
     * Returns the tile coordinate that contains this pixel coordinate in XYZ
     * tile space.
     */
    public TCoordinate asT() {
        return asT(true);
    }
    
    
    
    /**
     * Returns the tile coordinate that contains this pixel coordinate.
     * @param useXYZ if TRUE, the tile will be returned in the XYZ coordinate system.
     *   Otherwise, TMS is assumed.
     */
    public TCoordinate asT(boolean useXYZ) {
        int col = px / proj.getTileSize();
        int row = py / proj.getTileSize();
        if (!useXYZ) {
            row = TCoordinate.flipY(row, zoom);
        }
        return new TCoordinate(col, row, zoom, useXYZ, proj);
    }
    
    
    public boolean equals(PCoordinate other) {
        return (other != null) &&
               (this.px == other.px) &&
               (this.py == other.py) &&
               (this.zoom == other.zoom) &&
               (this.proj.equals(other.proj));
    }

    
    @Override
    public boolean equals(Object other) {
        return (other != null) &&
               (other instanceof PCoordinate) &&
               this.equals((PCoordinate)other);
    }
    
    
    @Override
    public int getPixelX() {
        return this.px;
    }

    @Override
    public void setPixelX(int newX) {
        this.px = newX;
    }

    
    /**
     * Adds the value of delta to the current X
     * coordinate
     */
    public void adjustX(int delta) {
        this.px += delta;
    }
    
    
    @Override
    public int getPixelY() {
        return this.py;
    }

    @Override
    public void setPixelY(int newY) {
        this.py = newY;
    }

    
    /**
     * Adds the value of delta to the current Y
     * coordinate
     */
    public void adjustY(int delta) {
        this.py += delta;
    }

    @Override
    public int getZoom() {
        return this.zoom;
    }
    
}
