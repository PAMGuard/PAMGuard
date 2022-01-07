package listening;

import PamUtils.PamCalendar;
import PamView.PamSymbol;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import autecPhones.AutecGraphics;

public class ListeningProcess extends PamProcess {

	private ListeningControl listeningControl;
	
	protected PamDataBlock<ListeningEffortData> effortDataBlock;
	
	protected PamDataBlock<ThingHeard> heardDataBlock;
	
	protected ListeningEffortData lastEffort;
	
	protected ThingHeardGraphics thingHeardGraphics;
	
	public ListeningProcess(ListeningControl listeningControl) {
		super(listeningControl, null);
		this.listeningControl = listeningControl;
		effortDataBlock = new PamDataBlock<ListeningEffortData>(ListeningEffortData.class, 
				"Listening Effort", this, 0);
		effortDataBlock.setOverlayDraw(new ListeningEffortGraphics(listeningControl));
		effortDataBlock.setPamSymbolManager(new StandardSymbolManager(effortDataBlock, ListeningEffortGraphics.defSymbol, true));
		effortDataBlock.setNaturalLifetime(3600 * 3);
		effortDataBlock.SetLogging(new ListeningEffortLogging(listeningControl, effortDataBlock));
		effortDataBlock.setMixedDirection(PamDataBlock.MIX_INTODATABASE);
		addOutputDataBlock(effortDataBlock);
		
		heardDataBlock = new PamDataBlock<ThingHeard>(ThingHeard.class, 
				"Things Heard", this, 0);
		heardDataBlock.setOverlayDraw(thingHeardGraphics = new ThingHeardGraphics(listeningControl));
		heardDataBlock.setNaturalLifetime(3600 * 3);
		heardDataBlock.SetLogging(new ThingHeardLogging(listeningControl, heardDataBlock));
		heardDataBlock.setPamSymbolManager(new StandardSymbolManager(heardDataBlock, ThingHeardGraphics.defaultSymbol, "Type"));
		addOutputDataBlock(heardDataBlock);
	}


	@Override
	public void pamStart() {

	}

	@Override
	public void pamStop() {

	}
	
	protected void effortButton(int index, int hydrophones) {
		String status = listeningControl.listeningParameters.effortStati.get(index);
		ListeningEffortData effortData = new ListeningEffortData(PamCalendar.getTimeInMillis(),
				status, hydrophones);
		effortDataBlock.addPamData(lastEffort = effortData);
	}
	
	protected void buttonPress(int speciesIndex, int volume, int hydrophones, String comment) {
		SpeciesItem speciesItem = null;
		if (speciesIndex >= 0) {
			speciesItem = listeningControl.listeningParameters.speciesList.get(speciesIndex);
		}
		ThingHeard newThing = new ThingHeard(PamCalendar.getTimeInMillis(), 
				speciesIndex, speciesItem, volume, comment);
		newThing.setChannelBitmap(hydrophones);
		heardDataBlock.addPamData(newThing);
	}

}
