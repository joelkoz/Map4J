package org.map4j.utils.mbtiles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import org.map4j.utils.ImageUtils;


/**
 * Represents a file stored according to the MBTiles specification.
 * 
 * Code here is borrowed and reworked from the org.imintel.mbtiles4j project
 * 
 * @author Joel Kozikowski
 *
 */
public class MBTilesFile {

    Connection connection;
    private File file;
    private boolean newFile = false;
    MBMetadata mbMeta = null;

    public MBTilesFile(File f) {
        try {
            file = f;
            this.newFile = !f.exists();
            establishConnection(f);
            if (this.newFile) {
                init();
            }
        }
        catch (Exception e) {
            throw new MBTilesException(e);
        }
    }

    
    public MBTilesFile() {
        try {
            this.newFile = true;
            file = File.createTempFile(UUID.randomUUID().toString(), ".mbtiles");
            establishConnection(file);
            init();
        }
        catch (Exception e) {
            throw new MBTilesException(e);
        }
    }

    
    public MBTilesFile(String name) {
        try {
            File file = new File(name);
            this.newFile = file.createNewFile();
            establishConnection(file);
            if (this.newFile) {
               init();
            }
        }
        catch (Exception e) {
            throw new MBTilesException(e);
        }
    }

    
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }


    /**
     * Returns TRUE if this MBTiles file had to be created in the file system to accommodate the
     * open request.
     */
    public boolean isNew() {
        return this.newFile;
    }
    
    
    
    private void init() throws SQLException {
        createTable("metadata", "(name text,value text)", "CREATE UNIQUE INDEX name on metadata (name);");
        createTable("tiles", "(zoom_level integer, tile_column integer, tile_row integer, tile_data blob)", "CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row);");
    }

    
    /**
     * Retrieves the metadata for this file.
     * @throws MBTilesException
     */
    public MBMetadata getMetadata() throws MBTilesException {
        if (mbMeta == null) { 
            String sql = "SELECT * from metadata;";
            try (Statement stmt = connection.createStatement()) {
                ResultSet resultSet = stmt.executeQuery(sql);
                mbMeta = new MBMetadata();
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String value = resultSet.getString("value");
                    mbMeta.addKeyValue(name, value);
                }
                mbMeta.dirty = false;
            } 
            catch (SQLException e) {
                throw new MBTilesException("Get Metadata failed", e);
            }
        }
        return mbMeta;
    }

    
    /**
     * Adds or updates the specified metadata to the file
     */
    public void updateMetadata(MBMetadata ent) throws MBTilesException {
        for (Map.Entry<String, String> metadata : ent.getRequiredKeyValuePairs()) {
            String schema = "(name,value)";
            String values = "('" + metadata.getKey() + "','" + metadata.getValue() + "')";
            try {
                update("metadata", schema, values);
            } 
            catch (SQLException e) {
                throw new MBTilesException("Add metadata failed.", e);
            }
        }
        
        for (Map.Entry<String, String> metadata : ent.getCustomKeyValuePairs()) {
            String schema = "(name,value)";
            String values = "('" + metadata.getKey() + "','" + metadata.getValue() + "')";
            try {
                update("metadata", schema, values);
            } 
            catch (SQLException e) {
                throw new MBTilesException("Add metadata failed.", e);
            }
        }
        
        this.mbMeta = ent;
        this.mbMeta.dirty = false;
    }

        
    private byte[] toByteArray(InputStream is) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
          os.write(data, 0, nRead);
        } // while

        return os.toByteArray();
    }
    
    
    public void addTile(InputStream tileIs, long zoom, long column, long row) throws MBTilesException {
        try {
            byte[] bytes = toByteArray(tileIs);
            addTile(bytes, zoom, column, row);
        } 
        catch (IOException e) {
            throw new MBTilesException("Add Tile Failed.", e);
        }
    }

    
    public void addTile(BufferedImage img, long zoom, long column, long row) throws MBTilesException {
        byte[] bytes = ImageUtils.getImageAsPng(img);
        if (bytes == null) {
            throw new MBTilesException("Could not convert image to PNG");
        }
        addTile(bytes, zoom, column, row);
    }

    
    
    public void addTile(byte[] bytes, long zoom, long column, long row) throws MBTilesException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO tiles (zoom_level,tile_column,tile_row,tile_data) VALUES(?,?,?,?)")) {
            stmt.setInt(1, (int) zoom);
            stmt.setInt(2, (int) column);
            stmt.setInt(3, (int) row);
            stmt.setBytes(4, bytes);
            stmt.execute();
        } 
        catch (SQLException e) {
            throw new MBTilesException("Add Tile to MBTiles file failed", e);
        }
    }

    
    
    public void addTile(File f, long zoom, long column, long row) throws MBTilesException {
        try {
            addTile(new FileInputStream(f), zoom, column, row);
        } 
        catch (FileNotFoundException e) {
            throw new MBTilesException("Add tile failed. No file found.", e);
        }
    }

    
    /**
     * Commits any cached metadata back to the file.
     */
    public void flush() throws MBTilesException {
        if (mbMeta != null && mbMeta.isDirty()) {
            this.updateMetadata(mbMeta);
        }
    }

    
    /**
     * Closes down the file by flushing any metadata then
     * releasing the sql connection
     * @throws MBTilesException
     */
    public void close() throws MBTilesException {
        try {
            this.flush();
            connection.close();
        } 
        catch (SQLException e) {
        }
    }

    
   
    /**
     * Returns a tile iterator for the specified zoom level. If the zoom level is
     * -1, all tiles in the file will be returned.
     */
    public MBTileIterator getTiles(int zoom) throws MBTilesException {
        String sql = "SELECT * from tiles;";
        if (zoom >= 0) {
            sql = sql + " WHERE zoom_level = " + zoom;
        }
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery(sql);
            return new MBTileIterator(stmt, resultSet);
        } 
        catch (SQLException e) {
            throw new MBTilesException("Access Tiles failed", e);
        }
    }

    
    /**
     * Returns a tile at the specified zoom level. If no such tile exists, 
     * NULL is returned. 
     * @throws MBTilesException
     */
    public MBTile getTile(int zoom, int column, int row) throws MBTilesException {
        String sql = String.format("SELECT tile_data FROM tiles WHERE zoom_level = %d AND tile_column = %d AND tile_row = %d", zoom, column, row);
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next()) {
                byte[] bytes = toByteArray(resultSet.getBinaryStream("tile_data"));
                return new MBTile(zoom, column, row, bytes);
            }
            else {
                return null;
            }
        } 
        catch (Exception e) {
            throw new MBTilesException(String.format("Could not get Tile for z:%d, column:%d, row:%d", zoom, column, row), e);
        }
    }
    
    
    /**
     * Returns TRUE if this file contains at least one tile at the specified zoom level
     */
    public boolean hasTile(int zoom) {
        
        String sql = String.format("SELECT zoom_level FROM tiles WHERE zoom_level = %d LIMIT 1", zoom);
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next()) {
                return true;
            }
            else {
                return false;
            }
        } 
        catch (Exception e) {
            throw new MBTilesException(String.format("Could not check for zoom level z:%d", zoom), e);
        }
    }


    
    /**
     * Forces an update of the maxzoom attribute based on what is currently stored in the file.
     * The new value is returned.
     */
    public int updateMaxZoom() throws MBTilesException {
        // No definition in the metadata.  Calculate it from the tile entries...
        String sql = "SELECT MAX(zoom_level) FROM tiles";
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            int maxZoom = resultSet.getInt(1);
            getMetadata().setMaxZoom(maxZoom);
            return maxZoom;
        } 
        catch (SQLException e) {
            throw new MBTilesException("Could not get max zoom", e);
        }
    }
    
    /**
     * Returns the maximum zoom level supported in this file
     */
    public int getMaxZoom() throws MBTilesException {
        int maxZoom = getMetadata().getMaxZoom();
        if (maxZoom < 0) {
            maxZoom = updateMaxZoom();
        }
        return maxZoom;
    }


    /**
     * Forces an update of the maxzoom attribute based on what is currently stored in the file.
     * The new value is returned.
     */
    public int updateMinZoom() throws MBTilesException {
        // No definition in the metadata.  Calculate it from the tile entries...
        String sql = "SELECT MIN(zoom_level) FROM tiles";
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            int minZoom = resultSet.getInt(1);
            getMetadata().setMinZoom(minZoom);
            return minZoom;
        } 
        catch (SQLException e) {
            throw new MBTilesException("Could not get max zoom", e);
        }
    }

    
    /**
     * Returns the maximum zoom level supported in this file
     */
    public int getMinZoom() throws MBTilesException {
        int minZoom = getMetadata().getMinZoom();
        if (minZoom < 0) {
            minZoom = updateMinZoom();
        }
        return minZoom;
    }
    
    
    private Connection establishConnection(File file) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        return connection;
    }
    
    
    private void createTable(String tableName, String schema, String... onSuccess) throws SQLException {
        if (!tableExists(tableName)) {
            String sql = "CREATE TABLE  " + tableName + schema + ";";
            execute(sql);
            for (String cmd : onSuccess) {
                execute(cmd);
            }
        }
    }

    
    private void execute(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
           stmt.execute(sql);
        }
    }

    
    private void update(String tableName, String schema, String values) throws SQLException {
        execute("INSERT OR REPLACE INTO " + tableName + " " + schema + " VALUES " + values + ";");
    }

    
    private boolean tableExists(String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            boolean tableExists = resultSet.next();
            return tableExists;
        }
    }
    

}
