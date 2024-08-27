package spectrogramNoiseReduction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;

public class SpectrogramNoiseDialogPanel {

	private JPanel p;
	
	private SpectrogramNoiseProcess spectrogramNoiseProcess;

	private ArrayList<SpecNoiseMethod> methods;

	private JCheckBox[] enableMethod;
	
	private PamDataBlock dataSource;
	
	private SourcePanel sourcePanel;
	
	public SpectrogramNoiseDialogPanel(
			SpectrogramNoiseProcess spectrogramNoiseProcess) {
		super();
		this.spectrogramNoiseProcess = spectrogramNoiseProcess;

		p = new JPanel();
		p.setBorder(new EmptyBorder(10,10,10,10));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		
		methods = spectrogramNoiseProcess.getMethods();
		JPanel methodPanel;
		SpecNoiseDialogComponent dC;
		JComponent component;
		enableMethod = new JCheckBox[methods.size()];

		for (int i = 0; i < methods.size(); i++) {
			methodPanel = new JPanel();
			methodPanel.setBorder(new TitledBorder(methods.get(i).getName()));
			methodPanel.setLayout(new BorderLayout());
			methodPanel.add(BorderLayout.NORTH, enableMethod[i] = 
				new JCheckBox("Run " + methods.get(i).getName()));
			enableMethod[i].addActionListener(new CheckEnable());
			enableMethod[i].setToolTipText(methods.get(i).getDescription());
			dC = methods.get(i).getDialogComponent();
			if (dC != null) {
				component = dC.getSwingComponent();
				if (component != null) {
					methodPanel.add(BorderLayout.CENTER, component);
				}
			}
			p.add(methodPanel);
		}

	}
	
	public Component getPanel() {
		return p;
	}
	
	/**
	 * Set a source panel so that the dialog panel can respond to source changes. 
	 * @param sourcePanel sourcepanel. 
	 */
	public void setSourcePanel(SourcePanel sourcePanel) {
		this.sourcePanel = sourcePanel;
		sourcePanel.addSelectionListener(new SourceListener());
	}
	private class SourceListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			setSource(sourcePanel.getSource());
		}
		
	}
	
	public void setSource(PamDataBlock sourceDataBlock) {
		this.dataSource = sourceDataBlock;
		enableControls();
	}

	public void setParams(SpectrogramNoiseSettings spectrogramNoiseSettings) {
		SpecNoiseDialogComponent dC;
		for (int i = 0; i < methods.size(); i++) {
			enableMethod[i].setSelected(spectrogramNoiseSettings.isRunMethod(i));
			dC = methods.get(i).getDialogComponent();    
			if (dC != null) {
				dC.setParams();
				dC.setSelected(enableMethod[i].isSelected());
			}

		}

		enableControls();
		
	}
	
	public boolean getParams(SpectrogramNoiseSettings spectrogramNoiseSettings) {
		if (spectrogramNoiseSettings == null) {
			return false;
		}
		SpecNoiseDialogComponent dC;
		boolean answer;
		boolean sel;
		for (int i = 0; i < methods.size(); i++) {
			sel = enableMethod[i].isSelected();
			spectrogramNoiseSettings.setRunMethod(i, sel);
			if (sel) {
				dC = methods.get(i).getDialogComponent();
				if (dC != null && enableMethod[i].isSelected()) {
					answer = dC.getParams();
					if (!answer) {
						return false;
					}
				}
			}
		}
		return true;
		
	}

	class CheckEnable implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	public void enableControls() {
		SpecNoiseDialogComponent dC;
		boolean done;
		for (int i = 0; i < methods.size(); i++) {
			dC = methods.get(i).getDialogComponent();
			done = alreadyDone(methods.get(i));
			if (done) {
				enableMethod[i].setSelected(false);
			}
			enableMethod[i].setEnabled(!done);
			if (dC != null) {
				dC.setSelected(enableMethod[i].isSelected());
			}
		}
	}
	
	private boolean alreadyDone(SpecNoiseMethod noiseMethod) {
		if (dataSource == null) {
			return false;
		}
		return (dataSource.findAnnotation(noiseMethod.getAnnotation(spectrogramNoiseProcess)) != null);
	}
	
	/**
	 * Will return true for a method if either this panel has the method selected OR
	 * the source data has already had that method applied. 
	 * @param iMethod
	 * @return true if the method has been selected. 
	 */
	public boolean hasProcessed(int iMethod) {
		if (iMethod < 0 || iMethod >= methods.size()) {
			return false;
		}
		SpecNoiseMethod method = methods.get(iMethod);
		if (alreadyDone(method)) {
			return true;
		}
		if (enableMethod[iMethod].isSelected()) {
			return true;
		}
		return false;
	}

}
