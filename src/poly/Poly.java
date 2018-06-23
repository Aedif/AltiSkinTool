package poly;

public class Poly {
    public String poly_file;
	public int[] colours;
	public float[] coordinates;
	public String texture_path;
	public float scale;
	public float rotation;
	public boolean flipX;
	public boolean flipY;
	public float translateY;
	public float translateX;
	public int delay;
	
	public Poly(String poly_file, int[] colours, float[] coordinates, String texture_path, float scale, float rotation,
			boolean flipX, boolean flipY, float translateY, float translateX){
		this.poly_file = poly_file;
	    this.colours = colours;
		this.coordinates = coordinates;
		this.texture_path = texture_path;
		this.scale = scale;
		this.rotation = rotation;
		this.flipX = flipX;
		this.flipY = flipY;
		this.translateY = translateY;
		this.translateX = translateX;
	}
	
	public String coordinatesToString(){
	    if(coordinates == null) return "";
	    String coordString = "!-1,";
	    for(Float f : coordinates)
	        coordString += Float.toString(f) + ",";
	    return coordString;
	}
}
