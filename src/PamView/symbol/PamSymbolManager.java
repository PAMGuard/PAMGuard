package PamView.symbol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.GeneralProjector;
import PamView.symbol.modifier.HydrophoneSymbolModifier;
import PamView.symbol.modifier.SuperDetSymbolModifier;
import PamView.symbol.modifier.SuperDetSymbolWrapper;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetDataBlock;
import annotation.CentralAnnotationsList;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import clipgenerator.clipDisplay.ClipSymbolManager;
import offlineProcessing.superdet.OfflineSuperDetFilter;
import pamViewFX.symbol.FXSymbolOptionsPane;

/**
 * PAMSymbolManagers are attached to a single datablock and control the type 
 * of symbol and line that get's used to plot data on the variety of 
 * PAMGUard displays. Note that different displays may chose to plot 
 * data in different ways, so the ManagedSymbolData class will contain 
 * a list of different symbol selectors, each of which can behave independently
 * but all have a "generic" option which is a quick way of forcing them 
 * all to do the same thing. 
 * @author dg50
 *
 */
abstract public class PamSymbolManager<T extends PamSymbolChooser> implements PamSettings {

	public static final String GENERICNAME = "Generic";

	private PamDataBlock pamDataBlock;
	
	private ManagedSymbolData managedSymbolData = new ManagedSymbolData();
	
	private Hashtable<String, PamSymbolChooser> symbolChoosers;

	public PamSymbolManager(PamDataBlock pamDataBlock2) {
		super();
		this.pamDataBlock = pamDataBlock2;
		symbolChoosers = new Hashtable<>();
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getUnitName() {
		return pamDataBlock.getDataName();
	}

	@Override
	public String getUnitType() {
		return "Pam Symbol Manager";
	}
	
	/**
	 * Global clear of all symbol choosers.
	 */
	public static void globalClear() {
		ArrayList<PamDataBlock> allBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aBlock : allBlocks) {
			PamSymbolManager sm = aBlock.getPamSymbolManager();
			if (sm == null) {
				continue;
			}
			sm.clearChoosers();
		}
	}
	
	/**
	 * Clear all the symbol choosers for this datablock. Get's called when annotations for 
	 * the datablock have changed, so that they can all recreated with any new options. 
	 */
	public synchronized void clearChoosers() {
		/*
		 * Get the settings out of the existing choosers into the central list. 
		 */
		getSettingsReference();
		/*
		 * Now clear them. They will get recreated as needed
		 */
		symbolChoosers.clear();
	}

	@Override
	public Serializable getSettingsReference() {
		/**
		 * Make sure that the managedSymbolData has an up to date
		 * list of all the settings from all the individual choosers.
		 */
		for (PamSymbolChooser psc:symbolChoosers.values()) {
			PamSymbolOptions symbolOptions = psc.getSymbolOptions();
			if (symbolOptions != null) {
				managedSymbolData.getSymbolOptions().put(psc.getDisplayName(), symbolOptions);
			}
		}
		return managedSymbolData;
	}

	@Override
	public long getSettingsVersion() {
		return ManagedSymbolData.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.managedSymbolData = ((ManagedSymbolData) pamControlledUnitSettings.getSettings()).clone();
		
		return (managedSymbolData != null);
	}
	
	public synchronized PamSymbolChooser getSymbolChooser(String displayName, GeneralProjector projector) {
		
		/*
		 * This doesn't work since different displays will still need different options based on 
		 * their projector - so it can work, but need to set the projector.
		 */
		if (managedSymbolData.useGeneric) {
			displayName = GENERICNAME;
		}
		
		PamSymbolChooser psc = symbolChoosers.get(displayName);	
		
		if (psc == null) {
			psc = createSymbolChooser(displayName, projector);
			symbolChoosers.put(displayName, psc);
			
			addSymbolModifiers(psc);
			
			/*
			 * Now try to find it's options set in the stored list. 
			 */
			PamSymbolOptions symbolOptions = managedSymbolData.getSymbolOptions().get(displayName);
//			Debug.out.println("restore symbol modifier settiings: " + psc + "   " + symbolOptions); 
			if (symbolOptions != null) {
				psc.setSymbolOptions(symbolOptions);
			}
		}
		// always set the projector to make sure gets the right options. 
		psc.setProjector(projector);
		
		
		return psc;
	}


	abstract protected T createSymbolChooser(String displayName, GeneralProjector projector);

	/**
	 * @return the pamDataBlock
	 */
	public PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the managedSymbolData
	 */
	public ManagedSymbolData getManagedSymbolData() {
		return managedSymbolData;
	}

	/**
	 * Get the Swing pane with controls to change symbol colours, types and line colours. 
	 * @param displayName - the display name
	 * @param projector - the projector associated with the display
	 * @return panel with controls for changing symbol settings. 
	 */
	public SwingSymbolOptionsPanel getSwingOptionsPanel(String displayName, GeneralProjector projector) {
		PamSymbolChooser symbolChooser = getSymbolChooser(displayName, projector);
		return symbolChooser.getSwingOptionsPanel(projector);
	}
	
