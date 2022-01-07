package pamViewFX.fxNodes.pamDialogFX;

import java.awt.Window;
import javax.swing.SwingUtilities;
import PamController.SettingsPane;
import PamView.dialog.PamDialog;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import pamViewFX.fxNodes.utilityPanes.BorderPaneFX2AWT;

/**
 * 
 * Creates a swing dialog which holds an FXPane. 
 * this should be used so long as we're using the main Swing GUI since
 * the main dialog window is in the correct child list of the AWT thread. 
 * @author macst & Doug Gillespie
 *
 */
public class PamDialogFX2AWT<T> extends PamDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The settings pane
	 */
	private SettingsPane<T> settingsPane;

	private BorderPaneFX2AWT holder;
	
	private T returnedParams;

	private T setParams;

	private PamJFXPanel dlgContent;
	
	public PamDialogFX2AWT(Window owner, SettingsPane<T> settingsPane, boolean hasDefault) {
		super(owner, settingsPane.getName(), hasDefault);
		settingsPane.setOwnerWindow(this);
		this.settingsPane = settingsPane;
		this.setDialogComponent(createJFXPane());
//		this.setResizable(false);
		this.setResizable(true);
//		this.pack();
		this.setHelpPoint(settingsPane.getHelpPoint());
	}


	/**
	 * Create the jfx panel 
	 * @return
	 */
	private JFXPanel createJFXPane(){

		//this has to be called in order to initialise the FX toolkit. Otherwise will crash if no other 
		//FX has been called. 
		dlgContent = new PamJFXPanel();

//		final CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(()->{
			holder = new BorderPaneFX2AWT(this);
//			Node node;
			//			holder.setCenter(settingsPane.getContentNode());
			holder.getChildren().add(settingsPane.getContentNode());
//			VBox.setVgrow(holder, Priority.ALWAYS);
			dlgContent.setRoot(holder);
		});
		return dlgContent; 
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#pack()
	 */
	@Override
	public void pack() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				settingsPane.repackContents();
				dlgContent.prePackFX();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						superpack();
					}
				});
			}
		});
	}
	
	private void superpack() {
		super.pack();
	}
	/**
	 * Show the dialog and initialise it with the given parameters. 
	 * @param params parameters to set in the dialog. 
	 * @return parameters returned from the dialog or null if cancel was 
	 * pressed. 
	 */
	public T showDialog(T params) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setParams(params);
			}
		});
		this.setVisible(true);
		return getReturnedParams();
	}
	
	public void setParams(T setParams) {
		this.setParams = setParams;
		settingsPane.setParams(setParams);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				pack();
			}
		});
		// make a timer arrive 2 seconds later as a test...
//		Timer t = new Timer(2000, new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				pack();
//				
//			}
//		});
//		t.start();
	}

	@Override
	public boolean getParams() {
		returnedParams = settingsPane.getParams(setParams);
		return returnedParams != null;
	}

	@Override
	public void cancelButtonPressed() {
		returnedParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		settingsPane.setDefaults();
	}
	
//	public abstract Pane createContentPane();


	/**
	 * @return the returnedParams
	 */
	public T getReturnedParams() {
		return returnedParams;
	}


	/**
	 * @return the dlgContent
	 */
	public JFXPanel getDlgContent() {
		return dlgContent;
	}



//	/**
//	 * Set settings pane. 
//	 * @param settingsPane2
//	 */
//	public void setSettingsPane(MTSettingsPane settingsPane2) {
//		Platform.runLater(()->{
//			holder.getChildren().add(settingsPane.getContentNode());
//		});
//	}

}
