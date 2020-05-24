package org.map4j.loaders;

public interface IMapService {

    /**
     * Returns the Url that can be used to retrieve the specified tile.
     */
    String getTileUrl(Tile tile);

    
    /**
     * Returns an Id that uniquely identifies this map service
     */
    String getSourceId();
    
}
