package PamView.symbol;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.PamSymbolDialog;
import PamView.GeneralProjector.ParameterType;
import PamView.dialog.GenericSwingDialog;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SettingsButton;
import PamView.symbol.modifier.swing.SymbolModifierPanel;
import PamguardMVC.PamDataBlock;

public class StandardSymbolOptionsPanel implements SwingSymbolOptionsPanel {

	private JPanel mainPanel;

	private JRadioButton[] optionButtons;

	private StandardSymbolChooser standardSymbolChooser;

	private StandardSymbolManager standardSymbolManager;

	private JButton symbolButton;

	private JCheckBox genericBox;

	private JCheckBox useAnnotation;

	private JTextField lineLength;

	private JCheckBox showLinesToLatLongs;

	private boolean lineLengthOption;

	private PamSymbol currentSymbol;

	private PamDataBlock dataBlock;

//	private JComboBox<DataAnnotationType<?>> annotationList;

	private int[] optionButtonOrder = {4, 3, 2, 1, 0, 5};

	private SettingsButton anSetButton;
	
	private SymbolModifierPanel symbolModifierPanel;

	public StandardSymbolOptionsPanel(StandardSymbolManager standardSymbolManager, StandardSymbolChooser standardSymbolChooser) {
		this.standardSymbolManager = standardSymbolManager;
		this.standardSymbolChooser = standardSymbolChooser;
		dataBlock = standardSymbolManager.getPamDataBlock();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		optionButtons = new JRadioButton[StandardSymbolManager.NUM_STANDARD_CHOICES];
		ButtonGroup buttonGroup = new ButtonGroup();
		for (int i = 0; i < StandardSymbolManager.NUM_STANDARD_CHOICES; i++) {
			optionButtons[i] = new JRadioButton(standardSymbolManager.colourChoiceName(i));
			buttonGroup.add(optionButtons[i]);
		}

		/**
		 * Hide some option buttons that are unavailable with this set. 
		 */
		if (standardSymbolManager.getSpecialColourName() == null) {
			optionButtons[StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL].setVisible(false);
			optionButtons[StandardSymbolOptions.COLOUR_SPECIAL].setVisible(false);
		}
		optionButtons[StandardSymbolOptions.COLOUR_HYDROPHONE].setVisible(
				standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_CHANNEL_OPTIONS));

//		ArrayList<DataAnnotationType<?>> annotationTypes = getColouringAnnotationTypes();
//		if (annotationTypes.size() == 0) {
//			optionButtons[StandardSymbolOptions.COLOUR_ANNOTATION].setVisible(false);
//		}
		symbolModifierPanel = new SymbolModifierPanel(standardSymbolChooser);

		JPanel symbolPanel = new JPanel(new GridBagLayout());
		symbolPanel.setBorder(new TitledBorder("Base Symbol"));
		GridBagConstraints c = new PamGridBagContraints();
		symbolPanel.add(genericBox = new JCheckBox("Same on all displays"), c);
		genericBox.setToolTipText("Use the same symbol selection scheme on all displays");
		genericBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				genericBoxClick();
			}
		});
		c.gridy++;
		symbolButton = new JButton("Select symbol ...");
		symbolPanel.add(symbolButton, c);
		symbolButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				choseSymbol();
			}
		});
//		c.gridwidth = 2;
//		for (int i = 0; i < StandardSymbolManager.NUM_STANDARD_CHOICES; i++) {
//			int iB = optionButtonOrder[i];
//			if (optionButtons[iB].isVisible()) {
//				c.gridy++;
//				symbolPanel.add(optionButtons[iB], c);
//			}
//		}
		mainPanel.add(symbolPanel);
		if (standardSymbolChooser.getSymbolModifiers().size() > 0) {
			mainPanel.add(symbolModifierPanel.getDialogComponent());
		}

