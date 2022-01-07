package Array.sensors.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.JPanel;

import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.ArraySensorDataBlock;
import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import Array.swing.sidepanel.ArrayNumberDisplay;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamUtils;
import PamView.PamSlider;
import PamView.dialog.SourcePanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScrollSlider;
import pamScrollSystem.PamScroller;
import pamViewFX.PamSettingsMenuPane;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;

/**
 * HPR on a user display. 
 * @author dg50
 *
 */
public class ArraySensorComponent implements UserDisplayComponent, PamSettings, ArrayDisplayParamsProvider {
	
	private String uniqueName; 
	
	private PamPanel mainPanel;
	
	private HeadingComponent headingComponent;
	
	private PitchRollComponent pitchRollComponent;
	
	private DepthComponent depthComponent;
	
	private SourcePanel sensorSources;
	
	private PamDataBlock sensorDataBlock;
	
	private SensorObserver sensorObserver;
	
	private SensorDisplayParameters sensorDisplayParams = new SensorDisplayParameters();

	private PamPanel hprPanel;
	
	private PamScrollSlider viewerSlider;

	private boolean isViewer;	
	
	public ArraySensorComponent(ArraySensorPanelProvider arraySensorPanelProvider,
			UserDisplayControl userDisplayControl, String uniqueName) {
		this.uniqueName = uniqueName;
		
		sensorSources = new SourcePanel(null, ArraySensorDataUnit.class, false, true);
		
		mainPanel = new PamPanel(new BorderLayout());
		JPanel leftPanel = new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, leftPanel);
		leftPanel.add(BorderLayout.NORTH, sensorSources.getPanel());
		hprPanel = new PamPanel(new BorderLayout());
		leftPanel.add(BorderLayout.CENTER, hprPanel);
		hprPanel.add(BorderLayout.NORTH, headingComponent = new HeadingComponent(this, 1));
		hprPanel.add(BorderLayout.CENTER, pitchRollComponent = new PitchRollComponent(this, 1));
		hprPanel.add(BorderLayout.EAST, depthComponent = new DepthComponent(this, 1));
//		JPanel topPanel = new PamPanel(new BorderLayout());
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		if (isViewer) {
			viewerSlider = new PamScrollSlider(uniqueName, PamScroller.HORIZONTAL, 1000, 600000L, true);
			mainPanel.add(BorderLayout.SOUTH, viewerSlider.getComponent());
			viewerSlider.addObserver(new SensScrollObserver());
		}
		
