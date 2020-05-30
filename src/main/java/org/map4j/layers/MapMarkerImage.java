package org.map4j.layers;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

/** 
 * A MapMarkerImage represents an image marker that can be painted on the map.
 * A marker image is square image with a pixel size specified in the constructor
 * of the image.  Marker image sources can be an optional grid of images with 
 * the marker being an index into that grid.
 * 
 * @author Joel Kozikowski
 */
public class MapMarkerImage extends MapMarkerCircle {

    private BufferedImage markerImage;
    

    /**
     * Constructs a map marker image, located at the specified coordinates, where
     * markerImage makes up the entire image to be rendered on the map. The marker
     * image will be rendered with its center at (lat,lon)
     */
    public MapMarkerImage(BufferedImage markerImage, String name, double lat, double lon) {
        super(name, lat, lon, Math.max(markerImage.getWidth(), markerImage.getHeight()));
        this.markerImage = markerImage;
    }



    /**
     * Constructs a map maker image where the marker image is taken from sourceImage, assumed
     * to be a grid of rectangular tiles imageWidth x imageHeight, with the desired image 
     * located at the index imageIndex. The index is calculated as so:<p>
     * <code>
     *   index = rowNumber * numberOfColumns + columnNumber;
     * </code>
     * where column zero, row zero is in the upper left hand corner of the source image.
     */
    public MapMarkerImage(BufferedImage imageSource, int imageWidth, int imageHeight, int imageIndex, String name, double lat, double lon) {
        super(name, lat, lon, Math.max(imageWidth, imageHeight));
        
        int numOfColumns = imageSource.getWidth() / imageWidth;
        int row = imageIndex / numOfColumns;
        int col = imageIndex % numOfColumns;
        
        this.markerImage = imageSource.getSubimage(col * imageWidth, row * imageHeight, imageWidth, imageHeight);
    }

    
    @Override
    protected void paint(Graphics g, Point position, int drawRadius) {
        g.drawImage(this.markerImage, position.x-drawRadius,  position.y-drawRadius, null);
        if (getLayer() == null || getLayer().isVisibleTexts()) paintName(g, position);
    }    
}
