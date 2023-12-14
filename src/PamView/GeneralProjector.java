/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamView;

import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import PamController.PamController;
import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.symbol.PamSymbolChooser;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         GeneralProjector is an abstract class used to draw any type of
 *         information contained within a PamDataBlock on a display (e.g. Map,
 *         spectrogram, etc).
 *         <p>
 *         The display must set the parameter types and the units (these lists
 *         can be added to as necessary)
 *         <p>
 *         Drawing is all done via calls that the display makes to
 *         PanelOverlayDraw.DrawDataUnit(...)
 *         <p>
 *         The DataBlocks using the projector will need to check parameter types
 *         and units when parsed a projector to check that they can provide
 *         these parameters.
 *         <p>
 *         The owning display will need to create concrete instances of
 *         GeneralProjector which implement getCoord3d. It is also possible that
 *         they will need to create other functions which are called by the
 *         display to set scaling parameters, scale offsets or other information
 *         Required by the projector that may change with time. If a display
 *         requires many types of similar projectors (e.g. different map
 *         projections) then it will be necessary to create a special interface
 *         to manage these functions and to implement that interface in all
 *         concrete classes used by that display.
 *         
 *         @see PamUtils.Coordinate3d
 *         @see PanelOverlayDraw
 *        
 * 
 */
public abstract class GeneralProjector<T extends PamCoordinate> {

	static public enum ParameterType {
		TIME ("Time"), FREQUENCY ("Frequency"), AMPLITUDE ("Amplitude"), LATITUDE ("Latitude"), LONGITUDE ("Longitude") ,
		BEARING ("Bearing"), RANGE ("Range"), SLANTANGLE ("Slant angle"), ICI ("Inter-click-interval"),
		DEPTH ("Depth"), SLANTBEARING ("Slant bearing"), AMPLITUDE_STEM ("Amplitude (stem)"), AMPLITUDE_LIN ("Linear Amplitude"),
		SPEED ("Speed"), PROBABILITY ("Probability"),  NCYCLES ("No. cycles"), BANDWIDTH("Bandwidth"), ISHDET("Detector Output"), X("x coordinate"), Y("y coordinate");
		
		private String unit;

		ParameterType(String unit) {
			this.unit = unit;
		}

		@Override
		public String toString() {
			return unit;
		}
		
		
	};

	static public enum ParameterUnits {
		SECONDS ("s"), HZ ("Hz"), DB ("dB"), RAW ("raw"), DECIMALDEGREES ("\u00B0"), METERS ("m"), 
		NMILES ("nmi"), DEGREES ("\u00B0"), RADIANS ("rad"), METRESPERSECOND ("m/s"), PROBABILITY ("p"),
		N("N"), NONE("");
		
		private String unit;

		ParameterUnits(String unit) {
			this.unit = unit;
		}

		@Override
		public String toString() {
			if (this == ParameterUnits.DB) {
				//get the correct dB reference. 
				 return PamController.getInstance().getGlobalMediumManager().getdBRefString(); 
			}
			else return unit;
		}
		
		
//		public static String toStr() {
//			switch (this) {
//			
//			}
//		}
	};

	static public final int NPARAMETERS = 3;

	private ParameterType[] parameterTypes = new ParameterType[NPARAMETERS];

	private ParameterUnits[] parameterUnits = new ParameterUnits[NPARAMETERS];

	/**
	 * Flag to check that the clear function is being called, if not,
	 * then it will not be possible to add hover data to the list. 
	 */
	//	private boolean isHoverClearing = false;

	private DataSelector dataSelector;

	private int minHoverDistance = 10;

	private ProjectorDrawingOptions projectorDrawingOptions;

	/**
	 * List of points for whihc there is hover data. 
	 */
	private List<HoverData> hoverData = Collections.synchronizedList(new LinkedList<HoverData>());

	/**
	 * This should effectively be a list of everything that just got displayed on 
	 * whatever was using this projector. Get's used when marking stuff out. 
	 * @return the hoverData
	 */
	public List<HoverData> getHoverDataList() {
		return hoverData;
	}

	private PamDataUnit hoveredDataUnit;

	private PamSymbolChooser pamSymbolChooser = null;

	protected boolean viewer = (PamController.getInstance().getRunMode()== PamController.RUN_PAMVIEW);

	private Object hoverDataSynchroniser = new Object();

	/**
	 * Function ultimately used by a PamDataBlock to convert it's own data, in
	 * whatever form that is in into screen coordinates.
	 * 
	 * @param d1
	 *            d2 and d3 are data representing whatever is appropriate for
	 *            the concrete instance of the projector (e.g. Latitude,
	 *            Longitude, depth, Time Frequency, etc)
	 * @return A 3 dimensional coordinate (realistically z is never currently
	 *         used)
	 */
	abstract public Coordinate3d getCoord3d(double d1, double d2, double d3);

