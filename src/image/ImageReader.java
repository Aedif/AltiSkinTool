package image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

//TODO handle JPEG compressed images

public class ImageReader {
	public static byte[] readImageBytes(Header header, byte[] file){
		return Arrays.copyOfRange(file, header.getHeader_end_index(), file.length);
	}
	
	public static BufferedImage readImage(Header header, byte[] file){
		return constructImage(readImageBytes(header, file), 
				header.getTextureHeight(), header.getTextureWidth());
	}
	
	private static BufferedImage constructImage(byte[] bytes, int height, int width){

		DataBuffer buffer = new DataBufferByte(bytes, bytes.length);

		//3 bytes per pixel: red, green, blue
		WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 4 * width, 4, new int[] {0, 1, 2, 3}, (Point)null);
		ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE); 
		BufferedImage image = new BufferedImage(cm, raster, cm.isAlphaPremultiplied(), null);
		return image;
	}
}
