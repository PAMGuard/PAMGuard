package PamView.symbol.modifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import PamView.ColourComboBox;
import PamView.dialog.PamDialogPanel;
import PamView.panel.PamPanel;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Swing panel for changing parameters for the peak frequency symbol chooser. 
 * 
 * @author Jamie Macaulay	
 */
public class PeakFreqOptionsPanel implements PamDialogPanel {

	private PeakFreqModifier freqSymbolModifer;
	
	private PamPanel mainPanel;

	private JSpinner minFreq;

	private JSpinner maxFreq;

	private ColourComboBox colourBox;

	public PeakFreqOptionsPanel(PeakFreqModifier symbolModifer) {
		this.freqSymbolModifer=symbolModifer;
		mainPanel = createFreqPaneL();
	}


	/**
	 * Pane which changes the frequency limits. 
	 * @return pane with controls to change freq. limits. 
	 */
	private PamPanel createFreqPaneL(){

		PamPanel holder = new PamPanel();
		holder.setLayout(new GridBagLayout());
		holder.setBorder(new TitledBorder("Peak frequency colour map"));

		GridBagConstraints c = new GridBagConstraints(); 
		c.gridy=0;
		c.gridx=0;
		
		//doesn't seem to work so added spaces in labels instead
		c.ipadx = 5;
		

		c.anchor =GridBagConstraints.EAST;
		holder.add(new JLabel("Min. freq"), c);

		c.gridx++;
		c.anchor =GridBagConstraints.WEST;
		minFreq = new JSpinner(new SpinnerNumberModel(0.,  0., 10000000., 1000.)); 
		//make the lock button the same height as the spinner
		Dimension prefSize = minFreq.getPreferredSize();
		minFreq.setPreferredSize(new Dimension(90, prefSize.height));
		
		holder.add(minFreq, c);

		c.gridx++;
		c.ipadx = 5;
		c.anchor =GridBagConstraints.EAST;
		holder.add(new JLabel(" Max. freq"), c);
		
		c.gridx++;
		c.ipadx = 5;
		c.anchor =GridBagConstraints.WEST;
		maxFreq =new JSpinner(new SpinnerNumberModel(1000., 1., Math.max(1000.,freqSymbolModifer.getSymbolChooser().getPamDataBlock().getSampleRate()/2.), 1000.)); 
		maxFreq.setPreferredSize(new Dimension(90, prefSize.height));
		holder.add(maxFreq, c);
		
		c.gridx++;
		c.ipadx = 5;
		c.anchor =GridBagConstraints.WEST;
		holder.add(new JLabel(" Hz"), c);
		

		c.gridy++;
		c.ipadx = 5;
		c.gridwidth=2;
		
		c.gridx=0;
		c.anchor =GridBagConstraints.EAST;
		holder.add(new JLabel("Freq. colour map"), c);

		c.gridx=2;
		c.gridwidth=3;
		colourBox = new ColourComboBox();
		c.anchor =GridBagConstraints.WEST;
		prefSize = colourBox.getPreferredSize();
		colourBox.setPreferredSize(new Dimension(200, prefSize.height));
		holder.add(colourBox, c);

		return holder;

	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}




	
	@Override
	public boolean getParams(){

		//bit messy but works / 
//		PeakFreqSymbolOptions symbolOptions = (PeakFreqSymbolOptions) standardSymbolOptions.getModifierParams(this.getSymbolModifier().getName());

		PeakFreqSymbolOptions symbolOptions =  (PeakFreqSymbolOptions)freqSymbolModifer.getSymbolModifierParams().clone(); 
			//must make sure we do not call get parameters during a set parameters - the listeners on the controls call getParams so all goes
			//haywire if the setParams is not set properly. 
			//System.out.println("GETPARAMS: " +  ColourArray.getColorArrayType(this.colourBox.getSelectionModel().getSelectedItem()) + "  " + setParams); 
			symbolOptions.freqLimts=new double[] {(double) minFreq.getValue(), (double)  maxFreq.getValue()};
			symbolOptions.freqColourArray = PamUtilsFX.swingColArray2FX(this.colourBox.getSelectedColourMap()); 
	
		//System.out.println("StandardSymbolModifierPane : getParams(): new mod: " +mod); 

			freqSymbolModifer.setSymbolModifierParams(symbolOptions);
			
			System.out.println("Get freq limits: 1: " + symbolOptions.freqLimts[0] +  "  " + symbolOptions.freqLimts[1] + "  " + (double)  maxFreq.getValue());

			
			return true;

	}

	@Override
	public void setParams() {
				
//			StandardSymbolOptions standardSymbolOptions = (StandardSymbolOptions) getSymbolModifier().getSymbolChooser().getSymbolOptions();
//			PeakFreqSymbolOptions symbolOptions = (PeakFreqSymbolOptions) standardSymbolOptions.getModifierParams(this.getSymbolModifier().getName());
			PeakFreqSymbolOptions symbolOptions =  (PeakFreqSymbolOptions)freqSymbolModifer.getSymbolModifierParams(); 

			//now set frequency parameters; 
			checkFreqLimits( symbolOptions ) ;
			
			minFreq.setValue(symbolOptions.freqLimts[0]);
			maxFreq.setValue(symbolOptions.freqLimts[1]);

			colourBox.setSelectedColourMap((PamUtilsFX.fxColArray2Swing(symbolOptions.freqColourArray)));
	} 

	/**
	 * Check the frequency limits make sense for the given datablock 
	 * @param symbolOptions - the peak frequency options. 
	 */
	private void checkFreqLimits(PeakFreqSymbolOptions symbolOptions ) {

		System.out.println("Check freq limits: 1: " + symbolOptions.freqLimts[0] +  "  " + symbolOptions.freqLimts[1]);

		SpinnerNumberModel spinnerValFact = (SpinnerNumberModel) maxFreq.getModel();
		spinnerValFact.setMaximum(getSampleRate() /2.);
		//for some reason also need to set this
		spinnerValFact.setMinimum(1.);
		
		//set reasonable step sizes
		if (getSampleRate()>=10000) {
			spinnerValFact.setStepSize(1000.);
		}
		else if (getSampleRate()>=2000){
			spinnerValFact.setStepSize(200.);
		}
		else {
			spinnerValFact.setStepSize(50.);
		}

		if (symbolOptions.freqLimts==null) {
			symbolOptions.freqLimts= new double[] {0, getSampleRate() /2}; 
		}
		 System.out.println("Check freq limits: 2: " + symbolOptions.freqLimts[0] +  "  " + symbolOptions.freqLimts[1]);
		//check nyquist for upper limit
		if (symbolOptions.freqLimts[1]>getSampleRate() /2) {
			symbolOptions.freqLimts[1]=getSampleRate() /2; 
		}
		//check nyquist for lower limit
		if (symbolOptions.freqLimts[0]>getSampleRate() /2) {
			symbolOptions.freqLimts[0]=0; 
		}
	}
	
	

	private float getSampleRate() {
		return freqSymbolModifer.getSymbolChooser().getPamDataBlock().getSampleRate(); 
	}


}
