package AltiSkinTool.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import AltiSkinTool.poly.Poly;
import AltiSkinTool.poly.PolyWriter;

public class ViewPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private DrawPanel pnlDraw;
    private JSlider slider;

    private Poly[] polygons;
    private BufferedImage[] textures;
    private int p_draw;

    private Poly[] skinPolygons;
    private BufferedImage[] skinTextures;

    private boolean textureON = true;
    private boolean polyON = true;
    private boolean outlineON = true;

    public ViewPanel() {
        this.setLayout(new BorderLayout(0, 0));
        polygons = null;
        textures = null;
        slider = new JSlider();
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                p_draw = slider.getValue();
                pnlDraw.repaint();
            }
        });
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        this.add(slider, BorderLayout.NORTH);

        pnlDraw = new DrawPanel();
        pnlDraw.setBackground(Color.GRAY);
        this.add(pnlDraw, BorderLayout.CENTER);
    }

    /**
     * Assigns the plane to be displayed.
     * 
     * @param p plane polys
     * @param t plane textures
     */
    public void setPlane(Poly[] p, BufferedImage[] t) {
        if (p.length != t.length) {
            throw new IllegalArgumentException("Poly and texture frame count doesn't match.");
        }
        polygons = p;
        textures = t;
        slider.setMinimum(0);
        slider.setMaximum(polygons.length - 1);
        slider.setValue(0);
    }

    /**
     * Assigns the skin to be displayed.
     * 
     * @param p skins polys
     * @param t skins textures
     */
    public void setSkin(Poly[] p, BufferedImage[] t) {
        if (p.length != t.length) {
            throw new IllegalArgumentException("Poly and texture frame count doesn't match.");
        }
        skinPolygons = p;
        skinTextures = t;
        slider.setMinimum(0);
        slider.setMaximum(skinPolygons.length - 1);
        slider.setValue(0);
    }

    /**
     * Deselects the skin.
     */
    public void removeSkin() {
        skinPolygons = null;
        skinTextures = null;
    }

    /**
     * Disables/enables drawing of the plane and skin associated polys.
     * 
     * @param on
     */
    public void setPolyON(boolean on) {
        polyON = on;
        repaint();
    }

    /**
     * Disables/enables drawing of the plane and skin associated textures.
     */
    public void setTextureON(boolean on) {
        textureON = on;
        repaint();
    }

    public void setOutlineON(boolean on) {
        outlineON = on;
        repaint();
    }

    class DrawPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (textureON) {
                drawTexture(g, polygons, textures, p_draw);
                drawTexture(g, skinPolygons, skinTextures, p_draw);
                drawInfo(g, polygons, "== Plane ==", 10, p_draw);
                drawInfo(g, skinPolygons, "== Skin ==", 130, p_draw);
            }
            if (polyON) {
                drawPoly(g, polygons, p_draw);
                drawPoly(g, skinPolygons, p_draw);
            }
        }

        private void drawPoly(Graphics g, Poly[] poly, int j) {
            if (poly == null)
                return;

            float x_centre = getWidth() / 2.0f;
            float y_centre = getHeight() / 2.0f;

            if (poly[j] != null && polyON) {

                float[] coord = poly[j].coordinates;
                float[] dimensions = { 0.0f, 0.0f };

                int c_index = 0;

                if (coord != null) {
                    for (int i = 0; i < coord.length - 3; i += 2) {
                        if (c_index + 1 < poly[j].colours.length && i >= poly[j].colours[c_index + 1]) {
                            g.setColor(new Color(poly[j].colours[c_index]));
                            c_index += 2;
                        }

                        g.drawLine((int) (x_centre + coord[i] - dimensions[0] / 2.0f),
                                (int) (y_centre + coord[i + 1] - dimensions[1] / 2.0f),
                                (int) (x_centre + coord[i + 2] - dimensions[0] / 2.0f),
                                (int) (y_centre + coord[i + 3] - dimensions[1] / 2.0f));
                    }
                    g.drawLine((int) (x_centre + coord[coord.length - 2] - dimensions[0] / 2.0f),
                            (int) (y_centre + coord[coord.length - 1] - dimensions[1] / 2.0f),
                            (int) (x_centre + coord[0] - dimensions[0] / 2.0f),
                            (int) (y_centre + coord[1] - dimensions[1] / 2.0f));
                }
            }
        }

        private void drawTexture(Graphics g, Poly[] poly, BufferedImage[] text, int j) {
            if (text == null)
                return;

            float x_centre = getWidth() / 2.0f;
            float y_centre = getHeight() / 2.0f;

            if (text[j] == null) {
                System.out.println("Texture null at j = " + j);
                return;
            }

            AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -text[j].getHeight(null));
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            BufferedImage transText = op.filter(text[j], null);

            AffineTransform at = new AffineTransform();
            at.translate(x_centre, y_centre); // centering on panel
            at.translate(poly[j].translateX, poly[j].translateY);
            at.rotate(Math.toRadians(poly[j].rotation), 0, 0);

            Graphics2D g2d = (Graphics2D) g;

            if (poly[j].scale != 1.0) {
                g2d.drawImage(transText.getScaledInstance(
                        (int) (transText.getWidth() * poly[j].scale), (int) (transText.getHeight() * poly[j].scale),
                        Image.SCALE_SMOOTH), at, null);
            } else {
                g2d.drawImage(transText, at, null);
                if (outlineON) {
                    g.drawRect((int) (x_centre + poly[j].translateX), (int) (y_centre + poly[j].translateY),
                            transText.getWidth(), transText.getHeight()); // TEMP
                    g.setColor(Color.WHITE);
                    g.drawRect((int) (x_centre - (PolyWriter.customSpriteSize / 2)),
                            (int) (y_centre - (PolyWriter.customSpriteSize / 2)),
                            PolyWriter.customSpriteSize, PolyWriter.customSpriteSize);
                }
            }
        }

        private void drawInfo(Graphics g, Poly[] poly, String header, int y_offset, int j) {
            if (poly == null)
                return;

            g.setColor(Color.WHITE);
            g.drawString(header, 20, 10 + y_offset);
            g.drawString("scale: " + poly[j].scale, 20, 20 + y_offset);
            g.drawString("rotation: " + poly[j].rotation, 20, 30 + y_offset);
            g.drawString("flipX: " + poly[j].flipX, 20, 40 + y_offset);
            g.drawString("flipY: " + poly[j].flipY, 20, 50 + y_offset);

            g.drawString("translateX: " + poly[j].translateX, 20, 80 + y_offset);
            g.drawString("translateY: " + poly[j].translateY, 20, 90 + y_offset);

            if (poly[j].texture_path != null)
                g.drawString("texture path: " + poly[j].texture_path, 20, 110 + y_offset);
        }

    }
}
