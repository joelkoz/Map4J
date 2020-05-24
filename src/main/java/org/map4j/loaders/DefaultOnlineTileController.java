package org.map4j.loaders;

import org.map4j.loaders.cache.MemoryTileCache;


/**
 * The default loader controller that will load map tiles from an online
 * map service, such as Open Street Maps, caching them in a simple in-memory cache.
 * 
 * @author Joel Kozikowski
 */
public class DefaultOnlineTileController extends TileLoaderController {

    
    /**
     * A tile loader controller that will load its tiles from an online source.
     * @param sourceId A short identification to uniquely identify this tile source
     *   from other tile sources that are used in the same runtime environment. It is used
     *   primarily for cache key generation.
     * @param baseUrl The base extension of the URL to use, up to just before the slash
     *    preceding the "z" tile coordinate. It should NOT have an ending slash
     * @param imageExtension The expected image format (usually "png" or "jpg"), which
     *   is appended onto the url. specify NULL if no extension should be appended.
     * @param useXYZ TRUE if the tile service uses XYZ tile coordinates, false if using TMS
     */
    public DefaultOnlineTileController(String sourceId, String baseUrl, String imageExtension, boolean useXYZ) {
        super(new MapServiceLoader(new MapService(sourceId, baseUrl, imageExtension, useXYZ)), new MemoryTileCache());
    }

}
