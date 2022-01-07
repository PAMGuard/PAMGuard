package annotationMark;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import annotation.AnnotationDialogPanel;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamTextDisplay;
import PamguardMVC.PamDataBlock;


public class MarkAnnotationDialog extends PamDialog {

	private static MarkAnnotationDialog singleInstance;
	private MarkDataUnit annotationDataUnit;
	
	private PamTextDisplay startTime, duration, fRange;
	private ArrayList<AnnotationDialogPanel> dialogPanels = new ArrayList<>();
	private MarkModule annotationModule;
	private List<DataAnnotationType<?>> annotationTypes;
	
	private MarkAnnotationDialog(Window parentFrame, MarkModule annotationModule) {
		super(parentFrame, annotationModule.getUnitName(), false);
		this.annotationModule = annotationModule;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel acousticPanel = new JPanel(new GridBagLayout());
		mainPanel.add(acousticPanel);
		acousticPanel.setBorder(new TitledBorder("Info"));
		GridBagConstraints c = new PamGridBagContraints();
		
		acousticPanel.add(new JLabel("Start ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		acousticPanel.add(startTime = new PamTextDisplay(20), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		acousticPanel.add(new JLabel("Duration ", JLabel.RIGHT), c);
		c.gridx++;
		acousticPanel.add(duration = new PamTextDisplay(6), c);
		c.gridx++;
		acousticPanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		acousticPanel.add(new JLabel("Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		acousticPanel.add(fRange = new PamTextDisplay(20), c);
//		c.gridx = 0;
//		c.gridy++;
//		c.gridwidth = 3;
//		acousticPanel.add(new JLabel("Comment ... ", JLabel.LEFT), c);
//		c.gridy++;
//		acousticPanel.add((textArea = new DBTextArea(2, 25, SpectrogramAnnotationType.MAXTEXTLENGTH)).getComponent(), c);
		
		PamDataBlock annotationDataBlock = annotationModule.getAnnotationDataBlock();
		AnnotationHandler annotationHandler = annotationDataBlock.getAnnotationHandler();
		if (annotationHandler != null) {
			annotationTypes = annotationHandler.getUsedAnnotationTypes();
			for (int i = 0; i < annotationTypes.size(); i++) {
				DataAnnotationType anType = annotationTypes.get(i);
				AnnotationDialogPanel anPanel = anType.getDialogPanel();
				if (anPanel == null) {
					continue;
				}
				JPanel border = new JPanel(new BorderLayout());
				border.setBorder(new TitledBorder(anType.getAnnotationName()));
				border.add(BorderLayout.CENTER, anPanel.getDialogComponent());
				dialogPanels.add(anPanel);
				mainPanel.add(border);
			}
		}
//		int nTypes = annotationDataBlock.getNumDataAnnotationTypes();
//		for (int i = 0; i < nTypes; i++) {
//			DataAnnotationType anType = annotationDataBlock.getDataAnnotationType(i);
//			AnnotationDialogPanel anPanel = anType.getDialogPanel();
//			if (anPanel == null) {
//				continue;
//			}
//			JPanel border = new JPanel(new BorderLayout());
//			border.setBorder(new TitledBorder(anType.getAnnotationName()));
//			border.add(BorderLayout.CENTER, anPanel.getDialogComponent());
//			dialogPanels.add(anPanel);
//			mainPanel.add(border);
//		}
		
		setDialogComponent(mainPanel);
	}
	
	public static boolean showDialog(Window parentFrame, MarkModule markModule, MarkDataUnit annotationDataUnit, Point positionInFrame) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame || 
				singleInstance.annotationModule != markModule || 
						!singleInstance.annotationTypes.equals(getUsedAnnotationTypes())) {
			singleInstance = new MarkAnnotationDialog(parentFrame, markModule);
		}
	
		if (positionInFrame != null) {
			singleInstance.setLocation(positionInFrame);
		}
		singleInstance.annotationDataUnit = annotationDataUnit;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.annotationDataUnit != null;
	}

	private static List<DataAnnotationType<?>> getUsedAnnotationTypes() {
		PamDataBlock annotationDataBlock = singleInstance.annotationModule.getAnnotationDataBlock();
		AnnotationHandler annotationHandler = annotationDataBlock.getAnnotationHandler();

		List<DataAnnotationType<?>> annoType =null;
		if (annotationHandler != null)
			annoType = annotationDataBlock.getAnnotationHandler().getUsedAnnotationTypes();
		return annoType;
	}

	private void setParams() {
		startTime.setText(PamCalendar.formatDateTime(annotationDataUnit.getTimeMilliseconds()));
		duration.setText(new Double((double) annotationDataUnit.getDurationInMilliseconds() / 1000.).toString());
		fRange.setText(FrequencyFormat.formatFrequencyRange(annotationDataUnit.getFrequency(), true));
		for (AnnotationDialogPanel anPanel:dialogPanels) {
			anPanel.setParams(annotationDataUnit);
			if (anPanel.getDialogComponent().isFocusable()) {
				anPanel.getDialogComponent().requestFocusInWindow();
			}
		}
		
	}

	@Override
	public boolean getParams() {
//		String note = textArea.getText();
//		if (note != null) {
//			note = note.trim();
//		}
//		if (note != null && note.length() > 0) {
//			SpectrogramAnnotation sa = (SpectrogramAnnotation) annotationDataUnit.findDataAnnotation(SpectrogramAnnotation.class);
//			if (sa == null) {
//				sa = new SpectrogramAnnotation(annotationModule.getSpectrogramAnnotationType());
//				annotationDataUnit.addDataAnnotation(sa);
//			}
//			sa.setNote(textArea.getText());
//		}
//		annotationModule.getSnrAnnotationType().autoAnnotate(annotationDataUnit);
		boolean ok = true;
		for (AnnotationDialogPanel anPanel:dialogPanels) {
			ok &= anPanel.getParams(annotationDataUnit);
		}
		
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		annotationDataUnit = null;
	}

	@Override
	public void restoreDefaultSettings() {
	}

}
