package PamController;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.PamIcon;
import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;

/**
 * A simple dialog which allows the user to select which mode to run PAMGuard in. 
 */
public class PamRunModeDialog  extends PamDialog {

	private static final long serialVersionUID = 1L;

	private static PamRunModeDialog singleInstance;

	private JPanel mainPanel;

	private PamRunModeParams runbModeParams = new PamRunModeParams();

	private AbstractButton normalMode;

	private AbstractButton viewerMode;

	public PamRunModeDialog(Window parentFrame) {
		super(parentFrame, "Storage Options", false);
		
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Select PAMGuard mode"));
		mainPanel.setLayout(new GridBagLayout());

		//toggle buttonb
		normalMode = new JToggleButton("Normal");
		normalMode.setFont(normalMode.getFont().deriveFont(Font.BOLD));
		normalMode.addItemListener((a)->{
			// event is generated in button
			int state = a.getStateChange();
		});
		normalMode.setToolTipText(
				"<html>Run PAMGuard in normal mode. <p>"
						+ 	"Normal mode allows users to use PAMGuard in real time or processing <br>"
						+ 	"raw acoustic data from recorders.</html>");


		viewerMode = new JToggleButton("Viewer");
		viewerMode.setFont(viewerMode.getFont().deriveFont(Font.BOLD));
		viewerMode.addItemListener((a)->{
			// event is generated in button
			int state = a.getStateChange();
		});

		viewerMode.setToolTipText(
				"<html>Run PAMGuard in viewer mode. <p>"
						+"Viewer mode is used to view processed data from normal mode</html>");
		
		
	    try {
			Image img = ImageIO.read(getClass().getResource(File.separator+PamIcon.getPAMGuardIconPath(PamIcon.NORMAL)));
			normalMode.setIcon(new ImageIcon(img));
			normalMode.setHorizontalAlignment(SwingConstants.TRAILING);
		    viewerMode.setIcon(new ImageIcon(img));
		    viewerMode.setHorizontalAlignment(SwingConstants.TRAILING);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalMode);
		buttonGroup.add(viewerMode);
		
		GridBagConstraints gridBagConstrints = new GridBagConstraints(); 
		gridBagConstrints.gridy = 0;
		gridBagConstrints.gridx = 0;
		gridBagConstrints.insets = new Insets(5,5,5,5);

//		PamPanel.addComponent(mainPanel, new JLabel("Select PAMGuard mode"), gridBagConstrints);
//		gridBagConstrints.gridwidth=2;
//		
//		gridBagConstrints.gridwidth=1;
		gridBagConstrints.gridy ++;
		PamPanel.addComponent(mainPanel, normalMode, gridBagConstrints);
		gridBagConstrints.gridx ++;
		PamPanel.addComponent(mainPanel, viewerMode, gridBagConstrints);

		setDialogComponent(mainPanel);


	}

	public static PamRunModeParams showDialog(JFrame parentFrame) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new PamRunModeDialog(parentFrame);
		}
		singleInstance.setVisible(true);
		return singleInstance.runbModeParams;

	}

	@Override
	public boolean getParams() {
		if (normalMode.isSelected()) {
			runbModeParams.runMode=PamController.RUN_NORMAL;
			return true;
		}
		else if (viewerMode.isSelected()) {
			runbModeParams.runMode=PamController.RUN_PAMVIEW;
			return true;
		}
		else {
			PamDialog.showWarning(singleInstance, "No PAMGuard mode selected", "You must select which mode to run PAMGuard in");
		}
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		runbModeParams.runMode=-1;
	}
	

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}