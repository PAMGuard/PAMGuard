package pamViewFX.threeD;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

public class Utils3D {
	
	public static Group buildAxes(double axisSize, Color colour, String textx, String texty, String textz,
			Color textColour) {
		return buildAxes(axisSize,  colour,  colour,
				colour,  colour,
				 colour,  colour,
				 textColour, textx, texty, textz);
	}
	
	public static Group buildAxes(double axisSize, Color xAxisDiffuse, Color xAxisSpectacular,
			Color yAxisDiffuse, Color yAxisSpectacular,
			Color zAxisDiffuse, Color zAxisSpectacular,
			Color textColour) {
		return buildAxes(axisSize,  xAxisDiffuse,  xAxisSpectacular,
				 yAxisDiffuse,  yAxisSpectacular,
				 zAxisDiffuse,  zAxisSpectacular,
				 textColour, "x", "y", "z");
	}
	
	/**
	 * Create a 3D axis. 
	 * @param- size of the axis
	 */
	public static Group buildAxes(double axisSize, Color xAxisDiffuse, Color xAxisSpectacular,
			Color yAxisDiffuse, Color yAxisSpectacular,
			Color zAxisDiffuse, Color zAxisSpectacular,
			Color textColour, String textx, String texty, String textz) {
		Group axisGroup=new Group(); 
        double length = 2d*axisSize;
        double width = axisSize/100d;
        double radius = 2d*axisSize/100d;
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(xAxisDiffuse);
        redMaterial.setSpecularColor(xAxisSpectacular);
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(yAxisDiffuse);
        greenMaterial.setSpecularColor( yAxisSpectacular);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(zAxisDiffuse);
        blueMaterial.setSpecularColor(zAxisSpectacular);
        
        Text xText=new Text(textx); 
        xText.setStyle("-fx-font: 20px Tahoma;");
        xText.setFill(textColour);
        xText.setCache(true);
        Text yText=new Text(texty); 
        yText.setStyle("-fx-font: 20px Tahoma; ");
        yText.setFill(textColour);
        yText.setCache(true);
        Text zText=new Text(textz); 
        zText.setStyle("-fx-font: 20px Tahoma; ");
        zText.setCache(true);
        zText.setFill(textColour);

        xText.setTranslateX(axisSize*1.1);
        yText.setTranslateY(-(axisSize*1.1));
        zText.setTranslateZ((axisSize*1.1));
        zText.getTransforms().add(new Rotate(90, new Point3D(0,1,0)));
        
        Sphere xSphere = new Sphere(radius);
        Sphere ySphere = new Sphere(radius);
        Sphere zSphere = new Sphere(radius);
        xSphere.setMaterial(redMaterial);
        ySphere.setMaterial(greenMaterial);
        zSphere.setMaterial(blueMaterial);
         
        xSphere.setTranslateX(axisSize);
        ySphere.setTranslateY(-axisSize);
        zSphere.setTranslateZ(axisSize);
         
        Box xAxis = new Box(length, width, width);
        Box yAxis = new Box(width, length, width);
        Box zAxis = new Box(width, width, length);
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
         
        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.getChildren().addAll(xText, yText, zText);
        axisGroup.getChildren().addAll(xSphere, ySphere, zSphere);
        return axisGroup;
    }

}
