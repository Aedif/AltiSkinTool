package AltiSkinTool.poly;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import AltiSkinTool.misc.ImagePacker;

public class PolyWriter {

    public static int customSpriteSize = 90;
    public static int spriteSheetWidth = 1024;
    public static int spriteSheetHeight = 512;

    /**
     * Produces a sprite sheet using plane/skin poly and texture arrays. The size of
     * each sprite is defined by 'customSpriteSize.'
     * The width of the sprite sheet dimensions are defined by 'spriteSheetWidth,'
     * and 'spriteSheetHeight.'
     * 
     * @param polygons plane/skin poly array
     * @param textures plane/skin poly array
     * @return
     */
    public static BufferedImage generateSpriteSheet(Poly[] polygons, BufferedImage[] textures) {
        BufferedImage temp = new BufferedImage(spriteSheetWidth, spriteSheetHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = temp.createGraphics();
        int x = 0;
        int y = 0;
        for (int i = 0; i < polygons.length; i++) {
            if (x + customSpriteSize > spriteSheetWidth) {
                x = 0;
                y += customSpriteSize;
            }

            // Texture could be null in a case where a skin is completely obfuscated by the
            // plane. e.g. whales santa hat
            if (textures[i] != null) {
                AffineTransform af = new AffineTransform();
                af.translate(customSpriteSize / 2, 0); // center at customSpriteSize by customSpriteSize square
                af.translate(polygons[i].translateX,
                        -polygons[i].translateY - textures[i].getHeight() + (customSpriteSize / 2)); // y needs to equal
                                                                                                     // 0, for skins

                BufferedImage sprite = new BufferedImage(customSpriteSize, customSpriteSize,
                        BufferedImage.TYPE_4BYTE_ABGR);
                sprite.createGraphics().drawImage(textures[i], af, null);

                g2d.drawImage(sprite, x, y, null);
            }

            x += customSpriteSize;
        }
        // return temp;
        return temp;
    }

    /**
     * Takes a sprite sheet and produces a 1px border around each sprite.
     * 
     * @param spriteSheet original sprite sheet generated using
     *                    generateSpriteSheet(Poly[], BufferedImage[])
     * @param spriteCount number of sprites conntained within the sheet
     * @return
     */
    public static BufferedImage generateBorderedSpriteSheet(BufferedImage spriteSheet, int spriteCount) {
        // Define width and height of the bordered sprite sheet.
        // The size of the original sprite sheet is increased to accommodate extra
        // pixels
        int width = spriteSheetWidth;
        int height = spriteSheetHeight;
        width += (width / customSpriteSize) + 1;
        height += spriteCount / (width / customSpriteSize) + 1;
        if ((spriteCount % (width / customSpriteSize)) != 0)
            height += 1;

        BufferedImage borderedSpriteSheet = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = borderedSpriteSheet.createGraphics();

        int x = 0;
        int y = 0;
        int col = 1;
        int row = 1;
        for (int i = 0; i < spriteCount; i++) {
            if (x + customSpriteSize > spriteSheetWidth) {
                x = 0;
                y += customSpriteSize;
                row = 1;
                col += 1;
            }

            g2d.drawImage(spriteSheet.getSubimage(x, y, customSpriteSize, customSpriteSize), x + row, y + col, null);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + row - 1, y + col - 1, customSpriteSize + 1, customSpriteSize + 1);

            row++;
            x += customSpriteSize;
        }
        return borderedSpriteSheet;
    }

