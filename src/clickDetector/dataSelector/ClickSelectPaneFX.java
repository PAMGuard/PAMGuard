package clickDetector.dataSelector;

import javax.swing.JPanel;
import PamController.PamController;
import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.alarm.ClickAlarmParameters;
import clickDetector.offlineFuncs.ClicksOffline;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * Pane which allows selection of different click types. Used in displays. 
 * @author Jamie Macaulay 
 *
 */
public class ClickSelectPaneFX extends DynamicSettingsPane<Boolean>  {

	/**
	 * The species selector pane. 
	 */
	private SpeciesPane speciesPanel;

	/**
	 * The event selector pane 
	 */
	private EventTypePane eventTypePanel;

	/**
	 * True to allow scores 
	 */
	private boolean allowScores;

	/**
	 * The click data selector. 
	 */
	private ClickDataSelector clickDataSelector;

	/**
	 * True of viewer 
	 */
	private boolean isViewer;

	/**
	 * Reference to the click control 
	 */
	private ClickControl clickControl;

	private PamVBox mainPanel;


	public ClickSelectPaneFX(ClickDataSelector clickDataSelector, boolean allowScores, boolean useEventTypes) {
		super(null);
		this.clickDataSelector = clickDataSelector;
		this.allowScores = allowScores;
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

		speciesPanel = new SpeciesPane();
		eventTypePanel = new EventTypePane();
		clickControl = clickDataSelector.getClickControl();
		mainPanel = new PamVBox();
		mainPanel.setSpacing(5); 
		mainPanel.getChildren().add(speciesPanel);
		if (useEventTypes) {
			mainPanel.getChildren().add(eventTypePanel);
		}
	}


	@Override
	public Boolean getParams(Boolean p) {
		return (speciesPanel.getParams() & eventTypePanel.getParams());
	}

	@Override
	public void setParams(Boolean input) {
		eventTypePanel.setParams();
		speciesPanel.setParams();
	}

	@Override
	public String getName() {
		return "Filter by click type"; 
	}

	@Override
	public Node getContentNode() {
		return mainPanel;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}

	/**
	 * Species pane which allows different species flags to be selected
	 * @author Jamie Macaulay
	 *
	 */
	private class SpeciesPane extends PamBorderPane {

		private ClickIdentifier clickIdentifier;

		/**
		 * Use all available specie
		 */
		private CheckBox useAll;

		/**
		 * List of species types. 
		 */
		private CheckBox[] species;

		/**
		 * Set all weights to the same value
		 */
		private PamSpinner<Double>  allWeight;

		/**
		 * List of weight controls. 
		 */
		private PamSpinner<Double> [] weights;


		String[] speciesList;

		private PamGridPane centralPane;

		private PamGridPane speciesSelect;

		JPanel centralEastPanel = new JPanel();
		PamGridPane topPane;
		//		JRadioButton andEvents, orEvents;
		//		JRadioButton anyEvents, onlyEvents;


		private CheckBox useEchoes;
		private CheckBox scoreByAmplitude;
		private PamSpinner<Integer> minICI;


		private CheckBox unclassifiedClicks;


		private PamSpinner<Double> unclassifiedWeight;

		SpeciesPane() {

			//Top Pane
			topPane= new PamGridPane();
			topPane.setHgap(5);
			topPane.setVgap(5);

			Label title = new Label("Options"); 
			//PamGridPane.setColumnSpan(title, 3);
			PamGuiManagerFX.titleFont2style(title);
			topPane.add(title, 0, 0); 	
			PamGridPane.setColumnSpan(title, 3);


			topPane.add(new Label("Min ICI "), 0, 1);
			topPane.add(minICI = new PamSpinner<Integer>(0, 10000000, 1, 5), 1, 1);
			minICI.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
			topPane.add(new Label(" ms"), 2, 1);

			topPane.add(scoreByAmplitude = new CheckBox("Score by amplitude"), 0,2);
			PamGridPane.setColumnSpan(scoreByAmplitude, 3);
			scoreByAmplitude.setOnAction(new AllSpeciesListener());

			//Central Pane
			centralPane= new PamGridPane();
			centralPane.setHgap(5);
			centralPane.setVgap(5);

			Label speciesLabel = new Label("Click Type Selection"); 
			PamGuiManagerFX.titleFont2style(speciesLabel);
			PamGridPane.setColumnSpan(speciesLabel, 3);
			centralPane.add(speciesLabel, 0, 1); 

			//Echoes
			useEchoes = new CheckBox("Show echoes");
			setNotificationListener(useEchoes);

			centralPane.add(useEchoes, 0, 2); 

			//Species list.  
			centralPane.add(speciesSelect=new PamGridPane(), 0, 4); 
			speciesSelect.setVgap(5);
			speciesSelect.setHgap(5);

			allWeight = new PamSpinner<Double>(0.,100000., 0., 1.);

			if (allowScores) this.setTop(topPane);
			this.setCenter(centralPane);
		}

