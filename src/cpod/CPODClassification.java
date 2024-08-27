package cpod;

/**
 * Holds CPOD click train classification. 
 * 
 * @author Jamie Macaulay
 *
 */
public class CPODClassification {
	
		/**
		 * Enum for species classiifcation from CPOD/FPOD click train detector. 
		 * @author Jamie Macaulay
		 *
		 */
		public enum CPODSpeciesType{NBHF, DOLPHIN, SONAR, UNKNOWN}
		
		/**
		 * A unique ID for the click trian within the file
		 */
		public int clicktrainID = 0;
		
		/**
		 * The quality level of the click train
		 */
		public short qualitylevel = 0;
		
		/**
		 * True fo the clcik train is an echo. 
		 */
		public boolean isEcho = false;


		/**
		 * The species type. 
		 */
		public CPODSpeciesType species = CPODSpeciesType.UNKNOWN;
		
}