package org.map4j.loaders;

import java.io.InputStream;

import org.map4j.utils.mbtiles.MBTile;
import org.map4j.utils.mbtiles.MBTilesFile;

/**
 * A tile loader that can load tiles from a local MBTiles source
 * 
 * @author Joel Kozikowski
 */
public class MBTilesLoader extends AbstractTileLoader {

    /**
     * A TileJob that will load a single tile from the MBTiles reader
     * @author Joel Kozikowski
     */
    private final class MBTilesJob implements ITileLoaderJob {

        private final Tile m4jTile;
        private InputStream input;

        private MBTilesJob(Tile m4jTile) {
            this.m4jTile = m4jTile;
        }

        @Override
        public void run() {
            
            if (mbt == null) {
                // We failed to open the MBTiles file.
                return;
            }
            
            synchronized (m4jTile) {
                
                if ((m4jTile.isLoaded() && !m4jTile.hasError()) || m4jTile.isLoading()) {
                    // Tiles loaded without error as well as tiles currently being
                    // loaded do not need a new job, so simply return.
                    return;
                }
                
                m4jTile.startLoading();
            }
            
            boolean success = false;
            try {
               int z = m4jTile.coord.getZoom();
               int x = m4jTile.coord.getCol();
               int y = m4jTile.coord.getRow(useXYZ);
               MBTile mbTile = null;
               try {
                  mbTile = mbt.getTile(z, x, y);
               }
               catch (Exception ex) {
                  if (getRootCause(ex).getMessage().indexOf("ResultSet closed") > 0) {
                      throw ex;
                  }
               }
               if (mbTile != null) {
                   m4jTile.setImage(mbTile.getImage());
                   success = true;
               }
               else {
                   m4jTile.setError(m4jTile.getCacheKey() + " not found in MBTile database");
               }
            } catch (Exception e) {
                m4jTile.setError(e.getMessage());
                if (input == null) {
                    try {
                        System.err.println("Failed loading " + m4jTile.getCacheKey() + ": "
                                +e.getClass() + ": " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }
            } finally {
                m4jTile.finishLoading(success);
            }
        }

        @Override
        public Tile getTile() {
            return m4jTile;
        }
        
        @Override
        public void startTileLoad() {
            startTileLoad(false);
        }

        @Override
        public void startTileLoad(boolean force) {
            jobDispatcher.execute(this);
        }
    }
    
    
    private MBTilesFile mbt;
    private String sourceId;
    private boolean useXYZ;
    

    /**
     * @param mbtReader An MBTilesReader that contains the data being loaded
     */
    public MBTilesLoader(MBTilesFile mbtReader) {
        this(mbtReader, null, false);
    }

    
    public MBTilesLoader(MBTilesFile mbtReader, String sourceId) {
        this(mbtReader, sourceId, false);
    }
    
    
    /**
     * @param mbtReader An MBTilesReader that contains the data being loaded
     * @param useXYZ if TRUE, an XYZ tile coordinate system is assumed to be
     *   stored in the file vs. the normal TMS required in the MBTiles specification.
     */
    public MBTilesLoader(MBTilesFile mbtReader, String sourceId, boolean useXYZ) {
        super();
        this.mbt = mbtReader;
        this.sourceId = sourceId;
        this.useXYZ = useXYZ;
    }

    
    @Override
    public ITileLoaderJob createTileLoaderJob(org.map4j.loaders.Tile jmvTile) {
        return new MBTilesJob(jmvTile);
    }


    @Override
    public int getMaxLoadRetries() {
        return 0;
    }

    
    public static Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;        
    }


    @Override
    public String getSourceId() {
        if (sourceId == null) {
            sourceId = mbt.getMetadata().getTilesetName();
            if (sourceId == null) {
                // There is not tileset name. Generate a unique Id:
                sourceId = String.valueOf(System.currentTimeMillis());
            }
        }
        return this.sourceId;
    }
    
    
    private String stringVal = null;
    @Override
    public String toString() {
        if (stringVal == null) {
            stringVal = mbt.getMetadata().getTilesetDescription();
            if (stringVal == null) {
                stringVal = this.getSourceId();
            }
        }
        return stringVal;
    }


    private int minZoom = -1;
    
    @Override
    public int getMinZoom() {
        if (minZoom == -1) {
            mbt.getMinZoom();
        }
        return minZoom;
    }

    
    private int maxZoom = -1;
        
    @Override
    public int getMaxZoom() {
        if (maxZoom == -1) {
            maxZoom = mbt.getMaxZoom();
        }
        return maxZoom;
    }
    
}
