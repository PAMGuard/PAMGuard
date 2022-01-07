package Localiser.algorithms;

import java.util.ArrayList;

import javax.vecmath.Point3f;

/**
 * Contains functions required to calculate a hyperboloid using time delays.
 * @author Jamie Macaulay
 *
 */
public class TDHyperboleUtils {
	
	/**
	 * Paramteric equation for x,y co-ordinates of a DOUBLY RULED SURFACE hyperboloid orientated along 
	 * the z axis and defined by the Cartesian equation (x^2/a^2)+(y^2/a^2)+(z^2/c^2)=1
	 * @param a-equation constant
	 * @param c equation constant
	 * @param z z axis position
	 * @param n number of x,y points;
	 * @return an array of xy points corresponding to the points of a 3D hyperboloid intersected by an xy plane at height z.  
	 */
	public static Point3f[] hyperboloidParXYDR(float a, float c, float z, int n){
		float u=z/c;
		Point3f[] xyz=new Point3f[n];
		for (int i=0; i<n; i++){
			Point3f point=new Point3f(); 
			point.setX((float) (a*Math.sqrt(1+Math.pow(u,2))*Math.cos((2*Math.PI/n)*i)));
			point.setY((float) (a*Math.sqrt(1+Math.pow(u,2))*Math.sin((2*Math.PI/n)*i)));
			point.setZ(z);
			xyz[i]=point;
		}
		return xyz;
	}
	
	/**
	 * Paramteric equation for x,y co-ordinates of a TWO SHEETED SURFACE hyperboloid orientated along the z axis and defined by the Cartesian equation (x^2/a^2)+(y^2/a^2)-(z^2/c^2)=-1
	 * @param a-equation constant
	 * @param c equation constant
	 * @param z z axis position
	 * @param n number of x,y points;
	 * @return an array of x,y points corresponding to the points of a 3D hyperboloid intersected by an x,y plane at height z.  
	 */
	public static Point3f[] hyperboloidParXTTS(float a, float c, float z, int n){
		
		float u=(float) Math.log((z/c)+Math.sqrt(Math.sqrt((z/c)+1)*Math.sqrt((z/c)-1)));
		
		Point3f[] xyz=new Point3f[n];
		
		for (int i=0; i<n; i++){
			
			Point3f point=new Point3f(); 
			point.setX((float) (a*Math.sinh(u)*Math.cos((2*Math.PI/n)*i)));
			point.setY((float) (a*Math.sinh(u)*Math.sin((2*Math.PI/n)*i)));
			point.setZ((float) (c*Math.cosh(u)));
			
			if (Double.isNaN(point.getX())) return null;
			xyz[i]=point;
		}
		
		return xyz;
	}
	
	/**
	 * Calculates a series of points for the hyperboloid defined by a time delay and between two hydrophones separated by hDistance;
	 * @param tD- time delay in seconds. 
	 * @param hDistance- distance between hydrophones.
	 * @param soundSpeed- sound speed in m/s.
	 * @param zEnd- the distance from centre of the array to calculate hyperboloid points for.
	 * @param nStrips- the number of strips calculated for the hyperboloid.
	 * @param nCircPoints- the number of points calculated for each strip. 
	 * @return. An ArrayList<Point3f[]> of strips defining the hyperboloid. Each Point3f[] contains a series of points defining a circle perpendicular to z. The circle consists of the points of interesection between the hyperboloid and an x,y plane at z. 
	 */
	public static ArrayList<Point3f[]>  calcTDHyperboloid(double tD, double hDistance, double soundSpeed, float zEnd, float nStrips, int nCircPoints){
		
		float a=(float) (tD*soundSpeed/2);
		float c=(float) Math.sqrt(Math.pow(hDistance/2,2)-Math.pow(a, 2));
		
//		System.out.println("a: "+a+" c: "+c);
		
		float zStart=0;
		
		Point3f[] strip;
		ArrayList<Point3f[]> hyperbStrips=new ArrayList<Point3f[]>();
		 
		for (int i=0; i<nStrips; i++){
			float z=zStart+i*(zEnd-zStart)/nStrips;
		 	strip=hyperboloidParXTTS( c,  a,  z, nCircPoints);
		 	if (strip==null) continue;
		 	hyperbStrips.add(strip);
		}
		
		if (hyperbStrips.size()<2){
			return null;
		}
		 
		return hyperbStrips;
		
	}

}
