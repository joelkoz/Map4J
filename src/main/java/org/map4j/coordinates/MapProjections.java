package org.map4j.coordinates;

/**
 * This class implements projection/conversion methods for various coordinate
 * systems used in mapping. In particular, there are four coordinate systems:
 * <ol><li><b>WGS84</b> (aka World Space) - This is the Latitude/Longitude defined in WGS84 and found 
 *         on most GPS systems</li>
 *     <li><b>Web Mercator</b> (aka Map Space)- A system that uses a "Spherical Mercator" projection of the
 *         world map onto a 2D space, then divides it into a grid where 1 unit = 1 meter. This
 *         system was pioneered by Google Maps. Officially
 *         known as EPSG:3857, it is used by large numbers of online consumer map technologies, including
 *         OpenStreetMap. (0,0) of map space is the top left corner, which has a latitude of approx 85 degrees,
 *         and a longitude of -180.</li>
 *     <li><b>Pixel Space</b> - Map Tiling divides a Web Mercator projected map up into "tiles" that
 *         are exactly square (usually 256 x 256). The number of tiles needed to cover the world map depends
 *         on the "Zoom" level.  A zoom level divides the map up with an equal number of horizontal and
 *         vertical tiles, with that number being represented with the formula 2 ^ Zoom. Thus, a Zoom level 
 *         of "zero" has a single 256 x 256 tile that represents the entire earth. A zoom level of "one" 
 *         is a "2 x 2" matrix of tiles.  Level two is 4 x 4, etc. Assuming a 256 x 256 tile is used, a
 *         level two zoom would be able to display the entire world map as a 4 x 256 = 1024 x 1024 pixel
 *         image. This entire grid of pixels taken as a single image represents "pixel space". Pixel 
 *         space is usually written as (x, y, z), which represents the (x,y) coordinate of a position 
 *         in pixel space at a zoom level of z.</li>
 *     <li><b>Tile Space</b> - Similar to pixel space, Tile Space represents the actual tile column and tile
 *          row number.  The "origin" of Tile Space is (0,0). where the column zero is the left most tile column.
 *          For the ROW, however, there are two standards: "XYZ", used by Google, Bing, OpenStreetMap, etc. places
 *          row zero at the TOP of the map with increasing numbers going downward. The other standard is "TMS" 
 *          and it places the zero row at the BOTTOM of the map with increasing numbers going upward.  TMS is used
 *          in the popular storage file format MBTiles.</li>
 * </ol>
 * It is helpful to remember that for a tiling system, the tile size is ALWAYS FIXED, and is ALWAYS SQUARE. The
 * most commonly used tile size is 256 x 256. The tile grid is also ALWAYS SQUARE. In addition to being equal, 
 * the number of columns/rows that exist for a particular zoom level is always a POWER of two.
 * 
 * Some of this code was borrowed from the OpenStreeMap.org project "jmapviewer"
 * @author Jan Peter Stotz
 * @author Jason Huntley
 * @author Joel Kozikowski
 */
public class MapProjections {

    public static final MapProjections Merc256 = new MapProjections(256);
    
    
    /**
     * default tile size
     */
    public static final int DEFAUL_TILE_SIZE = 256;
    
    /** maximum latitude (north) for mercator display */
    public static final double MAX_LAT = 85.05112877980659;
    
    /** minimum latitude (south) for mercator display */
    public static final double MIN_LAT = -85.05112877980659;
    
    /** equatorial earth radius for EPSG:3857 (Mercator) in meters */
    public static final double EARTH_RADIUS = 6378137;


    /** tile size of the displayed tiles */
    private int tileSize = DEFAUL_TILE_SIZE;

    /**
     * Creates instance with default tile size of 256
     */
    public MapProjections() {
    }

    
    /**
     * Creates instance with provided tile size.
     * @param tileSize tile size in pixels
     */
    public MapProjections(int tileSize) {
        this.tileSize = tileSize;
    }

    
    /**
     * The tile size is the width and height of a single tile. The Defacto standard
     * tile size is 256 x 256 pixels, though that is not universally the case.
     * @return
     */
    public int getTileSize() {
        return this.tileSize;
    }

    
    /**
     * Returns the absolute number of pixels in y or x, defined as:
     * <code>tileSize * getMaxTiles(zoom)</code> 
     * where tileSize is the width of a single tile in pixels
     *
     * @param zoom Zoom level to request pixel data
     * @return number of pixels
     */
    public int getMaxPixels(int zoom) {
        return tileSize * (1 << zoom);
    }


