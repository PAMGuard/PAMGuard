package beamformer.localiser.plot;

import java.util.ArrayList;

import Array.ArrayManager;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamGroupProcess;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import userDisplay.UserDisplayControl;

public class FXBeamLocDisplay {

	private Scene mainScene;
	private BeamLocDisplayProvider beamLocDisplayProvider;
	private BeamFormLocaliserControl bfLocaliserControl;
	private UserDisplayControl userDisplayControl;
	private String uniqueDisplayName;
	private ArrayList<BeamDataDisplay> dataDisplays = new ArrayList<>();
	
	public FXBeamLocDisplay(BeamLocDisplayProvider beamLocDisplayProvider,
			BeamFormLocaliserControl bfLocaliserControl, UserDisplayControl userDisplayControl,
			String uniqueDisplayName) {
		this.beamLocDisplayProvider = beamLocDisplayProvider;
		this.bfLocaliserControl = bfLocaliserControl;
		this.userDisplayControl = userDisplayControl;
		this.uniqueDisplayName = uniqueDisplayName;
		
		bfLocaliserControl.getQueuedDataBlock().addObserver(new DataObserver(), false);

		mainScene = new Scene(new BorderPane());
		createDisplays();
	}
	private void createDisplays() {
		ArrayList<BeamGroupProcess> groupProcs = bfLocaliserControl.getBeamFormerProcess().getGroupProcesses();
		dataDisplays.clear();
		
//		miniSpec = new FXMiniSpectrogram();
		dataDisplays.add(new MiniSpectrogram(bfLocaliserControl, 0));
		if (groupProcs != null) for (BeamGroupProcess groupProc:groupProcs) {
			BeamAlgorithmParams algoParams = groupProc.getAlgorithmParams();
			int[] slants = algoParams.getBeamOGramSlants();
			boolean hasSlant = (slants != null && slants[1] > slants[0]);
			int arrayShape = groupProc.getArrayShape();
			if (hasSlant && arrayShape >= ArrayManager.ARRAY_TYPE_PLANE) {
				dataDisplays.add(new MiniBeamOGram2D(bfLocaliserControl, algoParams.getChannelMap()));
			}
			else {
				dataDisplays.add(new MiniBeamOGram(bfLocaliserControl, algoParams.getChannelMap()));
			}
		}
		SplitPane mainNode;
//		HBox hBox = new HBox(dataDisplays.size());
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		for (BeamDataDisplay bds:dataDisplays) {
//			hBox.getChildren().add(bds.getNode());
			splitPane.getItems().add(bds.getNode());
		}
		ObservableList<Divider> dividers = splitPane.getDividers();
		int n = dividers.size();
		for (int i = 0; i < n; i++) {
		    dividers.get(i).setPosition((i + 1.0) / (n+1));
		}

		Pane mainPane = new BorderPane(mainNode = splitPane);
		
//		mainScene = new Scene(mainPane);
		mainPane.setManaged(true);
		mainScene.setRoot(mainPane);
		InvalidationListener listener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
//				h.widthProperty().
//				double oldh = p.getHeight();
//				double oldw = p.getWidth();
				double w = mainScene.getWidth();
				double h = mainScene.getHeight();

				mainNode.setMaxWidth(w);
				mainNode.setMaxHeight(h);
				mainNode.setMinWidth(w);
				mainNode.setMinHeight(h);	
				mainNode.setPrefHeight(w);
				mainNode.setPrefWidth(w);
//				System.out.printf("Set scene width %3.1f height %3.1f  and %3.1f,%3.1f\n", 
//						mainScene.getWidth(), mainScene.getHeight(),  mainNode.getWidth(), mainNode.getHeight());
			}
		};
		mainScene.widthProperty().addListener(listener);
		mainScene.heightProperty().addListener(listener);
	}

	/**
	 * @return the mainScene
	 */
	public Scene getMainScene() {
		return mainScene;
	}

	private class DataObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return uniqueDisplayName;
		}

		/* (non-Javadoc)
		 * @see PamguardMVC.PamObserverAdapter#update(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
		 */
		@Override
		public void addData(PamObservable o, PamDataUnit pamDataUnit) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
//					miniSpec.newDataUnit(o, pamDataUnit);
				}
			});
		}

	}

	public void update(BeamLocaliserData beamLocData) {
//		beamLocData.
		for (BeamDataDisplay beamDataDisplay:dataDisplays) {
			if (beamDataDisplay.getChannelMap() != 0 && (beamDataDisplay.getChannelMap() & beamLocData.getChannelBitmap()) == 0) {
				continue;
			}
			beamDataDisplay.update(beamLocData);
		}
	}

	public void updateSettings() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				createDisplays();
			}
		});
	}
	
}
