package annotation;

import java.awt.Point;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import PamView.dialog.DataUnitSummaryPanel;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataUnit;

/**
 * General dialog that can be used with any annotation type. 
 * @author Douglas Gillespie
 *
 */
public class AnnotationDialog extends PamDialog {

	private DataAnnotationType dataAnnotationType;
	private PamDataUnit dataUnit;
	private AnnotationDialogPanel dialogPanel;
	private boolean okButton;
	private DataUnitSummaryPanel summaryPanel;

	private AnnotationDialog(Window parentFrame, DataAnnotationType dataAnnotationType, PamDataUnit dataUnit) {
		super(parentFrame, dataAnnotationType.getAnnotationName(), false);
		this.dataAnnotationType = dataAnnotationType;
		this.dataUnit = dataUnit;
		dialogPanel = dataAnnotationType.getDialogPanel();
		summaryPanel = new DataUnitSummaryPanel();
		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));
		int IS = 4;
		borderPanel.setBorder(new EmptyBorder(IS, IS, IS, IS));
		borderPanel.add(summaryPanel.getComponent());
		borderPanel.add(dialogPanel.getDialogComponent());
		setDialogComponent(borderPanel);
	}
	
	public static boolean showDialog(Window parentFrame, DataAnnotationType dataAnnotationType, PamDataUnit dataUnit, Point positionInFrame) {
		if (dataAnnotationType.getDialogPanel()==null) return false;
		
		AnnotationDialog annotationDialog = new AnnotationDialog(parentFrame, dataAnnotationType, dataUnit);
		if (positionInFrame != null) {
			annotationDialog.setCentreLocation(positionInFrame);
		}
		annotationDialog.setParams();
		annotationDialog.setVisible(true);
		return annotationDialog.okButton;
	}

	private void setParams() {
		dialogPanel.setParams(dataUnit);
		summaryPanel.setData(dataUnit);
		JComponent dlgComponent = dialogPanel.getDialogComponent();
//		dlgComponent.invalidate();
//		dlgComponent.doLayout();
//		pack();
	}

	@Override
	public boolean getParams() {
		return okButton = dialogPanel.getParams(dataUnit);
	}

	@Override
	public void cancelButtonPressed() {
		okButton = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
