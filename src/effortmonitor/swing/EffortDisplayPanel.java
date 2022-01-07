package effortmonitor.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;
import effortmonitor.EffortControl;
import effortmonitor.EffortObserver;
import userDisplay.UserDisplayComponent;

public class EffortDisplayPanel implements UserDisplayComponent, EffortObserver {

	private EffortControl effortControl;
	
	private String uniqueName;
	
	private JPanel mainPanel;
	
	private EffortTableView effortTableView;
	
	private JCheckBox start_stop;
	
	public EffortDisplayPanel(EffortDisplayProvider effortDisplayProvider, EffortControl effortControl, String name) {
		this.effortControl = effortControl;
		setUniqueName(name);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder(effortControl.getUnitName()));
		effortTableView = new EffortTableView(effortControl, effortControl.getEffortDataBlock());
		mainPanel.add(BorderLayout.CENTER, effortTableView.getComponent());
		mainPanel.add(BorderLayout.NORTH, start_stop = new JCheckBox("Record Effort"));
		effortControl.addObserver(this);
		start_stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startStopAction();
			}
		});
	}

	protected void startStopAction() {
		effortControl.setOnEffort(start_stop.isSelected());
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return effortControl.getUnitName();
	}

	@Override
	public void statusChange() {
		start_stop.setSelected(effortControl.isOnEffort());
	}


}
