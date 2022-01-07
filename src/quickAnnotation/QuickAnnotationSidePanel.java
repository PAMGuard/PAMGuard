package quickAnnotation;

import generalDatabase.lookupTables.LookupItem;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
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
public class QuickAnnotationSidePanel implements PamSidePanel {

	QuickAnnotationModule quickAnnotationControl;

	PamPanel sidePanel;

	PamCheckBox assignClassification, exportWav, showDialog;

	DefaultListModel<LookupItem> defaultListModel;
	PamList labelList;
	private final Object defaultItem= new Object(){
		public String toString() {
			return "";
		};
	};

	private PamScrollPane scrollPane;
		
	public QuickAnnotationSidePanel(QuickAnnotationModule qac){
		this.quickAnnotationControl = qac;
		sidePanel = new PamPanel();
		sidePanel.setBorder(new TitledBorder("Quick Annotation"));
		sidePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridy = 0;
		c.weightx = 1;

		defaultListModel = new DefaultListModel<LookupItem>();
		labelList = new PamList(defaultListModel)     {
            public String getToolTipText( MouseEvent e )
            {
                int row = locationToIndex( e.getPoint() );
                Object o = getModel().getElementAt(row);
                return o.toString();
            }

            public Point getToolTipLocation(MouseEvent e)
            {
                int row = locationToIndex( e.getPoint() );
                Rectangle r = getCellBounds(row, row);
                return new Point(r.width, r.y);
            }
        };
		scrollPane = new PamScrollPane(labelList);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		labelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sidePanel.add(scrollPane,c);
		c.gridy++;
		
		updateAnnotationSelector();

		labelList.setVisibleRowCount(Math.min(10, labelList.getModel().getSize()));
		int width = Math.min(labelList.getWidth(), 200);
		int height = (int) Math.min(200, labelList.getPreferredScrollableViewportSize().getHeight());
		labelList.setPreferredSize(new Dimension(width,height));
		
		Object selectedParam = quickAnnotationControl.getQuickAnnotationParameters().selectedClassification;
		selectedParam = (selectedParam == null) ? defaultItem : selectedParam; 
		labelList.setSelectedValue(selectedParam, true);
		assignClassification = new PamCheckBox("Assign Label");
		sidePanel.add(assignClassification,c);
		assignClassification.setSelected(quickAnnotationControl.getQuickAnnotationParameters().assignLabels);
		c.gridy++;
		
		showDialog = new PamCheckBox("Edit new");
		sidePanel.add(showDialog,c);
		showDialog.setSelected(quickAnnotationControl.getQuickAnnotationParameters().shouldPopupDialog);
		c.gridy++;

		exportWav = new PamCheckBox("Wav export");
		sidePanel.add(exportWav,c);
		exportWav.setSelected(quickAnnotationControl.getQuickAnnotationParameters().exportClips);
		c.gridy++;
		
		assignClassification.addActionListener(new ToggleControl());
		showDialog.addActionListener(new ToggleControl());
		exportWav.addActionListener(new ToggleControl());
		
		labelList.addListSelectionListener(new SelectedLabel());

		enableControls();
	}
	
	
	class ToggleControl implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	
	private void enableControls(){
		labelList.setEnabled(assignClassification.isSelected());
		updateSelection();
		quickAnnotationControl.getQuickAnnotationParameters().assignLabels = assignClassification.isSelected();
		quickAnnotationControl.getQuickAnnotationParameters().shouldPopupDialog = showDialog.isSelected();
		quickAnnotationControl.getQuickAnnotationParameters().exportClips = exportWav.isSelected();
	}
	
	class SelectedLabel implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent e) {
			updateSelection();
		}
	}
	
	private void updateSelection(){
		LookupItem li = (LookupItem) labelList.getSelectedValue();
		if (li == null || li==defaultItem || !labelList.isEnabled())
			quickAnnotationControl.getQuickAnnotationParameters().selectedClassification = null;
		else
			quickAnnotationControl.getQuickAnnotationParameters().selectedClassification = 
					(LookupItem) labelList.getSelectedValue();		
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
		return labelList;
	}

	public void updateAnnotationSelector(){
		Vector<LookupItem> newList = quickAnnotationControl.getQuickAnnotationParameters().quickList.getSelectedList();
		defaultListModel.removeAllElements();
		for (LookupItem li:newList){
			defaultListModel.addElement(li);
		}
		labelList.setVisibleRowCount(newList.size());
		int height = (int) labelList.getPreferredScrollableViewportSize().getHeight();
		int width = Math.min(scrollPane.getWidth(), 200);
		height = Math.max(20, height);
		scrollPane.setPreferredSize(new Dimension(width, height));
		scrollPane.getParent().revalidate();

	}
}