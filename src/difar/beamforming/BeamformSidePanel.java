package difar.beamforming;

import generalDatabase.lookupTables.LookupItem;
import pamScrollSystem.AbstractScrollManager;
import pamScrollSystem.ViewLoadObserver;
import pamScrollSystem.ViewerScrollerManager;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import difar.DifarControl;
import difar.DifarParameters;
import PamView.PamList;
import PamView.PamSidePanel;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamTextArea;
import PamView.panel.PamPanel;
import PamView.panel.PamScrollPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * The DIFAR SidePanel contains the list of DIFAR classifications, to allow
 * the user to choose which classification is assigned by default.
 * @author Brian Miller
 *
 */
public class BeamformSidePanel implements PamSidePanel {

	BeamformControl beamformControl;

	PamPanel sidePanel;
	PamList defaultClassList;
	Vector<JTextField> beamformAngle  = new Vector<JTextField>();
	
		
	public BeamformSidePanel(BeamformControl dc){
		this.beamformControl = dc;
		sidePanel = new PamPanel();
		sidePanel.setBorder(new TitledBorder("DIFAR Beamformer"));
		sidePanel.setLayout(new GridBagLayout());
		newSource();
	}
	
	void enableControls(){
		int n = beamformAngle.size();
		for (int i = 0; i < n; i++){
			beamformAngle.get(i).setEnabled(!beamformControl.getBeamformParameters().useGpsNoiseSource);
		}
		updateAngles();
	}
	
	
	class SetBeamformAngle implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			int n = beamformControl.getBeamformParameters().getNumChannels();
			Double theta[] = new Double[n]; 
			for (int i = 0; i < n; i++){
				String angle = beamformAngle.get(i).getText();
				if (angle.equals("") || angle==null)
					theta[i] = null;
				else
					theta[i] = Math.toRadians(new Double(angle));
			}
			beamformControl.getBeamformParameters().setTheta(theta);
			if (beamformControl.isViewer()){
				PamDataBlock<PamDataUnit> dataBlock = beamformControl.getBeamformProcess().getSourceDataBlock();
				long dataStart = dataBlock.getCurrentViewDataStart();
				// Kludge! Add 1 millisecond to the start time in order to force it to refresh.
//				ViewerScrollerManager.getScrollManager().startDataAt(dataBlock, PamCalendar.getViewPosition()+1, false);
				ViewerScrollerManager.getScrollManager().reLoad();
			}
		}
	}
	
	
	@Override
	public JComponent getPanel() {
		return sidePanel;
	}

	@Override
	public void rename(String newName) {
		sidePanel.repaint();
	}

	public void newSource() {
		sidePanel.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 1;
		c.gridx = 0;

		int n = beamformControl.getBeamformParameters().getNumChannels();
		beamformAngle  = new Vector<JTextField>();
		for (int i = 0; i < n; i++){
			sidePanel.add(new JLabel("Ch. " + i + " Angle:"),c);
			c.gridx++;
			beamformAngle.add(i,new JTextField(3));
			sidePanel.add(beamformAngle.get(i),c);
			beamformAngle.get(i).addActionListener(new SetBeamformAngle());
			c.gridy++;
			c.gridx = 0;
		}
		enableControls();
		getPanel().setVisible(n>0);
		this.getPanel().revalidate();
		this.getPanel().repaint();
		
	}

	public void updateAngles() {
		Double[] theta = beamformControl.getBeamformParameters().getTheta();
		for (int i = 0; i < beamformAngle.size(); i++){
			if (theta[i] != null){
				beamformAngle.get(i).setText(String.format("%2.0f", Math.toDegrees(theta[i])));
			} else {
				beamformAngle.get(i).setText("");
			}
		}
	}

}