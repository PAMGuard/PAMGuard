package clickDetector;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public class NoiseDataBlock extends PamDataBlock<ClickDetection> {

		ClickControl clickControl;
		
		public NoiseDataBlock(ClickControl clickControl, PamProcess parentProcess, int channelMap) {
			super(ClickDetection.class, clickControl.getDataBlockPrefix() + "Noise Data Samples", parentProcess, channelMap);			
			this.clickControl = clickControl;
			addLocalisationContents(LocContents.HAS_BEARING);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean getShouldLog(PamDataUnit pamDataUnit) {
			return false;
		}

}
