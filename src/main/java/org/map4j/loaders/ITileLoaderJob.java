package org.map4j.loaders;

/**
 * Interface to be implemented by processes that can load an individual tile 
 *
 * @author Dirk Stöcker
 * @author Joel Kozikowski
 */
public interface ITileLoaderJob extends Runnable {

    
    /**
     * Returns the tile object this tile job is responsible for loading.
     */
    public Tile getTile();
    
    
    /**
     * submits download job to backend.
     */
    void startTileLoad();

    /**
     * submits download job to backend.
     * @param force true if the load should skip all the caches (local &amp; remote)
     */
    void startTileLoad(boolean force);
}