//		if (annotationTypes.size() > 0) {
//			JPanel anPanel = new PamAlignmentPanel(BorderLayout.WEST);
//			anPanel.setLayout(new GridBagLayout());
//			anPanel.setBorder(new TitledBorder("Modify by annotation"));
//			c = new PamGridBagContraints();
//			c.gridwidth = 3;
//			anPanel.add(useAnnotation = new JCheckBox("Use annotation data"), c);
//			c.gridy++;
//			c.gridwidth = 1;
//			anPanel.add(new JLabel(" Annotation ", JLabel.RIGHT), c);
//			c.gridx++;
//			annotationList = new JComboBox<DataAnnotationType<?>>();
//			anPanel.add(annotationList, c);
//			anSetButton = new SettingsButton();
//			anSetButton.setToolTipText("Change annotation options");
//			c.gridx++;
//			anPanel.add(anSetButton, c);
//			mainPanel.add(anPanel);
//			useAnnotation.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent arg0) {
//					enableControls();
//				}
//			});
//			anSetButton.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent arg0) {
//					annotationSettings();
//				}
//			});
//		}

		/*
		 *  if need line length information .... but only if the line options are included
		 *  and if the projector has something vaguely representing lat or long. 
		 */

		if (standardSymbolManager.hasSymbolOption(StandardSymbolManager.HAS_LINE) && 
				showLineLengthOption(standardSymbolChooser.getProjector())) {
			lineLengthOption = true;
			JPanel linePanel = new JPanel(new GridBagLayout());
			linePanel.setBorder(new TitledBorder("Bearing lines"));
			c = new PamGridBagContraints();
			linePanel.add(new JLabel("Bearing line length ", JLabel.RIGHT), c);
			c.gridx++;
			linePanel.add(lineLength = new JTextField(6));
			c.gridx++;
			linePanel.add(new JLabel(" m ", JLabel.LEFT), c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 3;
			linePanel.add(showLinesToLatLongs = new JCheckBox("Draw lines to detections with Lat Long"), c);
			lineLength.setToolTipText("Standard length of lines to detections that have bearing but no range information");
			showLinesToLatLongs.setToolTipText("<html>Show lines to detections that have range or lat long information.<br>" + 
					"Hiding the lines can make the map less cluttered.</html>");
			mainPanel.add(linePanel);
		}
		setParams();
	}

	public boolean showLineLengthOption(GeneralProjector projector) {
		if (projector == null) return false;
		ParameterType[] paramTypes = projector.getParameterTypes();
		if (paramTypes == null) return false;
		if (paramTypes.length < 0) return false;
		return paramTypes[0] == ParameterType.LATITUDE;
	}

