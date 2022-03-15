package AltiSkinTool.ui;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JLabel;
import AltiSkinTool.poly.Poly;
import AltiSkinTool.poly.PolyReader;
import AltiSkinTool.poly.PolyWriter;
import AltiSkinTool.util.Config;

import java.awt.FlowLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaneView {

    public JFrame frame;
    private ViewPanel pnlView;
    private String dist_path;

    private JComboBox<String> cmbBase;
    private JComboBox<String> cmbSkin;

    private JMenu mnTools;

    private final JFileChooser fc = new JFileChooser();

    private String selected_plane;

    // Plane
    private Poly[] planePolygons;
    private BufferedImage[] planeTextures;

    // Skin
    private Poly[] skinPolygons;
    private BufferedImage[] skinTextures;
    private JTextField txtCustomSpriteSize;

    /**
     * Create the application.
     */
    public PlaneView(String d_path) {
        dist_path = d_path;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        frame = new JFrame();
        frame.setBounds(100, 100, 948, 411);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnSelectPlane = new JMenu("Plane");
        menuBar.add(mnSelectPlane);

        JMenuItem mntmLoopy = new JMenuItem("Loopy");
        mntmLoopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected_plane = "loopy";
                loadPlane(selected_plane);
            }
        });
        mnSelectPlane.add(mntmLoopy);

        JMenuItem mntmExplodet = new JMenuItem("Explodet");
        mntmExplodet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected_plane = "explodet";
                loadPlane(selected_plane);
            }
        });
        mnSelectPlane.add(mntmExplodet);

        JMenuItem mntmMiranda = new JMenuItem("Miranda");
        mntmMiranda.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected_plane = "miranda";
                loadPlane(selected_plane);
            }
        });
        mnSelectPlane.add(mntmMiranda);

        JMenuItem mntmBiplane = new JMenuItem("Biplane");
        mntmBiplane.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected_plane = "biplane";
                loadPlane(selected_plane);
            }
        });
        mnSelectPlane.add(mntmBiplane);

        JMenuItem mntmBomber = new JMenuItem("Bomber");
        mntmBomber.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selected_plane = "bomber";
                loadPlane(selected_plane);
            }
        });
        mnSelectPlane.add(mntmBomber);

        mnTools = new JMenu("Tools");
        mnTools.setEnabled(false);
        menuBar.add(mnTools);

        JMenuItem mntmPlaneSpritesheet = new JMenuItem("Plane SpriteSheet");
        mntmPlaneSpritesheet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JDialog spriteDialog = new SpriteSheetDialog(
                        PolyWriter.generateSpriteSheet(planePolygons, planeTextures), planePolygons.length);
                spriteDialog.setVisible(true);
            }
        });
        mnTools.add(mntmPlaneSpritesheet);

        JMenuItem mntmLoadCustomPlane = new JMenuItem("Load custom plane");
        mntmLoadCustomPlane.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fc.setCurrentDirectory(new File(dist_path));
                int returnVal = fc.showOpenDialog(pnlView);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    int dialogResult = JOptionPane.showConfirmDialog(frame, "Is this a bordered sprite sheet?",
                            "Border", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        planeTextures = PolyReader.parseBorderedCustomSpritesheet(planePolygons, fc.getSelectedFile());
                    } else {
                        planeTextures = PolyReader.parseCustomSpritesheet(planePolygons, fc.getSelectedFile());
                    }
                    pnlView.setPlane(planePolygons, planeTextures);
                }
            }
        });

        JMenuItem mntmSkinSpritesheet = new JMenuItem("Skin SpriteSheet");
        mntmSkinSpritesheet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JDialog spriteDialog = new SpriteSheetDialog(PolyWriter.generateSpriteSheet(skinPolygons, skinTextures),
                        skinPolygons.length);
                spriteDialog.setVisible(true);
            }
        });
        mntmSkinSpritesheet.setEnabled(false);
        mnTools.add(mntmSkinSpritesheet);
        mnTools.add(mntmLoadCustomPlane);

        JMenuItem mntmLoadCustomSkin = new JMenuItem("Load custom skin");
        mntmLoadCustomSkin.setEnabled(false);
        mntmLoadCustomSkin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fc.setCurrentDirectory(new File(dist_path));
                int returnVal = fc.showOpenDialog(pnlView);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    int dialogResult = JOptionPane.showConfirmDialog(frame, "Is this a bordered sprite sheet?",
                            "Border", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        skinTextures = PolyReader.parseBorderedCustomSpritesheet(skinPolygons, fc.getSelectedFile());
                    } else {
                        skinTextures = PolyReader.parseCustomSpritesheet(skinPolygons, fc.getSelectedFile());
                    }
                    pnlView.setSkin(skinPolygons, skinTextures);
                }
            }
        });
        mnTools.add(mntmLoadCustomSkin);

        JMenuItem mntmWritePlaneTo = new JMenuItem("Write plane to pack");
        mntmWritePlaneTo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PolyWriter.writeSpriteSheet(dist_path, planePolygons, planeTextures);
            }
        });
        mnTools.add(mntmWritePlaneTo);

        JMenuItem mntmWriteSkinTo = new JMenuItem("Write skin to pack");
        mntmWriteSkinTo.setEnabled(false);
        mntmWriteSkinTo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PolyWriter.writeSpriteSheet(dist_path, skinPolygons, skinTextures);
            }
        });
        mnTools.add(mntmWriteSkinTo);

        JMenu config = new JMenu("Config");
        config.setEnabled(true);
        menuBar.add(config);

        JMenuItem mntmSetPath = new JMenuItem("Set dist Path");
        mntmSetPath.setEnabled(true);
        mntmSetPath.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Properties prop = Config.getProperties();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (prop.containsKey("dist_path")) {
                    fc.setCurrentDirectory(new File(prop.getProperty("dist_path")));
                }
                int returnVal = fc.showOpenDialog(pnlView);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    dist_path = fc.getSelectedFile().getAbsolutePath();
                    prop.setProperty("dist_path", dist_path);
                    Config.saveProperties(prop);
                }
            }
        });
        config.add(mntmSetPath);

        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel pnlPolyView = new JPanel();
        frame.getContentPane().add(pnlPolyView, BorderLayout.CENTER);
        pnlPolyView.setLayout(new BorderLayout(0, 0));

        JPanel pnlMenu = new JPanel();
        pnlPolyView.add(pnlMenu, BorderLayout.NORTH);
        pnlMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JLabel lblBase = new JLabel("Base");
        pnlMenu.add(lblBase);

        cmbBase = new JComboBox<String>();
        cmbBase.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("cmb state change");
                if (cmbBase.getSelectedItem() != null)
                    loadBase((String) cmbBase.getSelectedItem());
            }
        });
        pnlMenu.add(cmbBase);

        JLabel lblSkin = new JLabel("Skin");
        pnlMenu.add(lblSkin);

        cmbSkin = new JComboBox<String>();
        cmbSkin.setEnabled(false);
        cmbSkin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cmbSkin.getSelectedItem() != null) {
                    if (((String) cmbSkin.getSelectedItem()).equals("NONE")) {
                        pnlView.removeSkin();
                        mntmSkinSpritesheet.setEnabled(false);
                        mntmLoadCustomSkin.setEnabled(false);
                        mntmWriteSkinTo.setEnabled(false);
                    } else {
                        loadSkin((String) cmbSkin.getSelectedItem());
                        mntmSkinSpritesheet.setEnabled(true);
                        mntmLoadCustomSkin.setEnabled(true);
                        mntmWriteSkinTo.setEnabled(true);
                    }
                }
            }
        });
        cmbSkin.setModel(new DefaultComboBoxModel<String>(
                new String[] { "NONE", "Checker", "Flame", "Santa", "Shark", "Zebra" }));
        pnlMenu.add(cmbSkin);

        JLabel lblMisc = new JLabel("Misc");
        pnlMenu.add(lblMisc);

        JComboBox<String> cmbMisc = new JComboBox<String>();
        pnlMenu.add(cmbMisc);

        JCheckBox chckbxShowPoly = new JCheckBox("Show poly");
        chckbxShowPoly.setSelected(true);
        chckbxShowPoly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pnlView.setPolyON(chckbxShowPoly.isSelected());
            }
        });
        pnlMenu.add(chckbxShowPoly);

        JCheckBox chckbxShowTexture = new JCheckBox("Show texture");
        chckbxShowTexture.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pnlView.setTextureON(chckbxShowTexture.isSelected());
            }
        });
        chckbxShowTexture.setSelected(true);
        pnlMenu.add(chckbxShowTexture);

        JCheckBox chckbxShowOutline = new JCheckBox("Show outline");
        chckbxShowOutline.setSelected(true);
        chckbxShowOutline.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pnlView.setOutlineON(chckbxShowOutline.isSelected());
            }
        });
        pnlMenu.add(chckbxShowOutline);

        JLabel lblCustomSpriteSize = new JLabel("|   Custom sprite size");
        pnlMenu.add(lblCustomSpriteSize);

        txtCustomSpriteSize = new JTextField();
        txtCustomSpriteSize.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0) {
                String customString = txtCustomSpriteSize.getText();
                int newCustom = 90;
                if (customString.matches("\\d+")) {
                    newCustom = Integer.parseInt(customString);
                    if (newCustom < 90 || newCustom > 200) {
                        txtCustomSpriteSize.setText("90");
                        newCustom = 90;
                    }
                } else {
                    txtCustomSpriteSize.setText("90");
                    newCustom = 90;
                }
                PolyWriter.customSpriteSize = newCustom;
                System.out.println("focus lost");
            }
        });
        txtCustomSpriteSize.setText("90");
        pnlMenu.add(txtCustomSpriteSize);
        txtCustomSpriteSize.setColumns(4);

        pnlView = new ViewPanel();
        pnlPolyView.add(pnlView, BorderLayout.CENTER);
    }

    private void loadPlane(String plane) {
        populateBaseCMB(plane);
        populateSkinCMB(plane);
        mnTools.setEnabled(true);
    }

    public void loadBase(String base) {
        File f = new File(dist_path + "\\.poly\\render\\planes\\" + selected_plane + "\\" + base + ".animatedpoly");
        planePolygons = PolyReader.readPoly(f);
        planeTextures = PolyReader.getTextures(dist_path, planePolygons);
        pnlView.setPlane(planePolygons, planeTextures);
    }

    public void loadSkin(String skin) {
        File f = new File(dist_path + "\\.poly\\render\\skins\\" + selected_plane + "\\" + skin + ".animatedpoly");
        skinPolygons = PolyReader.readPoly(f);
        skinTextures = PolyReader.getTextures(dist_path, skinPolygons);
        pnlView.setSkin(skinPolygons, skinTextures);
    }

    public void populateBaseCMB(String plane) {
        File dir = new File(dist_path + "\\.poly\\render\\planes\\" + plane + "\\");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".animatedpoly");
            }
        });
        cmbBase.removeAllItems();
        for (File file : files) {
            cmbBase.addItem(file.getName().replaceFirst(".animatedpoly", ""));
        }
    }

    public void populateSkinCMB(String plane) {
        File dir = new File(dist_path + "\\.poly\\render\\skins\\" + plane + "\\");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".animatedpoly");
            }
        });
        cmbSkin.removeAllItems();
        cmbSkin.addItem("NONE");
        for (File file : files) {
            cmbSkin.addItem(file.getName().replaceFirst(".animatedpoly", ""));
        }
        cmbSkin.setEnabled(true);
    }
}