	/**
	 * Same as getCoordinate3d but using the generic type
	 * @param dataObject object extending PamCoordinate
	 * @return 3d coordinate. 
	 */
	abstract public Coordinate3d getCoord3d(T dataObject);

	/**
	 * 
	 * Do the exact opposite of getCoord3d and turn a screen position back
	 * into a data coordinate (e.g. a time / freq, a lat long, etc)/.
	 * @param screenPosition screen position
	 * @return data object. 
	 */
	abstract public T getDataPosition(PamCoordinate screenPosition);

	/**
	 * Returns the parameter type for a specific data type required by
	 * Coordinate3d
	 * 
	 * @param iDim
	 *            Dimension number (0 - 2)
	 * @return enum ParameterType
	 */
	public ParameterType getParmeterType(int iDim) {
		return parameterTypes[iDim];
	}

	/**
	 * @return the full list of parameter types. 
	 */
	public ParameterType[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Sets the parameter type for a specific data type required by Coordinate3d
	 * 
	 * @param iDim
	 *            dimension number (0 - 2)
	 * @param parmeterType
	 *            Parameter Type (see enum ParmaeterType)
	 */
	public void setParmeterType(int iDim, ParameterType parmeterType) {
		this.parameterTypes[iDim] = parmeterType;
	}

	/**
	 * Returns the prameter unit for a specific data type required by
	 * Coordinate3d
	 * 
	 * @param iDim
	 *            Dimension number (0 - 2)
	 * @return enum ParameterUnit
	 */
	public ParameterUnits getParmeterUnits(int iDim) {
		return parameterUnits[iDim];
	}

	/**
	 * Sets the parameter unit for a specific data type required by Coordinate3d
	 * 
	 * @param iDim
	 *            Dimension number (0 - 2)
	 * @param parmeterUnits
	 *            enum ParameterUnit
	 */
	public void setParmeterUnits(int iDim, ParameterUnits parmeterUnits) {
		this.parameterUnits[iDim] = parmeterUnits;
	}

	/**
	 * 
	 * @return full list of parameter units. 
	 */
	public ParameterUnits[] getParameterUnits() {
		return parameterUnits;
	}

	MouseHoverAdapter mouseHoverAdapter;

	JComponent toolTipComponent;

	/**
	 * Gets an adapter that can provide tooltips automatically based on plotted data units. 
	 * @param component
	 * @return
	 */
	public MouseHoverAdapter getMouseHoverAdapter(JComponent component) {
		ToolTipManager tt = ToolTipManager.sharedInstance();
		tt.registerComponent(component);
		toolTipComponent = component;
		toolTipComponent.setToolTipText(null);
		if (mouseHoverAdapter == null) {
			mouseHoverAdapter = new MouseHoverAdapter(this);
		}
		return mouseHoverAdapter;
	}

	/**
	 * @return data unit currently being hovered, or null.
	 */
	public PamDataUnit getHoveredDataUnit() {
		return hoveredDataUnit;
	}

	class MouseHoverAdapter  implements ActionListener, MouseMotionListener, MouseListener {

		Timer timer;

		Point mousePoint = null;

		GeneralProjector generalProjector;


		public MouseHoverAdapter(GeneralProjector generalProjector) {
			super();
			this.generalProjector = generalProjector;
			timer = new Timer(10,this);
		}

		public void mouseExited(MouseEvent e) {
			hideHoverItem();
			timer.stop();
		}

		public void mouseMoved(MouseEvent e) {
			mousePoint = e.getPoint();
			hideHoverItem();
			timer.restart();
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			timer.start();
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == timer) {
				showHoverItem();
			}

		}

		public boolean showHoverItem() {
			hideHoverItem();
			timer.stop();
			String hoverText = getHoverText(mousePoint);
			toolTipComponent.setToolTipText(hoverText);
			return (hoverText != null);
		}

		public void hideHoverItem() {
			timer.restart();
		}

		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}
	public String getHoverText(Point mousePoint) {
		return getHoverText(mousePoint, -1);
	}
	