    /**
     * Packs the supplied images into a single texture using ImagePacker. Each
     * images position can be
     * retrieved using ImagePackers getRects(), and image index values.
     * 
     * @param textures
     * @return
     */
    public static ImagePacker generatePackedSpriteSheet(Poly[] polygons, BufferedImage[] textures) {
        class ImgSprite {
            String num;
            BufferedImage img;

            public ImgSprite(String num, BufferedImage img) {
                this.num = num;
                this.img = img;
            }
        }

        // used in determining the minimum spritesheet size required to contain all of
        // the textures
        int total_area = 0;

        ImgSprite[] t = new ImgSprite[textures.length];
        for (int i = 0; i < textures.length; i++) {
            t[i] = new ImgSprite(Integer.toString(i), getMinimumBound(polygons[i], textures[i]));
            total_area += t[i].img.getHeight() * t[i].img.getWidth();
        }

        // Sort the images according to their area to aid the packing algorithm.
        Arrays.sort(t, new Comparator<ImgSprite>() {
            @Override
            public int compare(ImgSprite o1, ImgSprite o2) {
                return o2.img.getWidth() * o2.img.getHeight() - o1.img.getWidth() * o1.img.getHeight();
            }
        });

        // determine starting texture sheet dimensions
        int pow1 = 5;
        int pow2 = 5;
        while (((1 << pow1) * (1 << pow2)) < total_area) {
            if (pow1 == pow2) {
                pow1++;
            } else {
                pow2++;
            }
        }

        // pack images
        boolean packed = false;
        ImagePacker imgPack = null;
        while (!packed) {
            imgPack = new ImagePacker((1 << pow1), (1 << pow2), 1, true);
            try {
                for (ImgSprite i : t) {
                    imgPack.insertImage(i.num, i.img);
                }
                packed = true;
            } catch (RuntimeException e) {
                if (pow1 == pow2)
                    pow1++;
                else
                    pow2++;
            }
        }
        return imgPack;
    }

