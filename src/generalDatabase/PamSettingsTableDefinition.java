package generalDatabase;

import java.sql.Types;

import PamguardMVC.PamConstants;

public class PamSettingsTableDefinition extends PamTableDefinition {
	
	private PamTableItem type, name, versionLo, versionHi, groupTotal, groupIndex, data, spareBits;

	public PamSettingsTableDefinition(String tableName, int updatePolicy, int dataLength){
		super(tableName, updatePolicy);
		addTableItem(type = new PamTableItem("unitType", Types.CHAR, PamConstants.MAX_ITEM_NAME_LENGTH));
		addTableItem(name = new PamTableItem("unitName", Types.CHAR, PamConstants.MAX_ITEM_NAME_LENGTH));
		addTableItem(versionLo = new PamTableItem("versionLo", Types.INTEGER));
		addTableItem(versionHi = new PamTableItem("versionHI", Types.INTEGER));
		addTableItem(groupTotal = new PamTableItem("groupTotal", Types.INTEGER));
		addTableItem(groupIndex = new PamTableItem("groupIndex", Types.INTEGER));
		addTableItem(data = new PamTableItem("data", Types.CHAR, dataLength));
		addTableItem(spareBits = new PamTableItem("spareBits", Types.INTEGER));
		setUseCheatIndexing(true);
	}
	public PamTableItem getType() {
		return type;
	}

	public PamTableItem getName() {
		return name;
	}

	public PamTableItem getVersionLo() {
		return versionLo;
	}

	public PamTableItem getVersionHi() {
		return versionHi;
	}

	public PamTableItem getGroupTotal() {
		return groupTotal;
	}

	public PamTableItem getGroupIndex() {
		return groupIndex;
	}

	public PamTableItem getData() {
		return data;
	}

	public PamTableItem getSpareBits() {
		return spareBits;
	}

	
}
