package org.map4j.utils.mbtiles;

import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MBTile {
    private int zoom;
    private int column;
    private int row;
    private byte[] data;

    public MBTile(int zoom, int column, int row, byte[] tile_data) {
        this.zoom = zoom;
        this.column = column;
        this.row = row;
        this.data = tile_data;
    }

    public byte[] getData() {
        return data;
    }

    
    public BufferedImage getImage() throws MBTilesException {
        try {
           return ImageIO.read(new ByteArrayInputStream(data));
        }
        catch (IOException ex){
            throw new MBTilesException("Error converting image data. ", ex); 
        }
    }
    
    public int getZoom() {
        return zoom;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }
}