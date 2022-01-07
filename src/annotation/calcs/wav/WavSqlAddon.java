package annotation.calcs.wav;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class WavSqlAddon implements SQLLoggingAddon {

	private PamTableItem wavFileName;
	
	private WavAnnotationType wavAnnotationType;
	
	public WavSqlAddon(WavAnnotationType splAnnotationType) {
		super();
		this.wavAnnotationType = splAnnotationType;
		wavFileName  = new PamTableItem("Wav", Types.CHAR, 255);
	}

	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(wavFileName);
		
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		WavAnnotation annotation = (WavAnnotation) pamDataUnit.findDataAnnotation(WavAnnotation.class);
		if (annotation == null) {
			wavFileName.setValue(null);
		}
		else {
			wavFileName.setValue(annotation.getExportedWavFileName());
		}
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition,
			PamDataUnit pamDataUnit) {
		String fileName = (String) wavFileName.getValue();
		if (fileName != null) {
			WavAnnotation wavAnnotation = new WavAnnotation(wavAnnotationType);
			wavAnnotation.setExportedWavFileName(fileName);
			pamDataUnit.addDataAnnotation(wavAnnotation);
		}
		return true;
	}

	@Override
	public String getName() {
		return wavAnnotationType.getAnnotationName();
	}

}
