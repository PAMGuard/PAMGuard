package PamUtils;

import java.util.Map;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamModel.PamModel;
import PamView.dialog.warn.WarnOnce;

/**
 * An exception handler intended to catch Run-time exceptions.  Initially developed
 * to handle problems caused by incompatible plugins, this can be expanded to deal
 * with many more types of exceptions.
 * 
 * @author MO
 *
 */
public class PamExceptionHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		handleException(t, e);
	}

	protected void handleException(Thread thread, Throwable exception) {
		
	    // if PamModel is currently accessing an interface, warn the user and then move
	    // on
	    String pluginInterface = ((PamModel) PamController.getInstance().getModelInterface()).getPluginBeingLoaded();
	    if (pluginInterface!="none") {
			String title = "Error accessing plug-in module";
			String msg = "There is an error with the plug-in module " + pluginInterface + ".<p>" +
					"This may have been caused by an incompatibility between " +
					"the plug-in and this version of PAMGuard.  Please check the developer's website " +
					"for help.<p>" +
					"This plug-in will not be available for loading";
			String help = null;
			int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help, exception);
			System.err.println("Exception while loading " +	pluginInterface);
		    exception.printStackTrace();
	    } else {

	    	// if PamController is currently loading a module, warn the user and then remove
	    	// the module from the list
	    	PamControlledUnit module = PamController.getInstance().getUnitBeingLoaded();
	    	if (module!=null) {
	    		String title = "Error loading module";
	    		String msg = "There was an error while trying to load " + module.getUnitName() + ".<p>" +
						"If this is a plug-in, the error may have been caused by an incompatibility between " +
						"it and this version of PAMGuard.  Please check the developer's website " +
						"for help.<p>" +
						"If this is a core Pamguard module, please copy the error message text and email to" +
						"support@pamguard.org.<p>" +
						"This module will not be loaded.";
	    		String help = null;
	    		int ans = WarnOnce.showWarning(title, msg, WarnOnce.WARNING_MESSAGE, help, exception);
	    		System.err.println("Exception while loading " +	module.getUnitName());
	    		PamController.getInstance().removeControlledUnt(module);
	    		PamController.getInstance().clearLoadedUnit();
	    		exception.printStackTrace();
	    		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
	    	} else try {
	    		/* These might be handy to make future error messages more informative, so
	    		 * leave the code here for now
	    		 */
//	    		if (stackTrace == null || stackTrace.length == 0) {
//	    			
//	    		}
//	    		else {
	    		// sometimes len of stack trace is 0, so be careful !
//	    		StackTraceElement[] stackTrace = exception.getStackTrace();
//	    		String exceptionClass = exception.getStackTrace()[0].getClassName();
//	    		String exceptionMethod = exception.getStackTrace()[0].getMethodName();
//	    		String exceptionMessage = exception.getMessage();
//	    		String exceptionType = exception.getClass().getSimpleName();
//	    		String appName = exception.getClass().getPackage().getImplementationTitle();
	    		System.err.println("\nPamExceptionHandler: Caught Exception on thread " + thread.getName());
	    		
	    		// get Runtime instance and memory stats
	    		int mb = 1024 * 1024; 
	    		Runtime instance = Runtime.getRuntime();
	    		System.out.println("***** Heap utilization statistics [MB] *****");
	    		System.out.println("Total Memory Java has allocated: " + instance.totalMemory() / mb);
	    		System.out.println("Free Memory: " + instance.freeMemory() / mb);
	    		System.out.println("Used Memory: "
	    				+ (instance.totalMemory() - instance.freeMemory()) / mb);
	    		System.out.println("Max Memory Java can allocate: " + instance.maxMemory() / mb);
	    		
	    		

	    		// print stack trace
	    		exception.printStackTrace();
	    		
	    	}
	    	catch (Exception eeeeeee){
	    		exception.printStackTrace();
	    		eeeeeee.printStackTrace();
	    	}
	    } 
	    System.out.println(" ");
	    System.out.println("***********************************************************************");
	    System.out.println("*                         Windows Users                               *");
	    System.out.println("* In order to copy the entire contents of the DOS window to the clip  *");
	    System.out.println("* board, right-click in the window and click on 'Select All'.  This   *");
	    System.out.println("* highlights everything in the window (you can tell, because the      *");
	    System.out.println("* colours are now reversed).  Then right-click again in the window.   *");
	    System.out.println("* Everything that was highlighted is copied to the clip board, and    *");
	    System.out.println("* the colours return to normal.  You can now paste (CTRL-V) the text  *");
	    System.out.println("* into a Word document or email, for troubleshooting purposes later.  *");
	    System.out.println("***********************************************************************");
	}
}
