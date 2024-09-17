package clickDetector;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamUtils.PamUtils;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickDetector.offlineFuncs.OfflineEventListPanel;

/**
 * The edit template dialog allows users to create mean spectrums from marked out events. A created mean spectrum can be exported as .csv file and displayed on the Click Spectrum window. Templates contain data on species
 * ,sample rate, and standard deviation of each frequency bin along with the mean FFT. The idea behind these templates is that they can be used to help the user identify dolphin clicks and perhaps highlight common features over multiple clicks.
	The problem of classifying dolphin clicks is a difficult one. Different clicks from the same animal often have highly different spectral properties. For some species the average spectrum of multiple clicks does contain peaks and features which can be useful. The aim of this class is to allow the user
	to gain some kind of handle on these features. Further development must look at some sort of statistical analysis to quantify the similarity between a template and click/click event.
	<p>
	A note on creating mean spectrums;
	<p>
	The rules of logarithms are, to the casual mathematician,  a bit strange. We need to be careful when constructing a mean or standard deviation of logarithmic spectrums mainly because log(x)+log(y) does NOT equal log(x+y). So if we create a mean spectrum, then take the log of that, it is not the same as calculating the log of each click fft and then taking the mean. The same applies to standard deviation.  
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickSpectrumTemplateEditDialog extends PamDialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//Current click spectrum
	ClickSpectrum clickSpectrum;

	//Data
	ClickSpectrumTemplateParams clickSpectrumTemplateParams;
	
	ClickControl clickControl;
	
	private static ClickSpectrumTemplateEditDialog singleInstance;
	
	private double[] averageFFT;
	
	private double[] stdFFT;
	
	private double[] averageFFTLog;
	
	private double[] stdFFTLog;
	
	private Color fftColour=new Color(0.5f,0.5f,0.5f);
	
	private String species="unknown";
	
	private float sampleRate;
	
	private int N;
	
	private Integer channel;

	//Graphics components;
	private JSplitPane mainPanel;
	
	private OfflineEventListPanel offlineEventListPanel;
	
	private TemplateTable templateTable;
	
	private ArrayList<OfflineEventDataUnit> selectedEvents= new ArrayList<OfflineEventDataUnit>();
	
	SpectrumPlotPanel spectrumPlot;
	
	JTabbedPane tabbedPane;

	private Window parentFrame;
	
	//Menu Items
	JCheckBoxMenuItem logScale;
	
	JCheckBoxMenuItem confidenceIntervals;
	
	private Boolean logScaleb=false;
	
	private double logMinimum=-40;
	
	private double logMaximum=0;
	
	private Boolean confidenceIntervalsb=false;
	
	private JButton save, load, colour, addTo, clearAll;

	

	//Preset graphics 
	final static float dash1[] = {3.0f};
	final static BasicStroke dashedtmplate =
	        new BasicStroke(2.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        5.0f, dash1, 0.0f);
	
	 final static BasicStroke solid =
	        new BasicStroke(2f);
	

	public ClickSpectrumTemplateEditDialog(Window parentFrame, Point pt,
			 ClickControl clickControl) {
		
		super(parentFrame, "Create Template", false);
		
		channel=null;
		
		this.clickControl=clickControl;
		sampleRate = clickControl.getClickDetector().getSampleRate();
		this.parentFrame =parentFrame;

		JPanel eventListPanel = new JPanel(new BorderLayout());
		offlineEventListPanel = new OfflineEventListPanel(clickControl);
		offlineEventListPanel.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		offlineEventListPanel.getPanel().setPreferredSize(new Dimension(300, 200));
		offlineEventListPanel.addMouseListener(new MouseEventFuncs());
		eventListPanel.add(BorderLayout.CENTER, offlineEventListPanel.getPanel());
		eventListPanel.add(BorderLayout.SOUTH,offlineEventListPanel.getSelectionPanel());
		eventListPanel.setBorder(new TitledBorder("Event List"));
		//eventListPanel.add(BorderLayout.NORTH, new JLabel("Event list"));
		
		JPanel templateListPanel=new JPanel(new BorderLayout());
		templateTable=new TemplateTable();
		templateListPanel.add(BorderLayout.CENTER,templateTable);
				
		tabbedPane = new 	JTabbedPane();
		tabbedPane.addTab("Events",null,eventListPanel);
		tabbedPane.addTab("Template",null,templateListPanel);
		tabbedPane.addChangeListener(new TabChange_Event_Template());
		JPanel controls=controls();
		
		spectrumPlot=new SpectrumPlotPanel();
	
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		westPanel.add(BorderLayout.CENTER, spectrumPlot.getPanel());
		westPanel.add(BorderLayout.WEST, controls);
		
		offlineEventListPanel.addListSelectionListener(new ListSelection());
		offlineEventListPanel.tableDataChanged();
		
		mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				westPanel, tabbedPane);
		mainPanel.setDividerLocation(400); //set the location of the split pane. 
		
		this.setPreferredSize(new Dimension(700, 400));
		this.setResizable(true);
		this.setDialogComponent(mainPanel);
		
	}
	
	/**
	 * Creates an instance of the edit template dialog, associated with the Click Spectrum window it was opened from. 
	 * @param parentFrame
	 * @param pt
	 * @param clickSpectrum
	 * @param clickSpectrumTemplateParams
	 * @param clickControl
	 * @return new clickSpectrumTemplateParams
	 */
	public static ClickSpectrumTemplateParams showDialog(Window parentFrame, Point pt, ClickSpectrum clickSpectrum, ClickSpectrumTemplateParams clickSpectrumTemplateParams, ClickControl clickControl){
		
		if (singleInstance == null ) {
			singleInstance = new ClickSpectrumTemplateEditDialog(parentFrame, pt,clickControl);
		}
		singleInstance.setClickSpectrum(clickSpectrum);
		singleInstance.clickSpectrumTemplateParams = clickSpectrumTemplateParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		singleInstance.offlineEventListPanel.tableDataChanged();
		return singleInstance.clickSpectrumTemplateParams;
	}
	
	public void setClickSpectrum(ClickSpectrum clickSpectrum){
		this.clickSpectrum=clickSpectrum;
	}
	
	public void addListSelectionListener(ListSelectionListener listSelectionListener) {
		offlineEventListPanel.getTable().getSelectionModel().addListSelectionListener(listSelectionListener);
	}
	
	public JPanel controls(){
	
		PamPanel controls=new PamPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.ipady=0; 
		c.anchor=GridBagConstraints.NORTHWEST ;
		c.gridx=0;
		c.gridy=0;
		c.gridwidth = 4;
		
		save=new JButton("Export...");
		save.addActionListener(new SaveSpec());
		PamDialog.addComponent(controls, save, c);
		c.gridy++;
		load=new JButton("Load...");
		load.addActionListener(new LoadSpec());
		PamDialog.addComponent(controls, load, c);
		c.insets = new Insets(10,0,0,0);  //top padding
		c.gridy++;
		addTo=new JButton("Add to Spectrum");
		addTo.addActionListener(new AddToSpec());
		PamDialog.addComponent(controls, addTo, c);
		c.insets = new Insets(0,0,0,0);  //remove top padding
		c.gridy++;
		colour=new JButton("Colour...");
		colour.addActionListener(new Colour());
		PamDialog.addComponent(controls, colour, c);
		c.gridy++;
		c.weighty=1;
		c.insets = new Insets(10,0,0,0);
		clearAll=new JButton("Clear All");
		clearAll.addActionListener(new ClearAll());
		PamDialog.addComponent(controls, clearAll, c);

		controls.setBorder(new TitledBorder("Controls"));

		return controls;
	
	}
	
	

	/**
	 * Listens for which events are selected. Single or multiple events can be selected. These are combined to form a mean spectrum template and a standard deviation for each mean frequency bin. ]
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class ListSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			//must set channels to null (all) in case an event only has clicks form certain channels. 
			channel=null;
			createEventsAverage();
			
		}
	}

	
	/**
	 * Creates an average normal and log template plus corresponding standard deviations for the selected event. Note the log templates are NOT created by taking the mean of the average template but by taking the mean of all the log FFTs in the selected event(s);
	 */
	private void createEventsAverage(){
		
		int[] selectedRows=offlineEventListPanel.getTable().getSelectedRows();
		
		selectedEvents= new ArrayList<OfflineEventDataUnit>();
		
		//create a list of selected events
		for (int i=0; i<selectedRows.length;i++){
			selectedEvents.add(	offlineEventListPanel.getSelectedEvent(selectedRows[i]));
		}
		
		if (selectedEvents.size()==0 || selectedEvents==null){
			return;
		}
			
		ArrayList<double[]> fftAll=getEventFFTs(channel,true);
		//create standard average spectrum
		this.averageFFT=fftMean(fftAll);
		printFFT( averageFFT);
		this.stdFFT=fftstd(fftAll,averageFFT);
		//create log template
		this.averageFFTLog=fftMean(convertAllFFttoLog(fftAll));		
		this.stdFFTLog=fftstd(convertAllFFttoLog(fftAll), averageFFTLog);
		//other data
		this.N=fftAll.size();
		this.species="unknown";
			
		if (averageFFT!=null){
			spectrumPlot.rePaintAll();
		}
	}
	
	/**
	 * Calculates the log values (10*Log10) for an array. 
	 * @param array to convert
	 
	 */
	private  double[] convertFFTtoLog(double[] array){
		
		if (array==null) return null;
		
		double[] averageFFTLog = Arrays.copyOf(array, array.length);
			
		for (int i=0;i<averageFFTLog.length;i++){
			averageFFTLog[i]=10*Math.log10(averageFFTLog[i]);
		}
			
		return averageFFTLog;
		
	}
	
	
	private ArrayList<double[]> convertAllFFttoLog(ArrayList<double[]> ffts){
		
		ArrayList<double[]> logFFts=new ArrayList<double[]>(ffts.size());
		double[] logfft;
		for (int i=0; i<ffts.size(); i++){
			logfft=convertFFTtoLog(ffts.get(i));
			logFFts.add(logfft);
		}
		
		return logFFts;	
	}
	
	/**
	 * This is a bit messy but we have to calculate the log max before repaint. Otherwise the axis values are not updated untill the next repaint. 
	 */
	private void calcLogMaxValue(){
		
		if (averageFFTLog==null || stdFFTLog==null) return;
		
		if (!confidenceIntervalsb){
			this.logMaximum=0; 
			return;
		}
		
		double maxVal=-Double.MAX_VALUE;
		double val;
		for (int i=0; i<averageFFTLog.length; i++){
			val=(averageFFTLog[i]+(stdFFTLog[i]*2));
			if (maxVal<val) maxVal=val;
		}
		this.logMaximum=maxVal;
		
	}
	
	
	/**
	 * Get the event ffts for the selected channel. If channel is null then then all ffts from every event are selected. Have to be careful here with channel numbers here. If an event does not have any clicks form the selected channel then an empty ArrayList is returned.  
	 * @param channel
	 * @return
	 */
	public ArrayList<double[]>  getEventFFTs(Integer channel, boolean normalise ){
		ClickDetection click;
		ArrayList<double[]> fftAll=new ArrayList<double[]>();
		int[] channels;
		double[] fft;
		
		//need to calculate a proper fft length or else sections of the waveform can be cut off. 
		
		int FFTlengthd=Integer.MAX_VALUE; 
		for (int i=0; i<selectedEvents.size(); i++){
			for (int j=0; j<selectedEvents.get(i).getSubDetectionsCount(); j++){
				PamDataUnit pamDataUnit = selectedEvents.get(i).getSubDetection(j);
				if (ClickDetection.class.isAssignableFrom(pamDataUnit.getClass())) {
					click = (ClickDetection) pamDataUnit;
				}
				else {
					continue;
				}
				if (click.getWaveData()[0].length<FFTlengthd){
					FFTlengthd=click.getWaveData()[0].length; 
				}
			}
		}
		
		FFTlengthd=PamUtils.getMinFftLength(FFTlengthd);
		
		for (int i=0; i<selectedEvents.size(); i++){

			for (int j=0; j<selectedEvents.get(i).getSubDetectionsCount(); j++){
				PamDataUnit pamDataUnit = selectedEvents.get(i).getSubDetection(j);
				if (ClickDetection.class.isAssignableFrom(pamDataUnit.getClass())) {
					click = (ClickDetection) pamDataUnit;
				}
				else {
					continue;
				}

				
				channels=PamUtils.getChannelArray (click.getChannelBitmap());
				 
				 for (int l=0; l<channels.length;l++){
					 if (channel==null){
						 	if (normalise) fft=normalise(click.getPowerSpectrum(l, FFTlengthd));
						 	else fft=click.getPowerSpectrum(l, FFTlengthd);
						 	fftAll.add(fft);
					 }
					 else if (channel==channels[l]){
						 	if (normalise) fft=normalise(click.getPowerSpectrum(l, FFTlengthd));
						 	else fft=click.getPowerSpectrum(l, FFTlengthd);
						 	fftAll.add(fft);
					 }
				 }
			}
		}
		return fftAll;
	}
	
	private static void printFFT(double[] fft){
	 	System.out.println("fft: ");
	 	for (int i=0; i<fft.length; i++){
	 			System.out.print(" "+fft[i]);
	 	}
		System.out.println("");
	}
	
	
	/**
	 * Normalises an array.
	 * @param array input array
	 * @return a normalised array between 0 and 1 of the input array;
	 */
	private double[] normalise(double [] array){
		
		double[] normalisedArray=new double[array.length];
		double maxVal=0;
		
		for (int i=0; i<array.length; i++){
			if (maxVal<array[i]) maxVal=array[i];
		}
		
		for (int j=0; j<array.length; j++){ 
			normalisedArray[j]=array[j]/maxVal;
		}
		
		return normalisedArray;
	}
	
	/**
	 * Opens a load dialog and loads the selected template.
	 * @author Jamie Macaulay
	 *
	 */
	class LoadSpec implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String template=openFileBrowser();
			
			if (template==null){
				return;
			}
			clickSpectrumTemplateParams.templateFile = new File(template);
			loadTemplate();
		}		
	}
	
	public String openFileBrowser(){
		
		PamFileFilter fileFilter = new PamFileFilter("Click Template", ".csv");
		fileFilter.addFileType(".txt");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(fileFilter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int state = fileChooser.showOpenDialog(parentFrame);
		if (state == JFileChooser.APPROVE_OPTION) {
			File currFile = fileChooser.getSelectedFile();
			//System.out.println(currFile);
			return currFile.getAbsolutePath();
		}
		return null;
	}
	
	public void loadTemplate(){
		ClickTemplate clkTemplate=ClickTemplate.getCSVResults(clickSpectrumTemplateParams.templateFile.getAbsolutePath());
		if (clkTemplate!=null){
		clickSpectrumTemplateParams.clickTemplateArray.add(clkTemplate);
		clickSpectrumTemplateParams.clickTempVisible.add(true);
		}
	}
	
	
	/**
	 * Clears all the templates.
	 * @author Jamie Macaulay
	 *
	 */
	class ClearAll implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			clearAll();
		}		
	}
	
	/**
	 * Clears all templates from manager and click spectrum.
	 */
	private void clearAll(){
		clickSpectrumTemplateParams.clickTemplateArray=new ArrayList<ClickTemplate>();
		updateClickSpectrum();
		//refresh the table
		templateTable.refreshData(clickSpectrumTemplateParams.clickTemplateArray,clickSpectrumTemplateParams.clickTempVisible);
	}
	
	
	
	/**
	 * Opens a save dialog and saves the selected events as a spectrum template. The fft data, standard deviation of each fft bin, colour and sample rate ofd the data are all saved.
	 * @author Jamie Macaulay
	 *
	 */
	class SaveSpec implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			saveTemplate();
		}		
	}
	
	/**
	 * Saves the current template and standard deviation to a .csv file. 
	 */
	public void saveTemplate(){
		if (averageFFT==null) return;
		String file=saveFileBrowser();
		if (file!=null){
			ClickTemplate clickTemplate=createTemplate();

			if (!file.endsWith(".csv")){
				ClickTemplate.writeClickTemptoFile(clickTemplate, file+".csv");
			}
			else{
				ClickTemplate.writeClickTemptoFile(clickTemplate,file);
			}
		}
		
//		System.out.println("Save click template file: "+file+".csv");
	}
	
	
	class SaveSpecAll implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			saveTemplatePlusSpectrums();
		}		
	}
	
	/**
	 * Saves a template plus a list of all the spectrums used in creating that template- note the template is from normalised spectrums but the list of spectrums is not normalised- this gives the maximum amount of data to the user.
	 */
	public void saveTemplatePlusSpectrums(){
		if (averageFFT==null) return;
		String file=saveFileBrowser();
		
		//this is a nasty piece of code but want to keep fftAll out of memory (i.e.not an instance variable)- could be very large in some cases.
		ArrayList<double[]> fftAll=new ArrayList<double[]>();
		ArrayList<ArrayList<Double>> fftAllD=new 	ArrayList<ArrayList<Double>>();
		fftAll=getEventFFTs(0,false);
		for (int i=0; i<fftAll.size();i++){
			fftAllD.add(convertToArrayList(fftAll.get(i)));
		}
		
		if (file!=null){
		ClickTemplate clickTemplate=createTemplate();

			if (!file.endsWith(".csv")){
				ClickTemplate.writeClickTemptoFile(clickTemplate,fftAllD, file+".csv");
			}
			else{
				ClickTemplate.writeClickTemptoFile(clickTemplate,fftAllD, file);
			}
		
		}
//		System.out.println("Save All click template file: "+file+".csv");
	}
	
	
	/**
	 * Adds the selected events to the spectrum window
	 * @author Jamie Macaulay.
	 */
	class AddToSpec implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			addToSpectrum();

			}		
		}
	
	/**
	 * Save the average event data to a ClickTemplate class and add to list of click templates. Make visible as a default.
	 */
	public void addToSpectrum(){
		
		if (averageFFT==null)return;
		if (averageFFT.length==0)return;
		
		ClickTemplate clickTemplate=createTemplate();
		
		clickSpectrumTemplateParams.clickTemplateArray.add(clickTemplate);
		clickSpectrumTemplateParams.clickTempVisible.add(true);
		
		updateClickSpectrum();
	}
	

	class Colour implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			ColourChooser colorchoose=new ColourChooser(parentFrame,null);
			colorchoose.setVisible(true);
		}		
	}

	
	/**
	 * Calculates the average fft for an ArrayList of fft's. Ignores any NaN values.
	 * @param fftAll
	 * @return average fft. 
	 */
	public double[] fftMean(ArrayList<double[]> fftAll){
		
		if (fftAll.size()==0){
			return null;
		}
		
		double  fftVal;
		
		double[] average=new double[fftAll.get(0).length];
		
		for (int i=0; i<fftAll.get(0).length;i++ ){ 
			for (int j=0; j<fftAll.size();j++){
				fftVal=fftAll.get(j)[i];
				if (!Double.isNaN(fftVal)) average[i]+=fftVal;
			}
			average[i]=average[i]/fftAll.size();

		}

		return average;
	}
	
	/**
	 * Create standard deviation of all fft's. 
	 * @param fftAll- all the fft's
	 * @param meanVals- ArrayList of the means if the fft's ;
	 * @return
	 */
	public double[] fftstd(ArrayList<double[]> fftAll, double[] meanVals){
		
		if (fftAll.size()==0 || meanVals==null){
			return null;
		}
		
		double[] std=new double[fftAll.get(0).length];
		for (int i=0; i<fftAll.get(0).length;i++ ){
			
			for (int j=0; j<fftAll.size();j++){
				std[i]+=Math.pow((fftAll.get(j)[i]-meanVals[i]),2);
			}
			
			if (std[i]==0.0) continue;
			std[i]=Math.sqrt(std[i]/(fftAll.size()-1));
		}
			
		return std;
	}
	
	
	
	private JPopupMenu graphPopUpMenu(){
		
		JCheckBoxMenuItem jBoxMenuItem;
		JPopupMenu menu = new JPopupMenu();
		logScale = new JCheckBoxMenuItem("Log scale");
		logScale.setSelected(logScaleb);
		logScale.addActionListener(new LogScaleA());
		menu.add(logScale);
		confidenceIntervals = new JCheckBoxMenuItem("Confidence Intervals");
		confidenceIntervals.addActionListener(new ConfidenceIntervals());
		confidenceIntervals.setSelected(confidenceIntervalsb);

		if (selectedEvents!=null && tabbedPane.getSelectedIndex()==0){
			if (selectedEvents.size()!=0){
				
				menu.addSeparator();
				
				Integer chan;
				for (int i = 0; i < PamUtils.getNumChannels(selectedEvents.get(0).getChannelBitmap()); i++) {
					chan=PamUtils.getNthChannel(i, selectedEvents.get(0).getChannelBitmap());
					jBoxMenuItem = new JCheckBoxMenuItem("Channel " + chan);
					if (channel==chan) jBoxMenuItem.setSelected(true);
					jBoxMenuItem.addActionListener(new ChannelSelect(chan));
					menu.add(jBoxMenuItem);
				}
				
				jBoxMenuItem = new JCheckBoxMenuItem("All Channels");
				if (channel==null) jBoxMenuItem.setSelected(true);
				jBoxMenuItem.addActionListener(new ChannelSelect(null));
				menu.add(jBoxMenuItem);
				
			}	
		}
		
		menu.add(confidenceIntervals);
			
		return menu;
	}
	
	/**
	 * Pop up menu for the events menu. 
	 * @return pop up menu. 
	 */
	private JPopupMenu eventPopUpMenu(){
		
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem=new JMenuItem("Save Template");
		menuItem.addActionListener(new SaveSpec());
		menu.add(menuItem);
		menuItem=new JMenuItem("Save All Clicks");
		menuItem.addActionListener(new SaveSpecAll());
		menu.add(menuItem);
		menuItem=new JMenuItem("Add to Spectrum");
		menuItem.addActionListener(new AddToSpec());
		menu.add(menuItem);
		menuItem=new JMenuItem("Colour");
		menuItem.addActionListener(new Colour());
		menu.add(menuItem);

		return menu;
	}
	
	class ChannelSelect implements ActionListener {
		Integer chan;

		public ChannelSelect(Integer channel) {
			super();
			this.chan = channel;
		}
		@Override
		
		public void actionPerformed(ActionEvent arg0) {
			channel=chan;
			createEventsAverage();
			calcLogMaxValue();
			}		
		}
	

	class LogScaleA implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			logScaleb=logScale.getState();
			spectrumPlot.rePaintAll();

			}		
		}
	

	class ConfidenceIntervals implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			confidenceIntervalsb=confidenceIntervals.getState();
			calcLogMaxValue();
			spectrumPlot.rePaintAll();
			}		
		}
	

	
	/**
	 * Listens for when a tab is changed.
	 * @author Jamie Macaulay
	 *
	 */
	class TabChange_Event_Template implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent arg0) {
			tabStateChanged();
		}

	}
	
	/**
	 * Controls what changes when switching between tabs. 
	 */
	private void tabStateChanged(){
		
		if (tabbedPane.getSelectedIndex()==0){
		sampleRate = clickControl.getClickDetector().getSampleRate();
		addTo.setEnabled(true);
		colour.setEnabled(true);
		createEventsAverage();
		}
		
		if(tabbedPane.getSelectedIndex()==1){
		addTo.setEnabled(false);
		colour.setEnabled(false);
		if (clickSpectrumTemplateParams.clickTemplateArray==null) return;
		templateTable.refreshData(clickSpectrumTemplateParams.clickTemplateArray,clickSpectrumTemplateParams.clickTempVisible);
		averageFFT=null;
		averageFFTLog=null;
		spectrumPlot.rePaintAll();
		}
	}
	
	/**
	 * Class which creates a table of all templates shown in the click spectrum. Tables can be deleted from memory or hidden from view in this menu. Allows for more streamlined management of multiple templates.
	 * @author Jamie Macaulay
	 *
	 */
	class TemplateTable extends JPanel{

		private static final long serialVersionUID = 1L;
		
		Integer row=null;
		TemplateTableModel table;
		JTable tempTable;
		JScrollPane mainPanel;
		
		public TemplateTable(){
			super();
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			TemplateTableMouse tableMouse=new TemplateTableMouse();
			
			
			table =new TemplateTableModel(); 
			tempTable=new JTable(table);
			tempTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
			tempTable.setFillsViewportHeight(false);
			tempTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			tempTable.addMouseListener(tableMouse);
			tempTable.getSelectionModel().addListSelectionListener(new RowListener());
			tempTable.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
			tempTable.addPropertyChangeListener(new SpeciesChange());
			CustomTableCellRenderer rend=new CustomTableCellRenderer();
			try {
				tempTable.setDefaultRenderer(Class.forName
				           ( "java.awt.Color" ), rend);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			mainPanel=new JScrollPane(tempTable);
			add(mainPanel);
		}
		
		public JScrollPane getPanel(){
			return mainPanel;
		} 
		
		public void createTemplateTable(){

		}
		
		public void refreshData(ArrayList<ClickTemplate> clicktemplates, ArrayList<Boolean> visible){
			if (clicktemplates==null) return;
			Object[][] data=new Object[clicktemplates.size()][4];
			for (int i=0; i<clicktemplates.size(); i++){
				data[i][0]=i;
				data[i][1]=""+clicktemplates.get(i).getSpecies();
				data[i][2]=clicktemplates.get(i).getColour();
				data[i][3]=visible.get(i);
			
			}
			table.setData(data);
			table.fireTableDataChanged();
		}

		
		private void showPopupMenu(MouseEvent me){
			if (row == null) {
				return;
			}	
			JPopupMenu menu = new JPopupMenu();
			JMenuItem menuItem=new JMenuItem("Save");
			menuItem.addActionListener(new SaveSpec());
			menu.add(menuItem);
			menuItem=new JMenuItem("Delete");
			menuItem.addActionListener(new Delete());
			menu.add(menuItem);
			
			//add a colour change option if the correct cell is selected.
			if(tempTable.getSelectedColumn()==2){
				menuItem=new JMenuItem("Colour");
				menuItem.addActionListener(new ChangeColour());
				menu.add(menuItem);
			}
			
			menu.show(me.getComponent(), me.getX(), me.getY());

		}
		
		/**
		 * Called if a table property is changed- not just a row change/. Used when a user types in a species or changes a set visible check box. 
		 * @author Jamie Macaulay
		 */
		class SpeciesChange implements PropertyChangeListener {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
//				System.out.println("Change property...");
				if (tempTable.getSelectedRow()>=0){
					species=(String) table.getValueAt(tempTable.getSelectedRow(), 1);
					//set the species
					clickSpectrumTemplateParams.clickTemplateArray.get(tempTable.getSelectedRow()).setSpecies(species);
					//check to see if the setVisible box has been changed- if so then repaint the clickSpectrum. 
					clickSpectrumTemplateParams.clickTempVisible.set(tempTable.getSelectedRow(),(Boolean) table.getValueAt(tempTable.getSelectedRow(), 3));
					updateClickSpectrum();
				}
			}
		}
		
		/**
		 * Deletes a template from the curren template list. Removes the deleted template from the click spectrum and table. 
		 * @author Jamie Macaulay
		 *
		 */
		private class Delete implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tempTable.getSelectedRow();
				 deleteTemplate(tempTable.getSelectedRow());
				 refreshData(clickSpectrumTemplateParams.clickTemplateArray,clickSpectrumTemplateParams.clickTempVisible);
				 updateClickSpectrum();
				}		
		}
		
		/**
		 * Changes the colour of the click spectrum. 
		 * @author Jamie Macaulay
		 *
		 */
		private class ChangeColour implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ColourChooser colorchoose=new ColourChooser(parentFrame,null);
				colorchoose.setVisible(true);
				clickSpectrumTemplateParams.clickTemplateArray.get(tempTable.getSelectedRow()).setColor(fftColour);
				refreshData(clickSpectrumTemplateParams.clickTemplateArray,clickSpectrumTemplateParams.clickTempVisible);
				
				spectrumPlot.rePaintAll();
				updateClickSpectrum();
				}		
		}

		
		private class RowListener implements ListSelectionListener {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selectedRow=tempTable.getSelectedRow();
				row=selectedRow;
				if (row>=0){
					averageFFT=convertToDoubleArray(clickSpectrumTemplateParams.clickTemplateArray.get(row).getSpectrum());
					stdFFT=convertToDoubleArray(clickSpectrumTemplateParams.clickTemplateArray.get(row).getSpectrumStd());
					averageFFTLog=convertToDoubleArray(clickSpectrumTemplateParams.clickTemplateArray.get(row).getSpectrumLog());
					stdFFTLog=convertToDoubleArray(clickSpectrumTemplateParams.clickTemplateArray.get(row).getSpectrumStdLog());
					sampleRate=clickSpectrumTemplateParams.clickTemplateArray.get(row).getSampleRate();
					fftColour=clickSpectrumTemplateParams.clickTemplateArray.get(row).getColour();
					species=clickSpectrumTemplateParams.clickTemplateArray.get(row).getSpecies();
					N=clickSpectrumTemplateParams.clickTemplateArray.get(row).getN();
					spectrumPlot.rePaintAll();
				}
			}
		}
		
		
		 private class ColumnListener implements ListSelectionListener {
		        @Override
				public void valueChanged(ListSelectionEvent event) {
		            if (event.getValueIsAdjusting()) {
		                return;
		            }
		        }
		    }
		 

		public class CustomTableCellRenderer extends DefaultTableCellRenderer {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent
		       (JTable table, Object value, boolean isSelected,
		       boolean hasFocus, int row, int column) 
		    {
		        Component cell = super.getTableCellRendererComponent
		           (table, value, false, hasFocus, row, column);
		        if( value instanceof Color ){
		        	cell.setBackground( (Color) value );
		        	cell.setForeground((Color) value);
		        }
		        return cell;
		    }
		}
		
		 
		/**
		 * Mouse functions for the template table. 
		 * @author Jamie Macaulay
		 *
		 */
	private class TemplateTableMouse extends MouseAdapter {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showPopupMenu(e);
				}
			}

			@Override
			public void mousePressed(MouseEvent me) {
				if (me.isPopupTrigger()) {
				}
			}

			 @Override
		    /** 
		     * This shows a pop up menu and selects the cell the mouse has right clicked on. Much smoother than left clicking to select cell, then right clicking to get appropriate menu
		     */
			 public void mouseReleased(MouseEvent e) {
		            int r = tempTable.rowAtPoint(e.getPoint());
		            int c = tempTable.columnAtPoint(e.getPoint());
		            
		            if (r >= 0 && r < tempTable.getRowCount() && c>=0 && c< tempTable.getColumnCount()) {
		            	tempTable.setRowSelectionInterval(r, r);
		            	tempTable.setColumnSelectionInterval(c, c);
		            } else {
		            	tempTable.clearSelection();
		            }

		            int rowindex = tempTable.getSelectedRow();
		            int columnIndex = tempTable.getSelectedColumn();
		            if (rowindex < 0 && columnIndex<0)
		                return;
		            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
		            	showPopupMenu(e);
		            }
			 }
		}
	}
	
	/**
	 * Table model for the click template window
	 * @author Jamie Macaulay
	 *
	 */
	class TemplateTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = {"Template",
                  "Species",
                  "Colour","Visible"
               		};
		  
		 // private Object[][] data;
		  
		  private Object[][] data=   {
				  { new Integer(0),
			         "", new Color(1.0f,1.0f,1.0f), new Boolean(true)}
				  };
			         
		  @Override
			public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnNames.length;
		  }

		  @Override
		  public int getRowCount() {
			  if (data==null) return 0;
			  return data.length;
		  }

		  @Override
		  public Object getValueAt(int row, int col) {
			  if (data==null) return null;
			  return data[row][col];
			}
		
			@Override
			public String getColumnName(int col) {
				return columnNames[col];
			}
		
			@Override
			public void setValueAt(Object value, int row, int col) {
				data[row][col] = value;
       	     fireTableCellUpdated(row, col);
       	     }
				

			/*Use to insert checkboxes into table*/
       	    @Override
			public Class getColumnClass(int c) {
            	 if (getValueAt(0, c)==null) return null;
                return getValueAt(0, c).getClass();
             }
             
             @Override
			public boolean isCellEditable(int row, int col) {
                 if (col==0) {
                     return false;
                 } else {
                     return true;
                 }
             }

			public void setData(Object[][] data){
				this.data=data;
			}

	}

	
	/**
	 * Colour chooser window. Allows user to assign colours to  click templates.
	 * @author Jamie Macaulay
	 *
	 */
	class ColourChooser extends PamDialog implements ChangeListener{

		private static final long serialVersionUID = 1L;
		
		JColorChooser tcc;
		
		public ColourChooser(Window parentFrame, Point pt){
		super(parentFrame, "Create Template", false);
		this.tcc = new JColorChooser();
		tcc.setBorder(BorderFactory.createTitledBorder("Choose Text Color"));
		tcc.setPreviewPanel(new JPanel());
		tcc.getSelectionModel().addChangeListener(this);
		this.setPreferredSize(new Dimension(600, 400));
		this.setResizable(true);
		this.setDialogComponent(tcc);
		
			if (pt != null) {
				setLocation(pt);
			}
		}

		@Override
		public void stateChanged(ChangeEvent arg0) {
		 Color newColour = tcc.getColor();
//		 System.out.println("New Color");
			fftColour=newColour;
		}

		@Override
		public boolean getParams() {		
			spectrumPlot.rePaintAll();
			return true;
		}

		@Override
		public void cancelButtonPressed() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void restoreDefaultSettings() {
			// TODO Auto-generated method stub
			
		}

	}
	
	/**
	 * Mouse functions for the preview plot window. Right click brings up pop up menu
	 * @author Jamie Macaulay
	 *
	 */
	private class MouseMenuFuncs extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showMenu(e);
		}

		private void showMenu(MouseEvent e) {
			if (e.isPopupTrigger()){
				JPopupMenu menu=graphPopUpMenu();
				menu.setVisible(true);
				//System.out.println("Point" + e.getX()+e.getY());
				menu.show(e.getComponent(), e.getX(), e.getY());
				
			}
		}
	}
	
	
	private class MouseEventFuncs extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showMenu(e);
		}

		private void showMenu(MouseEvent e) {
			if (e.isPopupTrigger()){
				JPopupMenu menu= eventPopUpMenu();
				menu.setVisible(true);
				//System.out.println("Point" + e.getX()+e.getY());
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	
	
	
	/**
	 * Creates the plot panel and axis of the template preview window. Shows a mean spectrum of the selected event/s along with confidence intervals. The template can be viewed with a normal or log axis. 
	 * @author Jamie Macaulay
	 *
	 */
	 class SpectrumPlotPanel   {
		private JPanel mainPlotPanel;
		SpectrumAxis axisPanel;
		PamPanel plotPanel;
		JPanel innerPanel;
		private PamAxis xAxis, yAxis;
		
		private SpectrumPlotPanel(){
			
			mainPlotPanel = new JPanel();
			mainPlotPanel.setLayout(new BorderLayout());
			
			axisPanel = new SpectrumAxis();
			
			plotPanel=new SpecPlotPanel();
			
			innerPanel = new JPanel(new BorderLayout());
			innerPanel.add(BorderLayout.CENTER, plotPanel);
			innerPanel.setBorder(PamBorder.createInnerBorder());
			
			axisPanel.setInnerPanel(innerPanel);
			axisPanel.setPlotPanel(plotPanel);
			
			MouseMenuFuncs mouseFunction=new MouseMenuFuncs();
			plotPanel.addMouseListener(mouseFunction);
			
			setResizable(true);
//			mainPlotPanel.setBorder(new TitledBorder("Template Preview"));
			mainPlotPanel.add(BorderLayout.CENTER,axisPanel);
			mainPlotPanel.validate();
			
		}
		
		private JPanel getPanel() {
			return mainPlotPanel;
		}
		
		public void rePaintAll(){
			this.plotPanel.repaint();
			this.axisPanel.repaint();
		}
		
	
	
		/**
		 * Creates the plot panel which shows a preview of the mean spectrum of an event along with 95% confidence intervals. 
		 * @author Jamie Macaulay
		 *
		 */
		class SpecPlotPanel extends PamPanel {
		
			private static final long serialVersionUID = 1L;

			public SpecPlotPanel() {
				super(PamColor.PlOTWINDOW);
			}
			
			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				          RenderingHints.VALUE_ANTIALIAS_ON);
				        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				          RenderingHints.VALUE_RENDER_QUALITY);
				
				super.paintComponent(g2);
				if (logScaleb) paintPLineSpectrum(g, averageFFTLog,stdFFTLog);
				else paintPLineSpectrum(g, averageFFT,stdFFT);
				//System.out.println("RepaintPlot: "+System.currentTimeMillis());
			}
				
			
			
			private void paintPLineSpectrum(Graphics g, double[] specdata, double[] stdSpec){

				if (specdata==null){
					System.out.println("null SpecData");
					return;
				}
				
				double[] specDataTemp=Arrays.copyOf(specdata, specdata.length);
				
				double xScale, yScale;
				
				Graphics2D g2=(Graphics2D) g;
				Rectangle r = getBounds();
				double maxVal=-Double.MAX_VALUE;
				
				if (confidenceIntervalsb==true && stdSpec!=null){
					
				double[] lowerStd=new double[specdata.length];
				double[] upperStd=new double[specdata.length];
				
				for (int i=0; i<stdSpec.length;i++){
					lowerStd[i]=specdata[i]-(stdSpec[i]*2);
					upperStd[i]=specdata[i]+(stdSpec[i]*2);
					//System.out.println(lowerStd[i]+"    "+ upperStd[i]);
				}
				
				//work out max value
				for (int i=0; i< specdata.length; i++){
					if (maxVal<upperStd[i]) maxVal=upperStd[i];
				}
				
				xScale = (double) r.width / (double) (specdata.length - 1);
				yScale = r.height / (maxVal * 1.1);
				
				if (logScaleb){
					yScale = r.height / Math.abs(logMinimum-maxVal);
				}

				GeneralPath polygonstdUp = ClickSpectrum.drawPolygon(upperStd,maxVal,xScale,yScale,logScaleb,r);
				GeneralPath polygonstdLw = ClickSpectrum.drawPolygon(lowerStd,maxVal,xScale,yScale,logScaleb,r);
				g2.setStroke(dashedtmplate);
				g2.setPaint(fftColour);
				g2.draw(polygonstdUp);
				g2.draw(polygonstdLw);
				
				}
				
				else{
					for (int i=0; i< specdata.length; i++){
						if (maxVal<specdata[i]) maxVal=specdata[i];
					}
					xScale = (double) r.width / (double) (specdata.length - 1);
					yScale = r.height / (maxVal * 1.1);
					if (logScaleb)yScale = r.height / Math.abs(logMinimum) ;
				}

				GeneralPath polygon = ClickSpectrum.drawPolygon(specDataTemp,maxVal,xScale,yScale,logScaleb,r);
				g2.setPaint(fftColour.darker());
				g2.setStroke(solid);
				g2.draw(polygon);
	
			}
		}
		
		
		/**
		 * Create the spectrum axis. This will resize if the window is resized. The y axis will either be 0-1 for normalised spectrum and logMax-logMin for log spectrum
		 * @author Jamie Macaulay
		 *
		 */
		class SpectrumAxis extends PamAxisPanel {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public SpectrumAxis() {
				super();
				xAxis = new PamAxis(0, 0, 1, 1, 0, 1, false, "Frequency (kHz)", "%.1f");
				yAxis = new PamAxis(0, 0, 1, 1, 0, 1, true, "Amplitude", "%d");
				setSouthAxis(xAxis);
				setWestAxis(yAxis);
				this.SetBorderMins(10, 50, 10, 20);
			}

			@Override
			public void paintComponent(Graphics g) {
				setSpectrumXAxis();
				setSpectrumYAxis();
				super.paintComponent(g);
				//System.out.println("RepaintAxis: "+System.currentTimeMillis());

			}
			
			public void setSpectrumXAxis() {
				xAxis.setRange(0, sampleRate/ 2 / 1000);
			}
			
			
			public void setSpectrumYAxis() {
				if (logScaleb) {
					yAxis.setRange(logMinimum,logMaximum);
					yAxis.setLabel("Amplitude (dB)");
					yAxis.setInterval(PamAxis.INTERVAL_AUTO);
				}
				else {
					yAxis.setRange(0, 1);
					yAxis.setLabel("Amplitude (Linear)");
					yAxis.setInterval(PamAxis.INTERVAL_AUTO);
				}
				this.SetBorderMins(10, 50, 10, 20);
				

			}
		}
	}
	 
	 
	 //Useful functions
	 
	 /**
	  * Deletes template from memory; -both the template and corresponding setVisble boolean must be deleted. 
	  */
	 public void deleteTemplate(int row){
			ArrayList<ClickTemplate> clickTempl=new ArrayList<ClickTemplate>();
			ArrayList<Boolean> setVisible= new ArrayList<Boolean>();
			for (int i=0; i<clickSpectrumTemplateParams.clickTemplateArray.size();i++){
				if (i!= row){
					clickTempl.add(clickSpectrumTemplateParams.clickTemplateArray.get(i));
					setVisible.add(clickSpectrumTemplateParams.clickTempVisible.get(i));
				}
			}
			clickSpectrumTemplateParams.clickTemplateArray=clickTempl;
			clickSpectrumTemplateParams.clickTempVisible=setVisible;
			
	}
	 
	/**
	 * Opens file save dialog and allows user to select save location.
	 * @return Path to save file to
	 */
	public String saveFileBrowser(){
			
			JFileChooser fileChooser = new PamFileChooser();
			PamFileFilter fileFilter = new PamFileFilter("Click Template", ".csv");		
			fileChooser.setFileFilter(fileFilter);
			
			int state = fileChooser.showSaveDialog(parentFrame);
			if (state == JFileChooser.APPROVE_OPTION) {
				File currFile = fileChooser.getSelectedFile();
				//System.out.println(currFile);
				return currFile.getAbsolutePath();
			}
			
		return null;
	}
	 
		/**
		 * Converts and ArrayList<Double> to a double[]
		 * @param array
		 * @return double[] array with same values as inputed ArrayList<Double>
		 */
	 public static double[] convertToDoubleArray(ArrayList<Double> array){
		 if (array==null) return null;
		 
		 double[] newArray=new double[array.size()];
		 for (int i=0; i<array.size();i++){
			 newArray[i]=array.get(i);
		 }
		return newArray;
	 }
	 
	 public static ArrayList<Double> convertToArrayList(double[] array){
		 if (array==null) return null;
		 
		 ArrayList<Double> newArray=new ArrayList<Double>();
		 for (int i=0; i<array.length;i++){
			 newArray.add(array[i]);
		 }
		return newArray;
	 }
	 
	 /**
	  * Creates a click template from the saved instance variables. Be careful that these variables have been updated when using this function.
	  * @return ClickTemplate
	  */
	 public ClickTemplate createTemplate(){
		 if (averageFFT==null)return null;
		 if (averageFFTLog==null)return null;
			if (averageFFT.length==0)return null;
			
			ClickTemplate clickTemplate=new ClickTemplate();
			clickTemplate.setSampleRate(sampleRate);
			clickTemplate.setSpectrum(averageFFT);
			clickTemplate.setSpectrumLog(averageFFTLog);
			clickTemplate.setSpectrumStd(stdFFT);
			clickTemplate.setSpectrumStdLog(stdFFTLog);
			clickTemplate.setColor(fftColour);
			clickTemplate.setN(N);
			
			return clickTemplate;
	 }
	
	
	public boolean setParams(){
		templateTable.refreshData(clickSpectrumTemplateParams.clickTemplateArray,clickSpectrumTemplateParams.clickTempVisible);
		return true;
	}
	
	/**
	 * Updates the clickSpectrum whenever a template is added or removed. This function basically changes the clickSpectrum before the dialog box is closed.  
	 */
	public void updateClickSpectrum(){
		try {
			clickSpectrum.setClickTemplateParams(clickSpectrumTemplateParams);
			clickSpectrum.getTemplateClick();
			clickSpectrum.repaint(100);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	@Override
	public boolean getParams() {
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}


	
}
