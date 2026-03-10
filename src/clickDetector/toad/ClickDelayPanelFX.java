package clickDetector.toad;

import javax.swing.SwingUtilities;

import PamController.SettingsPane;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import clickDetector.dialogs.ClickDelayDialog;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import pamViewFX.fxNodes.PamBorderPane;

public class ClickDelayPanelFX extends SettingsPane<ClickParameters>{
	
	private Button pButton;
	private ClickControl clickControl;
	private PamBorderPane borderPane;

	public ClickDelayPanelFX(Object ownerWindow, ClickControl clickControl) {
		super(ownerWindow);
		this.clickControl = clickControl;
		pButton = new Button(" Click timing settings ");
		pButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				clickTimingSettings();
			}
		});
		borderPane = new PamBorderPane();
		borderPane.setTop(pButton);
		borderPane.setPadding(new Insets(10, 10, 10, 10));
	}
	
	private void clickTimingSettings() {
		
		//TODO - convert to FX dialog
		SwingUtilities.invokeLater(() ->ClickDelayDialog.showDialog(clickControl.getGuiFrame(), clickControl));
	}

	@Override
	public ClickParameters getParams(ClickParameters currParams) {
		return clickControl.getClickParameters();
	}

	@Override
	public void setParams(ClickParameters clickParameters) {
//		clickDelayPanel.setParams(clickParameters);
	}

	@Override
	public String getName() {
		return "Click TOAD Measurement";
	}

	@Override
	public Node getContentNode() {
//		return null;
		return borderPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}


}
