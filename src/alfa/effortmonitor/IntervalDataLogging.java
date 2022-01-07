package alfa.effortmonitor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;

import javax.swing.JTree;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParser;
//import org.codehaus.jackson.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import GPS.GpsData;
import PamUtils.LatLong;
import PamUtils.LatLongDatabaseSet;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import generalDatabase.DBControlUnit;
import generalDatabase.DBProcess;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class IntervalDataLogging extends SQLLogging {
	
	private PamTableItem secondsMonitored, endTime;
	private LatLongDatabaseSet startGPS, endGPS;
	private PamTableItem clickTrains, clicks;
	private PamTableItem[] histogramColumns;
	private PamTableDefinition tableDefinition;
	
	private static final int HISTO_TEXT_LENGTH = 30;

	public IntervalDataLogging(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		tableDefinition = new PamTableDefinition("Sperm Whale Intervals");
		tableDefinition.addTableItem(endTime = new PamTableItem("End Time", Types.TIMESTAMP));
		tableDefinition.addTableItem(secondsMonitored = new PamTableItem("Monitored Seconds", Types.INTEGER));
		startGPS = new LatLongDatabaseSet("Start", LatLongDatabaseSet.VERTICAL_NONE, true, true);
		endGPS = new LatLongDatabaseSet("End", LatLongDatabaseSet.VERTICAL_NONE, true, true);
		startGPS.addTableItems(tableDefinition);
		endGPS.addTableItems(tableDefinition);
		tableDefinition.addTableItem(clickTrains = new PamTableItem("Click Trains", Types.INTEGER));
		tableDefinition.addTableItem(clicks = new PamTableItem("Clicks", Types.INTEGER));
		tableDefinition.setUpdatePolicy(SQLLogging.UPDATE_POLICY_OVERWRITE);
		setTableDefinition(tableDefinition);
	}
	
	public boolean checkHistogramColumns(int nColumns) {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null || con.getConnection() == null) {
			return false;
		}
		if (histogramColumns == null) {
			histogramColumns = new PamTableItem[nColumns];
		}
		else if (histogramColumns.length < nColumns) {
			histogramColumns = Arrays.copyOf(histogramColumns, nColumns);
		}
		boolean ok = true;
		for (int i = 0; i < nColumns; i++) {
			ok |= checkHistogramColumn(con, i);
		}
		return ok;
	}

	private boolean checkHistogramColumn(PamConnection con, int iColumn) {
		DBProcess dbProc = DBControlUnit.findDatabaseControl().getDbProcess();
		String columnName = "AngleHistogram_"+iColumn;
		histogramColumns[iColumn] = tableDefinition.findTableItem(columnName);
		if (histogramColumns[iColumn] == null) {
			histogramColumns[iColumn] = new PamTableItem(columnName, Types.CHAR, HISTO_TEXT_LENGTH);
			tableDefinition.addTableItem(histogramColumns[iColumn]);
		}
		return dbProc.checkColumn(tableDefinition, histogramColumns[iColumn]);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		IntervalDataUnit intervalDataUnit = (IntervalDataUnit) pamDataUnit;
		endTime.setValue(sqlTypes.getTimeStamp(intervalDataUnit.getEndTimeInMilliseconds()));
		secondsMonitored.setValue((int) intervalDataUnit.getActualEffort()/1000); 
		startGPS.setLatLongData(intervalDataUnit.getFirstGPSData());
		endGPS.setLatLongData(intervalDataUnit.getLastGPSData());
		clickTrains.setValue(intervalDataUnit.getnClickTrains());
		clicks.setValue(intervalDataUnit.getnClicks());
		if (histogramColumns != null) for (int i = 0; i < histogramColumns.length; i++) {
			writeAngleHistogram(sqlTypes, intervalDataUnit, i);
		}
	}
	
	private void writeAngleHistogram(SQLTypes sqlTypes, IntervalDataUnit intervalDataUnit, int iAngleHist) {
		AngleHistogram angleHist = intervalDataUnit.getAngleHistogram(iAngleHist);
		if (angleHist == null) {
			histogramColumns[iAngleHist].setValue(null);
			return;
		}
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		String str = null;
		try {
			JsonGenerator jg = jf.createJsonGenerator(os, JsonEncoding.UTF8);
			jg.writeStartObject();
//			jg.writeNumberField("NCOL", angleHist.getNBins());
			jg.writeArrayFieldStart("NW");
			double[] data = angleHist.getData();
			for (int i = 0; i < data.length; i++) {
				jg.writeNumber((int) data[i]); 
			}
			jg.writeEndArray();
			jg.writeEndObject();
			jg.close();
			String jsonString = os.toString();
			histogramColumns[iAngleHist].setValue(jsonString);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		LatLong startLL = startGPS.getLatLongData(sqlTypes);
		LatLong endLL = endGPS.getLatLongData(sqlTypes);
		
		if (startLL==null || endLL==null) return null; //for corrupt entries. 
		
		long monTime = secondsMonitored.getIntegerValue()*1000;
		long endTimeMillis = SQLTypes.millisFromTimeStamp(endTime.getValue());
		int nClickTrains = clickTrains.getIntegerValue();
		int nClicks = clickTrains.getIntegerValue();
		GpsData sGPS = new GpsData(timeMilliseconds, startLL);
		GpsData eGPS = new GpsData(endTimeMillis, endLL);
		IntervalDataUnit du = new IntervalDataUnit(timeMilliseconds, sGPS);
		du.setDurationInMilliseconds(endTimeMillis-timeMilliseconds);
		du.setActualEffort(monTime);
		du.setLastGPSData(eGPS);
		du.setnClickTrains(nClickTrains);
		du.setnClicks(nClicks);
		if (histogramColumns != null) {
			long histInterval = (endTimeMillis-timeMilliseconds) / histogramColumns.length;
			for (int i = 0; i < histogramColumns.length; i++) {
				AngleHistogram angleHist = readAngleHistogram(i);
				if (angleHist != null) {
					long histTime = timeMilliseconds + histInterval*i;
					angleHist.setStartTime(histTime);
					du.addAngleHistogram(angleHist);
				}
			}
		}
		return du;
	}

	private AngleHistogram readAngleHistogram(int iHist) {
		String jString = histogramColumns[iHist].getDeblankedStringValue();
		if (jString == null || jString.length() == 0) {
			return null;
		}
		JsonFactory jf = new JsonFactory();
		int[] data = null;
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jString.getBytes()));
			JsonNode jNode = jTree.findValue("NW");
			if (jNode.isArray()) {
				ArrayNode an = (ArrayNode) jNode;
				int n = an.size();
				data = new int[n];
				int i = 0;
				for (JsonNode jn : an) {			
					data[i++] = jn.intValue();
				}
			}
		} catch (IOException e) {
			Debug.out.printf("Interval data logging unable to interpret histogram string: %s", jString);
			return null;
		}
		if (data == null) {
			return null;
		}
		AngleHistogram angleHist = new AngleHistogram(0, 0, Math.PI, data.length);
		angleHist.setData(data);
		return angleHist;

	}

}