    /**
     * Returns the number of tiles in a single column or row
     * that are needed to cover the world at a specified zoom level. 
     * @param zoom Zoom level to request tile data
     * @return number of tiles in a single row or column
     */
    public int getMaxTiles(int zoom) {
        return (1 << zoom);
    }
    
    
    
    public int falseEasting(int zoom) {
        return getMaxPixels(zoom) / 2;
    }

    
    
    public int falseNorthing(int zoom) {
        return -1 * getMaxPixels(zoom) / 2;
    }

    
    /**
     * Transform pixelspace to coordinates and get the distance.
     *
     * @param px1 the first x coordinate
     * @param py1 the first y coordinate
     * @param px2 the second x coordinate
     * @param py2 the second y coordinate
     *
     * @param zoom the zoom level
     * @return the distance
     */
    public double getDistance(int px1, int py1, int px2, int py2, int zoom) {
        double la1 = pyToLat(py1, zoom);
        double lo1 = pxToLon(px1, zoom);
        double la2 = pyToLat(py2, zoom);
        double lo2 = pxToLon(px2, zoom);

        return getDistance(la1, lo1, la2, lo2);
    }

    /**
     * Gets the distance using Spherical law of cosines.
     *
     * @param la1 the Latitude in degrees
     * @param lo1 the Longitude in degrees
     * @param la2 the Latitude from 2nd coordinate in degrees
     * @param lo2 the Longitude from 2nd coordinate in degrees
     * @return the distance
     */
    public double getDistance(double la1, double lo1, double la2, double lo2) {
        double aStartLat = Math.toRadians(la1);
        double aStartLong = Math.toRadians(lo1);
        double aEndLat = Math.toRadians(la2);
        double aEndLong = Math.toRadians(lo2);

        double distance = Math.acos(Math.sin(aStartLat) * Math.sin(aEndLat)
                + Math.cos(aStartLat) * Math.cos(aEndLat)
                * Math.cos(aEndLong - aStartLong));

        return EARTH_RADIUS * distance;
    }

    /**
     * Transform longitude to pixelspace
     * @param lon
     * @param aZoomlevel zoom level
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    public int lonToPX(double lon, int aZoomlevel) {
        int mp = getMaxPixels(aZoomlevel);
        double x = (mp * (lon + 180L)) / 360L;
        return (int)Math.min(x, mp);
    }

    /**
     * Transforms latitude to pixelspace
     * @param lat
     * @param aZoomlevel zoom level
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    public int latToPY(double lat, int aZoomlevel) {
        if (lat < MIN_LAT)
            lat = MIN_LAT;
        else if (lat > MAX_LAT)
            lat = MAX_LAT;
        double sinLat = Math.sin(Math.toRadians(lat));
        double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
        int mp = getMaxPixels(aZoomlevel);
        double y = mp * (0.5 - (log / (4.0 * Math.PI)));
        return (int)Math.min(y, mp - 1);
    }

    /**
     * Transforms pixel coordinate X to longitude
     *
     * @param px
     * @param zoom zoom level
     * @return ]-180..180[
     */
    public double pxToLon(int px, int zoom) {
        return ((360d * px) / getMaxPixels(zoom)) - 180.0;
    }

    /**
     * Transforms pixel coordinate Y to latitude
     *
     * @param py
     * @param zoom zoom level
     * @return [MIN_LAT..MAX_LAT] is about [-85..85]
     */
    public double pyToLat(int py, int zoom) {
        py += falseNorthing(zoom);
        double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * py / radius(zoom))));
        return -1 * Math.toDegrees(latitude);
    }

    
    private double radius(int zoom) {
        return (tileSize * (1 << zoom)) / (2.0 * Math.PI);
    }

    
    public boolean equals(MapProjections other) {
        return (this.tileSize == other.tileSize);
    }
    
}
