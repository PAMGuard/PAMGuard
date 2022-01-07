package beamformer.annotation;

import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import beamformer.loc.BeamFormerLocalisation;

/**
 * Beam former annotation that can be added to any data unit as an annotation. 
 * It's a wrapper around a BeamFormerLoclaisation and the localisation
 * should also be set in the data unit where it can be used by displays, 
 * etc. in the usual way. Adding it as an annotation makes it easy to store the 
 * BF data in the binary store and the database. 
 * @author Doug Gillespie
 *
 */
public class BeamFormerAnnotation extends DataAnnotation<DataAnnotationType> {

	private BeamFormerLocalisation beamFormerLocalisation;

	public BeamFormerAnnotation(DataAnnotationType dataAnnotationType, BeamFormerLocalisation beamFormerLocalisation) {
		super(dataAnnotationType);
		this.beamFormerLocalisation = beamFormerLocalisation;
	}

	/**
	 * @return the beamFormerLocalisation
	 */
	public BeamFormerLocalisation getBeamFormerLocalisation() {
		return beamFormerLocalisation;
	}

	/**
	 * @param beamFormerLocalisation the beamFormerLocalisation to set
	 */
	public void setBeamFormerLocalisation(BeamFormerLocalisation beamFormerLocalisation) {
		this.beamFormerLocalisation = beamFormerLocalisation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (beamFormerLocalisation != null) {
			return beamFormerLocalisation.toString();
		}
		else {
			return "Null Beamformer data in " + super.toString();
		}
	}

}
