package org.map4j.loaders;

/**
 * A tile loader is a class capable of loading the image of tiles
 * into tile objects via a background tile loader job.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public interface ITileLoader {

    /**
     * Creates a loader job to load
     * @param tile
     * @return
     */
    ITileLoaderJob createTileLoaderJob(Tile tile);

    /**
     * cancels all outstanding tasks in the queue. This should rollback the state of the tiles in the queue
     * to loading = false / loaded = false
     */
    void cancelOutstandingJobs();

    /**
     * Determines whether this {@link ITileLoader} has tasks which have not completed.
     * @return whether this {@link ITileLoader} has tasks which have not completed. This answer may well be
     * "approximate" given that many implementations will be using mechanisms where a queue's state can change
     * during the computation.
     */
    boolean hasOutstandingJobs();
    
    
    /**
     * Returns TRUE if the job queue is currently full and any attempt to 
     * create more jobs will result in an error.
     */
    public boolean jobQueueFull();
    
    
    /**
     * Returns the maximum number of times the tile controller should attempt to reload a tile if
     * an error occurred on the previous try. If the load count for a tile exceeds this number, the
     * controller will stop requesting the tile be loaded.
     */
    int getMaxLoadRetries();
    
    
    /**
     * Returns a unique identifier for the source of tiles being used. This identifier is used
     * primarily for internal identification, such as cache keys. It is never seen by end users.
     */
    String getSourceId();
    
    /**
     * Returns the minimum zoom level this tile loader can display
     */
    default int getMinZoom() {
        return 0;
    }
    
    /**
     * Returns the maximum zoom level this tile loader can display
     */
    default int getMaxZoom() {
        return 20;
    }
    
}
