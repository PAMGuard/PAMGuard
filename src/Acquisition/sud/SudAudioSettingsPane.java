package Acquisition.sud;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Acquisition.pamAudio.PamAudioSettingsPane;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;

/**
 * Some Swing and FX controls to allow a user to change sud parameters. 
 */
public class SudAudioSettingsPane implements PamAudioSettingsPane {

	private SudAudioFile sudAudioFile;

	private SudSettingsPanel sudAudioPanel;
	
	private SudSettingsPane sudAudioPaneFX;
	
	private String sudTooltip = "Zero pad sud files. Zero padding replaces sections of sud files \n"
			+ "with corrupt or no data with zeros. This can improve time drift. ";


	public SudAudioSettingsPane(SudAudioFile sudAudioFile) {
		this.sudAudioFile=sudAudioFile; 
	}

	@Override
	public Pane getAudioLoaderPane() {
		if (sudAudioPaneFX==null) {
			createSudAudioPaneFX(); 
		}
		return sudAudioPaneFX;
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
	
	private void createSudAudioPaneFX() {
		sudAudioPaneFX = new SudSettingsPane(); 
	}

	public void setParams(PamSudParams sudParams) {
		//System.out.println("Set SUD PARAMS: " + sudParams + "  " + sudParams.zeroPad); 
		if (sudAudioPanel!=null) sudAudioPanel.setParams(sudParams);; 
		if (sudAudioPaneFX!=null) sudAudioPaneFX.setParams(sudParams);; 

	}
	
	public PamSudParams getParams(PamSudParams sudParams) {
		//System.out.println("Get SUD PARAMS: " + sudParams + "  " + sudParams.zeroPad); 

		if (sudAudioPanel!=null)  return sudAudioPanel.getParams(sudParams);
		if (sudAudioPaneFX!=null)  return sudAudioPaneFX.getParams(sudParams);; 

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
			zeroPadSud.setToolTipText(sudTooltip);
						
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
	
	
	/**
	 * The sud settings panel for JavaFX
	 */
	public class SudSettingsPane extends PamHBox {
				
		private PamToggleSwitch zeroPadSud; 

		public SudSettingsPane() {
			
//			soundTrapDate.setPreferredSize(tzPanel.getPreferredSize());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridy = 0;
			c.gridx = 0;
			c.gridwidth = 1;
			
			zeroPadSud = new PamToggleSwitch("Zero pad sud files"); 
			zeroPadSud.setTooltip(new Tooltip(sudTooltip));
						
			this.getChildren().add(zeroPadSud);
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
