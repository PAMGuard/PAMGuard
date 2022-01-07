package annotation.calcs.wav;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamUtils.PamCalendar;
import PamUtils.SelectFolder;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialogPanel;

public class WavAnnotationPanel implements AnnotationDialogPanel {

	private WavAnnotationType wavAnnotationType;
	
	private PamLabel wavLabel;
	
	private SelectFolder selectFolder;
	
	private PamPanel wavAnnotationPanel;

	public WavAnnotationPanel(WavAnnotationType wavAnnotationType) {
		super();
		this.wavAnnotationType = wavAnnotationType;
		wavAnnotationPanel = new PamPanel();

		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.gridx = 0;
		c.gridy = 0;
		wavAnnotationPanel.setLayout(gb);
		PamLabel wavLabel = new PamLabel("Placeholder for Wav Export Options", JLabel.CENTER);		
		wavAnnotationPanel.add(wavLabel,c);
//		c.gridy++;
//		selectFolder = new SelectFolder("Select output folder", 20, false);
//		wavAnnotationPanel.add(selectFolder.getFolderPanel());
//		c.gridy++;
	}
	
	@Override
	public JComponent getDialogComponent() {
		return wavAnnotationPanel;

	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
//		WavAnnotation an = (WavAnnotation) pamDataUnit.findDataAnnotation(WavAnnotationType.class);
//		if (an == null){
//			an = new WavAnnotation(wavAnnotationType);
//		}
//		String fileName = PamCalendar.createFileNameMillis(pamDataUnit.getTimeMilliseconds(),
//				an.getWavFolderName(),  an.getWavPrefix(), ".wav");
//			selectFolder.setFolderName(fileName);
	}
	

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		return true;
	}

	
}
