package clickDetector.localisation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Localiser.ModelControlPanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;

/**
 * Dialog panel for click localisation, and probably some
 * other types of localisation too ... 
 * @author dg50
 *
 */
public class ClickLocDialogPanel implements PamDialogPanel {
	
	/**
	 * Reference to the group localiser. 
	 */
	protected GeneralGroupLocaliser groupLocaliser;

	/*
	 * The model control panel  
	 */
	private ModelControlPanel modelControlPanel;


	private JTextField maxRange;


	private JTextField maxDepth;


	private JTextField maxTime;


	private JTextField minDepth;

	private JCheckBox limitPoints;
	
	private JTextField maxPoints;

	/** 
	 * The main panel
	 */
	private JPanel p;

	/**
	 * The tab pane that holds the various tabs. 
	 */
	private JTabbedPane tPane;

	/**
	 * The filter panel holder.
	 */
	private JPanel filterPanelHolder;

	/**
	 * The algorithm panel holder. 
	 */
	private JPanel algPanel;


	public ClickLocDialogPanel(GeneralGroupLocaliser groupLocaliser) {
		this.groupLocaliser = groupLocaliser;
		//the model control pane. 
		modelControlPanel=new ModelControlPanel();
		modelControlPanel.getPanel().setBorder(new TitledBorder("Localisation Algorithms"));

		//panel for filtering dat
		JPanel filterPanel=new JPanel(); 
		filterPanel.setBorder(new TitledBorder("Results Filter"));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new PamGridBagContraints();
		filterPanel.setLayout(layout);
		
		//the maximum range
		constraints.gridy = 0;
		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Maximum Range"), constraints);
		constraints.gridx++;
		PamDialog.addComponent(filterPanel, maxRange=new JTextField(6), constraints);
		maxRange.setToolTipText("<html><p width=\"200\">The maximum range for a loclaisation before it is discarded. "
								+ "Localisation algorithms can 'run away' to infinity if "
								+ "there are large errors in the data used to loclaise. This "
								+ "should be around the same value as the maximum expected detection"
								+ " range for the target species.</p></html> "); 
		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel(" m"), constraints);
		
