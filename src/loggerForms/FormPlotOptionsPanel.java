package loggerForms;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;

/**
 * Class to provide a panel of plot options for a single Logger form. 
 * Plot options will be in the form of a single component (generally a Panel of checkboxes)
 * which control which items from within a Logger form get plotted on the map or any other 
 * projections. 
 * <p>These are accessed from the detailed plot options menu and will also become accessible
 * from a multi tab panel which will give access to options from all logger forms. 
 * 
 * @author Doug Gillespie
 *
 */
public class FormPlotOptionsPanel {

	private JPanel mainPanel;
	
	private FormDescription formDescription;

	private ArrayList<FormsPanel> formsPanels = new ArrayList<FormsPanel>();
	
	
	/**
	 * @param formDescription
	 */
	public FormPlotOptionsPanel(FormDescription formDescription) {
		super();
		this.formDescription = formDescription;
		mainPanel = new JPanel();
//		mainPanel.setBorder(new TitledBorder(formDescription.getFormName() + " Plot Options"));
//		BoxLayout boxLayout;
//		mainPanel.setLayout(boxLayout = new BoxLayout(mainPanel, BoxLayout.X_AXIS));
//		boxLayout.
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.fill = GridBagConstraints.VERTICAL;
		SinglesPanel singlesPanel = new SinglesPanel();
		int nCols = 0;
		if (singlesPanel.shouldShow) {
//			mainPanel.add(singlesPanel);
			PamDialog.addComponent(mainPanel, singlesPanel, c);
			formsPanels.add(singlesPanel);
			c.gridx++;
			nCols++;
		}
		int iControl = 0;
		for (ControlDescription aControl:formDescription.getInputControlDescriptions()) {
			iControl++;
			if (aControl.getEType() != ControlTypes.LOOKUP) {
				continue;
			}
			if (aControl.getPlot() == null || !aControl.getPlot()) {
				continue;
			}
			LookupPanel lookupPanel = new LookupPanel(iControl, aControl);
//			mainPanel.add(lookupPanel);
			if (nCols > 0) {
				PamDialog.addComponent(mainPanel, new JLabel(" OR "), c);
				c.gridx++;
			}
			PamDialog.addComponent(mainPanel, lookupPanel, c);
			formsPanels.add(lookupPanel);
			c.gridx++;
			nCols++;
		}
	}

	public void setParams() {
		for (FormsPanel aPanel:formsPanels) {
			aPanel.setParams(formDescription.getFormPlotOptions());
			aPanel.enableControls();
		}
	}
	
	public void getParams() {
		for (FormsPanel aPanel:formsPanels) {
			aPanel.getParams(formDescription.getFormPlotOptions());
		}
	}

	public Component getComponent() {
		return mainPanel;
	}
	
	private abstract class FormsPanel extends JPanel {
		int controlIndex;
		ArrayList<FormCheckBox> checkBoxes = new ArrayList<FormCheckBox>();
		FormCheckBox showAll;
		private ChangeAction changeAction;
		public FormsPanel(int controlIndex) {
			super();
			this.controlIndex = controlIndex;
			changeAction = new ChangeAction();
		}
		
		protected void add(FormCheckBox formCheckBox) {
			checkBoxes.add(formCheckBox);
			super.add(formCheckBox);
			formCheckBox.addActionListener(changeAction);
		}
		
		public void setParams(FormPlotOptions formPlotOptions) {
			FormCheckBox aBox;
			for (int i = 0; i < checkBoxes.size(); i++) {
				aBox = checkBoxes.get(i);
				aBox.setSelected(formPlotOptions.isPlotControl(aBox.controlIndex, aBox.itemIndex));
			}
		}
		
		public void getParams(FormPlotOptions formPlotOptions) {
			FormCheckBox aBox;
			for (int i = 0; i < checkBoxes.size(); i++) {
				aBox = checkBoxes.get(i);
				formPlotOptions.setPlotControl(aBox.controlIndex, aBox.itemIndex, aBox.isSelected());
			}
		}
		
		private class ChangeAction implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		}
		
		private void enableControls() {
			if (showAll == null) {
				return;
			}
			boolean s = showAll.isSelected();
			for (int i = 1; i < checkBoxes.size(); i++) {
				checkBoxes.get(i).setEnabled(!s);
			}
		}
	}
	
	private class SinglesPanel extends FormsPanel {

		private boolean shouldShow = false;
				
		public SinglesPanel() {
			super(0);
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new TitledBorder("General"));
			if (formDescription.findProperty(PropertyTypes.PLOT) != null) {
				add(showAll = new FormCheckBox(0, 0, "Show all"));
				shouldShow = true;
			}
			int iControl = 0;
			for (ControlDescription aControl:formDescription.getInputControlDescriptions()) {
				iControl++;
				if (aControl.getEType() == ControlTypes.LOOKUP) {
					continue;
				}
				if (aControl.getPlot() != null && aControl.getPlot() == true) {
					add(new FormCheckBox(iControl, 0, aControl.getTitle() + " not 0 or null"));
					shouldShow = true;
				}
			}
		}
		
	}
	
	private class LookupPanel extends FormsPanel {
		
		private ControlDescription controlDescription;
		
		private LookupComponent lookupComponent;
		

		/**
		 * @param lookup
		 */
		public LookupPanel(int controlIndex, ControlDescription controlDescription) {
			super(controlIndex);
			this.controlDescription = controlDescription;
			lookupComponent = new LookupComponent(controlDescription.getTopic(),null);
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(new TitledBorder(controlDescription.getTitle()));
			add(showAll = new FormCheckBox(controlIndex, 0, "Show all"));
			int iItem = 0;
			for (LookupItem anItem:lookupComponent.getSelectedList()) {
				iItem++;
				add(new FormCheckBox(controlIndex, iItem, anItem.getText()));
			}
		}
		
	}
	
	private class FormCheckBox extends JCheckBox {

		int controlIndex, itemIndex;
		/**
		 * @param arg0
		 */
		public FormCheckBox(int controlIndex, int itemIndex, String title) {
			super(title);
			this.controlIndex = controlIndex;
			this.itemIndex = itemIndex;
		}
		
	}
}
