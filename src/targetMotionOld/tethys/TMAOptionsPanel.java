package targetMotionOld.tethys;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import targetMotionOld.TargetMotionLocaliser;
import targetMotionOld.TargetMotionOptions;
import tethys.localization.CoordinateName;
import tethys.swing.export.LocalizationOptionsPanel;

public class TMAOptionsPanel implements LocalizationOptionsPanel {
	
	private TargetMotionLocaliser targetMotionLocaliser;
	private JPanel mainPanel;
	
	private JRadioButton wgs84, xyz, cylindrical;

	public TMAOptionsPanel(Window parent, TargetMotionLocaliser targetMotionLocaliser) {
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("TMA options"));
		GridBagConstraints c = new PamGridBagContraints();
		wgs84 = new JRadioButton("WGS84");
		xyz = new JRadioButton("Local cartesian frame");
		cylindrical = new JRadioButton("Local cylindrical coordinate");
		ButtonGroup bg = new ButtonGroup();
		bg.add(wgs84);
		bg.add(xyz);
		bg.add(cylindrical);
		
		
		mainPanel.add(wgs84, c);
		c.gridy++;
		mainPanel.add(xyz, c);
		c.gridy++;
		mainPanel.add(cylindrical, c);
		c.gridy++;
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		TargetMotionOptions tmaOptions = targetMotionLocaliser.getTargetMotionOptions();
		wgs84.setSelected(tmaOptions.exportCoordinate == CoordinateName.WGS84);
		xyz.setSelected(tmaOptions.exportCoordinate == CoordinateName.Cartesian);
		cylindrical.setSelected(tmaOptions.exportCoordinate == CoordinateName.Cylindrical);

	}

	@Override
	public boolean getParams() {
		TargetMotionOptions tmaOptions = targetMotionLocaliser.getTargetMotionOptions();
		if (wgs84.isSelected()) {
			tmaOptions.exportCoordinate = CoordinateName.WGS84;
		}
		if (xyz.isSelected()) {
			tmaOptions.exportCoordinate = CoordinateName.Cartesian;
		}
		if (cylindrical.isSelected()) {
			tmaOptions.exportCoordinate = CoordinateName.Cylindrical;
		}
		return tmaOptions.exportCoordinate != null;
	}

	@Override
	public boolean isBig() {
		return false;
	}

}
