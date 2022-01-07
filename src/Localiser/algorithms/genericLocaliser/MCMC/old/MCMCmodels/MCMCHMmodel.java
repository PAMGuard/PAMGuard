package Localiser.algorithms.genericLocaliser.MCMC.old.MCMCmodels;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import PamView.panel.PamPanel;

/**
 * An MCMCHM (Markov Chain Monte Carlo Hydrophoe Movement) algorithm adds hydrophone positions as a parameter in the MCMC localiser. Each hydrophone has three dimensions which can be changed, meaning three paramaters per hydrophone. Therefore very quickly the number
 * of extra jump parameters in the MCMC algorithm becomes so large the chain will not converge, e.g. just moving 2 hydrophones introduces six new independent paramaters.A four hydrophone array has only six time delays, therefore six hydrophone movement paramters plus three source position paramters exceeds the number of observables (six time delays). 
 * Hence it is not always ideal to move individual hydrophones, instead it is better to have a few paramaters which moves the array or parts of the hydrophone array as a whole. An example of this is a 3D array which contains a vertical array component. The vertical array is held underwater by a large weight which makes the whole thing act like a pendulum. Hence that part of the array can be described by a single parameter, an angle. This angle can be used to directly calculate the position 
 * of all hydrophones in the vertical array. This is only one example of a possible model, others can be added.
 * <p>
 * MCMCHMmodel allows the  creation of hydrophone movement models for the MCMChm localiser. All models are held within an ArrayList in the MCMCHMParams class. Users can select which model to use from a combo box in the MCMCHM dialog. A model can have a gui interface allowing the user to change paramaters if necessary. This appears under the combo box when a model is selected. 
 * <p>
 * @author Jamie Macaulay
 *
 */
public interface MCMCHMmodel {
	
	/**
	 * Get new jump point. 
	 * @param hydrophoneArray
	 * @return
	 */
	public ArrayList<ArrayList<Point3f>> getNewHydrophonePoints(ArrayList<ArrayList<Point3f>> hydrophoneArray);
		
	public PamPanel getModelGui();
	
	public String modelName();
	

}
