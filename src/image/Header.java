package image;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Header {
	
	// Pixel format
	public final static int ALPHA8 	= 0;
	public final static int RGB5 	= 1;
	public final static int RGBA4 	= 2;
	public final static int RGBA8 	= 3;
	public final static int DXT1 	= 4;
	public final static int DXT1A 	= 5;
	public final static int DXT3 	= 6;
	public final static int DXT5 	= 7;
	
	// Compression type
	public final static int UNCOMPRESSED = 10000;
	public final static int DXT 		 = 10001;
	public final static int JPEG 		 = 10002;
	public final static int LZMA		 = 10003;
	
	//Repeat type
	public final static int CLAMPED = 0;
	public final static int REPEAT  = 1;
	
	//Filter type
	public final static int NEAREST_NEIGHBOUR = 0;
	public final static int LINEAR = 1;
	
	private int 		numberOfSprites;
	private Sprite[] 	sprites;
	private int 		pixelFormat;
	private int 		compressionType;
	private int 		repeatType;
	private int 		filterType;
	
	// TODO need to take into account multiple texture
	private int			numberOfTextures;
	private int			textureHeight;
	private int 		textureWidth;
	
	private int pIndex;
	
	private int header_end_index;
	
	private byte[] file;
	
	public Header(byte[] file){
		pIndex = 0;
		header_end_index = -1;
		this.file = file;
		readNumberOfSprites();
		readSpriteInfo();
		readMeta();
		
		//assumed single texture
		getTextureDimensions();
	}
	
	private void readNumberOfSprites(){
		byte[] be = {file[pIndex+3], file[pIndex+2], file[pIndex+1], file[pIndex]};
		pIndex+=4;
		numberOfSprites = ByteBuffer.wrap(be).getInt();
	}
	
	private void readSpriteInfo(){
		sprites = new Sprite[numberOfSprites];
		
		for(int i = 0; i < numberOfSprites; i++){
			int nameLength = (file[pIndex+1]<<8) | file[pIndex];
			pIndex+=2;
			
			byte[] b_name = new byte[nameLength];
			for(int j = 0; j < nameLength; j++)
				b_name[j] = file[pIndex+j];
			String spriteName = null;
			try { spriteName = new String(b_name, "UTF-8");} 
			catch (UnsupportedEncodingException e) {e.printStackTrace();}
			pIndex+=nameLength;
			
			int x_offset = ByteBuffer.wrap(new byte[]{
					file[pIndex+3], file[pIndex+2], file[pIndex+1], file[pIndex]
			}).getInt();
			pIndex+=4;
			
			int y_offset = ByteBuffer.wrap(new byte[]{
					file[pIndex+3], file[pIndex+2], file[pIndex+1], file[pIndex]
			}).getInt();
			pIndex+=4;
			
			int width = ByteBuffer.wrap(new byte[]{
					file[pIndex+3], file[pIndex+2], file[pIndex+1], file[pIndex]
			}).getInt();
			pIndex+=4;
			
			int height = ByteBuffer.wrap(new byte[]{
					file[pIndex+3], file[pIndex+2], file[pIndex+1], file[pIndex]
			}).getInt();
			pIndex+=4;
			
			sprites[i] = new Sprite(spriteName, x_offset, y_offset, width, height);
		}
	}
	
	private void readMeta(){
		int comp_format = ByteBuffer.wrap(new byte[]{
				file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
		}).getInt();
		pIndex+=4;
		
		if(comp_format<10000){
			pixelFormat = comp_format;
			compressionType = -1;
		}else{
			compressionType = comp_format;
			pixelFormat = ByteBuffer.wrap(new byte[]{
					file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
			}).getInt();
			pIndex+=4;
		}
		
		repeatType = ByteBuffer.wrap(new byte[]{
				file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
		}).getInt();
		pIndex+=4;
		
		filterType = ByteBuffer.wrap(new byte[]{
				file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
		}).getInt();
		pIndex+=4;
		
		//header_end_index = pIndex;
	}
	
	private void getTextureDimensions(){
		numberOfTextures = ByteBuffer.wrap(new byte[]{
				file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
		}).getInt();
		pIndex+=4;
		
		textureWidth = ByteBuffer.wrap(new byte[]{
				file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
		}).getInt();
		pIndex+=4;		
		
		textureHeight = ByteBuffer.wrap(new byte[]{
				file[pIndex], file[pIndex+1], file[pIndex+2], file[pIndex+3]
		}).getInt();
		pIndex+=4;
		
		header_end_index = pIndex;
	}
	
	public byte[] getBytes(){
		int hIndex = 0;
		byte[] b_header = new byte[headerSize()];
		
		System.out.println("EXPECTED SIZE = " + headerSize());
		
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(numberOfSprites); bb.position(0);
		bb.get(b_header, hIndex, 4); hIndex+=4;
		
		bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		for(Sprite sprite : sprites){
			bb.clear();
			bb.putShort((short)sprite.spriteName.length()); bb.position(0);
			bb.get(b_header, hIndex, 2); hIndex+=2;
			try {
				for(Byte b: sprite.spriteName.getBytes("UTF-8"))
					b_header[hIndex++] = b;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			ByteBuffer b4 = ByteBuffer.allocate(4);
			b4.order(ByteOrder.LITTLE_ENDIAN);
			b4.putInt(sprite.x); b4.position(0);
			b4.get(b_header, hIndex, 4); hIndex+=4;
			b4.clear();
			
			b4.putInt(sprite.y); b4.position(0);
			b4.get(b_header, hIndex, 4); hIndex +=4;
			b4.clear();
			
			b4.putInt(sprite.width); b4.position(0);
			b4.get(b_header, hIndex, 4); hIndex +=4;
			b4.clear();
			
			b4.putInt(sprite.height); b4.position(0);
			b4.get(b_header, hIndex, 4); hIndex +=4;
		}
		
		bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		
		if(compressionType != -1){
			bb.putInt(compressionType); bb.position(0);
			bb.get(b_header, hIndex, 4); hIndex +=4;
			bb.clear();
		}
		
		bb.putInt(repeatType); bb.position(0);
		bb.get(b_header, hIndex, 4); hIndex +=4;
		bb.clear();
		
		bb.putInt(filterType); bb.position(0);
		bb.get(b_header, hIndex, 4); hIndex +=4;
		bb.clear();
		
		bb.putInt(numberOfTextures); bb.position(0);
		bb.get(b_header, hIndex, 4); hIndex +=4;
		bb.clear();
		
		bb.putInt(textureWidth); bb.position(0);
		bb.get(b_header, hIndex, 4); hIndex +=4;
		bb.clear();
		
		bb.putInt(textureHeight); bb.position(0);
		bb.get(b_header, hIndex, 4);
		
		return b_header;
	}
	
	public int headerSize(){
		int size = 0;
		System.out.println("NUM of sprites = " + sprites.length);
		for(Sprite s:sprites) size+= s.spriteName.length();
		size += 28 + (18*numberOfSprites);
		if(compressionType != -1) size +=4;
		return size;
	}
	
	
	// GETTERS and SETTERS
	public int getNumberOfSprites() {
		return numberOfSprites;
	}

	public void setNumberOfSprites(int numberOfSprites) {
		this.numberOfSprites = numberOfSprites;
	}
	
	public Sprite[] getSprites() {
		return sprites;
	}
	public void setSprites(Sprite[] sprites) {
		this.sprites = sprites;
	}
	public int getPixelFormat() {
		return pixelFormat;
	}
	public void setPixelFormat(int pixelFormat) {
		this.pixelFormat = pixelFormat;
	}
	public int getCompressionType() {
		return compressionType;
	}
	public void setCompressionType(int compressionType) {
		this.compressionType = compressionType;
	}
	public int getRepeatType() {
		return repeatType;
	}
	public void setRepeatType(int repeatType) {
		this.repeatType = repeatType;
	}
	public int getFilterType() {
		return filterType;
	}
	public void setFilterType(int filterType) {
		this.filterType = filterType;
	}

	public int getHeader_end_index() {
		return header_end_index;
	}

	public int getTextureHeight() {
		return textureHeight;
	}

	public void setTextureHeight(int textureHeight) {
		this.textureHeight = textureHeight;
	}

	public int getTextureWidth() {
		return textureWidth;
	}

	public void setTextureWidth(int textureWidth) {
		this.textureWidth = textureWidth;
	}

	public int getNumberOfTextures() {
		return numberOfTextures;
	}

	public void setNumberOfTextures(int numberOfTextures) {
		this.numberOfTextures = numberOfTextures;
	}
	
	public String toString(){
		String s_header = "";
		s_header +=   "Number of sprites:  " + numberOfSprites;
		s_header += "\nSprites:            ";
		for(Sprite sprite: sprites) s_header += "\n                    " + sprite;
		s_header += "\nPixel Format:       " + pixelFormatToString(pixelFormat);
		s_header += "\nCompression Type:   " + compressionTypeToString(compressionType);
		s_header += "\nRepeat Type:        " + repeatTypeToString(repeatType);
		s_header += "\nFilter Type:        " + filterTypeToString(filterType);
		s_header += "\nNumber of textures: " + numberOfTextures;
		s_header += "\nTexture height:     " + textureHeight;
		s_header += "\nTexture width:      " + textureWidth;
		return s_header;
	}
	
	public String pixelFormatToString(int i){
		switch(i){
			case ALPHA8: return "ALPHA8";
			case RGB5:	 return "RGB5";
			case RGBA4:	 return "RGBA4";
			case RGBA8:	 return "RGBA8";
			case DXT1:	 return "DXT1";
			case DXT1A:	 return "DXT1A";
			case DXT3:   return "DXT3";
			case DXT5:	 return "DXT5";
			default: 	 return "INVALID FORMAT";
		}
	}

	public String compressionTypeToString(int i){
		switch(i){
			case UNCOMPRESSED: 	return "UNCOMPRESSED";
			case DXT: 		   	return "DXT";
			case JPEG: 			return "JPEG";
			case LZMA: 			return "LZMA";
			default: 			return "INVALID TYPE";
		}
	}
	
	public String repeatTypeToString(int i){
		switch(i){
			case CLAMPED: 	return "CLAMPED";
			case REPEAT: 	return "REPEAT";
			default: 		return "INVALID TYPE";
		}
	}
	
	public String filterTypeToString(int i){
		switch(i){
			case NEAREST_NEIGHBOUR: return "NEAREST_NEIGHBOUR";
			case LINEAR: 		   	return "LINEAR";
			default: 				return "INVALID TYPE";
		}
	}
}
