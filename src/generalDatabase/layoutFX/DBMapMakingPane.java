package generalDatabase.layoutFX;

import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import PamController.SettingsPane;
import generalDatabase.CreateMapInfo;

/**
 * Pane which shows progress in mapping the database.
 * @author Jamie Macaulay
 */
public class DBMapMakingPane extends SettingsPane<CreateMapInfo> {
	
	private Label databaseName;
	
	private ProgressBar streamProgress;
	
	private Label streamName;
	
	PamBorderPane mainPane;
	
	public DBMapMakingPane(){
		super(null); 
		mainPane=new PamBorderPane(); 
		mainPane.setCenter(createDBMapPane());
	}
	
	private Pane createDBMapPane(){
		PamVBox pamVBox=new PamVBox();
		pamVBox.setSpacing(5);
		
		databaseName=new Label();
		streamProgress=new ProgressBar(); 
		streamName=new Label(); 
		
		pamVBox.getChildren().addAll(databaseName, streamProgress, streamName);
		
		return pamVBox; 
		
	}

	
	public void newData(CreateMapInfo mapInfo) {
		switch (mapInfo.getStatus()) {
		case CreateMapInfo.BLOCK_COUNT:
			databaseName.setText(mapInfo.getDatabaseName());
			streamProgress.setProgress(0);
			break;
		case CreateMapInfo.START_TABLE:
			streamName.setText(mapInfo.getTableName());
			streamProgress.setProgress(mapInfo.getTableNum()+1/(double) mapInfo.getNumBlocks());
		}
	}

	@Override
	public void setParams(CreateMapInfo input) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		return "Database Data Mapping";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CreateMapInfo getParams(CreateMapInfo currParams) {
		// TODO Auto-generated method stub
		return null;
	}

}

