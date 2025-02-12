package rawDeepLearningClassifier.dataPlotFX;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import PamView.ColourComboBox;
import PamView.PamAWTUtils;
import PamView.dialog.GenericSwingDialog;
import PamView.dialog.PamButton;
import PamView.dialog.PamDialogPanel;
import PamView.panel.PamPanel;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.SymbolModifier;
import Spectrogram.ColourRangeSlider;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import rawDeepLearningClassifier.dlClassification.DLClassName;

/**
 * 
 * Swing symbol options for the annotation pane.
 * 
 * @author Jamie Macaulay
 */
public class DLSymbolOptionPanel implements PamDialogPanel, ActionListener {

	private static final long serialVersionUID = 1L;

	private static final double CLASS_NAME_BOX_WIDTH = 130;

	private DLSymbolModifier dlSymbolModifier;

	/**
	 * The color range slider for coloring probabilities.
	 */
	private ColourRangeSlider colorRangeSlider;

	/**
	 * The combo box allowing users to select which class to show.
	 */
	private JComboBox<String> classNameBox;
	private JComboBox<String> classNameBox2;

	/**
	 * Check box allowing users only to show only those detections which have passed binary classification.
	 */
	private JCheckBox showOnlyBinary;

	/**
	 * Color picker which allows a user to select the gradient for coloring predictions
	 */
	private ColourComboBox colorComboBox;

	/**
	 * Color picker which allows a user to select color for each class.
	 */
	private JColorChooser colorPicker;

	private boolean initialized = true;

	/**
	 * Pane which holds controls for changing the colour based on prediciton value
	 */
	private JPanel probPane;

	/**
	 * Pane which holds controls for changing the colour based on the highest prediction value
	 */
	private JPanel classPane;

	private PamPanel holder;

	/**
	 * Button to select how to colour.
	 */
	private JToggleButton b1, b2;

	private PamPanel mainPanel;

	private int[] classColours;

	private AbstractButton colourButton;


	public DLSymbolOptionPanel(SymbolModifier symbolModifer) {
		this.dlSymbolModifier = (DLSymbolModifier) symbolModifer;

		probPane = createProbPane();
		probPane.setBorder(new TitledBorder("Colour by prediction value"));

		classPane = createClassPane();
		classPane.setBorder(new TitledBorder("Colour by class"));

		b1 = new JToggleButton("Prediction");
		b1.setPreferredSize(new Dimension(100, 25)); // Set preferred size

		b2 = new JToggleButton("Class");
		b2.setPreferredSize(new Dimension(100, 25));

		ButtonGroup buttonGroup = new ButtonGroup(); // Group toggle buttons
		buttonGroup.add(b1);
		buttonGroup.add(b2);

		b1.addActionListener(this);
		b2.addActionListener(this);

		JPanel segmentedButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		segmentedButtonPanel.add(b1);
		segmentedButtonPanel.add(b2);

		holder = new PamPanel();
		holder.setLayout(new BorderLayout());
		holder.add(new JLabel("Hello"),  BorderLayout.NORTH);

		mainPanel = new PamPanel();
		mainPanel.setLayout(new BorderLayout());

		mainPanel.add(segmentedButtonPanel, BorderLayout.NORTH);
		mainPanel.add(holder, BorderLayout.CENTER);
		mainPanel.add(showOnlyBinary = new JCheckBox("Show only binary classification"), BorderLayout.SOUTH);

		setSettingsPane();

		initialized = true;
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		//get the symbool options
		DLSymbolModifierParams symbolOptions =  dlSymbolModifier.getSymbolModifierParams(); 

		//			b1.setSelected(false);
		//			b2.setSelected(false);
		if (symbolOptions.colTypeSelection == DLSymbolModifierParams.PREDICITON_COL) b1.setSelected(true);
		if (symbolOptions.colTypeSelection == DLSymbolModifierParams.CLASS_COL) b2.setSelected(true);

		setSettingsPane();

		//set the parameters for colouring by prediction
		setPredictionColParams(symbolOptions);

		//set the class colour parameters
		setClassColParams(symbolOptions);

		//set the selected. 
		showOnlyBinary.setSelected(symbolOptions.showOnlyBinary);

		setSettingsPane();
	} 


