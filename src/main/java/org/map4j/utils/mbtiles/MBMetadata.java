/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */
package org.map4j.utils.mbtiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A container for the metadata table
 */
public class MBMetadata {
    
    
    public static class CoordinateBox {
        private double left;
        private double bottom;
        private double right;
        private double top;

        public CoordinateBox(String serialized) {
            String[] split = serialized.split(",");
            double left = Double.parseDouble(split[0]);
            double bottom = Double.parseDouble(split[1]);
            double right = Double.parseDouble(split[2]);
            double top = Double.parseDouble(split[3]);
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.top = top;
            if (left < -180) {
                this.left = -180;
            }
            if (bottom < -85) {
                this.bottom = -85;
            }
            if (right > 180) {
                this.right = 180;
            }
            if (top > 85) {
                this.top = 85;
            }
        }

        public CoordinateBox(double left, double bottom, double right, double top) {
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.top = top;
            if (left < -180) {
                this.left = -180;
            }
            if (bottom < -85) {
                this.bottom = -85;
            }
            if (right > 180) {
                this.right = 180;
            }
            if (top > 85) {
                this.top = 85;
            }
        }

        @Override
        public String toString() {
            return left + "," + bottom + "," + right + "," + top;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null) {
                return obj.toString().equals(this.toString());
            }
            return false;
        }

        public double getLeft() {
            return left;
        }

        public double getBottom() {
            return bottom;
        }

        public double getRight() {
            return right;
        }

