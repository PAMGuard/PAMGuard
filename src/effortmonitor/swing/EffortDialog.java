package effortmonitor.swing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.DBTextArea;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamTextArea;
import effortmonitor.EffortControl;
import effortmonitor.EffortLogging;
import effortmonitor.EffortParams;

public class EffortDialog extends PamDialog {
	
	private static EffortDialog singleInstance = null;
	private EffortControl effortControl;
	private EffortParams effortParams;
	private JComboBox<String> observer;
	private JComboBox<String> oldObjectives;
	private DBTextArea objective;
	private JRadioButton outerOnly, allActions;

	private EffortDialog(Window parentFrame, EffortControl effortControl) {
		super(parentFrame, effortControl.getUnitName(), false);
		this.effortControl = effortControl;
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Observer scroller logging"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Observer name or initials"), c);
		c.gridx++;
		mainPanel.add(observer = new JComboBox<String>(), c);
		outerOnly = new JRadioButton("Log outer scroll only");
		allActions = new JRadioButton("Log all scroll actions");
		ButtonGroup bg = new ButtonGroup();
		bg.add(allActions);
		bg.add(outerOnly);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(allActions, c);
		c.gridx+=c.gridwidth;
		mainPanel.add(outerOnly, c);
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		mainPanel.add(new JLabel("Objective"), c);
		c.gridy++;
		objective = new DBTextArea(5, 50, EffortLogging.MAX_OBJECTIVE_LENGTH);
		mainPanel.add(objective.getComponent(), c);
		Dimension dim = new Dimension(40, 450);
		objective.setDimension(dim);
		c.gridy++;
		mainPanel.add(oldObjectives = new JComboBox<String>(), c);
		oldObjectives.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectObjective();
			}
		});
		
		observer.setEditable(true);
		oldObjectives.setEditable(false);
		
		setDialogComponent(mainPanel);
	}
	
	public static final EffortParams showDialog(Window frame, EffortControl effortControl) {
//		if (singleInstance == null || singleInstance.getParent() != frame || singleInstance.effortControl != effortControl) {
			singleInstance = new EffortDialog(frame, effortControl);
//		}
		singleInstance.setParams(effortControl.getEffortParams());
		singleInstance.setVisible(true);
		return singleInstance.effortParams;
	}

	private void setParams(EffortParams effortParams) {
		this.effortParams = effortParams;
		observer.removeAllItems();
		LinkedList<String> oldObs = effortParams.getRecentObservers();
		for (String obs : oldObs) {
			observer.addItem(obs);
		}
		allActions.setSelected(effortParams.outserScrollOnly == false);
		outerOnly.setSelected(effortParams.outserScrollOnly);
		oldObjectives.removeAllItems();
		oldObjectives.addItem("");
		for (String obj : effortParams.getRecentObjectives()) {
			String obString = obj;
			if (obj == null) {
				continue;
			}
			if (obString.length() > 50) {
				obString = obString.substring(0, 46) + " ...";
			}
			oldObjectives.addItem(obString);
		}
	}

	@Override
	public boolean getParams() {
		String obs = (String) observer.getSelectedItem();
		if (obs == null || obs.length() == 0) {
			return showWarning("You must give your name or initials to contine");
		}
		effortParams.outserScrollOnly = outerOnly.isSelected();
		effortParams.setObserver(obs);
		effortParams.setObjective(objective.getText());
		return true;
	}

	/**
	 * Pick selected previous objective and put in main text area. 
	 */
	protected void selectObjective() {
		int ind = oldObjectives.getSelectedIndex();
		LinkedList<String> objs = effortParams.getRecentObjectives();
		if (ind >= 1 && ind <= objs.size()) {
			objective.setText(objs.get(ind-1));
		}
		else {
			objective.setText(null);
		}
	}

	@Override
	public void cancelButtonPressed() {
		effortParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
	}


}
