package org.map4j.render;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.map4j.coordinates.PBox;
import org.map4j.coordinates.PCoordinate;


/**
 * Instances of MapImage holds an actual buffered image for a specific
 * location in pixel space. The map image is always square, and is
 * large enough to be rotated and fill the display area completely.
 * The center of the image is the current location of the moving map.
 * <p>This is a container class for the MovingMapRenderer
 * 
 * @author Joel Kozikowski
 */
public class DisplayImage {

    private PBox pLocation;
    private BufferedImage image;
    private BufferedImage rotImage;
    private int rotHeading = -999;
    private int displayWidth;
    private int displayHeight;

    protected DisplayImage(PBox pLocation, BufferedImage image, int displayWidth, int displayHeight) {
        this.pLocation = pLocation;
        this.image = image;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
    }

    
    Point getCenter() {
        Point center = new Point();
        center.x = image.getWidth() / 2;
        center.y = image.getHeight() / 2;
        return center;
    }
    
    
    PBox getLocation() {
        return this.pLocation;
    }
    
    
    BufferedImage getImage() {
        return this.image;
    }
    
    
    public int getWidth() {
        return image.getWidth();
    }
    
    
    public int getHeight() {
        return image.getHeight();
    }
    
    
    /**
     * Returns the x,y coordinate of the point on this MapImage
     * that corresponds to the specified pixel coordinate. If
     * coord is not located on this map, NULL is returned. 
     */
    Point getPoint(PCoordinate coord) {

        if (coord != null && this.pLocation.contains(coord)) {
            int x = coord.getPixelX() - pLocation.p1.getPixelX();
            int y = coord.getPixelY() - pLocation.p1.getPixelY();
            return new Point(x,y);
        }
        else {
            return null;
        }
    }

    
    /**
     * Returns the pixel coordinate of the specified point on this
     * map.  If pt does not reside on this map, NULL is returned.
     */
    PCoordinate getPCoord(Point pt) {
        if (pt != null && 
            pt.x >= 0 && pt.x < this.getWidth() &&
            pt.y >= 0 && pt.y < this.getHeight()) {
            int px = pLocation.p1.getPixelX() + pt.x;
            int py = pLocation.p1.getPixelY() + pt.y;
            return new PCoordinate(px, py, pLocation.p1);
        }
        else {
            return null;
        }
    }
    
    
    /**
     * Draws this image in the upper left hand corner of the specified graphics context, 
     * rotated to have the specified heading at the top image position.
     * @param degHeading A heading, in degrees, between 0 (North) and 359 (North North West). 90 is East, 180 South, etc.
     */
    public void drawImageHeadsUp(int degHeading, Graphics2D g2d) {
        
        Point imgCenter = this.getCenter();
        Point displayCenter = new Point(this.displayWidth / 2, this.displayHeight / 2);
        
        if (degHeading != rotHeading) {

            // Calculate the rotation angle needed to make the image "heading up"
            double radRotate = Math.toRadians(360 - degHeading);
            AffineTransform txRot = new AffineTransform();
            txRot.rotate(radRotate, imgCenter.x, imgCenter.y);

            // Rotate the image...
            rotImage = new BufferedImage(pLocation.getWidth(), pLocation.getHeight(), this.image.getType());
            Graphics2D g2dRot = rotImage.createGraphics();
            AffineTransform beforeRotTx = g2dRot.getTransform();
            g2dRot.setTransform(txRot);
            g2dRot.drawImage(this.image,  0,  0,  null);
            g2dRot.setTransform(beforeRotTx);
            g2dRot.dispose();
            
            rotHeading = degHeading;
        }

        // Render the rotated image onto the display, translating the center of the image to the center
        // of the display
        Point offsetUL = new Point(imgCenter.x - displayCenter.x, imgCenter.y - displayCenter.y);
        
        AffineTransform txTrans = new AffineTransform();
        txTrans.translate(-offsetUL.x, -offsetUL.y);

        AffineTransform oldG2dTx = g2d.getTransform();
        g2d.setTransform(txTrans);
        
        g2d.drawImage(rotImage, 0, 0, null);
        
        g2d.setTransform(oldG2dTx);
    }
    
}
