package whistleClassifier;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import PamView.PamTabPanel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.PamBorderPanel;
import pamMaths.HistogramDisplay;

public class WhistleClassifierTabPanelControl implements PamTabPanel {

	WhistleClassifierControl whistleClassifierControl;
	
	private WhistleClassifierPanel whistleClassifierPanel;
	
	HistogramDisplay[] fitDisplays = new HistogramDisplay[3];
	HistogramDisplay posInflections, negInflections;
	
	ClassifierHistoryWindow classifierHistoryWindow;
	
	public WhistleClassifierTabPanelControl(WhistleClassifierControl whistleClassifierControl) {
		super();
		this.whistleClassifierControl = whistleClassifierControl;
		
		FragmentStore fragmentStore = findFragmentStore();
		
		for (int i = 0; i < 3; i++) {
			fitDisplays[i] = new HistogramDisplay(fragmentStore.getFitHistogram(i));
			fitDisplays[i].setXLabel("Hz");
			fitDisplays[i].setXAxisNumberFormat("%d");
			fitDisplays[i].setStatsWindowPos(CornerLayoutContraint.FIRST_LINE_END);
		}
		fitDisplays[1].setXLabel("Hz/s");
		fitDisplays[2].setXLabel("Hz/s^2");
		negInflections = new HistogramDisplay(fragmentStore.getNegInflectionsHistogram());
		negInflections.setXAxisNumberFormat("%d");
		negInflections.setStatsWindowPos(CornerLayoutContraint.FIRST_LINE_END);
		posInflections = new HistogramDisplay(fragmentStore.getPosInflectionsHistogram());
		posInflections.setXAxisNumberFormat("%d");
		posInflections.setStatsWindowPos(CornerLayoutContraint.FIRST_LINE_END);

		classifierHistoryWindow = new ClassifierHistoryWindow(whistleClassifierControl);
		
		whistleClassifierPanel = new WhistleClassifierPanel();
		
	}
	
	FragmentStore findFragmentStore() {
		return whistleClassifierControl.whistleClassifierProcess.getFragmentStore();
	}

	@Override
	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getPanel() {
		return whistleClassifierPanel;
	}

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void prepareDisplay() {
		classifierHistoryWindow.noteNewSettings();
	}
	
	class WhistleClassifierPanel extends PamBorderPanel {

		public WhistleClassifierPanel() {
			super();
			JPanel hPanel = new PamBorderPanel();
			hPanel.setBorder(new TitledBorder("Classification Statistics"));
			JPanel hPanelC = new PamBorderPanel();
			JPanel hPanelE = new PamBorderPanel();
			hPanel.setLayout(new BorderLayout());
			hPanelC.setLayout(new GridLayout(1,4));
			hPanelE.setLayout(new GridLayout(2,1));
			hPanel.add(BorderLayout.CENTER, hPanelC);
			
			for (int i = 0; i < 3; i++) {
				hPanelC.add(fitDisplays[i].getGraphicComponent());
			}
			
			hPanelC.add(hPanelE);
			hPanelE.add(negInflections.getGraphicComponent());
			hPanelE.add(posInflections.getGraphicComponent());
			this.setLayout(new GridLayout(2,1));
			this.add(hPanel);
			this.add(classifierHistoryWindow.getGraphicComponent());
		}
		
	}

}