	private int checkClassNamesBox(DLSymbolModifierParams symbolOptions, JComboBox<String> classNameBox) {

		DLClassName[] classNames = dlSymbolModifier.getDLAnnotType().getDlControl().getDLModel().getClassNames(); 

		//		for (int i =0; i<classNames.length; i++) {
		//			System.out.println("DLSymbolOptionsPane: classNames: " + i + "  " + classNames[i].className); 
		//		}

		int nClass = dlSymbolModifier.getDLAnnotType().getDlControl().getDLModel().getNumClasses(); 

		classNameBox.removeAllItems();
		for (int i=0; i<nClass; i++) {
			if (classNames!=null && classNames.length>i) {
				classNameBox.addItem(classNames[i].className); 
			}
			else {
				classNameBox.addItem("Class: " +  i);
			}
		}

		return nClass;

	}


	/**
	 * Set parameters for controls to change the colour gradient based on prediction. 
	 * @param symbolOptions - the symbol options
	 */
	private void setPredictionColParams(DLSymbolModifierParams symbolOptions) {
		
		if (symbolOptions.clims[0]==symbolOptions.clims[1]) {
			
			if (symbolOptions.clims[0]>0 && symbolOptions.clims[0]<1.) {
				symbolOptions.clims[0]=symbolOptions.clims[0]-0.1;
				symbolOptions.clims[0]=symbolOptions.clims[0]+0.1;
			}
			else {
			symbolOptions.clims[0] = 0.1;
			symbolOptions.clims[1] = 0.9;
			}
		}
		
		//System.out.println("Set upper value: " + symbolOptions.clims[0]  + " " + symbolOptions.clims[1]); 

		//now set frequency parameters 
		colorRangeSlider.setValue((int) (symbolOptions.clims[0]*100));
		colorRangeSlider.setUpperValue((int) (symbolOptions.clims[1]*100));
		//		colorRangeSlider.setColourArrayType( symbolOptions.colArray);
		
		//System.out.println("Set upper value: " + colorRangeSlider.getValue() + " " + colorRangeSlider.getUpperValue()); 


		colorRangeSlider.setColourMap(PamUtilsFX.fxColArray2Swing(symbolOptions.colArray));


		int nClass = checkClassNamesBox( symbolOptions, classNameBox); 

		symbolOptions.classIndex = Math.min(symbolOptions.classIndex, nClass-1); 
		classNameBox.setSelectedIndex(Math.max(symbolOptions.classIndex, 0));

		//color box.
		colorComboBox.setSelectedColourMap(PamUtilsFX.fxColArray2Swing(symbolOptions.colArray));
	}

	/**
	 * Set parameters for controls to change the colour gradient based on prediction. 
	 * @param symbolOptions - the symbol options
	 */
	private void setClassColParams(DLSymbolModifierParams symbolOptions) {

		//create a temporary array to save different class colours in - this needs to be
		//before other params are set. 
		this.classColours = symbolOptions.classColors;


		int nClass = checkClassNamesBox( symbolOptions, classNameBox2); 

		symbolOptions.classIndex = Math.min(symbolOptions.classIndex, nClass-1); 

		classNameBox2.setSelectedIndex(Math.max(symbolOptions.classIndex2, 0));


//		int index = symbolOptions.classIndex2>=0 ? symbolOptions.classIndex2 : 0;

		if (symbolOptions.classColors==null) {
			symbolOptions.setDefaultClassColors(nClass);
		}
		
		if (classNameBox2.getSelectedIndex()>=0) {
			colourButton.setBackground(PamAWTUtils.intToColor(classColours[classNameBox2.getSelectedIndex()]));
			colourButton.repaint();
		}


		//		//set the correct colour
		//		colorPicker.setColor(symbolOptions.classColors[index]);
	}


	/**
	 * get parameters for colouring by class. 
	 * @param symbolOptions - the symbol options. 
	 * @return
	 */
	public DLSymbolModifierParams getClassColParams(DLSymbolModifierParams symbolOptions ) {

		//		int index = classNameBox2.getSelectedIndex()>=0 ? classNameBox2.getSelectedIndex():0;

		symbolOptions.classColors = classColours;

		symbolOptions.classIndex2 = classNameBox2.getSelectedIndex(); 

		return symbolOptions; 
	}

