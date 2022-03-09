package AltiSkinTool.poly;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import AltiSkinTool.image.Header;
import AltiSkinTool.image.ImageReader;
import AltiSkinTool.image.Sprite;

public class PolyReader {

	/**
	 * Parses '*.poly' and '*.animatedpoly' files, returning an array of Poly
	 * instances.
	 * 
	 * @param file '*.poly' or '*.animatedpoly' file to parse
	 * @return
	 */
	public static Poly[] readPoly(File file) {
		Document doc;

		// Read in .animatedpoly file
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return new Poly[0];
		}

		// If the file contains 'frames' tag it is deemed to be an '*.animatedpoly'
		// file,
		// and is processed as such.
		NodeList n_list = doc.getElementsByTagName("frames");
		if (n_list != null && n_list.getLength() != 0) {
			return readAniPoly(file.getAbsolutePath(), doc);
		}

		int[] colours = null;
		float[] coordinates = null;
		String image_path = null;
		float scale = 1.0f;
		float rotation = 0.0f;
		boolean flipX = false;
		boolean flipY = false;
		float translateY = 0;
		float translateX = 0;

		Element texturedPoly = (Element) doc.getElementsByTagName("TexturedPoly").item(0);
		if (texturedPoly != null) {
			String s_coordinates = texturedPoly.getAttribute("hull");
			coordinates = parseCoordinates(s_coordinates);
			colours = parseColours(s_coordinates);
		}

		Element image = (Element) doc.getElementsByTagName("image").item(0);
		if (image != null)
			image_path = image.getAttribute("path");
		if (image_path != null && image_path.equals("REPLACE_WITH_IMAGE_PATH"))
			image_path = null;

		Element xForm = (Element) doc.getElementsByTagName("xform").item(0);
		if (xForm != null) {
			scale = Float.parseFloat(xForm.getAttribute("scale"));
			rotation = Float.parseFloat(xForm.getAttribute("rotate"));
			flipX = Boolean.parseBoolean(xForm.getAttribute("flipX"));
			flipY = Boolean.parseBoolean(xForm.getAttribute("flipY"));
		}

		Element translate = (Element) doc.getElementsByTagName("translate").item(0);
		if (translate != null) {
			translateX = Float.parseFloat(translate.getAttribute("x"));
			translateY = Float.parseFloat(translate.getAttribute("y"));
		}

		String poly_file = file.getAbsolutePath();

