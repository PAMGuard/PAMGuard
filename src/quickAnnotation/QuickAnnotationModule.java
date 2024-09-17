package quickAnnotation;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.importData.ImportDataSystem;
import annotation.calcs.snr.SNRAnnotationType;
import annotation.calcs.spl.SPLAnnotationType;
import annotation.calcs.wav.WavAnnotation;
import annotation.calcs.wav.WavAnnotationType;
import annotation.string.StringAnnotation;
import annotation.string.StringAnnotationType;
import annotationMark.MarkAnnotationDialog;
import annotationMark.MarkDataUnit;
import annotationMark.spectrogram.SpectrogramAnnotationModule;
import generalDatabase.lookupTables.LookupItem;
import quickAnnotation.importAnnotation.QuickAnnotationImport;
import quickAnnotation.importAnnotation.QuickAnnotationImportParams;

public class QuickAnnotationModule extends SpectrogramAnnotationModule implements PamSettings {

	private QuickAnnotationParameters quickAnnotationParams = new QuickAnnotationParameters();
	private LookupItem selectedAnnotationItem = null;
	private WavAnnotationType wavAnnotationType;
	private QuickAnnotationSidePanel annotationSidePanel;
	private ImportDataSystem<ArrayList<String>> quickAnnotationImportManager;
	private QuickAnnotationImportParams quickAnnotationImportParameters;

	public QuickAnnotationModule(String unitName) {
		super(unitName);

		annotationHandler.addAnnotationType(wavAnnotationType =new WavAnnotationType());

		PamSettingManager.getInstance().registerSettings(this);
		
		getAnnotationDataBlock().setOverlayDraw(new QuickAnnotationOverlayDraw(getAnnotationDataBlock(), this));
		annotationSidePanel = new QuickAnnotationSidePanel(this);
		setSidePanel(annotationSidePanel);

		if (isViewer){
			quickAnnotationImportManager = new ImportDataSystem<ArrayList<String>>(new QuickAnnotationImport(this));
			quickAnnotationImportManager.setName("Quick Annotation Data Import");
		}

	}

	
	public SNRAnnotationType getSnrAnnotationType() {
		return snrAnnotationType;
	}

	public SPLAnnotationType getSPLAnnotationType() {
		return splAnnotationType;
	}
	
	public WavAnnotationType getWavAnnotationType() {
		return wavAnnotationType;
	}


	@Override
	public Serializable getSettingsReference() {
		return quickAnnotationParams;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return QuickAnnotationParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
			quickAnnotationParams = ((QuickAnnotationParameters) pamControlledUnitSettings.getSettings()).clone();
			return (quickAnnotationParams != null);
	}

	public QuickAnnotationParameters getQuickAnnotationParameters() {
		return quickAnnotationParams;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu menu = new JMenu(getUnitName());
		JMenuItem menuItem = new JMenuItem(getUnitName() + " settings ...");
		menuItem.addActionListener(new SettingsMenu(parentFrame));
		menu.add(menuItem);
//		JMenuItem typesItem = new JMenuItem(getUnitName() + " Types ...");
//		typesItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				showSettingsMenu(parentFrame);
//			}
//		});
//		menu.add(typesItem);
		if (isViewer){
			JMenuItem offlineDataItem = new JMenuItem("Import annotations from CSV");
			offlineDataItem.addActionListener(new ImportMenu(parentFrame));
			menu.add(offlineDataItem);
			return menu;
		} else
			return menuItem;
	}
	
	class SettingsMenu implements ActionListener {

		private Frame parentFrame;

