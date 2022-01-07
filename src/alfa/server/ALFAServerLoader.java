package alfa.server;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.Timer;

import org.postgresql.jdbc.PgConnection;

import GPS.GpsData;
import PamController.OfflineDataStore;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.debug.Debug;
import alfa.effortmonitor.AngleHistogram;
import alfa.effortmonitor.IntervalDataBlock;
import alfa.effortmonitor.IntervalDataUnit;
import dataGram.DatagramManager;
import dataMap.OfflineDataMapPoint;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.PamScroller;
import pamScrollSystem.ViewLoadObserver;

/**
 * Connect to the  web server hosting satellite messages
 * @author Jamie Macaulay
 *
 */

public class ALFAServerLoader extends SQLLogging implements OfflineDataStore{

	private IntervalDataBlock intervalDataBlock;

	private static String password = "notneeded";

	private ArrayList<Long> imeiNumbers;

	private int errorCount;

	private int highestId;

	private ALFAOfflineDataMap dataMap; 

	private static final String dataTable = "data_manager_rawposteddata";
	
	private javax.swing.Timer autoUpdateTimer;
	
	private PgConnection currentConnection;

	public ALFAServerLoader(IntervalDataBlock intervalDataBlock) {
		super(intervalDataBlock);
		this.intervalDataBlock = intervalDataBlock;
		autoUpdateTimer = new Timer(15*60*1000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				autoUpdateAction();
			}
		});
	}

	private PgConnection makeConnection() {
		if (currentConnection != null) {
			return currentConnection;
		}
		if (checkPassword() == false) {
			return null;
		}
		String db_user = "alfa_db_user";
		String db_pw = "spermwhale";
		String dbName =  "alfa_sw_db";
		//		String conStr = "http://157.230.166.95:8000/api/raw_data_string/";

		String driverClass = "org.postgresql.Driver";
		try {
			Class.forName(driverClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		//		try {
		String url = "jdbc:postgresql://157.230.166.95:5432/"+dbName;
		//			System.out.println("Open connection " + url);
		PgConnection conn = null;
		try {
			conn = (PgConnection) DriverManager.getConnection(url, db_user, db_pw);
		} catch (SQLException e) {
			WarnOnce.showWarning(PamController.getMainFrame(), "Server Error", "Unable to Connect to ALFA sperm whale database", WarnOnce.WARNING_MESSAGE);
			currentConnection = null;
			return null;
		}
		//		Debug.out.println("server connection open");
		//			DatabaseMetaData dbm = conn.getMetaData();
		//			ResultSet tables = dbm.getTables(null, null, null, null);
		return currentConnection = conn;
	}
	
	private void closeConnection( ) {
		if (currentConnection != null) {
			try {
				currentConnection.close();
			} catch (SQLException e) {
//				e.printStackTrace();
			}
			currentConnection = null;
		}
	}
	
	/**
	 * Called from controller to set up or disable auto updates. 
	 * @param autoUpdates
	 */
	public void runAutoUpdates(boolean autoUpdates) {
		autoUpdateTimer.stop();
		if (autoUpdates) {
			autoUpdateTimer.start();
			checkForUpdates(true); // check immediately. 
		}
	}

	private synchronized boolean checkForUpdates(boolean scrollAnyway) {
		int newHighestId = getHighestId();
		boolean needUpdate = (newHighestId > highestId);
		Debug.out.printf("Current ALFA server map highest id = %d, new highest id = %d\n", highestId, newHighestId);
		if (needUpdate) {
			if (dataMap != null) {
				// clear existing data map
				dataMap.clear();
			}
			/*
			 *  make new data map - just do the entire thing from scratch 
			 *  since there is so little data. 
			 */		
			createOfflineDataMap(PamController.getMainFrame());
		}
		if (needUpdate || scrollAnyway) {
			AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
			long lastTime = dataMap.getLastDataTime();
			if (lastTime > 0) {
				Debug.out.printf("Moving ALFA display to center at %s\n", PamCalendar.formatDateTime(lastTime, true));
				// just call the center function - it will auto adjust to this becomes the last time. 
				scrollManager.centreDataAt(intervalDataBlock, lastTime);
			}
		}
		
		return needUpdate;
	}
	protected void autoUpdateAction() {
		checkForUpdates(false);
	}

	/**
	 * Check the highest id in the database
	 * @return
	 */
	public int getHighestId() {
		PgConnection conn = makeConnection();
		if (conn == null) {
			return 0;
		}
		int id = 0;
		String qStr = "SELECT id FROM data_manager_rawposteddata WHERE decoded_message LIKE '%%$PGSTA%%' ORDER By Id DESC";
		try {
			ResultSet dResult = conn.execSQLQuery(qStr);
			if (dResult.next()) {
				id = dResult.getInt(1);
			} 
			dResult.close();
		}	
		
		catch (SQLException e) {
			e.printStackTrace();
			closeConnection();
		}
		return id;
	}

	public boolean loadALFAServerData(long startMillis, long endMillis) {


		String clause = String.format("WHERE decoded_message LIKE '%%$PGSTA%%' AND datetime_sent BETWEEN '%s' AND '%s'", 
				PamCalendar.formatDBDateTime(startMillis, false), PamCalendar.formatDBDateTime(endMillis+5000*3600L, false));

		String qStr = String.format("SELECT id, imei, date_received, decoded_message, raw_posted_data FROM data_manager_rawposteddata %s ORDER BY datetime_sent", clause);

		Debug.out.println(qStr);
		PgConnection conn = makeConnection();
		if (conn == null) {
			return false;
		}
		try {
			ResultSet dResult = conn.execSQLQuery(qStr);
			while (dResult.next()) {
				int id = dResult.getInt(1);
				long imei = dResult.getLong(2);
				String date = dResult.getString(3);
				Object dateObject = dResult.getObject(3);
				String strData = dResult.getString(4);
				String rawData = dResult.getString(5);
				//				Debug.out.printf("%d %d %s \"%s\" %s\n", id, imei, date, dateObject.toString(), strData);
				IntervalDataUnit intervalDataUnit = parseDataString(strData);
				if (intervalDataUnit != null) {
					intervalDataUnit.setImeiNumber(imei);
					intervalDataUnit.setUID(id);

				}
				/*
				 * Try to unpack the rawPostedData to get additional information such as the 
				 * message number. 
				 */
				ServerRawData serverRaw = ServerRawData.unpackRawJson(rawData);
				if (serverRaw != null) {
					intervalDataUnit.setServerRawData(serverRaw);
				}
				intervalDataBlock.addPamData(intervalDataUnit);
			}
			dResult.close();
			//			System.out.println(conn);
//			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			closeConnection();
			return false;
		}

		intervalDataBlock.sortData();
		//		Debug.out.printf("%d data units loaded for ALFA intervals", intervalDataBlock.getUnitsCount());
		IntervalDataUnit idu = intervalDataBlock.getFirstUnit();
		if (idu != null) {
			//			MasterReferencePoint.
		}
		return true;
	}


	private IntervalDataUnit parseDataString(String strData) {
		String[] parts = strData.split(",");
		String stringId = parts[0];
		int version = Integer.valueOf(parts[1]);
		String date = parts[2];
		String time = parts[3];
		long startMillis = parseDateTime(date, time);
		double lat1 = Double.valueOf(parts[4]);
		double lon1 = Double.valueOf(parts[5]);
		int minutes = Integer.valueOf(parts[6]);
		long endMillis = startMillis + minutes*60000;
		int percActive = Integer.valueOf(parts[7]);
		double lat2 = Double.valueOf(parts[8]);
		double lon2 = Double.valueOf(parts[9]);
		int nClickTrains = Integer.valueOf(parts[10]);
		int nClicks = Integer.valueOf(parts[11]);
		LatLong ll1 = new LatLong(lat1, lon1);
		LatLong ll2 = new LatLong(lat2, lon2);
		GpsData gps1 = new GpsData(startMillis, ll1);
		GpsData gps2 = new GpsData(endMillis, ll2);
		IntervalDataUnit idu = new IntervalDataUnit(startMillis, gps1);
		idu.setDurationInMilliseconds(endMillis-startMillis);
		idu.setActualEffort(minutes*60000);
		idu.setLastGPSData(gps2);
		idu.setnClickTrains(nClickTrains);
		idu.setnClicks(nClicks);
		int iCol = 12;
		while (iCol < parts.length) {
			String h = parts[iCol++];
			if (h.equals("H") == false) {
				break;
			}
			int nH = Integer.valueOf(parts[iCol++]);
			int[] hDat = new int[nH];
			for (int i = 0; i < nH; i++) {
				hDat[i] = Integer.valueOf(parts[iCol++]);
			}
			AngleHistogram angleHist = new AngleHistogram(0, 0, Math.PI, nH);
			angleHist.setData(hDat);
			idu.addAngleHistogram(angleHist);
		}
		int nhists = idu.getAngleHistograms().size();
		if (nhists > 0) {
			long histInterval = minutes / nhists * 60*1000;
			for (int i = 0; i < nhists; i++) {
				AngleHistogram angleHist = idu.getAngleHistogram(i);
				long histStart = startMillis + i*histInterval;
				angleHist.setStartTime(histStart);
			}
		}
		return idu;
	}

	private long parseDateTime(String date, String time) {try {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date d = df.parse("20"+date+"_"+time);  //throws ParseException if no match
		long millis = d.getTime();
		//		Debug.out.printf("%s %s parses as %s\n", date, time, PamCalendar.formatDateTime(millis));
		return millis;
	}
	catch (java.text.ParseException ex) {
		//No problem, just go on to next format to try.
	}
	return 0;
	}

	private boolean checkPassword() {
		if (password != null) {
			return true;
		}
		JPasswordField pwd = new JPasswordField(10);
		int action = JOptionPane.showConfirmDialog(null, pwd,"Enter server Password",JOptionPane.OK_CANCEL_OPTION);
		if(action < 0) {
			return false;
		}
		char[] pw = pwd.getPassword();
		if (pw == null || pw.length == 0) {
			return false;
		}
		password = new String(pw);
		//	    System.out.println(password);
		return true;
	}

	public List<Long> getImeiList() {
		if (imeiNumbers == null) {
			imeiNumbers = makeImeiList();
		}
		return imeiNumbers;
	}

	private ArrayList<Long> makeImeiList() {
		PgConnection conn = makeConnection();
		if (conn == null) {
			return null;
		}
		imeiNumbers = new ArrayList<>();
		String qStr = "SELECT DISTINCT imei FROM data_manager_rawposteddata";
		try {
			ResultSet dResult = conn.execSQLQuery(qStr);
			while (dResult.next()) {
				Long id = dResult.getLong(1);
				imeiNumbers.add(id);
			}
//			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return imeiNumbers;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}
	@Override
	public synchronized void createOfflineDataMap(Window parentFrame) {
		PgConnection conn = makeConnection();
		if (dataMap == null) {
			dataMap = new ALFAOfflineDataMap(this, intervalDataBlock);
			intervalDataBlock.addOfflineDataMap(dataMap);
		}
		ALFADataMapPoint admp = null;
		long mapInterval = 3600L*1L*1000L;
		if (conn == null) {
			return;
		}
		long wantedIMEI = 300234068339920L;
		String selStr = String.format("Select id, datetime_sent, imei FROM %s WHERE  decoded_message LIKE '%%$PGSTA%%' ORDER BY datetime_sent", dataTable);
//		String selStr = String.format("Select id, utc, imei FROM %s WHERE  decoded_message LIKE '%%$PGSTA%%' ORDER BY datetime_sent", dataTable);
		//		String selStr = String.format("Select id, datetime_sent FROM %s ORDER BY datetime_sent", dataTable);
		//		Debug.out.println(selStr);
		highestId = 0;
		try {
			ResultSet dResult = conn.execSQLQuery(selStr);
			while (dResult.next()) {
				int id = dResult.getInt(1);
				highestId = Math.max(id, highestId);
				Timestamp dateObject = (Timestamp) dResult.getObject(2);
				//				long millis = SQLTypes.millisFromTimeStamp(dateObject);
				long millis = dateObject.getTime()+3600000L;
				long imei = dResult.getLong(3);
				if (imei != wantedIMEI) continue;
				//				PamCalendar.
				//				System.out.println(dateObject + " " + PamCalendar.formatDateTime(millis));
				if (admp == null || millis-admp.getStartTime() > mapInterval) {
					admp = new ALFADataMapPoint(millis, millis, 1, 0);
					admp.setLowestUID((long) id);
					dataMap.addDataPoint(admp);
				}
				else {
					admp.setNDatas(admp.getNDatas()+1);
					admp.setHighestUID((long) id); 
					admp.setEndTime(millis);
				}
			}
			dResult.close();
//			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return ;
		}
		dataMap.sortRanges();
	}

	@Override
	public String getDataSourceName() {
		return "ALFA Server";
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {
		return loadALFAServerData(offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());
	}

	@Override
	public boolean saveData(PamDataBlock dataBlock) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rewriteIndexFile(PamDataBlock dataBlock, OfflineDataMapPoint dmp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DatagramManager getDatagramManager() {
		// TODO Auto-generated method stub
		return null;
	}
}
