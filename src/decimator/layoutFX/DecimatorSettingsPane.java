package decimator.layoutFX;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamAlignmentPanel;
import PamguardMVC.PamDataBlock;
import dataMap.filemaps.OfflineFileDialogPanel;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import decimator.DecimatorParamsDialog.DefaultFilterButton;
import decimator.DecimatorParamsDialog.FilterButton;
import decimator.DecimatorParamsDialog.SPMonitor;
import decimator.DecimatorParamsDialog.SPSelection;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

/**
 * 
 * Settings for the decimator.
 * 
 * @author Jamie Macaulay
 */
public class DecimatorSettingsPane extends SettingsPane<DecimatorParams> {
	
	private PamBorderPane mainPane;

	private DecimatorControl decimatorControl;

	private SourcePaneFX sourcePanel;

	private Label sourceSampleRate;

	private float inputSampleRate;

	private TextField newSampleRate;

	public DecimatorSettingsPane(DecimatorControl aquisitionControl) {
		super(null);
		
		mainPane= new PamBorderPane();
		this.decimatorControl = aquisitionControl;

		mainPane.setCenter(new Label("Hello Decimator Pane"));
		
	}
	
	private Pane createPane() {
		
		PamVBox mainPanel = new PamVBox();
		
		GridBagConstraints constraints = new PamGridBagContraints();
//		insets = new Insets(2,2,2,2);
			
		sourcePanel = new SourcePaneFX( RawDataUnit.class, true, true);
		sourcePanel.addSelectionListener((obsval, oldVal, newVal)->{
			newDataSource();
		});
		
//		sourcePanel.addSourcePanelMonitor(new SPMonitor());
		mainPanel.getChildren().add(sourcePanel.getPane());		
		
		PamGridPane decimatorPanel = new PamGridPane();

		
		Label label = new Label("Decimator settings");
		
		int gridx = 0;
		int gridy = 0;
		decimatorPanel.add(new Label("Source sample rate "), gridx, gridy);
		gridx++;
		decimatorPanel.add(sourceSampleRate = new Label(" - Hz"),  gridx, gridy);
		gridx = 0;
		gridy ++;
		decimatorPanel.add(new JLabel("Output sample rate "),  gridx, gridy);
		gridx ++;
		decimatorPanel.add(newSampleRate = new TextField(),  gridx, gridy);
		gridx ++;
		decimatorPanel.add(new JLabel(" Hz"),  gridx, gridy);
		gridy ++;
		gridx = 0;
		gridwidth = 1;
		decimatorPanel.add(filterButton = new PamButton("Filter settings"),  gridx, gridy);
		filterButton.addActionListener(new FilterButton());
		gridx = 1;
		gridwidth = 2;
		addComponent(decimatorPanel, defaultFilterButton = new PamButton("Default Filter"),  gridx, gridy);
		defaultFilterButton.addActionListener(new DefaultFilterButton());
		gridx = 0;
		gridwidth = 3;
		gridy++;
		addComponent(decimatorPanel, filterInfo = new JLabel("Filter: "),  gridx, gridy);
		gridx = 0;
		gridwidth = 1;
		gridy++;
		addComponent(decimatorPanel, new JLabel("Interpolation: ", JLabel.RIGHT),  gridx, gridy);
		gridx += gridwidth;
		gridwidth = 2;
		addComponent(decimatorPanel, interpolator = new JComboBox<String>(),  gridx, gridy);
		interpolator.addItem("None");
		interpolator.addItem("Linear");
		interpolator.addItem("Quadratic");

		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		if (isViewer) {
			JTabbedPane tabbedPane = new JTabbedPane();
			offlineDAQDialogPanel = new OfflineFileDialogPanel(decimatorControl, this);
			tabbedPane.add("Offline Files", offlineDAQDialogPanel.getComponent());
			tabbedPane.add("Runtime Settings", mainPanel);
			setDialogComponent(tabbedPane);
		}
		else {
			setDialogComponent(mainPanel);
		}
		
		setHelpPoint("sound_processing.decimatorHelp.docs.decimator_decimator");
		filterButton.setToolTipText("Manual adjustment of filter settings");
		defaultFilterButton.setToolTipText("Set a default filter (6th order Butterworth low pass at Decimator Nyquist frequency)");
		interpolator.setToolTipText("If Decimation / upsampling is not by an integer value, you should use interpolation to improve waveform reconstruction");
	}


	@Override
	public DecimatorParams getParams(DecimatorParams currParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(DecimatorParams input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	private void newDataSource() {
		PamDataBlock block = sourcePanel.getSource();
		if (block != null) {
			sourceSampleRate.setText(String.format("%.1f Hz", 
					inputSampleRate = block.getSampleRate()));
		}
	}

}