	/**
	 * 
	 * @param symbolOptions
	 * @return
	 */
	public DLSymbolModifierParams getPredictionColParams(DLSymbolModifierParams symbolOptions ) {

		symbolOptions.clims=new double[] {((double) colorRangeSlider.getValue())/100., ((double) colorRangeSlider.getUpperValue())/100.};

		symbolOptions.colArray = PamUtilsFX.swingColArray2FX(this.colorComboBox.getSelectedColourMap()); 

		symbolOptions.classIndex = classNameBox.getSelectedIndex(); 

		return symbolOptions; 
	}

	@Override
	public boolean getParams() {

		//bit messy but works
		DLSymbolModifierParams symbolOptions =  dlSymbolModifier.getSymbolModifierParams().clone(); 

		//need to check this here. 
		//checkClassNamesBox(symbolOptions); 

		if (b1.isSelected()) symbolOptions.colTypeSelection = DLSymbolModifierParams.PREDICITON_COL;
		if (b2.isSelected()) symbolOptions.colTypeSelection = DLSymbolModifierParams.CLASS_COL;

		//get parameters for colouring 
		symbolOptions = getClassColParams(symbolOptions);

		//get parameters for colouring by prediction value
		symbolOptions = getPredictionColParams(symbolOptions) ;

		symbolOptions.showOnlyBinary = showOnlyBinary.isSelected(); 

		dlSymbolModifier.checkColourArray(); 

		//set the paratmers. 
		dlSymbolModifier.setSymbolModifierParams(symbolOptions);

		return true;
	}


	private void setSettingsPane() {
		holder.removeAll();
		//holder.setLayout(new BorderLayout());
		holder.validate();

		if (b1.isSelected()) {
			holder.add(probPane, BorderLayout.CENTER);
		} else if (b2.isSelected()) {
			holder.add(classPane, BorderLayout.CENTER);
		}

		holder.validate();
		mainPanel.validate();

		if (mainPanel.getRootPane()!=null) {
			//pack the dialog because it is a different size 
			((GenericSwingDialog) mainPanel.getRootPane().getParent()).pack();
		}
	}


	private JPanel createClassPane() {

		classNameBox2 = new JComboBox<>();
		classNameBox2.addActionListener((action)->{
			//make sure the setting button shows the colour
			if (classNameBox2.getSelectedIndex()>=0) {
				colourButton.setBackground(PamAWTUtils.intToColor(classColours[classNameBox2.getSelectedIndex()]));
			}
			setSettingsPane();
		});

		classNameBox2.setPreferredSize(new Dimension((int) CLASS_NAME_BOX_WIDTH, 25));

		//		colorPicker.setPreferredSize(new Dimension(60, 25));
		colourButton = new PamButton("Color");
		colourButton.addActionListener((action)->{
			Color color = JColorChooser.showDialog(colourButton, "Pick colour for class", PamAWTUtils.intToColor(classColours[classNameBox2.getSelectedIndex()])); 
			if (color!=null) {
				colourButton.setBackground(color);
				//			colourButton.setForeground(color);
				classColours[classNameBox2.getSelectedIndex()]=PamAWTUtils.colorToInt(color);
			}
		});

		JPanel classHolder = new JPanel();
		FlowLayout flowLayout = new FlowLayout();
		classHolder.setLayout(flowLayout);

		classHolder.add(classNameBox2);
		classHolder.add(colourButton);

		return classHolder;
	}


	private PamPanel createProbPane() {
		PamPanel holder = new PamPanel();
		holder.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;

		holder.add(new JLabel("Select class"), c); 

		c.gridx++;
		classNameBox = new JComboBox<String>();
		holder.add(classNameBox, c); 

		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		holder.add(new JLabel("Color map"), c); 

		c.gridx++;
		colorComboBox = new ColourComboBox();
		colorComboBox.addActionListener((action)->{
			colorRangeSlider.setColourMap(colorComboBox.getSelectedColourMap());
			colorRangeSlider.repaint();
		});

		holder.add(colorComboBox, c); 

		c.gridx = 0;
		c.gridy++;
		c.gridwidth =2;
		colorRangeSlider = new ColourRangeSlider(0, 100, JSlider.HORIZONTAL); // Min 0, Max 1 for probabilities
		colorRangeSlider.setPaintTicks(true);
		colorRangeSlider.setMinorTickSpacing(20);
		holder.add(colorRangeSlider, c); 

		return holder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setSettingsPane();
	}

}
