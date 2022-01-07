package PamDetection;

import java.util.ArrayList;
import PamguardMVC.PamDataUnit;

public interface DetectionMatchModel {

	/**
	 * Gets a list of all detections to be used in the localisation. Usually this
	 * will be a list of detections which fall within the time windows of each
	 * hydrophone element, with respect to a primary detection. .
	 * 
	 * @return a list of detections to be used in localisation in no particular
	 *         order.
	 */
	public ArrayList<PamDataUnit> findDetectionGroup();

	/**
	 * To group detections run through every detection on the primary hydrophone.
	 * Use the absolute distance of each hydrophone element from the primary
	 * hydrophone to create a time window before and after the primary detection.
	 * All detections located within the time window of each hydrophone possibly
	 * correspond to the primary detection. Work out all the possible combination of
	 * detections and the corresponding time delays.
	 * 
	 * @param channels
	 *            includes detections on these channels only for the calculation.
	 * @return a set of time delays. Each ArrayList<Double> represents a set of time
	 *         delays between ALL hydrophones using the indexM1 and indexM2 rules.
	 *         Multiple combinations are then added to ArrayList<ArrayList<Double>>
	 *         and returned by this function;
	 */
	public ArrayList<TDArrayList<Double>> getGroupTimeDelays(int[] channels);

	/**
	 * Gets the channel bitmap of hydrophones which contain at least one detection
	 * to be used for localisation . Note: this is not the same number of possible
	 * time delay possibilities. For any detection, if there is more than one
	 * detection that might correpsond to the primary detection on any channel, then
	 * there is more than one combination of time delays. However, although there
	 * might be more than one detection on a channel, there also may be a channel
	 * with no possible corresponding detections at all. In this case, depending on
	 * the number of channels without a detection, there will be set number of null
	 * time delays. This equivalent to simply removing a hydrophone from the array.
	 * 
	 * @param channels:
	 *            includes detections on these channels only for the calculation.
	 * @return bitmap of channels which have a corresponding detection.
	 */
	public int getNMatchDetections(int[] channels);

	/**
	 * Quickly calculate the number of possible time delay possibilities there will
	 * be for this localisation. Remember to include flags,e.g. the number of
	 * possibilities for only event clicks
	 * 
	 * @param channels
	 *            includes detections on these channels only for the calculation.
	 * @return the number of time delay possibilities.
	 */
	public int getNPossibilities(int[] channels);

	/**
	 * Check to see if a detection can be localised as part of this detection match
	 */
	public boolean canLocalise(PamDataUnit pamDetection);

}
