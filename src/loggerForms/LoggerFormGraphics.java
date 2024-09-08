package loggerForms;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;

import GPS.GpsData;
import Map.MapRectProjector;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import geoMag.MagneticVariation;
import loggerForms.controlDescriptions.CdLookup;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.controlDescriptions.InputControlDescription;
import loggerForms.formdesign.propertypanels.SymbolPanel;
import loggerForms.propertyInfos.BEARINGinfo;
import loggerForms.propertyInfos.HEADINGinfo;
import loggerForms.propertyInfos.RANGEinfo;

public class LoggerFormGraphics extends PanelOverlayDraw {

	private FormsControl formsControl;
	private FormDescription formDescription;
	private PamSymbol standardSymbol;
	
	public static final SymbolData defaultSymbol = new SymbolData();


	/**
	 * @param formsControl
	 * @param formDescription
	 */
	public LoggerFormGraphics(FormsControl formsControl,
			FormDescription formDescription) {
		super(new PamSymbol(defaultSymbol));
		this.formsControl = formsControl;
		this.formDescription = formDescription;
		/*
		 * this constructor gets called after all the controls for the form have been unpacked
		 * from the UDF table, so should be possible to initialise symbols, menus, etc. 
		 */
		createSymbols();
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		// can draw on the map 
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return formDescription.canDrawOnMap();
		}
		else {
			return false;
		}
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {

		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LONGITUDE) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}


		return null;
	}

	/**
	 * Draw a data unit on the map. 
	 * <p>This will only be called once the menu's controlling which units to plot have 
	 * already been examined (in shouldPlot). This function therefore just has to deal 
	 * with the plotting itself. There are a number of factors governing how a dataunit
	 * will be plotted:
	 * <p>Colour and symbol will be decided on whether a general colour/symbol has been selected for the 
	 * entire form, or for individual items in a Lookup table (lookup items should be able
	 * to give a coloured symbol based on information in the Lookup table). If it's a general 
	 * plotting of the entire form, then it's likely to be a single symbol and colour set for the 
	 * whole form.
	 * <p>Plot origin will generally be the GPS time of the vessel found on the map. This may also be
	 * referenced to one or more of the hydrophones. Eventually we may add the possibility to add 
	 * information on viewing platform location relative to the GPS receiver.
	 * <p>Points or lines. If range and bearing information are included in the form, then it's possible
	 * that bearing /  range lines will need to be drawn away from the plot origin. 
	 * @param g reference to graphics 
	 * @param pamDataUnit data unit to plot
	 * @param generalProjector projector to use (will be a map projector). 
	 * @return rectangle around plotted object.
	 */
	private Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		FormsDataUnit formDataUnit = (FormsDataUnit) pamDataUnit;
		if (!shouldPlotOnMap(formDataUnit, generalProjector)) {
			return null;
		}

		GpsData plotOrigin = formDataUnit.getOriginLatLong(false);
		if (plotOrigin == null) {
			return null;
		}
		
		Object[] formData = formDataUnit.getFormData();
		
		Double heading = null;
		HEADINGinfo headingInfo = formDescription.getHeadingInfo();
		if (headingInfo != null) {
			try {
				heading = Double.valueOf(formData[headingInfo.getControlIndex()].toString());
			}
			catch (NumberFormatException e) {	
			}
			catch (NullPointerException e2) {
			}
			if (heading != null) {
				// convert to a true heading now, whatever it is to start. 
				
				//TODO FIXME needs work relative doesn't work as the array average doesn't have direction
				// default true so normally ok, also goes weird when taking into account map rotation
				switch(headingInfo.getType()) {
				case MAGNETIC:
					heading += MagneticVariation.getInstance().getVariation(plotOrigin);
					break;
				case TRUE:
					break;
				case RELATIVE1:
				case RELATIVE2:
					heading += plotOrigin.getTrueHeading();
				}
				heading = 90-heading; // convert to an angle. 
			}
		}
		
		Double range = null;
		Double bearing = null;
		BEARINGinfo bearingInfo = formDescription.getBearingInfo();
		RANGEinfo rangeInfo = formDescription.getRangeInfo();
		
		if (bearingInfo != null) {
			try {
				bearing = Double.valueOf(formData[bearingInfo.getControlIndex()].toString());
			}
			catch (NumberFormatException e) {	
			}
			catch (NullPointerException e2) {
			}
		}
		if (rangeInfo != null) {
			try {
				range = Double.valueOf(formData[rangeInfo.getControlIndex()].toString());
			}
			catch (NumberFormatException e) {	
			}
			catch (NullPointerException e2) {
			}
			range = rangeInfo.getRangeMetres(range);
		}
		if (heading != null && range==null && bearing==null){
			Rectangle llRect = plotLatLongControls(g, formDataUnit, generalProjector, headingInfo, heading);
			if (llRect != null) {
				return llRect;
			}
		}

		double shipCourse = plotOrigin.getCourseOverGround();
		double shipHead = plotOrigin.getHeading();
		PamSymbol plotSymbol = getPlotSymbol(generalProjector, formDataUnit);

		Coordinate3d detOrigin = generalProjector.getCoord3d(plotOrigin.getLatitude(), plotOrigin.getLongitude(), plotOrigin.getHeight());
		// see if there is range heading and bearing data. 
		// need both of these in order to be able to do anything useful !
		
		LatLong endLatLong;
		Coordinate3d endPoint;
		Rectangle rect = null;
		if (bearing != null && range != null) {
			BearingTypes bearingType = bearingInfo.getType();
			if (bearingType != null) {
				switch (bearingType) {
				case MAGNETIC:
					if (plotOrigin.getMagneticVariation() != null) {
						bearing += plotOrigin.getMagneticVariation();
					}
					// then let this case run into the more general true case. 
					// i.e. don't put in a break statement
				case TRUE:
					endLatLong = plotOrigin.travelDistanceMeters(bearing, range);
					endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), endLatLong.getHeight());
					generalProjector.addHoverData(endPoint, pamDataUnit);
					return plotBearRangeHead(g, generalProjector, detOrigin, endPoint, plotSymbol, headingInfo, heading);
				case RELATIVE2:
					endLatLong = plotOrigin.travelDistanceMeters(shipHead-bearing, range);
					endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), endLatLong.getHeight());
					generalProjector.addHoverData(endPoint, pamDataUnit, 0);
					rect = plotBearRangeHead(g, generalProjector, detOrigin, endPoint, plotSymbol, headingInfo, heading);
					// don't put in a break point so that it also does the other side !!
				case RELATIVE1:
					endLatLong = plotOrigin.travelDistanceMeters(shipHead+bearing, range);
					endPoint = generalProjector.getCoord3d(endLatLong.getLatitude(), endLatLong.getLongitude(), endLatLong.getHeight());
					Rectangle rect2 = plotBearRangeHead(g, generalProjector, detOrigin, endPoint, plotSymbol, headingInfo, heading);
					generalProjector.addHoverData(endPoint, pamDataUnit, 0);
					if (rect != null) {
						return rect2.union(rect);
					}
					else {
						return rect2;
					}
				}
			}
		}

		generalProjector.addHoverData(detOrigin, pamDataUnit);

		return plotSymbol.draw(g, detOrigin.getXYPoint());
	}

	private Rectangle plotLatLongControls(Graphics g, FormsDataUnit dataUnit,
			GeneralProjector generalProjector, HEADINGinfo headingInfo, Double heading) {
		// go through the data and see if there are any latlong controls to plotObject[] data = dataUnit.getFormData();
		// should be 1:1 mapping between form data and form controls. 
		FormPlotOptions formPlotOptions = formDescription.getFormPlotOptions();
		Object[] data = dataUnit.getFormData();
		int nControls = data.length;
		ControlDescription controlDescription;
		ArrayList<InputControlDescription> controlDescriptions = formDescription.getInputControlDescriptions();
		PamSymbol plotSymbol = getPlotSymbol(generalProjector, dataUnit);
		if (plotSymbol == null) {
			return null;
		}
		Rectangle r = null;
		for (int i = 0; i < nControls; i++) {
			if (data[i] == null) {
				continue;
			}
			controlDescription = controlDescriptions.get(i);
			if (data[i].getClass() == LatLong.class) {// should see if is instance of rather than equal? could be GPSdata etc in future
				LatLong latLong = (LatLong) data[i];
				Coordinate3d endPoint = generalProjector.getCoord3d(latLong.getLatitude(), latLong.getLongitude(), latLong.getHeight());
				r = plotSymbol.draw(g, endPoint.getXYPoint());
				generalProjector.addHoverData(endPoint, dataUnit, 0);
				drawHeadingArrow(g, generalProjector, endPoint.getXYPoint(), headingInfo, heading);
			}
		}
		return r;
	}

	private Rectangle plotBearRangeHead(Graphics g,  GeneralProjector projector, Coordinate3d origin, Coordinate3d endPoint, PamSymbol symbol, HEADINGinfo headingInfo, Double heading) {
		Rectangle rect = symbol.draw(g, endPoint.getXYPoint());
		g.drawLine((int)origin.x, (int)origin.y, (int)endPoint.x, (int)endPoint.y);
		drawHeadingArrow(g, projector, endPoint.getXYPoint(), headingInfo, heading);
		return rect;
	}

	private void drawHeadingArrow(Graphics g, GeneralProjector projector, Point start, HEADINGinfo headingInfo, Double heading) {
		if (heading == null || start == null || headingInfo == null) {
			return;
		}
		// also draw a little arrow for the heading about 10 pixels long. 
		MapRectProjector mapProjector = (MapRectProjector) projector;
		double head = heading + mapProjector.getMapRotationDegrees();
		head = Math.toRadians(head);
		
		
		int endX = (int) (start.x + 20.*Math.cos(head));
		int endY = (int) (start.y - 20.*Math.sin(head));
		//			g.drawLine((int)endPoint.x, (int)endPoint.y, endX, endY);
		PamSymbol.drawArrow(g, start.x, start.y, endX, endY, 5);

	}



	/**
	 * Rules for drawing:<br> 
	 * There are two places where a form can be enabled to plot and as a consequence
	 * there are multiple ways of deciding whether or not a particular data unit
	 * should be plotted. 
	 * <p>
	 * Ways of deciding whether or not to plot:
	 * <p>1) Can insert the PLOT line into the control type field of a control
	 * <p>2) Can check the plot column of individual controls, which is mainly used 
	 * with lookup controls, so that a lit of, say species, to select can be generated.
	 * @param pamDataUnit data unit to plot
	 * @param generalProjector projector in use. 
	 * @return true if the unit should be plotted. 
	 */
	private boolean shouldPlotOnMap(FormsDataUnit dataUnit, GeneralProjector generalProjector) {
		Object[] data = dataUnit.getFormData();
		// should be 1:1 mapping between form data and form controls. 
		FormPlotOptions formPlotOptions = formDescription.getFormPlotOptions();
		if (formPlotOptions.isPlotControl(0,0)) {
			// plot everything option. 
			return true;
		}
		int nControls = data.length;
		InputControlDescription controlDescription;
		ArrayList<InputControlDescription> controlDescriptions = formDescription.getInputControlDescriptions();
		for (int i = 0; i < nControls; i++) {
			if (data[i] == null) {
				// control has null data, so won't decide based on that !
				continue;
			}
			controlDescription = controlDescriptions.get(i);
			if (!controlDescription.getPlot()) {
				// control description is not one to plot, so won't decide on that either !
				continue;
			}
			if (formPlotOptions.isPlotControl(i+1)) {
				/*
				 *  this one will work for non lookup controls and for lookup 
				 *  controls where the select all function has been selected.
				 */
				return true;
			}
			if (controlDescription.getEType() == ControlTypes.LOOKUP) {
				// need to find index of data and decide on that
				int lutIndex = ((CdLookup)controlDescription).getLookupList().indexOfCode(data[i].toString());
				if (formPlotOptions.isPlotControl(i+1, lutIndex+1) || formPlotOptions.isPlotControl(i+1, 0)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Work out what symbol to use, based on types of control selected, 
	 * etc. 
	 * @param pamDataUnit data unit to plot
	 * @return symbol. 
	 */
	private PamSymbol getPlotSymbol(GeneralProjector projector, FormsDataUnit dataUnit) {
		/**
		 * Try to use the new selector system. If it's not there, then revert to
		 * the older system. 
		 */
		PamSymbolChooser chooser = null;
		if (projector != null) {
			chooser = projector.getPamSymbolChooser();
		}
		if (chooser != null) {
			PamSymbol chosenSymbol = chooser.getPamSymbol(projector, dataUnit);
			if (chosenSymbol != null) {
				return chosenSymbol;
			}
		}
		
		/**
		 * first go through all the controls and see which is 
		 * the first one that's initiated plotting. If it's 
		 * a lookup, then return the appropriate symbol from 
		 * the lookup table. If not, then use the standard symbol. 
		 */
		FormPlotOptions formPlotOptions = formDescription.getFormPlotOptions();
		Object[] data = dataUnit.getFormData();
		int nControls = data.length;
		InputControlDescription controlDescription;
		ArrayList<InputControlDescription> controlDescriptions = formDescription.getInputControlDescriptions();

		PamSymbol lutSymbol = null;
		Rectangle r = null;
		for (int i = 0; i < nControls; i++) {
			if (data[i] == null) {
				continue;
			}
			controlDescription = controlDescriptions.get(i);
			if (!controlDescription.getPlot()) {
				// control description is not one to plot, so won't decide on that either !
				continue;
			}
			if (controlDescription.getEType() == ControlTypes.LOOKUP) {
				// need to find index of data and decide on that
				LookupList lutList = ((CdLookup)controlDescription).getLookupList();
				int lutIndex = lutList.indexOfCode(data[i].toString());
				// above line will return -1 if the lut item not found, but carry on since
				// the next line is ok - will plot if default ALL is selected. 
				if (formPlotOptions.isPlotControl(i+1, lutIndex+1) || formPlotOptions.isPlotControl(i+1, 0)) {
					LookupItem lookupItem = lutList.getLookupItem(lutIndex);
					if (lookupItem != null) {
						lutSymbol = lookupItem.getSymbol();
					}
					if (lutSymbol != null) {
						return lutSymbol;
					}
				}
			}
			if (!formPlotOptions.isPlotControl(i+1)) {
				/*
				 *  this one will work for non lookup controls and for lookup 
				 *  controls where the select all function has been selected.
				 */
				continue;
			}
		}

		return standardSymbol;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

	private void createSymbols() {
		/*
		 * Now see if the form description has got any more specific information
		 * about any of the above.
		 */
		PropertyDescription formProperty = formDescription.findProperty(PropertyTypes.SYMBOLTYPE);
		if (formProperty != null) {
			standardSymbol = SymbolPanel.createSymbol(formProperty.getItemInformation());
		}
		if (standardSymbol == null) {
			standardSymbol = new PamSymbol();
		}
	}

	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {		
		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LONGITUDE) {
			return formDescription.canDrawOnMap();
		}
		else {
			return false;
		}
	}

	@Override
	public boolean showOptions(Window parentWindow,
			GeneralProjector generalProjector) {
		return false;
	}

}
