package demo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.map4j.swing.Map4JPanel;


/**
 * The main panel of the navigation UI. Demonstrates how to
 * embed the Map4JPanel Swing component into a Swing Split Pane
 */
public class DemoUIPanel extends JPanel {
    
    /** Serial Version UID */
    private static final long serialVersionUID = 3050203054402323973L;

    private Map4JPanel map;
    
    private JPanel menuPanel;
    
    private JSplitPane splitPane;

    public DemoUIPanel() {
        this(false);
    }

    public DemoUIPanel(boolean splitVisible) {
        super();
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        menuPanel = new JPanel();

        map = new Map4JPanel(15);

        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(800 - 150);

        map.setMinimumSize(new Dimension(500, 600));
        
        setLayout(new BorderLayout());
        
        setSplitVisible(splitVisible);
       
    }
    
    public Map4JPanel getMapPanel() {
        return map;
    }
    
    public void setSplitVisible(boolean visible) {
        removeAll();
        revalidate();
        if (visible) {
            // Menu panel is visible: display window as split pane
            splitPane.setLeftComponent(map);
            splitPane.setRightComponent(menuPanel);
            add(splitPane, BorderLayout.CENTER);
        } else { 
            // Menu is NOT visible: display "map only"
            add(map, BorderLayout.CENTER);
        }
        repaint();
    }

}
