package annotation.handler;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import annotation.AnnotationSettingsDialog;
import annotation.DataAnnotationType;

public class AnnotationsSelectionPanel implements PamDialogPanel {
	
	private AnnotationChoiceHandler annotationHandler;
	
	private JPanel mainPanel;

	private JCheckBox[] annotationSelection;

	private JButton[] settingsButtons;

	private boolean allowMany;
	/**
	 * @param annotationHandler
	 */
	public AnnotationsSelectionPanel(AnnotationChoiceHandler annotationHandler) {
		super();
		this.annotationHandler = annotationHandler;
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		List<DataAnnotationType<?>> typeList = annotationHandler.getAvailableAnnotationTypes();
		annotationSelection = new JCheckBox[typeList.size()];
		settingsButtons = new JButton[typeList.size()];
		c.gridx = c.gridy = 0;
		mainPanel.add(new JLabel("Annotation Types"));
		ButtonGroup buttonGroup = new ButtonGroup();
//		buttonGroup.
		EnableControls enableControls = new EnableControls();
		for (int i = 0; i < typeList.size(); i++) {
			DataAnnotationType anType = typeList.get(i);
			annotationSelection[i] = new JCheckBox(anType.getAnnotationName());
			annotationSelection[i].addActionListener(enableControls);
			c.gridy++;
			c.gridx = 0;
			mainPanel.add(annotationSelection[i], c);
			if (annotationHandler.getAnnotationChoices().isAllowMany() == false) {
				buttonGroup.add(annotationSelection[i]);
			}
			c.gridx++;
			if (anType.hasSettingsPanel()) {
				settingsButtons[i] = new PamSettingsIconButton();
				mainPanel.add(settingsButtons[i], c);
				settingsButtons[i].addActionListener(new AnnotationSettings(anType));
				settingsButtons[i].setToolTipText("More options for " + anType.getAnnotationName());
				
			}
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		AnnotationChoices annotationChoices = annotationHandler.getAnnotationChoices();
		List<DataAnnotationType<?>> typeList = annotationHandler.getAvailableAnnotationTypes();
		allowMany = annotationHandler.getAnnotationChoices().isAllowMany();
		for (int i = 0; i < typeList.size(); i++) {
			DataAnnotationType anType = typeList.get(i);
			AnnotationOptions typeConfig = annotationChoices.getAnnotationOptions(anType.getAnnotationName());
			if (typeConfig == null) {
				annotationSelection[i].setSelected(false);
				continue;
			}
			annotationSelection[i].setSelected(typeConfig.isIsSelected());
		}
	}

	@Override
	public boolean getParams() {
		AnnotationChoices annotationChoices = annotationHandler.getAnnotationChoices();
		List<DataAnnotationType<?>> typeList = annotationHandler.getAvailableAnnotationTypes();
		for (int i = 0; i < typeList.size(); i++) {
			DataAnnotationType anType = typeList.get(i);
			AnnotationOptions anOptions = anType.getAnnotationOptions();
			annotationChoices.setAnnotionOption(anType.getAnnotationName(), anOptions, annotationSelection[i].isSelected());
		}
		return true;
	}
	
	private class EnableControls implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enablControls();
		}
	}
	
	private void enablControls() {
		for (int i = 0; i < annotationSelection.length; i++) {
			if (settingsButtons[i] != null) {
				settingsButtons[i].setEnabled(annotationSelection[i].isSelected());
			}
		}
	}
	
	private class AnnotationSettings implements ActionListener {

		private DataAnnotationType annotationType;
		/**
		 * @param annotationType
		 */
		public AnnotationSettings(DataAnnotationType annotationType) {
			super();
			this.annotationType = annotationType;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window parent = null;
			try {
				parent = (Window) mainPanel.getTopLevelAncestor();
			}
			catch (Exception er) {
				
			}
			AnnotationSettingsDialog.showDialog(parent, annotationType);
		}
		
	}

}
