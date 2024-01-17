package nidaqdev.networkdaq;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class NIDaqLogging implements SQLLoggingAddon {

	private NINetworkDaq niNetworkDaq;
	
	private PamTableItem crioTemperature;

	public NIDaqLogging(NINetworkDaq niNetworkDaq) {
		this.niNetworkDaq = niNetworkDaq;
		crioTemperature = new PamTableItem("Crio Temp", Types.REAL);
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(crioTemperature);
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		Double lastTemp = niNetworkDaq.getLastTemperature();
		if (lastTemp == null) {
			crioTemperature.setValue(null);
		}
		else {
			double t = lastTemp;
			crioTemperature.setValue((float) t); 
		}
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		return niNetworkDaq.getDeviceName();
	}

}