		/**
		 * Set the current params for the species pane.
		 */
		void setParams() {
			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters();
			speciesSelect.getChildren().clear();
			
			
			//species pane setup
			species = null;
			weights = null;
			speciesList = null;

			clickIdentifier = clickControl.getClickIdentifier();
			speciesList = clickIdentifier.getSpeciesList();

			if (clickIdentifier != null && speciesList!= null) {
				int iRow=0; 

				Label speciesLabel = new Label("Species");
				if (speciesList != null && speciesList.length>0) {
					speciesSelect.add(speciesLabel, 0,iRow);

					if (allowScores) {
						speciesSelect.add(new Label("Score"), 1,iRow);
					}

					//Convenience check box for selecting all types of species 
					useAll=new CheckBox("All Species"); 
					useAll.setOnAction((action)->{
						if (species == null) return;
						unclassifiedClicks.setSelected(useAll.isSelected());
						for (int i = 0; i < species.length; i++) {
							species[i].setSelected(useAll.isSelected());
						}
						notifySettingsListeners();
					});
					speciesSelect.add(useAll, 0, ++iRow);
					PamGridPane.setColumnSpan(useAll, 3);

					Separator sep=new Separator(Orientation.HORIZONTAL); 
					speciesSelect.add(sep, 0, ++iRow);
					PamGridPane.setColumnSpan(sep, 4);
					
					//add unclassified clicks to the mix
					speciesSelect.add(unclassifiedClicks= new CheckBox("Unclassified") , 0, ++iRow);
					unclassifiedClicks.setOnAction((action)->{
						if (useAll!=null) setUseAllCheckBox(); 
						notifySettingsListeners();
					});
					if (allowScores) {
						speciesSelect.add(unclassifiedWeight = new PamSpinner<Double>(0.,100000.,0.,1.), 4, iRow);
						unclassifiedWeight.setPrefWidth(50);
					}
					PamGridPane.setColumnSpan(unclassifiedClicks, 3);

					
					//add specific species. 
					species = new CheckBox[speciesList.length];
					weights = new PamSpinner[speciesList.length];
					for (int i = 0; i < speciesList.length; i++) {
						speciesSelect.add(species[i] = new CheckBox(speciesList[i]), 0, ++iRow);
						species[i].setOnAction((action)->{
							if (useAll!=null) setUseAllCheckBox(); 
							notifySettingsListeners();
						});
						if (allowScores) {
							speciesSelect.add(weights[i] = new PamSpinner<Double>(0.,100000.,0.,1.), 4, iRow);
							weights[i].setPrefWidth(50);
						}
						PamGridPane.setColumnSpan(speciesSelect, 3);
					}
				}
			}

			//other stuff
			useEchoes.setSelected(clickAlarmParameters.useEchoes);
			minICI.getValueFactory().setValue(clickAlarmParameters.minICIMillis);
			scoreByAmplitude.setSelected(clickAlarmParameters.scoreByAmplitude);
			allWeight.getValueFactory().setValue(clickAlarmParameters.getSpeciesWeight(0));


			if (species != null) {
				if (unclassifiedClicks!=null)  unclassifiedClicks.setSelected(clickAlarmParameters.getUseSpecies(0));
				for (int i = 0; i < species.length; i++) {
					species[i].setSelected(clickAlarmParameters.getUseSpecies(i+1));
					// getting java.lang.ClassCastException: java.lang.Double cannot be cast to java.lang.Integer
					// on next line so have commented for now
					//						weights[i].getValueFactory().setValue(clickAlarmParameters.getSpeciesWeight(i+1));
				}
				//				if (btDisplayParameters.showSpeciesList != null) {
				//					for (int i = 0; i < Math.min(species.length, btDisplayParameters.showSpeciesList.length);i++) {
				//						species[i].setSelected(btDisplayParameters.showSpeciesList[i]);
				//					}
				//				}
			}

			setUseAllCheckBox() ; 
			enableButtons();
		}


