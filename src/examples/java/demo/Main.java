package demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.map4j.coordinates.WCoordinate;
import org.map4j.loaders.DefaultMBTilesController;
import org.map4j.loaders.DefaultOnlineTileController;
import org.map4j.loaders.TileLoaderController;
import org.map4j.swing.Map4JPanel;


/**
 * Demonstrates the usage of Map4J. This "Main" frame displays some controls
 * for manipulating the map.
 *
 * @author Joel Kozikowski
 */
public class Main extends JFrame {

    private static final long serialVersionUID = 1L;

    private final DemoUIFrame mapFrame;

    private final JLabel zoomLabel;
    private final JSlider zoomValue;

    private final int WINDOW_WIDTH = 1200;
    private final int WINDOW_HEIGHT = 125;
    
    private WCoordinate simPosition = new WCoordinate(26.3356, -80.067);
    private int simHeading = 90;
    
    private TileLoaderController initialController;
    
    private boolean animate = true;
    
    /**
     * Constructs the {@code Demo}.
     */
    public Main() {
        super("Map4J Demo");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        mapFrame = new DemoUIFrame();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - WINDOW_WIDTH) / 2, screenSize.height / 2 + 312);
        

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panelTop = new JPanel();
        JPanel panelBottom = new JPanel();
        JPanel helpPanel = new JPanel();

        zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JSlider(0, 20, 20);
        zoomValue.setPaintLabels(true);
        zoomValue.setPaintTicks(true);
        zoomValue.setMajorTickSpacing(5);
        zoomValue.setMinorTickSpacing(1);
        zoomValue.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int zv = zoomValue.getValue();
                getMapPanel().setZoom(zv);
            }
        });
        
        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelBottom, BorderLayout.SOUTH);
        JLabel helpLabel = new JLabel("Use right mouse button to move,\n "
                + "left double click or mouse wheel to zoom.");
        helpPanel.add(helpLabel);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getMapPanel().refresh(true);
            }
        });
        panelBottom.add(btnRefresh);

        JButton btnAnimate = new JButton("Animate");
        btnAnimate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animate = !animate;
            }
        });
        panelBottom.add(btnAnimate);

        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                simPosition = new WCoordinate(26.3356, -80.067);
                simHeading = 90;
            }
        });
        panelBottom.add(btnReset);
        
        
        JComboBox<TileLoaderController> tileSourceSelector = new JComboBox<>(new TileLoaderController[] {
                initialController = new DefaultOnlineTileController("OpenStreetMap", "https://a.tile.openstreetmap.org", "png", true),
                new DefaultMBTilesController("NOAA Raster South Florida Sonar.mbtiles")
        });
        
        this.getMapPanel().setTileLoaderController(initialController);
        
        tileSourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                getMapPanel().setTileLoaderController((TileLoaderController) e.getItem());
            }
        });
        
        panelTop.add(tileSourceSelector);
        final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
        showMapMarker.setSelected(getMapPanel().isMapLayersVisible());
        showMapMarker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getMapPanel().setMapLayersVisible(showMapMarker.isSelected());
            }
        });
        panelBottom.add(showMapMarker);
        
        ///
        final JCheckBox showTreeLayers = new JCheckBox("Split panel visible");
        showTreeLayers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uiPanel().setSplitVisible(showTreeLayers.isSelected());
            }
        });
        panelBottom.add(showTreeLayers);
        
        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        
        getMapPanel().setPosition(simPosition);
        
        
        
        // Run a simulator that shows motion...
        Timer timer = new Timer(1300, new ActionListener() {
            private double delta = 0.001;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (animate) {
                    if (simPosition.getLon() > -80.058) {
                        if (simHeading < 180) {
                            simHeading += 10;
                        }
                        else {
                          simHeading = 180;
                          simPosition.setLat(simPosition.getLat() - delta);
                        }
                    }
                    else {
                       simPosition.setLon(simPosition.getLon() + delta);
                    }
                    System.out.println("Sim heading " + simHeading + " at position is " + simPosition.toString());                
                    getMapPanel().setPosition(simPosition);
                    getMapPanel().setHeading(simHeading);
                }
            }
        });
        timer.start();

    }

    private Map4JPanel getMapPanel() {
        return mapFrame.getMapPanel();
    }

    public DemoUIPanel uiPanel() {
        return mapFrame.uiPanel();
    }
    

    @Override
    public void setVisible(boolean b) {
        mapFrame.setVisible(b);
        super.setVisible(b);
    }
    
    /**
     * @param args Main program arguments
     */
    public static void main(String[] args) {
        new Main().setVisible(true);
    }
    
}
