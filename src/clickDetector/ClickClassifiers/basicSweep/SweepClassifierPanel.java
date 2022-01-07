package clickDetector.ClickClassifiers.basicSweep;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import PamModel.SMRUEnable;
import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.UserTypesPanel;

public class SweepClassifierPanel extends UserTypesPanel {

	/**
	 * Reference to the sweep classifier 
	 */
	private SweepClassifier sweepClassifier;
	
	/**
	 * Create check box to set whether annotation data is saved. 
	 */
	private JCheckBox classiferSetBox; 
	
	 public SweepClassifierPanel(SweepClassifier sweepClassifier, Window pWindow, ClickControl clickControl) {
		super(pWindow, clickControl);
		this.sweepClassifier = sweepClassifier;
		//create check box to save annotation data. 
		classiferSetBox= new JCheckBox("Save classifier set"); 
		if (SMRUEnable.isEnable()) {
			((JPanel) getComponent()).add(BorderLayout.SOUTH, classiferSetBox);
		}
	}
	

	@Override
	public void addButton() {
		SweepClassifierSet newSet = new SweepClassifierSet();
		newSet.setSpeciesCode(sweepClassifier.getNextFreeCode(0));
//		newSet = SweepClassifierDialog.showDialog(getPWindow(), sweepClassifier, newSet);
		newSet = SweepClassifierDialog.showDialog(getPWindow(),
                clickControl,
                sweepClassifier,
                newSet);
		if (newSet != null) {
			sweepClassifier.sweepClassifierParameters.addSet(newSet);
		}
		fireTableDataChanged();
	}


	@Override
	public void deleteButton() {
		int row = getSelectedRow();
		if (row >= 0) {
			sweepClassifier.sweepClassifierParameters.remove(row);
			fireTableDataChanged();
		}
	}


	@Override
	public void downButton() {
		int row = getSelectedRow();
		int nRows = getNumSpecies();
		if (row < nRows-1) {
			SweepClassifierSet ss = sweepClassifier.sweepClassifierParameters.getSet(row);
			sweepClassifier.sweepClassifierParameters.remove(row);
			sweepClassifier.sweepClassifierParameters.addSet(row+1, ss);
			fireTableDataChanged();
			setSelectedRow(row+1);
		}
	}

	@Override
	public void upButton() {
		int row = getSelectedRow();
		int nRows = getNumSpecies();
		if (row >= 1) {
			SweepClassifierSet ss = sweepClassifier.sweepClassifierParameters.getSet(row);
			sweepClassifier.sweepClassifierParameters.remove(row);
			sweepClassifier.sweepClassifierParameters.addSet(row-1, ss);
			fireTableDataChanged();
			setSelectedRow(row-1);
		}
		
	}

	@Override
	public void editButton() {
		int row = getSelectedRow();
		if (row < 0) {
			return;
		}
		SweepClassifierSet oldSet = sweepClassifier.sweepClassifierParameters.getSet(row); 
//		SweepClassifierSet newSet = SweepClassifierDialog.showDialog(getPWindow(), sweepClassifier, oldSet);
		SweepClassifierSet newSet = SweepClassifierDialog.showDialog(getPWindow(),
                clickControl,
                sweepClassifier,
                oldSet);
		if (newSet != null) {
			sweepClassifier.sweepClassifierParameters.remove(row);
			sweepClassifier.sweepClassifierParameters.addSet(row, newSet);
		}
		fireTableDataChanged();
	}


	@Override
	public int getNumSpecies() {
		/*
		 * Need this since it will get called from the super constructor before
		 * sweepclassifier has been correctly set in this constructor
		 */
		if (sweepClassifier == null) {
			return 0;
		}
		return sweepClassifier.sweepClassifierParameters.getNumSets();
	}


	@Override
	public String getSpeciesCode(int species) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		return String.format("%d", sweepSet.getSpeciesCode());
	}


	@Override
	public Boolean getSpeciesDiscard(int species) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		return sweepSet.getDiscard();
	}

	
	@Override
	public Boolean getAlarm(int species) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		return sweepSet.getAlarmEnabled();
	}

	@Override
	public void setAlarm(int species, Boolean alarmEnabled) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		sweepSet.setAlarmEnabled(alarmEnabled);
	}


	public boolean getSpeciesCanProcess(int iSpecies, double sampleRate) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(iSpecies);
		return sweepSet.canProcess(sampleRate, false);
	}

	@Override
	public Boolean getSpeciesEnable(int species) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		if (sweepSet == null) {
			return null;
		}
		return sweepSet.enable;
	}


	@Override
	public void setSpeciesEnable(int species, Boolean enable) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		sweepSet.enable = enable;		
	}


	@Override
	public String getSpeciesName(int species) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		return sweepSet.getName();
	}


	@Override
	public Icon getSymbol(int species) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		return sweepSet.symbol;
	}


	@Override
	public void setSpeciesDiscard(int species, Boolean discard) {
		SweepClassifierSet sweepSet = sweepClassifier.sweepClassifierParameters.getSet(species);
		sweepSet.setDiscard(discard);
	}


	@Override
	public String getHelpPoint() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean getParams() {
		sweepClassifier.sweepClassifierParameters.checkAllClassifiers=classiferSetBox.isSelected(); 
		return true;
//		boolean ok = getNumSpecies() > 0;
//		return ok;
	}

	@Override
	public void setParams() {
		classiferSetBox.setSelected(sweepClassifier.sweepClassifierParameters.checkAllClassifiers);
		fireTableDataChanged();
	}


	@Override
	public void setActive(boolean b) {
		fireTableDataChangedLater();	
	}

}
