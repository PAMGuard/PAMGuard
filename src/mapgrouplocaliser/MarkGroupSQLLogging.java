package mapgrouplocaliser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import PamUtils.LatLong;
import PamUtils.PamCoordinate;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMark.OverlayMarkType;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import generalDatabase.JsonConverter;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
//import gov.nasa.worldwind.geom.LatLon;

public class MarkGroupSQLLogging extends SuperDetLogging {

	private MapGroupLocaliserControl mapGroupLocaliserControl;

	private JsonConverter jsonConverter = new JsonConverter();

	JsonFactory jasonFactory = new JsonFactory();

	public MarkGroupSQLLogging(MapGroupLocaliserControl mapGroupLocaliserControl, SuperDetDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.mapGroupLocaliserControl = mapGroupLocaliserControl;
		
		setTableDefinition(new MarkGroupTableDefinition(mapGroupLocaliserControl.getUnitName(), SQLLogging.UPDATE_POLICY_OVERWRITE));
		// set up a subform and make sure it gets created (doesn't happen automatically because it's
		// not linked to a PamDataBlock)
		MarkGroupSubSQLLogging subForm = new MarkGroupSubSQLLogging(null, this);
		this.setSubLogging(subForm);
//		DBControlUnit.findDatabaseControl().getDbProcess().checkTable(subForm.getTableDefinition());
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		MarkGroupDataUnit mgdu = (MarkGroupDataUnit) pamDataUnit;
		OverlayMark mark = mgdu.getOverlayMark();
		MarkGroupTableDefinition tableDef = (MarkGroupTableDefinition) getTableDefinition();
		/* 
		 * need to extract the XY path from the shape and write using json stype data 
		 * as was developed for writing out errors. 
		 * Trouble is number of decimal places - if dealing with latlong, we have a value of abou
		 * 57 (in Scotland) but need to be accurate to metres - about 10^-6 degrees. Will therefore 
		 * have to store a central value and then offsets from that me thinks in order to get a 
		 * broad enough scale. . 
		 */
		ArrayList<PamCoordinate> coords = mark.getCoordinates();
		double x[] = new double[coords.size()];
		double y[] = new double[coords.size()];
		PamCoordinate centre = mark.getCentre();
		for (int i = 0; i < coords.size(); i++) {
			PamCoordinate coord = coords.get(i);
			x[i] = coord.getCoordinate(0)-centre.getCoordinate(0);
			y[i] = coord.getCoordinate(1)-centre.getCoordinate(1);
		}
		/*
		 * Now write the error arrays out as json like sring. 
		 */
		String xString = jsonConverter.quickJsonString("X", x);
		String yString = jsonConverter.quickJsonString("Y", y);	

		tableDef.getMarkType().setValue(mark.getMarkType().toString());
		tableDef.getCentX().setValue((float) centre.getCoordinate(0));
		tableDef.getCentY().setValue((float) centre.getCoordinate(1));
		tableDef.getOutlineX().setValue(xString);
		tableDef.getOutlineY().setValue(yString);
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(generalDatabase.SQLTypes, long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		MarkGroupTableDefinition tableDef = (MarkGroupTableDefinition) getTableDefinition();
		String markType = tableDef.getMarkType().getDeblankedStringValue();
		if (markType == null) {
			return null;
		}
		OverlayMarkType type = OverlayMarkType.valueOf(markType);
		float centX = tableDef.getCentX().getFloatValue();
		float centY = tableDef.getCentY().getFloatValue();
		String outlineX = tableDef.getOutlineX().getDeblankedStringValue();
		String outlineY = tableDef.getOutlineY().getDeblankedStringValue();
		ObjectMapper om = new ObjectMapper();
		JsonNode jTree;
		double[] x = null, y = null;
		try {
			jTree = om.readTree(new ByteArrayInputStream(outlineX.getBytes()));
			//		JsonNode nv = jTree.findValue("NAME");
			JsonNode nx = jTree.findValue("X");
			jTree = om.readTree(new ByteArrayInputStream(outlineY.getBytes()));
			JsonNode ny = jTree.findValue("Y");
			if (nx != null && ArrayNode.class.isAssignableFrom(nx.getClass())) {
				x = jsonConverter.unpackJsonArray((ArrayNode) nx);
				y = jsonConverter.unpackJsonArray((ArrayNode) ny);
			}
		} catch (IOException e) {
			System.err.println("Ellipse Error interpreting " + outlineX + " or " + outlineY);
			return null;
		}
		OverlayMark overlayMark = new OverlayMark(null, null, null, 0, mapGroupLocaliserControl.getMapOverlayParameters(), 
				mapGroupLocaliserControl.getMapOverlayParameterUnits());
		overlayMark.setMarkType(type);
		if (x != null && y != null) {
			for (int i = 0; i < x.length; i++) {
				LatLong c3d = new LatLong(x[i]+centX, y[i]+centY);
				overlayMark.addCoordinate(c3d);
			}
		}

		MarkGroupDataUnit markDataUnit = new MarkGroupDataUnit(timeMilliseconds, overlayMark);
		return markDataUnit;
	}

	public MapGroupLocaliserControl getMapGroupLocaliserControl() {
		return mapGroupLocaliserControl;
	}

}
