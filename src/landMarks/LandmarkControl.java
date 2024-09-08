package landMarks;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamProcess;

public class LandmarkControl extends PamControlledUnit implements PamSettings {

	private LandmarkDataBlock landmarkDataBlock;
		
	private LandmarkProcess landmarkProcess;
	
	protected LandmarkDatas landmarkDatas = new LandmarkDatas();
		
	private Color defFill = Color.BLACK;
	
	private Color defLine = Color.BLUE;
	
	private int defWidth = 4;

	private int defHeight = 4;
	
	private PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, defWidth, defHeight, true, defFill, defLine);
	
	public LandmarkControl(String unitName) {
		super("Landmarks", unitName);
		landmarkProcess = new LandmarkProcess(this);
		addPamProcess(landmarkProcess);
		PamSettingManager.getInstance().registerSettings(this);
		landmarkDataBlock.createDataUnits(landmarkDatas);
	}

	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {

		JMenuItem menuItem = new JMenuItem(getUnitName() + " objects");
		menuItem.addActionListener(new DisplayMenuAction(parentFrame));
		return menuItem;
	}

	@Override
	public Serializable getSettingsReference() {
		return landmarkDatas;
	}

	@Override
	public long getSettingsVersion() {
		return LandmarkDatas.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		landmarkDatas = ((LandmarkDatas) pamControlledUnitSettings.getSettings()).clone();
		landmarkDataBlock.createDataUnits(landmarkDatas);
		return true;
	}

	class DisplayMenuAction implements ActionListener {
		
		Frame parentFrame;

		public DisplayMenuAction(Frame parentFrame) {
			super();
			this.parentFrame = parentFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			landmarkSettings(parentFrame);
		}
		
	}
	
	class LandmarkProcess extends PamProcess {

		LandmarkControl landmarkControl;
		
		public LandmarkProcess(LandmarkControl landmarkControl) {
			super(landmarkControl, null);
			this.landmarkControl = landmarkControl;
			addOutputDataBlock(landmarkDataBlock = new LandmarkDataBlock(getUnitName(), landmarkControl, this));
		}

		@Override
		public void pamStart() {
			
		}

		@Override
		public void pamStop() {
			
		}
		
	}

	public PamSymbol getDefaultSymbol() {
		return defaultSymbol;
	}

	public void landmarkSettings(Window parent) {
		LandmarkDatas newLandmarks = LandmarksDialog.showDialog(parent, this);
		if (newLandmarks != null) {
			landmarkDatas = newLandmarks;
			landmarkDataBlock.createDataUnits(landmarkDatas);
		}		
	}

	public void setDefaultSymbol(PamSymbol defaultSymbol) {
		this.defaultSymbol = defaultSymbol;
		this.defFill = defaultSymbol.getFillColor();
		this.defLine = defaultSymbol.getLineColor();
		this.defWidth = defaultSymbol.getWidth();
		this.defHeight = defaultSymbol.getHeight();
	}
	
	public Color getDefaultSymFillColor() {
		return defFill;
	}

	public Color getDefaultSymLineColor() {
		return defLine;
	}
	
	public int getDefaultSymSizeWidth() {
		return defWidth;
	}

	public int getDefaultSymSizeHeight() {
		return defHeight;
	}
}
