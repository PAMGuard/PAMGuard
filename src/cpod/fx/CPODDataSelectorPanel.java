package cpod.fx;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import PamView.dialog.PamDialogPanel;
import PamView.panel.PamPanel;
import PamView.sliders.PamRangeSlider;
import cpod.dataSelector.CPODDataSelector;
import cpod.dataSelector.StandardCPODFilterParams;

/**
 * Swing data selector 
 */
public class CPODDataSelectorPanel implements PamDialogPanel {

	private JComponent mainPane;

	private CPODDataSelector cpodDataSelector;
	
	private ArrayList<StandardCPODFilterPanel> standardCPODFilterPanes;
	
	private JCheckBox clcikTrainCheckBox;

	private JComboBox<String> speciesSelectBox;

//	private CPODDataSelectorPane cpodDataSlectorPane;

	public CPODDataSelectorPanel(CPODDataSelector cpodDataSelelctor) {
		this.cpodDataSelector=cpodDataSelelctor; 
		mainPane = createPanel();
//		createFXPanel();
	}
	
	
	private JComponent createPanel() {
		PamPanel vBox = new PamPanel();
		vBox.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints(); 
		
		c.gridheight=3;
		c.gridx = 0;
		c.gridy = 0;

		
		standardCPODFilterPanes = new ArrayList<StandardCPODFilterPanel>();
		ArrayList<StandardCPODFilterParams> standParams = cpodDataSelector.getParams().cpodDataFilterParams; 
		for (int i=0; i<standParams.size(); i++) {
			standardCPODFilterPanes.add(new StandardCPODFilterPanel( cpodDataSelector.getParams().cpodDataFilterParams.get(i))); 
			
			PamPanel.addComponent(vBox, standardCPODFilterPanes.get(i), c);
			c.gridx = 0;

			c.gridy++;
		}
		
		this.clcikTrainCheckBox = new JCheckBox("Select click trains only: Species"); 
		clcikTrainCheckBox.addActionListener((action)->{
			speciesSelectBox.setEnabled(clcikTrainCheckBox.isSelected()); 
		});
		this.speciesSelectBox = new JComboBox<String>(); 
		
		c.gridy++;

		PamPanel.addComponent(vBox, clcikTrainCheckBox, c);
		c.gridx = 2;
		PamPanel.addComponent(vBox, speciesSelectBox, c);

		//CPODs and FPODs have set species identifiers.
		this.speciesSelectBox.addItem("All");
		this.speciesSelectBox.addItem("Unknown");
		this.speciesSelectBox.addItem("NBHF");
		this.speciesSelectBox.addItem("Dolphins");
		this.speciesSelectBox.addItem("Sonar");


		return vBox; 
	}
	
	
	

//	private void createFXPanel() {
//		// This method is invoked on Swing thread
//		final JFXPanel fxPanel = new JFXPanel();
//		mainPane.add(fxPanel); 
//
//
//		Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
//				initFX(fxPanel);
//			}
//		});
//	}
//
//	private void initFX(JFXPanel fxPanel) {
//		cpodDataSlectorPane = new CPODDataSelectorPane(cpodDataSelelctor); 
//		// This method is invoked on JavaFX thread
//		Group  root  =  new  Group();
//		Scene  scene  =  new  Scene(root);
//
//		root.getChildren().add(cpodDataSlectorPane.getContentNode());
//
//		fxPanel.setScene(scene);
//	}




	@Override
	public JComponent getDialogComponent() {
		return mainPane;
	}

	@Override
	public void setParams() {
		
		for (int i=0; i<cpodDataSelector.getParams().cpodDataFilterParams.size(); i++) {
			standardCPODFilterPanes.get(i).setParams(cpodDataSelector.getParams().cpodDataFilterParams.get(i)); 
		}
		
		clcikTrainCheckBox.setSelected(cpodDataSelector.getParams().selectClickTrain);
		speciesSelectBox.setSelectedIndex(CPODDataSelectorPane.getSpeciesIndex(cpodDataSelector.getParams().speciesID));
		
		speciesSelectBox.setEnabled(cpodDataSelector.getParams().selectClickTrain); 

	}

