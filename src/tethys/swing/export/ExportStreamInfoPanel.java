package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;

public class ExportStreamInfoPanel extends JPanel {

	private PamDataBlock dataBlock;

	public ExportStreamInfoPanel(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
		setBorder(new TitledBorder("Stream information"));
		JPanel infoPanel = new JPanel();
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.WEST, infoPanel);
		infoPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		infoPanel.add(new JLabel(dataBlock.getLongDataName(), JLabel.LEFT), c);
		c.gridy++;
		OfflineDataMap dataMap = dataBlock.getPrimaryDataMap();
		if (dataMap == null) {
			infoPanel.add(new JLabel("This stream contains no mapped data!!"), c);
			return;
		}
		else {
			String mapName = dataMap.getDataMapName();
			int nData = dataMap.getDataCount();
			int nPoints = dataMap.getNumMapPoints();
			if (nPoints == 0) {
				infoPanel.add(new JLabel("This stream contains no mapped data!!"), c);
				return;
			}
			long startT = dataMap.getMapStartTime();
			long endT = dataMap.getMapEndTime();
			String str = String.format("Data stream contains %d data items", nData);
			c.gridy++;
			infoPanel.add(new JLabel(str, JLabel.LEFT), c);
			str = String.format("Between %s and %s UTC", PamCalendar.formatDBDateTime(startT), PamCalendar.formatDBDateTime(endT));
			c.gridy++;
			infoPanel.add(new JLabel(str, JLabel.LEFT), c);
			
//			for (Enumeration keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
//			    Object key = keys.nextElement();
//			    Object value = UIManager.get(key);
////			    if (key.toLowerCase().contains("frame") )
//			    String opStr = String.format("key %s value %s", key, value);
//			    if (opStr.toLowerCase().contains("window") && opStr.toLowerCase().contains("color")) {
//			    	System.out.println(opStr);
//			    }
//			  }
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Object titleColour = UIManager.get("window");
					if (titleColour instanceof Color) {
						setBackground(ExportStreamInfoPanel.this, (Color) titleColour);
					}
					else {
						setBackground(ExportStreamInfoPanel.this, Color.WHITE);
					}
				}
			});
		}
	}

	public void setBackground(Component component, Color bg) {
		component.setBackground(bg);
		if (component instanceof JComponent) {
			JComponent jComponent = (JComponent) component;
			int nSub = jComponent.getComponentCount();
			for (int i = 0; i < nSub; i++) {
				setBackground(jComponent.getComponent(i), bg);
			}
		}
	}


}
