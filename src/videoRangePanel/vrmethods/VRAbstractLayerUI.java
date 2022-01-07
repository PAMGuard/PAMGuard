package videoRangePanel.vrmethods;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

import videoRangePanel.VRControl;

public class VRAbstractLayerUI extends LayerUI<JPanel> {
	
	private static final long serialVersionUID = 1L;
	
	private VRControl vrControl;
	private Point currentMouse;
	
	/**
	 * Reference to the method
	 */
	private VRMethod method; 
 
	public VRAbstractLayerUI(VRControl vRControl, VRMethod method){
		this.method=method;
		this.vrControl=vRControl;
	}
	
	  @Override
	  public void installUI(JComponent c) {
	    super.installUI(c);
	    JLayer jlayer = (JLayer)c;
	    jlayer.setLayerEventMask(
	      AWTEvent.MOUSE_EVENT_MASK |
	      AWTEvent.MOUSE_MOTION_EVENT_MASK
	    );
	  }

	  @Override
	  public void uninstallUI(JComponent c) {
	    JLayer jlayer = (JLayer)c;
	    jlayer.setLayerEventMask(0);
	    super.uninstallUI(c);
	  }


	 @Override
	 public void paint(Graphics g, JComponent c) {
		 super.paint(g, c);
		 Graphics2D g2 = (Graphics2D) g;
		  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		          RenderingHints.VALUE_ANTIALIAS_ON);
		        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
		          RenderingHints.VALUE_RENDER_QUALITY);
		        //paint the marks for the method. 
		        method.getOverlayAWT().paintMarks(g2);
		

	}
	 
	///////////////////mouse events/////////////////////
	@Override
	protected void processMouseMotionEvent(MouseEvent e, JLayer l) {
		super.processMouseMotionEvent(e, l);
		vrControl.newMousePoint(vrControl.getVRPanel().screenToImage(e.getPoint()));
		currentMouse = e.getPoint();
		method.getOverlayAWT().mouseAction(e, true);
	//	checkHoverText(e.getPoint());
	}

	@Override
	protected void processMouseEvent(MouseEvent e, JLayer l) {
		super.processMouseEvent(e, l);
		if (e.getID() == MouseEvent.MOUSE_ENTERED){
			method.getOverlayAWT().mouseAction(e, true);
		} 
		else if (e.getID() == MouseEvent.MOUSE_EXITED){
			vrControl.newMousePoint(null);
			currentMouse = null;
			method.getOverlayAWT().mouseAction(e, true);
		}
		else  {
			//only send mouse clicks otherwise get pressed, released, clicked which
			//messes things up. 
			method.getOverlayAWT().mouseAction(e, false);
		}
	}

	public void setVRMethod(VRMethod currentMethod) {
		this.method=currentMethod; 
	}
	
//	protected void mouseClick(Point point){
//		
//	}


}