	public String getHoverText(Point mousePoint, int ploNumberMatch) {

		hoveredDataUnit = null;
		if (mousePoint == null) return null;
		//			System.out.println("Mouse at (" + mousePoint.x + "," + mousePoint.y + ")");
		//				timer.stop();


		/**
		 * Have a choice here. Hover data may be inside shape or at a point. A point maybe inside 
		 * a shape. Give points priority,  
		 */
		int unitIndex = findClosestDataUnitIndex(new Coordinate3d(mousePoint.x, mousePoint.y), minHoverDistance, ploNumberMatch);
		if (unitIndex < 0 || unitIndex >= hoverData.size()){
			return null;
		}


		hoveredDataUnit = hoverData.get(unitIndex).getDataUnit();
		if (hoveredDataUnit == null) return null;
		PamDataBlock dataBlock = hoveredDataUnit.getParentDataBlock();
		if (dataBlock == null) {
			return hoveredDataUnit.getSummaryString();
		}
		String hintText = dataBlock.getHoverText(this, hoveredDataUnit, hoverData.get(unitIndex).getAmbiguity());

		if (hintText == null) {
			return null;
		}
		//			System.out.println(hintText);
		return hintText;
	}

	/**
	 * Any display that is using click hovering should call this at the start of their 
	 * paintComponent(Graphics g) function. The list will be repopulated as data are drawn in the
	 * PanelOverlayDraw implementations. 
	 *
	 *@see PanelOverlayDraw
	 */
	synchronized public void clearHoverList() {
		//		isHoverClearing = true;
		hoverData.clear();
	}

	/**
	 * 
	 * @param coordinate3d 3D coordinate of point plotted on map
	 * @param pamDataUnit corresponding data unit
	 * @return true
	 */
	synchronized public boolean addHoverData(Coordinate3d coordinate3d, PamDataUnit pamDataUnit) {
		return addHoverData(coordinate3d, pamDataUnit, 0);
	}

	/**
	 * Add hover data to the projector. 
	 * @param hoverData
	 */
	synchronized public void addHoverData(HoverData hoverData) {
		this.hoverData.add(hoverData);
	}
	/**
	 * 
	 * @param coordinate3d 3D coordinate of point plotted on map
	 * @param pamDataUnit corresponding data unit
	 * @param iSide either 0 or 1 (the index, not +/- 1) 
	 * @return true
	 */
	synchronized public boolean addHoverData(Coordinate3d coordinate3d, PamDataUnit pamDataUnit, int iSide) {
		//		if (isHoverClearing == false) return false;
		HoverData hoverData=new HoverData(); 
		hoverData.setCoordinate3D(coordinate3d); 
		hoverData.setDataUnit(pamDataUnit); 
		hoverData.setAmbiguity(iSide); 
		this.hoverData.add(hoverData); 
		return true;
	}

	synchronized public boolean addHoverData(Shape drawnShape, PamDataUnit pamDataUnit) {
		HoverData hoverData=new HoverData(); 
		hoverData.setDataUnit(pamDataUnit); 
		hoverData.setDrawnShape(drawnShape);
		hoverData.setAmbiguity(0); 
		this.hoverData.add(hoverData); 
		return true;
	}

	/**
	 * Add hover data to the projector
	 * @param shape shape plotted on the map
	 * @param pamDataUnit corresponding data unit
	 */
	public void addHoverData(TransformShape shape, PamDataUnit pamDetection) {
		addHoverData(shape, pamDetection, 0); 		
	}

	/**
	 * Add hover data to the projector
	 * @param shape shape plotted on the map
	 * @param pamDataUnit corresponding data unit
	 * @param iSide either 0 or 1 (the index, not +/- 1) 
	 * @return true
	 */
	synchronized public boolean addHoverData(TransformShape shape, PamDataUnit pamDetection, int Side) {
		if (shape==null) return false; 
		HoverData hoverData=new HoverData(); 
		hoverData.setTransformShape(shape); 
		hoverData.setDataUnit(pamDetection); 
		this.hoverData.add(hoverData); 
		return true;
	}

	public int findClosestDataUnitIndex(Coordinate3d coordinate3d) {
		return findClosestDataUnitIndex(coordinate3d, minHoverDistance, -1);

	}

