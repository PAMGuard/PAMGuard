package Localiser.algorithms.genericLocaliser.MCMC.old;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Localiser.algorithms.genericLocaliser.MCMC.MCMCParams2;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;


public class MCMCParamsDialog extends PamDialog{
	
	private static final long serialVersionUID = 1L;

	private static MCMCParamsDialog singleInstance;

	/**
	 * MCMC params for loclaiser
	 */
	private MCMCParams2 mCMCParameters2;
	
	/**
	 * Other MCMC params - legacy settings class. 
	 */
	protected MCMCParams mCMCParameters;
	
	private JTextField Error;
	private JTextField jumpSize;
	private JTextField numberOfJumps;
	private JTextField numberOfChains;
	private JTextField ChainStartDispersion;
	private JCheckBox cylindricalCoOrdinates;
	
	private JRadioButton percentageButton;
	private JTextField percentage;
	
	private JRadioButton kMeansClustering;
	private JTextField nClusters; 
	private JTextField minDistance;

	private JRadioButton noClustering;

	protected PamPanel mainPanel;
	protected PamPanel mCMCPropPanel;

	/**
	 * The panel which holds controsl for changing the error. 
	 */
	private PamPanel errors; 


	
	public MCMCParamsDialog(Window parentFrame) {
		
		super(parentFrame, "MCMC Settings", false);
		
		mCMCPropPanel = mCMCPropertiesPanel();
		mainPanel=createMainPanel();
	
		setDialogComponent(mainPanel);
		
	}
	
	protected PamPanel createMainPanel() {
		
		PamPanel mainPanel=new PamPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER,mCMCPropPanel);
		
