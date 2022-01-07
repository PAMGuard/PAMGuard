package bearinglocaliser.display;

import java.util.ArrayList;

import PamUtils.SimpleObserver;
import PamguardMVC.PamDataUnit;
import beamformer.localiser.plot.BeamDataDisplay;
import bearinglocaliser.BearingAlgorithmGroup;
import bearinglocaliser.BearingLocaliserControl;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import userDisplay.UserDisplayControl;

public class FXBearingDisplay implements SimpleObserver<PamDataUnit> {

	private BearingDisplayProvider bearingDisplayProvider;
	private BearingLocaliserControl bearingLocaliserControl;
	private UserDisplayControl userDisplayControl;
	private String uniqueDisplayName;
	private Scene mainScene;
	private ArrayList<BearingDataDisplay> bearingDataDisplays = new ArrayList<>();

	public FXBearingDisplay(BearingDisplayProvider bearingDisplayProvider,
			BearingLocaliserControl bearingLocaliserControl, UserDisplayControl userDisplayControl,
			String uniqueDisplayName) {
		this.bearingDisplayProvider = bearingDisplayProvider;
		this.bearingLocaliserControl = bearingLocaliserControl;
		this.userDisplayControl = userDisplayControl;
		this.uniqueDisplayName = uniqueDisplayName;

		mainScene = new Scene(new BorderPane());

		createDisplays();
	}

	public Scene getMainScene() {
		return mainScene;
	}

	private void createDisplays() {
		
		if (Platform.isFxApplicationThread() == false) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					createDisplays();
				}
			});
		}

		bearingDataDisplays.clear();
		// @TODO Add standard displays.

		/*
		 *  get a list of currently available algorithms ...
		 *  Note that not every algorithm will necessarily have a display 
		 *  (especially during development)
		 */
		BearingAlgorithmGroup[] algoGroups = bearingLocaliserControl.getBearingProcess().getBearingAlgorithmGroups();
		if (algoGroups != null) {
			for (int i = 0; i < algoGroups.length; i++) {
				if (algoGroups[i] == null) continue;
				BearingDataDisplay bdd = algoGroups[i].getDataDisplay();
				if (bdd != null) {
					bearingDataDisplays.add(bdd);
				}
			}
		}
		SplitPane mainNode;
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		for (BearingDataDisplay bds:bearingDataDisplays) {
			splitPane.getItems().add(bds.getNode());
		}
		ObservableList<Divider> dividers = splitPane.getDividers();
		int n = dividers.size();
		for (int i = 0; i < n; i++) {
		    dividers.get(i).setPosition((i + 1.0) / (n+1));
		}

		Pane mainPane = new BorderPane(mainNode = splitPane);

		mainPane.setManaged(true);
		mainScene.setRoot(mainPane);
		InvalidationListener listener = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
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
	}

	@Override
	public void updateSettings() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				createDisplays();
			}
		});
	}

	@Override
	public void update(PamDataUnit newData) {
		// TODO Auto-generated method stub
		
	}

}
