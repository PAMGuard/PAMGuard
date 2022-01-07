package clickDetector.ClickClassifiers.basic;

import java.awt.Frame;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import clickDetector.BasicClickIdParameters;
import clickDetector.ClickControl;
import clickDetector.ClickTypeParams;
import clickDetector.ClickClassifiers.UserTypesPanel;

public class BasicIdentifierPanel extends UserTypesPanel {

	private BasicClickIdentifier basicClickIdentifier;
	
	private BasicClickIdParameters basicClickIdParameters;
	
	private Frame windowFrame;

	private ClickControl clickControl;
	
	public BasicIdentifierPanel(BasicClickIdentifier basicClickIdentifier, 
			Frame windowFrame, ClickControl clickControl) {
		super(windowFrame, clickControl);
		this.basicClickIdentifier = basicClickIdentifier;
		this.windowFrame = windowFrame;
		this.clickControl = clickControl;
	}
	
	@Override
	public void addButton() {
		ClickTypeParams p = new ClickTypeParams(basicClickIdParameters
				.getFirstFreeClickIdentifier());
//		if ((p = ClickTypeDialog.showDialog(windowFrame, basicClickIdParameters, p)) != null) {
		if ((p = ClickTypeDialog.showDialog(windowFrame, clickControl, basicClickIdParameters, p)) != null) {
			basicClickIdParameters.clickTypeParams.add(p);
			fireTableDataChanged();
		}
	}

	@Override
	public void deleteButton() {
		int r = getSelectedRow();
		if (r >= 0) {
			basicClickIdParameters.clickTypeParams.remove(r);
			fireTableDataChanged();
		}
	}

	@Override
	public void editButton() {
		int r = getSelectedRow();
		if (r >= 0) {
			ClickTypeParams p;
//			if ((p = ClickTypeDialog.showDialog(windowFrame, basicClickIdParameters,
			if ((p = ClickTypeDialog.showDialog(windowFrame, clickControl, basicClickIdParameters,
					basicClickIdParameters.clickTypeParams.get(r))) != null) {
				basicClickIdParameters.clickTypeParams.remove(r);
				basicClickIdParameters.clickTypeParams.add(r, p);
				fireTableDataChanged();
			}
		}
	}

	@Override
	public int getNumSpecies() {
		if (basicClickIdParameters == null ||
				basicClickIdParameters.clickTypeParams == null) {
			return 0;
		}
		return basicClickIdParameters.clickTypeParams.size();
	}

	@Override
	public String getSpeciesCode(int species) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return null;
		}
		return String.format("%d", basicClickIdParameters.clickTypeParams.get(species).getSpeciesCode());
	}

	@Override
	public String getSpeciesName(int species) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return null;
		}
		return basicClickIdParameters.clickTypeParams.get(species).getName();
	}

	@Override
	public Icon getSymbol(int species) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return null;
		}
		return basicClickIdParameters.clickTypeParams.get(species).symbol;
	}

	@Override
	public Boolean getSpeciesDiscard(int species) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return false;
		}
		return basicClickIdParameters.clickTypeParams.get(species).getDiscard();
	}
	

	@Override
	public void setSpeciesDiscard(int species, Boolean discard) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return ;
		}
		basicClickIdParameters.clickTypeParams.get(species).setDiscard(discard);		
	}

	@Override
	public Boolean getAlarm(int species) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return false;
		}
		return basicClickIdParameters.clickTypeParams.get(species).getAlarmEnabled();
	}


	@Override
	public void setAlarm(int species, Boolean alarmEnabled) {
		if (species < 0 || species >= basicClickIdParameters.clickTypeParams.size()) {
			return ;
		}
		basicClickIdParameters.clickTypeParams.get(species).setAlarmEnabled(alarmEnabled);
	}

	@Override
	public boolean getSpeciesCanProcess(int species, double sampleRate) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Boolean getSpeciesEnable(int species) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setSpeciesEnable(int species, Boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downButton() {
		int iRow = getSelectedRow();
		if (iRow < 0 || iRow >= basicClickIdParameters.clickTypeParams.size()-1) {
			return;
		}
		ClickTypeParams p = basicClickIdParameters.clickTypeParams.remove(iRow);
		basicClickIdParameters.clickTypeParams.add(iRow+1, p);
		fireTableDataChanged();
		setSelectedRow(iRow+1);
	}

	@Override
	public void upButton() {
		int iRow = getSelectedRow();
		if (iRow < 1 || iRow >= basicClickIdParameters.clickTypeParams.size()) {
			return;
		}
		ClickTypeParams p = basicClickIdParameters.clickTypeParams.remove(iRow);
		basicClickIdParameters.clickTypeParams.add(iRow-1, p);
		fireTableDataChanged();
		setSelectedRow(iRow-1);
	}

	@Override
	public String getHelpPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getParams() {
		basicClickIdentifier.idParameters = basicClickIdParameters;
		return true;
	}

	@Override
	public void setParams() {
		basicClickIdParameters = basicClickIdentifier.idParameters.clone();
		fireTableDataChanged();
	}

	@Override
	public void setActive(boolean b) {
		fireTableDataChangedLater();		
	}

}
