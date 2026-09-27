package PamController.pamWizard;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import PamController.PamController;
import PamController.PamGUIManager;
import PamUtils.worker.PamWorkDialog;
import PamUtils.worker.PamWorkProgressMessage;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

/**
 * A small, non-modal progress display for the CPOD/FPOD import which is kicked
 * off automatically when POD files are dropped onto a blank configuration.
 * Converting POD files into binary files can take several minutes, so the user
 * needs to see that something is happening (and be able to stop it).
 * <p>
 * The import is a JavaFX {@link Task}, so an FX dialog is used in the FX GUI. In
 * the Swing GUI the existing {@link PamWorkDialog} is used instead, with the task
 * updates marshalled from the JavaFX thread onto the Swing event thread.
 *
 * @author Jamie Macaulay
 */
public class PODImportProgress {

	private static final String TITLE = "Importing POD data";

	private static final String HEADER = "Converting CPOD/FPOD files into PAMGuard binary files";

	private PODImportProgress() {
		// static utility class.
	}

	/**
	 * Show the progress display for a POD import task and close it again when the
	 * task finishes (or is cancelled or fails). Cancelling the dialog cancels the
	 * import.
	 * <p>
	 * Must be called on the JavaFX application thread, and before the task is
	 * started, so that no updates are missed.
	 *
	 * @param task the import task.
	 */
	public static void showProgress(Task<Integer> task) {
		if (task == null) {
			return;
		}
		if (PamGUIManager.isFX()) {
			showFXProgress(task);
		}
		else {
			showSwingProgress(task);
		}
	}

	/**
	 * FX GUI: a non-modal dialog with a progress bar bound to the task.
	 */
	private static void showFXProgress(Task<Integer> task) {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		dialog.setTitle(TITLE);
		dialog.setHeaderText(HEADER);

		ProgressBar progressBar = new ProgressBar();
		progressBar.setMaxWidth(Double.MAX_VALUE);
		progressBar.progressProperty().bind(task.progressProperty());

		Label message = new Label();
		message.textProperty().bind(task.messageProperty());

		VBox content = new VBox(5, progressBar, message);
		content.setPadding(new Insets(10, 10, 10, 10));
		content.setPrefWidth(440);
		dialog.getDialogPane().setContent(content);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

		// if the user closes the dialog before the import has finished, stop the import.
		final boolean[] finished = { false };
		dialog.setOnHidden(e -> {
			if (!finished[0]) {
				task.cancel();
			}
		});

		/*
		 * Use a state listener rather than setOnSucceeded etc. so that the caller is
		 * still free to use those handlers - they can only hold one handler each.
		 */
		task.stateProperty().addListener((obs, oldState, newState) -> {
			if (isFinished(newState)) {
				finished[0] = true;
				progressBar.progressProperty().unbind();
				message.textProperty().unbind();
				dialog.close();
			}
		});

		dialog.show();
	}

	/**
	 * Swing GUI: the standard PAMGuard work dialog, updated from the JavaFX thread.
	 * <p>
	 * The dialog is made modeless and disposable. It must not be modal: the import
	 * itself puts up modal warnings (see {@code CPODImporter.showImportWarning}) when
	 * a POD file cannot be read, and two modal siblings deadlock - this dialog blocks
	 * the warning from taking input, while the warning holds the Swing thread in a
	 * nested event loop. Since the work dialog hides its buttons and normally does
	 * nothing when closed, there is then no way out and PAMGuard appears frozen with
	 * the import progress still on screen.
	 */
	private static void showSwingProgress(Task<Integer> task) {
		final PamWorkDialog[] dialog = new PamWorkDialog[1];

		/*
		 * Set before the dialog is closed, and checked while it is being created, so
		 * that an import which finishes very quickly can never leave a dialog behind.
		 */
		final AtomicBoolean finished = new AtomicBoolean(false);

		task.stateProperty().addListener((obs, oldState, newState) -> {
			if (isFinished(newState)) {
				finished.set(true);
				SwingUtilities.invokeLater(() -> closeDialog(dialog));
			}
		});

		/*
		 * The dialog has to be created on the Swing event thread. Everything that
		 * touches it is sent to that thread with invokeLater, so the ordering
		 * guarantees the dialog exists before any update or dispose runs.
		 */
		SwingUtilities.invokeLater(() -> {
			if (finished.get()) {
				return; // already over - no point showing anything.
			}
			dialog[0] = new PamWorkDialog(PamController.getMainFrame(), 1, TITLE);
			dialog[0].setModalityType(java.awt.Dialog.ModalityType.MODELESS);
			dialog[0].setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog[0].setVisible(true);
			if (finished.get()) {
				closeDialog(dialog); // finished while the dialog was being created.
			}
		});

		task.progressProperty().addListener((obs, oldVal, newVal) -> {
			updateSwing(dialog, new PamWorkProgressMessage(toPercent(newVal.doubleValue())));
		});
		task.messageProperty().addListener((obs, oldVal, newVal) -> {
			updateSwing(dialog, new PamWorkProgressMessage(null, newVal));
		});
	}

	/**
	 * Close and dispose of the work dialog. Must be called on the Swing event thread.
	 */
	private static void closeDialog(PamWorkDialog[] dialog) {
		if (dialog[0] != null) {
			dialog[0].setVisible(false);
			dialog[0].dispose();
			dialog[0] = null;
		}
	}

	/**
	 * Send an update to the Swing work dialog on the Swing event thread.
	 */
	private static void updateSwing(PamWorkDialog[] dialog, PamWorkProgressMessage message) {
		SwingUtilities.invokeLater(() -> {
			if (dialog[0] != null && dialog[0].isVisible()) {
				dialog[0].update(message);
			}
		});
	}

	/**
	 * Convert a JavaFX task progress (0-1, or negative for indeterminate) into the
	 * percentage used by {@link PamWorkProgressMessage} (0-100, -1 indeterminate).
	 */
	private static Integer toPercent(double progress) {
		if (progress < 0 || Double.isNaN(progress)) {
			return -1;
		}
		return (int) Math.round(Math.min(1., progress) * 100.);
	}

	/**
	 * @return true if the task has reached a state it can't come back from.
	 */
	private static boolean isFinished(State state) {
		return state == State.SUCCEEDED || state == State.CANCELLED || state == State.FAILED;
	}
}