		return new Poly[] { new Poly(poly_file, colours, coordinates, image_path, scale, rotation, flipX, flipY,
				translateY, translateX) };
	}

	/**
	 * Parses a '*.animatedpoly' file, and array of Poly instances.
	 * 
	 * @param filePath path to the '*.animatedpoly' file
	 * @param doc
	 * @return
	 */
	private static Poly[] readAniPoly(String filePath, Document doc) {

		int[] colours = null;
		float[] coordinates = null;
		String image_path = null;
		float scale = 1.0f;
		float rotation = 0.0f;
		boolean flipX = false;
		boolean flipY = false;
		float translateY = 0;
		float translateX = 0;
		int delay = 0;

		NodeList nl_frame = doc.getElementsByTagName("Frame");

		if (nl_frame == null || nl_frame.getLength() == 0)
			return new Poly[0];

		Poly[] polygons = new Poly[nl_frame.getLength()];

		for (int i = 0; i < nl_frame.getLength(); i++) {
			Element delayMS = (Element) nl_frame.item(i);
			if (delayMS != null) {
				delay = Integer.parseInt(delayMS.getAttribute("delayMs"));
			}

			polygons[i] = new Poly(filePath, colours, coordinates, image_path, scale, rotation, flipX, flipY,
					translateY, translateX);
			polygons[i].delay = delay;

			traverseNode(nl_frame.item(i), polygons[i]);
		}

		return polygons;
	}

	/**
	 * Populates a Poly instance with values parsed from a Node..
	 * 
	 * @param node 'Frame' or its child nodes.
	 * @param p    Poly to contain 'Frame' tag values.
	 */
	private static void traverseNode(Node node, Poly p) {

		// For each child node, call traverseNode(...)
		NodeList nl = node.getChildNodes();
		if (nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++)
				traverseNode(nl.item(i), p);
		}
		if (node.getNodeType() != Node.ELEMENT_NODE)
			return;

		// Parse information depending on the type of the tag.
		Element el = (Element) node;
		String tag_name = el.getTagName();

		if (tag_name.equals("poly")) {
			String s_coordinates = el.getAttribute("hull");
			p.coordinates = parseCoordinates(s_coordinates);
			p.colours = parseColours(s_coordinates);
		} else {
			if (tag_name.equals("image")) {
				p.texture_path = el.getAttribute("path");
				if (p.texture_path != null && p.texture_path.equals("REPLACE_WITH_IMAGE_PATH"))
					p.texture_path = null;
			} else if (tag_name.equals("xform")) {
				p.scale = Float.parseFloat(el.getAttribute("scale"));
				p.rotation = Float.parseFloat(el.getAttribute("rotate"));
				p.flipX = Boolean.parseBoolean(el.getAttribute("flipX"));
				p.flipY = Boolean.parseBoolean(el.getAttribute("flipY"));
			} else if (tag_name.equals("translate")) {
				p.translateX = Float.parseFloat(el.getAttribute("x"));
				p.translateY = Float.parseFloat(el.getAttribute("y"));
			}
		}
	}

	/**
	 * Parses a 'hull' tag string value into a float array.
	 * 
	 * @param s_coordinates
	 * @return hull coordinates
	 */
	private static float[] parseCoordinates(String s_coordinates) {
		if (s_coordinates.isEmpty()) {
			return null;
		}
		String[] s_values = s_coordinates.split(",");
		float[] coordinates = new float[s_values.length - occurencesOfChar(s_coordinates, '!')];
		int c_index = 0;
		for (int i = 0; i < s_values.length; i++) {
			if (s_values[i].charAt(0) != '!') {
				coordinates[c_index] = Float.parseFloat(s_values[i]);
				c_index++;
			}
		}
		return coordinates;
	}

	/**
	 * Parses 'hull' tag string value looking for floats preceded with an '!'. Such
	 * values indicate
	 * the colour the 'hull' should be drawn in until another '!' indicated value is
	 * reached.
	 * 
	 * @param s_coordinates
	 * @return array of (colour, start_index) pairs. a[n] = colour, a[n+1] =
	 *         start_index
	 */
	private static int[] parseColours(String s_coordinates) {
		if (s_coordinates.isEmpty()) {
			return null;
		}
		String[] s_values = s_coordinates.split(",");
		int[] colours = new int[occurencesOfChar(s_coordinates, '!') * 2];
		int c_index = 0;
		for (int i = 0; i < s_values.length; i++) {
			if (s_values[i].charAt(0) == '!') {
				colours[c_index] = Integer.parseInt(s_values[i].substring(1));
				colours[c_index + 1] = i - c_index;
				c_index += 2;
			}
		}
		return colours;
	}

	/**
	 * Counts the number of occurences of char ch within a string.
	 * 
	 * @param s
	 * @param ch
	 * @return
	 */
	private static int occurencesOfChar(String s, char ch) {
		int count = 0;
		for (char c : s.toCharArray())
			if (c == ch)
				count++;
		return count;
	}

	/**
	 * Produces a BufferedImage from a pack file.
	 * 
	 * @param dist_path    path to the dist directory
	 * @param texture_path relative path to the texture containing pack file
	 * @return
	 */
	public static BufferedImage getTexture(String dist_path, String texture_path) {
		String t_path = dist_path + "/.image/" + texture_path;
		File texture_file = new File(t_path);

		String lookupTXT = t_path.substring(0, t_path.length() - texture_file.getName().length())
				+ "image-lookup-map.txt";

		File imgFile = getPFile(lookupTXT, texture_file.getName());

		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(imgFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (fileBytes == null) {
			System.out.println("fileBytes is NULL");
			return null;
		}

		Header header = new Header(fileBytes);
		BufferedImage buffImg = ImageReader.readImage(header, fileBytes);
		for (Sprite sprite : header.getSprites()) {
			if (sprite.spriteName.equals(texture_path)) {
				int[] rgb_arr = buffImg.getRGB(sprite.x, buffImg.getHeight() - sprite.y - sprite.height, sprite.width,
						sprite.height, null, 0, sprite.width);
				BufferedImage toReturn = new BufferedImage(sprite.width, sprite.height, BufferedImage.TYPE_4BYTE_ABGR);
				toReturn.setRGB(0, 0, sprite.width, sprite.height, rgb_arr, 0, toReturn.getWidth());
				return toReturn;
			}
		}
		return null;
	}

	public static BufferedImage[] getTextures(String dist_path, Poly[] polygons) {
		BufferedImage[] textures = new BufferedImage[polygons.length];
		for (int i = 0; i < polygons.length; i++) {
			if (polygons[i].texture_path != null) {
				textures[i] = getTexture(dist_path, polygons[i].texture_path);
			}
		}
		return textures;
	}

	/**
	 * Identifies the pack file of a given png image name using an
	 * image-lookup-map.txt path.
	 * 
	 * @param lookup_path image-lookup-map.txt path
	 * @param png_name    png image name
	 * @return
	 */
	public static File getPFile(String lookup_path, String png_name) {
		Document doc;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();

			StringBuilder xmlStringBuilder = new StringBuilder();
			xmlStringBuilder.append("<?xml version=\"1.0\"?>");
			doc = builder.parse(new File(lookup_path));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}

		NodeList n_list = doc.getElementsByTagName("entry");

		for (int i = 0; i < n_list.getLength(); i++) {
			Element entry = (Element) n_list.item(i);
			if (entry.getAttribute("key").equals(png_name)) {
				String p_path = lookup_path.substring(0,
						lookup_path.length() - "image-lookup-map.txt".length())
						+ entry.getAttribute("value");
				return new File(p_path);
			}
		}

		return null;
	}

	/**
	 * Retrieves sprites from a customs spritesheet.
	 * 
	 * @param polygons     plane/skin polys
	 * @param sprite_sheet plane/skin spritesheet
	 * @return
	 */
	public static BufferedImage[] parseCustomSpritesheet(Poly[] polygons, File sprite_sheet) {
		BufferedImage buffImg;
		try {
			buffImg = ImageIO.read(sprite_sheet);

			BufferedImage[] textures = new BufferedImage[polygons.length];
			int x = 0;
			int y = 0;
			for (int i = 0; i < polygons.length; i++) {
				polygons[i].translateX = -PolyWriter.customSpriteSize / 2;
				polygons[i].translateY = -PolyWriter.customSpriteSize / 2;
				if (x + PolyWriter.customSpriteSize > PolyWriter.spriteSheetWidth) {
					x = 0;
					y += PolyWriter.customSpriteSize;
				}
				textures[i] = buffImg.getSubimage(x, y, PolyWriter.customSpriteSize, PolyWriter.customSpriteSize);

				x += PolyWriter.customSpriteSize;
			}
			return textures;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage[] parseBorderedCustomSpritesheet(Poly[] polygons, File sprite_sheet) {
		BufferedImage buffImg;
		try {
			buffImg = ImageIO.read(sprite_sheet);

			BufferedImage[] textures = new BufferedImage[polygons.length];
			int x = 0;
			int y = 0;
			int row = 1;
			int col = 1;
			for (int i = 0; i < polygons.length; i++) {
				polygons[i].translateX = -PolyWriter.customSpriteSize / 2;
				polygons[i].translateY = -PolyWriter.customSpriteSize / 2;
				if (x + PolyWriter.customSpriteSize > PolyWriter.spriteSheetWidth) {
					x = 0;
					y += PolyWriter.customSpriteSize;
					row = 1;
					col += 1;
				}
				textures[i] = buffImg.getSubimage(x + row, y + col, PolyWriter.customSpriteSize,
						PolyWriter.customSpriteSize);
				x += PolyWriter.customSpriteSize;
				row++;
			}
			return textures;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