//	/**
//	 * Get a list of annotation types that have a symbol chooser. 
//	 * @return list of annotation types with a symbol chooser 
//	 */
//	private ArrayList<DataAnnotationType<?>> getColouringAnnotationTypes() {
//		ArrayList<DataAnnotationType<?>> annotationList = new ArrayList<DataAnnotationType<?>>();
//		AnnotationHandler annotationHandler = dataBlock.getAnnotationHandler();
//		if (annotationHandler != null) {
//			List<DataAnnotationType<?>> anTypes = annotationHandler.getAvailableAnnotationTypes();
//			for (DataAnnotationType<?> anType : anTypes) {
//				if (anType.getSymbolChooser() != null) {
//					annotationList.add(anType);
//				}
//			}
//		}
//
//		return annotationList;
//	}

	protected void genericBoxClick() {
		fillParams(genericBox.isSelected());
	}

	protected void choseSymbol() {
		PamSymbol newSymbol = PamSymbolDialog.show(null, currentSymbol.clone());
		if (newSymbol != null) {
			setCurrentSymbol(newSymbol);
		}
	}

	private void setCurrentSymbol(PamSymbol newSymbol) {
		symbolButton.setIcon(currentSymbol = newSymbol);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		ManagedSymbolData msd = standardSymbolManager.getManagedSymbolData();
		genericBox.setSelected(msd.useGeneric);

		fillParams(msd.useGeneric);
		
		if (symbolModifierPanel != null) {
			symbolModifierPanel.setParams();
		}

		enableControls();
	}

	private void fillParams(boolean useGeneric) {

		String name = standardSymbolChooser.getDisplayName();
		if (genericBox.isSelected()) {
			name = PamSymbolManager.GENERICNAME;
		}

		StandardSymbolChooser chooser = getWhichChooser();
		StandardSymbolOptions params = chooser.getSymbolOptions();

		for (int i = 0; i < optionButtons.length; i++) {
			optionButtons[i].setSelected(params.colourChoice == i);
		}
		setCurrentSymbol(new PamSymbol(params.symbolData));
		if (lineLengthOption) {
			lineLength.setText(String.format("%3.1f", params.mapLineLength));
			showLinesToLatLongs.setSelected(!params.hideLinesWithLatLong);
		}

//		if (useAnnotation != null) {
//			useAnnotation.setSelected(params.useAnnotation);
//			ArrayList<DataAnnotationType<?>> annotationTypes = getColouringAnnotationTypes();
//			DataAnnotationType<?> selAnnotation = null;
//			annotationList.removeAllItems();
//			for (DataAnnotationType<?> aT : annotationTypes) {
//				annotationList.addItem(aT);
//				if (aT.getClass().getName().equals(params.annotationChoice)) {
//					selAnnotation = aT; 
//				}
//			}
//			if (selAnnotation != null) {
//				annotationList.setSelectedItem(selAnnotation);
//			}
//		}
	}

	/**
	 * Select based on whether or not the generic button is presses. 
	 * @return
	 */
	private StandardSymbolChooser getWhichChooser() {
		if (genericBox.isSelected()) {
			PamSymbolChooser chooser = standardSymbolManager.getSymbolChooser(PamSymbolManager.GENERICNAME, standardSymbolChooser.getProjector());
			if (StandardSymbolChooser.class.isAssignableFrom(chooser.getClass())) {
				return (StandardSymbolChooser) chooser;
			}
		}
		return standardSymbolChooser;
	}


	@Override
	public boolean getParams() {
		ManagedSymbolData msd = standardSymbolManager.getManagedSymbolData();
		msd.useGeneric = genericBox.isSelected();

		StandardSymbolChooser chooser = getWhichChooser();
		StandardSymbolOptions params = chooser.getSymbolOptions();


		boolean ok = true;
//		for (int i = 0; i < optionButtons.length; i++) {
//			if (optionButtons[i].isSelected()) {
//				params.colourChoice = i;
//				ok = true;
//			}
//		}
//		if (!ok) {
//			return PamDialog.showWarning(null, "Error", "No selected Colour type");
//		}
		if (lineLengthOption) {
			try {
				params.mapLineLength = Double.valueOf(lineLength.getText());
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(null, "Error", "Invalid line length value: " + lineLength.getText());
			}
			params.hideLinesWithLatLong = !showLinesToLatLongs.isSelected();
		}

//		if (useAnnotation != null) {
//			params.useAnnotation = useAnnotation.isSelected();
//			DataAnnotationType<?> selAnnotation = (DataAnnotationType<?>) annotationList.getSelectedItem();
//			if (selAnnotation != null) {
//				params.annotationChoice = selAnnotation.getClass().getName();
//			}
//			if (params.useAnnotation && params.annotationChoice == null) {
//				return PamDialog.showWarning(null, "Warning", "No selected annotation");
//			}
//		}

		if (currentSymbol != null) {
			params.symbolData = currentSymbol.getSymbolData();
		}
		else {
			return PamDialog.showWarning(null, "Error", "No selected symbol");
		}
		
		if (symbolModifierPanel != null) {
			ok &= symbolModifierPanel.getParams();
		}
		
		return ok;
	}

	private void enableControls() {
//		if (useAnnotation != null) {
//			annotationList.setEnabled(useAnnotation.isSelected());
//			anSetButton.setEnabled(useAnnotation.isSelected());
//		}
	}

	protected void annotationSettings() {
		StandardSymbolChooser chooser = getWhichChooser();
		StandardSymbolOptions params = chooser.getSymbolOptions();
		AnnotationSymboloptsPanel optsPanel = new AnnotationSymboloptsPanel(params.getAnnotationSymbolOptions());
		GenericSwingDialog.showDialog(null, "Annotation options", optsPanel);
	}

	/**
	 * Get the main panel which has a box layout. 
	 * @return
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * 
	 * @return the symbol chooser
	 */
	public StandardSymbolChooser getStandardSymbolChooser() {
		return standardSymbolChooser;
	}

}
