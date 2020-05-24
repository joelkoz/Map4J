package org.map4j.layers;

import java.util.ArrayList;
import java.util.List;


/**
 * A MapLayerNode is a collection of MapLayerNodes, which are either map objects,
 * or other MapLayers.
 * 
 * @author Joel Kozikowski
 */
public class MapLayer extends MapLayerNode {

    private List<MapLayerNode> children;
    
    public MapLayer(String name) {
        this(name, (String) null);
    }

    public MapLayer(String name, String description) {
        this(name, description, MapMarkerCircle.getDefaultStyle());
    }

    public MapLayer(String name, Style style) {
        this(name, null, style);
    }

    public MapLayer(String name, String description, Style style) {
        this(null, null, name, description, style);
    }

    public MapLayer(MapLayer parent, String name) {
        this(parent, name, MapMarkerCircle.getDefaultStyle());
    }

    public MapLayer(MapLayer parent, String name, Style style) {
        this(parent, null, name, null, style);
    }
    
    public MapLayer(MapLayer parent, IMapObject mapObject, String name, String description, Style style) {
        super(parent, null, name, description, style);
        children = null;
        if (parent != null) parent.add(this);
    }
    
    
    @Override
    public boolean isMapObject() {
        return false;
    }
    
    @Override
    public IMapObject getMapObject() {
        return null;
    }

    
    @Override
    public void setMapObject(IMapObject mapObject) {
        throw new RuntimeException("Only MapLayerNodes are allowed to be assigned mapObjects.");
    }
    
    
    public boolean hasChildren() {
        return (children != null && children.size() > 0);
    }
    
    
    public List<MapLayerNode> getChildren() {
        if (children == null) {
            children = new ArrayList<MapLayerNode>();
        }
        return children;
    }

    
    /**
     * Adds a new marker set with the name markerSetName 
     * as a child of this marker set.
     */
    public MapLayer add(String markerSetName) {
        new MapLayer(this, markerSetName);
        return this;
    }
    
    
    /**
     * Adds the specified marker set as a child of this marker set
     * @return
     */
    public MapLayer add(MapLayerNode layerNode) {
        getChildren().add(layerNode);
        layerNode.setParent(this);
        return this;
    }
    
    
    public MapLayer add(IMapObject mapObject) {
        new MapLayerNode(this, mapObject);
        return this;
    }
        
}
