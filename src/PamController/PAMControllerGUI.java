package PamController;

import PamModel.PamModel;
import PamModel.PamModuleInfo;
import PamView.PamViewInterface;
import pamViewFX.pamTask.PamTaskUpdate;

/**
 * Basic interface for the primary UI and UI manager showing the PAMGuard GUI. 
 *  
 * @author Jamie Macaulay 
 *
 */
public interface PAMControllerGUI extends PamViewInterface {

	/**
	 * Add a PAMView Interface. This mirrors many of the functions in PAMControllerGUI
	 */
	public void addView(PamViewInterface newView);

	/**
	 * Called to destroy model
	 */
	public void destroyModel();

	/**
	 * Notification flag that the model has changed
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType);

	/**
	 * Notify the GUI that some loading is taking place
	 * @param progress - the progress. 
	 */
	public void notifyLoadProgress(PamTaskUpdate progress);
	
	/**
	 * Called on startup to grab pamsettings/database from somewhere. 
	 * @return  the pamsettings/database. 
	 */
	public PamSettings getInitialSettings();
	
	/**
	 * Called immediately after the creation of the GUI in the PAMController. 
	 */
	public void init();
	
	/**
	 * Called when the controller is ready for the fire view. 
	 * @param pamModelInterface 
	 * @param pamController 
	 * @return init and retuirn the first view
	 */
	public PamViewInterface initPrimaryView(PamController pamController, PamModel pamModelInterface);


	/**
	 * The GUI may have a callback into the PamController once it has all been set up. If this is the
	 * case then on init() PAMController stops and relies on the GUI code to call the relevant 
	 * setupPAMGuard folder once the GUI thread is happy everything is being set up properly. 
	 * @return
	 */
	public boolean hasCallBack();

	/**
	 * Get the name for a controlled unit from user input. 
	 * @param parentFrame - an object which can be the parent frame for the dialog. 
	 * @param moduleInfo - the type of controlled unit. 
	 * @return the name of the module. 
	 */
	public String getModuleName(Object parentFrame, PamModuleInfo moduleInfo);

}