		// the maximum depth
		constraints.gridy = 1;
		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Minimum Depth"), constraints);
		constraints.gridx++;
		PamDialog.addComponent(filterPanel, minDepth=new JTextField(6), constraints);
		minDepth.setToolTipText("<html><p width=\"200\">The minimum depth for a loclaisation before it is discarded. As error in localisation "
				+ "are always present it is usually a good idea to set this slightly above the sea surface <i>i.e.</i> a small negative value.</p></html>"); 
		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel(" m"), constraints);
		
		
		// the maximum depth
		constraints.gridy = 2;
		constraints.gridx = 0;
		PamDialog.addComponent(filterPanel, new JLabel("Maximum Depth"), constraints);
		constraints.gridx++;
		PamDialog.addComponent(filterPanel, maxDepth=new JTextField(6), constraints);
		maxDepth.setToolTipText("<html><p width=\"200\">The maximum depth for a loclaisation before it is discarded. "
				+ "This should be slightly below the depth of the seabed. </p></html>"); 
		constraints.gridx++;
		PamDialog.addComponent(filterPanel, new JLabel(" m"), constraints);
		
		//the maximum time a localisation can take before a warning is shown. 
		constraints.gridy = 3;
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		JLabel label;
		PamDialog.addComponent(filterPanel, label=new JLabel("<html>Localisation will be selected from the "
				+ "<br/>remaining result with the lowest AIC <br/>score.</html>"), constraints);
		label.setToolTipText("<html><p width=\"200\">Once all algortihms have been checked for a maximum depth and range, the algorithm with the lowest AIC score is selected as the best fit. "
				+ "The AIC score is essentially the chi squared value calculated during the localisation divided by the number of degrees of freedom.</p></html>");

		
		JPanel warningPanel=new JPanel(); 
		warningPanel.setBorder(new TitledBorder("Algorithm Limits"));
		layout = new GridBagLayout();
		constraints = new PamGridBagContraints();
		warningPanel.setLayout(layout);

		constraints.gridwidth = 3;
		warningPanel.add(limitPoints = new JCheckBox("Limit number of sounds"), constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		PamDialog.addComponent(warningPanel, new JLabel("Max number of clicks ", JLabel.RIGHT), constraints);
		constraints.gridx++;
		PamDialog.addComponent(warningPanel, maxPoints=new JTextField(6), constraints);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		PamDialog.addComponent(warningPanel, new JLabel("Max processing time ", JLabel.RIGHT), constraints);
		constraints.gridx++;
		PamDialog.addComponent(warningPanel, maxTime=new JTextField(6), constraints);
		limitPoints.setToolTipText("<html><p width=\"200\">Real time localisation can be slow if large numbers of clicks are included in " + 
		" a target motion analysis. Limiting the number of clicks can reduce processing time. If the maximum number of clicks" + 
				" is exceeded, then clicks will be selected evenly from the entire track.");
		limitPoints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		maxTime.setToolTipText("<html><p width=\"200\">Real time loclaisation can be processor intensive. If a loclaisation takes over the maximum allowed number of milliseconds, a warning will appear in PAMGuard's main window. "
				+ "If this occurs, reduce the number of algorithms or switch to a less processor intensive algorithm e.g. least squares</p></html>");

		constraints.gridx++;
		PamDialog.addComponent(warningPanel, new JLabel(" ms"), constraints);
		
		//create holder
		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		 tPane = new JTabbedPane();
		
		 algPanel = new JPanel();
		algPanel.setLayout(new BoxLayout(algPanel, BoxLayout.Y_AXIS));
		algPanel.add(modelControlPanel.getPanel());
		algPanel.add(warningPanel);
		tPane.add("Algorithm", algPanel);
		
		filterPanelHolder = new JPanel();
		filterPanelHolder.setLayout(new BoxLayout(filterPanelHolder, BoxLayout.Y_AXIS));
		filterPanelHolder.add(filterPanel);

		tPane.addTab("Filters", filterPanelHolder);
		
//		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
//		p.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//		p.add(modelControlPanel.getPanel());
//		p.add(filterPanel);
//		p.add(warningPanel);
		
		p.add(tPane);
	}

	/**
	 * Get the main tab pane. 
	 * @return the tab pane. 
	 */
	public JTabbedPane getTabPane() {
		return tPane;
	}

	@Override
	public JComponent getDialogComponent() {
		return p;
	}
	
	private int getNModels() {
		return groupLocaliser.locAlgorithmList.size();
	}

	@Override
	public void setParams() {
		ClickLocParams clickLocParams2 = groupLocaliser.getClickLocParams();
		//might as well make a new panel as it is possible (but unlikely) that
		//the list of localisers might have changed. 
		if (modelControlPanel.getNModels()==0){
			modelControlPanel.populateModelPanel(groupLocaliser.locAlgorithmList); 
		}
		
		for (int i=0; i<getNModels(); i++){
			modelControlPanel.setEnable(i, clickLocParams2.getIsSelected(i));
		}
		
		maxRange.setText(String.format("%3.1f", clickLocParams2.maxRange));
		minDepth.setText(String.format("%3.1f", -clickLocParams2.maxHeight));
		maxDepth.setText(String.format("%3.1f", -clickLocParams2.minHeight));
		maxTime.setText(String.format("%d", clickLocParams2.maxTime));
		maxPoints.setText(String.format("%d", clickLocParams2.maxLocPoints));
		limitPoints.setSelected(clickLocParams2.limitLocPoints);
		
		//need to pack as number of locliaser might have chnaged. 
		enableControls();
		
	}

	@Override
	public boolean getParams() {
		ClickLocParams clickLocParams = groupLocaliser.getClickLocParams();
		boolean hasSimplex = false;
		for (int i=0; i<getNModels(); i++){
			boolean isSel = modelControlPanel.isEnabled(i);
			clickLocParams.setIsSelected(i, isSel);
			if (isSel) {
				if (groupLocaliser.locAlgorithmList.get(i).getName().contains("Simplex")) {
					hasSimplex = true;
				}
			}
//			System.out.println("Loclaisers enable "+ clickLocParams.isSelected[i]+ " "+modelControlPanel.isEnabled(i));
		}
		
		try {
			clickLocParams.maxRange = Double.valueOf(maxRange.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, groupLocaliser.getName(), "Invalid maximum range value");
		}
		try {
			clickLocParams.minHeight = -Double.valueOf(maxDepth.getText());

		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, groupLocaliser.getName(), "Invalid maximum depth value");
		}
		
		try {
			clickLocParams.maxHeight = -Double.valueOf(minDepth.getText());

		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, groupLocaliser.getName(), "Invalid minimum depth value");
		}
		try {
			clickLocParams.maxTime = Long.valueOf(maxTime.getText());
		}
		catch (NumberFormatException e) {
			return PamDialog.showWarning(null, groupLocaliser.getName(), "Invalid maximum time value");
		}
		
		clickLocParams.limitLocPoints = limitPoints.isSelected();
		if (clickLocParams.limitLocPoints) {
			try {
				clickLocParams.maxLocPoints = Integer.valueOf(maxPoints.getText());
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(null, groupLocaliser.getName(), "Invalid macimum number of clicks");
			}
		}
		
		if (hasSimplex) {
			String msg = "<html>Simplex based methods can be too slow when combined with automatic click train <br>idenitification" + 
		" due to the large number of clicks included in click trains. <p>Consider Least Squares for real time processing.</html>";
			int ans = WarnOnce.showWarning(null, "Click train localisation", 
					msg, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
//			showWarning("Simplex can be too slow when combined with automatic click train idenitification");
		}
		
		return true;
	}
	
	/**
	 * Get the data filter panel. 
	 * @return the data filter panel. 
	 */
	public JPanel getFilterPanel() {
		return filterPanelHolder;
	}
	
	/**
	 * Get the localisation panel. Contains algorithm choice etc. 
	 * @return the loc panel
	 */
	public JPanel getLocPanel() {
		return algPanel;
	}
	
	protected void enableControls() {
		maxPoints.setEnabled(limitPoints.isSelected());
	}

	public GeneralGroupLocaliser getGroupLocaliser() {
		return groupLocaliser;
	}
}
