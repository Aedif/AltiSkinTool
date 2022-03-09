package AltiSkinTool.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

//TODO handle JPEG compressed images

public class ImageWriter {
	public static void save(File sourceFile, File imageFile) {
		byte[] sourceBytes = null;
		try {
			sourceBytes = Files.readAllBytes(sourceFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Header header = new Header(sourceBytes);

		// temp
		FileOutputStream fos = null;
		try {
			BufferedImage buffImg = ImageIO.read(imageFile);
			byte[] imageBytes = new byte[buffImg.getHeight() * buffImg.getWidth() * 4];
			int index = -1;
			for (int y = 0; y < buffImg.getHeight(); y++)
				for (int x = 0; x < buffImg.getWidth(); x++) {
					int pixel = buffImg.getRGB(x, y);
					imageBytes[++index] = (byte) ((pixel >> 16) & 0x000000ff);
					imageBytes[++index] = (byte) ((pixel >> 8) & 0x000000ff);
					imageBytes[++index] = (byte) ((pixel) & 0x000000ff);
					imageBytes[++index] = (byte) ((pixel >> 24) & 0x000000ff);
				}

			int imgStartIndex = header.getHeader_end_index();

			byte[] outBytes = new byte[imgStartIndex + imageBytes.length];
			int outIndex = -1;
			for (int i = 0; i < imgStartIndex; i++)
				outBytes[++outIndex] = sourceBytes[i];
			for (int i = 0; i < imageBytes.length; i++)
				outBytes[++outIndex] = imageBytes[i];

			// temp
			// outBytes = header.getBytes();
			// temp

			fos = new FileOutputStream(sourceFile.getAbsolutePath());
			fos.write(outBytes);
			fos.close();
		} catch (IOException e) {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			e.printStackTrace();
		}
	}

	public static void saveAsPNG(BufferedImage img, File outFile) {
		try {
			ImageIO.write(img, "png", outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
