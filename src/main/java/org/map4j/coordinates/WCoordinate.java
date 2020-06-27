package org.map4j.coordinates;


/**
 * A class that represents a coordinate in World Space.
 * <p>This is the Latitude/Longitude defined in WGS84 and found 
 * on most GPS systems
 * 
 * @author Joel Kozikowski
 */
public class WCoordinate implements IWorldCoordinate {

    private double lat;
    private double lon;
    protected MapProjections proj;

    private PCoordinate cachedPCoord;

    public WCoordinate() {
        this(0,0);
    }

    
    /**
     * Constructs a coordinate. If conversions are needed to other spaces,
     * a 256 pixel tile size will be used.
     * @param lat WGS84 latitude
     * @param lon WGS84 longitude
     */
    public WCoordinate(double lat, double lon) {
        this(lat, lon, MapProjections.Merc256);
    }
    
    
    public WCoordinate(double lat, double lon, MapProjections proj) {
        this.lat = lat;
        this.lon = lon;
        this.proj = proj;
    }
    
    
    public WCoordinate(WCoordinate other) {
        this.lat = other.lat;
        this.lon = other.lon;
        this.proj = other.proj;
    }

    
    /**
     * Returns this coordinate converted to pixel space.
     */
    public PCoordinate asP(int zoom) {
        
        if (cachedPCoord == null || cachedPCoord.zoom != zoom) {
            // We don't have a matching cached PCoord value to
            // use, so create a new one...
            int px = proj.lonToPX(lon, zoom);
            int py = proj.latToPY(lat, zoom);
            cachedPCoord = new PCoordinate(px, py, (byte)zoom, proj);
        }
                
        return cachedPCoord;
    }

    
    /**
     * Returns this coordinate converted to tile space.
     */
    public TCoordinate asT(int zoom) {
        PCoordinate pcoord = this.asP(zoom);
        return pcoord.asT();
    }
    
    
    public boolean equals(WCoordinate other) {
        return (other != null) &&
               (this.lat == other.lat) &&
               (this.lon == other.lon &&
               (this.proj.equals(other.proj)));
    }


    @Override
    public boolean equals(Object other) {
        return (other != null) &&
               (other instanceof WCoordinate) &&
               this.equals((WCoordinate)other);
    }
    

    @Override
    public double getLat() {
        return this.lat;
    }


    @Override
    public void setLat(double newLat) {
        this.lat = newLat;
        cachedPCoord = null;
    }


    @Override
    public double getLon() {
        return this.lon;
    }


    @Override
    public void setLon(double newLon) {
        this.lon = newLon;
        cachedPCoord = null;
    }
    
    @Override
    public String toString() {
        return "(" + lat + ", " + lon + ")";
    }
    
    
    /**
     * Returns the WCoordinate of org with the latitude and longitude adjusted
     * by the specified values (in meters).
     * @param deltaLat Change in latitude, specified in meters
     * @param deltaLon Change in longitude, specified in meters
     */
    public static WCoordinate adjustPosition(WCoordinate org, double deltaLat, double deltaLon) {
        double r_earth = MapProjections.EARTH_RADIUS;
        double new_lat  = org.lat  + (deltaLat / r_earth) * (180 / Math.PI);
        double new_lon = org.lon + (deltaLon / r_earth) * (180 / Math.PI) / Math.cos(org.lat * Math.PI/180);
        return new WCoordinate(new_lat, new_lon, org.proj);
    }
    
    
    /**
     * Adjusts this coordinate with the specified adjustment values (in meters).
     * @param deltaLat Change in latitude, specified in meters
     * @param deltaLon Change in longitude, specified in meters
     */
    public void adjustPosition(double deltaLat, double deltaLon) {
        WCoordinate newPos = adjustPosition(this, deltaLat, deltaLon);
        setLat(newPos.lat);
        setLon(newPos.lon);
    }
    
}
