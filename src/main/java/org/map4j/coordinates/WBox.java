package org.map4j.coordinates;

/**
 * A WBox represents two points in World Space that form a rectangle.
 * The rectangle is always normalized so coordinate number one is
 * upper left, and coordinate 2 is lower right (i.e. w1.lat < w2.lat
 * and w1.lon < w2.lon).
 * 
 * @author Joel Kozikowski
 */
public class WBox {

    public WCoordinate w1;
    public WCoordinate w2;

    
    public WBox(double lat1, double lon1, double lat2, double lon2) {
        this(lat1, lon1, lat2, lon2, MapProjections.Merc256);
    }
    
    
    public WBox(double lat1, double lon1, double lat2, double lon2, MapProjections proj) {

        if (lat1 > lat2) {
            double temp = lat1;
            lat1 = lat2;
            lat2 = temp;
        }
        
        if (lon1 > lon2) {
            double temp = lon1;
            lon1 = lon2;
            lon2 = temp;
        }
        
        w1 = new WCoordinate(lat1, lon1, proj);
        w2 = new WCoordinate(lat2, lon2, proj);
    }


    public WBox(WCoordinate arg1, WCoordinate arg2) {
        this(arg1.getLat(), arg1.getLon(), arg2.getLat(), arg2.getLon(), arg1.proj);
        assert(arg1.proj.getTileSize() == arg2.proj.getTileSize());
    }
    
    
    public PBox asPBox(int zoom) {
        PCoordinate p1 = w1.asP(zoom);
        PCoordinate p2 = w2.asP(zoom);
        return new PBox(p1, p2); 
    }
    
    
    public TBox asTBox(int zoom) {
        return asTBox(zoom, true);
    }
    
    
    public TBox asTBox(int zoom, boolean useXYZ) {
        PBox pbox = this.asPBox(zoom);
        return pbox.asTBox(useXYZ);
    }
    
}
