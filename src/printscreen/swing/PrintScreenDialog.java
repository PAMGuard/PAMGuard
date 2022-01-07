package printscreen.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.FolderChangeListener;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamFlowLayout;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationHandler;
import annotation.handler.AnnotationsSelectionPanel;
import printscreen.PrintScreenControl;
import printscreen.PrintScreenParameters;
import weka.core.SingleIndex;

public class PrintScreenDialog extends PamDialog {

	private SelectFolder folderSelector;
	
	private JComboBox<String> typesList;

	private PrintScreenParameters params;

	private PrintScreenControl printScreenControl;
	
	private static PrintScreenDialog singleInstance;
	
	private AnnotationsSelectionPanel annotationsSelectionPanel;
	
	private PrintScreenDialog(PrintScreenControl printScreenControl, Window parentFrame) {
		super(parentFrame, "Print Screen Options", true);
		this.printScreenControl = printScreenControl;
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//		GridBagConstraints c = new PamGridBagContraints();
		folderSelector = new SelectFolder("Output Folder", 30, true);
		folderSelector.setSubFolderButtonName("Store in sub folders by date");
		JComponent panel;
		mainPanel.add(panel = folderSelector.getFolderPanel());
		panel.setBorder(new TitledBorder("Output Folder"));
		
		JPanel formatPanel = new JPanel(new BorderLayout());
		formatPanel.setBorder(new TitledBorder("Format"));
		formatPanel.add(BorderLayout.WEST, new JLabel("Output format ", JLabel.RIGHT));
		typesList = new JComboBox<>();
		String[] types = ImageIO.getWriterFileSuffixes();
		for (int i = 0; i < types.length; i++) {
			typesList.addItem(types[i]);
		}
		formatPanel.add(BorderLayout.CENTER, typesList);
		mainPanel.add(formatPanel);
		
		JPanel annotationPanel = new JPanel(new BorderLayout());
		AnnotationChoiceHandler annotationHandler = printScreenControl.getAnnotationHandler();
		annotationsSelectionPanel = new AnnotationsSelectionPanel(annotationHandler);
		annotationPanel.add(BorderLayout.WEST, annotationsSelectionPanel.getDialogComponent());
		annotationPanel.setBorder(new TitledBorder("Annotations"));
		mainPanel.add(annotationPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static PrintScreenParameters showDialog(PrintScreenControl printScreenControl, Window parentFrame, PrintScreenParameters params) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new PrintScreenDialog(printScreenControl, parentFrame);
//		}
		singleInstance.setParams(params);
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	private void setParams(PrintScreenParameters params) {
		this.params = params;
		folderSelector.setFolderName(params.getDestFolder());
		folderSelector.setIncludeSubFolders(params.datedSubFolders);
		typesList.setSelectedItem(params.imageType);
		annotationsSelectionPanel.setParams();
	}

	@Override
	public boolean getParams() {
		params.setDestFolder(folderSelector.getFolderName(true));
		params.datedSubFolders = folderSelector.isIncludeSubFolders();
		params.imageType = typesList.getSelectedItem().toString();
		return annotationsSelectionPanel.getParams();
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new PrintScreenParameters());
	}

}
