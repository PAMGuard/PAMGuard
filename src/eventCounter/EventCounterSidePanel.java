package eventCounter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

public class EventCounterSidePanel implements PamSidePanel {

	private DataCounter dataCounter;
	
	private JPanel panel;
	
	private TitledBorder border;

	private String shortName;
	
	private JLabel title, dataLabel, eventLabel;
	private JTextField dataCount, eventCount;
	
	public EventCounterSidePanel(DataCounter dataCounter) {
		super();
		this.dataCounter = dataCounter;
		panel = new PamPanel();
		panel.setBorder(border = new TitledBorder(dataCounter.getName()));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		PamDialog.addComponent(panel, title = new PamLabel("Counts in "), c);
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, dataLabel = new PamLabel("Single " + dataCounter.getShortName() + "s"), c);
		c.gridx++;
		PamDialog.addComponent(panel, dataCount = new JTextField(5), c);
		dataCount.setEditable(false);
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(panel, eventLabel = new PamLabel(dataCounter.getShortName() + " events"), c);
		c.gridx++;
		PamDialog.addComponent(panel, eventCount = new JTextField(5), c);
		eventCount.setEditable(false);
		
		setCountSeconds();
		
	}

	@Override
	public JComponent getPanel() {
		return panel;
	}

	@Override
	public void rename(String newName) {
		border.setTitle(newName);
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
		setLabelNames();
	}
	
	public void setCountSeconds() {
		int countSecs = dataCounter.getDataCountSeconds();
		if (countSecs % 60 == 0) {
			title.setText(String.format("Counts in %d minutes", countSecs/60));
		}
		else {
			title.setText(String.format("Counts in %d seconds", countSecs));
		}
	}
	
	private void setLabelNames() {
		shortName = dataCounter.getShortName();
		dataLabel.setText("Single " + dataCounter.getShortName() + "s");
		eventLabel.setText(dataCounter.getShortName() + " events");
	}
	
	public void updateCounts(int nData, int nEvents) {
		dataCount.setText(String.format("%d", nData));
		eventCount.setText(String.format("%d", nEvents));
		
	}

}