		/**
		 * Use the all check box. 
		 */
		private void setUseAllCheckBox() {
			boolean selected=false; 
			boolean deselected=false ;
			if (species != null) {
				for (int i=0; i<species.length; i++) {
					if (species[i].isSelected()) selected=true; 
					else deselected=true; 
				}

				if (this.unclassifiedClicks.isSelected()) selected=true; 
				else deselected=true; 

				if (selected && deselected) useAll.setIndeterminate(true);
				else if (selected) {
					useAll.setIndeterminate(false);
					useAll.setSelected(true); //all are selected 
				}
				else if (deselected) {
					useAll.setIndeterminate(false);
					useAll.setSelected(false);
				}
			}
		}

		/**
		 * Set a notification flag for a control
		 * @param labeled - the control. 
		 */
		private void setNotificationListener(ButtonBase labeled) {
			labeled.setOnAction((action)->{
				notifySettingsListeners();
			});
		}

		boolean getParams() {
			

			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters().clone();
			clickAlarmParameters.useEchoes = useEchoes.isSelected();
			try {
				clickAlarmParameters.minICIMillis = minICI.getValue(); 
			}
			catch (NumberFormatException e) {
				return false; //PamDialog.showWarning(null, "Invalid ICI value - must be integer");
			}
			if (useAll!=null) clickAlarmParameters.setUseSpecies(0, useAll.isSelected());
			double w;
			if (allowScores) {
				clickAlarmParameters.scoreByAmplitude = scoreByAmplitude.isSelected();
				try {
					w = allWeight.getValue();
					clickAlarmParameters.setSpeciesWeighting(0, w);
				}
				catch (NumberFormatException e) {
					return false; //showWarning("Invalid weight for unclassified species ");
				}
			}
			
			if (unclassifiedClicks!=null) {
				clickAlarmParameters.setUseSpecies(0, unclassifiedClicks.isSelected());
				if (unclassifiedClicks.isSelected() && !scoreByAmplitude.isSelected() && allowScores) {
					clickAlarmParameters.setSpeciesWeighting(0, this.unclassifiedWeight.getValue());
				}
			}
			
			if (species != null) {
				for (int i = 0; i < species.length; i++) {
					clickAlarmParameters.setUseSpecies(i+1, species[i].isSelected());
					if (species[i].isSelected() && !scoreByAmplitude.isSelected() && allowScores) {
						try {
							w = weights[i].getValue();
							clickAlarmParameters.setSpeciesWeighting(i+1, w);
						}
						catch (NumberFormatException e) {
							return false; //showWarning("Invalid weight for species: " + species[i].getText());
						}
					}
				}

			}

			//			btDisplayParameters.showANDEvents = andEvents.isSelected();
			//			btDisplayParameters.showEventsOnly = onlyEvents.isSelected();
			
			clickDataSelector.setClickAlarmParameters(clickAlarmParameters);
			return true;
		}
	
		private class AllSpeciesListener implements EventHandler<ActionEvent> {

			@Override
			public void handle(ActionEvent event) {
				enableButtons();		
			}
		}

		private void enableButtons() {
			boolean scoreAmp = scoreByAmplitude.isSelected();
			allWeight.setDisable(scoreAmp);
			if (weights != null) {
				for (int i = 0; i < weights.length; i++) {
					if (weights[i]!=null) weights[i].setDisable(scoreAmp);
				}
			}
		}



	}

	/**
	 * Pane which  allows click trains and events to be displayed and selected. 
	 * @author Jamie Macaulay 
	 *
	 */
	private class EventTypePane extends PamVBox {
		
