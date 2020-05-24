package org.map4j.layers;


/**
 * A class that visits all of the map objects that are visible
 * in a particular map object tree.
 * 
 * @author Joel Kozikowski
 */
public abstract class VisibleMapObjectVisitor implements Runnable {

    private MapLayer root;
    
    public VisibleMapObjectVisitor(MapLayer root) {
        this.root = root;
    }

    public void run() {
        visitVisible(root);
    }
    
    private void visitVisible(MapLayer layer) {

        if (layer != null && layer.isVisible() && layer.hasChildren()) {

            for (MapLayerNode node : layer.getChildren()) {
                
                if (node.isMapObject()) {
                    IMapObject mapObject = node.getMapObject();
                    if (mapObject.isVisible()) {
                        visit(mapObject);
                    }
                }
                else if (node instanceof MapLayer) {
                    visitVisible((MapLayer)node);
                }
                
            }
        } // if group.isVisibile
    } // visitVisible()
    
    
    abstract public void visit(IMapObject mapObject);
    
}
