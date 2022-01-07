package effortmonitor;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractScrollManager;

public class EffortLogging extends SQLLogging{

	private EffortControl effortControl;
	
	private PamTableItem scrollEndTime, runMode, sessionStartTime, sessionEndTime, observer, objective, display;
	
	public static final int MAX_OBSERVER_LENGTH = 30;
	public static final int MAX_DISPLAY_LENGTH = 80;
	public static final int MAX_OBJECTIVE_LENGTH = 256;

	protected EffortLogging(EffortControl effortControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.effortControl = effortControl;
		PamTableDefinition tableDef = new PamTableDefinition(effortControl.getUnitName());
		tableDef.addTableItem(scrollEndTime = new PamTableItem("End Time", Types.TIMESTAMP));
		tableDef.addTableItem(display = new PamTableItem("Display", Types.CHAR, MAX_DISPLAY_LENGTH));
		tableDef.addTableItem(observer = new PamTableItem("Observer", Types.CHAR, MAX_OBSERVER_LENGTH));
		tableDef.addTableItem(objective = new PamTableItem("Objective", Types.CHAR, MAX_OBJECTIVE_LENGTH));
		tableDef.addTableItem(sessionStartTime = new PamTableItem("Session Start", Types.TIMESTAMP));
		tableDef.addTableItem(sessionEndTime = new PamTableItem("Session End", Types.TIMESTAMP));
		tableDef.addTableItem(runMode = new PamTableItem("PamguardMode", Types.CHAR, 30));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		EffortDataUnit effort = (EffortDataUnit) pamDataUnit;
		scrollEndTime.setValue(sqlTypes.getTimeStamp(effort.getEndTimeInMilliseconds()));
		display.setValue(effort.getDisplayName());
		observer.setValue(effort.getObserver());
		objective.setValue(effort.getObjective());
		sessionStartTime.setValue(sqlTypes.getTimeStamp(effort.getSessionStartTime()));
		sessionEndTime.setValue(sqlTypes.getTimeStamp(effort.getSessionEndTime()));
		runMode.setValue(effort.getRunMode());
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		long endTime = SQLTypes.millisFromTimeStamp(scrollEndTime.getValue());
		String displ = display.getDeblankedStringValue();
		String objt = objective.getDeblankedStringValue();
		String obs = observer.getDeblankedStringValue();
		long sessStart = SQLTypes.millisFromTimeStamp(sessionStartTime.getValue());
		long sessEnd = SQLTypes.millisFromTimeStamp(sessionEndTime.getValue());
		AbstractScrollManager scrollManager = AbstractScrollManager.getScrollManager();
		AbstractPamScroller scroller = scrollManager.findScroller(displ);
		String mode = this.runMode.getDeblankedStringValue();
		EffortDataUnit effort = new EffortDataUnit(databaseIndex, scroller, displ, timeMilliseconds, 
				endTime, obs, objt, sessStart, sessEnd, mode);
		return effort;
	}


}
