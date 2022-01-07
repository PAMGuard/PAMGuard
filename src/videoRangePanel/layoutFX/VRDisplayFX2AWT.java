package videoRangePanel.layoutFX;

import java.awt.Point;
import java.io.File;

import javafx.scene.layout.Pane;
import videoRangePanel.VRControl;
import videoRangePanel.VRPane;

/**
 * A wrapper to allow an FX pane to be become part of the tab p[ane. 
 * @author macst
 *
 */
public class VRDisplayFX2AWT implements VRPane {
	
	/**
	 * Reference to the VR control.
	 */
	private VRControl vrControl;
	
	/**
	 * The VRDisplay. 
	 */
	public VRDisplayFX vRDisplayFX; 

	public VRDisplayFX2AWT(VRControl vrControl){
		this.vrControl=vrControl; 
	}
	
	protected void createPane(){
		vRDisplayFX=new VRDisplayFX(vrControl);
//		vRDisplayFX.prefWidthProperty().bind(this.widthProperty());
//		vRDisplayFX.prefHeightProperty().bind(vRDisplayFX.heightProperty());	

	}

	@Override
	public void repaint() {
		if (vRDisplayFX!=null)
		vRDisplayFX.repaint();
	}

	@Override
	public int getImageWidth() {
		return vRDisplayFX.getImageWidth();
	}

	@Override
	public int getImageHeight() {
		return vRDisplayFX.getImageHeight();
	}

	@Override
	public Point imageToScreen(Point p1) {
		return vRDisplayFX.imageToScreen(p1);
	}

	@Override
	public Point screenToImage(Point p1) {
		return  vRDisplayFX.screenToImage(p1);
	}

	@Override
	public void newImage() {
		 vRDisplayFX.newImage();
	}

	public VRDisplayFX getPane() {
		return vRDisplayFX;
	}

	public void update(int updateType) {
		if (vRDisplayFX!=null) {
			vRDisplayFX.update(updateType); 
		}
	}

	/**
	 * Load a an image of media file
	 * @param file - the file to load
	 * @return true if the file loads successfully
	 */
	public boolean openImageFile(File file) {
		return vRDisplayFX.openImageFile(file);
	}


}