		private CheckBox useUnassigned, onlineAuto, onlineManual;
		private CheckBox[] useType;
		private LookupList lutList;
		private CheckBox useAll;

		public EventTypePane() {
			this.setSpacing(5);
		}

		void setParams() {
			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters();
			this.getChildren().clear(); 

			Label eventTitle=new Label("Event Type Selection"); 
			PamGuiManagerFX.titleFont2style(eventTitle);
			this.getChildren().add(eventTitle);
			
			this.getChildren().add(useAll = new CheckBox("All event types"));
			useAll.setOnAction((action)->{
				useUnassigned.setSelected(useAll.isSelected());
				onlineManual.setSelected(useAll.isSelected());
				onlineAuto.setSelected(useAll.isSelected());
				if (useType != null) {
					for (int i=0; i<useType.length; i++) {
						useType[i].setSelected(useAll.isSelected());
					}
				}
				notifySettingsListeners();
			});
			this.getChildren().add(new Separator(Orientation.HORIZONTAL));


			this.getChildren().add(useUnassigned = new CheckBox("Unassigned events"));
			useUnassigned.setSelected(clickAlarmParameters.unassignedEvents);
			useUnassigned.setOnAction((action)->{
				 setUseAllCheckBox();
				notifySettingsListeners();
			});
			this.getChildren().add(onlineManual = new CheckBox("Manually detected click trains"));
			onlineManual.setSelected(clickAlarmParameters.onlineManualEvents);
			onlineManual.setOnAction((action)->{
				 setUseAllCheckBox();
				notifySettingsListeners();
			});
			this.getChildren().add(onlineAuto = new CheckBox("Automatically detected click trains"));
			onlineAuto.setSelected(clickAlarmParameters.onlineAutoEvents);
			onlineAuto.setOnAction((action)->{
				 setUseAllCheckBox();
				notifySettingsListeners();
			});
			
			
			//2024-12-18 This is causing an excpetion because lookup tabel does not exist. 
//			lutList = LookUpTables.getLookUpTables().getLookupList(ClicksOffline.ClickTypeLookupName);
//			if (lutList == null) {
//				return;
//			}
//			useType = new CheckBox[lutList.getList().size()];
//			for (int i = 0; i < useType.length; i++) {
//				this.getChildren().add(useType[i] = new CheckBox(lutList.getList().get(i).getText()));
//				useType[i].setSelected(clickAlarmParameters.isUseEventType(lutList.getList().get(i).getCode()));
//				useType[i].setOnAction((action)->{
//					 setUseAllCheckBox();
//					notifySettingsListeners();
//				});
//			}

			 setUseAllCheckBox();
		}
		
		
		/**
		 * Use the all check box. 
		 */
		private void setUseAllCheckBox() {
			boolean selected=false; 
			boolean deselected=false ;
	

				if (this.useUnassigned.isSelected()) selected=true; 
				else deselected=true; 
				
				if (this.onlineManual.isSelected()) selected=true; 
				else deselected=true; 
				
				if (this.onlineAuto.isSelected()) selected=true; 
				else deselected=true; 
				
				if (useType != null) {
					for (int i=0; i<useType.length; i++) {
						if (useType[i].isSelected()) selected=true; 
						else deselected=true; 
					}
				}

				if (selected && deselected) useAll.setIndeterminate(true);
				else if (selected) {
					useAll.setIndeterminate(false);
					useAll.setSelected(true); //all are selected 
				}
				else if (deselected) {
					useAll.setIndeterminate(false);
					useAll.setSelected(false);
				}
			}
		


		boolean getParams() {
			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters();
			try {
				clickAlarmParameters.unassignedEvents = useUnassigned.isSelected();
				clickAlarmParameters.onlineAutoEvents = onlineAuto.isSelected();
				clickAlarmParameters.onlineManualEvents = onlineManual.isSelected();

				if (useType != null) {
					for (int i = 0; i < useType.length; i++) {
						clickAlarmParameters.setUseEventType(lutList.getList().get(i).getCode(), useType[i].isSelected());
					}
				}
			}
			catch (Exception e) {
				return false;
			}
			return true;
		}
	}
}
