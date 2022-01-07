package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import videoRangePanel.VRControl;
import videoRangePanel.VRCursor;
import videoRangePanel.VRPane;
import videoRangePanel.VRParameters;
import videoRangePanel.VRSymbolManager;
import videoRangePanel.pamImage.PamImage;
import videoRangePanel.vrmethods.VRAbstractLayerUI;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.debug.Debug;

@SuppressWarnings("serial")
public class VRPanel extends JPanel implements VRPane {

	private VRControl vrControl;
		
	private BufferedImage scaledImage;
		
	private JScrollPane scrollPane;

	private JPanel picturePanel;
	
	private VRAbstractLayerUI currentLayerUi;
	
	private JLayer<JPanel> jlayer;

	int imageWidth, imageHeight; // image actual size in pixels. 
	int frameWidth, frameHeight; // viewable images size on screen.
	int panelWidth, panelHeight; // size of scaled image on screen. 
	int cropWidth, cropHeight; // viewable area of image. 
	double xScale = 1, yScale = 1;
	
	Point currentMouse;
	
	//symbols which can be used by multiple methods
	public static PamSymbol horizonMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.BLUE, Color.BLUE);
	public static PamSymbol animalMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.GREEN, Color.GREEN);
	public static PamSymbol candidateMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.RED, Color.RED);
	public static PamSymbol calibrationMarker = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.RED, Color.RED);
	public static VRSymbolManager horizonSymbol = new VRSymbolManager(horizonMarker, "Video Range Horizon");
	public static VRSymbolManager animalSymbol = new VRSymbolManager(animalMarker, "Video Range Animal");
	public static VRSymbolManager candidateSymbol = new VRSymbolManager(candidateMarker, "Video Range Candidate Animal");
	public static VRSymbolManager calibrationSymbol = new VRSymbolManager(calibrationMarker, "Video Range Calibration Mark. ");

	public static Dimension settingsButtonSize=new Dimension(40,25);
	
	
	@SuppressWarnings("unchecked")
	public VRPanel(VRControl vrControl) {
		super();
		this.vrControl = vrControl;
		this.setLayout(new BorderLayout());
		//create the layered pane. The innerPanel shows pictures and the layerUI creates the interactive mouse bits and pieces . 
		picturePanel = new InnerPanel();
		currentLayerUi=new VRAbstractLayerUI(vrControl, vrControl.getCurrentMethod());
		jlayer = new JLayer<JPanel>(picturePanel,currentLayerUi);
		
		add(BorderLayout.CENTER, scrollPane = new JScrollPane(jlayer));
		//add a bit of space for the side panels
		JPanel space=new JPanel(new BorderLayout());
		space.setPreferredSize(new Dimension(25,50));
		add(BorderLayout.EAST, space);

		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setAutoscrolls(true);
		VRMouseAdapter vRMouseAdapter=new VRMouseAdapter();
		
		jlayer.addMouseListener(vRMouseAdapter);
		jlayer.addMouseMotionListener(vRMouseAdapter);

	}
	
	public JPanel getPicturePanel() {
		return picturePanel;
	}

	boolean loadImageFromFile(File file) {
		long time0=System.currentTimeMillis();
		PamImage image=new PamImage(file, vrControl.getImageTimeParser());
		if (!image.imageOK()) return false;
		long time1=System.currentTimeMillis();
		vrControl.setCurrentImage(image);
		newImage();
		long time2=System.currentTimeMillis();
		Debug.out.println("VrPanel. time to create PamImage: "+(time1-time0)+ " time to call newImage(): "+(time2-time1));
		return (image.imageOK());
	}
	
	@SuppressWarnings("unchecked")
	boolean pasteImage() {
		
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            
                Image bimage = (Image) t.getTransferData(DataFlavor.imageFlavor);
                if (bimage == null) {
                	System.out.println("pasteImage().Pasted image is null:");
                	return false;
                }
                int w = bimage.getWidth(null);
                int h = bimage.getHeight(null);
                BufferedImage vrImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
                Graphics g = vrImage.getGraphics();
                g.drawImage(bimage, 0, 0, w, h, 0, 0, w, h, null);
                //create the pam image
        		PamImage image=new PamImage(vrImage);
        		vrControl.setCurrentImage(image);
                newImage();
        		return (image.imageOK());
            }
        } catch (UnsupportedFlavorException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        ///could be a copied image file!
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> imageFile = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                Debug.out.println("Pasted image file: "+imageFile.get(0));
                return loadImageFromFile(imageFile.get(0));   
            }
        }
        catch (UnsupportedFlavorException e) {
        	e.printStackTrace();
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        
        return false;

	}
	
	public void newImage() {
		if (vrControl.getCurrentImage() == null) {
			return;
		}
		sortScales();
		long time0=System.currentTimeMillis();
		cloneNewImage();
		long time1=System.currentTimeMillis();
		Debug.out.println("VRPanel: time to clone image: "+(time1-time0));
//		setImageBrightness();
		long time2=System.currentTimeMillis();
		Debug.out.println("VRPanel: time to set image brightness: "+(time2-time1));
		//reset the scroll pane
		anchor=new Point(0,0);
		scrollPane.repaint();
		long time3=System.currentTimeMillis();
		Debug.out.println("VRPanel: time to repaint image: "+(time3-time2));
	}
	
	@Override
	public void repaint() {
		super.repaint();
//		System.out.println("Repaint the VR Pane");
		if (jlayer != null && vrControl.getVRTabPanel()!=null) {
//			System.out.println("Repaint the JLayer Pane");
			jlayer.repaint();
			vrControl.getVRTabPanel().update(VRControl.REPAINT);
		}
	}
	
	private void cloneNewImage() {
		if (vrControl.getCurrentImage()==null)  return; 
		if (vrControl.getCurrentImage().getImage() == null) return;
		int w = vrControl.getCurrentImage().getImage().getWidth();
		int h =vrControl.getCurrentImage().getImage().getHeight();
		scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics g = scaledImage.getGraphics();
        g.drawImage(vrControl.getCurrentImage().getImage(), 0, 0, w, h, 0, 0, w, h, null);
	}
	
	private float brightness = 1;
	private float contrast = 10;
	public void setImageBrightness(float brightness, float contrast) {
		/*
		 * scaleFctor can be between 0 and 2
		 * offset can be between 0 and 255
		 */
//		float scaleFactor = 1 + brightness;
//		float offset = 10;
		this.brightness = brightness;
		this.contrast = contrast;
		setImageBrightness();
		this.repaint();
	}
	
	protected void setImageBrightness() {
		if (vrControl.getCurrentImage()==null)  return; 
		if (vrControl.getCurrentImage().getImage() == null || scaledImage == null) {
			return;
		}
		RescaleOp rescaleOp = new RescaleOp(brightness, contrast, null);
		
		rescaleOp.filter(vrControl.getCurrentImage().getImage(), scaledImage);
	}

	void sortScales() {
		sortScales(vrControl.getVRParams().imageScaling);
	}
	
	void sortScales(int scaleType) {
		
		if (vrControl.getCurrentImage()==null)  return; 
		if (vrControl.getCurrentImage().getImage() == null) return;
		
		double shrinkScaleX = 1, shrinkScaleY = 1;

		imageHeight = vrControl.getCurrentImage().getImage().getHeight(this);
		imageWidth = vrControl.getCurrentImage().getImage().getWidth(this);
		cropWidth = imageWidth;
		cropHeight = imageHeight;
		Insets frameInsets;
		switch (scaleType) {
		case VRParameters.IMAGE_CROP:
			showScrollBars(false);
			frameInsets = scrollPane.getInsets(); 
			frameWidth = scrollPane.getWidth() - frameInsets.right - frameInsets.left;
			frameHeight = scrollPane.getHeight() - frameInsets.top - frameInsets.bottom;
			cropWidth = panelWidth = Math.min(frameWidth, imageWidth);
			cropHeight = panelHeight = Math.min(frameHeight, imageHeight);
			xScale = yScale = 1;
			break;
		case VRParameters.IMAGE_SCROLL:
			showScrollBars(false);
			frameInsets = scrollPane.getInsets(); 
			frameWidth = scrollPane.getWidth() - frameInsets.right - frameInsets.left;
			frameHeight = scrollPane.getHeight() - frameInsets.top - frameInsets.bottom;
			panelWidth = imageWidth;
			panelHeight = imageHeight;
			xScale = yScale = 1;
			break;
		case VRParameters.IMAGE_SHRINK:
			showScrollBars(false);
			frameInsets = scrollPane.getInsets(); 
			frameWidth = scrollPane.getWidth() - frameInsets.right - frameInsets.left;
			frameHeight = scrollPane.getHeight() - frameInsets.top - frameInsets.bottom;
			shrinkScaleX = 1; shrinkScaleY = 1;
			if (imageWidth > frameWidth) {
				shrinkScaleX = (double) frameWidth / imageWidth;
			}
			if (imageHeight > frameHeight) {
				shrinkScaleY = (double) frameHeight / imageHeight;
			}
			xScale = yScale = Math.min(shrinkScaleX, shrinkScaleY);
			panelWidth = (int) Math.floor(imageWidth * xScale);
			panelHeight = (int) Math.floor(imageHeight * yScale);
			break;
		case VRParameters.IMAGE_SHRINKORSTRETCH:
		case VRParameters.IMAGE_STRETCH:
		case VRParameters.MOUSE_WHEEL_CONTROL:
			showScrollBars(false);
			frameInsets = scrollPane.getInsets(); 
			frameWidth = scrollPane.getWidth() - frameInsets.right - frameInsets.left;
			frameHeight = scrollPane.getHeight() - frameInsets.top - frameInsets.bottom;
			shrinkScaleX = 1; shrinkScaleY = 1;
			if (imageWidth > frameWidth) {
				shrinkScaleX = (double) frameWidth / imageWidth;
			}
			if (imageHeight > frameHeight) {
				shrinkScaleY = (double) frameHeight / imageHeight;
			}
			xScale = yScale = Math.min(shrinkScaleX, shrinkScaleY);
			panelWidth = (int) Math.floor(imageWidth * xScale);
			panelHeight = (int) Math.floor(imageHeight * yScale);
			break;
		}
		
//		System.out.println("Sort scales width, height = " + imageWidth + ", " + imageHeight);
		
		//need to make sure the jlayer and picture is set to the correct size. 
		picturePanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
		jlayer.setSize(new Dimension(panelWidth, panelHeight));
		scrollPane.invalidate();
	}
	
	private void showScrollBars(boolean show) {
		if (show) {
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
		else {
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		}
		scrollPane.invalidate();
	}
	
//	@Override
//	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
//		// from ImageObserver interface.
//		boolean ans = super.imageUpdate(img, infoflags, x, y, width, height);
//		repaint();
//		System.out.println("Image update called");
//		return ans;
//	}
	
	
	class InnerPanel extends JPanel {

		public InnerPanel() {
			super();
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(31, 31);
			VRCursor cursor = new VRCursor(d);
			setCursor(cursor.getCursor());
			
		}

		@Override
		protected void paintComponent(Graphics g) {
//			sortScales();
			super.paintComponent(g);
			if (vrControl.getCurrentImage()==null) return;
			if (vrControl.getCurrentImage().getImage() == null) {
//				String msg = "No Image";
				return;
			}
			if (scaledImage == null) {
				scaledImage = vrControl.getCurrentImage().getImage();
			}
			g.drawImage(scaledImage, 0, 0, panelWidth, panelHeight, 0, 0, cropWidth, cropHeight, this);

		}
	}

	Point anchor=new Point(0,0);
	
	private class VRMouseAdapter extends MouseAdapter{
		
			@Override
	        public void mousePressed(MouseEvent e) {
				Point mousePos=e.getPoint();
			    anchor.x=mousePos.x;
	            anchor.y=mousePos.y;
			}
		 
            @Override
            public void mouseDragged(MouseEvent e) {
            	if (vrControl.getVRParams().imageScaling==VRParameters.IMAGE_SCROLL){
	            	Point mousePos=e.getPoint();
	            	            	
	            	JViewport viewport = scrollPane.getViewport();
	            	
	            	int dx=anchor.x-mousePos.x;
	            	int dy=anchor.y-mousePos.y;
	            	
	            	anchor.x=mousePos.x;
	            	anchor.y=mousePos.y;
	            	
	            	int x = viewport.getViewPosition().x + dx;
	            	int y = viewport.getViewPosition().y + dy;
	            	
	            	viewport.setViewPosition(new Point(x,y));
	
	                scrollPane.repaint();
	                vrControl.getVRTabPanel().update(VRControl.REPAINT);
            	}
            }  
		
	}

	public void zoomPicture(){

	}

	
//	class VRMouseAdapter extends MouseAdapter {
//
//		@Override
//		public void mouseClicked(MouseEvent e) {
//
//			super.mouseClicked(e);
//			if (e.getButton() == MouseEvent.BUTTON1) {
//				vrControl.mouseClick(screenToImage(e.getPoint()));
//				picturePanel.repaint();
//			}
//		}
//
//		@Override
//		public void mouseMoved(MouseEvent e) {
//			super.mouseMoved(e);
//			vrControl.newMousePoint(screenToImage(e.getPoint()));
//			currentMouse = e.getPoint();
//			if (vrControl.getVrSubStatus() == VRControl.MEASURE_HORIZON_2 || 
//					vrControl.getVrSubStatus() == VRControl.CALIBRATE_2) {
//				picturePanel.repaint();
//			}
//			checkHoverText(e.getPoint());
//		}
//		
//		private void checkHoverText(Point mousePoint) {
//			Point imagePoint = screenToImage(mousePoint);
//			double closest = 50;
//			double newDist;
//			String txt = null;
//			Point tp;
//			tp = vrControl.getHorizonPoint1();
//			if (tp != null)
//				if ((newDist = imagePoint.distance(tp)) < closest) {
//				txt = "Horizon point 1";
//				closest = newDist;
//			}
//			tp = vrControl.getHorizonPoint2();
//			if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//				txt = "Horizon point 2";
//				closest = newDist;
//			}
//			tp = vrControl.getShorePoint();
//			if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//				txt = "Shore point";
//				closest = newDist;
//			}
//			tp = vrControl.getCalibratePoint1();
//			if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//				txt = "Calibration point 1";
//				closest = newDist;
//			}
//			tp = vrControl.getCalibratePoint2();
//			if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//				txt = "Calibration point 2";
//				closest = newDist;
//			}
//			ArrayList<VRMeasurement> vrms = vrControl.getMeasuredAnimals();
//			VRMeasurement vrm;
//			if (vrms != null) for (int i = 0; i < vrms.size(); i++) {
//				vrm = vrms.get(i);
//				tp = vrm.animalPoint;
//				if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//					closest = newDist;
////					txt = String.format("<html>Animal measurement %d, range %.1f m", vrm.imageAnimal, vrm.distanceMeters);
////					if (vrm.comment != null && vrm.comment.length() > 0) {
////						txt += "<br>" + vrm.comment;
////					}
////					txt += "</html>";
//					txt = "<html>Animal measurement<br>" + vrm.getHoverText();
//				}
//				tp = vrm.horizonPoint;
//				if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//					closest = newDist;
//					txt = String.format("<html>Horizon intercept animal %d, range %.1f m", vrm.imageAnimal, vrm.distanceMeters);
//					if (vrm.comment != null && vrm.comment.length() > 0) {
//						txt += "<br>" + vrm.comment;
//					}
//					txt += "</html>";
//				}
//			}
//			if ((vrm = vrControl.getCandidateMeasurement()) != null) {
//				tp = vrm.animalPoint;
//				if (tp != null && (newDist = imagePoint.distance(tp)) < closest) {
//					closest = newDist;
//					txt = "<html>Canididate measurement<br>" + vrm.getHoverText();
//				}
//				
//			}
//			if (txt != null) {
//				picturePanel.setToolTipText(txt);
//			}
//			else {
//				picturePanel.setToolTipText(vrControl.getInstruction());
//			}
//				
//		}
////		private getDist()
//
//		@Override
//		public void mouseExited(MouseEvent e) {			
//			super.mouseExited(e);
//			vrControl.newMousePoint(null);
//			currentMouse = null;
//		}
//		
//	}
	
	public Point screenToImage(Point screenPoint) {
		Point p = new Point(screenPoint);
		p.x = (int) (p.x / xScale);
		p.y = (int) (p.y / yScale);
		return p;
	}
	public Point imageToScreen(Point screenPoint) {
		Point p = new Point(screenPoint);
		p.x = (int) (p.x * xScale);
		p.y = (int) (p.y * yScale);
		return p;
	}
	

	public int getImageHeight() {
		return imageHeight;
	}

	public int getImageWidth() {
		return imageWidth;
	}
	

	@SuppressWarnings("unchecked")
	public void changeMethod() {
//		jlayer.setUI(new MethodOverlayUI(vrControl, vrControl.getCurrentMethod()));
		this.currentLayerUi.setVRMethod(vrControl.getCurrentMethod());
		//need tp sort scales as panels may have changed. 
		this.validate();
		this.repaint();
	}
	

}