		public SettingsMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			settingMenu(parentFrame);
		}
		
	}

	public boolean settingMenu(Frame parentFrame) {
		if (parentFrame == null) {
			parentFrame = this.getGuiFrame();
		}
		QuickAnnotationParameters newParams = QuickAnnotationParamsDialog.showDialog(parentFrame, this, quickAnnotationParams);
		if (newParams != null) {
			quickAnnotationParams = newParams.clone();
//			quickAnnotationProcess.setupProcess();
			return true;
		}
		else {
			return false;
		}

	}
	
	class ImportMenu implements ActionListener {

		private Frame parentFrame;

		public ImportMenu(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			importMenu(parentFrame);
		}
		
	}

	public boolean importMenu(Frame parentFrame) {
		if (parentFrame == null) {
			parentFrame = this.getGuiFrame();
		}
//		QuickAnnotationImportParams newParams = QuickAnnotationDialog.showDialog(parentFrame, new QuickAnnotationImportParams());
		QuickAnnotationImportParams newParams =  new QuickAnnotationImportParams();
		quickAnnotationImportParameters = newParams.clone();
		quickAnnotationImportManager.showImportDialog();
		return true;

	}
	
	public void updateSidePanel() {
		annotationSidePanel.updateAnnotationSelector();
	}

	/**
	 * Here we need to handle all of the annotations that cannot autoAnnotate.
	 */
	@Override
	protected boolean manualAnnotate(MarkDataUnit adu, Point locOnScreen) {
		boolean shouldAdd = true;
		StringAnnotation note = (StringAnnotation) adu.findDataAnnotation(StringAnnotation.class,
				"Note");
		StringAnnotation label = (StringAnnotation) adu.findDataAnnotation(StringAnnotation.class,
				getLabelAnnotationType().getAnnotationName());
		if (label==null)
			label = new StringAnnotation(getLabelAnnotationType());
		if (note==null)
			note = new StringAnnotation(getStringAnnotationType());
		
		if (getQuickAnnotationParameters().assignLabels){
			label.setString(getSelectedAnnotationCode());
		}
		adu.addDataAnnotation(label);
		adu.addDataAnnotation(note);

		if (getQuickAnnotationParameters().shouldPopupDialog){
			shouldAdd = MarkAnnotationDialog.showDialog(getGuiFrame(), QuickAnnotationModule.this, adu, locOnScreen);
		} 
		if (shouldAdd) {
			if (getQuickAnnotationParameters().exportClips) {
				WavAnnotation wavAnnotate = new WavAnnotation(getWavAnnotationType());
				wavAnnotate.setWavFolderName(getQuickAnnotationParameters().getFolderName());
				wavAnnotate.setWavPrefix(label.getString());
				adu.addDataAnnotation(wavAnnotate);
				getWavAnnotationType().autoAnnotate(adu);	
			}

		}
		return shouldAdd;
	}
	
	
//	/**
//	 * Find an existing annotation data unit. 
//	 * @param channel
//	 * @param startMilliseconds
//	 * @param f1
//	 * @return existing unit, or null. 
//	 */
//	public MarkDataUnit findAnnotationUnit(int channel, long startMilliseconds, double f1) {
//		ListIterator<MarkDataUnit> anIt = getAnnotationDataBlock().getListIterator(0);
//		while (anIt.hasNext()) {
//			MarkDataUnit aUnit = anIt.next();
//			if (startMilliseconds < aUnit.getTimeMilliseconds()) continue;
//			if ((aUnit.getChannelBitmap() & 1<<channel) == 0) continue;
////			SpectrogramAnnotation specAnnotation = (SpectrogramAnnotation) aUnit.findDataAnnotation(SpectrogramAnnotation.class);
////			if (specAnnotation == null) continue;
//			if (startMilliseconds > aUnit.getEndTimeInMilliseconds()) continue;
//			if (f1 < aUnit.getFrequency()[0] || f1 > aUnit.getFrequency()[1]) continue;
//			return aUnit;
//		}
//		return null;
//	}


	public StringAnnotationType getStringAnnotationType() {
		return stringAnnotationType;
	}

	public StringAnnotationType getLabelAnnotationType() {
		return labelAnnotationType;
	}
	
	public LookupItem getSelectedAnnotationItem() {
		return quickAnnotationParams.selectedClassification;
	}
	
	public String getSelectedAnnotationCode(){
		if (getSelectedAnnotationItem() == null)
			return "";
		else
			return getSelectedAnnotationItem().getCode();
	}

	@Override
	public String getMarkType() {
		return getSelectedAnnotationCode();
	}
}



