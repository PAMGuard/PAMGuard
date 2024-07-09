package Acquisition.sud;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Acquisition.pamAudio.PamAudioSettingsPane;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import javafx.scene.layout.Pane;

/**
 * Some Swing and FX controls to allow a user to chnage sud parameters. 
 */
public class SudAudioSettingsPane implements PamAudioSettingsPane {

	private SudAudioFile sudAudioFile;

	private SudSettingsPanel sudAudioPanel;

	public SudAudioSettingsPane(SudAudioFile sudAudioFile) {
		this.sudAudioFile=sudAudioFile; 
	}

	@Override
	public Pane getAudioLoaderPane() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamPanel getAudioLoaderPanel() {
		if (sudAudioPanel==null) {
			createSudAudioPanel(); 
		}
		return sudAudioPanel;
	}

	private void createSudAudioPanel() {
		sudAudioPanel = new SudSettingsPanel(); 
	}

	public void setParams(PamSudParams sudParams) {
		//System.out.println("Set SUD PARAMS: " + sudParams + "  " + sudParams.zeroPad); 
		if (sudAudioPanel!=null) sudAudioPanel.setParams(sudParams);; 

	}
	
	public PamSudParams getParams(PamSudParams sudParams) {
		//System.out.println("Get SUD PARAMS: " + sudParams + "  " + sudParams.zeroPad); 

		if (sudAudioPanel!=null)  return sudAudioPanel.getParams(sudParams);; 
		return null;
	}


	/**
	 * The sud settings panel. 
	 */
	public class SudSettingsPanel extends PamPanel {
		
		private static final long serialVersionUID = 1L;
		
		private JCheckBox zeroPadSud; 

		public SudSettingsPanel() {
			
			this.setLayout(new GridBagLayout());
//			soundTrapDate.setPreferredSize(tzPanel.getPreferredSize());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridy = 0;
			c.gridx = 0;
			c.gridwidth = 1;
			
			zeroPadSud = new JCheckBox("Zero pad sud files"); 
			zeroPadSud.setToolTipText("Zero pad sud files. Zero padding replaces sections of sud files \n"
									+ "with corrupt or no data with zeros. This can improve time drift. ");
						
			this.add(zeroPadSud,c);
		}
		
		public void setParams(PamSudParams sudParams) {
			this.zeroPadSud.setSelected(sudParams.zeroPad);
		}
		
		
		public PamSudParams getParams(PamSudParams sudParams) {
			sudParams.zeroPad = zeroPadSud.isSelected();
			return sudParams;
		}

	}


	@Override
	public void getParams() {
		getParams(sudAudioFile.getSudParams());
	}

	@Override
	public void setParams() {
		setParams(sudAudioFile.getSudParams());
	}

}
