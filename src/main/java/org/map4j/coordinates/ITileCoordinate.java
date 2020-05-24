package org.map4j.coordinates;


/**
 * An interface implemented by any object that can be expressed
 * as a point in the tile coordinate system.
 * @author Joel Kozikowski
 */
public interface ITileCoordinate {

    /**
     * Returns the column value of this coordinate in tile space.
     */
    public int getCol();
    
    
    /**
     * Sets the column value of this coordinate in tile space to newCol.
     */
    public void setCol(int newCol);
    
    
    /**
     * Returns the row value of this coordinate in tile space.
     */
    public int getRow();
    
    
    /**
     * Returns the row value of this coordinate in tile space. If useXYZ
     * is TRUE, the row will be in XYZ space, otherwise it is assumed to
     * be in TMS space.
     * @param useXYZ
     */
    public int getRow(boolean useXYZ);

    
    /**
     * Sets the row value of this coordinate in tile space to newRow.
     */
    public void setRow(int newRow);

    
    /**
     * Returns the zoom level of the tile space this coordinate lies within.
     */
    public int getZoom();
}
