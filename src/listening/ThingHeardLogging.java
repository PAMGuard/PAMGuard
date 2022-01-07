package listening;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class ThingHeardLogging extends SQLLogging {

	PamTableDefinition tableDef;
	ListeningControl listeningControl;
	PamTableItem species, volume, channels, comment;
	PamDataBlock<ThingHeard> thingHeardDataBlock;
	
	public ThingHeardLogging(ListeningControl listeningControl, PamDataBlock<ThingHeard> pamDataBlock) {
		super(pamDataBlock);
		this.listeningControl = listeningControl;
		this.thingHeardDataBlock = pamDataBlock;
		setCanView(true);
		tableDef = new PamTableDefinition(pamDataBlock.getDataName(), SQLLogging.UPDATE_POLICY_WRITENEW);
		tableDef.addTableItem(species = new PamTableItem("Species",Types.CHAR,ListeningControl.SPECIES_LENGTH));
		tableDef.addTableItem(volume = new PamTableItem("Volume",Types.INTEGER));
		tableDef.addTableItem(channels = new PamTableItem("Channels",Types.INTEGER));
		tableDef.addTableItem(comment = new PamTableItem("Comment",Types.CHAR,ListeningControl.COMMENT_LENGTH));
		tableDef.setUseCheatIndexing(true);

		setTableDefinition(tableDef);
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {	
//		return tableDef;
//	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		ThingHeard thingHeard = (ThingHeard) pamDataUnit;
		if (thingHeard.getSpeciesItem() != null) {
			species.setValue(thingHeard.getSpeciesItem().getName());
		}
		else {
			species.setValue(null);
		}
		volume.setValue(thingHeard.getVolume());
		channels.setValue(thingHeard.getChannelBitmap());
		comment.setValue(thingHeard.getComment());
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {

//		Timestamp ts = (Timestamp) getTableDefinition().getTimeStampItem().getValue();
//		long t = PamCalendar.millisFromTimeStamp(ts);
		String strSpecies = species.getDeblankedStringValue();
		int vol = (Integer) volume.getValue();
		int chan = (Integer) channels.getValue();
		String strComment = comment.getDeblankedStringValue();
		SpeciesItem speciesItem;
		
		ThingHeard thingHeard = new ThingHeard(timeMilliseconds, 0, speciesItem = new SpeciesItem(strSpecies), 
				vol, strComment);
		thingHeard.setDatabaseIndex(databaseIndex);
		// now need to work out what symbol it should be using !
		speciesItem.setSymbol(listeningControl.getSpeciesSymbol(strSpecies));
		
		getPamDataBlock().addPamData(thingHeard);
		
		return thingHeard;
	}

}
