package difar.display;

import generalDatabase.lookupTables.LookupItem;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import difar.DifarControl;
import difar.DifarParameters;
import PamView.PamList;
import PamView.PamSidePanel;
import PamView.dialog.PamCheckBox;
import PamView.panel.PamPanel;
import PamView.panel.PamScrollPane;

/**
 * The DIFAR SidePanel contains the list of DIFAR classifications, to allow
 * the user to choose which classification is assigned by default.
 * @author Brian Miller
 *
 */
public class DifarSidePanel implements PamSidePanel {

	DifarControl difarControl;

	PamPanel sidePanel;

	PamCheckBox assignClassification, multiChannel;

	DefaultListModel<LookupItem> defaultListModel;
	PamList defaultClassList;
	private final Object defaultItem= new Object(){
		public String toString() {
			return DifarParameters.Default;
		};
	};
		
	public DifarSidePanel(DifarControl dc){
		this.difarControl = dc;
		sidePanel = new PamPanel();
		sidePanel.setBorder(new TitledBorder("DIFAR Selection"));
		sidePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridy = 0;
		c.weightx = 1;
//		sidePanel.setPreferredSize(new Dimension(0,250));

		defaultListModel = new DefaultListModel<LookupItem>();
		defaultClassList = new PamList(defaultListModel);
		PamScrollPane scrollPane = new PamScrollPane(defaultClassList);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
//		scrollPane.setMaximumSize(new Dimension(400, 400));
//		scrollPane.setPreferredSize(new Dimension(0,200));
		defaultClassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sidePanel.add(scrollPane,c);
		c.gridy++;
		
		updateDifarDefaultSelector();

		defaultClassList.setVisibleRowCount(defaultClassList.getModel().getSize());
		int width = Math.min(defaultClassList.getWidth(), 200);
		int height = (int) Math.min(800, defaultClassList.getPreferredScrollableViewportSize().getHeight());
		defaultClassList.setPreferredSize(new Dimension(width,height));
		
		Object selectedParam = difarControl.getDifarParameters().selectedClassification;
		selectedParam = (selectedParam == null) ? defaultItem : selectedParam; 
		defaultClassList.setSelectedValue(selectedParam, true);
		assignClassification = new PamCheckBox("Assign Classification");
		sidePanel.add(assignClassification,c);
		c.gridy++;
		
		multiChannel = new PamCheckBox("Multi-Channel");
		multiChannel.setHorizontalTextPosition(SwingConstants.RIGHT);
		multiChannel.setSelected(difarControl.getDifarParameters().multiChannelClips);
		sidePanel.add(multiChannel,c);

		assignClassification.addActionListener(new ClassificationToggle());
		assignClassification.setSelected(difarControl.getDifarParameters().assignClassification);
		
		defaultClassList.addListSelectionListener(new SelectedClassification());
		multiChannel.addActionListener(new MultiChannelToggle());
		assignClassification.addActionListener(new AssignClassificationToggle());
		enableControls();
	}
	
	class ClassificationToggle implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	
	private void enableControls(){
		defaultClassList.setEnabled(assignClassification.isSelected());
		updateSelection();
	}
	
	class AssignClassificationToggle implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			difarControl.getDifarParameters().assignClassification
					=!difarControl.getDifarParameters().assignClassification;
		}
	}
	
	class MultiChannelToggle implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			difarControl.getDifarParameters().multiChannelClips
					=!difarControl.getDifarParameters().multiChannelClips;
		}
	}
	
	class SelectedClassification implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent e) {
			updateSelection();
		}
	}
	
	private void updateSelection(){
		LookupItem li = (LookupItem) defaultClassList.getSelectedValue();
		if (li == null || li==defaultItem || !defaultClassList.isEnabled())
			difarControl.getDifarParameters().selectedClassification = null;
		else
			difarControl.getDifarParameters().selectedClassification = 
					(LookupItem) defaultClassList.getSelectedValue();		
	}
	
	@Override
	public JComponent getPanel() {
		return sidePanel;
	}

	@Override
	public void rename(String newName) {
		sidePanel.repaint();
	}
	
	public JList<?> getSpeciesSelector(){
		return defaultClassList;
	}

	public void updateDifarDefaultSelector(){
		Vector<LookupItem> newList = difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList();
		defaultListModel.removeAllElements();
		for (LookupItem li:newList){
			defaultListModel.addElement(li);
		}
		defaultClassList.setVisibleRowCount(difarControl.getDifarParameters().getSpeciesList(difarControl).getSelectedList().size());
		defaultClassList.revalidate();
//		defaultListModel.add(0, defaultItem);
	}
}