		sensorObserver = new SensorObserver();
		sensorSources.addSelectionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newSensorSource();
			}
		});
		
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	
	

	/**
	 * Called when selection made in drop down list. 
	 */
	protected void newSensorSource() {
		if (sensorDataBlock != null) {
			sensorDataBlock.deleteObserver(sensorObserver);
			if (viewerSlider != null) {
				viewerSlider.removeDataBlock(sensorDataBlock);
			}
		}
		sensorDataBlock = sensorSources.getSource();
		
		rebuildDisplay(sensorDataBlock);
		
		if (sensorDataBlock != null) {
			if (viewerSlider != null) {
				viewerSlider.addDataBlock(sensorDataBlock);
				viewerSlider.reLoad();
			}
			else {
				sensorDataBlock.addObserver(sensorObserver);
			}
		}
	}
	
	// called after restore settings. 
	private void setSensorSource() {
		PamDataBlock dataBlock = PamController.getInstance().getDataBlockByLongName(sensorDisplayParams.dataSource);
		if (dataBlock != null) {
			sensorSources.setSource(dataBlock);
		}
	}

	private void rebuildDisplay(PamDataBlock dataBlock) {
		hprPanel.removeAll();
		int dispMap = 1;
		boolean showHead = true, showPR = true, showHeight = true;
		if (dataBlock != null) {
			ArraySensorDataBlock sensDataBlock = (ArraySensorDataBlock) dataBlock;
			dispMap = PamUtils.makeChannelMap(Math.max(1,sensDataBlock.getNumSensorGroups()));
			showHead = sensDataBlock.hasSensorField(ArraySensorFieldType.HEADING);
			showPR = sensDataBlock.hasSensorField(ArraySensorFieldType.PITCH) | sensDataBlock.hasSensorField(ArraySensorFieldType.ROLL);
			showHeight = sensDataBlock.hasSensorField(ArraySensorFieldType.HEIGHT);
		}
		headingComponent = new HeadingComponent(this, dispMap);
		pitchRollComponent = new PitchRollComponent(this, dispMap);
		depthComponent = new DepthComponent(this, dispMap);
				
		hprPanel.add(BorderLayout.NORTH, headingComponent);
		if (showPR) {
			hprPanel.add(BorderLayout.CENTER, pitchRollComponent);
			hprPanel.add(BorderLayout.EAST, depthComponent);
		}
		else {
			hprPanel.add(BorderLayout.CENTER, depthComponent);
		}
		
		headingComponent.setVisible(showHead);
		pitchRollComponent.setVisible(showPR);
		depthComponent.setVisible(showHeight);
		
	}

	private class SensorObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return getUniqueName();
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			if (pamDataUnit instanceof ArraySensorDataUnit) {
				newSensorData(pamDataUnit);
			}
			
		}
		
	}
	
	private class SensScrollObserver implements PamScrollObserver {

		@Override
		public void scrollValueChanged(AbstractPamScroller abstractPamScroller) {
			scrollChanged(viewerSlider.getValueMillis());
		}

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			scrollChanged(viewerSlider.getValueMillis());
		}
		
	}
	
	@Override
	public Component getComponent() {
		return mainPanel;
	}

	public void scrollChanged(long valueMillis) {
		if (sensorDataBlock == null) {
			return;
		}
		int sensMap = Math.max(sensorDataBlock.getChannelMap(), 1);
		int[] chans = PamUtils.getChannelArray(sensMap);
		for (int i = 0; i < chans.length; i++) {
			PamDataUnit closest = sensorDataBlock.getClosestUnitMillis(valueMillis, 1<<chans[i]);
			if (closest instanceof ArraySensorDataUnit) {
				newSensorData(closest);
			}
		}
		PamDataUnit closest = sensorDataBlock.getClosestUnitMillis(valueMillis, sensMap);
		if (closest instanceof ArraySensorDataUnit) {
			newSensorData(closest);
		}
	}

	public void newSensorData(PamDataUnit pamDataUnit) {
		ArraySensorDataUnit sensDataUnit = (ArraySensorDataUnit) pamDataUnit;
		int chanMap = pamDataUnit.getChannelBitmap();
		if (chanMap == 0) {
			chanMap = 1;
		}
		int nChan = PamUtils.getNumChannels(chanMap);
		for (int i = 0; i < nChan; i++) {
			int aChan = PamUtils.getNthChannel(i, chanMap);	

			headingComponent.setSensorData(aChan, sensDataUnit);
			
			pitchRollComponent.setSensorData(aChan, sensDataUnit);
			
			depthComponent.setSensorData(aChan, sensDataUnit);
		}
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			newSensorSource();
		}		
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;		
	}

	@Override
	public String getFrameTitle() {
		return uniqueName;
	}

	@Override
	public String getUnitName() {
		return uniqueName;
	}

	@Override
	public String getUnitType() {
		return "Sensor Orientation Display";
	}

	@Override
	public Serializable getSettingsReference() {
		return sensorDisplayParams;
	}

	@Override
	public long getSettingsVersion() {
		return SensorDisplayParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		sensorDisplayParams = ((SensorDisplayParameters) pamControlledUnitSettings.getSettings()).clone();
		setSensorSource();
		setFunnyImage();
		newSensorSource();
		return true;
	}

	private void setFunnyImage() {
		if (sensorDisplayParams == null) {
			return;
		}
		pitchRollComponent.loadFunnyImage();
	}

	@Override
	public ArrayDisplayParameters getDisplayParameters() {
		return sensorDisplayParams.getArrayDisplayParams();
	}

	@Override
	public boolean showDisplayParamsDialog(Window window) {
		return ArrayDisplayParamsDialog.showDialog(window, this);
	}

	@Override
	public void setDisplayParameters(ArrayDisplayParameters displayParameters) {
		sensorDisplayParams.setArrayDisplayParams(displayParameters);
	}

}
