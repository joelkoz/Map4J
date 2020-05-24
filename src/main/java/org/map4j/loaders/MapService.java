package org.map4j.loaders;

/**
 * MapService represents a web tile service that can have tiles retrieved using
 * MapServiceLoader.
 * 
 * @author Joel Kozikowski
 */
public class MapService implements IMapService, ITileLoaderFactory {

    private String baseUrl;
    private String imageExtension;
    private boolean useXYZ;
    private String sourceId;
    
    
    public MapService(String sourceId, String baseUrl) {
        this(sourceId, baseUrl, "png", true);
    }
    
    
    /**
     * MapService can specify loading tiles from web services
     * @param sourceId A short identification to uniquely identify this tile source
     *   from other tile sources that are used in the same runtime environment. It is used
     *   primarily for cache key generation.
     * @param baseUrl The base extension of the URL to use, up to just before the slash
     *    preceding the "z" tile coordinate. It should NOT have an ending slash
     * @param imageExtension The expected image format (usually "png" or "jpg"), which
     *   is appended onto the url. specify NULL if no extension should be appended.
     * @param useXYZ TRUE if the tile service uses XYZ tile coordinates, false if using TMS
     */
    public MapService(String sourceId, String baseUrl, String imageExtension, boolean useXYZ) {
        this.sourceId = sourceId;
        this.baseUrl = baseUrl;
        this.imageExtension = imageExtension;
        this.useXYZ = useXYZ;
    }

    
    @Override
    public String getTileUrl(Tile tile) {
        String url = this.baseUrl + "/" + tile.coord.getRequestPath(this.useXYZ);
        if (this.imageExtension != null) {
            return url + "." + this.imageExtension;
        }
        else {
            return url;
        }
    }


    @Override
    public ITileLoader getTileLoader() {
        return new MapServiceLoader(this);
    }


    @Override
    public String getSourceId() {
        return this.sourceId;
    }

}
