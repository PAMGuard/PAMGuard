package spectrogramNoiseReduction.layoutFX;


import java.util.ArrayList;

import org.controlsfx.control.ToggleSwitch;

import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import spectrogramNoiseReduction.SpecNoiseMethod;
import spectrogramNoiseReduction.SpectrogramNoiseProcess;
import spectrogramNoiseReduction.SpectrogramNoiseSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;

/**
 * JavaFX pane for changing SpectrogramNosie settings. 
 * @author 	Jamie Macaulay 
 *
 */
public class SpectrogramNoisePaneFX extends SettingsPane<SpectrogramNoiseSettings> {
	

	private PamVBox p;
	
	private SpectrogramNoiseProcess spectrogramNoiseProcess;

	private ArrayList<SpecNoiseMethod> methods;
	
	private SpectrogramNoiseSettings spectrogramNoiseSettings=new SpectrogramNoiseSettings(); 

	private ToggleSwitch[] enableMethod;
	
	private PamDataBlock dataSource;
	
	private SourcePaneFX sourcePane;
	
	public SpectrogramNoisePaneFX(
			SpectrogramNoiseProcess spectrogramNoiseProcess) {
		super(null);
		this.spectrogramNoiseProcess = spectrogramNoiseProcess;

		p = new PamVBox();
		p.setSpacing(5);
		p.setPadding(new Insets(5,5,5,5));

		methods = spectrogramNoiseProcess.getMethods();
		PamVBox methodPane;
		SpecNoiseNodeFX dC;
		Node node;
		enableMethod = new ToggleSwitch[methods.size()];
		

		for (int i = 0; i < methods.size(); i++) {
			methodPane = new PamVBox();
			methodPane.setSpacing(10); 
			//create name
			Label title=new Label(methods.get(i).getName());
			PamGuiManagerFX.titleFont2style(title);
//			title.setFont(PamGuiManagerFX.titleFontSize2);
			
			PamHBox toggleHolder = new PamHBox();
			toggleHolder.setSpacing(5);
			toggleHolder.getChildren().addAll(enableMethod[i] = 
					new ToggleSwitch(), title); 
			enableMethod[i].setAlignment(Pos.CENTER_LEFT);
			toggleHolder.setAlignment(Pos.CENTER_LEFT);
			
			methodPane.getChildren().add(toggleHolder);

						
			enableMethod[i].selectedProperty().addListener((obsval, oldval, newval)->enableCheckControls());
			enableMethod[i].setTooltip(new Tooltip(methods.get(i).getDescription()));
			title.setTooltip(new Tooltip(methods.get(i).getDescription()));
			//methodPane.getChildren().add(enableMethod[i]);
			
			dC = methods.get(i).getNode();
			if (dC != null) {
				node = dC.getNode(); 
				if (node != null) {
					methodPane.getChildren().add(node);
				}
			}
			p.getChildren().add(methodPane);
		}

	}
	
	/**
	 * Set a source panel so that the dialog panel can respond to source changes. 
	 * @param sourcePanel sourcepanel. 
	 */
	public void setSourcePanel(SourcePaneFX sourcePane) {
		this.sourcePane = sourcePane;
		sourcePane.addSelectionListener((obsValue, oldVal, newVal)->{
			setSource(sourcePane.getSource());
		});
	}

	public void setSource(PamDataBlock sourceDataBlock) {
		this.dataSource = sourceDataBlock;
		enableCheckControls();
	}

	public void setParams(SpectrogramNoiseSettings spectrogramNoiseSettings) {
		this.spectrogramNoiseSettings=spectrogramNoiseSettings; 
		SpecNoiseNodeFX dC;
		for (int i = 0; i < methods.size(); i++) {
			enableMethod[i].setSelected(spectrogramNoiseSettings.isRunMethod(i));
			dC = methods.get(i).getNode(); 
			if (dC != null) {
				dC.setParams();
				dC.setSelected(enableMethod[i].isSelected());
			}
		}

		enableCheckControls();
		
	}
	
	public SpectrogramNoiseSettings getParams() {
		if (spectrogramNoiseSettings == null) {
			return null;
		}
		SpecNoiseNodeFX dC;
		boolean answer;
		boolean sel;
		for (int i = 0; i < methods.size(); i++) {
			sel = enableMethod[i].isSelected();
			spectrogramNoiseSettings.setRunMethod(i, sel);
			if (sel) {
				dC = methods.get(i).getNode();
				if (dC != null && enableMethod[i].isSelected()) {
					answer = dC.getParams();
					if (answer == false) {
						return null;
					}
				}
			}
		}
		return this.spectrogramNoiseSettings;
		
	}


	public void enableCheckControls() {
		SpecNoiseNodeFX dC;
		boolean done;
		for (int i = 0; i < methods.size(); i++) {
			dC = methods.get(i).getNode();
			done = alreadyDone(methods.get(i));
			if (done) {
				enableMethod[i].setSelected(false);
			}
			enableMethod[i].setDisable(done == true);
			if (dC != null) {
				dC.setSelected(enableMethod[i].isSelected());
			}
		}
	}
	
	private boolean alreadyDone(SpecNoiseMethod noiseMethod) {
		if (dataSource == null) {
			return false;
		}
		return (dataSource.findAnnotation(noiseMethod.getAnnotation(spectrogramNoiseProcess)) != null);
	}
	
	/**
	 * Will return true for a method if either this panel has the method selected OR
	 * the source data has already had that method applied. 
	 * @param iMethod
	 * @return true if the method has been selected. 
	 */
	public boolean hasProcessed(int iMethod) {
		if (iMethod < 0 || iMethod >= methods.size()) {
			return false;
		}
		SpecNoiseMethod method = methods.get(iMethod);
		if (alreadyDone(method)) {
			return true;
		}
		if (enableMethod[iMethod].isSelected()) {
			return true;
		}
		return false;
	}


	@Override
	public String getName() {
		return "Spectrogram Noise Settings";
	}

	@Override
	public Node getContentNode() {
		return this.p ;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SpectrogramNoiseSettings getParams(SpectrogramNoiseSettings currParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
