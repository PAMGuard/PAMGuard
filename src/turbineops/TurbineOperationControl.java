package turbineops;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import dataPlotsFX.data.TDScaleInfo;
import generalDatabase.lineplots.EnhancedTableItem;
import generalDatabase.lineplots.LinePlotControl;
import generalDatabase.lineplots.LinePlotScaleInfo;

public class TurbineOperationControl extends LinePlotControl {

	public static final String unitType = "Turbine Operation";
	
	private ArrayList<EnhancedTableItem> eTableItems;
	
	public TurbineOperationControl(String unitName) {
		super(unitType, unitName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<EnhancedTableItem> getColumnItems() {
		if (eTableItems == null) {
			makeTableItems();
		}
		return eTableItems;
	}

	private void makeTableItems() {
		eTableItems = new ArrayList<>();
		EnhancedTableItem item;
		item = new EnhancedTableItem("Rotation", Types.REAL);
		item.setDisplayName("Rotation RPM");
		item.setTdScaleInfo(new LinePlotScaleInfo(0, 15, ParameterType.SPEED, ParameterUnits.METRESPERSECOND, 0));
		eTableItems.add(item);
		
		item = new EnhancedTableItem("Flow", Types.REAL);
		item.setDisplayName("Tidal Flow");
		item.setTdScaleInfo(new LinePlotScaleInfo(-4, 4, ParameterType.SPEED, ParameterUnits.METRESPERSECOND, 1));
		eTableItems.add(item);
		
		item = new EnhancedTableItem("Raw Value", Types.CHAR, 50);
		item.setDisplayName("Notes");
		eTableItems.add(item);
		
		item = new EnhancedTableItem("PolprodFlow", Types.REAL);
		item.setDisplayName("Tidal Flow (from Polprod)");
		item.setTdScaleInfo(new LinePlotScaleInfo(-4, 4, ParameterType.SPEED, ParameterUnits.METRESPERSECOND, 3));
		eTableItems.add(item);
	}

}
