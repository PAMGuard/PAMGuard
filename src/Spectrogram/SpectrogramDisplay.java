/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package Spectrogram;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import Acquisition.AcquisitionProcess;
import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.Coordinate3d;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamUtils.SIUnitFormat;
import PamUtils.complex.ComplexArray;
import PamView.ClipboardCopier;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamView;
import PamView.dialog.PamLabel;
import PamView.hidingpanel.HidingDialogPanel;
import PamView.panel.CornerLayout;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamPanel;
import PamView.paneloverlay.OverlayDataInfo;
import PamView.paneloverlay.overlaymark.ExtMouseAdapter;
import PamView.paneloverlay.overlaymark.MarkRelationships;
import PamView.paneloverlay.overlaymark.MarkRelationshipsData;
import PamView.paneloverlay.overlaymark.OverlayMark;
import PamView.paneloverlay.overlaymark.OverlayMark.OverlayMarkType;
import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;
import PamView.paneloverlay.overlaymark.OverlayMarkProviders;
import PamView.paneloverlay.overlaymark.OverlayMarker;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.SimpleDataObserver;
import PamguardMVC.dataOffline.OfflineDataLoading;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import dataPlotsFX.data.DataTypeInfo;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import ltsa.LtsaDataBlock;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScroller;
import pamScrollSystem.PamScrollerData;
import pamScrollSystem.RangeSpinner;
import pamScrollSystem.RangeSpinnerListener;
import pamguard.GlobalArguments;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackProgressMonitor;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayFrame;
import userDisplay.UserFramePlots;