	/**
	 * Get the FX pane with controls to change symbol colours, types and line colours. 
	 * @param displayName - the display name
	 * @param projector - the projector associated with the display
	 * @return panel with controls for changing symbol settings. 
	 */
	public FXSymbolOptionsPane<?> getFXOptionsPane(String displayName, GeneralProjector projector) {
		//HACK- if the managedSymbolData is set to true then the symbolChooser in a panel is the egenric symbol chooser whihc 
		//we don;t want. Have to force the non generic symbol chooser. 
		boolean prevManSymbBool=	managedSymbolData.useGeneric; 
		managedSymbolData.useGeneric=false; 
		
		PamSymbolChooser symbolChooser = getSymbolChooser(displayName, projector);
		managedSymbolData.useGeneric=prevManSymbBool;
		
		return symbolChooser.getFXOptionPane(projector); 
	}	
	
	/**
	 * Add a list of available (AVAILABLE, not used) symbol modifiers to 
	 * the chooser when it is created. This get's called whenever a symbol chooser 
	 * if created, but probably needs to be called again whenever a model is changes in case
	 * a module is added that contains an additional annotation modifier AFTER the first
	 * call to the display. One to worry about!
	 * @param psc
	 */
	public void addSymbolModifiers(PamSymbolChooser psc) {
		psc.addSymbolModifier(new HydrophoneSymbolModifier(psc));
//		psc.addSymbolModifier(new SuperDetSymbolModifier(psc));
		addSuperDetectionModifiers(psc);
		String name = psc.getDisplayName();
//		Debug.out.println(name);
		addAnnotationModifiers(psc);
	}
	
	/**
	 * See if the super detections associated with this data block might also have
	 * symbol modifiers which we can steal. 
	 * @param psc
	 */
	private void addSuperDetectionModifiers(PamSymbolChooser psc) {
		/**
		 * This is going in an infinite loop. Need to do something to stop it !
		 */
		ArrayList<SuperDetDataBlock> superDetBlocks = OfflineSuperDetFilter.findPossibleSuperDetections(getPamDataBlock());
		
//		Debug.out.println("The number of super det datablock is: " + superDetBlocks + " for the chooser: " +  psc); 
		if (superDetBlocks == null || superDetBlocks.size() == 0) { 
			return;
		}
		for (SuperDetDataBlock superBlock : superDetBlocks) {
			PamSymbolManager superMan = superBlock.getPamSymbolManager();
			if (superMan == null) {
				continue;
			}
			if (superBlock == getPamDataBlock()) {
				continue;
			}
			if (superBlock.getClass() == getPamDataBlock().getClass()) {
				continue;
			}
//			Debug.out.println("The number of super det datablock superMan: " + superMan +  " for the chooser: " +  psc); 

			PamSymbolChooser superSC = superMan.getSymbolChooser("Super det': " + psc.getDisplayName(), psc.getProjector());
			
//			System.out.println("The number of super det datablock superSC: " + superSC +   " for the chooser: " +  psc); 

			// and copy it's modifiers ?
			if (superSC == null) {
				continue;
			}
			ArrayList<SymbolModifier> modifiers = superSC.getSymbolModifiers();
			
//			System.out.println("Add super symbol modifier:  " + modifiers); 

			for (SymbolModifier modifier : modifiers) {
				if (modifier instanceof HydrophoneSymbolModifier) {
					continue;
				}
//				System.out.println("Add super symbol modifier: 2: " + superBlock); 
				psc.addSymbolModifier(new SuperDetSymbolWrapper(superBlock, modifier));
			}
		}
	}

	/**
	 * Add symbol modifiers from annotations. 
	 * @param psc - the symbol chooser. 
	 */
	public void addAnnotationModifiers(PamSymbolChooser psc) {
		AnnotationHandler anHandler = pamDataBlock.getAnnotationHandler();
		if (anHandler == null) {
			return;
		}
//		if (getClass() == ClipSymbolManager.class) {
//			Debug.out.println("Creating clipsymbol manager");
//		}
		List<DataAnnotationType<?>> anTypes = anHandler.getUsedAnnotationTypes();
		
		ArrayList<DataAnnotationType> moreAnTypes = CentralAnnotationsList.getList().findAnnotators(getPamDataBlock());
		
		List<DataAnnotationType> fullList = new ArrayList();
		
		if (anTypes != null) {
			fullList.addAll(anTypes);
		}
		/**
		 * This is a bit doge, and may end up re-adding stuff that's already added. 
		 */
		if (moreAnTypes != null) {
			fullList.addAll(moreAnTypes);
		}
		
		for (DataAnnotationType anType : fullList) {
			SymbolModifier sm = anType.getSymbolModifier(psc);
			if (sm != null) {
				psc.addSymbolModifier(sm);
			}
		}
	}
	
}
