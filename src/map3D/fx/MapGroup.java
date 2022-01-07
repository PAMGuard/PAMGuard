package map3D.fx;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class MapGroup extends Group {

	public Translate t  = new Translate();
	public Rotate rx = new Rotate();
	{ rx.setAxis(Rotate.X_AXIS); }
	public Rotate ry = new Rotate();
	{ ry.setAxis(Rotate.Y_AXIS); }
	public Rotate rz = new Rotate();
	{ rz.setAxis(Rotate.Z_AXIS); }
	public Scale s = new Scale();
	
	public MapGroup() {
		super();
		getTransforms().addAll(rz, ry, rx, s, t);
	}

}
