package AltiSkinTool.image;

public class Sprite {

	public String spriteName;
	public int x;
	public int y;
	public int width;
	public int height;

	public Sprite() {
		this(null, -1, -1, -1, -1);
	}

	public Sprite(String spriteName, int x, int y, int width, int height) {
		this.spriteName = spriteName;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public String toString() {
		String s_sprite = "{";
		if (spriteName == null)
			s_sprite += "NULL";
		else
			s_sprite += spriteName;
		s_sprite += ",x = " + x;
		s_sprite += ",y = " + y;
		s_sprite += ",width = " + width;
		s_sprite += ",height = " + height;
		s_sprite += "}";
		return s_sprite;
	}
}
