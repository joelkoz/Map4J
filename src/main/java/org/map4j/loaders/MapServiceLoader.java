package org.map4j.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A tile loader implementation that loads tiles from online sources 
 * such as OpenStreetMaps and Bing.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public class MapServiceLoader extends AbstractTileLoader {
    
    private final class MapServiceJob implements ITileLoaderJob {
        private final Tile tile;
        private InputStream input;
        private boolean force;

        private MapServiceJob(Tile tile) {
            this.tile = tile;
        }

        @Override
        public void run() {
            synchronized (tile) {
                
                if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading()) {
                    // Tiles loaded without error as well as tiles currently being
                    // loaded do not need a new job, so simply return.
                    return;
                }
                
                tile.startLoading();
            }
            boolean success = false;
            try {
                URLConnection conn = loadTileFromOsm(tile);
                if (force) {
                    conn.setUseCaches(false);
                }
                loadTileMetadata(tile, conn);
                if ("no-tile".equals(tile.getMetaValue("tile-info"))) {
                    tile.setError("No tile at this zoom level");
                } else {
                    input = conn.getInputStream();
                    try {
                        tile.setImage(input);
                    } finally {
                        input.close();
                        input = null;
                    }
                }
                success = true;
            } catch (IOException e) {
                tile.setError(e.getMessage());
                if (input == null) {
                    try {
                        System.err.println("Failed loading " + getTileUrl(tile) +": "
                                +e.getClass() + ": " + e.getMessage());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            } finally {
                tile.finishLoading(success);
            }
        }

        @Override
        public Tile getTile() {
            return tile;
        }

        @Override
        public void startTileLoad() {
            startTileLoad(false);
        }

        @Override
        public void startTileLoad(boolean force) {
            this.force = force;
            jobDispatcher.execute(this);
        }
    }

    /**
     * Holds the HTTP headers. Insert e.g. User-Agent here when default should not be used.
     */
    public Map<String, String> headers = new HashMap<>();

    public int timeoutConnect;
    public int timeoutRead;

    protected IMapService mapService;

    public MapServiceLoader(IMapService mapService) {
        this(mapService, null);
    }

    public MapServiceLoader(IMapService mapService, Map<String, String> headers) {
        this.headers.put("Accept", "text/html, image/png, image/jpeg, image/gif, */*");
        this.headers.put("User-Agent", "Map4J/1.0 (" + System.getProperty("java.version") + ")");
        if (headers != null) {
            this.headers.putAll(headers);
        }
        this.mapService = mapService;
    }

    
    @Override
    public ITileLoaderJob createTileLoaderJob(final Tile tile) {
        return new MapServiceJob(tile);
    }

    
    protected String getTileUrl(Tile tile) throws IOException {
        return mapService.getTileUrl(tile);
    }
    
    protected URLConnection loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(getTileUrl(tile));
        URLConnection urlConn = url.openConnection();
        if (urlConn instanceof HttpURLConnection) {
            prepareHttpUrlConnection((HttpURLConnection) urlConn);
        }
        return urlConn;
    }

    protected void loadTileMetadata(Tile tile, URLConnection urlConn) {
        String str = urlConn.getHeaderField("X-VE-TILEMETA-CaptureDatesRange");
        if (str != null) {
            tile.putMetaValue("capture-date", str);
        }
        str = urlConn.getHeaderField("X-VE-Tile-Info");
        if (str != null) {
            tile.putMetaValue("tile-info", str);
        }

        Long lng = urlConn.getExpiration();
        if (lng.equals(0L)) {
            try {
                str = urlConn.getHeaderField("Cache-Control");
                if (str != null) {
                    for (String token: str.split(",")) {
                        if (token.startsWith("max-age=")) {
                            lng = Long.parseLong(token.substring(8)) * 1000 +
                                    System.currentTimeMillis();
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // ignore malformed Cache-Control headers
            }
        }
        if (!lng.equals(0L)) {
            tile.putMetaValue("expires", lng.toString());
        }
    }

    protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
        for (Entry<String, String> e : headers.entrySet()) {
            urlConn.setRequestProperty(e.getKey(), e.getValue());
        }
        if (timeoutConnect != 0)
            urlConn.setConnectTimeout(timeoutConnect);
        if (timeoutRead != 0)
            urlConn.setReadTimeout(timeoutRead);
    }

    
    @Override
    public int getMaxLoadRetries() {
        return 2;
    }

    @Override
    public String getSourceId() {
        return mapService.getSourceId();
    }

}
