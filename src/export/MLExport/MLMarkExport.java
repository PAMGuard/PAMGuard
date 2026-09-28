package export.MLExport;

import PamguardMVC.PamDataBlock;
import annotationMark.MarkDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Struct;

/**
 * Export manual annotation marks (e.g. the boxes drawn on the spectrogram by the
 * Spectrogram Annotation module) to MATLAB.
 * <p>
 * A mark holds no acoustic data of its own - it is a time and frequency box the
 * user has drawn, plus whatever annotations (note, label, SPL, SNR, logger form)
 * were attached to it. All of that is in the generic part of the structure which
 * {@link MLDataUnitExport} writes, so all that is added here is the duration of
 * the mark, which is otherwise only available in samples and is the thing that
 * makes a mark a box rather than a point.
 *
 * @author Jamie Macaulay
 */
public class MLMarkExport extends MLDataUnitExport<MarkDataUnit> {

	@Override
	public Struct addDetectionSpecificFields(Struct mlStruct, int index, MarkDataUnit dataUnit) {

		double millisDuration = 0;
		if (dataUnit.getDurationInMilliseconds() != null) {
			millisDuration = dataUnit.getDurationInMilliseconds();
		}
		mlStruct.set("duration", index, Mat5.newScalar(millisDuration));

		return mlStruct;
	}

	@Override
	protected Struct detectionHeader(PamDataBlock pamDataBlock) {
		return null;
	}

	@Override
	public Class<?> getUnitClass() {
		return MarkDataUnit.class;
	}

	@Override
	public String getName() {
		return "annotations";
	}

}
