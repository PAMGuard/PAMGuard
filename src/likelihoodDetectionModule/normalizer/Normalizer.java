package likelihoodDetectionModule.normalizer;

import likelihoodDetectionModule.spectralEti.SpectralEtiDataUnit;

/** Interface to abstract the various normalizers for the likelihood detector
 *  It is OK to return a null DataUnit if you cannot yet produce data based on
 *  the input given.
 * 
 * @author Dave Flogeras
 *
 */
public interface Normalizer {
	NormalizedDataUnit Process( SpectralEtiDataUnit a );
}