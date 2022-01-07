package listening;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

public class ThingHeard extends PamDataUnit implements PamDetection {

	
	
	private int speciesIndex;
	private SpeciesItem speciesItem; 
	private int volume;
	private String comment;
	
	public ThingHeard(long timeMilliseconds, int speciesIndex, SpeciesItem speciesItem, 
			int volume, String comment) {
		super(timeMilliseconds);
		this.speciesItem = speciesItem;
		this.speciesIndex = speciesIndex;
		this.volume = volume;
		this.comment = comment;
	}

	public int getSpeciesIndex() {
		return speciesIndex;
	}

	public void setSpeciesIndex(int speciesIndex) {
		this.speciesIndex = speciesIndex;
	}

	public SpeciesItem getSpeciesItem() {
		return speciesItem;
	}

	public void setSpeciesItem(SpeciesItem speciesItem) {
		this.speciesItem = speciesItem;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


}
