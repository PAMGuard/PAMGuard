package PamController.pamWizard;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.Timer;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamGUIManager;
import PamModel.PamModuleInfo;
import PamguardMVC.PamDataBlock;
import dataPlotsFX.TDControlAWT;
import dataPlotsFX.TDDisplayController;
import dataPlotsFX.layout.TDDisplayFX;
import javafx.application.Platform;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

/**
 * Helper functions shared by {@link PamAutoConfig} implementations which build a
 * configuration around an FX time display. Adding modules and creating the time
 * display are the same whatever data are being viewed; only which data blocks end
 * up on the display differs.
 *
 * @author Jamie Macaulay
 */
public class PamAutoConfigUtils {

	public static final String TD_DISPLAY_CLASS = "dataPlotsFX.TDDisplayController";
	public static final String USER_DISPLAY_CLASS = "userDisplay.UserDisplayControl";

	/**
	 * Default number of polls made by {@link #whenReady} before giving up. With the
	 * 100 ms poll interval this is about a minute, which is enough for a data map
	 * scan of a reasonably large data set.
	 */
	private static final int DEFAULT_MAX_TRIES = 600;

	private PamAutoConfigUtils() {
		// static utility class.
	}

	/**
	 * Add a module by class name and return the created controlled unit.
	 *
	 * @param className  the module's controlled unit class name.
	 * @param moduleName the name to give the new module.
	 * @return the new controlled unit, or null if the module could not be found.
	 */
	public static PamControlledUnit addModule(String className, String moduleName) {
		PamModuleInfo moduleInfo = PamModuleInfo.findModuleInfo(className);
		if (moduleInfo == null) {
			System.err.println("PamAutoConfigUtils: could not find module " + className);
			return null;
		}
		return PamController.getInstance().addModule(moduleInfo, moduleName);
	}

	/**
	 * Add an FX time display in a GUI-appropriate way and return a supplier that
	 * resolves to its {@link TDDisplayFX}. In the FX GUI the time display is its own
	 * controlled unit; in the Swing GUI a User Display module is added and the FX
	 * time display created within it. Either way the display itself is created
	 * asynchronously on the JavaFX thread, so the supplier may return null for a
	 * while - see {@link #whenReady}.
	 *
	 * @param moduleName the name to give the display module.
	 * @return a supplier of the time display. Never null.
	 */
	public static Supplier<TDDisplayFX> addTimeDisplay(String moduleName) {
		if (PamGUIManager.isFX()) {
			TDDisplayController controller = (TDDisplayController) addModule(TD_DISPLAY_CLASS, moduleName);
			return () -> (controller == null) ? null : controller.getMainDisplay();
		}
		UserDisplayControl userDisplay = (UserDisplayControl) addModule(USER_DISPLAY_CLASS, moduleName);
		if (userDisplay == null) {
			return () -> null;
		}
		UserDisplayComponent component = userDisplay.addUserDisplay(TDControlAWT.class);
		if (component instanceof TDControlAWT) {
			TDControlAWT tdControl = (TDControlAWT) component;
			return tdControl::getMainDisplay;
		}
		return () -> null;
	}

	/**
	 * Wait (by polling) until the asynchronously-created {@link TDDisplayFX} is
	 * available, and until any additional condition is met, then run the given
	 * action on the JavaFX thread.
	 *
	 * @param supplier       supplier of the time display.
	 * @param extraCondition an additional condition which must be true before the
	 *                       action runs (e.g. that a data map has been built). May
	 *                       be null.
	 * @param action         the action to run on the JavaFX thread.
	 */
	public static void whenReady(Supplier<TDDisplayFX> supplier, BooleanSupplier extraCondition,
			Consumer<TDDisplayFX> action) {
		whenReady(supplier, extraCondition, action, DEFAULT_MAX_TRIES);
	}

	/**
	 * Wait (by polling) until the asynchronously-created {@link TDDisplayFX} is
	 * available, and until any additional condition is met, then run the given
	 * action on the JavaFX thread.
	 *
	 * @param supplier       supplier of the time display.
	 * @param extraCondition an additional condition which must be true before the
	 *                       action runs. May be null.
	 * @param action         the action to run on the JavaFX thread.
	 * @param maxTries       number of 100 ms polls before giving up.
	 */
	public static void whenReady(Supplier<TDDisplayFX> supplier, BooleanSupplier extraCondition,
			Consumer<TDDisplayFX> action, int maxTries) {
		final int[] tries = { 0 };
		Timer timer = new Timer(100, null);
		timer.addActionListener(e -> {
			boolean ready = false;
			TDDisplayFX display = null;
			try {
				display = supplier.get();
				ready = display != null && (extraCondition == null || extraCondition.getAsBoolean());
			}
			catch (Throwable t) {
				/*
				 * The things being polled - a display being built on the JavaFX thread, data
				 * maps being rebuilt by background workers - are being modified while this
				 * looks at them, so an occasional failure here means "not ready yet" rather
				 * than anything being wrong. It must not be allowed out onto the Swing thread.
				 */
				ready = false;
			}
			if (ready) {
				timer.stop();
				final TDDisplayFX readyDisplay = display;
				runOnFx(() -> action.accept(readyDisplay));
			}
			else if (++tries[0] > maxTries) {
				timer.stop();
				System.err.println("PamAutoConfigUtils: timed out waiting for the display / data map to initialise");
			}
		});
		timer.setRepeats(true);
		timer.start();
	}

	/**
	 * Run a task on the JavaFX application thread.
	 *
	 * @param runnable the task to run.
	 */
	public static void runOnFx(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		}
		else {
			Platform.runLater(runnable);
		}
	}


	/**
	 * Get the first output data block of a controlled unit, which is what a builder
	 * generally wants to put on a display.
	 *
	 * @param unit the controlled unit, may be null.
	 * @return the first output data block, or null.
	 */
	public static PamDataBlock getOutputDataBlock(PamControlledUnit unit) {
		if (unit == null || unit.getNumPamProcesses() == 0) {
			return null;
		}
		if (unit.getPamProcess(0).getNumOutputDataBlocks() == 0) {
			return null;
		}
		return unit.getPamProcess(0).getOutputDataBlock(0);
	}

}
