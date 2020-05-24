package org.map4j.loaders;


/**
 * ILoaderFactory creates new tile loader objects
 * @see ITileLoader
 * 
 * @author Joel Kozikowski
 */
public interface ITileLoaderFactory {

    /**
     * Creates a tile loader 
     */
    ITileLoader getTileLoader();

}
