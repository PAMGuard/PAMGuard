package annotation.userforms.species;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import annotation.userforms.UserFormAnnotationType;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.controlDescriptions.InputControlDescription;
import tethys.species.SpeciesManagerObserver;

public class FormsSpeciesOptionsPanel implements PamDialogPanel {

	private UserFormAnnotationType userFormAnnotationType;
	private FormsAnnotationSpeciesManager formsAnnotationSpeciesManager;
	
	private JPanel mainPanel;
	private JComboBox<String> lookups;
	private SpeciesManagerObserver speciesManagerObserver;

	public FormsSpeciesOptionsPanel(FormsAnnotationSpeciesManager formsAnnotationSpeciesManager, SpeciesManagerObserver speciesManagerObserver) {
		this.formsAnnotationSpeciesManager = formsAnnotationSpeciesManager;
		this.userFormAnnotationType = formsAnnotationSpeciesManager.getUserFormAnnotationType();
		this.speciesManagerObserver = speciesManagerObserver;
		
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("User form control for species ", JLabel.RIGHT), c);
		lookups = new JComboBox<>();
		lookups.setToolTipText("Select a drop down list that you use for species identity");
		c.gridx++;
		mainPanel.add(lookups, c);
		// fill with names of controls that are lookups. 
		lookups.addItem("--no selection--");
		ArrayList<InputControlDescription> ipControls = userFormAnnotationType.getFormDescription().getInputControlDescriptions();
		for (InputControlDescription aCtrl : ipControls) {
			if (aCtrl.getEType() != ControlTypes.LOOKUP) {
				continue;
			}
			lookups.addItem(aCtrl.getTitle());
		}
		setParams();
	}

	protected void selectionChanged() {
		getParams();
		if (speciesManagerObserver != null) {
			speciesManagerObserver.update();
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		String sel = formsAnnotationSpeciesManager.getSpeciesSettings().selectedControl;
		if (sel != null) {
			lookups.setSelectedItem(sel);
		}
		lookups.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionChanged();
			}

		});
		
	}

	@Override
	public boolean getParams() {
		String item = (String) lookups.getSelectedItem();
		formsAnnotationSpeciesManager.getSpeciesSettings().selectedControl = item;
		return (item != null);
	}

}
