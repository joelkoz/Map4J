package org.map4j.loaders;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The abstract base class of all tile loaders, it supports multi-threaded loading of 
 * individual tiles via a ThreadPoolExecutor.
 *
 * @author Joel Kozikowski
 */
public abstract class AbstractTileLoader implements ITileLoader {

    protected static final ThreadPoolExecutor jobDispatcher = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);


    public AbstractTileLoader() {
    }
  
    
    @Override
    public boolean hasOutstandingJobs() {
        return jobDispatcher.getTaskCount() > jobDispatcher.getCompletedTaskCount();
    }

    
    @Override
    public void cancelOutstandingJobs() {
        jobDispatcher.getQueue().clear();
    }

    
    /**
     * Returns TRUE if the job queue is currently full and any attempt to 
     * create more jobs will result in an error.
     */
    public boolean jobQueueFull() {
        return jobDispatcher.getQueue().size() >= jobDispatcher.getMaximumPoolSize();
    }

    
    /**
     * Sets the maximum number of concurrent connections the tile loader will do
     * @param num number of concurrent connections
     */
    public static void setConcurrentConnections(int num) {
        jobDispatcher.setMaximumPoolSize(num);
    }
}
