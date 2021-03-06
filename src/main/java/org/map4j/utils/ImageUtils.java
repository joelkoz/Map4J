package org.map4j.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {

    public static BufferedImage load(String fileName) {
        BufferedImage i = null;

        try {
            i = ImageIO.read(new File(fileName));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return i;
    }


    /**
     * Returns the specified image as an array of bytes that represents
     * the image as a PNG file. This is useful when storing images in
     * a database.  null is returned if the conversion could not be done.
     */
    public static byte[] getImageAsPng(BufferedImage image) {
        return getImageAs(image, "PNG");
    }
    
    
    
    public static boolean savePng(BufferedImage image, String fileName) {
        return saveAs(image, fileName, "PNG");
    }



    /**
     * Returns the specified image as an array of bytes that represents
     * the image as a JPG file. This is useful when storing images in
     * a database.  null is returned if the conversion could not be done.
     */
    public static byte[] getImageAsJpg(BufferedImage image) {
        return getImageAs(image, "JPG");
    }
    
    
    
    public static boolean saveJpg(BufferedImage image, String fileName) {
        return saveAs(image, fileName, "JPG");
    }


    
    
    /**
     * Returns the specified image as an array of bytes that represents
     * the image formatted as formatName. This is useful when storing images in
     * a database.  null is returned if the conversion could not be done.
     */
    public static byte[] getImageAs(BufferedImage image, String formatName) {
        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ImageIO.write(image, formatName, out);
                return out.toByteArray();
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }        
    }
    
    
    public static boolean saveAs(BufferedImage image, String fileName, String formatName) {
        boolean saved = false;
        try {
            ImageIO.write(image, formatName, new FileOutputStream(new File(fileName)));
            saved = true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return saved;
    }

    
    
    /**
     * Rotates an arbitrary image of an arbitrary dimension by degRotation degrees.
     * The returned image will be sized to be able to hold the rotated source image in its
     * entirety.
     * @param sourceImg The source image to rotate
     * @param degRotation The rotation angle, in degrees
     * @return A copy of sourceImg, rotated by the requested amount.
     */
    public static BufferedImage rotateImage(BufferedImage sourceImg, double degRotation) {

        double rads = Math.toRadians(degRotation);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = sourceImg.getWidth();
        int h = sourceImg.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);
        int centerX = w / 2;
        int centerY = h / 2;

        BufferedImage targetImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        AffineTransform tx = new AffineTransform();
        tx.translate((newWidth - w) / 2, (newHeight - h) / 2);
        tx.rotate(rads, centerX, centerY);
        
        Graphics2D g2d = targetImg.createGraphics();
        g2d.setTransform(tx);
        g2d.drawImage(sourceImg, 0, 0, null);
        g2d.dispose();

        return targetImg;
    }
    

    
    /**
     * "Dyes" the specified image with the specified color. The dye is applied to
     * the entire image
     * @param image The source image
     * @param color The "dye" color to use on the source image. It is important 
     *     that this color's alpha channel be transparent to some degree
     * @return A new buffered image with the dye color applied. The original image is unmodified
     * @see #dye(BufferedImage, Color, int, int, int, int)
     */
    public static BufferedImage dye(BufferedImage image, Color dyeColor) {
        return dye(image, dyeColor, 0, 0, image.getWidth(), image.getHeight());
    }
    
    
    /**
     * "Dyes" the specified image with the specified color. This is done by
     * doing an AlphaComposite over the image using dyeColor. 
     * @param image The source image
     * @param color The "dye" color to use on the source image. It is important 
     *     that this color's alpha channel be transparent to some degree
     * @param x x coordinate of rectangle in image to apply dye to
     * @param y y coordinate of rectangle in image to apply dye to
     * @param w width of rectangle in image to apply dye to
     * @param h height of rectangle in image to apply dye to
     * @return A new buffered image with the dye color applied. The original image is unmodified
     */
    public static BufferedImage dye(BufferedImage image, Color dyeColor, int x, int y, int w, int h)
    {
        BufferedImage dyed = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dyed.createGraphics();
        g.drawImage(image, 0,0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(dyeColor);
        g.fillRect(x,y,w,h);
        g.dispose();
        return dyed;
    }    

    
    
    /**
     * Loads an image resource from path where path is the
     * complete path, starting from root. This is the best
     * way to access resources in the "src/main/resources" package
     */
    public static BufferedImage loadResourceImage(String path) {
        return loadResourceImage(ImageUtils.class, path);
    }
    
    
    
    /**
     * Loads an image resource using a resource loader, where path is the
     * path relative to the specified resourceRoot class.
     */
    public static BufferedImage loadResourceImage(Class<?> resourceRoot, String path) {
        try {
            return ImageIO.read(resourceRoot.getResourceAsStream(path));
        } catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}
