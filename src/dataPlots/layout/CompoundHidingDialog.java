package dataPlots.layout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dataPlots.data.TDDataInfo;
import PamView.hidingpanel.HidingDialog;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.hidingpanel.HidingDialogPanel;
import PamView.hidingpanel.TabbedHidingPane;

/**
 * A hiding dialog panel for use with multiple display overlays. 
 * If there is only one, then it will go in on it's own. If there
 * are several, then they will be incorporated into a tabbed panel. 
 * @author Doug Gillespie
 *
 */
public class CompoundHidingDialog extends HidingDialogComponent {

	private TDGraph tdGraph;
	
	private ArrayList<HidingDialogComponent> hidingDialogComponents = new ArrayList<>();
	
	private JComponent hidingComponent;

	private CompoundHidingTabPane tabPane;

	private HidingDialogPanel hidingDialogPanel;
	
	public CompoundHidingDialog(TDGraph tdGraph) {
		super();
		this.tdGraph = tdGraph;
	}

	public void createDataList() {
		hidingDialogComponents.clear();
		for (TDDataInfo aInfo:tdGraph.getDataList()) {
			HidingDialogComponent aComp = aInfo.getHidingDialogComponent();
			if (aComp != null) {
				hidingDialogComponents.add(aComp);
			}
		}
		if (hidingDialogComponents.size() == 1) {
			hidingComponent = hidingDialogComponents.get(0).getComponent();
		}
		else if (hidingDialogComponents.size() > 1) {
			tabPane=new CompoundHidingTabPane();
			tabPane.setOpaque(false);
			hidingComponent = tabPane;
			int n=0;
			for (HidingDialogComponent aComponent:hidingDialogComponents) {
//				System.out.println("CompoundHidingPanel: panels added "+aComponent.getName()+ " component: "+aComponent.getComponent());
				tabPane.addTab(aComponent.getName(), aComponent.getIcon(), aComponent.getComponent(), false);
				tabPane.addTabChangeListener(new TabListener());
				tabPane.getSettingsButton(n).addActionListener(new SettingsListener(aComponent));
				n++;
			}
			System.out.println("No. of components for tabbed pane: "+n);
			hidingComponent = tabPane;
		}		
	}

	@Override
	public JComponent getComponent() {
		return hidingComponent;
	}

	@Override
	public boolean canHide() {
		for (HidingDialogComponent aComponent:hidingDialogComponents) {
			if (aComponent.canHide() == false) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void showComponent(boolean visible) {
		for (HidingDialogComponent aComponent:hidingDialogComponents) {
			aComponent.showComponent(visible);
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#hasMore()
	 */
	@Override
	public boolean hasMore() {
		if ((hidingDialogComponents.size() == 1) && hidingDialogComponents.get(0).hasMore()) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#showMore(PamView.hidingpanel.HidingDialog)
	 */
	@Override
	public boolean showMore(HidingDialog hidingDialog) {
		if (hidingDialogComponents.size() == 1) {
			return hidingDialogComponents.get(0).showMore(hidingDialog);
		}
		else if (tabPane != null) {
			int tab = tabPane.getSelectedIndex();
			if (tab >= 0 && tab < hidingDialogComponents.size()) {
				return hidingDialogComponents.get(tab).showMore(hidingDialog);
			}
		}
		return false;
	}
	
	class SettingsListener implements ActionListener{
		
		private HidingDialogComponent aComponent;

		public SettingsListener(HidingDialogComponent aComponent){
			this.aComponent=aComponent;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			aComponent.showMore(hidingDialogPanel.getHidingDialog());
		}
		
	}
	
	class TabListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			int tab = tabPane.getSelectedIndex();
			if (tab >= 0 && tab < hidingDialogComponents.size()) {
				boolean enable = hidingDialogComponents.get(tab).hasMore();
				// need to somehow get this through to the dialog to diabel the tab
			}
			//if we change panel then change the size of the hiding dialog and reposition correctly
			if (hidingDialogPanel.getHidingDialog()!=null ){
				hidingDialogPanel.getHidingDialog().pack();
				hidingDialogPanel.setDialogPosition();
			}
		}
		
	}

	public void setHidingDialogPanel(HidingDialogPanel hidingDialogPanel) {
		this.hidingDialogPanel=hidingDialogPanel;
	}

}
