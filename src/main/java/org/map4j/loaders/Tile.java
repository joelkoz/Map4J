package org.map4j.loaders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.map4j.coordinates.TCoordinate;
import org.map4j.tinymq.TinyMQ;
import org.map4j.utils.ImageUtils;

/**
 * A class that represents a single tile image. As soon as
 * the tile has been constructed, getImage() may be called to
 * retrieve SOME type of image that represents this tile
 * (even if that is a placeholder image or a temporary image).
 * Once that actual image has been loaded, a TOPIC_LOADED topic
 * will be published to the TinyMQ broker.
 * 
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public class Tile {

    /**
     * A message broker that allows listeners to subscribe to tile events.
     */
    public static TinyMQ<Tile> broker = new TinyMQ<Tile>();

    public interface TileTopicListener extends TinyMQ.ITopicSubscriber<Tile> {};

    
    /**
     * The topic published whenever a tile has completed a loading attempt.
     */
    public static final String TOPIC_LOADED = "loaded";
    
    /**
     * The tile coordinate where this tile is located in the tile grid
     */
    public TCoordinate coord;
    public String sourceId;
    
    protected BufferedImage image = null;

    public Tile(String sourceId, TCoordinate coord) {
        this.coord = new TCoordinate(coord);
        this.sourceId = sourceId;
    }
   
    
    private BufferedImage placeholder = null;
    
    
    /**
     * Sets a placeholder image that should be displayed until an
     * actual image is available. The placeholder image will be
     * returned by getImage() until an actual image as been
     * set by a call to setImage().
     */
    public void setPlaceholder(BufferedImage placeholder) {
        this.placeholder = placeholder;
    }

    
    /**
     * Returns an image that represents this tile. That image may be
     * a temporary image to represent the tile until it has been loaded
     * from the source.
     * @return
     */
    public BufferedImage getImage() {
        if (image == null && placeholder != null) {
            return this.placeholder;
        }
        else if (this.status == LoadStatus.LOADING || this.status == LoadStatus.UNLOADED) {
            return getLoadingImage();
        }
        else if (this.hasError()) {
            return getErrorImage();
        }
        else {
           return image;
        }
    }

    
    /**
     * Sets the current image that will be displayed by 
     * this tile to the specified image. 
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        if (this.image != null) {
            this.status = LoadStatus.LOADED;
            this.placeholder = null;
        }
    }


    /**
     * Sets the current image displayed by this tile from
     * the specified byte array.
     */
    public void setImage(byte[] bytes) throws IOException {
        setImage(ImageIO.read(new ByteArrayInputStream(bytes)));
    }
    
    
    /**
     * Sets the image that will be displayed to the image that is
     * read from the specified input stream
     * @param input The stream to read the image data from.
     */
    public void setImage(InputStream input) throws IOException {
        setImage(ImageIO.read(input));
    }

    
    /**
     * Sets the current image displayed by this tile from
     * the specified file name.
     */
    public void setImage(String fileName) throws IOException {
        setImage(ImageUtils.load(fileName));
    }
    
    
    enum LoadStatus {
        UNLOADED,
        LOADING,
        LOADED
    };
    
    private volatile LoadStatus status = LoadStatus.UNLOADED;
    private volatile int loadErrorCount = 0;

    protected String error_message;
    
    public boolean isLoaded() {
        return (status == LoadStatus.LOADED);
    }

    public boolean isLoading() {
        return (status == LoadStatus.LOADING);
    }

    
    public boolean hasError() {
        return loadErrorCount > 0;
    }

    public String getErrorMessage() {
        return error_message;
    }

    public void setError(Exception e) {
        setError(e.toString());
    }

    public void setError(String message) {
        error_message = message;
        loadErrorCount++;
    }

    
    public int getLoadErrorCount() {
        return loadErrorCount;
    }

    
    /**
     * indicate that loading process for this tile has started
     */
    public void startLoading() {
        this.status = LoadStatus.LOADING;
    }

    
    /**
     * indicate that loading process for this tile has ended
     */
    public void finishLoading(boolean success) {
        this.status = LoadStatus.LOADED;
        
        // Notify whomever is interested, this tile has finished a loading attempt
        broker.publish(TOPIC_LOADED, this);
    }
    

    /**
     * indicate that loading process for this tile has been canceled
     */
    public void loadingCanceled() {
        this.status = LoadStatus.UNLOADED;
    }

    
    /**
     * Returns a key unit to this tile that can be used for cache storage.
     */
    public String getCacheKey() {
        return Tile.getCacheKey(this.sourceId, this.coord);
    }

    
    public static String getCacheKey(String sourceId, TCoordinate coord) {
        return coord.getXYZRequestPath() + "@" + sourceId;
    }
    
    
    /** TileLoader-specific tile metadata */
    protected Map<String, String> metadata;
    
    
    /**
     * Puts the given key/value pair to the metadata of the tile.
     * If value is null, the (possibly existing) key/value pair is removed from
     * the meta data.
     *
     * @param key Key
     * @param value Value
     */
    public void putMetaValue(String key, String value) {
        if (value == null || value.isEmpty()) {
            if (metadata != null) {
                metadata.remove(key);
            }
            return;
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    
    /**
     * returns the metadata of the Tile
     *
     * @param key metadata key that should be returned
     * @return null if no such metadata exists, or the value of the metadata
     */
    public String getMetaValue(String key) {
        if (metadata == null) return null;
        return metadata.get(key);
    }

    
    /**
     *
     * @return metadata of the tile
     */
    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }
    

    /**
     * Returns an image that represents a tile that is currently being loaded.
     * The image is lazy loaded if necessary.
     */
    public static BufferedImage getLoadingImage() {
        if (LOADING_IMAGE == null) {
            LOADING_IMAGE = loadResourceImage("/images/loader/hourglass.png");
        }
        return LOADING_IMAGE;
    }

    
    /**
     * Returns an image that represents a tile that has an error condition.
     * The image is lazy loaded if necessary.
     */
    public static BufferedImage getErrorImage() {
        if (ERROR_IMAGE == null) {
            ERROR_IMAGE = loadResourceImage("/images/loader/error.png");
        }
        return ERROR_IMAGE;
    }
    
    
    /**
     * Hourglass image that is displayed until a map tile has been loaded
     */
    private static BufferedImage LOADING_IMAGE = null;
    
    /**
     * Red "X" image that is displayed to represent a tile with a loading error
     */
    private static BufferedImage ERROR_IMAGE = null;;
    
    private static BufferedImage loadResourceImage(String path) {
        return ImageUtils.loadResourceImage(path);
    }

    
}
