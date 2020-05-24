package org.map4j.loaders;

import org.map4j.coordinates.TCoordinate;
import org.map4j.loaders.cache.ITileCache;

/**
 * A TileLoaderController manages the loading of tiles by queuing up tile load jobs
 * in the tile loader if the tile is not already available in the tile cache.
 */
public class TileLoaderController {

    private ITileLoader tileLoader;
    private ITileCache tileCache;
    
    /**
     * @param tileSource The initial source to load tiles from
     * @param listener The listener to be notified when tiles have been loaded.
     */
    public TileLoaderController(ITileLoader tileLoader, ITileCache tileCache) {
        this.tileLoader = tileLoader;
        this.tileCache = tileCache;
    }

    
    /**
     * retrieves a tile from the cache. If the tile is not present in the cache
     * a load job is added to the working queue of {@link TileLoader}.
     *
     * @param coord The tile coordinate of the tile to load
     * @return specified tile from the cache, or a new tile object
     *   that is created with a corresponding loader job started.
     */
    public Tile getTile(TCoordinate coord) {

        Tile tile = tileCache.getTile(tileLoader.getSourceId(), coord);
        if (tile == null) {
            tile = new Tile(tileLoader.getSourceId(), coord);
            tileCache.addTile(tile);
            tile.setPlaceholder(tileCache.getPlaceholder(tileLoader.getSourceId(), coord));
        }
        if (!tile.isLoaded()) {
            if (tile.getLoadErrorCount() <= tileLoader.getMaxLoadRetries()) {
               tileLoader.createTileLoaderJob(tile).startTileLoad();
            }
        }
        return tile;
    }

    
    public ITileCache getTileCache() {
        return tileCache;
    }

    
    public void setTileCache(ITileCache tileCache) {
        this.tileCache = tileCache;
    }
    
    
    /**
     * Removes all jobs from the queue that are currently not being processed by
     * the tile loader (if any).
     */
    public void cancelOutstandingJobs() {
        tileLoader.cancelOutstandingJobs();
    }
    
    
    public int getMinZoom() {
        return tileLoader.getMinZoom();
    }
    
    
    public int getMaxZoom() {
        return tileLoader.getMaxZoom();
    }
    
    @Override
    public String toString() {
       return tileLoader.toString();  
    }
}
