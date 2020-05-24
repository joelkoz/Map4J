package org.map4j.coordinates;


/**
 * An interface implemented by any object that can be expressed
 * as a point in the tile coordinate system.
 * <p>Similar to pixel space, Tile Space represents the actual tile column and tile
 * row number.  The "origin" of Tile Space is (0,0). where the column zero is the left most tile column.
 * For the ROW, however, there are two standards: 
 * <ol><li>"XYZ", used by Google, Bing, OpenStreetMap, etc. places
 * row zero at the TOP of the map with increasing numbers going downward.</li>
 * <li>"TMS" places the zero row at the BOTTOM of the map with increasing numbers going upward.  TMS is used
 * in the popular storage file format MBTiles.</li>
 * </ol>
 *  
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
