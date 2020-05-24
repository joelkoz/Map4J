package org.map4j.utils;

import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;
import org.map4j.utils.mbtiles.MBTile;
import org.map4j.utils.mbtiles.MBTileIterator;
import org.map4j.utils.mbtiles.MBTilesFile;

/**
 * Creates a "zoom level" in an MBTiles file out of pre-existing
 * zoom level.
 * @author Joel Kozikowski
 */
public class MakeMBTilesZoomLevel implements Runnable {

    private int sourceZoom;
    private int targetZoom;
    private MBTilesFile mbt;
    private int creationCount;
    
    public MakeMBTilesZoomLevel(String mbTilesName, int sourceZoom, int targetZoom) throws Exception {
        this.sourceZoom = sourceZoom;
        this.targetZoom = targetZoom;
        if (this.targetZoom <= this.sourceZoom) {
            throw new RuntimeException("MakeMBTilesZoomLevel does not currently support zoom OUT generation.");
        }
        mbt = new MBTilesFile(mbTilesName);
    }

    
    private void scaleTileUp(MBTile mbTile) {

        if (mbTile.getZoom() != sourceZoom) {
            throw new RuntimeException("Tile zoom does not match sourceZoom");
        }
        
        System.out.println("Processing tile " + mbTile.getZoom() + "/" + mbTile.getColumn() + "/" + mbTile.getRow());

        int magnification = targetZoom - sourceZoom;
        if (magnification <= 0) {
            throw new RuntimeException("targetZoom (" + targetZoom + ") must be greater than sourceZoom (" + sourceZoom + ")");
        }

        int magFactor = (1 << magnification);
        int masterTileSize = 256 * magFactor;

        // Size up the source image...
        BufferedImage masterImage = Scalr.resize(mbTile.getImage(), Scalr.Method.ULTRA_QUALITY, masterTileSize, masterTileSize);
        
        int newOriginX = mbTile.getColumn() * magFactor;
        int newOriginY = mbTile.getRow() * magFactor;

        // Now, slice it up and save as new tiles on the target level...
        for (int r = 0; r < magFactor; r++) {
            for (int c = 0; c < magFactor; c++) {
                int subX = c * 256;
                int subY = r * 256;
                BufferedImage subImg = masterImage.getSubimage(subX, subY, 256, 256);
                int subCol = newOriginX + c;
                int subRow = newOriginY + magFactor - r - 1;
                
                System.out.println("   Saving subimage (" + c + "," + r + ") to " + targetZoom + "/" + subCol + "/" + subRow);
                mbt.addTile(subImg, targetZoom, subCol, subRow);
                this.creationCount++;
            } // for r
        } // for c
    }

    
    public void run() {
        System.out.println("Running zoom level maker");
        long start = System.currentTimeMillis();
        this.creationCount = 0;
        
        MBTileIterator iter = mbt.getTiles(this.sourceZoom);
        int tileCount = 0;
        while (iter.hasNext()) {
            MBTile mbTile = iter.next();
            if (mbTile.getZoom() == this.sourceZoom) {
                // A bug in iter causes us to start traversing other rows in the database. 
                // Once they don't match, stop processing.
                tileCount++;
                scaleTileUp(mbTile);
            }
        }
        
        System.out.println("Examined " + tileCount + " tiles.");
        System.out.println("Create " + creationCount + " new tiles.");
        System.out.println("Updating min/max zoom levels...");
        mbt.updateMaxZoom();
        mbt.updateMinZoom();
        mbt.close();
        System.out.println("Done.");
        long ttl = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Run completed in " + ttl / 60.0 + " minutes");
    }
    
    
    public static BufferedImage saveTileToFile(MBTile mbTile, int magnification) {

        int newSize = 256 * (1 << magnification);
        BufferedImage img2 = Scalr.resize(mbTile.getImage(), Scalr.Method.ULTRA_QUALITY, newSize);
        
        String fileName = "after-m" + magnification + ".png";
        System.out.println("Saving " + fileName);
        ImageUtils.savePng(img2, fileName);
        
        return img2;
    }

    
    public static void main(String[] args) throws Exception {
        
        final String mbTilesName = "NOAA Raster South Florida Sonar.mbtiles";
        
        MakeMBTilesZoomLevel makeZoom = new MakeMBTilesZoomLevel(mbTilesName, 15, 16);
        makeZoom.run();

        System.exit(0);
    }
    
}
