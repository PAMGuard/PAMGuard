/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package beamformer.algorithms.basicFreqDomain;

import Spectrogram.WindowFunction;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseControl;
import beamformer.algorithms.BeamAlgoParamsPane;
import beamformer.algorithms.BeamAlgoParamsPane.BeamInfo;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

/**
 * @author mo55
 *
 */
/**
 * Dialog for the basic frequency domain beam former parameters
 * @author mo55
 *
 */
public class BasicFreqDomParamsPane2 extends BeamAlgoParamsPane  {

	/**
	 * A choicebox listing the types of windows available
	 */
	private ChoiceBox<String> windowType;
	
	/**
	 * The window type to use for the beamogram
	 */
	private ChoiceBox<String> beamogramWindow;
	

	/**
	 * @param basicFreqDomBeamProvider
	 */
	public BasicFreqDomParamsPane2(Object window, BeamFormerBaseControl beamFormerControl2) {
		super(window, beamFormerControl2);
	}

	/**
	 * Create the pane holding the list of beams
	 * @return
	 */
	@Override
	public Node createBeamListPane() {
		Node newPane = super.createBeamListPane();

		// add the Window type to the list
		TableColumn<BeamInfo, String> windowCol = new TableColumn<>("Window");
		windowCol.setMinWidth(80);
		windowCol.setCellValueFactory(new PropertyValueFactory<BeamInfo, String>("windowName"));
		windowCol.setStyle("-fx-alignment: CENTER;");
		this.beamTable.getColumns().add(windowCol);

		// add another listener to the table to update the window type
		beamTable.getSelectionModel().selectedItemProperty().addListener((obj, oldSel, newSel) -> {
			if (newSel != null) {
				synchronized (lineChart) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							windowType.getSelectionModel().select(selectedBeam.getWindow());
						}
					});
				}
			}
		});

		ChoiceBox<String> addWindow = new ChoiceBox<String>(FXCollections.observableArrayList(WindowFunction.getNames()));
		addWindow.getSelectionModel().selectFirst();
		addWindow.setMinWidth(100);

		addButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				if (addSecAngle.isDisabled()) {
					addSecAngle.setText("0");
				}
				beamList.add(new BeamInfo(
						nextBeamNum++,
						Integer.parseInt(addAngle.getText()),
						Integer.parseInt(addSecAngle.getText()),
						addWindow.getSelectionModel().getSelectedIndex(),
						fullFreqRange
						));
				addAngle.clear();
				addWindow.getSelectionModel().selectFirst();
				beamTable.getSelectionModel().select(beamTable.getItems().size()-1);
			}
		});		
		
		BooleanBinding angleField = Bindings.isEmpty(addAngle.textProperty());
		addWindow.disableProperty().bind(angleField);
		addBox.getChildren().remove(addButton);
		addBox.getChildren().addAll(addWindow, addButton);

		// return the pane
		return newPane;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Node createBeamPatternPane() {

		// get the pane from the super class
		Node newPane = super.createBeamPatternPane();

		// add the Window type
		grid.add(new Text("Window Type"), 0, 5);
		windowType = new ChoiceBox<>(FXCollections.observableArrayList(WindowFunction.getNames()));
		windowType.getSelectionModel().selectFirst();
		windowType.disableProperty().bind(Bindings.isEmpty(beamTable.getSelectionModel().getSelectedItems()));
		grid.add(windowType, 1, 5, 2, 1);
		windowType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				windowChanged((int) newValue);
			}
		});
		
		// return the pane
		return newPane;
	}

	private void windowChanged(int windowType) {
		selectedBeam.setWindow(windowType);
		selectedBeam.calculateWeights();
		beamTable.refresh();
	}
	
	/**
	 * @return
	 */
	@Override
	protected Node createBeamogramPane() {
		
		Node newPane = super.createBeamogramPane();
		
		// window function - not used right now
		beamogramWindow = new ChoiceBox<>(FXCollections.observableArrayList(WindowFunction.getNames()));
		beamogramWindow.getSelectionModel().selectFirst();
		beamogramWindow.setMinWidth(200);
		
		// return pane
		return newPane;
	}

	/**
	 * Take settings from the pane and save them into the parameters object
	 * @param p settings passed into this object by PamDialogFX2AWT - ignored
	 */
	@Override
	public BeamAlgorithmParams getParams(BeamAlgorithmParams p) {
		
		if(super.getParams(p)==null) {
			return null;
		}
		
		// save the current beam information into the parameters
		if (beamList.size()>0) {
			int[] beamWindows = new int[beamList.size()];
			double[][] beamWeights = null;
			if (elementLocs != null) {
				beamWeights = new double[beamList.size()][this.elementLocs.length];
				for (int i=0; i<beamList.size(); i++) {
					beamWindows[i]=beamList.get(i).getWindow();
					beamWeights[i]=beamList.get(i).getWeights();
				}
			}
			((BasicFreqDomParams) curParams).setWindowTypes(beamWindows);
			((BasicFreqDomParams) curParams).setWeights(beamWeights);
		}
		
		// save additional beamogram information
		if (curParams.getNumBeamogram()==1) {
			double[] beamogramWeights = new double[this.elementLocs.length];
			beamogramWeights = WindowFunction.getWindowFunc(beamogramWindow.getSelectionModel().selectedIndexProperty().get(),this.elementLocs.length);
			((BasicFreqDomParams) curParams).setBeamogramWeights(beamogramWeights);
		}
		
		// return the parameters
		return curParams;
	}
	
	/**
	 * Take settings from the parameters object and load them into the pane.  Need
	 * to override the entire method here - if the window values aren't added when
	 * the beams are first created, it's very hard refresh the beam pattern chart afterwards
	 */
	@Override
	public void setParams(BeamAlgorithmParams newParams) {
		curParams = newParams;
//		curParams = newParams.clone();
		
		// get the hydrophone locations and set constants
		this.prepareConstants();

		// Clear the line chart data.  Synchronize on
		// line chart so that we don't start adding beams to it (the code after this
		// synchronized block) before it's properly cleared
		synchronized (lineChart) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					lineChart.getData().clear();
				}
			});
		}
		
		// loop through the beam list and create new beams for each.
		beamList.clear();
		int numBeams = newParams.getNumBeams();
		if (numBeams>0) {
			for (int i=0; i<numBeams; i++) {
				int primeAngle = newParams.getHeadings()[i];
				primeAngle = Math.max(primeAngle, minPrime);
				primeAngle = Math.min(primeAngle, maxPrime);
				int secAngle = newParams.getSlants()[i];
				secAngle = Math.max(secAngle, minSec);
				secAngle = Math.min(secAngle, maxSec);
//				BeamInfo newBeam = new BeamInfo(i, primeAngle, secAngle, ((BasicFreqDomParams) newParams).getWindowTypes()[i], newParams.getFreqRange()[i]);
				BeamInfo newBeam = new BeamInfo(i, primeAngle, secAngle, ((BasicFreqDomParams) newParams).getWindowTypes()[i], fullFreqRange); // since we're not allowing user to set freq range, always use the updated range calc
				beamList.add(newBeam);
			}
			beamTable.getSelectionModel().selectFirst();
		}
		
		// set the beamogram information, or clear it if there is no beamogram
		setAngleRange();
		setBeamogramData();
		
		// hide/show the tabs
		this.setTabVisibility();
	}

}
