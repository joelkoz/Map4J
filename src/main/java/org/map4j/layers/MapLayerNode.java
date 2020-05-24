package org.map4j.layers;

import java.util.List;

/**
 * A MapLayerNode is the basic member of a MapLayer collection. It usually holds a single
 * map object. It also serves as the base class of a MapLayer, which is a collection of MapLayerNodes
 * 
 * @author Joel Kozikowski
 */
public class MapLayerNode {

    private IMapObject mapObject;

    private MapLayer parent;
    private String name;
    private String description;
    private Style style;
    private boolean visible = true;
    private boolean visibleTexts = true;

    public MapLayerNode(IMapObject mapObject) {
        this(null, mapObject, mapObject.getName(), null, MapMarkerCircle.getDefaultStyle());
    }
    
    public MapLayerNode(MapLayer parent, IMapObject mapObject) {
        this(parent, mapObject, null, null, MapMarkerCircle.getDefaultStyle());
        
    }
    
    public MapLayerNode(MapLayer parent, IMapObject mapObject, String name, String description, Style style) {
        
        this.mapObject = mapObject;
        setParent(parent);
        setName(name);
        setDescription(description);
        setStyle(style);
        setVisible(Boolean.TRUE);

        if (parent != null) parent.add(this);
    }
    
    
    public boolean isMapObject() {
        return (mapObject != null);
    }
    
    
    public IMapObject getMapObject() {
        return mapObject;
    }

    
    public void setMapObject(IMapObject mapObject) {
        this.mapObject = mapObject;
    }
    
    
    public boolean hasChildren() {
        return false;
    }
    
    
    public List<MapLayerNode> getChildren() {
        return null;
    }

    
    public MapLayer getParent() {
        return parent;
    }

    
    public void setParent(MapLayer parent) {
        this.parent = parent;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Style getStyle() {
        return style;
    }

    
    public void setStyle(Style style) {
        this.style = style;
    }

    
    public boolean isVisible() {
        return visible;
    }

    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    
    public Boolean isVisibleTexts() {
        return visibleTexts;
    }

    
    public void setVisibleTexts(Boolean visibleTexts) {
        this.visibleTexts = visibleTexts;
    }

    
    @Override
    public String toString() {
        return name;
    }
    
}
