package export.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fileicons.FileIcons;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

import PamController.PamController;
import PamUtils.PamFileChooser;
import PamView.dialog.PamButton;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import export.PamExporterManager;
import export.layoutFX.ExportParams;
import offlineProcessing.OLProcessDialog;
import offlineProcessing.OfflineTaskGroup;

/**
 * Handles an offline dialog for processing offline data and exporting to bespoke file types.
 *  
 * @author Jamie Macaulay
 *
 */
public class ExportProcessDialog {


	/**
	 * The offline task group
	 */
	private OfflineTaskGroup dlOfflineGroup;


	private ExportOLDialog mtOfflineDialog;

	/**
	 * Reference to the export manager. 
	 */
	private PamExporterManager exportManager;

	/**
	 * The current paramters. 
	 */
	private ExportParams currentParams;

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

	public void showOfflineDialog(Frame parentFrame, ExportParams params) {

		createExportGroup();

		//if null open the dialog- also create a new offlineTask group if the datablock has changed. 
		if (mtOfflineDialog == null) {
			mtOfflineDialog = new ExportOLDialog(parentFrame, 
					dlOfflineGroup, "Export Data");
			//batchLocaliseDialog.setModalityType(Dialog.ModalityType.MODELESS);
		}
		mtOfflineDialog.setParams(params); 
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

		/**
		 * The current parameters for exporting. 
		 */
		private ExportParams currentParams;

		/**
		 * The file chooser. 
		 */
		private JFileChooser fc;

		private JTextField exportTo;

		/**
		 * Spinner for setting the maximum file size. 
		 */
		private JSpinner spinner;



		public ExportOLDialog(Window parentFrame, OfflineTaskGroup taskGroup, String title) {
			super(parentFrame, taskGroup, title);
			// TODO Auto-generated constructor stub

			PamPanel mainPanel = new PamPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			mainPanel.setBorder(new TitledBorder("Export Settings"));

			ButtonGroup buttonGroup = new ButtonGroup();

			PamPanel buttonPanel = new PamPanel();
			ActionListener listener = actionEvent -> {
				System.out.println(actionEvent.getActionCommand() + " Selected");
			};

			for (int i = 0; i < exportManager.getNumExporters(); i++) {
				JToggleButton b = new JToggleButton();
				b.setToolTipText("Export to " + exportManager.getExporter(i).getName() + " file ("  + exportManager.getExporter(i).getFileExtension() + ")");

				FontIcon icon = FontIcon.of(getIconFromString(exportManager.getExporter(i).getIconString()));
				icon.setIconSize(25);
				icon.setIconColor(Color.GRAY);

				b.setIcon(icon);

				b.addActionListener(listener);
				buttonGroup.add(b);
				buttonPanel.add(b);
			}


			PamPanel p = new PamPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 3;
			c.gridx = 0;
			c.gridy = 0; 

			addComponent(p, exportTo = new JTextField(), c);
			exportTo.setMinimumSize(new Dimension(170, 25));
			exportTo.setPreferredSize(new Dimension(170, 25));

			c.gridx +=3;
			c.gridwidth = 1;
			PamButton button = new PamButton("Browse...");

			fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			button.addActionListener((action)->{
				int returnVal = fc.showSaveDialog(this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File yourFolder = fc.getSelectedFile();
					exportTo.setText(yourFolder.getAbsolutePath()); 
					exportTo.setToolTipText(yourFolder.getAbsolutePath()); 
				}
			});		

			addComponent(p, button, c);

			c.gridx = 1;
			c.gridy++;
			c.gridwidth = 2;
			
			JLabel label = new JLabel("Maximum file size", SwingConstants.RIGHT);
			addComponent(p, label, c);

			c.gridwidth = 1;
			c.gridx +=2;
			
			SpinnerListModel list = new SpinnerListModel(new Double[] {10.,30., 60., 100., 200., 300., 600., 1000.});
			
			spinner = new JSpinner(list);
			//don't want the user to to able to set values
			((DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
			spinner.setBounds(50, 80, 70, 100);
			addComponent(p, spinner, c);
			
			c.gridx ++;
			addComponent(p, new JLabel("MB"), c);

		
			
			mainPanel.add(p);
			mainPanel.add(buttonPanel);

			//add the main panel at a different index. 
			getMainPanel().add(mainPanel, 1);
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
		
		public ExportParams getExportParams() {
			
	
			return currentParams;
		}
		
		@Override
		public boolean getParams() {
			//make sure we update the current paramters before processing starts. 
			this.currentParams = getExportParams();
			return super.getParams();
		}

		

		public void setParams(ExportParams params) {
			if (params ==null) currentParams = new ExportParams(); 
			currentParams = params.clone(); 

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