		return mainPanel;
	}

	/**
	 * Create the panel which allows users to change params for the MCMC chains
	 * @return 
	 */
	private PamPanel mCMCPropertiesPanel(){
		
	    GridBagConstraints c = new PamGridBagContraints();
		
	    errors=new PamPanel(new GridBagLayout());
		errors.setBorder(new TitledBorder("Errors"));
		
		PamDialog.addComponent(errors, new JLabel("Time Error ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(errors, Error = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(errors, new JLabel(" (samples)", SwingConstants.LEFT), c);
		c.gridy++;
		
		PamPanel markovChain=new PamPanel(new GridBagLayout());
		markovChain.setBorder(new TitledBorder("Markov Chain"));
		
		
		c.gridx=c.gridx-2;
		PamDialog.addComponent(markovChain, new JLabel("Max Jump Size ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, jumpSize = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, new JLabel(" (meters) ", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx=c.gridx-2;
		PamDialog.addComponent(markovChain, new JLabel(" Number of Jumps ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, numberOfJumps  = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, new JLabel(" (int)", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx=c.gridx-2;
		PamDialog.addComponent(markovChain, new JLabel("Number of Chains ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, numberOfChains  = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, new JLabel(" (int) ", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx=c.gridx-2;
		PamDialog.addComponent(markovChain, new JLabel(" Chain Start Dispersion", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, ChainStartDispersion  = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, new JLabel(" (meters) ", SwingConstants.LEFT), c);
		c.gridy++;
		c.gridx=c.gridx-2;
		PamDialog.addComponent(markovChain, new JLabel("", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(markovChain, cylindricalCoOrdinates  = new JCheckBox("Cylindrical Co-Ordinates"),c);
		c.gridx++;
		
		PamPanel markovChainDiagnosis=new PamPanel(new GridBagLayout());
		markovChainDiagnosis.setBorder(new TitledBorder("Chain Convergence"));
		
		percentageButton = new JRadioButton("Remove Fixed Percentage");
		percentageButton.addActionListener(new PercentageButtonSelect());
		
		ButtonGroup group = new ButtonGroup();
		group.add(percentageButton);
		
		
		c.gridx=c.gridx-2;
		PamDialog.addComponent(markovChainDiagnosis, percentageButton,c);
		c.gridx++;
		PamDialog.addComponent(markovChainDiagnosis,  percentage = new JTextField(6), c);
		c.gridx++;
		PamDialog.addComponent(markovChainDiagnosis,  new JLabel("%"), c);
		
		
		PamPanel clustering=new PamPanel(new GridBagLayout());
		clustering.setBorder(new TitledBorder("Chain Clustering"));
		
		noClustering = new JRadioButton("none: ");
		noClustering.addActionListener(new kMeansButtonSelect());
		
		kMeansClustering = new JRadioButton("k-means: ");
		kMeansClustering.addActionListener(new kMeansButtonSelect());
		
		ButtonGroup clusterGroup = new ButtonGroup();
		clusterGroup.add(kMeansClustering);
		clusterGroup.add(noClustering);
		

		c.gridx=c.gridx-2;
		
		PamDialog.addComponent(clustering,noClustering ,c);
		c.gridy++;
		PamDialog.addComponent(clustering,kMeansClustering ,c);
		c.gridx++;
		PamDialog.addComponent(clustering,  new JLabel("No. clusters"), c);
		c.gridx++;
		PamDialog.addComponent(clustering,  nClusters = new JTextField(4), c);
		c.gridx++;
		PamDialog.addComponent(clustering,  new JLabel(" (int) "), c);
		c.gridy++;
		c.gridx=c.gridx-2; 
		PamDialog.addComponent(clustering,  new JLabel("Max cluster Size"), c);
		c.gridx++;
		PamDialog.addComponent(clustering,  minDistance = new JTextField(4), c);
		c.gridx++;
		PamDialog.addComponent(clustering,  new JLabel(" (meteres) "), c);
		
		
		GridBagConstraints cMain = new PamGridBagContraints();
		PamPanel mainPanel = new PamPanel(new GridBagLayout());
		PamDialog.addComponent(mainPanel,  errors, cMain);
		cMain.gridy++;
		cMain.gridy++;
		PamDialog.addComponent(mainPanel,  markovChain, cMain);
		cMain.gridy++;
		cMain.gridy++;
		PamDialog.addComponent(mainPanel,  markovChainDiagnosis, cMain);
		cMain.gridy++;
		cMain.gridy++;
		PamDialog.addComponent(mainPanel,  clustering, cMain);
		
		return mainPanel; 
		
	}
	
	class PercentageButtonSelect implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			percentage.setEnabled(percentageButton.isSelected());	
		}
	}
	
	class kMeansButtonSelect implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			kMeanSelected(kMeansClustering.isSelected());
		}
	}
	
	public void kMeanSelected(boolean selected){
		nClusters.setEnabled(selected);	
		minDistance.setEnabled(selected);
	}
	
	
	public static MCMCParams showDialog(Frame frame, MCMCParams mcmcParams) {
		if (getSingleInstance() == null || getSingleInstance().getOwner() != frame) {
			setSingleInstance(new MCMCParamsDialog(frame));
		}
		getSingleInstance().mCMCParameters2=null; 
		getSingleInstance().mCMCParameters = mcmcParams;
		getSingleInstance().setParams(mcmcParams);
		getSingleInstance().setVisible(true);
		
		return getSingleInstance().mCMCParameters;
	}
	
	
	/**
	 * Show dialog for new MCMCParams (MCMCParams2). 
	 * @param frame - the parent frame. 
	 * @param mcmcParams - parameters class. 
	 * @return
	 */
	public static MCMCParams2 showDialog(Frame frame, MCMCParams2 mcmcParams) {
		if (getSingleInstance() == null || getSingleInstance().getOwner() != frame) {
			setSingleInstance(new MCMCParamsDialog(frame));
		}
		getSingleInstance().mCMCParameters=null; 
		getSingleInstance().mCMCParameters2 = mcmcParams;
		getSingleInstance().setParams(mcmcParams);
		getSingleInstance().setVisible(true);
		
		return getSingleInstance().mCMCParameters2;
	}
	
	
	protected void setParams(MCMCParams2 mCMCParameters) {
		
		MCMCParams2 params =mCMCParameters;
		
		this.errors.setVisible(false);
		
		//Error.setText(String.format("%3.2f", params.timeError));
		
		jumpSize.setText(String.format("%3.2f", params.jumpSize[0]));
		
		numberOfJumps.setText(String.valueOf(params.numberOfJumps));
		
		numberOfChains.setText(String.valueOf(params.numberOfChains));
		
		ChainStartDispersion.setText(String.format("%3.2f",params.chainStartDispersion[0][1]));
		
		cylindricalCoOrdinates.setSelected(params.cylindricalCoOrdinates);
		
		//chain analysis 
		if (params.chainAnalysis==MCMCParams.IGNORE_PERCENTAGE) percentageButton.setSelected(true);
		else percentageButton.setSelected(false);
		
		percentage.setText(String.format("%3.1f", 100*params.percentageToIgnore));
		
		//cluster analysis 
		if (params.clusterAnalysis==MCMCParams.NONE) noClustering.setSelected(true);
		if (params.clusterAnalysis==MCMCParams.K_MEANS) kMeansClustering.setSelected(true);
		kMeanSelected(kMeansClustering.isSelected());
	
		if (params.nKMeans==null) params.nKMeans=2;
		nClusters.setText(params.nKMeans.toString());
		minDistance.setText(String.format("%3.2f",params.maxClusterSize));

	}

	
	/**
	 * Set parameters for old MCMCParam class. 
	 * @param mCMCParameters - the MCMCM parameters class. 
	 */
	protected void setParams(MCMCParams mCMCParameters) {
		
		this.errors.setVisible(true);

		
		MCMCParams params =mCMCParameters;
		
		Error.setText(String.format("%3.2f", params.timeError));
		
		jumpSize.setText(String.format("%3.2f", params.jumpSize));
		
		numberOfJumps.setText(params.numberOfJumps.toString());
		
		numberOfChains.setText(params.numberOfChains.toString());
		
		ChainStartDispersion.setText(String.format("%3.2f",params.chainStartDispersion));
		
		cylindricalCoOrdinates.setSelected(params.cylindricalCoOrdinates);
		
		//chain analysis 
		if (params.chainAnalysis==MCMCParams.IGNORE_PERCENTAGE) percentageButton.setSelected(true);
		else percentageButton.setSelected(false);
		
		percentage.setText(String.format("%3.1f", params.percentageToIgnore));
		
		//cluster analysis 
		if (params.clusterAnalysis==MCMCParams.NONE) noClustering.setSelected(true);
		if (params.clusterAnalysis==MCMCParams.K_MEANS) kMeansClustering.setSelected(true);
		kMeanSelected(kMeansClustering.isSelected());
	
		if (params.nKMeans==null) params.nKMeans=2;
		nClusters.setText(params.nKMeans.toString());
		minDistance.setText(String.format("%3.2f",params.maxClusterSize));

	}
	
	@Override
	public boolean getParams() {
		//use new settings class
		if (this.mCMCParameters==null) {
			return getParams2(); 
		}
		//old settings class
		else return getParams1(); 
	}

	
	
	public boolean getParams2() {
		
		try {
			//Error
			//double newError = Double.valueOf(Error.getText());
			//Markov Chain
			double newJumpsize = Double.valueOf(jumpSize.getText());
			int newnumberOfJumps = Integer.valueOf(numberOfJumps.getText());
//			System.out.println("newnumberOfJumps: "+newnumberOfJumps);
			int newnumberOfChains = Integer.valueOf(numberOfChains.getText());
//			System.out.println("newnumberOfChains: "+newnumberOfChains);
			double newChainStartDispersion = Double.valueOf(ChainStartDispersion.getText());
			boolean newCylindricalCoOrdinates=cylindricalCoOrdinates.isSelected();
			//Diagnosis	
			double newPercentage = Double.valueOf(percentage.getText());
			//Cluster analysis
			int numClusters=Integer.valueOf(nClusters.getText());
			double minClustereDist=Double.valueOf(minDistance.getText());
			
			//errors
			//mCMCParameters2.timeError = newError;
//			System.out.println("New Error"+mCMCParameters2.timeError);
			//chain properties
			mCMCParameters2.jumpSize= new double[] {newJumpsize, newJumpsize, newJumpsize};
			mCMCParameters2.numberOfJumps=newnumberOfJumps;
			mCMCParameters2.numberOfChains=newnumberOfChains;
			mCMCParameters2.chainStartDispersion = new double[][] {{0, newChainStartDispersion},
				{0, newChainStartDispersion}, {0,newChainStartDispersion}};
			mCMCParameters2.cylindricalCoOrdinates=newCylindricalCoOrdinates;
			// chain analysis
			if (percentageButton.isSelected()){
				mCMCParameters2.chainAnalysis=MCMCParams.IGNORE_PERCENTAGE;
			}
			mCMCParameters2.percentageToIgnore=newPercentage/100;
			
			//clustering
			if (kMeansClustering.isSelected()) mCMCParameters2.clusterAnalysis=MCMCParams2.K_MEANS;
			if (noClustering.isSelected()) mCMCParameters2.clusterAnalysis=MCMCParams2.NONE;
			
			if (numClusters<=0) return PamDialog.showWarning(null, "MCMC Parameters", "Number of cluster must be an integer >0");
			mCMCParameters2.nKMeans= numClusters;
			if (minClustereDist<=0) return PamDialog.showWarning(null, "MCMC Parameters", "Min cluster size must be >0");
			mCMCParameters2.maxClusterSize=minClustereDist;
		}
		
		catch (NumberFormatException e) {
			e.printStackTrace();
			return PamDialog.showWarning(null, "MCMC Parameters", "Invalid value");
		}
		return true;
	}

	
	public boolean getParams1() {
		
		try {
			//Error
			double newError = Double.valueOf(Error.getText());
			
			//Markov Chain
//			System.out.println("newnumberOfJumps: "+newError);
			double newJumpsize = Double.valueOf(jumpSize.getText());
			int newnumberOfJumps = Integer.valueOf(numberOfJumps.getText());
//			System.out.println("newnumberOfJumps: "+newnumberOfJumps);
			int newnumberOfChains = Integer.valueOf(numberOfChains.getText());
//			System.out.println("newnumberOfChains: "+newnumberOfChains);
			double newChainStartDispersion = Double.valueOf(ChainStartDispersion.getText());
			boolean newCylindricalCoOrdinates=cylindricalCoOrdinates.isSelected();
			//Diagnosis	
			double newPercentage = Double.valueOf(percentage.getText());
			//Cluster analysis
			int numClusters=Integer.valueOf(nClusters.getText());
			double minClustereDist=Double.valueOf(minDistance.getText());
			
		
			//errors
			mCMCParameters.timeError = newError;
//			System.out.println("New Error"+mCMCParameters.timeError);
			//chain properties
			mCMCParameters.jumpSize= newJumpsize;
			mCMCParameters.numberOfJumps=newnumberOfJumps;
			mCMCParameters.numberOfChains=newnumberOfChains;
			mCMCParameters.chainStartDispersion=newChainStartDispersion;
			mCMCParameters.cylindricalCoOrdinates=newCylindricalCoOrdinates;
			// chain analysis
			if (percentageButton.isSelected()){
				mCMCParameters.chainAnalysis=MCMCParams.IGNORE_PERCENTAGE;
			}
			mCMCParameters.percentageToIgnore=newPercentage;
			
			//clustering
			if (kMeansClustering.isSelected()) mCMCParameters.clusterAnalysis=MCMCParams.K_MEANS;
			if (noClustering.isSelected()) mCMCParameters.clusterAnalysis=MCMCParams.NONE;
			
			if (numClusters<=0) return PamDialog.showWarning(null, "MCMC Parameters", "Number of cluster must be an integer >0");
			mCMCParameters.nKMeans= numClusters;
			if (minClustereDist<=0) return PamDialog.showWarning(null, "MCMC Parameters", "Min cluster size must be >0");
			mCMCParameters.maxClusterSize=minClustereDist;
		}
		
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, "MCMC Parameters", "Invalid value");
		}
	
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		mCMCParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	public static MCMCParamsDialog getSingleInstance() {
		return singleInstance;
	}

	public static void setSingleInstance(MCMCParamsDialog singleInstance) {
		MCMCParamsDialog.singleInstance = singleInstance;
	}

}