public class SpectrogramDisplay extends UserFramePlots implements PamObserver, LoadObserver,
InternalFrameListener, DisplayPanelContainer, SpectrogramParametersUser, PamSettings {

	private SpectrogramParameters spectrogramParameters;

	private SpectrogramParameters oldLayoutParameters;

	private SpectrogramOuterPanel spectrogramOuterPanel;

	private SpectrogramPanel[] spectrogramPanels;

	private SpectrogramAxis spectrogramAxis;

	private AmplitudePanel amplitudePanel;

	private SpectrogramPlotPanel spectrogramPlotPanel;

	private FFTDataBlock sourceFFTDataBlock;

	private PamRawDataBlock sourceRawDataBlock;

	private SpectrogramDisplay spectrogramDisplay;

	private BufferedImage amplitudeImage;

	private double[][] colorValues;

	private ColourArray colourArray;

	private double[] overlayColour = { 255, 0, 0 };

	private static int instanceCount;

	private float sampleRate = 1;

	private PamAxis timeAxis, frequencyAxis, amplitudeAxis;

	private int[] freqBinRange = new int[2];
	private int[] freqBinDisplayRange = new int[2];

	private double scaleX;

	private double scaleY;

	private Dimension panelSize;

	private Rectangle r;

	private int xAxisExtent, yAxisExtent;
	
	/**
	 * Params for fading Azigram by intensity
	 * Use a linear fade between fadeStart and fadeStop when dB levels lower than threshold
	 * TODO: Create GUI so user can control these params		 * 
	 */
	double fadeThreshold = 90; 			 // Start to fade below this amount (dB)
	double fadeFloor = fadeThreshold-20; // Everything same solid color below this amount
	double fadeStart = 0;
	double fadeStop = 255;
	double[] fadeFloorColor = new double[] {0,0,0};

	private SpectrogramProjector spectrogramProjector;

	private boolean firstUpdate = true;

	private ArrayList<PamDataBlock> detectorDataBlocks;

	//	private MouseAdapter popupListener;

	private int nMarkObservers;

	private PamScroller viewerScroller;

	private Color freezeColour = Color.RED;

	private Color freezeColour2 = new Color(0, 198, 0);

	private boolean frozen = false;

	private PamLabel frequencyLabel = new PamLabel();

	private Object innerPanelSynchObject = new Object();

	/*
	 * Remember where the mouse was pressed and released. 
	 */
	private Point mouseDownPoint, currentMousePoint;
	private long mouseDownTime;

	protected ClipboardCopier panelClipBoardCopier;

	private boolean viewerMode, netRXMode;

	private UserDisplayControl userDisplayControl;

	private RangeSpinner rangeSpinner;

	private long masterClockMilliseconds;

	private long masterClockSamples;

	private int spectrogramPixelOverlap = 1;

	private SpectrogramHidingPanel hidingPanel;

	private HidingDialogPanel hidingDialogPanel;

	//	private OverlayMarker overlayMarker;

	private SpectrogramDisplayComponent specDisplayComponent;

	private SpectrogramScrollJumper scrollJumper;


	public SpectrogramDisplay(UserDisplayControl userDisplayControl, SpectrogramDisplayComponent specDisplayComponent, 
			SpectrogramParameters oldSpecParameters) {

		super(userDisplayControl);

		this.specDisplayComponent = specDisplayComponent;
		this.userDisplayControl = userDisplayControl;

		/**
		 * Three possibilities here - if spectrgroamParameters is no tnull
		 * then this is being created from an old config, s odo nothing, this
		 * will only happen once when an old config is loaded.  
		 * otherwise it may have settnigs of it's own through the 'normal'
		 * channels, otherwise if it's a brand new spectrogram they will 
		 * be null so will have to ask for some. 
		 */
		if (oldSpecParameters != null) {
			this.spectrogramParameters = oldSpecParameters.clone();
		}
		//		else {
		// this should result in settings being loaded if they exist. 
		PamSettingManager.getInstance().registerSettings(this); // always need to register, even if we're using old parameters
		//		}
		boolean isBatch = GlobalArguments.getParam("-batch") != null;
		if (spectrogramParameters == null && !isBatch) {
			this.spectrogramParameters = new SpectrogramParameters();
			PamView view = userDisplayControl.getPamView();
			if (view != null) {
				SpectrogramParameters newParams = SpectrogramParamsDialog
						.showDialog(userDisplayControl.getGuiFrame(), spectrogramPanels, spectrogramParameters);
				if (newParams != null) {
					this.spectrogramParameters = newParams;
				}
			}
		}
		if (spectrogramParameters == null) {
			/*
			 *  this can happen in batch mode if a display was added.
			 *  Hopefully not a problem, but may need to set some parameters to 
			 *  set display up correctly.  
			 */
			spectrogramParameters = new SpectrogramParameters();
		}

		spectrogramDisplay = this;

		viewerMode = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
		netRXMode = (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER);
		if (viewerMode || netRXMode) {
			viewerScroller = new PamScroller(getFullTitle(), AbstractPamScrollerAWT.HORIZONTAL,
					1, 10000, true);
			viewerScroller.addObserver(new ViewScrollObserver());
			if (viewerMode) {
				viewerScroller.coupleScroller(userDisplayControl.getUnitName());
			}
			rangeSpinner = new RangeSpinner();
			rangeSpinner.addRangeSpinnerListener(new SpecTimeRangeListener());
			viewerScroller.addControl(rangeSpinner.getComponent());
			viewerScroller.addObserver(rangeSpinner);
			//TODO- set a better range of spinner values. 
			rangeSpinner.setSpinnerValue(this.spectrogramParameters.displayLength);
			//			viewerScroller.setRangeMillis(minimumMillis, maximumMillis, notify);
			//			viewerRangeSpinner = new RangeSpinner();
			//			viewerScroller.addControl(viewerRangeSpinner.getComponent());
			//			viewerRangeSpinner.addRangeSpinnerListener(new ViewRangeSpinnerListener());
		}

		createColours();

		spectrogramProjector = new SpectrogramProjector(this);

		//create the side panel
		hidingPanel=new SpectrogramHidingPanel(this);
		//		hidingPanel.getPanel().showPanel(!spectrogramParameters.hideSidePanels);

		setAxisPanel(spectrogramAxis = new SpectrogramAxis());
		setPlotPanel(spectrogramPlotPanel = new SpectrogramPlotPanel());
		setEastPanel(amplitudePanel = new AmplitudePanel());

		if (viewerMode) {
			viewerScroller.addMouseWheelSource(spectrogramPlotPanel);
			viewerScroller.addMouseWheelSource(spectrogramAxis);
		}



		//		for (int i = 0; i < spectrogramPanels.length; i++) {
		//			spectrogramPanels[i].addMouseListener(popupListener);
		//		}

		oldLayoutParameters = spectrogramParameters.clone();
		setParams(spectrogramParameters, true);

		if (viewerScroller != null) {
			spectrogramAxis.requestFocusInWindow();
			scrollJumper = new SpectrogramScrollJumper(this, viewerScroller, spectrogramAxis);
			scrollJumper.addSelectObserver(spectrogramAxis);
		}

		//		overlayMarker = new SpectrogramMarker(this, 0, spectrogramProjector);
		//		OverlayMarkProviders.singleInstance().addProvider(overlayMarker);
	}


	/**
	 * Class to make sure the layered pane properly repaints oberlayed buttons. 
	 * @author Jamie Macaulay
	 *
	 */
	class SpecLayeredPane extends JLayeredPane{

		private static final long serialVersionUID = 1L;

		@Override
		public void repaint(){
			super.repaint();
			repaintButtons();
		}

		@Override
		public void repaint(long millis){
			super.repaint(millis);
			repaintButtons();
		}

		protected void repaintButtons(){
			if (hidingDialogPanel!=null) hidingDialogPanel.getShowButton().repaint();
		}
	}

	class SpecTimeRangeListener implements RangeSpinnerListener {

		@Override
		public void valueChanged(double oldValue, double newValue) {
			if (oldValue == newValue || spectrogramParameters.displayLength == newValue) {
				return;
			}
			spectrogramParameters.displayLength = newValue;
			spectrogramParameters.timeScaleFixed = true;
			setParams(spectrogramParameters, false);
			newScrollPosition();
			//repaintAll();
		}

	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	@Override
	public String getName() {
		return spectrogramParameters.sourceName;
	}

	/**
	 * Called when amplitude range is changed on the hiding amplitude range slider.
	 */
	public void setAmplitudeParams() {
		if (spectrogramPanels == null) return;
		for (int i = 0; i < spectrogramPanels.length; i++) {
			spectrogramPanels[i].updateImageAmplitude();
		}
		//		repaint the side bar here too !
		if (amplitudeAxis != null) {
			amplitudeAxis.setRange(spectrogramParameters.amplitudeLimits[0], spectrogramParameters.amplitudeLimits[1]);
		}
		paintAmplitudeImage();
		repaint(100);
	}

	long lastComputerTime=0;

	private double freqAxisScale;

	//	/**
	//	 * Called when amplitude range is changed on the hiding amplitude range slider. Note there is a timer here which allows this to only be called if a certain time has passed since the last call.
	//	 *@param millis- time to wait before this function repaints amplitude image again. 
	//	 */
	//	public void setAmplitudeParams(long millis){
	//		long currentTime=System.currentTimeMillis();
	//		if (currentTime-lastComputerTime<millis){
	//			return;
	//		}
	//		lastComputerTime=currentTime;
	//		setAmplitudeParams(); 
	//	}
	/**
	 * Checks the frequency range range to be displayed. 
	 */
	public void calcFrequencyRangeDisplay(){
		// check the frequency bins ...
		if (spectrogramParameters == null || sourceFFTDataBlock == null) {
			return;
		}
		double[] freqLimits = spectrogramParameters.frequencyLimits;
		double freqStart = 0;
		int nBins = sourceFFTDataBlock.getDataWidth(0);
		if (sourceFFTDataBlock != null) {
			freqStart = sourceFFTDataBlock.getMinDataValue();
			freqLimits[0] = Math.max((double) freqLimits[0], sourceFFTDataBlock.getMinDataValue());
			freqLimits[1] = Math.min((double) freqLimits[1], sourceFFTDataBlock.getMaxDataValue());
		}
		freqBinRange[0] = (int) Math.floor((spectrogramParameters.frequencyLimits[0]-freqStart)
						* sourceFFTDataBlock.getFftLength() / sampleRate);
		freqBinRange[1] = (int) Math.ceil((spectrogramParameters.frequencyLimits[1]-freqStart)
				* sourceFFTDataBlock.getFftLength() / sampleRate);
		if (sourceFFTDataBlock != null) {
			freqBinRange[1] = Math.min(freqBinRange[1], sourceFFTDataBlock.getDataWidth(0));
		}
		for (int i = 0; i < 2; i++) {
			freqBinRange[i] = Math.min(Math.max(freqBinRange[i], 0),
					sourceFFTDataBlock.getFftLength() / 2 - 1);
			freqBinDisplayRange[1-i] = nBins-freqBinRange[i];
			//			freqBinDisplayRange[i] = freqBinRange[i];
		}
	}


	public void setParams(SpectrogramParameters newParameters, boolean fullLayout) {
		if (!PamController.getInstance().isInitializationComplete()) {
			return;
		}

		this.spectrogramParameters = newParameters.clone();

		detectorDataBlocks = PamController.getInstance().getDataBlocks(PamDataUnit.class, true);

		// count the number of SpectrogramMarkObservers
		//		nMarkObservers = 0;
		//		if (spectrogramParameters.useSpectrogramMarkObserver != null) {
		//			for (int i = 0; i < Math.min(spectrogramParameters.useSpectrogramMarkObserver.length,
		//					SpectrogramMarkObservers.getSpectrogramMarkObservers().size()); i++) {
		//				if (spectrogramParameters.useSpectrogramMarkObserver[i]) {
		//					nMarkObservers ++;
		//				}
		//			}
		//		}

		//		ParameterType[] paramTypes = {ParameterType.TIME, ParameterType.FREQUENCY};
		//		ArrayList<OverlayMarker> markers = markProviders.getMarkProviders(paramTypes);
		//		markObservers.getMarkObservers()

		//		if (oldLayoutParameters.nPanels != spectrogramParameters.nPanels ||
		//		oldLayoutParameters.showWaveform != spectrogramParameters.showWaveform) {
		if (fullLayout) {
			spectrogramPlotPanel.layoutPlots();
		}

		/**
		 * New framework for handling overlay markers. 
		 */
		MarkRelationships markLinks = MarkRelationships.getInstance();
		ArrayList<OverlayMarkObserver> markObservers = OverlayMarkObservers.singleInstance().getMarkObservers();
		if (spectrogramPanels != null) {
			nMarkObservers = 0;
			for (int i = 0; i < markObservers.size(); i++) {
				OverlayMarkObserver markObserver = markObservers.get(i);
				for (SpectrogramPanel aPanel : spectrogramPanels) {
					if (markLinks.getRelationship(aPanel.getMarker(), markObserver)) {
						aPanel.getMarker().addObserver(markObserver);
						nMarkObservers++;
					}
					else {
						aPanel.getMarker().removeObserver(markObserver);
					}
				}
			}
		}
		oldLayoutParameters = spectrogramParameters.clone();
		//		}

		if (getFrame() != null) {
			getFrame().setTitle(specDisplayComponent.getFrameTitle());
			//			this.getFrame().setTitle(spectrogramParameters.sourceName);
		}

		//		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance()
		//				.getFFTDataBlocks();
		//
		//		if (spectrogramParameters.fftBlockIndex >= 0
		//				&& spectrogramParameters.fftBlockIndex < fftBlocks.size()) {
		//			if (sourceFFTDataBlock != (FFTDataBlock) fftBlocks.get(spectrogramParameters.fftBlockIndex)) {
		//				if (sourceFFTDataBlock != null) {
		//					sourceFFTDataBlock.deleteObserver(this);
		//				}
		//				sourceFFTDataBlock = (FFTDataBlock) fftBlocks.get(spectrogramParameters.fftBlockIndex);
		//				sourceFFTDataBlock.addObserver(this);
		//			}
		//			sampleRate = sourceFFTDataBlock.getSampleRate();
		//
		//			//			PamProcess pamProcess = (PamProcess) fftDataSource;
		//			if (sourceFFTDataBlock != null) {
		//				//				spectrogramParameters.fftHop = sourceFFTDataBlock.getFftHop();
		//				//				spectrogramParameters.fftLength = sourceFFTDataBlock.getFftLength();
		//				spectrogramParameters.sourceName = sourceFFTDataBlock.getLongDataName();
		//			}
		//			else {
		//				System.out.println();
		//			}
		//		}
		//		if (sourceFFTDataBlock == null) {
		//			//System.out.println("No source data block for spectrogram " + getName());
		//		}
		FFTDataBlock newFFTDataBlock = findFFTDataBlock(spectrogramParameters);
		if (sourceFFTDataBlock != newFFTDataBlock) {
			if (sourceFFTDataBlock != null) {
				sourceFFTDataBlock.deleteObserver(this);
			}
			sourceFFTDataBlock = newFFTDataBlock;
			if (sourceFFTDataBlock != null) {
				sourceFFTDataBlock.addObserver(this);
				sampleRate = sourceFFTDataBlock.getSampleRate();
			}
		}

		subscribeRawDataBlock();

		if (sourceFFTDataBlock == null) {
			return;
		}

		//check the frequency range to be displayed.
		calcFrequencyRangeDisplay();

		// now make the image - work out the required width and height
		try {
		createAllImages();
		}
		catch (Exception e) {
			
		}

		createColours();

		paintAmplitudeImage();


		if (viewerMode) {
			newScrollPosition();
		}

		hidingPanel.setParams(spectrogramParameters);
		
		String displName = getFullTitle();
		if (viewerScroller != null) {
			viewerScroller.getScrollerData().setName(displName);
		}

		// try to find the UserDisplayFrame that is holding this spectrogram, and update the name.  Need to cycle through all parents until we
		// either find a UserDisplayFrame, or a null
		Component specDisplay = specDisplayComponent.getComponent();
		if (specDisplay!=null) {
			Container parentFrame = specDisplay.getParent();
			while(parentFrame!=null) {
				if (parentFrame instanceof UserDisplayFrame) {
					((UserDisplayFrame) parentFrame).setTitle(displName);
					break;
				}
				parentFrame = parentFrame.getParent();
			}
		}

		repaintAll();
	}
	
	public String getFullTitle() {
		return specDisplayComponent.getUniqueName() + ": " + getFrameTitle();
	}

	public String getFrameTitle() {
		if (sourceFFTDataBlock == null) {
			return "No input data";
		}
		return String.format("%s FFTLen %d, Hop %d", sourceFFTDataBlock.getDataName(), 
				sourceFFTDataBlock.getFftLength(), sourceFFTDataBlock.getFftHop());
	}

	/**
	 * Find the right FFT source data. Old versions used block index, but better
	 * to use block name. However block names can change, so we'll use 
	 * name by preference, then index as a backup, making sure both 
	 * are set by the end of the function. 
	 * @param params
	 * @return an FFTDataBlock. 
	 */
	private FFTDataBlock findFFTDataBlock(SpectrogramParameters params) {
		PamDataBlock datablock = PamController.getInstance().getDataBlockByLongName(params.sourceName);
		try {
			return (FFTDataBlock) datablock;
		}
		catch (ClassCastException e) {
			return null;
		}
		//		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance().getFFTDataBlocks();
		//		if (fftBlocks == null || fftBlocks.size() == 0) {
		//			return null;
		//		}
		//		if (params.sourceName != null) {
		//			// find by name
		//			PamDataBlock fftBlock = PamController.getInstance().getDataBlockByLongName(params.sourceName);
		//			if (fftBlock != null) {
		//				// set the index too, just to be safe. search for long data name
		//				for (int i = 0; i < fftBlocks.size(); i++) {
		//					PamDataBlock fB = fftBlocks.get(i);
		//					if (fB == fftBlock) {
		//						params.fftBlockIndex = i;
		//						break;
		//					}
		//				}
		//				return (FFTDataBlock) fftBlock;
		//			}
		//		}
		//		else {
		//			// find by index. 
		//			if (params.fftBlockIndex >= fftBlocks.size() || params.fftBlockIndex < 0) {
		//				params.fftBlockIndex = 0;
		//			}
		//			PamDataBlock fftBlock = fftBlocks.get(params.fftBlockIndex);
		//			params.sourceName = fftBlock.getLongDataName();
		//			return (FFTDataBlock) fftBlock;
		//		}
		//		return null;
	}

	void paintAmplitudeImage() {
		// now make a standard amplitude image
		if (colorValues != null && colorValues.length > 0) {
			//			synchronized (spectrogramDisplay) {
			amplitudeImage = new BufferedImage(1, colorValues.length,
					BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = amplitudeImage.getRaster();
			for (int i = 0; i < colorValues.length; i++) {
				raster.setPixel(0, colorValues.length - i - 1, colorValues[i]);
			}
			//			}
		}
	}

	public void repaintAll() {
		setAxisLimits();

		//		sayWithDate("repaint all");
		if (spectrogramOuterPanel != null) {
			spectrogramOuterPanel.repaint();
		}
		if (spectrogramAxis != null) {
			spectrogramAxis.repaint();
		}
		if (spectrogramPanels != null){
			for (int i = 0; i < spectrogramPanels.length; i++){
				if (spectrogramPanels[i] == null) continue;
				spectrogramPanels[i].repaint();

			}
		}
		if (amplitudePanel != null) {
			amplitudePanel.SetAmplitudePanelBorder();
			amplitudePanel.repaint();
		}

		if (spectrogramPlotPanel!=null){
			spectrogramPlotPanel.repaintSidePanel();
		}

		//		if (waveformPanel != null) waveformPanel.repaint();
	}

	/**
	 * Width of the window thats being drawn on - not the number of pixels, 
	 * though these may often be the same.
	 * @return the width of the final rendered image in pixels
	 */
	public int getFrozenImageWidth() {
		int width;
		if (sourceFFTDataBlock == null) {
			return 1;
		}
		if (spectrogramParameters.timeScaleFixed) {
			width = (int) (spectrogramParameters.displayLength
					* sampleRate / sourceFFTDataBlock.getFftHop());
			spectrogramParameters.pixelsPerSlics = 1;
		} else {
			width = getDisplayWidth() / spectrogramParameters.pixelsPerSlics;
			spectrogramParameters.displayLength = (double) width
					* sourceFFTDataBlock.getFftHop() / sampleRate;
		}
		int displayWidth = spectrogramAxis.getWidth();
		spectrogramPixelOverlap  = 1;
		//		while (width > displayWidth * 2) {
		//			width /= 2;
		//			spectrogramPixelOverlap *= 2;
		//		}
		return Math.max(1, width);
	}

	/**
	 * 
	 * @return The width of the main spectrogram image that new data are drawn onto. 
	 * This is generally a lot longer than the final image that's drawn. 
	 */
	int getSpectrogramImageWidth() {
		if (viewerMode || spectrogramParameters.wrapDisplay) {
			return getFrozenImageWidth();
			//		if (spectrogramPanels == null || spectrogramPanels[0] == null) return 1;
			//		int w = spectrogramPanels[0].getWidth();
			//		w /= spectrogramParameters.pixelsPerSlics;
			//		return 
		}

		/*
		 *  Tell the image to be at least 5000 pixels wide which is larger than
		 *  any screen we're currently likely to encounter. A future mod will make
		 *  this a lot bigger and will also permit scrolling back and forth through this image. 
		 */		
		//		return Math.max(w, 5000);
		spectrogramPixelOverlap = 1;
		return 2000; // stop it going crazy if someone really wants to make a long spectrogram !
	}

	/**
	 *  
	 * @return With of the display panels in pixels.
	 */
	public int getDisplayWidth() {
		if (spectrogramPanels == null || spectrogramPanels[0] == null) {
			return 1;
		}
		return spectrogramPanels[0].getWidth();
	}

	/**
	 * 
	 * @return half the fft length. Always draw the full image then 
	 * select a portion as it's rendered onto the screen (if zoomed).
	 */
	public int getImageHeight() {
		//		int height = freqBinRange[1] - freqBinRange[0] + 1;
		//		return Math.max(1, height);
		if (sourceFFTDataBlock == null) {
			return 1;
		}
		return sourceFFTDataBlock.getDataWidth(0);
	}

	/**
	 * Called whenever the display size changes so that
	 * buffered images can be recreated. 
	 */
	public void createAllImages() {
		// imageHeight = spectrogramParameters.fftLength / 2;
		int imageHeight = getImageHeight();
		int imageWidth = getSpectrogramImageWidth();
		if (imageWidth <= 1)
			return;

		if (spectrogramPanels == null) {
			return;
		}
		for (int i = 0; i < spectrogramPanels.length; i++) {
			spectrogramPanels[i].createImage();
		}

		createColours();

		timeAxis = new PamAxis(10, 0, imageWidth, 0, 0,
				spectrogramParameters.displayLength, true, "Time (s)", "%3.1f");
		spectrogramAxis.setNorthAxis(timeAxis);

		if (rangeSpinner != null) {
			rangeSpinner.setSpinnerValue(spectrogramParameters.displayLength);
		}

		double fScale = 1;
		String westLabel = "Frequency (Hz)";
		if (spectrogramParameters.frequencyLimits[1] > 2000) {
			fScale = 1000;
			westLabel = "Frequency (kHz)";
		}

		frequencyAxis = new PamAxis(0, 200, 0, 10,
				spectrogramParameters.frequencyLimits[0] / fScale,
				spectrogramParameters.frequencyLimits[1] / fScale, true, westLabel,
				null);
		frequencyAxis.setFractionalScale(true);
		frequencyAxis.setCrampLabels(true);
		spectrogramAxis.setWestAxis(frequencyAxis);

		if (amplitudeAxis != null) {
			amplitudeAxis.setRange(spectrogramParameters.amplitudeLimits[0], spectrogramParameters.amplitudeLimits[1]);
		}

		setupViewScroller();
		
		setAxisLimits();
	}

	private void setAxisLimits() {
		if (sourceFFTDataBlock == null) {
			return;
		}
		if (frequencyAxis != null && spectrogramParameters != null) {
			freqAxisScale = 1.;
			if (spectrogramParameters.frequencyLimits[1] > 2000) {
				freqAxisScale = 1000;
			}
			
			// special processing for data blocks that are not showing frequency data in the vertical axis
			if (sourceFFTDataBlock != null && sourceFFTDataBlock.getClass() != FFTDataBlock.class && 
					sourceFFTDataBlock.getClass() != LtsaDataBlock.class) {
				DataTypeInfo scaleInfo = sourceFFTDataBlock.getScaleInfo();
				if (scaleInfo != null) {
					//					frequencyAxis.setLabel(scaleInfo.getTypeString());
					/*
					 *  all control is assuming frequency, so for cepstrum its wrong
					 *  so try to scale to right values. 
					 */
					double fRange = spectrogramParameters.frequencyLimits[1] - spectrogramParameters.frequencyLimits[0];
					double dRange = sourceFFTDataBlock.getMaxDataValue() - sourceFFTDataBlock.getMinDataValue();
					double minV = spectrogramParameters.frequencyLimits[0] / fRange * dRange;
					double maxV = spectrogramParameters.frequencyLimits[1] / fRange * dRange;
					if (maxV < 1) {
						freqAxisScale = 0.001;
					}
					frequencyAxis.setRange(minV/freqAxisScale, maxV/freqAxisScale);
				}
			}
			else {
//				Debug.out.println("Freq top = " + spectrogramParameters.frequencyLimits[1]/freqAxisScale);
				frequencyAxis.setRange(spectrogramParameters.frequencyLimits[0]/freqAxisScale,
						spectrogramParameters.frequencyLimits[1]/freqAxisScale);
			}
			frequencyAxis.setLogScale(sourceFFTDataBlock.isLogScale());
		}
	}

	ColourArrayType previousColourType;
	protected boolean createColours() {

		if (previousColourType == spectrogramParameters.getColourMap()) {
			return false;
		}
		previousColourType = spectrogramParameters.getColourMap();
		//		colourArray = ColourArray.createHotArray(256);
		colourArray = ColourArray.createStandardColourArray(256, previousColourType);
		colorValues = new double[256][3];
		for (int i = 0; i < 256; i++) {
			//			for (int j = 0; j < 3; j++) {
			//				colorValues[i][j] = 255 - i;
			////				colorValues[i][j] = colourArray.getColours()[i].ge
			//			}
			colorValues[i][0] = colourArray.getColours()[i].getRed();
			colorValues[i][1] = colourArray.getColours()[i].getGreen();
			colorValues[i][2] = colourArray.getColours()[i].getBlue();
		}
		return true;
	}


	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		//		System.out.println("samplerate " + sampleRate);
		this.sampleRate = sampleRate;
		// since this always gets called just before pam starts, use it to
		// reset the displays
		//		createAllImages(); don't do this since this get's called a lot when reloading data offline.
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		this.masterClockMilliseconds = milliSeconds;
		this.masterClockSamples = sampleNumber;
		if (netRXMode) {
			setupNetRXScroller();
		}
		//		spectrogramAxis.repaint(10);
	}

	private void setupNetRXScroller() {
		//		viewerScroller.getValueMillis();
		//		scro
		int spin = (int) (rangeSpinner.getSpinnerValue() * 1000.);
		long range = viewerScroller.getRangeMillis();
		//		range = Math.max(60000, spin*2);
		range = spin; // set the range of the scroller to spin. This stops scrolling but gives the correct axis and tool tip values. 

		viewerScroller.setVisibleMillis(spin);
		long visAm = viewerScroller.getVisibleAmount();
		if (range == 0) {
			range = 60000;
		}
		boolean onMax = viewerScroller.getValueMillis() ==
				(viewerScroller.getMaximumMillis()-viewerScroller.getVisibleAmount());
		long currentValue = viewerScroller.getValueMillis();
		if (currentValue <= 0) {
			onMax = true;
		}
		long ahead = 1000;
		ahead = Math.min(ahead, spin/5);
		viewerScroller.setRangeMillis(masterClockMilliseconds-range+ahead, masterClockMilliseconds+ahead, true);
		if (onMax) {
			viewerScroller.setValueMillis(masterClockMilliseconds+ahead-viewerScroller.getVisibleAmount());
		}
		else {
			viewerScroller.setValueMillis(currentValue);
		}
		//		viewerScroller.
		//		repaintAll();
		newScrollPosition();
	}

	public void PamToStart() {
		this.createAllImages();
		noteNewSettings();
	}


	/**
	 * Required data history depends on what's happening with the mouse.
	 * If the mouse is doing nothing and there are no SpectrogramMarkObservers
	 * then no data needs to be stored for drawing the spectrogram. 
	 * If there are mark observers and the mouse is up, then make sure that
	 * at least one screen full of data is always in memory (both FFT data
	 * and Raw wave data).
	 * If the mouse is down, then keep the maximum of either one screen full
	 * of from whenever the mouse was pressed - the use may hold it down for 
	 * a long time !
	 * 
	 * @see SpectrogramMarkObserver
	 */
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {

		// WTF was this line of code doing here ? This is what's been messing up 
		// data keeping for the spectrogram all along !
//		if (nMarkObservers == 0) {
//			return 0;
//		}

		long history = (long) getXDuration();
		if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
			if (viewerScroller != null) {
				history = viewerScroller.getRangeMillis();
			}
		}
		if (frozen) { // then the mouse is down !
			history += (PamCalendar.getTimeInMillis()-mouseDownTime); 
		}
		if (o instanceof PamRawDataBlock) {
			return maxRawHistory(history);
		}

		return history + 1000; // save an extra second of data 
	}

	/**
	 * Work out a new max history for raw data. At high sample rates, this
	 * can get silly, so limit it to 500MBytes. May need to do something more
	 * clever at some time later, but this can only help 
	 * @param history
	 * @return minimum of history input and max allowance for raw data. 
	 */
	private long maxRawHistory(long history) {
		if (sourceRawDataBlock == null) {
			return history;
		}
		long secondSize = (long) (sourceRawDataBlock.getSampleRate() * PamUtils.getNumChannels(sourceRawDataBlock.getChannelMap()) * Double.BYTES);
		if (secondSize == 0) {
			return history;
		}
		long maxBytes = 500000000L;
		long maxSecs = Math.max(1, maxBytes/secondSize);

		return Math.min(history, maxSecs*1000);
	}

	@Override
	public String getObserverName() {
		return "Spectrogam Display";
	}
	@Override
	public void addData(PamObservable obs, PamDataUnit newData) {
		//if (((1<<spectrogramParameters.channelList[0]) & newData.channelBitmap) == 0) return;
		// work out which panel(s) gets the data ...
		// it is possible for more than one panel to be using the same data
		if (spectrogramPanels == null) return;
		int dataChannel = -1;
		//		AcousticDataUnit acousticData = (AcousticDataUnit) newData; commented out when AcousticDataUnit converted to interface
		PamDataUnit acousticData = newData;
		/*
		 * To work out which display data associates with, need to do a bitwise compare.
		 * The basic waveform and fft data will always be single channel so we could in principle
		 * use ==. click data and other detector output may be multi channel though, so == will
		 * always be false since the channelList is always single channels. The bitwise comparison
		 * gets around this. 
		 * TODO - make it so each spectrogram can chose independently of the other.
		 */
		for (int i = 0; i < spectrogramParameters.nPanels; i++) {
			if (((1<<spectrogramParameters.channelList[i]) & acousticData.getSequenceBitmap()) != 0) {
				//			if ((spectrogramParameters.channelList[i]) == acousticData.getSequenceBitmap()) {
				updateChannel(obs, acousticData, i);
			}
		}

	}
	
	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

	@Override
	public void setLoadStatus(int loadState) {
		//				System.out.println("Data load status = " + loadState);
		if (loadState == PamDataBlock.REQUEST_DATA_LOADED) {
			//			drawBackgroundImages();
			repaintAll();
		}
	}

	private void updateChannel(PamObservable obs, PamDataUnit newData, int panelNumber){
		synchronized (innerPanelSynchObject) {
			if (viewerMode) {
				if (obs == this.sourceFFTDataBlock) {
					FFTDataUnit fftDataUnit = (FFTDataUnit) newData;
					//			System.out.println("New FFT Data for Channel " + dataChannel + " Data length " + fftDataUnit.getFftData().length + " bins");
					int imagePos = spectrogramPanels[panelNumber].drawViewerSpectrogram(obs, fftDataUnit, panelNumber);
					if (imagePos > 100) {
						// only draw once it's gone for a while to reduce flickering. 
						spectrogramPanels[panelNumber].repaint(100);
					}
				}
				return;				}
			if (newData == null || spectrogramPanels == null || spectrogramProjector == null) return;
			if (spectrogramPanels.length <= panelNumber || spectrogramPanels[panelNumber] == null) return;
			if (obs == this.sourceFFTDataBlock) {
				FFTDataUnit fftDataUnit = (FFTDataUnit) newData;
				//			System.out.println("New FFT Data for Channel " + dataChannel + " Data length " + fftDataUnit.getFftData().length + " bins");
				spectrogramPanels[panelNumber].drawSpectrogram(obs, fftDataUnit, panelNumber);
				if (!viewerMode) {
					spectrogramProjector.setOffset(newData.getTimeMilliseconds(), spectrogramPanels[panelNumber].imagePos);
				}
				if (!spectrogramPanels[panelNumber].getSize().equals(panelSize)) {
					panelSize = spectrogramPanels[panelNumber].getSize();
					//				createOverlay(panelSize.width, panelSize.height);
				}
			} 
			else if (newData instanceof RawDataUnit) {
				spectrogramPanels[panelNumber].repaint(200);
			}
			else {
				spectrogramPanels[panelNumber].repaint(200);
			}
		}

	}


	private int getColourIndex(double dBLevel) {
		// fftMag is << 1
		double  p;
		p = 256
				* (dBLevel - spectrogramParameters.amplitudeLimits[0])
				/ (spectrogramParameters.amplitudeLimits[1] - spectrogramParameters.amplitudeLimits[0]);
		return (int) Math.max(Math.min(p, 255), 0);
	}




	//	private void sayWithDate(String msg) {
	//		System.out.println(String.format("%s: %s", PamCalendar.formatTime(System.currentTimeMillis()), msg));
	//	}


	private class SettingsAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			spectrogramDisplay.setSettings();
		}
	}
	private class MenuCancelPlayback implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			PlaybackControl.getViewerPlayback().stopViewerPlayback();
		}
	}
	private class MenuPlayAll implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			long startMillis = viewerScroller.getValueMillis();
			long endMillis = startMillis + viewerScroller.getVisibleAmount();
			PlaybackControl.getViewerPlayback().playViewerData(startMillis, endMillis, new PlayProgress());
		}
	}
	class MenuPlayChannel implements ActionListener {
		int channel;

		public MenuPlayChannel(int channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			long startMillis = viewerScroller.getValueMillis();
			long endMillis = startMillis + viewerScroller.getVisibleAmount();
			PlaybackControl.getViewerPlayback().playViewerData(1<<channel, startMillis, endMillis, new PlayProgress());
		}
	}

	class MenuPlayFrom implements ActionListener {
		int channel;
		long startMillis;

		public MenuPlayFrom(int channel, long startMillis) {
			super();
			this.channel = channel;
			this.startMillis = startMillis;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			long dispStartMillis = viewerScroller.getValueMillis();
			long endMillis = dispStartMillis + viewerScroller.getVisibleAmount();
			PlaybackControl.getViewerPlayback().playViewerData(1<<channel, startMillis, endMillis, new PlayProgress());
		}
	}

	private int playbackChannels;
	private long playbackTimeMillis;
	private int playbackStatus = PlaybackProgressMonitor.PLAY_END;
	class PlayProgress implements PlaybackProgressMonitor {

		@Override
		public void setProgress(int channels, long timeMillis, double percent) {
			playbackChannels = channels;
			playbackTimeMillis = timeMillis;
			repaintAll();
		}

		@Override
		public void setStatus(int status) {
			playbackStatus = status;
			repaintAll();
		}

	}

	public void setSettings() {
		// check that fft data is available before even trying

		ArrayList<PamDataBlock> fftBlocks = PamController.getInstance()
				.getFFTDataBlocks();
		if (fftBlocks == null || fftBlocks.size() == 0) {
			JOptionPane.showMessageDialog(spectrogramPlotPanel, "No spectrogram data is available in the current model." + 
					"\nCreate at least one FFT (spectrogram) machine");
			return;
		}
		//		SpectrogramParameters newParams = SpectrogramParamsDialog
		//				.showDialog(userDisplayControl.getPamView().getGuiFrame(), this.getOverlayMarker(), spectrogramParameters);
		SpectrogramParameters newParams = SpectrogramParamsDialog
				.showDialog(userDisplayControl.getGuiFrame(), spectrogramPanels, spectrogramParameters);


		if (newParams == null) return;
		spectrogramParameters = newParams.clone();

		noteNewSettings();
		//		setParams(newParams);
	}

	/**
	 * set up the page size on the view scroller
	 * and also sets up the range spinner
	 */
	private void setupViewScroller() {
		if (viewerScroller == null) {
			return;
		}
		long visibleMillis = (long)(spectrogramParameters.displayLength * 1000);
		viewerScroller.setVisibleMillis(visibleMillis);
		viewerScroller.setBlockIncrement(visibleMillis * 9 / 10);
		viewerScroller.setUnitIncrement(visibleMillis * 1 / 10);

		if (sourceFFTDataBlock != null) {
			// now depending on the hop set the minimum step size for the viewerscroller
			if (viewerScroller != null) {
				PamScrollerData scrollerData = viewerScroller.getScrollerData();
				// default step size is only 1000 millis. for LTSA data we want to make
				// this a LOT longer. 
				double hopSeconds = sourceFFTDataBlock.getFftHop() / sourceFFTDataBlock.getSampleRate();
				int minStep = (int) Math.max(1, hopSeconds * 1000);
				scrollerData.setStepSizeMillis(minStep);
			}
		}

		requestFFTData();

		subscribeDataBlocks();
	}

	/**
	 * Create lists of data blocks that each panel is viewing
	 * so they can plot the data efficiently. 
	 */
	private void subscribeDataBlocks() {
		if (spectrogramPanels == null) {
			return;
		}
		for (int i = 0; i < spectrogramPanels.length; i++) {
			if (spectrogramPanels[i] != null) {
				spectrogramPanels[i].subscribeDataBlocks();
			}
		}
		subscribeViewScrollData();
	}

	/**
	 * If there are SpectrogramMarkObservers, then it's
	 * necessary to subscribe to the waveform data since
	 * it may be needed by one of the mark observers.
	 * Go back through the block / process chain until we
	 * find the first raw data. This may not be the first block
	 * we came to - the fft block used for the spectrogram may
	 * have been through multiple porcesses (e.g. kernel smoothing)
	 * since it was first created from raw audio data. 
	 */
	private void subscribeRawDataBlock() {	
		PamRawDataBlock oldRawDataBlock = sourceRawDataBlock;
		sourceRawDataBlock = null;
		PamDataBlock pBlock;
		PamProcess pProcess;
		if (sourceFFTDataBlock != null) {
			pBlock = sourceFFTDataBlock;
			while (pBlock != null) {
				pProcess = pBlock.getParentProcess();
				if (pProcess == null) break;
				pBlock = pProcess.getParentDataBlock();
				if (pBlock != null && pBlock.getUnitClass() == RawDataUnit.class) {
					sourceRawDataBlock = (PamRawDataBlock) pBlock;
					break;
				}
			}
		}
		if (sourceRawDataBlock != oldRawDataBlock) {
			if (oldRawDataBlock != null) {
				oldRawDataBlock.deleteObserver(this);
			}
		}
		if (sourceRawDataBlock != null) {
			sourceRawDataBlock.addObserver(this);
//			sampleRate = sourceRawDataBlock.getSampleRate();
			sampleRate = sourceFFTDataBlock.getSampleRate();
		}

	}

	/**
	 * Subscribe overlaying data to the view scroller. 
	 */
	private void subscribeViewScrollData() {
		if (viewerScroller == null) {
			return;
		}
		PamDataBlock dataBlock;
		detectorDataBlocks = PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
		//			if (spectrogramParameters.showDetector == null || spectrogramParameters.showDetector[panelId] == null) return;
		boolean want;
		for (int i = 0; i < detectorDataBlocks.size(); i++) {
			//				checkMenuItem = (JCheckBoxMenuItem) detectorMenu.getComponent(i);
			dataBlock = detectorDataBlocks.get(i);
			if (dataBlock.canDraw(spectrogramProjector)) {
				want = false;
				for (int iPanel = 0; iPanel < spectrogramParameters.nPanels; iPanel++) {
					if (spectrogramParameters.getOverlayDataInfo(dataBlock, iPanel).select) {
						want = true;
					}
				}
				if (want) {
					viewerScroller.addDataBlock(dataBlock);
				}
				else {
					viewerScroller.removeDataBlock(dataBlock);
				}
			}
		}
	}

	class ViewScrollObserver implements PamScrollObserver {

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			//			System.out.println("Scroll range changed " + PamCalendar.formatTime(pamScroller.getMinimumMillis()));
			setupViewScroller();
		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			//			System.out.println("Scroll value changed " + PamCalendar.formatTime(pamScroller.getValueMillis()));
			//			sayWithDate(String.format("scrollValueChanged to %d", pamScroller.getValueMillis()));
			checkVisibleAmount();
			newScrollPosition();
		}
	}

	/**
	 * Needed when zoomed from a different display ...
	 */
	private void checkVisibleAmount() {
		if (viewerScroller == null) {
			return;
		}
		long vis = viewerScroller.getVisibleAmount();
		long curVis = (long) (spectrogramParameters.displayLength * 1000.);

		// Trying to fix an issue introduced by new Scroller Coupling that messes up the spinner values
		//		if (vis != curVis) {
		// Only resize if the discrepancy is less than 1%
		if (curVis <= 0) return;
		if (Math.abs(vis - curVis)/curVis>0.01) {
			spectrogramParameters.displayLength = vis/1000.;
			rangeSpinner.setSpinnerValue(spectrogramParameters.displayLength); 
			if (timeAxis!=null)timeAxis.setMaxVal(spectrogramParameters.displayLength);
			//			repaintAll();
		}
	}


	void newScrollPosition() {
		if (spectrogramPanels == null) {
			return;
		}
		long s = viewerScroller.getValueMillis();
		//		System.out.println(String.format("Scroll spectrogram to %s", PamCalendar.formatDateTime(s)));
		long e = s + (long) (spectrogramParameters.displayLength * 1100.) + 2000; // give extra 10% + 200ms
		if (viewerMode) {
			requestFFTData(s, e);
		}

		try {
			for (int i = 0; i <  spectrogramPanels.length; i++) {
				spectrogramPanels[i].currentTimeMilliseconds = viewerScroller.getValueMillis() +
						(long) (spectrogramParameters.displayLength * 1000.);
			}
		}
		catch (Exception ee) {

		}

		//		System.out.println(String.format("Repaint spectrogram to %s", PamCalendar.formatDateTime(s)));
		repaintAll();
	}

	private void requestFFTData() {
		if (viewerScroller == null) {
			return;
		}
		long s = viewerScroller.getValueMillis();
		long e = s + (long) (spectrogramParameters.displayLength * 1100.) + 2000;
		requestFFTData(s, e);
	}


	private long lastReqStart, lastReqEnd;

	public int mousePressedButton;
	private void requestFFTData(long startMillis, long endMillis) {

		if (spectrogramParameters.hideViewerSpectrogram) {
			return;
		}
		if (sourceFFTDataBlock == null) {
			return;
		}
		if (lastReqStart == startMillis && lastReqEnd == endMillis) {
			return;
		}

		lastReqStart = startMillis;
		lastReqEnd = endMillis;

		/**
		 * Need to call this before locking the innnerPanelSynchObject 
		 * or it will lock up. 
		 */
		sourceFFTDataBlock.cancelDataOrder();

		//		synchronized (innerPanelSynchObject) {

		clearBackgroundImages();

		//		//clear all databl
		//		clearAllFFTBlocks();

		//		long t1 = System.nanoTime();
		sourceFFTDataBlock.orderOfflineData(this, this, startMillis, endMillis, 1,
				OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
		//		}
		//		long t2 = System.nanoTime();
		//		System.out.println(String.format("%s %3.2fs to request data",
		//				sourceFFTDataBlock.getDataName(),
		//				(double)(t2-t1)/1.e9));

	}

	private void clearBackgroundImages() {
		synchronized (innerPanelSynchObject) {
			for (int i = 0; i < spectrogramPanels.length; i++) {
				if (spectrogramPanels[i] != null) {
					spectrogramPanels[i].clearImage();
				}
			}
		}
	}


	//	class ViewRangeSpinnerListener implements RangeSpinnerListener {
	//		@Override
	//		public void valueChanged(double oldValue, double newValue) {
	//			double range = viewerRangeSpinner.getSpinnerValue();
	//			timeAxis.setRange(0, range);
	//			spectrogramParameters.displayLength = range;
	//			noteNewSettings();
	//			if (spectrogramPanels == null) {
	//				return;
	//			}
	//			for (int i = 0; i <  spectrogramPanels.length; i++) {
	//				spectrogramPanels[i].currentTimeMilliseconds = viewerScroller.getValueMillis() +
	//				(long) (spectrogramParameters.displayLength * 1000.);
	//			}
	//			repaintAll();
	//		}
	//	}

	/**
	 * Need to extend PamAxisPanel in order to 
	 * override the axis drawing to allow for the
	 * plug in panels at the bottom of the display.
	 * @author Doug
	 *
	 */
	class SpectrogramAxis extends PamAxisPanel implements SimpleDataObserver {
		Rectangle oldBounds = new Rectangle();

		SpectrogramAxis() {
			super();
			//			PamColors.getInstance().registerComponent(this,
			//					PamColors.PamColor.BORDER);
			this.SetBorderMins(10, 10, 10, 10);

		}

		// need to redraw when heights of inner components are zero
		// but make sure it doesn't get stuck in an inf' loop !
		int heightErrors = 0; 
		/*
		 * Need to override paint to allow for funny scaling if waveform and
		 * spectrogram both shown
		 */
		@Override
		public void paintComponent(Graphics g) {
			//			super.paint(g);
			if (!isShowing()) return;
			Rectangle b = g.getClipBounds();
			g.setColor(getBackground());
			g.fillRect(b.x, b.y, b.width, b.height);
			/*
			 * Check that there is enough space for each of up to four axis and
			 * draw it.
			 */
			// Rectangle r = getBounds();
			Insets currentInsets = getInsets();
			Insets newInsets = new Insets(getMinNorth(), getMinWest(), getMinSouth(), getMinEast());
			// Insets plotInsets;
			if (getInnerPanel() != null) {
				// plotInsets = innerPanel.getInsets();
			} else {
				// plotInsets = new Insets(0,0,0,0);
			}
			if (getNorthAxis() != null) {
				newInsets.top = Math.max(newInsets.top, getNorthAxis().getExtent(g));
			}
			if (getWestAxis() != null) {
				getWestAxis().setFormat("%.4g%n");
				newInsets.left = Math
						.max(newInsets.left, getWestAxis().getExtent(g));
			}
			if (getSouthAxis() != null) {
				newInsets.bottom = Math.max(newInsets.bottom, getSouthAxis()
						.getExtent(g));
			}
			if (getEastAxis() != null) {
				newInsets.right = Math.max(newInsets.right, getEastAxis()
						.getExtent(g));
			}
			if (!currentInsets.equals(newInsets)) {
				setBorder(new EmptyBorder(newInsets));
			}

			int panelRight = getWidth() - newInsets.right;
			int panelBottom = getHeight() - newInsets.bottom;
			if (getNorthAxis() != null) {
				getNorthAxis().drawAxis(g, newInsets.left, newInsets.top,
						panelRight, newInsets.top);
				// and draw the time at the left end of the axis.
				//				long axisStart = spectrogramProjector.getTimeOffsetMillis();
				//				String timeString = PamCalendar.formatDateTime(masterClockMilliseconds);
				//				g.drawString(timeString, newInsets.left, g.getFontMetrics().getHeight());
			}
			Point thisScreenLoc = this.getLocationOnScreen();
			if (getWestAxis() != null) {
				for (int i = 0; i < spectrogramParameters.nPanels; i++){
					if (spectrogramPanels[0] == null) return;
					/*
					 * Try to work out the relative positions of this panel 
					 * and each of the spectrogram panels. 
					 */
					if (!spectrogramPanels[i].isShowing()) return;
					Point panelScreenLoc = null;
					try {
						panelScreenLoc = spectrogramPanels[i].getLocationOnScreen();
					}
					catch (IllegalComponentStateException e) {
						continue;
					}
					
					if (spectrogramPanels[i].getHeight() == 0) {
						continue;
					}
					int y1 = panelScreenLoc.y - thisScreenLoc.y;
					int y2 = y1 + spectrogramPanels[i].getHeight() - 1;

					int singleHeight = spectrogramPanels[i].getHeight();
					if (singleHeight == 0 && heightErrors++ < 2) {
//						repaint();
						return;
					}
					heightErrors = 0;
					//					getWestAxis().drawAxis(g, newInsets.left, newInsets.top
					//							+ singleHeight * (i+1), newInsets.left, newInsets.top + singleHeight *  i);
					getWestAxis().drawAxis(g, newInsets.left, y2, newInsets.left, y1);
					//					System.out.println(String.format("Draw spec W axis with insets %d %d %d %d, single height = %d",
					//							newInsets.left, newInsets.top
					//							+ singleHeight * (i+1), newInsets.left, newInsets.top + singleHeight *  i, singleHeight));
				}
			}
			if (getSouthAxis() != null) {
				getSouthAxis().drawAxis(g, newInsets.left, panelBottom, panelRight,
						panelBottom);
			}
			if (getEastAxis() != null) {
				getEastAxis().drawAxis(g, panelRight, panelBottom, panelRight,
						newInsets.top);
			}
			/* 
			 * Also need to draw east and west axis of any plugged in displays, should they have them !
			 */
			//			synchronized (displayPanels) {
			//			if (((Object)displayPanels).getClass().) {
			//				
			//			}
			DisplayPanel dp;
			Point fLoc, dpLoc; 
			fLoc = getLocationOnScreen();
			int axTop, axBottom;
			PamAxis axis;

			synchronized(displayPanels) {
				int nD = displayPanels.size();
				for (int i = 0; i < nD; i++) {
					dp = displayPanels.get(i);
					if ((axis = dp.getWestAxis()) != null) {
						// need to work out where the hell the 
						// window is relative to this one...
						if (!dp.getInnerPanel().isShowing()) {
							return;
						}
						dpLoc = dp.getInnerPanel().getLocationOnScreen();
						axTop = dpLoc.y - fLoc.y;
						axBottom = axTop + dp.getInnerPanel().getHeight();
						axis.drawAxis(g, newInsets.left, axBottom, newInsets.left, axTop);
					}
					if ((axis = dp.getSouthAxis()) != null) {
						dpLoc = dp.getInnerPanel().getLocationOnScreen();
						axBottom = dpLoc.y - fLoc.y + dp.getInnerPanel().getHeight();
						axis.drawAxis(g, newInsets.left, axBottom, newInsets.left + dp.getInnerPanel().getWidth(), axBottom);
					}
					//				if ((axis = dp.getEastAxis()) != null) {
					//				// need to work out where the hell the 
					//				// window is relative to this one...
					//				dpLoc = dp.getInnerPanel().getLocationOnScreen();
					//				axTop = dpLoc.y - fLoc.y;
					//				axBottom = axTop + dp.getInnerPanel().getHeight();
					//				axis.drawAxis(g, panelRight, axBottom, panelRight, axTop);
					//				}
				}
			}
			if (viewerScroller != null) {
				long t = viewerScroller.getValueMillis();
				long t2 = t + viewerScroller.getVisibleAmount();
				FontMetrics fm = g.getFontMetrics();
				g.drawString(PamCalendar.formatDateTime(t, true), newInsets.left, fm.getHeight());
				String ts = PamCalendar.formatDateTime(t2, true);
				g.drawString(ts, getWidth() - newInsets.right - fm.stringWidth(ts), fm.getHeight());
			}
			
			if (scrollJumper != null) {
				paintHighlighted(g, newInsets, scrollJumper.getLastFoundDataUnit());
			}
		}
		
		private void paintHighlighted(Graphics g, Insets insets, PamDataUnit dataUnit) {
			if (dataUnit == null) {
				return;
			}
			long t1 = dataUnit.getTimeMilliseconds()-viewerScroller.getValueMillis();
			if (timeAxis == null) {
				return;
			}
			int x1 = (int) Math.floor(timeAxis.getPosition(t1/1000));
			int x2 = x1;
			if (dataUnit.getDurationInMilliseconds() != null) {
				x2 = (int) Math.ceil(timeAxis.getPosition((t1+dataUnit.getDurationInMilliseconds())/1000.));
			}
			if (x2 < 0 || x1 > getWidth()-insets.left-insets.right) {
				return;
			}
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(Color.RED);
			g2d.setStroke(new BasicStroke(4));
			x1 += insets.left;
			x2 += insets.left;
			int y = insets.top-1;
			g.drawLine(x1, y, x2, y);
			y = getHeight()-insets.bottom+2;
			g.drawLine(x1, y, x2, y);
		}

		@Override
		public void update(PamDataUnit dataUnit) {
			repaint();
		}

	}

	class AmplitudePanel extends PamAxisPanel {
		AmplitudePanel() {
			super();
			//			PamColors.getInstance().registerComponent(this,
			//					PamColors.PamColor.BORDER);
			// setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
			setLayout(new BorderLayout());
			add(new AmplitudeBar(), BorderLayout.CENTER);

			String label = String.format("PSD (%s)", PamController.getInstance().getGlobalMediumManager().getdBPSDString());
			amplitudeAxis = new PamAxis(0, 200, 0, 10,
					spectrogramParameters.amplitudeLimits[0],
					spectrogramParameters.amplitudeLimits[1], false, label, "%3.0f");
			setEastAxis(amplitudeAxis);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

		/**
		 * needs to be called whenever the other sizes change to kepp
		 * borders the same.
		 * Should also resize, to allow space under if plug in plots are 
		 * created. Easiest to do this by checking on size of 
		 * spectrogramOutperPanel and making sure it's the same.
		 *
		 */
		void SetAmplitudePanelBorder() {
			// needs to be called whenever the other sizes change to keep
			// borders the same.

			Insets axisInsets = spectrogramAxis.getInsets();

			int botInset = axisInsets.bottom;
			if (spectrogramOuterPanel != null) {
				int h = spectrogramOuterPanel.getHeight();
				if (h > 0) {
					// gets a bit confused when plots are recreated at startup since h is 0. 
					botInset += (spectrogramPlotPanel.getHeight() - h);
				}
				else {
					botInset += 2;
				}
//				if (spectrogramOuterPanel.getHeight() == 0) {
//					System.out.println("no outer panel");
//				}
//				System.out.printf("Set spec botInset to %d as %d - %d\n", botInset, spectrogramPlotPanel.getHeight(), spectrogramOuterPanel.getHeight());
			}
//			System.out.printf("Setting spec axis positiong at %d / %d\n", axisInsets.top, botInset);
			SetBorderMins(axisInsets.top, 0, botInset, 10);
			//			SetBorderMins(12, 0, 19, 100);
			// setMinimumSize(new Dimension(300, 20));
		}

		class AmplitudeBar extends JPanel {
			AmplitudeBar() {
				setMinimumSize(new Dimension(100, 20));
			}

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (amplitudeImage == null)
					return;

				Graphics2D g2d = (Graphics2D) g;

				// double scaleWidthfactor = 1;
				double ascaleX = this.getWidth()
						/ (double) amplitudeImage.getWidth(null);
				double ascaleY = this.getHeight()
						/ (double) amplitudeImage.getHeight(null);
				AffineTransform xform = new AffineTransform();
				// xform.translate(1, amplitudeImage.getWidth(null));
				xform.scale(ascaleX, ascaleY);
				g2d.drawImage(amplitudeImage, xform, this);
			}
		}
	}

	class SpectrogramOuterPanel extends JPanel {

		private SpectrogramDisplay spectrogramDisplay;
		int nPanels;
		public SpectrogramOuterPanel(SpectrogramDisplay spectrogramDisplay) {
			super();
			this.spectrogramDisplay = spectrogramDisplay;

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			int topBorder, bottomBorder;
			Color borderColour = Color.BLACK;
			synchronized (innerPanelSynchObject) {
				/*
				 * Update code August 2022 so it only creates and destroys
				 * panels if the number of panels has changed. 
				 */			
				int nOld = 0;
				int nNew =spectrogramParameters.nPanels; 
				if (spectrogramPanels != null) {
					nOld = spectrogramPanels.length;
					if (nNew < nOld) {
						for (int i = nNew; i < nOld; i++) {
							spectrogramPanels[i].destroyPanel();
						}
						spectrogramPanels = Arrays.copyOf(spectrogramPanels, nNew);
					}
//					for (int i = 0; i < spectrogramPanels.length; i++) {
//						if (spectrogramPanels[i] != null) {
//							spectrogramPanels[i].destroyPanel();
//						}
//					}
				}
				if (spectrogramPanels == null) {
					spectrogramPanels = new SpectrogramPanel[spectrogramParameters.nPanels];
				}
				else if (nOld != nNew) {
					spectrogramPanels = Arrays.copyOf(spectrogramPanels, nNew);
				}
				for (int i = nOld; i < nNew; i++) {
					spectrogramPanels[i] = new SpectrogramPanel(spectrogramDisplay, i);
				}
//				//popupListener = new PopupListener();
//				nPanels = spectrogramParameters.nPanels;
				for (int i = 0; i < nNew; i++) {
					PamPanel panelBorder = new PamPanel(new BorderLayout());
					if (spectrogramPanels[i].getParent() != null) {
						// remove from any previous panel parent if it's being reused. 
						spectrogramPanels[i].getParent().remove(spectrogramPanels[i]);
					}
					topBorder = 1;
					bottomBorder = 0;
					if (i == 0) topBorder = 0;
					if (i == spectrogramParameters.nPanels-1) bottomBorder = 0;
					panelBorder.setBorder((BorderFactory.createEmptyBorder(topBorder,0,bottomBorder,0)));
					panelBorder.add(BorderLayout.CENTER, spectrogramPanels[i]);
					add(panelBorder);
					//					add(spectrogramPanels[i]);
					//					spectrogramPanels[i].addMouseListener(spectrogramDisplay.popupListener);
				}
			}
		}

	}
	/**
	 * Outer panel which contains the spectrogram outer panel
	 * and also all the plug in plots a the bottom 
	 * 
	 * @author Doug
	 *
	 */
	class SpectrogramPlotPanel extends JPanel {

		JSplitPane splitPane;
		JPanel simplePane;
		SpecLayeredPane spectroAndSidePanel;

		public SpectrogramPlotPanel() {
			super();

			setLayout(new BorderLayout());

			layoutPlots();

		}

		@Override
		public void repaint(){
			super.repaint();
			if (spectroAndSidePanel!=null) repaintSidePanel();
		}

		public void repaintSidePanel(){	
			spectroAndSidePanel.repaint();
		}

		public void layoutPlots() {

			// need to remove the old plots first
			removeAll();

			//need to use layered panes
			CornerLayoutContraint c = new CornerLayoutContraint();
			spectroAndSidePanel = new SpecLayeredPane();
			spectroAndSidePanel.setLayout(new CornerLayout(c));
			c.anchor = CornerLayoutContraint.EAST;
			//			spectroAndSidePanel.add(hidingPanel.getPanel(), c,JLayeredPane.PALETTE_LAYER);
			CornerLayoutContraint clc = new CornerLayoutContraint();
			clc.anchor = CornerLayoutContraint.FIRST_LINE_END;
			hidingDialogPanel = new HidingDialogPanel(clc.anchor, hidingPanel);
			hidingDialogPanel.setOpacity(0.75f);
			hidingDialogPanel.setSizingComponent(spectroAndSidePanel);
			hidingDialogPanel.setAutoHideTime(1000);
			spectroAndSidePanel.add(hidingDialogPanel.getShowButton(), clc, JLayeredPane.PALETTE_LAYER);
			c.anchor = CornerLayoutContraint.FILL;	

			if (viewerScroller != null) {
				add(BorderLayout.SOUTH, viewerScroller.getComponent());
			}

			oldDisplayPanels = displayPanels;
			displayPanels = new Vector<DisplayPanel>();

			splitPane = null;
			simplePane = null;
			//			if (spectrogramOuterPanel == null || spectrogramOuterPanel.nPanels != spectrogramParameters.nPanels) {
			spectrogramOuterPanel = new SpectrogramOuterPanel(spectrogramDisplay);
			spectroAndSidePanel.add(spectrogramOuterPanel, c,JLayeredPane.FRAME_CONTENT_LAYER);
			//			}
			int nAvailablePlugins = DisplayProviderList.getDisplayPanelProviders().size();
			int nUsedPlugins = 0;
			if (nAvailablePlugins > 0 && spectrogramParameters.showPluginDisplay != null) {
				for (int i = 0; i < Math.min(nAvailablePlugins, spectrogramParameters.showPluginDisplay.length); i++){
					if (spectrogramParameters.showPluginDisplay[i]) {
						nUsedPlugins ++;
					}
				}
			}

			JPanel bottomPanel = null;

			//			Synchronised (displayPanels) {
			if (nUsedPlugins > 0) {
				DisplayPanelProvider dp;
				DisplayPanel displayPanel;
				bottomPanel = new JPanel();
				splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				splitPane.setTopComponent(spectroAndSidePanel);
				bottomPanel.setLayout(new GridLayout(nUsedPlugins, 1));
				for (int i = 0; i < Math.min(nAvailablePlugins, spectrogramParameters.showPluginDisplay.length); i++){
					if (spectrogramParameters.showPluginDisplay[i]) {
						dp = DisplayProviderList.getDisplayPanelProviders().get(i);
						displayPanel = recyleOldPanel(dp);
						if (displayPanel == null) {
							displayPanel = dp.createDisplayPanel(spectrogramDisplay);
						}
						displayPanels.add(displayPanel);
						bottomPanel.add(displayPanel.getPanel());
					}
				}
				splitPane.setBottomComponent(bottomPanel);
				splitPane.addPropertyChangeListener(new SplitListener());
				if (spectrogramParameters.horizontalSplitLocation == null) {
					splitPane.setResizeWeight(0.7);
				}
				else {
					splitPane.setDividerLocation(spectrogramParameters.horizontalSplitLocation);
				}
				add(BorderLayout.CENTER, splitPane);
			}
			else {
				//FIXME- the commented out stuff appears to cause a serious Swing Exception?
				//				simplePane = new JPanel();
				//				simplePane.setLayout(new BoxLayout(simplePane, BoxLayout.Y_AXIS));
				//				simplePane.add(spectroAndSidePanel);
				//				add(BorderLayout.CENTER, simplePane);
				add(BorderLayout.CENTER, spectroAndSidePanel);
			}
			spectroAndSidePanel.setOpaque(false);

			clearOldPlugins();
			//			}

			if (bottomPanel != null) {
				bottomPanel.addAncestorListener(new SplitPaneListener());
			}
			// this bodge seems to be needed to make the new window appear
			invalidate();

			//			for (int i = 0; i < spectrogramPanels.length; i++)
			//				spectrogramPanels[i].addMouseListener(popupListener);


		}

		class SplitListener implements PropertyChangeListener {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				spectrogramParameters.horizontalSplitLocation = splitPane.getDividerLocation();
			}
		}

		DisplayPanel recyleOldPanel(DisplayPanelProvider displayPanelProvider) {
			if (oldDisplayPanels == null) {
				return null;
			}
			for (int i = 0; i < oldDisplayPanels.size(); i++) {
				if (oldDisplayPanels.get(i).getDisplayPanelProvider() == displayPanelProvider) {
					return oldDisplayPanels.remove(i);
				}
			}
			return null;
		}
		void clearOldPlugins() {
			// properly remove all existing plugin panels.
			// each needs to be told it's on it's way out so that
			// it can unsubscribe from any data

			synchronized (oldDisplayPanels) {
				for (int i = 0; i < oldDisplayPanels.size(); i++) {
					oldDisplayPanels.get(i).destroyPanel();
				}
				oldDisplayPanels.clear();
			}
		}

	} // end class SpectrogramPlotPanel

	/**
	 * 
	 * Inner panels showing a single spectrogram display. 
	 * @author Doug Gillespie
	 *
	 */
	class SpectrogramPanel extends JPanel implements PamObserver {

		private SpectrogramDisplay specDisplay;

		Dimension panelSize = new Dimension();

		int panelId; // identifier for this panel - 0, 1, 2, 3, etc...

		JPopupMenu detectorMenu;

		JCheckBoxMenuItem[] checkMenuItems;

		SpectrogramOverlayDataManager overlayDataManager;

		//		PopupListener popupListener;

		private ArrayList<PamDataBlock> viewedDataBlocks = new ArrayList<PamDataBlock>();

		//		Zoomer spectroZoomer; 



		SpectrogramZoomer spectroZoomable;

		//		private ScrollableBufferedImage specImage;
		/**
		 * specImage is an overly long spectrogram image which will eventually be used for scrolling. 
		 */
		private BufferedImage specImage;

		/**
		 * floating point array to sit parallel with specImage holding amplitude data
		 * on a db scale so that image can be recreated quickly if amplitude scale dragged. 
		 */
		private float[][] specFloatData; 

		private DirectDrawProjector directDrawProjector;

		private WritableRaster writableRaster;

		SpecPanelMouse specPanelMouse;

		int imagePos;

		int frozenImagePos;

		long currentTimeMilliseconds;

		long frozenTimeMilliseconds;

		JLabel channelLabel = new PamLabel();

		//		private JToolTip frequencyTooltip;

		/**
		 * Frozen image used when the mouse is pressed over the display
		 */
		private BufferedImage frozenImage;

		/*
		 * Indicates that this is the marked spectrogram panel
		 */
		boolean markThis = false;

		private boolean firstWrapDone;

		boolean allowRepaint=true;

		private int imageLines;

		private OverlayMarker overlayMarker;

		private int nPanelMarkObs;
		
		private long lastDataSample = 0;


		SpectrogramPanel(SpectrogramDisplay spectrogramDisplay, int iD) {

			super();

			this.specDisplay = spectrogramDisplay;

			panelId = iD;

			directDrawProjector = new DirectDrawProjector(spectrogramDisplay, iD);
			overlayDataManager = new SpectrogramOverlayDataManager(spectrogramDisplay, this);
			//			if (viewerMode) {
			//				this.addMouseMotionListener(directDrawProjector.getMouseHoverAdapter(this));
			//				this.addMouseListener(directDrawProjector.getMouseHoverAdapter(this));
			//			}

			//			setBorder(new EmptyBorder(10, 5, 10, 5));	


			overlayMarker = new SpectrogramMarker(spectrogramDisplay, panelId, 0, directDrawProjector);
			OverlayMarkProviders.singleInstance().addProvider(overlayMarker);


			setToolTipText("Spectrogram Display Panel");

			setBackground(Color.BLACK);

			//			popupListener = new PopupListener(this);

			getPlotDetectorMenu(null); // initialises the menu immediately

			//			addMouseListener(popupListener);	

			//			spectroZoomer=new Zoomer(spectroZoomable=new SpectrogramZoomer(spectrogramDisplay,this), this);

			createImage();

			if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				SpecPanelOfflineMouse specPanelOfflineMouse = new SpecPanelOfflineMouse(this);
				addMouseListener(specPanelOfflineMouse);
				addMouseMotionListener(specPanelOfflineMouse);

				specPanelMouse = new SpecPanelMouse(this);
				addMouseListener(specPanelMouse);
				addMouseMotionListener(specPanelMouse);
			}
			else {
				specPanelMouse = new SpecPanelMouse(this);
				addMouseListener(specPanelMouse);
				addMouseMotionListener(specPanelMouse);
			}

			channelLabel.setFont(PamColors.getInstance().getBoldFont());
			setLayout(new FlowLayout(FlowLayout.LEFT));
			add(channelLabel);

			if (colourArray != null) {
				setBackground(colourArray.getColours()[0]);
			}

			subscribeDataBlocks();



			//			this.setToolTipText("Spectrogram Display");
			//			frequencyTooltip = createToolTip();

		}

		public SpectrogramDisplay getSpectrogramDisplay() {
			return this.specDisplay;
		}

		private void setImagePos(int imagePos) {
			synchronized (lineSynch) {
				this.imagePos = imagePos;
			}
		}

		public void subscribeDataBlocks() {

			viewedDataBlocks.clear();
			PamDataBlock dataBlock;
			detectorDataBlocks = PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
			//			if (spectrogramParameters.showDetector == null || spectrogramParameters.showDetector[panelId] == null) return;

			for (int i = 0; i < detectorDataBlocks.size(); i++) {
				//				checkMenuItem = (JCheckBoxMenuItem) detectorMenu.getComponent(i);
				dataBlock = detectorDataBlocks.get(i);
				boolean obs = false;
				if (dataBlock.canDraw(spectrogramProjector)) {
					if (spectrogramParameters.getOverlayDataInfo(dataBlock, panelId).select) {
						//					if (spectrogramParameters.getShowDetector(panelId,i)) {
						viewedDataBlocks.add(dataBlock);
						dataBlock.addObserver(this);
						obs = true;
					}
				}
				if (!obs) {
					dataBlock.deleteObserver(this);
				}
			}
		}

		void destroyPanel() {
			if (detectorDataBlocks == null) return;
			for (int i = 0; i < detectorDataBlocks.size(); i++) {
				detectorDataBlocks.get(i).deleteObserver(this);
			}
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

		private void createImage() {
			int imageWidth;
			int imageHeight;
			// only ever create the image if it's size has changed.
			synchronized (spectrogramDisplay) {	
				imageWidth = getSpectrogramImageWidth();
				imageHeight = getImageHeight();
				if (imageHeight<= 0 || imageWidth <= 0) {
					return;
				}
				//				System.out.println(String.format("Spec Image width and height = %d,%d", imageWidth, imageHeight));
				if (specImage == null || specImage.getWidth() != imageWidth || specImage.getHeight() != imageHeight) { 
					//TODO FIXME - this was causeing problems when the width is coming back at 4million in viewer mode giving VM outof memory error -Graham
					if (imageWidth>4000000) imageWidth=2000;
					if (imageHeight>4000000) imageHeight=2000; //you never know
					//if one value slightly less than 4000000 then can still run out of memory. 
					if (imageWidth*imageHeight>(1024*1024*300)/3) {
						//3 bytes per colour 300MB max for image
						System.err.println("Warning: SpectrogramDisplay: "
								+ "Image size was too big memory @ " +  (imageWidth*imageWidth)
								+ " default 2000x2000 applied");
						imageWidth=2000;
						imageHeight=2000;
					}

					specImage = new BufferedImage(imageWidth, imageHeight,
							BufferedImage.TYPE_INT_RGB);
					specFloatData = new float[imageWidth][imageHeight];
					//				specImage.setParentComponent(this);
					writableRaster = specImage.getRaster();
					setImagePos(-1);
					firstWrapDone = false;
				}

				//			currentTimeMilliseconds = 0;

				if (colourArray != null) {
					Graphics g = specImage.getGraphics();
					//				g.setColor(colourArray.getColours()[colourArray.getNumbColours()-1]);
					//				g.setColor(colourArray.getColours()[0]);
					g.setColor(Color.MAGENTA);
					specImage.getGraphics().fillRect(0, 0, imageWidth, imageHeight);
				}
			}

			channelLabel.setText(String.format("channel %d", spectrogramParameters.channelList[panelId]));
		}

		/**
		 * 
		 * @return XScale in pixels per second 
		 */
		double getXScale() {
			return getWidth() / spectrogramParameters.displayLength;
		}


		/**
		 * Called when new spectrogram data arrive. 
		 * @param obs PAmDataBlock that sent the data
		 * @param dataUnit FFT Data unit
		 * @param panelNumber number of panel to update. 
		 */
		private void drawSpectrogram(PamObservable obs, FFTDataUnit dataUnit, int panelNumber) {
			/*
			 * check the image is the correct width and make a new one if necessary
			 * this tends to occur when the display is using a fixed number of
			 * pixels per fftSlice.
			 */
			//			spectrogramParameters.wrapDisplay = true;
			//			System.out.println("Draw spectrogram at " + PamCalendar.formatTime2(dataUnit.getTimeMilliseconds(),3));
			if (sourceFFTDataBlock == null) {
				return;
			}
			
			if (dataUnit.getStartSample() < lastDataSample) {
				clearImage();
			}
			lastDataSample = dataUnit.getStartSample();
			
			if (!spectrogramParameters.timeScaleFixed) {
				int newWidth = getSpectrogramImageWidth();
				if (newWidth != specImage.getWidth()) {
					createAllImages();
				}
			}


			ComplexArray fftData = dataUnit.getFftData();

			double[] colval;

			/** getSpectrogramData() will return magnitude for FFTDataUnits or
			 * direction for Azigram units
			 */ 
			double[] cellValues = dataUnit.getSpectrogramData();
			double[] dBlevel = dataUnit.getMagnitudeData();

			// int xStart = imagePos;
			int xDraw = imagePos;

			// get the channel number in order to calculate gains.  If the FFT data block we're displaying is actually dealing
			// with sequence numbers, simply take the lowest channel in the channelMap and use that
			int channelNumber = sourceFFTDataBlock.getARealChannel(spectrogramParameters.channelList[panelNumber]);

			int iBin = 0;
			//			daqProcess.prepareFastAmplitudeCalculation(channelNumber);

			int imageWidth, imageHeight;
			synchronized (spectrogramDisplay) {
				imageHeight = specImage.getHeight();
				imageWidth = specImage.getWidth();
				float sampleRate = sourceFFTDataBlock.getSampleRate();
				/*
				 * minBin and maxBin are bins in the FFT data. 
				 */
				//				int maxBin = Math.min(freqBinRange[1], fftData.length - 1);
				//				int minBin = Math.max(0, freqBinRange[0]);sourceFFTDataBlock.getFftLength();
				//				if (maxBin < 0) return;
				int minBin = 0;
				int maxBin = Math.min(cellValues.length, imageHeight)-1;
				/**
				 * With new scroll system, drawing on the base image will 
				 * always wrap - it will be given the appearance of 
				 * scrolling at the point it's rendered onto the actual display in two parts. 
				 */
				//				if (spectrogramParameters.wrapDisplay) {
				/*
				 * ImagePos is set to -1 when the image is created, we add one
				 * to it before drawing ever starts. 
				 */
				xDraw = imagePos;

				// for (int p = 0; p < spectrogramParameters.pixelsPerSlics; p++) {
				if (++xDraw >= imageWidth) {
					xDraw = 0;
					firstWrapDone = true;
					if (viewerMode) {
						setImagePos(-1);
						return;
					}
				}
				//				System.out.println(String.format("Draw slice %d of %d", xDraw, specImage.getWidth()));
			
				for (int i = minBin; i <= maxBin; i++) {
					colval = colorValues[getColourIndex(cellValues[i])].clone();

					//Hack to check for Azigram
					if ( dBlevel[i] != cellValues[i]) {	  
						colval = fadePixel(colval,dBlevel[i]);
					}

					writableRaster.setPixel(xDraw, imageHeight - iBin - 1, colval);
					// }
					if (++iBin >= imageHeight) {
						break;
					}
				}
				// need to update currentTimeMilliseconds
				// at the same time as imagePos !
				imagePos = xDraw;// += spectrogramParameters.pixelsPerSlics;
				currentTimeMilliseconds = dataUnit.getTimeMilliseconds();
				//				System.out.println("Current time millis " + PamCalendar.formatTime(currentTimeMilliseconds) + 
				//						"." + new Integer((int) (currentTimeMilliseconds%1000)));
			}

			int newPos = imagePos;
			int len = imageHeight;
			// int lineWidth = Math.max(2, spectrogramParameters.pixelsPerSlics);
			//			if (spectrogramParameters.wrapDisplay) {
			newPos = imagePos;
			if (++newPos >= imageWidth) {
				newPos = 0;
				if (viewerMode) {
					setImagePos(-1);
					return;
				}
			}
			if (!viewerMode && spectrogramParameters.wrapDisplay) {
				for (int i = 0; i < len; i++) {
					writableRaster.setPixel(newPos, i, overlayColour);
				}
			}

			repaint(50);

			if (panelId == 0) notifyDisplayPanels(0);
		}

		/**
		 * Calculate db values and store them in the floating point
		 * array prior to converting to colour and writing into 
		 * the main image. 
		 * @param xDraw
		 * @param fftMagSq
		 */
		private void fillSpectrogramFLoatLine(int xDraw, double[] fftMagSq) {
			int minBin = 0;
			int maxBin = Math.min(fftMagSq.length, writableRaster.getHeight())-1;
			int h = writableRaster.getHeight();
			for (int i = minBin; i <= maxBin; i++) {
				specFloatData[xDraw][i] = (float) fftMagSq[i];
			}
		}

		private void drawSpectrogramLine(WritableRaster writableRaster,
				int xDraw, float[] floatLine, double[] dBlevel) {
			int minBin = 0;
			int maxBin = Math.min(floatLine.length, writableRaster.getHeight())-1;
			// at start of image filling, check the calibration. 
			int h = writableRaster.getHeight();
			for (int i = minBin; i <= maxBin; i++) {

				double[] colval = colorValues[getColourIndex(floatLine[i])].clone();
				
				//Hack to check for Azigram
				if (dBlevel!=null) {	  
					colval = fadePixel(colval,dBlevel[i]);
				}
				writableRaster.setPixel(xDraw, h - i - 1, colval);
			}

			imageLines++;

		}
		
		/**
		 * Hack to display Azigram data on a black background with pseudo transparency
		 * Eventually would be good to make spectrogram an ARGB image, remove this hack,
		 * and to provide some user-facing controls for adjusting transparency &
		 * thresholding of the Azigram. 
 		 * @param colval - original colour of pixel
		 * @param dBlevel - level of pixel used to determine fade 
		 */
		public double[] fadePixel(double[] colval, double dBlevel ) {
			if (dBlevel < fadeFloor) { // Levels below the floor are all the same color as floor
				return fadeFloorColor;
			}
			
			if (dBlevel < fadeThreshold){ //levels above threshold not faded
				// Linear fade between fadeStart and fadeStop when dB levels lower than threshold
				double fade = fadeStart+(fadeThreshold-dBlevel)*((fadeStop-fadeStart)/(fadeThreshold-fadeFloor));
				colval[0] = Math.max(colval[0]-fade, 0);
				colval[1] = Math.max(colval[1]-fade, 0);
				colval[2] = Math.max(colval[2]-fade, 0);
			}
			return colval;
		}
		
		/**
		 * Called to recopy spectrogram image data from the 
		 * float data into the actual image. 
		 */
		public void updateImageAmplitude() {
			// check the image and the float array are the same size
			if (writableRaster == null || specFloatData == null) {
				return;
			}
			if (writableRaster.getWidth() != specFloatData.length) {
				return;
			}
			if (writableRaster.getHeight() != specFloatData[0].length) {
				return;
			}
			for (int i = 0; i < specFloatData.length; i++) {
				drawSpectrogramLine(writableRaster, i, specFloatData[i], null);
			}
			repaint();
		}

		private Object lineSynch = new Object();
		private double[] scalingImageLine;
		private int scalingImagePoints;
		private int drawViewerSpectrogram(PamObservable obs, FFTDataUnit fftUnit, int panelNumber) {

			/*
			 * This behaves quite similarly to drawSpectrgram except that the image never wraps
			 * i.e. we just stop drawing. It's also possible that there are gaps in the spectrogram
			 * so we'll have to leave gaps if the times don't add up. 
			 */
			long s = viewerScroller.getValueMillis(); // start of image. 
			long e = s + (long) (spectrogramParameters.displayLength * 1000.);
			// work out how many pixels per millisecond there are on the display image
			double timeScale = (double) sourceFFTDataBlock.getSampleRate() / (double) sourceFFTDataBlock.getFftHop() / 1000.;
			double fairGap = Math.max(2.5,  timeScale);
			double xPos = (fftUnit.getTimeMilliseconds()-s) * timeScale;
			//			System.out.println(String.format("ch %d,  t %s, samp %d, xpos %3.1f, imagePos %d", 
			//					this.panelId, PamCalendar.formatTime2(fftUnit.getTimeMilliseconds(), 3), fftUnit.getStartSample(), xPos, imagePos));
			if (xPos < -.5) {
				return -1;
			}
			if (Math.abs(xPos - imagePos) > fairGap) {
				imagePos = (int) xPos;
				//				System.out.println("Skip to pixel " + imagePos);
			}
			if (imagePos >= writableRaster.getWidth()) {
				return -1;
			}

			/**
			 * Spectrogram image may be self scaling to stop it getting daft large in viewer mode. 
			 * Therefore sum up data in scalingImageLine until the number of points in 
			 * that line equals the overlap. Image pixels set every time in any case to 
			 * allow for possibility that last bin never gets completed. 
			 */
			synchronized (lineSynch) {
				double [] cellValue = fftUnit.getSpectrogramData();
				if (scalingImageLine == null) {
					scalingImageLine = new double[cellValue.length];
				}
				spectrogramPixelOverlap = Math.max(spectrogramPixelOverlap, 1);
				for (int i = 0; i < cellValue.length; i++) {
					scalingImageLine[i] += cellValue[i]/spectrogramPixelOverlap;
				}
				//				ComplexArray fftData = fftUnit.getFftData();
				//				if (scalingImageLine == null) {
				//					scalingImageLine = new double[fftData.length()];
				//				}
				//				spectrogramPixelOverlap = Math.max(spectrogramPixelOverlap, 1);
				//				for (int i = 0; i < fftData.length(); i++) {
				//					scalingImageLine[i] += fftData.magsq(i)/spectrogramPixelOverlap;
				//				}
				if (imagePos >= 0) {
					fillSpectrogramFLoatLine(imagePos, scalingImageLine);
					//Hack to determine if displaying Azigram
					if (cellValue == fftUnit.getSpectrogramData() )
						drawSpectrogramLine(writableRaster, imagePos, specFloatData[imagePos], fftUnit.getMagnitudeData());
					else 
						drawSpectrogramLine(writableRaster, imagePos, specFloatData[imagePos], null);
				}
				if (++scalingImagePoints >= spectrogramPixelOverlap) {
					imagePos++;
					scalingImageLine = null;
					scalingImagePoints = 0;
				}

			}

			return imagePos;
		}



		/**
		 * Clear the background image. 
		 */
		private void clearImage() {
			if (specImage == null) {
				return;
			}
			imagePos = 0;
			Graphics g = specImage.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, specImage.getWidth(), specImage.getHeight());
			imageLines = 0;
			synchronized (lineSynch) {
				scalingImageLine = null;
				scalingImagePoints = 0;
			}
			for (int i = 0; i < specFloatData.length; i++) {
				for (int j = 0; j < specFloatData[i].length; j++) {
					specFloatData[i][j] = Float.NaN;
				}
			}
		}


		@Override
		public void repaint(){
			super.repaint();
			if (spectrogramPlotPanel!=null) spectrogramPlotPanel.repaintSidePanel();
		}

		@Override
		public void repaint(long time){
			super.repaint(time);
			if (spectrogramPlotPanel!=null) {
				spectrogramPlotPanel.repaint(time);
				spectrogramPlotPanel.repaintSidePanel();
			}
		}
		
		/**
		 * @return the viewedDataBlocks
		 */
		public ArrayList<PamDataBlock> getViewedDataBlocks() {
			return viewedDataBlocks;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			Graphics2D g2d = (Graphics2D) g;
//			 g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
		       

			Dimension newSize = getSize();

			int channel = spectrogramParameters.channelList[panelId];

			//			if (viewerMode) {
			//				directDrawProjector.clearHoverList();  move down into the 'else' block, so that we're not clearing the list if the image is frozen
			//			}

			//			double secs = newSize.width * sourceFFTDataBlock.getFftHop() / sampleRate;
			//			System.out.println(String.format("Width = %d pixs = %5.2fs", 
			//					newSize.width, secs));
			//			sayWithDate("Draw SpectrogramPanel");

			//			if (sourceFFTDataBlock == null || (
			//					(1<<channel & 
			//							sourceFFTDataBlock.getChannelMap()) == 0)) {
			if (sourceFFTDataBlock == null || (
					(1<<channel & 
							sourceFFTDataBlock.getSequenceMap()) == 0)) {
				g.setColor(Color.RED);
				FontMetrics fm = g.getFontMetrics();
				String errTxt = String.format("FFT Data unavailable for channel %d", 
						spectrogramParameters.channelList[panelId]);
				int labelWidth = fm.charsWidth(errTxt.toCharArray(), 0, errTxt.length());
				g.drawString(errTxt, newSize.width / 2 - labelWidth / 2, newSize.height /2 - fm.getHeight());
				errTxt = "Check FFT parameters or select a different channel to display";
				labelWidth = fm.charsWidth(errTxt.toCharArray(), 0, errTxt.length());
				g.drawString(errTxt, newSize.width / 2 - labelWidth / 2, newSize.height /2 + fm.getHeight());
				return;
			}

			if (!newSize.equals(panelSize)) {
				amplitudePanel.SetAmplitudePanelBorder();
				panelSize = newSize;
			}

			if (!spectrogramParameters.timeScaleFixed) {
				if (getSpectrogramImageWidth() != specImage.getWidth()) {
					createAllImages();
				}
			}

			if (specImage == null)
				return;

			double scaleWidthfactor = 1;
			scaleX = this.getWidth() / (double) getFrozenImageWidth();
			scaleY = this.getHeight() / (double) specImage.getHeight(null);
			//			AffineTransform xform = new AffineTransform();
			//			xform.translate(scaleX * ((1 - scaleWidthfactor) / 2)
			//					* specImage.getWidth(null), 0);
			//			xform.scale(scaleX * scaleWidthfactor, scaleY);
			//			BufferedImage imageToDraw = specImage;


			/**
			 * If we have a frozenImage object, it means that the user is drawing a box in the spectrogram.  So don't try to update everything,
			 * just use the image and draw the box
			 */
			if (frozenImage != null && !viewerMode) {
				//				System.out.println("Drawing frozen image...");
				//				imageToDraw = frozenImage;
				g2d.drawImage(frozenImage, 0, 0, getWidth(), getHeight(), 0, 0, frozenImage.getWidth(), frozenImage.getHeight(), this);
				//				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));


				// draw the overlay data...
				directDrawProjector.clearHoverList();	// clear the existing hoverData list before repopulating it with objects
				long displayStartTime = frozenTimeMilliseconds;
				double directImagePos;
				displayStartTime -= spectrogramParameters.displayLength * 1000.;
				if (spectrogramParameters.wrapDisplay) {
					//					directImagePos = (double) imagePos * (double) getWidth() / specImage.getWidth();
					directImagePos = (double) frozenImagePos * (double) getWidth() / specImage.getWidth();
				}
				else {
					directImagePos = getWidth();
				}
				//				System.out.println(" Panel " + String.valueOf(this.panelId) + " directImagePos = " + String.valueOf(directImagePos));
				directDrawProjector.setLogScale(frequencyAxis.isLogScale());
				directDrawProjector.setScales(getWidth(), getHeight(), 
						displayStartTime,
						spectrogramParameters.displayLength * 1000 / spectrogramPixelOverlap, 
						spectrogramParameters.frequencyLimits, sampleRate, directImagePos);
				drawOverlayData(g);

			}


			/**
			 * Work out which bit of the image we want to draw - note that 
			 * these may be beyond the end of the image and will need to be wrapped. 
			 */
			else {
				directDrawProjector.clearHoverList();	// clear the existing hoverData list before repopulating it with objects

				fillSpectrogramImage(g2d, this);

				double millisPerBin = sampleRate / 1000. / sourceFFTDataBlock.getFftHop();
				directDrawProjector.setLogScale(frequencyAxis.isLogScale());
				spectrogramProjector.setScales(millisPerBin,
						2. / sourceFFTDataBlock.getFftLength(), specImage.getWidth(),
						specImage.getHeight());
				/*
				 * direct draw projector currently only used in viewer mode, but may change this
				 * to improve rendering of lines. 
				 */
				long displayStartTime = currentTimeMilliseconds;
				double directImagePos;
				if (viewerMode) {
					displayStartTime = viewerScroller.getValueMillis();
					directImagePos = getWidth();
				}
				else {
					displayStartTime -= spectrogramParameters.displayLength * 1000.;
					if (spectrogramParameters.wrapDisplay) {
						directImagePos = (double) imagePos * (double) getWidth() / specImage.getWidth();
					}
					else {
						directImagePos = getWidth();
					}
				}
				// draw the channel label - no idea why this got dropped. 
				String chanStr = "Ch " + channel;

				// Use a white background and use channel colouring instead of contrasting colour
				FontMetrics fm = g.getFontMetrics();
				Rectangle2D rect = fm.getStringBounds(chanStr, g);
				int x = 3;
				int y = 1 + g.getFontMetrics().getAscent();
				Color whiteShade =new Color(255,255,255,140);
				g.setColor(whiteShade);
				g.fillRect(x,
						y - fm.getAscent(),
						(int) rect.getWidth(),
						(int) rect.getHeight());

				g.setColor(PamColors.getInstance().getChannelColor(channel));
				//g.setColor(colourArray.getContrastingColour());
				g.drawString(chanStr, x, y);

				drawMarkLabel(g);

				directDrawProjector.setLogScale(frequencyAxis.isLogScale());
				directDrawProjector.setScales(getWidth(), getHeight(), 
						displayStartTime,
						spectrogramParameters.displayLength * 1000 / spectrogramPixelOverlap, 
						spectrogramParameters.frequencyLimits, sampleRate, directImagePos);

				//draw any overlay data
				drawOverlayData(g);

				//draw the zoomer shape
				//				if (spectroZoomable.isComplete()) spectroZoomer.paintShape( g, this, true);
				//				spectroZoomer.paintShape( g, this, false);

				/**
				 * And a drawing progress bar for playback!
				 */
				if (playbackStatus == PlaybackProgressMonitor.PLAY_START &&
						viewerScroller != null && timeAxis != null &&
						(playbackChannels & (1<<channel)) != 0) {
					double pos = (playbackTimeMillis - viewerScroller.getValueMillis())/1000.;
					int playX = (int) timeAxis.getPosition(pos);
					g.setColor(Color.RED);
					((Graphics2D) g).setStroke(new BasicStroke(1));
					g.drawLine(playX, 0, playX, getHeight());
				}
			}		

			//			System.out.println("Draw mouse mark");
			drawMouseMark(g2d);

		}
		private void drawMouseMark(Graphics2D g2d) {
			if (frozenImage == null) {
				//				System.out.println("Not Frozen");
				return;
			}
			OverlayMark currentMark = overlayMarker.getCurrentMark();
			if (currentMark == null) {
				//				System.out.println("No Mark");
				return;
			}
			//			System.out.println("Draw mark " + currentMark.toString());
			if (currentMark.isHidden()) {
				//				System.out.println("Mark hidden");
				return;
			}
			if (mousePressedButton != MouseEvent.BUTTON1) {
				//				System.out.println("Not button 1");
				return;
			}
			if (mouseDownPoint != null && currentMousePoint != null) {

				int x = Math.min(mouseDownPoint.x, currentMousePoint.x);
				int y = Math.min(mouseDownPoint.y, currentMousePoint.y);
				int w = Math.abs(mouseDownPoint.x - currentMousePoint.x);
				int h = Math.abs(mouseDownPoint.y - currentMousePoint.y);
				g2d.setColor(markThis ? freezeColour : freezeColour2);
				//					g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(3));
				g2d.drawRect(x, y, w, h);
				if (x + w > getWidth()) {
					g2d.drawRect(x - getWidth(), y, w, h);
				}
				if (x < 0) {
					g2d.drawRect(x + getWidth(), y, w, h);
				}
				drawMarkLabel(g2d);
			}
			else {

				System.out.println("no Mouse Points");
			}
		}

		private void fillSpectrogramImage(Graphics g, ImageObserver imageObserver) {
			if (spectrogramParameters.wrapDisplay || viewerMode) {
				//				System.out.println("Getting the frozen image");
				g.drawImage(specImage, 0, 0, getWidth(), getHeight(), 0, freqBinDisplayRange[0], specImage.getWidth(), freqBinDisplayRange[1], this);
			}
			else {
				// draw in two parts. 
				/*
				 * imagePos is the current x position within the image. 
				 * So drawing of the first part of the image will be from 
				 * imagePos+1 to the end of the image. Drawing of the second
				 * part will be from the start of the image to imagePos. 
				 * Just need to work out the equivalent xpos
				 */
				int usefulWidth = getFrozenImageWidth();
				int imageDrawPos = imagePos+1;
				int imageDrawStart = imageDrawPos - usefulWidth;
				int screenXPos = 0;
				int screenXEnd;
				if (imageDrawPos > usefulWidth) {
					g.drawImage(specImage, 0, 0, getWidth(), getHeight(), imageDrawPos-usefulWidth+1, freqBinDisplayRange[0], 
							imageDrawPos, freqBinDisplayRange[1], this);
				}
				else {
					int part1Pixs = (int) ((imageDrawPos+1)*scaleX);
					int remainingPixs = usefulWidth - imageDrawPos;
					g.drawImage(specImage, getWidth()-part1Pixs, 0, getWidth(), getHeight(), 0, freqBinDisplayRange[0], 
							imageDrawPos, freqBinDisplayRange[1], this);
					if (firstWrapDone) {
						g.drawImage(specImage, 0, 0, getWidth()-part1Pixs, getHeight(), specImage.getWidth()-remainingPixs, 
								freqBinDisplayRange[0], specImage.getWidth(), freqBinDisplayRange[1], this);
					}						
				}

				//				screenXPos = (int) (this.getWidth() - imageDrawPos*scaleX);
				//				int winH = getHeight();
				//				int imH = specImage.getHeight();
				//				if (firstWrapDone) {
				//					//						g.drawImage(specImage, 0, 0, screenXPos, winH, imageDrawPos, 0, 
				//					//								specImage.getWidth(), imH, null);
				//				}
				//				g.drawImage(specImage, screenXPos, 0, getWidth(), winH, Math.max(0, imageDrawStart), freqBinRange[0], 
				//						imageDrawPos, freqBinRange[1], null);

			}
		}

		/**
		 * Label the currently selected Mark Observers, but only if the 
		 * markType is not null (presently only DIFAR module). 
		 * Label is drawn centred at the top of each spectrogram panel.
		 */
		void drawMarkLabel(Graphics g) {
			String markStr = "";
			boolean shouldDrawMarkLabel = false;
			//			ArrayList<OverlayMarkObserver> observers = getOverlayMarker().getObservers();
			ArrayList<OverlayMarkObserver> observers = overlayMarker.getObservers();
			for (int i = 0; i<observers.size(); i++) {
				if (observers!=null) {
					String obsStr = observers.get(i).getObserverName();
					String markType = "";
					try {
						SpectrogramMarkObserver smo = ((SpectrogramMarkConverter) observers.get(i)).getSpectrogramMarkObserver();
						if (smo.getMarkName() != null) {
							markType =  " - " + smo.getMarkName();
							shouldDrawMarkLabel |= true;
							markStr += markType + "\n";
						}
					}catch (Exception e) {
						//	System.out.println(e.toString());
					}
				}
			}

			if (!shouldDrawMarkLabel)
				return;

			Rectangle2D bounds = getBounds();
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D rect = fm.getStringBounds(markStr, g);

			int x = (int) (bounds.getMinX() + (bounds.getWidth() - rect.getWidth()) / 2);
			//			int x = (int) (rect.getX()+rect.getWidth()+25);
			int yStart = 1 + g.getFontMetrics().getAscent();
			int y = yStart;
			String prefix = "Marking ";
			rect = fm.getStringBounds(prefix, g);
			int xMax = 0;
			int yMax = 0;
			Color whiteShade =new Color(255,255,255,160);
			g.setColor(whiteShade);
			for (String line : markStr.split("\n")) {
				bounds = fm.getStringBounds(prefix+line, g);
				xMax = (int) (Math.max(bounds.getMaxX(), xMax));
				yMax += g.getFontMetrics().getHeight();
			}
			g.fillRect(x,
					yStart - fm.getAscent(),
					xMax,
					yMax);

			int channel = spectrogramParameters.channelList[panelId];
			g.setColor(PamColors.getInstance().getChannelColor(channel));
			g.drawString(prefix, x, y);
			x += (int) (rect.getWidth());
			for (String line : markStr.split("\n")) {
				g.drawString(line, x, y);
				y += g.getFontMetrics().getHeight();
			}
		}
		/**
		 * Draw graphic overlays (only used in offline viewer mode)
		 * @param g
		 */
		private void drawOverlayData(Graphics g) {
			//			sayWithDate("Draw overlay data");
			int n = viewedDataBlocks.size();
			for (int i = 0; i < n; i++) {
				drawOverlayData(g, viewedDataBlocks.get(i));
			}
		}

		/**
		 * Draw the overlay data for a specific data block
		 * <p>Only used in offline viewer mode
		 * @param g graphics handle
		 * @param usedDataBlock datablock reference.
		 */
		private void drawOverlayData(Graphics g, PamDataBlock usedDataBlock) {

			ListIterator<PamDataUnit> iterator;
			PamDataUnit dataUnit;
			//			System.out.println("Draw " + usedDataBlock.getDataName());
			//			sayWithDate(String.format("Draw %s from %s to %s", usedDataBlock.getDataName(),
			//					PamCalendar.formatDateTime(currentTimeMilliseconds -
			//							(long)(spectrogramParameters.displayLength * 1000.)), 
			//							PamCalendar.formatTime(currentTimeMilliseconds)));
			String name = spectrogramDisplay.getDataSelectorName(panelId);
			DataSelector dataSelector = usedDataBlock.getDataSelector(name, false);
			
			if (dataSelector != null && dataSelector.getParams().getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE) {
				dataSelector = null;
			}
			directDrawProjector.setDataSelector(dataSelector);
			if (usedDataBlock.getPamSymbolManager() != null) {
				directDrawProjector.setPamSymbolChooser(usedDataBlock.getPamSymbolManager().getSymbolChooser(name, directDrawProjector));
			}
			else {
				directDrawProjector.setPamSymbolChooser(null);
			}
			try {
				//				System.out.println("Unlocked" + usedDataBlock.getDataName());
				iterator = usedDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
//				int iUn = 0;
				while(iterator.hasPrevious()) {
					dataUnit = iterator.previous();
//					System.out.printf("Draw data %d/%d at %s\n", ++iUn, usedDataBlock.getUnitsCount(),
//							PamCalendar.formatTime(dataUnit.getTimeMilliseconds()));			
					// decide whether or not to display the data unit based on the current time, unless we have frozen the
					// image.  In that case, base the decision on the frozen time
					long timeToUse = currentTimeMilliseconds;
					if (frozenImage != null && !viewerMode) {
						timeToUse = frozenTimeMilliseconds;	
					}

					if (dataUnit.getTimeMilliseconds() < timeToUse -
							(long)(spectrogramParameters.displayLength * 1000.)) {
						break;
					}
					if (dataUnit.getTimeMilliseconds() > timeToUse) {
						continue;
					}
					int wantedMap = 1<<spectrogramParameters.channelList[panelId];
					int dataChanMap = dataUnit.getSequenceBitmap();
					if (dataChanMap != 0 && (wantedMap & dataChanMap) == 0) {
						continue;
					}
					if (dataSelector != null && dataSelector.scoreData(dataUnit) <= 0) {
						continue;
					}
					usedDataBlock.drawDataUnit(g, dataUnit, directDrawProjector);
				}
			}
			catch (Exception e) {
				// avoid synck lock Occasional exceptions are a concurrentmodifiedexception I think. 
//				System.out.println("Exception in wsl draw: " + e.getMessage());
//				e.printStackTrace();
			}

		}

		/*
		 * Now held within each spectrogram panel so that each panel
		 * can hold it's own settings. 
		 */
		JPopupMenu getPlotDetectorMenu(MouseEvent e) {
			if (spectrogramParameters == null) {
				return null;
			}
			int channel = spectrogramParameters.channelList[panelId];
			//			if ((detectorMenu == null && detectorDataBlocks != null) 
			//				|| (detectorDataBlocks != null && detectorDataBlocks.size() != checkMenuItems.length)) {
			detectorMenu = new JPopupMenu();
			detectorDataBlocks = PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
			checkMenuItems = new JCheckBoxMenuItem[detectorDataBlocks.size()];
			//			DisplaySelection displaySelection = new DisplaySelection(this);
			SettingsAction settingsAction = new SettingsAction();
			JMenuItem menuItem;
			if (viewerMode && e != null) {
				if (playbackStatus == PlaybackProgressMonitor.PLAY_START) {
					menuItem = new JMenuItem("Stop playback");
					menuItem.addActionListener(new MenuCancelPlayback());
					detectorMenu.add(menuItem);
				}
				else {
					menuItem = new JMenuItem("Play channel " + channel + " from start");
					menuItem.addActionListener(new MenuPlayChannel(channel));
					detectorMenu.add(menuItem);		
					long currentTime = timeFromMouseX(e.getX());
					menuItem = new JMenuItem("Play channel " + channel + " from mouse position");
					menuItem.addActionListener(new MenuPlayFrom(channel, currentTime));
					detectorMenu.add(menuItem);		

					//					int playChannels = PlaybackControl.getViewerPlayback().getPlaybackParameters().channelBitmap;
					//					if (playChannels != 1<<channel) {
					//						menuItem = new JMenuItem("Play channel(s) " + PamUtils.getChannelList(playChannels));
					//						menuItem.addActionListener(new MenuPlayAll());
					//						detectorMenu.add(menuItem);
					//					}
				}
			}
			menuItem = new JMenuItem("Settings ...");
			menuItem.addActionListener(settingsAction);
			detectorMenu.add(menuItem);
			detectorMenu.addSeparator();

			overlayDataManager.addSelectionMenuItems(detectorMenu, null, false, false, true);
			//			PamDataBlock dataBlock;
			//			for (int i = 0; i < detectorDataBlocks.size(); i++) {
			//				dataBlock = detectorDataBlocks.get(i);
			//				if (dataBlock.canDraw(spectrogramProjector)) {
			//					checkMenuItems[i] = new JCheckBoxMenuItem(dataBlock
			//							.getDataName());
			//					checkMenuItems[i].addActionListener(displaySelection);
			//					checkMenuItems[i].setActionCommand(Integer.toString(i));
			//					detectorMenu.add(checkMenuItems[i]);
			//				}
			//			}			
			//			InitialisePlotDetectorMenu();
			if (panelClipBoardCopier == null && getFrame() != null) {
				panelClipBoardCopier = new ClipboardCopier(getFrame().getContentPane());
			}
			if (panelClipBoardCopier != null) {
				detectorMenu.addSeparator();
				detectorMenu.add(panelClipBoardCopier.getCopyMenuItem());
			}
			//			}
			return detectorMenu;
		}

		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return SpectrogramDisplay.this.getRequiredDataHistory(o, arg);
			//			long history = (long) getXDuration();
			//			if (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER) {
			//				if (viewerScroller != null) {
			//					history = viewerScroller.getRangeMillis();
			//				}
			//			}
			//			if (frozen) { // then the mouse is down !
			//				history = Math.max(history, PamCalendar.getTimeInMillis()-mouseDownTime); 
			//			}
			//			return history + 1000;
		}

		@Override
		public String getObserverName() {
			return "Spectrogram display panel";
		}

		@Override
		public void noteNewSettings() {
			//System.out.println("New spectrogram panel settings");
			//			setParams(spectrogramParameters);
		}

		@Override
		public void setSampleRate(float sampleRate, boolean notify) {
			//			System.out.println("New spectrogram panel sampleRate");
		}

		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
			spectrogramDisplay.masterClockUpdate(milliSeconds, sampleNumber);
		}

		@Override
		public void addData(PamObservable obs, PamDataUnit newData) {

			//for (int i = 0; i < spectrogramPanel.length; i++) {
			//			AcousticDataUnit acousticData = (AcousticDataUnit) newData;
			//			if ((1<<spectrogramParameters.channelList[panelId] & acousticData.getChannelBitmap()) == 0) {
			//				return;
			//			}
			//			Rectangle r = obs.drawDataUnit(specImage.getGraphics(), newData,
			//					spectrogramProjector);
			//			if (r != null) {
			//				//				System.out.println("Repaint spectorgram panel id " + panelId);
			//				spectrogramPanels[panelId].repaint();
			//			}
			//}
			repaint(100);

		}
		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			
		}

		@Override
		public void receiveSourceNotification(int type, Object object) {
			if (type == AcquisitionProcess.FIRSTDATA) {
				/*
				 *  clear out the spectrogram - does nothing first time, but will clear start of 
				 *  display panel when multiple files with gaps processing - looks neater. 
				 */
//				clearImage();
			}
		}

		@Override
		public String getToolTipText(MouseEvent mouseEvent)  {
			/*
			 * Make a standard time / frequency string
			 * THen if it's viewer mode, also see if we're hovered
			 * over an object of some sort. 
			 */
			String str = getMousePosText(mouseEvent.getPoint());
			if (str == null) {
				return null;
			}
			if (viewerMode) {
				String moreStr = 
						directDrawProjector.getHoverText(mouseEvent.getPoint());
				if (moreStr != null) {
					str += "<p>" + moreStr;
				}
			}
			return "<html>" + str + "</html>";
		}

		public String getMousePosText(Point pt) {
			if (sourceFFTDataBlock == null) {
				return "No input data";
			}
			if (frequencyAxis == null) {
				return null;
			}
			double frequency = frequencyAxis.getDataValue(pt.y) * freqAxisScale;
			String str;
//			if (sourceFFTDataBlock.getClass() == FFTDataBlock.class) {
////				str = FrequencyFormat.formatFrequency(frequency, true);
//				str = SIUnitFormat.formatValue(frequency, "Hz");
//			}
//			else {
				DataTypeInfo scaleInfo = sourceFFTDataBlock.getScaleInfo();
				str = SIUnitFormat.formatValue(frequency, scaleInfo.dataUnits.toString());
//			}

			if (viewerScroller != null) {
				long t = timeFromMouseX(pt.x);
				str = String.format("%s<p>%s %s s", 
						str, PamCalendar.formatDate(t, true), PamCalendar.formatTime(t, true, true));	
			}
			return str;
		}



		//		class DisplaySelection implements ActionListener {
		//
		//			SpectrogramPanel spectrogramPanel;
		//			DisplaySelection(SpectrogramPanel spectrogramPanel) {
		//				this.spectrogramPanel = spectrogramPanel;
		//			}
		//
		//			public void actionPerformed(ActionEvent e) {
		//				String str = e.getActionCommand();
		//				int menuId;
		//				try {
		//					menuId = Integer.valueOf(str);
		//				} catch (Exception ex) {
		//					return;
		//				}
		//				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
		//
		//				PamDataBlock dataBlock = detectorDataBlocks.get(menuId);
		//				boolean show = menuItem.isSelected();
		//				spectrogramParameters.setShowDetector(panelId, menuId, show);
		//				if (show) {
		//					dataBlock.addObserver(spectrogramPanel);
		//				} else {
		//					dataBlock.deleteObserver(spectrogramPanel);
		//				}
		//				subscribeDataBlocks();
		//				repaint();
		//			}
		//
		//		}

		//		void InitialisePlotDetectorMenu() {
		//			if (detectorMenu == null) return;
		//			if (checkMenuItems == null) return;
		//			PamDataBlock dataBlock;
		//			detectorDataBlocks = PamController.getInstance().getDataBlocks(PamDataUnit.class, true);
		//			//			if (spectrogramParameters.showDetector == null || spectrogramParameters.showDetector[panelId] == null) return;
		//			spectrogramParameters.getShowDetector(panelId, detectorDataBlocks.size()-1); // force check on size of showDetector variables
		//			for (int i = 0; i < detectorDataBlocks.size(); i++) {
		//				//				checkMenuItem = (JCheckBoxMenuItem) detectorMenu.getComponent(i);
		//				dataBlock = detectorDataBlocks.get(i);
		//				if (dataBlock.canDraw(spectrogramProjector) && checkMenuItems[i] != null) {
		//					checkMenuItems[i].setSelected(spectrogramParameters.getShowDetector(panelId,i));
		//					if (spectrogramParameters.getShowDetector(panelId,i)) {
		//						detectorDataBlocks.get(i).addObserver(this);
		//					}
		//					else {
		//						detectorDataBlocks.get(i).deleteObserver(this);
		//					}
		//				}
		//			}
		//		}

		@Override
		public void removeObservable(PamObservable o) {
			// TODO Auto-generated method stub

		}

		protected void freezeImage(boolean markThis) {
			BufferedImage oldImage = specImage;
			this.markThis = markThis;
			frozenTimeMilliseconds = currentTimeMilliseconds;
			//			System.out.println("Frozen time = " + PamCalendar.formatTime(frozenTimeMilliseconds, true));
			frozenImagePos = imagePos;
			//			frozenImage = new BufferedImage(getFrozenImageWidth(), oldImage.getHeight(), 
			//					BufferedImage.TYPE_INT_RGB);
			frozenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
			fillSpectrogramImage(frozenImage.getGraphics(), null);
			//			Graphics g = frozenImage.getGraphics();
			//			if (spectrogramParameters.wrapDisplay) {
			//				g.drawImage(oldImage, 0, 0, null);	
			//			}
			//			else {
			//				int imageDrawPos = imagePos+1;
			//				int screenXPos = (int) (this.getWidth() - imageDrawPos);
			//				if (firstWrapDone) {
			//					g.drawImage(specImage, 0, 0, screenXPos, oldImage.getHeight(), imageDrawPos, 0, 
			//							specImage.getWidth(), oldImage.getHeight(), null);
			//					//				g.setColor(Color.RED);
			//					//				g.drawLine(0, 10, screenXPos, winH/2);
			//				}
			//				g.drawImage(specImage, screenXPos, 0, getWidth(), oldImage.getHeight(), 0, 0, 
			//						imageDrawPos-1, oldImage.getHeight(), null);
			//			}
		}

		protected void unFreezeImage() {
			if (frozenImage == null) return;
			//			if (e.getButton() != MouseEvent.BUTTON1) return;
			frozenImage = null;
		}

		protected void sayMousePos(Point pt) {
			//			String str = getMousePosText(pt);
			//			setToolTipText("<html>" + str + "</html>");
		}

		long timeFromMouseX(int mouseX) {
			long t = (long) (mouseX * 1000. * spectrogramParameters.displayLength / getWidth());
			t += viewerScroller.getValueMillis();
			return t;
		}

		public GeneralProjector getProjector() {
			return directDrawProjector;
		}

		/**
		 * Moved from the outer class SpectrogramDisplay to this inner class
		 * @param downUp
		 * @param mouseEvent
		 * @param channel
		 * @param startTime
		 * @param duration
		 * @param f1
		 * @param f2
		 * @return
		 */
		private boolean notifyMarkObservers(int downUp, MouseEvent mouseEvent, int channel, long startTime, long duration, double f1, double f2) {
			/*
			 * Leave all the old mouse handling and drawing systems in place 
			 * and just create a mark from scratch to send to the observers. 
			 * A total fudge, but avoids rewriting a load of mouse handler crap. 
			 */
			//			System.out.println("Downup = " + downUp + " button = " + mouseEvent.getButton());
			//mouse drag events seem to pick up the wrong button number so can't include this !
			//			if (mousePressedButton != MouseEvent.BUTTON1) {
			//				return false;
			//			}
			//			if (mouseEvent.getButton() != MouseEvent.BUTTON1) {
			//				return false;
			//			}
			OverlayMark overlayMark = new OverlayMark(overlayMarker, this, null, 1<<channel, spectrogramProjector.getParameterTypes(), spectrogramProjector.getParameterUnits());
			//			overlayMark.addCoordinate(new TimeFrequencyPoint(startTime, f1)); // replace with Coordinate3d object, now that we're using the DirectDrawProjector
			//			overlayMark.addCoordinate(new TimeFrequencyPoint(startTime+duration, f2)); // replace with Coordinate3d object, now that we're using the DirectDrawProjector
			overlayMark.addCoordinate(new Coordinate3d(startTime, f1));
			overlayMark.addCoordinate(new Coordinate3d(startTime+duration, f2));
			overlayMark.setMarkType(OverlayMarkType.RECTANGLE);
			//			System.out.println("Make mark " + overlayMark.toString());
			overlayMarker.setCurrentMark(overlayMark);
			if (downUp == SpectrogramMarkObserver.MOUSE_UP) {
				//				System.out.println("Mouse up");
			}
			return overlayMarker.notifyObservers(downUp, ExtMouseAdapter.fxMouse(mouseEvent, null), overlayMarker, overlayMark);

			//		if (spectrogramParameters.useSpectrogramMarkObserver == null) return false;
			//		int n = Math.min(spectrogramParameters.useSpectrogramMarkObserver.length,
			//				SpectrogramMarkObservers.getSpectrogramMarkObservers().size());
			//		/**
			//		 * Put the button number -1 into the high word of the downUp
			//		 * command. Button numbers are actually 1, 2, 3 so this will make 
			//		 * 0,1,2, so for the standard button 1, the word will remain unchanged. For
			//		 * other buttons, the programmer will have to extract the top word. 
			//		 */
			//		int command = downUp;
			//		if (mouseEvent.getButton() > 0) {
			//			command += (mouseEvent.getButton()-1)<<16;
			//		}
			//		boolean ans = false;
			//		for (int i = 0; i < n; i++) {
			//			if (spectrogramParameters.useSpectrogramMarkObserver[i] == false) {
			//				continue;
			//			}
			//			if (SpectrogramMarkObservers.getSpectrogramMarkObservers().get(i).canMark()) {
			//				ans |= SpectrogramMarkObservers.getSpectrogramMarkObservers().get(i).spectrogramNotification(this, mouseEvent,
			//						command, channel, startTime, duration, f1, f2);
			//			}
			//		}
			//		return ans;
		}

		/**
		 * @return
		 */
		public OverlayMarker getMarker() {
			return overlayMarker;
		}

	}


	private void freezeImages(SpectrogramPanel selectedPanel) {
		for (int i = 0; i < spectrogramPanels.length; i++) {
			spectrogramPanels[i].freezeImage(spectrogramPanels[i] == selectedPanel);
		}
		//		System.out.println("Freeze");
		frozen = true;
	}

	private void unFreezeImages() {
		for (int i = 0; i < spectrogramPanels.length; i++) {
			spectrogramPanels[i].unFreezeImage();
		}
		//		System.out.println("UnFreeze");
		frozen = false;
	}

	class SpecPanelOfflineMouse extends MouseAdapter {

		private SpectrogramPanel spectrogramPanel;

		public SpecPanelOfflineMouse(SpectrogramPanel spectrogramPanel) {
			super();
			this.spectrogramPanel = spectrogramPanel;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			specMouseOffline(spectrogramPanel, e.getPoint());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			specMouseOffline(spectrogramPanel, e.getPoint());
			//			}
		}

	}

	class PopupListener extends MouseAdapter {

		SpectrogramPanel spectrogramPanel;
		public PopupListener(SpectrogramPanel spectrogramPanel) {
			super();
			this.spectrogramPanel = spectrogramPanel;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				spectrogramPanel.getPlotDetectorMenu(e)
				.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (viewerMode && e.getClickCount() == 2) {
				if (playbackStatus == PlaybackProgressMonitor.PLAY_END) {
					int channel = spectrogramParameters.channelList[spectrogramPanel.panelId];
					long currentTime = spectrogramPanel.timeFromMouseX(e.getX());
					long dispStartMillis = viewerScroller.getValueMillis();
					long endMillis = dispStartMillis + viewerScroller.getVisibleAmount();
					PlaybackControl.getViewerPlayback().playViewerData(1<<channel, currentTime, endMillis, new PlayProgress());
				}
				else {
					PlaybackControl.getViewerPlayback().stopViewerPlayback();
				}
			}
			else {
				maybeShowPopup(e);
			}
		}
	}
	class SpecPanelMouse extends PopupListener implements MouseMotionListener {

		SpectrogramPanel spectrogramPanel;

		long mouseDownTime, currentMouseTime;
		double mouseDownFrequency, currentMouseFrequency;

		public SpecPanelMouse(SpectrogramPanel spectrogramPanel) {
			super(spectrogramPanel);
			this.spectrogramPanel = spectrogramPanel;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			//			if (nMarkObservers == 0) {

			spectrogramAxis.requestFocusInWindow();
			
			if (scrollJumper != null) {
				scrollJumper.setSpectrogramPanel(spectrogramPanel);
			}
			
			if (spectrogramPanel.getMarker().getObserverCount() == 0) {
				super.mousePressed(e);
				return;
			}
			mousePressedButton = e.getButton();
			//						System.out.println("Pressed button  = " + e.getButton());
			currentMousePoint = mouseDownPoint = e.getPoint();
			freezeImages(spectrogramPanel);
			if (!fireMouseDownEvents(spectrogramPanel, e, mouseDownPoint)) {
				super.mousePressed(e);
			} 
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			//			if (spectrogramPanel.frozenImage == null) return;
			////			if (e.getButton() != MouseEvent.BUTTON1) return;
			boolean events = fireMouseUpEvents(spectrogramPanel, e, mouseDownPoint, e.getPoint());
			unFreezeImages();
			//			spectrogramPanel.frozenImage = null;
			if (!events) {
				super.mouseReleased(e);
			}	
			currentMousePoint = mouseDownPoint = null;
			mousePressedButton = MouseEvent.NOBUTTON;
			//			if (viewerMode) {
			repaintAll();
			//			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			/*
			 * Seems to be a bug in swing so it doesn't report the correct button !
			 */
			//			e = new MouseEvent(e.getComponent(), e.getX(), arg2, arg3, arg4, arg5, arg6, arg7)
			//			System.out.println("Dragged button  = " + e.getButton());
			spectrogramPanel.sayMousePos(e.getPoint());
			if (spectrogramPanel.frozenImage == null) return;
			currentMousePoint = e.getPoint();
			for (int i = 0; i < spectrogramPanels.length; i++) {
				spectrogramPanels[i].repaint();
			}
			if (!fireMouseDragEvents(spectrogramPanel, e, mouseDownPoint, e.getPoint())) {
				super.mouseDragged(e);
			}		

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			spectrogramPanel.sayMousePos(e.getPoint());
		}
	}

	class SplitPaneListener implements AncestorListener {

		@Override
		public void ancestorAdded(AncestorEvent event) {
			//			spectrogramAxis.repaint();		
			repaintAll();
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			//			spectrogramAxis.repaint();		
			repaintAll();		
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			//			spectrogramAxis.repaint();			
			repaintAll();	
		}

	}

	@Override
	public SpectrogramParameters getSpectrogramParameters() {
		if (getFrame() != null) {
			spectrogramParameters.boundingRectangle = getFrame().getBounds();
		}
		//		if (hidingPanel != null) {
		//			spectrogramParameters.hideSidePanels = !hidingPanel.getPanel().isExpanded();
		//		}
		return spectrogramParameters;
	}

	//changing the amplitude means we have to redraw the colour map
	private double[] prevAmplitudeLimits={0.0,0.0};
	
	@Override
	public void setSpectrogramParameters(
			SpectrogramParameters spectrogramParameters) {

		this.spectrogramParameters = spectrogramParameters;
		if (createColours()){
			//if true and in viewer mode then need to recolour. 
			if (viewerMode) setAmplitudeParams();
		}

		//if in viewer mode we set the amplitude params only if they are changed as processor intensive task
		if ((frozen || viewerMode) && !prevAmplitudeLimits.equals(spectrogramParameters.amplitudeLimits)){
			setAmplitudeParams();
		}

		//if in normal mode, not frozen and amplitude settings have changed then just recalc the amplitude bar
		if ((!viewerMode && !frozen) && !prevAmplitudeLimits.equals(spectrogramParameters.amplitudeLimits)) {
			if (amplitudeAxis != null) {
				paintAmplitudeImage();
				amplitudeAxis.setRange(spectrogramParameters.amplitudeLimits[0], spectrogramParameters.amplitudeLimits[1]);
			}
		}
		this.prevAmplitudeLimits=spectrogramParameters.amplitudeLimits;

		//set the frequencies
		calcFrequencyRangeDisplay();

		repaintAll();
	}

	/* (non-Javadoc)
	 * @see Spectrogram.SpectrogramParametersUser#getFFTDataBlock()
	 */
	@Override
	public FFTDataBlock getFFTDataBlock() {
		return sourceFFTDataBlock;
	}

	/**
	 * Called when the spectrogrammouse is moved or pressed in viewer mode. 
	 * @param spectrogramPanel 
	 * @param point
	 */
	public void specMouseOffline(SpectrogramPanel spectrogramPanel, Point point) {
		long mouseTime = getPixelXTime(point.x);
		double mouseFreq = getPixelFrequency(point.y);
		//		% now tell any plug in panels. 
		int chan = spectrogramParameters.channelList[spectrogramPanel.panelId];
		for (int i = 0; i < displayPanels.size(); i++) {
			//			System.out.println(String.format("Spectrogram mouse at %s, %s", 
			//					PamCalendar.formatDateTime(mouseTime), FrequencyFormat.formatFrequency(mouseFreq, true)));
			displayPanels.get(i).spectrogramMousePosition(chan, point, mouseTime, mouseFreq);
		}

	}

	DisplayPanelProvider displayPanelProvider;
	Vector<DisplayPanel> displayPanels = new Vector<DisplayPanel>();
	Vector<DisplayPanel> oldDisplayPanels;
	@Override
	public void notifyModelChanged(int changeType) {
		// get's called whenever a new unit is added or removed to some part of the Pam model
		if (PamController.getInstance().isInitializationComplete()) {
			if (changeType == PamControllerInterface.ADD_CONTROLLEDUNIT) {
				if (sourceFFTDataBlock == null) noteNewSettings(); // give it a try !
				if (spectrogramPlotPanel != null) {
					spectrogramPlotPanel.layoutPlots();
				}

			}
			else if (changeType == PamControllerInterface.REMOVE_CONTROLLEDUNIT) {
				noteNewSettings();
				if (spectrogramPlotPanel != null) {
					spectrogramPlotPanel.layoutPlots();
				}
			}
			else if (changeType == PamControllerInterface.NEW_SCROLL_TIME) {
				newScrollTime();
			}
			else if (changeType == PamControllerInterface.GLOBAL_MEDIUM_UPDATE) {
				double[] hzScales = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 
				hidingPanel.setAmplitudeAbsoluteRange(hzScales[0], hzScales[1]);
			}

		}
		if (changeType ==PamControllerInterface.INITIALIZATION_COMPLETE) {
			noteNewSettings();
			createAllImages();
			repaintAll();
			//			if (spectrogramPlotPanel != null) {
			//				spectrogramPlotPanel.LayoutPlots();
			//			}
		}
	}

	private void newScrollTime() {
		repaintAll();
	}



	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		if (sourceFFTDataBlock != null) {
			sourceFFTDataBlock.deleteObserver(this);
		}
	}

	@Override
	public void noteNewSettings() {
		setParams(spectrogramParameters, true);

		subscribeDataBlocks();
	}

	@Override
	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub

	}

	/**
	 * Used to get the time in milliseconds from a pixel number when 
	 * the user clicks on the display. Since the display is wrapping around, 
	 * this time is always going to be less than the currentXTime below.
	 * @param pixel
	 * @return time in milliseconds
	 */
	private long getPixelXTime(int pixel) {
		int pixsFromNow = pixel - (int) getCurrentXPixel();
		if (pixsFromNow > 0) {
			pixsFromNow -= spectrogramPanels[0].getWidth();
		}
		long pixelXTime =  (long) (getCurrentXTime() + pixsFromNow * getXDuration() / spectrogramPanels[0].getWidth());
		//		System.out.println(String.format("pixelXtime: currentXTime: %s + pixs %d = %s",
		//				PamCalendar.formatTime(getCurrentXTime()), pixsFromNow, PamCalendar.formatTime(pixelXTime)));
		return pixelXTime;
	}

	/**
	 * 
	 * @param pixels
	 * @return time in milliseconds
	 */
	private long getRelativePixelTime(int pixels) {
		return (long) (pixels * getXDuration() / spectrogramPanels[0].getWidth());
	}

	/**
	 * convert a pixel number into a frequency.
	 * @param pixel
	 * @return frequency in Hz
	 */
	private double getPixelFrequency(int pixel) {
		return  (1.-(double) pixel / spectrogramPanels[0].getHeight()) * 
				(spectrogramParameters.frequencyLimits[1]-spectrogramParameters.frequencyLimits[0]) +
				spectrogramParameters.frequencyLimits[0];
	}


	/**
	 * Get the current X pixel in screen (not image) coordinates.  
	 * <br>In viewer mode, this is 
	 * always 0; in normal operation, the following rules apply:<br>
	 * If it's wrapping, then the current pixel is the current imagePos
	 * scaled by the ratio of the panel width to the image width. 
	 * <br>If it's scrolling, then it's just the panel width. 
	 */
	@Override
	synchronized public double getCurrentXPixel() {
		if (viewerMode) {
			return 0;
		}
		if (spectrogramParameters.wrapDisplay) {
			if (spectrogramPanels[0].frozenImage == null) {
				return (double) spectrogramPanels[0].imagePos * spectrogramPanels[0].getWidth()/ getSpectrogramImageWidth();
			}
			else {
				return (double) spectrogramPanels[0].frozenImagePos * spectrogramPanels[0].getWidth()/ getSpectrogramImageWidth();
			}
		}
		else {
			return spectrogramPanels[0].getWidth();
		}
	}

	/**
	 * Get the current time, i.e. the time at the cursor in 
	 * milliseconds.  If there is a frozen image, this is
	 * the time of the when it was frozen. 
	 */
	@Override
	synchronized public long getCurrentXTime() {
		if (spectrogramPanels[0].frozenImage == null) {
			return spectrogramPanels[0].currentTimeMilliseconds;
		}
		else {
			return spectrogramPanels[0].frozenTimeMilliseconds;
		}
	}

	/* (non-Javadoc)
	 * @see Layout.DisplayPanelContainer#wrapDisplay()
	 */
	@Override
	public boolean wrapDisplay() {
		return spectrogramParameters.wrapDisplay;
	}

	/**
	 * @return the display length in milliseconds. 
	 */
	@Override
	public double getXDuration() {
		return spectrogramParameters.displayLength * 1000.;
	}

	@Override
	public void panelNotify(int noteType) {
		switch (noteType) {
		case DRAW_BORDER:
			//			spectrogramAxis.repaint();
			repaintAll();
			break;
		}

	}

	private void notifyDisplayPanels(int noteType) {
		if (displayPanels == null) return;
		for (int i = 0; i < displayPanels.size(); i++) {
			displayPanels.get(i).containerNotification(this, noteType);
		}
	}

	/**
	 * Original notifyMarkObservers moved to inner class SpectrogramPanel.  This method has been modified to simply
	 * cycle through the list of SpectrogramPanels and call notifyMarkObservers on each
	 * 
	 * @param downUp
	 * @param mouseEvent
	 * @param channel
	 * @param startTime
	 * @param duration
	 * @param f1
	 * @param f2
	 * @return
	 */
	private boolean notifyMarkObservers(int downUp, MouseEvent mouseEvent, int channel, long startTime, long duration, double f1, double f2) {
		boolean result = false;
		for (SpectrogramPanel aPanel : spectrogramPanels) {
			result |= aPanel.notifyMarkObservers(downUp, mouseEvent, channel, startTime, duration, f1, f2);
		}
		return result;
	}

	@Override
	public int getFrameType() {
		return UserFramePlots.FRAME_TYPE_SPECTROGRAM;
	}

	private boolean fireMouseDownEvents(SpectrogramPanel spectrogramPanel, MouseEvent mouseEvent, Point mouseDown) {
		mouseDownTime = getPixelXTime(mouseDown.x);
		double f1 = getPixelFrequency(mouseDown.y);
		int channel = spectrogramParameters.channelList[spectrogramPanel.panelId];

		// instead of calling notifyMarkObservers on every panel, only do it on the panel that the user actually marked
		//		return notifyMarkObservers(SpectrogramMarkObserver.MOUSE_DOWN, mouseEvent, channel, mouseDownTime, 
		//				0, f1, f1);
		return spectrogramPanel.notifyMarkObservers(SpectrogramMarkObserver.MOUSE_DOWN, mouseEvent, channel, mouseDownTime, 
				0, f1, f1);
	}

	private boolean fireMouseUpEvents(SpectrogramPanel spectrogramPanel, MouseEvent mouseEvent, Point mouseDown, Point mouseUp) {
		if (mouseDown == null || mouseUp == null) {
			return false;
		}
		//		if (mouseDown.equals(mouseUp)) {
		// should get out - just clicked - leave up to observers to decide on that !
		//		}
		long t1 = getPixelXTime(mouseDown.x);
		long dt = getRelativePixelTime(mouseUp.x-mouseDown.x);
		long startTime = Math.min(t1, t1+dt);
		long duration = Math.abs(dt);
		double f1 = getPixelFrequency(Math.max(mouseDown.y, mouseUp.y));
		double f2 = getPixelFrequency(Math.min(mouseDown.y, mouseUp.y));
		int channel = spectrogramParameters.channelList[spectrogramPanel.panelId];

		// instead of calling notifyMarkObservers on every panel, only do it on the panel that the user actually marked
		//		return notifyMarkObservers(SpectrogramMarkObserver.MOUSE_UP, mouseEvent, channel, startTime, 
		//				duration, f1, f2);
		boolean notify = spectrogramPanel.notifyMarkObservers(SpectrogramMarkObserver.MOUSE_UP, mouseEvent, channel, startTime, 
				duration, f1, f2);

		//		System.out.println(String.format("Event channel %d - start %s; duration %4.3fs; Frequency %4.1f to %4.1fHz",
		//				spectrogramParameters.channelList[spectrogramPanel.panelId],
		//				PamCalendar.formatTime(startTime), (double) duration / 1000., f1, f2));
		

		// can't get the selection box to stay on the screen while we wait for a right-click, so just show a pop-up
		// menu right now (as if the 'Show popup menus immediately' check box were checked)
		MarkRelationships links = MarkRelationships.getInstance();
		MarkRelationshipsData params = links.getMarkRelationshipsData();
//		if (params.isImmediateMenus()) {
			spectrogramPanel.overlayMarker.showObserverPopups(ExtMouseAdapter.fxMouse(mouseEvent, null));
//		};

		return notify;
	}

	private boolean fireMouseDragEvents(SpectrogramPanel spectrogramPanel, MouseEvent mouseEvent, Point mouseDown, Point mouseUp) {
		if (mouseDown == null || mouseUp == null) {
			return false;
		}

		//		if (mouseDown.equals(mouseUp)) {
		// should get out - just clicked - leave up to observers to decide on that !
		//		}		
		//		if (mouseEvent.getButton() != MouseEvent.BUTTON1) {
		//			return false;  //mouse drag events seem to pick up the wrong button number so can't include this !
		//		}
		long t1 = getPixelXTime(mouseDown.x);
		long dt = getRelativePixelTime(mouseUp.x-mouseDown.x);
		long startTime = Math.min(t1, t1+dt);
		long duration = Math.abs(dt);
		double f1 = getPixelFrequency(Math.max(mouseDown.y, mouseUp.y));
		double f2 = getPixelFrequency(Math.min(mouseDown.y, mouseUp.y));
		int channel = spectrogramParameters.channelList[spectrogramPanel.panelId];

		// instead of calling notifyMarkObservers on every panel, only do it on the panel that the user actually marked
		//		return notifyMarkObservers(SpectrogramMarkObserver.MOUSE_DRAG, mouseEvent, channel, startTime, 
		//				duration, f1, f2);
		return spectrogramPanel.notifyMarkObservers(SpectrogramMarkObserver.MOUSE_DRAG, mouseEvent, channel, startTime, 
				duration, f1, f2);


		//		System.out.println(String.format("Event channel %d - start %s; duration %4.3fs; Frequency %4.1f to %4.1fHz",
		//				spectrogramParameters.channelList[spectrogramPanel.panelId],
		//				PamCalendar.formatTime(startTime), (double) duration / 1000., f1, f2));
	}

	public FFTDataBlock getSourceFFTDataBlock() {
		return sourceFFTDataBlock;
	}

	public PamRawDataBlock getSourceRawDataBlock() {
		return sourceRawDataBlock;
	}

	/**
	 * Should receive play commands from the top toolbar. 
	 */
	public void playViewerSound() {
		long startMillis = viewerScroller.getValueMillis();
		long endMillis = startMillis + viewerScroller.getVisibleAmount();
		PlaybackControl.getViewerPlayback().playViewerData(startMillis, endMillis, new PlayProgress());
	}

	/**
	 * @return the colourArray
	 */
	public ColourArray getColourArray() {
		return colourArray;
	}

	/**
	 * @return the overlayMarker
	 */
	//	protected OverlayMarker getOverlayMarker() {
	//		return overlayMarker;
	//	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitName()
	 */
	@Override
	public String getUnitName() {
		return specDisplayComponent.getUniqueName();
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getUnitType()
	 */
	@Override
	public String getUnitType() {
		return "Spectrogram Display";
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsReference()
	 */
	@Override
	public Serializable getSettingsReference() {
		return spectrogramParameters;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#getSettingsVersion()
	 */
	@Override
	public long getSettingsVersion() {
		return SpectrogramParameters.serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see PamController.PamSettings#restoreSettings(PamController.PamControlledUnitSettings)
	 */
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.spectrogramParameters = ((SpectrogramParameters) pamControlledUnitSettings.getSettings()).clone();
		return (spectrogramParameters != null);
	}

	public String getDataSelectorName(int panelId) {
		return specDisplayComponent.getUniqueName()+"Panel"+panelId;
	}

	public OverlayDataInfo getOverlayDataInfo(PamDataBlock dataBlock, int panelId) {
		return spectrogramParameters.getOverlayDataInfo(dataBlock, panelId);
	}

	//	public long getStartMillis(){
	//		if (viewerMode) viewerScroller.getValueMillis();
	//		else currentTimeMilliseconds
	//	}
	//	
	//
	//	public long getRangeMillis(){
	//		//TODO
	//	}

}