    /**
     * Returns a minimum sized image containing all non-alpha pixels within the
     * original.
     * 
     * @param img sprite
     * @return
     */
    private static BufferedImage getMinimumBound(Poly p, BufferedImage img) {
        int s_img_height = 0;
        int s_img_y = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            boolean empty_row = true;
            for (int x = 0; x < img.getWidth(); x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) {
                    empty_row = false;
                    break;
                }
            }
            if (!empty_row) {
                if (s_img_y == 0) {
                    s_img_y = y;
                }
                s_img_height = y - s_img_y + 1;
            }
        }

        int s_img_width = 0;
        int s_img_x = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            boolean empty_col = true;
            for (int y = 0; y < img.getHeight(); y++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) != 0) {
                    empty_col = false;
                    break;
                }
            }
            if (!empty_col) {
                if (s_img_x == 0) {
                    s_img_x = x;
                }
                s_img_width = x - s_img_x + 1;
            }
        }

        // TEST
        p.translateX += s_img_x;
        p.translateY += img.getHeight() - s_img_y - s_img_height;
        return img.getSubimage(s_img_x, s_img_y, s_img_width, s_img_height);
    }

    /**
     * Generates a new pack and animatedpoly files, and overwrites the originals
     * pointed to by polygons[0].
     * 
     * @param dist_path dist directory path
     * @param polygons  plane/skin poly array
     * @param textures  plane/skin poly array
     */
    public static void writeSpriteSheet(String dist_path, Poly[] polygons, BufferedImage[] textures) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
            // root elements
            Document doc = builder.newDocument();
            Element rootElement = doc.createElement("AnimatedPoly");
            doc.appendChild(rootElement);

            // frame elements
            Element frames = doc.createElement("frames");
            rootElement.appendChild(frames);

            // new pack file byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // num of sprites
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(textures.length);
            bos.write(bb.array());

            // pack textures
            ImagePacker imgPack = generatePackedSpriteSheet(polygons, textures);
            BufferedImage spriteSheet = imgPack.getImage();
            Map<String, Rectangle> sRecs = imgPack.getRects();

            for (int i = 0; i < polygons.length; i++) {

                // sprite name length
                bos.write(new byte[] { (byte) (polygons[i].texture_path.length() & 0xFF),
                        (byte) ((polygons[i].texture_path.length() >> 8) & 0xFF) });
                // sprite name
                bos.write(polygons[i].texture_path.getBytes("UTF-8"));

                Rectangle rec = sRecs.get(Integer.toString(i));

                bos.write(intToLE(rec.x)); // x-offset
                bos.write(intToLE(spriteSheet.getHeight() - rec.y - (int) rec.getHeight())); // y-offset
                bos.write(intToLE((int) rec.getWidth())); // width
                bos.write(intToLE((int) rec.getHeight())); // height

                Element frame = constructFrame(polygons[i], polygons[i].translateX, polygons[i].translateY, doc);
                frames.appendChild(frame);
            }

            bos.write(intToBE(3)); // RGBA8
            bos.write(intToBE(0)); // CLAMPED
            bos.write(intToBE(1)); // LINEAR
            bos.write(intToBE(1)); // number of textures
            bos.write(intToBE(spriteSheet.getWidth())); // texture width
            bos.write(intToBE(spriteSheet.getHeight())); // texture height

            // write image bytes
            byte[] imageBytes = new byte[spriteSheet.getWidth() * spriteSheet.getHeight() * 4];
            int index = -1;
            for (int y_2 = 0; y_2 < spriteSheet.getHeight(); y_2++) {
                for (int x_2 = 0; x_2 < spriteSheet.getWidth(); x_2++) {
                    int pixel = spriteSheet.getRGB(x_2, y_2);
                    imageBytes[++index] = (byte) ((pixel >> 16) & 0x000000ff);
                    imageBytes[++index] = (byte) ((pixel >> 8) & 0x000000ff);
                    imageBytes[++index] = (byte) ((pixel) & 0x000000ff);
                    imageBytes[++index] = (byte) ((pixel >> 24) & 0x000000ff);
                }
            }
            bos.write(imageBytes);

            String t_path = dist_path + "/.image/" + polygons[0].texture_path;
            File texture_file = new File(t_path);
            String lookupTXT = t_path.substring(0, t_path.length() - texture_file.getName().length())
                    + "image-lookup-map.txt";
            File packFile = PolyReader.getPFile(lookupTXT, texture_file.getName());
            OutputStream out;
            out = new FileOutputStream(packFile);
            bos.writeTo(out);
            out.close();

            // write .animatedpoly file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(polygons[0].poly_file));
            transformer.transform(source, result);
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a DOM Element object describing an animatedpoly 'frame' tag.
     * 
     * @param p   polygon to be reversed to a 'frame' tag
     * @param c_x translateX value to overwrite the original
     * @param c_y translateY value to overwrite the original
     * @param doc
     * @return
     */
    private static Element constructFrame(Poly p, float c_x, float c_y, Document doc) {
        // frame
        Element frame = doc.createElement("Frame");
        frame.setAttribute("delayMs", Integer.toString(p.delay));

        // poly
        Element poly = doc.createElement("poly");
        frame.appendChild(poly);
        poly.setAttribute("hull", p.coordinatesToString());

        // texture
        Element texture = doc.createElement("texture");
        poly.appendChild(texture);

        // image
        Element image = doc.createElement("image");
        texture.appendChild(image);
        image.setAttribute("path", p.texture_path);

        // xform
        Element xform = doc.createElement("xform");
        texture.appendChild(xform);
        xform.setAttribute("scale", Float.toString(p.scale));
        xform.setAttribute("rotate", Float.toString(p.rotation));
        xform.setAttribute("flipX", Boolean.toString(p.flipX));
        xform.setAttribute("flipY", Boolean.toString(p.flipY)); // TEMP

        // translate
        Element translate = doc.createElement("translate");
        xform.appendChild(translate);
        translate.setAttribute("x", Float.toString(c_x));
        translate.setAttribute("y", Float.toString(c_y));

        return frame;
    }

    private static byte[] intToLE(int i) {
        return new byte[] { (byte) (i & 0xFF), (byte) ((i >> 8) & 0xFF), (byte) ((i >> 16) & 0xFF),
                (byte) ((i >> 24) & 0xFF) };
    }

    private static byte[] intToBE(int i) {
        return new byte[] { (byte) ((i >> 24) & 0xFF), (byte) ((i >> 16) & 0xFF), (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF) };
    }
}
