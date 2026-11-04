package annotation.handler;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

import PamView.component.PamFontIcon;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import annotation.AnnotationSettingsDialog;
import annotation.DataAnnotationType;

/**
 * Panel for choosing which annotation types to use. For handlers with an editable list
 * (see {@link AnnotationChoiceHandler#isEditableList()}) only the listed types are shown
 * and a + button opens {@link AddAnnotationTypesDialog} to add or remove types.
 */
public class AnnotationsSelectionPanel implements PamDialogPanel {

	private AnnotationChoiceHandler annotationHandler;

	private JPanel mainPanel;

	private JPanel listPanel;

	/**
	 * The annotation types currently shown in the list.
	 */
	private ArrayList<DataAnnotationType<?>> shownTypes = new ArrayList<>();

	private HashMap<DataAnnotationType<?>, JCheckBox> annotationSelection = new HashMap<>();

	private HashMap<DataAnnotationType<?>, JButton> settingsButtons = new HashMap<>();

	/**
	 * @param annotationHandler
	 */
	public AnnotationsSelectionPanel(AnnotationChoiceHandler annotationHandler) {
		super();
		this.annotationHandler = annotationHandler;
		mainPanel = new JPanel(new BorderLayout());
		listPanel = new JPanel(new GridBagLayout());
		mainPanel.add(BorderLayout.CENTER, listPanel);
		shownTypes = new ArrayList<>(annotationHandler.getListedAnnotationTypes());
		buildRows(new HashSet<>());
	}

	/**
	 * Lay out a row (check box and optional settings button) for each shown type.
	 * @param selectedNames names of the types to tick.
	 */
	private void buildRows(Set<String> selectedNames) {
		listPanel.removeAll();
		annotationSelection.clear();
		settingsButtons.clear();
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		listPanel.add(new JLabel("Annotation Types"), c);
		ButtonGroup buttonGroup = new ButtonGroup();
		EnableControls enableControls = new EnableControls();
		for (DataAnnotationType<?> anType : shownTypes) {
			JCheckBox cb = new JCheckBox(anType.getAnnotationName());
			cb.setToolTipText(anType.getAnnotationDescription());
			cb.setSelected(selectedNames.contains(anType.getAnnotationName()));
			cb.addActionListener(enableControls);
			annotationSelection.put(anType, cb);
			c.gridy++;
			c.gridx = 0;
			listPanel.add(cb, c);
			if (annotationHandler.getAnnotationChoices().isAllowMany() == false) {
				buttonGroup.add(cb);
			}
			c.gridx++;
			if (anType.hasSettingsPanel()) {
				JButton settingsButton = new PamSettingsIconButton();
				listPanel.add(settingsButton, c);
				settingsButton.addActionListener(new AnnotationSettings(anType));
				settingsButton.setToolTipText("More options for " + anType.getAnnotationName());
				settingsButtons.put(anType, settingsButton);
			}
		}
		if (annotationHandler.isEditableList()) {
			addAddRow(c);
		}
		enablControls();
		listPanel.revalidate();
		listPanel.repaint();
	}

	/**
	 * Add the last row: an "Add annotation" label lined up with the text of the check
	 * boxes, and a + button in the column of the settings buttons.
	 */
	private void addAddRow(GridBagConstraints c) {
		String tip = "Add annotation types, e.g. sound measurements, to the list";
		JCheckBox sample = new JCheckBox();
		Icon cbIcon = UIManager.getIcon("CheckBox.icon");
		int indent = sample.getInsets().left + sample.getIconTextGap() + (cbIcon == null ? 0 : cbIcon.getIconWidth());
		JLabel addLabel = new JLabel("Add annotation");
		addLabel.setBorder(BorderFactory.createEmptyBorder(0, indent, 0, 0));
		addLabel.setToolTipText(tip);
		c.gridy++;
		c.gridx = 0;
		listPanel.add(addLabel, c);
		JButton addButton = new JButton(PamFontIcon.of(MaterialDesignP.PLUS, PamSettingsIconButton.NORMAL_SIZE));
		addButton.setToolTipText(tip);
		addButton.addActionListener((e) -> addButtonPressed());
		c.gridx++;
		listPanel.add(addButton, c);
	}

	/**
	 * Open the dialog of all available types and update the list from it. Types newly
	 * added to the list are ticked, ready to use.
	 */
	private void addButtonPressed() {
		Set<String> selected = getTickedNames();
		List<DataAnnotationType<?>> newList = AddAnnotationTypesDialog.showDialog(getWindow(), annotationHandler, shownTypes);
		if (newList == null) {
			return;
		}
		for (DataAnnotationType<?> anType : newList) {
			if (!shownTypes.contains(anType)) {
				selected.add(anType.getAnnotationName());
			}
		}
		shownTypes = new ArrayList<>(newList);
		buildRows(selected);
		Window win = getWindow();
		if (win != null) {
			win.pack();
		}
	}

	private Set<String> getTickedNames() {
		HashSet<String> names = new HashSet<>();
		for (DataAnnotationType<?> anType : shownTypes) {
			JCheckBox cb = annotationSelection.get(anType);
			if (cb != null && cb.isSelected()) {
				names.add(anType.getAnnotationName());
			}
		}
		return names;
	}

	private Window getWindow() {
		return SwingUtilities.getWindowAncestor(mainPanel);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		AnnotationChoices annotationChoices = annotationHandler.getAnnotationChoices();
		shownTypes = new ArrayList<>(annotationHandler.getListedAnnotationTypes());
		HashSet<String> selected = new HashSet<>();
		for (DataAnnotationType<?> anType : shownTypes) {
			AnnotationOptions typeConfig = annotationChoices.getAnnotationOptions(anType.getAnnotationName());
			if (typeConfig != null && typeConfig.isIsSelected()) {
				selected.add(anType.getAnnotationName());
			}
		}
		buildRows(selected);
	}

	@Override
	public boolean getParams() {
		AnnotationChoices annotationChoices = annotationHandler.getAnnotationChoices();
		List<DataAnnotationType<?>> typeList = annotationHandler.getAvailableAnnotationTypes();
		for (DataAnnotationType<?> anType : typeList) {
			AnnotationOptions anOptions = anType.getAnnotationOptions();
			JCheckBox cb = annotationSelection.get(anType);
			annotationChoices.setAnnotionOption(anType.getAnnotationName(), anOptions, cb != null && cb.isSelected());
		}
		if (annotationHandler.isEditableList()) {
			ArrayList<String> listedNames = new ArrayList<>();
			for (DataAnnotationType<?> anType : shownTypes) {
				listedNames.add(anType.getAnnotationName());
			}
			annotationChoices.setListedAnnotations(listedNames);
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
		for (DataAnnotationType<?> anType : settingsButtons.keySet()) {
			JCheckBox cb = annotationSelection.get(anType);
			settingsButtons.get(anType).setEnabled(cb != null && cb.isSelected());
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
			AnnotationSettingsDialog.showDialog(getWindow(), annotationType);
		}

	}

}
