package quickAnnotation;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import java.io.File;

import PamController.PamController;
import PamController.PamSettingManager;
import PamUtils.SelectFolder;
import PamView.dialog.PamButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
import annotation.handler.AnnotationsSelectionPanel;
import generalDatabase.lookupTables.LookupEditDialog;
import generalDatabase.lookupTables.LookupList;

public class QuickAnnotationParamsDialog extends PamDialog {

	private static QuickAnnotationParamsDialog singleInstance;
	QuickAnnotationModule quickAnnotationModule;
	QuickAnnotationParameters quickAnnotationParams;
	PamCheckBox exportWav, shouldPopupDialog;
	SelectFolder wavFolderSelect;
	JTextField wavPrefix;
	PamButton editList, defaultList, annoTypes;
	
	QuickAnnotationParamsDialog(Window parentFrame, QuickAnnotationModule quickAnnotationModule){
		super(parentFrame, quickAnnotationModule.getUnitName() + " settings", false);
		this.quickAnnotationModule = quickAnnotationModule;
		PamPanel basePanel = new PamPanel();
		basePanel.setLayout(new BoxLayout(basePanel,BoxLayout.Y_AXIS));

		GridBagLayout gb = new GridBagLayout();
		
		PamPanel labelPanel = new PamPanel();
		labelPanel.setBorder(new TitledBorder("Labels and automation"));
		labelPanel.setLayout(gb);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		editList = new PamButton("Create and edit labels");
		labelPanel.add(editList,c);
		c.gridy++;
		
		defaultList = new PamButton("Preset labels");
		labelPanel.add(defaultList,c);
		c.gridy++;
		
		c.gridy++;
		annoTypes = new PamButton("Choose annotation types");
		annoTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quickAnnotationModule.showSettingsMenu((Frame) parentFrame);
			}
		});
		labelPanel.add(annoTypes,c);
		c.gridy++;
		
		shouldPopupDialog = new PamCheckBox("Popup annotation dialog");
		shouldPopupDialog.setToolTipText("When checked, a popup dialog will always be displayed when creating new annotations, even if assigning labels.");
		labelPanel.add(shouldPopupDialog,c);
		c.gridy++;
		
		PamBorderPanel wavPanel = new PamBorderPanel();
		wavPanel.setBorder(new TitledBorder("Wav export"));
		wavPanel.setLayout(gb);
		c.gridx = 0;
		c.gridy = 0;
		exportWav = new PamCheckBox("Export new annotations as wav file");
		wavPanel.add(exportWav,c);
		c.gridy++;		

		wavPanel.add(new PamLabel("Wav file folder:"),c);
		c.gridx++;
		wavPrefix = new JTextField(8);
//		wavPanel.add(wavPrefix,c);
		c.gridy++; 

		c.gridx = 0;
		c.gridwidth = c.REMAINDER;

		wavFolderSelect = new SelectFolder("Select output folder", 32, false);
		String filePath = PamSettingManager.getInstance().getDefaultFile();
		filePath = filePath.substring(0,filePath.lastIndexOf(File.separator));
		wavFolderSelect.setFolderName(filePath);

		wavPanel.add(wavFolderSelect.getFolderPanel(),c);
	
		c.gridy++;
		
		basePanel.add(labelPanel);
		basePanel.add(wavPanel);
		
		editList.addActionListener(new EditQuickList());
		defaultList.addActionListener(new DefaultQuickList());
		setDialogComponent(basePanel);
		
	}
	
	public QuickAnnotationParamsDialog(Window parentFrame, String title, boolean hasDefault) {
		super(parentFrame, title, hasDefault);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getParams() {
		singleInstance.quickAnnotationModule.getSidePanel().getPanel().getParent().validate();
		quickAnnotationModule.getQuickAnnotationParameters().shouldPopupDialog = shouldPopupDialog.isSelected();
		quickAnnotationModule.getQuickAnnotationParameters().exportClips = exportWav.isSelected();
		try {
			quickAnnotationModule.getQuickAnnotationParameters().folderName = wavFolderSelect.getFolderName(true);
			quickAnnotationModule.getQuickAnnotationParameters().fileNamePrefix = wavPrefix.getText();
		} catch(Exception e){
			return showWarning(e.getMessage());
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	
	class EditQuickList implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			editQuickList();
		}

		/**
		 * Edit lookup list and update the side panel of quick selections
		 */
		private void editQuickList() {
			LookupList newList = LookupEditDialog.showDialog(PamController.getInstance().getMainFrame(), 
					quickAnnotationModule.getQuickAnnotationParameters().getQuickList(quickAnnotationModule));
			if (newList != null) {
				quickAnnotationModule.getQuickAnnotationParameters().setQuickList(newList);
				quickAnnotationModule.updateSidePanel();
			}
		}
	}

	class DefaultQuickList implements ActionListener{

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JPopupMenu menu = new JPopupMenu("Select a Default Annotation Configuration");
			JMenuItem menuItem;
			for (int i = 0; i < 1; i++) {
				//TODO: Move these hard-coded defaults into the control module
				menuItem = new JMenuItem("SORP blue and fin whale presets");
				menuItem.addActionListener(new SorpDefaultAction());
				menu.add(menuItem);
			}
			menu.show(defaultList, defaultList.getWidth()/2, defaultList.getHeight()/2);
		}
	}
		
	
	/**
	 * Setup a default species from the list. 
	 * @author Douglas Gillespie
	 *
	 */
	class SorpDefaultAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			LookupList newList = quickAnnotationParams.sorpList(quickAnnotationModule);
			if (newList != null) {
				quickAnnotationModule.getQuickAnnotationParameters().setQuickList(newList);
				quickAnnotationModule.updateSidePanel();
			}
		}
		
	}
	
	public static final QuickAnnotationParameters showDialog(Frame parentFrame, QuickAnnotationModule quickAnnotationModule,
			QuickAnnotationParameters quickAnnotationParams) {
			
			if (singleInstance == null || singleInstance.quickAnnotationModule != quickAnnotationModule || singleInstance.getOwner() != parentFrame) {
				singleInstance = new QuickAnnotationParamsDialog(parentFrame, quickAnnotationModule);
			}
			
			singleInstance.quickAnnotationParams = quickAnnotationParams;
			singleInstance.setParams();
			singleInstance.validate();
			singleInstance.pack();
			singleInstance.setVisible(true);
			return singleInstance.quickAnnotationParams;
		}

	private void setParams() {
		shouldPopupDialog.setSelected(quickAnnotationModule.getQuickAnnotationParameters().shouldPopupDialog);
		exportWav.setSelected(quickAnnotationModule.getQuickAnnotationParameters().exportClips);
		try {
			wavFolderSelect.setFolderName(quickAnnotationModule.getQuickAnnotationParameters().getFolderName());
			wavPrefix.setText(quickAnnotationModule.getQuickAnnotationParameters().fileNamePrefix);
		} catch(Exception e){
			showWarning(e.getMessage());
		}
	}
}