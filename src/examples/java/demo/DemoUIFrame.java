package demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.map4j.layers.MapLayer;
import org.map4j.layers.MapMarkerDot;
import org.map4j.layers.MapMarkerImage;
import org.map4j.swing.Map4JPanel;
import org.map4j.utils.ImageUtils;

/**
 * A frame that represents the window that contains the navigation panel
 *
 * @author Joel Kozikowski
 *
 */
public class DemoUIFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final DemoUIPanel uiPanel;
    
    private boolean showToolTip;

    public DemoUIFrame() {
        super("Demo UI Window");
        
        Dimension fixedSize = new Dimension(800,600);
        setMinimumSize(fixedSize);
        setMaximumSize(fixedSize);
        setSize(fixedSize.width, fixedSize.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        
        uiPanel = new DemoUIPanel();

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(uiPanel, BorderLayout.CENTER);

        
        MapLayer markerSet = new MapLayer("Florida");
        MapLayer boca = markerSet.add("Boca Raton");
        boca.add(new MapMarkerDot("Boca Inlet", 26.3356, -80.0702));
        boca.add(new MapMarkerDot("Lake Boca", 26.3436, -80.0743));
        MapLayer lhp = markerSet.add("Lighthouse Point");
        lhp.add(new MapMarkerDot("Hillsboro Inlet", 26.2580, -80.0812));
        
        BufferedImage iconSource = ImageUtils.loadResourceImage("/images/markers/underwater-icons.png");
        
        boca.add(new MapMarkerImage(iconSource, 78, 13, "Dive #1", 26.3356, -80.058));

        this.getMapPanel().setLayerRoot(markerSet);
    }

    public Map4JPanel getMapPanel() {
        return uiPanel.getMapPanel();
    }
    
    
    public DemoUIPanel uiPanel() {
        return uiPanel;
    }

    
    public boolean isShowToolTip() {
        return showToolTip;
    }

    public void setShowToolTip(boolean showToolTip) {
        this.showToolTip = showToolTip;
    }
    
}