	synchronized public int findClosestDataUnitIndex(Coordinate3d cTest, int minDistance, int subPlotMatch){

		/*
		 * Go through all stored coordinates and see which is closest. 
		 * 
		 */
		double closestDistance = minDistance;
		double dist;
		Coordinate3d c3d;
		//		PamDataUnit closestUnit = null;
		int closestIndex = -1;

		Iterator<HoverData> hoverIterator = hoverData.iterator();
		//		for (int i = 0; i < hoverCoordinates.size(); i++) {
		//			c3d = hoverCoordinates.get(i);
		//first 
		int i = 0;
		while (hoverIterator.hasNext()) {
			//			c3d = hoverIterator.next().coordinate3d;
			//			if (c3d==null) continue;
			//			dist = Math.pow(c3d.x - cTest.x,2) + Math.pow(c3d.y - cTest.y,2) + Math.pow(c3d.z - cTest.z,2);
			HoverData hoverData = hoverIterator.next();
			if (hoverData.getSubPlotNumber() != -1 && hoverData.getSubPlotNumber() != subPlotMatch) {
				i++;
				continue;
			}
			dist = hoverData.distFromCentre(cTest.x, cTest.y);
			if (dist <= closestDistance) {
				closestDistance = dist;
				closestIndex = i;
				//				closestUnit = hoverDataUnits.get(i);
			}
			i++;
		}


		//System.out.println("ClosestDistance  "+closestDistance+" index: "+closestIndex);

		if (closestIndex>-1) {
			return closestIndex;
		}

		hoverIterator = hoverData.listIterator(0);
		TransformShape trsShape; 
		closestDistance=Double.MAX_VALUE; 

		while (hoverIterator.hasNext()) {


			trsShape = hoverIterator.next().getTransfromShape();

			//System.out.println("Transform Shape  "+ trsShape);


			if (trsShape==null) continue; 
			Shape drawnShape = trsShape.getShape();
			if (drawnShape == null) continue;

			//first is the point inside the shape, only 2D. 
			Point2D p=cTest.getPoint2D();
			
			/**
			 * Slightly different here. Hover data is only allowed if the inside the shape. But, what if we have a shape within a shape?
			 * And what if the centre of both shapes is exactly the same! In this case we need to find the unit in which the hover 
			 * position is closest to the boundary 
			 **/
			if (trsShape.getOrigin() != null) {
				//System.out.println("Origin: "+ trsShape.getOrigin().getX() + " "+ trsShape.getOrigin().getY());
				AffineTransform transform=AffineTransform.getRotateInstance(-trsShape.getAngle());			
				Point2D shapeLoc=new Point2D.Double(cTest.x-trsShape.getOrigin().getX(), 
						cTest.y-trsShape.getOrigin().getY());
				p=transform.transform(shapeLoc,null);
				//p = trsShape.getTransform().inverseTransform(new Point2D.Double(cTest.x-trsShape.getOrigin().getX(), cTest.y-trsShape.getOrigin().getY()), null);
				p.setLocation(p.getX()+trsShape.getOrigin().getX(), p.getY()+trsShape.getOrigin().getY());
			}
			//System.out.println("Before: "+ cTest.x + " "+ cTest.y+ " After: "+p.getX()+ " "+p.getY());

			if (drawnShape.contains(p)){
				//OK so the point is inside the shape. Now  to check the distance to the shape boundry
				Point2D point =PamAWTUtils.findClosestBoundry(trsShape.getShape(), p); 
				dist= point.distance(cTest.x, cTest.y); 
				//System.out.println("GeneralProjector: Min dist: "+dist);
				if (dist <= closestDistance) {
					closestDistance = dist;
					closestIndex = i;
					//					closestUnit = hoverDataUnits.get(i);
				}
			} 
			
			i++;
		}
		//		System.out.println("Shape index "+closestIndex);

		return closestIndex; 
	}

	/**
	 * @return the viewer
	 */
	public boolean isViewer() {
		return viewer;
	}


	/**
	 * @return the dataSelector
	 */
	public DataSelector getDataSelector() {
		return dataSelector;
	}

	/**
	 * @param dataSelector the dataSelector to set
	 */
	public void setDataSelector(DataSelector dataSelector) {
		this.dataSelector = dataSelector;
	}

	/**
	 * @return The projectorDrawingOptions. <br>!! Note that these will often be null !!
	 */
	public ProjectorDrawingOptions getProjectorDrawingOptions() {
		return projectorDrawingOptions;
	}

	/**
	 * @param projectorDrawingOptions the projectorDrawingOptions to set
	 */
	public void setProjectorDrawingOptions(ProjectorDrawingOptions projectorDrawingOptions) {
		this.projectorDrawingOptions = projectorDrawingOptions;
	}

	/**
	 * @return the pamSymbolChooser
	 */
	public PamSymbolChooser getPamSymbolChooser() {
		return pamSymbolChooser;
	}

	/**
	 * @param pamSymbolChooser the pamSymbolChooser to set
	 */
	public void setPamSymbolChooser(PamSymbolChooser pamSymbolChooser) {
		this.pamSymbolChooser = pamSymbolChooser;
	}

	/**
	 * @return the hoverDataSynchroniser
	 */
	public Object getHoverDataSynchroniser() {
		return hoverDataSynchroniser;
	}

}
