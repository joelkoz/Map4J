package org.map4j.loaders;

import org.map4j.loaders.cache.MemoryTileCache;
import org.map4j.utils.mbtiles.MBTilesFile;


/**
 * The default loader controller that will load map tiles from a local
 * MBTiles file, caching them in a simple in-memory cache.
 * @author Joel Kozikowski
 */
public class DefaultMBTilesController extends TileLoaderController {

    public DefaultMBTilesController(String mbTilesFileName) {
        super(new MBTilesLoader(new MBTilesFile(mbTilesFileName)), new MemoryTileCache());
    }

}
