package PamUtils;



/**
 * Created by IntelliJ IDEA.
 * User: Tomas
 * Date: 17-Mar-2010
 * Time: 20:13:26
 */
public class Point3d {
    double x;
    double y;
    double z;


    public Point3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double distance(Point3d target){
        return Math.sqrt(Math.pow(this.x-target.x,2)  + Math.pow(this.y-target.y,2) + Math.pow(this.z-target.z,2));
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }


    @Override
	public String toString() {
    return this.x+","+this.y+","+this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