        public double getTop() {
            return top;
        }
    }    
    
    //these are the key-value pairs for required fields
    private Map<String, String> keyValuePairs = new HashMap<>();
    //these are the key-value pairs for custom fields
    private Map<String, String> customPairs = new HashMap<>();
    //these are the keys for required fields
    private static Set<String> requiredKeys = null;

    
    /**
     * Construct an empty MBMetadata set to be filled in from reading the file
     */
    protected MBMetadata() {
        initRequiredKeys();
    }

    
    /**
     * Initialize a metadata entry, accepting default bounds and attribution fields
     *
     * @param name        The plain-english name of the tileset.
     * @param type        overlay or baselayer
     * @param version     The version of the tileset, as a plain number.
     * @param description A description of the layer as plain text.
     * @param mimeType    The image file format of the tile data: png or jpg
     */
    public MBMetadata(String name, TileSetType type, String version, String description, TileMimeType mimeType) {
        initRequiredKeys();
        setTilesetName(name);
        setTilesetType(type);
        setTilesetVersion(version);
        setTilesetDescription(description);
        setTileMimeType(mimeType);
    }

    /**
     * Initialize a metadata entry, accepting default attribution fields
     *
     * @param name        The plain-english name of the tileset.
     * @param type        overlay or baselayer
     * @param version     The version of the tileset, as a plain number.
     * @param description A description of the layer as plain text.
     * @param mimeType    The image file format of the tile data: png or jpg
     * @param bounds      The maximum extent of the rendered map area.
     *                    Bounds must define an area covered by all zoom levels.
     *                    The bounds are represented in WGS:84 - latitude and longitude values,
     *                    in the OpenLayers Bounds format - left, bottom, right, top.
     *                    Example of the full earth: -180.0,-85,180,85.
     */
    public MBMetadata(String name, TileSetType type, String version, String description, TileMimeType mimeType, CoordinateBox bounds) {
        initRequiredKeys();
        setTilesetName(name);
        setTilesetType(type);
        setTilesetVersion(version);
        setTilesetDescription(description);
        setTileMimeType(mimeType);
        setTilesetBounds(bounds);
    }

    /**
     * Initialize a metadata entry, accepting default bounds fields
     *
     * @param name        The plain-english name of the tileset.
     * @param type        overlay or baselayer
     * @param version     The version of the tileset, as a plain number.
     * @param description A description of the layer as plain text.
     * @param mimeType    The image file format of the tile data: png or jpg
     * @param attribution An attribution string, which explains in English (and HTML)
     *                    the sources of data and/or style for the map.
     */
    public MBMetadata(String name, TileSetType type, String version, String description, TileMimeType mimeType, String attribution) {
        initRequiredKeys();
        setTilesetName(name);
        setTilesetType(type);
        setTilesetVersion(version);
        setTilesetDescription(description);
        setTileMimeType(mimeType);
        setAttribution(attribution);
    }

    /**
     * Initialize a metadata entry
     *
     * @param name        The plain-english name of the tileset.
     * @param type        overlay or baselayer
     * @param version     The version of the tileset, as a plain number.
     * @param description A description of the layer as plain text.
     * @param mimeType    The image file format of the tile data: png or jpg
     * @param bounds      The maximum extent of the rendered map area.
     *                    Bounds must define an area covered by all zoom levels.
     *                    The bounds are represented in WGS:84 - latitude and longitude values,
     *                    in the OpenLayers Bounds format - left, bottom, right, top.
     *                    Example of the full earth: -180.0,-85,180,85.
     * @param attribution An attribution string, which explains in English (and HTML)
     *                    the sources of data and/or style for the map.
     */
    public MBMetadata(String name, TileSetType type, String version, String description, TileMimeType mimeType, CoordinateBox bounds, String attribution) {
        initRequiredKeys();
        setTilesetName(name);
        setTilesetType(type);
        setTilesetVersion(version);
        setTilesetDescription(description);
        setTileMimeType(mimeType);
        setTilesetBounds(bounds);
        setAttribution(attribution);
    }

    
    /**
     * Set the name of this tile set
     *
     * @param name The plain-english name of the tileset.
     * @return this entry
     */
    public MBMetadata setTilesetName(String name) {
        addKeyValue("name", name);
        return this;
    }

    /**
     * @return The plain-english name of the tileset.
     */
    public String getTilesetName() {
        return keyValuePairs.get("name");
    }

    /**
     * @param type overlay or baselayer
     * @return this entry
     */
    public MBMetadata setTilesetType(TileSetType type) {
        addKeyValue("type", type.toString());
        return this;
    }

    /**
     * @return overlay or baselayer
     */
    public TileSetType getTilesetType() {
        String strValue = keyValuePairs.get("type");
        return TileSetType.getTypeFromString(strValue);
    }

    /**
     * @param version The version of the tileset, e.g (0.2.0)
     * @return this entry
     */
    public MBMetadata setTilesetVersion(String version) {
        addKeyValue("version", version);
        return this;
    }

    /**
     * @return The version of the tileset, e.g (0.2.0)
     */
    public String getTilesetVersion() {
        return keyValuePairs.get("version");
    }

    /**
     * @param description A description of the layer as plain text.
     * @return this entry
     */
    public MBMetadata setTilesetDescription(String description) {
        addKeyValue("description", description);
        return this;
    }

    /**
     * @return A description of the layer as plain text.
     */
    public String getTilesetDescription() {
        return keyValuePairs.get("description");
    }

    /**
     * @param fmt The image file format of the tile data: png or jpg
     * @return this entry
     */
    public MBMetadata setTileMimeType(TileMimeType fmt) {
        addKeyValue("format", fmt.toString());
        return this;
    }

    /**
     * @return The image file format of the tile data: png or jpg
     */
    public TileMimeType getTileMimeType() {
        return TileMimeType.getTypeFromString(keyValuePairs.get("format"));
    }

    /**
     * The maximum extent of the rendered map area.
     * Bounds must define an area covered by all zoom levels.
     * The bounds are represented in WGS:84 - latitude and longitude values,
     * in the OpenLayers Bounds format - left, bottom, right, top.
     * Example of the full earth: -180.0,-85,180,85.
     *
     * @param left   left, long
     * @param bottom bottom, lat
     * @param right  right , long
     * @param top    top,lat
     * @return this entry
     */
    public MBMetadata setTilesetBounds(double left, double bottom, double right, double top) {
        return setTilesetBounds(new CoordinateBox(left, bottom, right, top));
    }

    /**
     * @param bounds The maximum extent of the rendered map area.
     *               Bounds must define an area covered by all zoom levels.
     *               The bounds are represented in WGS:84 - latitude and longitude values,
     *               in the OpenLayers Bounds format - left, bottom, right, top.
     *               Example of the full earth: -180.0,-85,180,85.
     * @return this entry
     */
    public MBMetadata setTilesetBounds(CoordinateBox bounds) {
        addKeyValue("bounds", bounds.toString());
        return this;
    }

    /**
     * @return The maximum extent of the rendered map area.
     * Bounds must define an area covered by all zoom levels.
     * The bounds are represented in WGS:84 - latitude and longitude values,
     * in the OpenLayers Bounds format - left, bottom, right, top.
     * Example of the full earth: -180.0,-85,180,85.
     */
    public CoordinateBox getTilesetBounds() {
        return new CoordinateBox(keyValuePairs.get("bounds"));
    }

    /**
     * @param attribution An attribution string, which explains in English (and HTML)
     *                    the sources of data and/or style for the map.
     * @return this entry
     */
    public MBMetadata setAttribution(String attribution) {
        addKeyValue("attribution", attribution);
        return this;
    }

    /**
     * @return An attribution string, which explains in English (and HTML)
     * the sources of data and/or style for the map.
     */
    public String getAttribution() {
        return keyValuePairs.get("attribution");
    }


    /**
     * Returns the value of "minzoom" from the metadata, or -1 if no
     * such value is defined.
     */
    public int getMinZoom() {
        String val = getKeyValue("minzoom");
        if (val != null) {
            return Integer.valueOf(val);
        }
        else {
            return -1;
        }
    }


    public void setMinZoom(int zoom) {
        addKeyValue("minzoom", String.valueOf(zoom));
    }
    
    /**
     * Returns the value of "maxzoom" from the metadata, or -1 if no
     * such value is defined.
     */
    public int getMaxZoom() {
        String val = getKeyValue("maxzoom");
        if (val != null) {
            return Integer.valueOf(val);
        }
        else {
            return -1;
        }
    }

    
    public void setMaxZoom(int zoom) {
        addKeyValue("maxzoom", String.valueOf(zoom));
    }
    
    
    
    /**
     * @return a set of custom key-value pairs
     */
    public Set<Map.Entry<String, String>> getCustomKeyValuePairs() {
        return customPairs.entrySet();
    }

    /**
     * @return a set of the required key-value pairs, per the standard
     */
    public Set<Map.Entry<String, String>> getRequiredKeyValuePairs() {
        return keyValuePairs.entrySet();
    }

    
    /**
     * An agnostic key-value pair, it figures out whether the key is a required key, or a custom key
     *
     * @param name  the name of the agnostic key
     * @param value the value of the agnostic key
     */
    public void addKeyValue(String name, String value) {
        if (requiredKeys.contains(name)) {
            keyValuePairs.put(name, value);
        } else {
            customPairs.put(name, value);
        }
        dirty = true;
    }

    
    /**
     * Returns the value for the specified key name (regardless of whether or not it
     * is a required value or a custom value).
     */
    public String getKeyValue(String name) {
        String val = keyValuePairs.get(name);
        if (val == null) {
            val = customPairs.get(name);
        }
        return val;
    }
    
    
    protected boolean dirty = true;
    
    
    /**
     * Returns TRUE if this metadata has changed since being read from the file.
     * @return
     */
    public boolean isDirty() {
        return dirty;
    }
    
    
    private void initRequiredKeys() {
        if (requiredKeys == null) { 
            requiredKeys = new HashSet<>();
            requiredKeys.add("name");
            requiredKeys.add("type");
            requiredKeys.add("version");
            requiredKeys.add("description");
            requiredKeys.add("format");
            requiredKeys.add("bounds");
            requiredKeys.add("attribution");
        }
    }


    public enum TileMimeType {
        PNG,
        JPG;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public static TileMimeType getTypeFromString(String format) {
            for (TileMimeType t : TileMimeType.values()) {
                if (t.toString().equals(format)) {
                    return t;
                }
            }
            return null;
        }
    }

    public enum TileSetType {
        OVERLAY("overlay"),
        BASE_LAYER("baselayer");

        private String str;

        TileSetType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }

        public static TileSetType getTypeFromString(String strValue) {
            for (TileSetType t : TileSetType.values()) {
                if (t.str.equals(strValue)) {
                    return t;
                }
            }
            return null;
        }
    }

}
