package ui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import poly.PolyWriter;

import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class SpriteSheetDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private final JPanel contentPanel = new JPanel();
    private JLabel lblSpriteSheet;
    private JCheckBox chckbxBorder;

    /**
     * Create the dialog.
     */
    public SpriteSheetDialog(BufferedImage spriteSheet, int spriteCount) {
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            lblSpriteSheet = new JLabel("");
            lblSpriteSheet.setIcon(new ImageIcon(spriteSheet));
            contentPanel.add(lblSpriteSheet);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton btnSave = new JButton("Save");
                btnSave.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fc = new JFileChooser();
                        int returnVal = fc.showSaveDialog(contentPanel);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                if(chckbxBorder.isSelected()){
                                    ImageIO.write(PolyWriter.generateBorderedSpriteSheet(spriteSheet, spriteCount), "png", file);
                                } else {
                                    ImageIO.write(spriteSheet, "png", file);
                                }
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                           
                        }
                    }
                });
                {
                    chckbxBorder = new JCheckBox("Border");
                    chckbxBorder.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent arg0) {
                            if(chckbxBorder.isSelected()){
                                lblSpriteSheet.setIcon(new ImageIcon(PolyWriter.generateBorderedSpriteSheet(spriteSheet, spriteCount)));
                            } else {
                                lblSpriteSheet.setIcon(new ImageIcon(spriteSheet));
                            }
                        }
                    });
                    buttonPane.add(chckbxBorder);
                }
                btnSave.setActionCommand("OK");
                buttonPane.add(btnSave);
                getRootPane().setDefaultButton(btnSave);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        close();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }
    
    private void close(){
        this.dispose();
    }

}