	@Override
	public boolean getParams() {
		
		for (int i=0; i<cpodDataSelector.getParams().cpodDataFilterParams.size(); i++) {
			standardCPODFilterPanes.get(i).getParams(cpodDataSelector.getParams().cpodDataFilterParams.get(i)); 
		}
		
		cpodDataSelector.getParams().selectClickTrain = clcikTrainCheckBox.isSelected();
		 
		cpodDataSelector.getParams().speciesID = CPODDataSelectorPane.getSpecies(speciesSelectBox.getSelectedIndex());		
		
		return true;
	}
	
	
	/**
	 * The CPOD data filter pane.
	 * @author Jamie Macaulay
	 *
	 */
	public class StandardCPODFilterPanel extends PamPanel {

		/**
		 * The range slider. 
		 */
		private PamRangeSlider rangeSlider;

		public StandardCPODFilterPanel(StandardCPODFilterParams params) {

			rangeSlider = new PamRangeSlider(); 
			JLabel topLabel = new JLabel(); 

			rangeSlider.setMinimum(0);
			rangeSlider.setMaximum(255);

			rangeSlider.setPaintTicks(true);
//			rangeSlider.setShowTickMarks(true);

			switch (params.dataType) {
			case StandardCPODFilterParams.AMPLITUDE:
				topLabel.setText("Amplitude (dB re 1\u03BCPa)");
				rangeSlider.setMinimum(80);
				rangeSlider.setMaximum(170);
				break;
			case StandardCPODFilterParams.PEAK_FREQ:
				topLabel.setText("Peak Frequency (kHz)");
				break;
			case StandardCPODFilterParams.BW:
				topLabel.setText("Bandwidth (kHz)");
				rangeSlider.setMinimum(0);
				rangeSlider.setMaximum(100);

				break;
			case StandardCPODFilterParams.END_F:
				topLabel.setText("End Frequency (kHz)");

				break;

			case StandardCPODFilterParams.NCYCLES:
				topLabel.setText("Number cycles");
				rangeSlider.setMinimum(0);
				rangeSlider.setMaximum(40);
				break;
			}

			this.setLayout(new BorderLayout());

			this.add(topLabel, BorderLayout.NORTH);
			this.add(rangeSlider, BorderLayout.CENTER);
		
			
			this.setParams(params);


		}

		public void setParams(StandardCPODFilterParams standardCPODFilterParams) {
//			System.out.println("StandardCPODFilterPane. SET PARAMS: min: " + standardCPODFilterParams.min + "  " + standardCPODFilterParams.max); 
			//set the parameters. 
			
			rangeSlider.setValue((int) standardCPODFilterParams.max);
			rangeSlider.setUpperValue((int) standardCPODFilterParams.min);
		}

		public void getParams(StandardCPODFilterParams standardCPODFilterParams) {
			//standardCPODFilterParams.max = rangeSlider.getMax();
			
			//if the range sliders are maxed out then all values are used. 
			if (rangeSlider.getUpperValue()==rangeSlider.getMinimum()) {
				standardCPODFilterParams.max = Double.POSITIVE_INFINITY; 
			}
			else {
				standardCPODFilterParams.max = rangeSlider.getUpperValue();
			}

			if (rangeSlider.getValue()==rangeSlider.getMinimum()) {
				standardCPODFilterParams.min = Double.NEGATIVE_INFINITY; 
			}
			else {
				standardCPODFilterParams.min = rangeSlider.getValue();
			}
			
//			standardCPODFilterParams.max = rangeSlider.getHighValue();
//			standardCPODFilterParams.min = rangeSlider.getLowValue();
	
		}

	}



}
