package printscreen;

import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import PamView.GuiFrameManager;
import PamView.PamSidePanel;
import PamguardMVC.PamProcess;
import annotation.handler.AnnotationChoiceHandler;
import annotation.string.StringAnnotationType;
import annotation.userforms.UserFormAnnotationType;
import generalDatabase.DBControlUnit;
import printscreen.swing.PrintScreenDialog;
import printscreen.swing.PrintSidePanel;

public class PrintScreenControl extends PamControlledUnit implements PamSettings {

	public static String unitType = "Print Screen";
	
	private PrintScreenParameters printScreenParameters = new PrintScreenParameters();
	
	private PrintScreenProcess printScreenProcess;
	
	private PrintScreenDataBlock printScreenDataBlock;

	private AnnotationChoiceHandler annotationHandler;

	private PrintScreenLogging screenLogging;
	
	public PrintScreenControl(String unitName) {
		super(unitType, unitName);
		printScreenProcess = new PrintScreenProcess(this);
		printScreenDataBlock = new PrintScreenDataBlock("Screen Captures", printScreenProcess);
		printScreenDataBlock.SetLogging(screenLogging = new PrintScreenLogging(this));
		printScreenProcess.addOutputDataBlock(printScreenDataBlock);
		addPamProcess(printScreenProcess);
		
		// now add optional annotations ...
		annotationHandler = new PrintScreenAnnotationChoiceHandler(this, printScreenDataBlock);
		printScreenDataBlock.setAnnotationHandler(annotationHandler);
		printScreenDataBlock.addDataAnnotationType(new StringAnnotationType("Comment", 50));
		printScreenDataBlock.addDataAnnotationType(new UserFormAnnotationType());
		
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return printScreenParameters;
	}

	@Override
	public long getSettingsVersion() {
		return PrintScreenParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		printScreenParameters = ((PrintScreenParameters) pamControlledUnitSettings.getSettings()).clone();
		return (printScreenParameters != null);
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			annotationHandler.loadAnnotationChoices();
			sortSQLLogging();
			break;
		}
	}
	/**
	 * Check all the SQL Logging additions are set up correctly. 
	 */
	protected void sortSQLLogging() {
		screenLogging.setTableDefinition(screenLogging.createBaseTable());
		if (annotationHandler.addAnnotationSqlAddons(screenLogging) > 0) {
			// will have to recheck the table in the database. 
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc != null) {
				dbc.getDbProcess().checkTable(screenLogging.getTableDefinition());
			}
		}
	}

	/**
	 * Print all GUI frames into jpeg files in destination folder. 
	 */
	public void printScreen() {
		long timeMillis = PamCalendar.getTimeInMillis();
		GuiFrameManager frameManager = PamController.getInstance().getGuiFrameManager();
		int nFrames = frameManager.getNumFrames();
		for (int i = 0; i < nFrames; i++) {
			JFrame aFrame = frameManager.getFrame(i);
			printFrame(i, aFrame, timeMillis);
		}
	}

	private boolean printFrame(int frameIndex, JFrame aFrame, long timeMillis) {
		/*
		 * 
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = image.createGraphics();
            frame.paint(graphics2D);
            ImageIO.write(image,"jpeg", new File("/home/deniz/Desktop/jmemPractice.jpeg"));
       
		 */
        BufferedImage image = new BufferedImage(aFrame.getWidth(), aFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        aFrame.paint(graphics2D);
        String filePrefix = String.format("PAMFrame_%d_", frameIndex);
        
        File outFolder = FileFunctions.getStorageFileFolder(printScreenParameters.getDestFolder(), timeMillis, printScreenParameters.datedSubFolders, true);
        String fileName = PamCalendar.createFileName(timeMillis, outFolder.getAbsolutePath(), filePrefix, printScreenParameters.imageType);
        File imageFile = new File(fileName); 

        PrintScreenDataUnit psdu = new PrintScreenDataUnit(timeMillis, frameIndex, imageFile.getName(), image);
        
        if (frameIndex == 0) {
        	// only do annotations for the first frame to keep life simple. 
        	boolean ok = annotationHandler.annotateDataUnit(getGuiFrame(), psdu);
        	if (!ok) {
        		return false;
        	}
        }
        printScreenDataBlock.addPamData(psdu);
        
        try {
			ImageIO.write(image, printScreenParameters.imageType, imageFile);
			System.out.println("Write image to " + fileName);
		} catch (IOException e) {
//			System.err.println("Unable to write screen file " + fileName + " Error " + e.getMessage());
			return false;
		}
        return true;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getSidePanel()
	 */
	@Override
	public PamSidePanel getSidePanel() {
		PrintSidePanel sidePanel = new PrintSidePanel(this);
		return sidePanel;
	}

	public static String getToolTip() {
		return "Capture PAMGuard screens to time-stamped image files and the clipboard.  Use <CTRL-P> as a shortcut"; 
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " options...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptionsMenu(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showOptionsMenu(Frame parentFrame) {
		PrintScreenParameters newParams = PrintScreenDialog.showDialog(this, parentFrame, printScreenParameters.clone());
		if (newParams != null) {
			printScreenParameters = newParams;
			sortSQLLogging();
		}
	}
	
	private class PrintScreenProcess extends PamProcess {

		public PrintScreenProcess(PamControlledUnit pamControlledUnit) {
			super(pamControlledUnit, null);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void pamStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pamStop() {
			// TODO Auto-generated method stub
			
		}
		
	}

	/**
	 * @return the printScreenParameters
	 */
	public PrintScreenParameters getPrintScreenParameters() {
		return printScreenParameters;
	}

	/**
	 * @return the printScreenProcess
	 */
	public PrintScreenProcess getPrintScreenProcess() {
		return printScreenProcess;
	}

	/**
	 * @return the printScreenDataBlock
	 */
	public PrintScreenDataBlock getPrintScreenDataBlock() {
		return printScreenDataBlock;
	}

	/**
	 * @return the annotationHandler
	 */
	public AnnotationChoiceHandler getAnnotationHandler() {
		return annotationHandler;
	}

}
