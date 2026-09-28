package export.RExport;

import org.renjin.sexp.ListVector.NamedBuilder;

import annotationMark.MarkDataUnit;

/**
 * Export manual annotation marks (e.g. the boxes drawn on the spectrogram by the
 * Spectrogram Annotation module) to R.
 * <p>
 * See {@link export.MLExport.MLMarkExport} - a mark holds no acoustic data of its
 * own, so everything but the duration of the box comes from the generic part of
 * the data frame written by {@link RDataUnitExport}.
 *
 * @author Jamie Macaulay
 */
public class RMarkExport extends RDataUnitExport<MarkDataUnit> {

	@Override
	public NamedBuilder addDetectionSpecificFields(NamedBuilder rData, MarkDataUnit dataUnit, int index) {

		double millisDuration = 0;
		if (dataUnit.getDurationInMilliseconds() != null) {
			millisDuration = dataUnit.getDurationInMilliseconds();
		}
		rData.add("duration", millisDuration);

		return rData;
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
