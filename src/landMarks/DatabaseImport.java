package landMarks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.lang.reflect.Field;

import PamUtils.LatLong;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.pamCursor.PamCursor;
import java.awt.Color;


public class DatabaseImport {

	private LandmarkControl landmarkControl;

	private EmptyTableDefinition tableDef;

	private PamTableItem latitude, longitude, name, height, symType, symWidth, symHeight, symFill, lineColour, fillColour;

	public DatabaseImport(LandmarkControl landmarkControl) {
		this.landmarkControl = landmarkControl;
		tableDef = new EmptyTableDefinition("");
		tableDef.addTableItem(latitude = new PamTableItem("Latitude", Types.REAL));
		tableDef.addTableItem(longitude = new PamTableItem("Longitude", Types.REAL));
		tableDef.addTableItem(height = new PamTableItem("Height", Types.REAL));
		tableDef.addTableItem(name = new PamTableItem("Name", Types.CHAR, 255));
		tableDef.addTableItem(symType = new PamTableItem("SymbolType", Types.CHAR, 255));
		tableDef.addTableItem(symWidth = new PamTableItem("SymbolWidth", Types.INTEGER));
		tableDef.addTableItem(symHeight = new PamTableItem("SymbolHeight", Types.INTEGER));
		tableDef.addTableItem(symFill = new PamTableItem("SymbolFill", Types.BOOLEAN));
		tableDef.addTableItem(lineColour = new PamTableItem("SymbolLineColour", Types.CHAR, 255));
		tableDef.addTableItem(fillColour = new PamTableItem("SymbolFillColour", Types.CHAR, 255));

	}

	public boolean checkTable(String tableName) {
		tableDef.setTableName(tableName);
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return false;
		}
		return dbControl.getDbProcess().checkTable(tableDef);
	}

	public ArrayList<LandmarkData> readDatas(String tableName) {
		if (checkTable(tableName) == false) {
			return null;
		}
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
		ArrayList<LandmarkData> newData = new ArrayList<>();
		PamCursor cursor = dbControl.getDatabaseSystem().createPamCursor(tableDef);
		ResultSet resultSet = cursor.openReadOnlyCursor(dbControl.getConnection(), "");
		try {
			while (resultSet.next()) {

				for (int i = 0; i < tableDef.getTableItemCount(); i++) {
					PamTableItem tableItem = tableDef.getTableItem(i);
					tableItem.setValue(resultSet.getObject(i + 1));
				}
				LandmarkData ld = new LandmarkData();
				ld.latLong = new LatLong(latitude.getFloatValue(), longitude.getFloatValue());
				ld.height = height.getFloatValue();
				ld.name = name.getDeblankedStringValue();
				
				// add the symbol information, if it exists in the database table
				try {
					PamSymbolType theSymbolType = PamSymbolType.valueOf(symType.getDeblankedStringValue());
					Field field = Class.forName("java.awt.Color").getField(fillColour.getDeblankedStringValue().toLowerCase());
				    Color fillCol = (Color) field.get(null);
//					if (fillCol == null) fillCol = landmarkControl.getDefaultSymFillColor();
					field = Class.forName("java.awt.Color").getField(lineColour.getDeblankedStringValue().toLowerCase());
				    Color lineCol = (Color) field.get(null);
//					if (lineCol == null) lineCol = landmarkControl.getDefaultSymLineColor();
					int width = symWidth.getIntegerValue();
					if (width <= 0 ) width = landmarkControl.getDefaultSymSizeWidth();
					int height = symHeight.getIntegerValue();
					if (height <= 0 ) height = landmarkControl.getDefaultSymSizeHeight();
					ld.symbol = new PamSymbol(theSymbolType,
							width,
							height,
							symFill.getBooleanValue(),
							fillCol,
							lineCol);
				}
				// if the SymbolType column isn't in the table (NullPointerException) OR
				// the SymbolType value wasn't recognized (IllegalArgumentException) OR
				// the color wasn't recognized (NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalAccessException), 
				// just go to the default
				catch (NullPointerException | IllegalArgumentException | NoSuchFieldException | SecurityException | ClassNotFoundException | IllegalAccessException ex) {
					ld.symbol = landmarkControl.getDefaultSymbol();
				}
				newData.add(ld);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return newData;
	}

}
