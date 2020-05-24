package org.map4j.loaders.cache;

import java.awt.image.BufferedImage;

import org.map4j.coordinates.TCoordinate;
import org.map4j.loaders.Tile;

/**
 * Interface for managing a cache of tiles
 * {@link JMapViewer}.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 * 
 */
public interface ITileCache {

    /**
     * Retrieves a tile from the cache if present, otherwise <code>null</code>
     * will be returned.
     *
     * @param sourceId The unique identifier for the source of the tiles
     *            
     * @return the requested tile or <code>null</code> if the tile is not
     *         present in the cache
     */
    Tile getTile(String sourceId, TCoordinate coord);

    
    /**
     * Adds a tile to the cache. How long after adding a tile can be retrieved
     * via {@link #getTile(String, TCoordinate)} is unspecified and depends on the
     * implementation.
     *
     * @param tile the tile to be added
     */
    void addTile(Tile tile);

    
    /**
     * @return the number of tiles hold by the cache
     */
    int getTileCount();

    
    /**
     * Clears the cache deleting all tiles from memory.
     */
    void clear();

    
    /**
     * Size of the cache.
     * @return maximum number of tiles in cache
     */
    int getCacheSize();


    /**
     * Retrieves a placeholder image from the cache for the specified tile coordinates
     * If no such image can be made, null should be returned.
     */
    BufferedImage getPlaceholder(String sourceId, TCoordinate coord);
}
