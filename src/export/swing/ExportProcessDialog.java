package export.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fileicons.FileIcons;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

import PamController.PamController;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import export.PamExporterManager;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

/**
 * Processes exportying data. 
 * @author Jamie Macaulay
 *
 */
public class ExportProcessDialog {


	/**
	 * The offline task group
	 */
	private OfflineTaskGroup dlOfflineGroup;

	private OLProcessDialog mtOfflineDialog;

	/**
	 * Reference to the export manager. 
	 */
	private PamExporterManager exportManager;

	public ExportProcessDialog(PamExporterManager exportManager) {
		//create the offline task group. 
		this.exportManager=exportManager;
		dlOfflineGroup = new ExportTaskGroup("Export data");
	}


	public void createExportGroup() {
		
		//clear current tasks. 
		dlOfflineGroup.clearTasks();

		//go through every data block we have and check if we can export the data units...
		ArrayList<PamDataBlock> dataBlocks= PamController.getInstance().getDataBlocks();
		
		for (int i=0; i<dataBlocks.size(); i++) {
			if (exportManager.canExportDataBlock(dataBlocks.get(i))) {
				dlOfflineGroup.addTask(new ExportTask(dataBlocks.get(i), exportManager));
			}
		}

	}
	////---Swing stuff----/// should not be here but this is how PG works. 

	public void showOfflineDialog(Frame parentFrame) {

		createExportGroup();
		
		//if null open the dialog- also create a new offlineTask group if the datablock has changed. 
		if (mtOfflineDialog == null) {
			mtOfflineDialog = new ExportOLDialog(parentFrame, 
					dlOfflineGroup, "Export Data");
			//batchLocaliseDialog.setModalityType(Dialog.ModalityType.MODELESS);
		}
		mtOfflineDialog.enableControls();
		mtOfflineDialog.setVisible(true);
	}

	/**
	 * Custom dialog which shows some extra options/ 
	 * @author Jamie Macaulay
	 *
	 */
	class ExportOLDialog extends OLProcessDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ExportOLDialog(Window parentFrame, OfflineTaskGroup taskGroup, String title) {
			super(parentFrame, taskGroup, title);
			// TODO Auto-generated constructor stub

			BorderLayout layout = new BorderLayout();
			PamPanel mainPanel = new PamPanel();

			ButtonGroup buttonGroup = new ButtonGroup();


			PamPanel buttonPanel = new PamPanel();
			ActionListener listener = actionEvent -> {
				System.out.println(actionEvent.getActionCommand() + " Selected");
			};

			for (int i = 0; i < exportManager.getNumExporters(); i++) {
				JToggleButton b = new JToggleButton();
								
				FontIcon icon = FontIcon.of(getIconFromString(exportManager.getExporter(i).getIconString()));
				icon.setIconSize(25);
				icon.setIconColor(Color.GRAY);
				
				
				b.setIcon(icon);
				
				b.addActionListener(listener);
				buttonGroup.add(b);
				buttonPanel.add(b);
			}

			mainPanel.add(buttonPanel, BorderLayout.CENTER);

			//add the main panel at a diffderent index. 
			getMainPanel().add(buttonPanel, 1);
		}
		
		private Ikon getIconFromString(String iconString) {
			
			Ikon icon = null;
			/**
			 * This is nasty but we won't have many exporters and this is the only
			 * good way to get this to work in Swing. 
			 */
			switch (iconString) {
			case "file-matlab":
				icon=FileIcons.MATLAB;
				break;
			case "file-r":
				icon=FileIcons.R;
					break;
			case "mdi2f-file-music":
				icon=MaterialDesignF.FILE_MUSIC;
				break;
			}
			
			return icon;
		
		}
		


	}


	class ExportTaskGroup extends OfflineTaskGroup{

		public ExportTaskGroup(String settingsName) {
			super(null, settingsName);
			// TODO Auto-generated constructor stub

		}

		@Override
		public String getUnitType() {
			return "Export Data";
		}
	}



}