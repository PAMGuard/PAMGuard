package loggerForms.dataselect;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.dataSelector.DataSelector;
import loggerForms.FormDescription;
import loggerForms.controlDescriptions.InputControlDescription;

public class FormDataSelPanel implements PamDialogPanel {

	private FormDataSelector formDataSelector;
	
	private JComboBox<String> controlSelector;
	
	private JPanel controlPanel;
	
	private JPanel mainPanel;

	private FormDescription formDescription;

	private ArrayList<InputControlDescription> dsInputCtrls;

	private PamDialogPanel ctrlDSPanel;

	private ControlDataSelector controlDataSelector;

	public FormDataSelPanel(FormDataSelector formDataSelector) {
		this.formDataSelector = formDataSelector;
		formDescription = formDataSelector.getFromDescription();
		mainPanel = new JPanel(new BorderLayout());
		controlSelector = new JComboBox<String>();
		mainPanel.add(controlSelector, BorderLayout.NORTH);
		mainPanel.add(controlPanel = new JPanel(new BorderLayout()), BorderLayout.CENTER);
		controlSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectControl();
			}
		});
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		FormDataSelParams params = formDataSelector.getParams();
		ArrayList<InputControlDescription> inputCtrls = formDescription.getInputControlDescriptions();
		dsInputCtrls = new ArrayList<>();
		controlSelector.removeAllItems();
		int ind = 0, selInd = -1;
		for (InputControlDescription icd : inputCtrls) {
			if (icd.getDataSelectCreator() != null) {
				dsInputCtrls.add(icd);
				if (icd.getTitle().equals(params.controlName)) {
					selInd = ind;
				}
				controlSelector.addItem(icd.getTitle());
				ind++;
			}
		}
		if (selInd >= 0) {
			controlSelector.setSelectedIndex(selInd);
		}
		selectControl();
	}

	protected void selectControl() {
		controlPanel.removeAll();
		int selInd = controlSelector.getSelectedIndex();
		if (selInd < 0|| selInd >= dsInputCtrls.size()) {
			return;
		}
		InputControlDescription inputCtrl = dsInputCtrls.get(selInd);
		ControlDataSelCreator ctrlDS = inputCtrl.getDataSelectCreator();
		if (ctrlDS == null) {
			return;
		}
		controlDataSelector = ctrlDS.getDataSelector(formDataSelector.getSelectorName(), formDataSelector.isAllowScores(), null);
		ctrlDSPanel = controlDataSelector.getDialogPanel();
		if (ctrlDSPanel != null) {
			ctrlDSPanel.setParams();
			controlPanel.add(ctrlDSPanel.getDialogComponent(), BorderLayout.CENTER);
		}
	}

	@Override
	public boolean getParams() {
		FormDataSelParams params = formDataSelector.getParams();
		int selInd = controlSelector.getSelectedIndex();
		if (selInd < 0) {
			return false;
		}
		InputControlDescription inputCtrl = dsInputCtrls.get(selInd);
		params.controlName = inputCtrl.getTitle();
		boolean ok = false;
		if (ctrlDSPanel != null) {
			ok = ctrlDSPanel.getParams();
		}
		if (ok) {
			params.controlParams = controlDataSelector.getParams();
		}
		formDataSelector.setParams(params);
		return ok;
	}

}
