package pamViewFX.pamTask;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamVBox;

/**
 * Pane which shows users load progress from ALL PAMGuard loading tasks. These
 * might include:
 * <p>
 * <p>
 * Loading Data Maps.
 * <p>
 * Loading Binary files
 * <p>
 * Importing data.
 * <p>
 * Batch processing in viewer mode.
 * <p>
 * <p>
 * The pane receives a PAMTaskUpdate from a running thread. This contains
 * information on what type of loading is occurring, what the progress is and
 * update messages which should be displayed. The pane creates a new progress
 * bar for each new PAMTaskUpdate it receives but ONLY if that update is from a
 * running thread which does not already have a progress bar
 * 
 * @author Jamie Macaulay
 *
 */
public class PamLoadingPane extends PamVBox {
	
	/**
	 * List of current task panes. 
	 */
	ObservableList<PamTaskPane> taskPanes =FXCollections.observableArrayList();
	
	/*
	 *Reference to the PamGuiManagerFX
	 */
	private PamGuiManagerFX pamGuiManager;
	
	/**
	 * Time for loading before load pane showsn.
	 */
	private long waitBeforeShiow=1000; // wait one seconds before a load 
	
	
	public PamLoadingPane(PamGuiManagerFX pamGuiManager){
		this.pamGuiManager=pamGuiManager;
		this.setSpacing(20);
		this.setPadding(new Insets(30,0,0,0));
	}
	
	/**
	 * Called whenever an update from a loading task 
	 * @param taskUpdate
	 */
	public void updateLoadPane(PamTaskUpdate taskUpdate){
		
//		System.out.println("New PAMTask Update: " + taskUpdate.getName() + " Progress: "  + taskUpdate.getProgress() + " Status: "+ taskUpdate.getStatus() + " Progress: "  + taskUpdate.getProgressString());
		
		//first, figure out if there there is a PamTaskPane for the update; 
		PamTaskPane pamTaskPane = null;
		for (int i=0; i<taskPanes.size(); i++){
			if (taskPanes.get(i).getTaskName()==taskUpdate.getName()){
				pamTaskPane=taskPanes.get(i);
				continue;
			}
		}
		
		//now have correct taskPane or null. 
		if (pamTaskPane!=null){
			pamTaskPane.setCurrentTaskUpdate(taskUpdate); 
		}
		else {
			//create a new PamTaskPane and add it to the list
			PamTaskPane newPamTaskPane;
			//if a single update need a pane with just one progress bar
			if (!taskUpdate.isDualProgressUpdate()){
				newPamTaskPane=new PamTaskPane(taskUpdate);
			}
			//if a dual update then need a pane with two progress bars. 
			else {
				newPamTaskPane=new DualPamTaskPane(taskUpdate);
			}
			this.taskPanes.add(newPamTaskPane);
			this.getChildren().add(newPamTaskPane);

		}

		//now check if any task are done. If so remove them from both the list and the pane. 
		removeDoneTasks();

		//check whether the pane should be showing
		checkPaneShow();

	}
	
	/**
	 * Remove panes with task that have finsihed. 
	 */
	private void removeDoneTasks(){
		for (int i=0; i<taskPanes.size(); i++){
			if (taskPanes.get(i).getCurrentTaskUpdate().getStatus()==PamTaskUpdate.STATUS_DONE){
				//remove this task from the pane. 
				this.getChildren().remove(taskPanes.get(i)); 
				taskPanes.remove(taskPanes.get(i));
			}
		}
	}
	
	
	Timeline timeline; 
	/**
	 * Check whether the loading pane should be visible or not
	 */
	private void checkPaneShow(){
		if (taskPanes.size()<=0) pamGuiManager.showLoadMode(false);
//		else  pamGuiManager.showLoadMode(true);
		else {
			if (timeline==null || timeline.getStatus()!=Status.RUNNING) {
				timeline = new Timeline(new KeyFrame(
						Duration.millis(waitBeforeShiow),
						ae ->{
							if (taskPanes.size()>0) {
								Platform.runLater(()-> pamGuiManager.showLoadMode(true)); 
							}
						}));
				timeline.play();
			}
		}
	}
	
	/**
	 * Shows the progress of one task. 
	 * @author Jamie Macaulay
	 *
	 */
	private class PamTaskPane extends PamVBox {
		
	
		protected PamTaskUpdate currentTaskUpdate;
		
		/**
		 * The name of the task. 
		 */
		private String taskName;

		/**
		 * Progress bar to show progress.
		 */
		private ProgressBar progressBar;
		

		/**
		 * Shows a message which informas on loading
		 */
		private Label messageLabel; 

		PamTaskPane(PamTaskUpdate taskUpdate){
			this.currentTaskUpdate=taskUpdate;
			this.taskName=taskUpdate.getName();
			
			//create title
			this.getChildren().add(new Label(taskName));
			
			//create progress bar
			progressBar=new ProgressBar();
			progressBar.prefWidthProperty().bind(this.widthProperty());

			this.getChildren().add(progressBar);
			
			//create message label
			this.getChildren().add(messageLabel=new Label()); 

			//set padding so not edge to edge progress bar. 
			this.setPadding(new Insets(0,10,0,10));

		}
	
		/**
		 * Get the name of the task. Note that this is the task updates unique identifier and should not be the same for tasks
		 * running at the same time. 
		 * @return the task name. 
		 */
		public String getTaskName() {
			return taskName;
		}

		/**
		 * Get the last update
		 * @return the last update
		 */
		public PamTaskUpdate getCurrentTaskUpdate() {
			return currentTaskUpdate;
		}

		/**
		 * Set the current state of the task. Updates the progress bar and message. 
		 * @param currentTaskUpdate - update task. 
		 */
		public void setCurrentTaskUpdate(PamTaskUpdate currentTaskUpdate) {
			this.currentTaskUpdate = currentTaskUpdate;
			progressBar.setProgress(currentTaskUpdate.getProgress());
			messageLabel.setText(currentTaskUpdate.getProgressString());
		}
		
		/**
		 * Get the progress bar. 
		 * @return the progress bar. 
		 */
		protected ProgressBar getProgressBar() {
			return progressBar;
		}

		
	}
	
	/**
	 * A pane which shows updates from a single Task whihc contains an overall and fine scale progress. 
	 * @author Jamie Macaulay
	 *
	 */
	class DualPamTaskPane extends PamTaskPane {
		
		private ProgressBar progressBar2;
		
		private Label message2;


		public DualPamTaskPane(PamTaskUpdate taskUpdate) {
			super(taskUpdate);
			//need to add another progress bar underneath
			progressBar2=new ProgressBar();
			progressBar2.prefWidthProperty().bind(this.widthProperty());

			message2=new Label(); 
			this.getChildren().addAll(progressBar2,message2); 
		}
		
		/**
		 * Set the current state of the task. Updates the progress bar and message. 
		 * @param currentTaskUpdate - update task. 
		 */
		public void setCurrentTaskUpdate(PamTaskUpdate currentTaskUpdate) {
			//set overall progress
			super. setCurrentTaskUpdate(currentTaskUpdate);
			//also update the detailed progress bar
			progressBar2.setProgress(currentTaskUpdate.getProgress2());
			message2.setText(currentTaskUpdate.getProgressString2());

		}


		
	}

}
