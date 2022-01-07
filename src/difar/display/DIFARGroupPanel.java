package difar.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import PamView.PamColors.PamColor;
import PamView.PamList;
import PamView.dialog.PamButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;
import PamView.PopupTextField;
import PamView.component.PamScrollPane;
import difar.DIFARMessage;
import difar.DifarControl;
import difar.DifarParameters;

public class DIFARGroupPanel extends PamPanel implements DIFARDisplayUnit {

	private DifarControl difarControl;
	
	private PamPanel controlPanel;

	private JList trackedGroup;
	private PamButton addGroup;
	private PamButton removeGroup;
	private GroupNameDialog groupNameDialog;

	private DefaultListModel<String> listModel;
	
	
	public DIFARGroupPanel(DifarControl difarControl){

		
			super();
			this.difarControl = difarControl; 
			
			controlPanel = new PamPanel(new GridBagLayout());
			controlPanel.setBorder(new TitledBorder("Groups"));
			GridBagConstraints c = new GridBagConstraints();
			
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.gridx = 0;
			c.gridy = 0;
			controlPanel.add(addGroup = new PamButton("Add group"),c);
			c.gridy++;
			controlPanel.add(removeGroup = new PamButton("Remove group"),c);
			c.gridy++;
			int n = difarControl.getDifarParameters().getTrackedGroupList().size();
			String [] groupList = difarControl.getDifarParameters().getTrackedGroupList().toArray(new String[n]);
			
			listModel = new DefaultListModel();
			for (int i = 0; i< difarControl.getDifarParameters().getTrackedGroupList().size(); i++){
				listModel.addElement(difarControl.getDifarParameters().getTrackedGroupList().get(i));
			}
			trackedGroup = new PamList(listModel);
			PamScrollPane listScroller = new PamScrollPane(trackedGroup);
			listScroller.setPreferredSize(new Dimension(20, 150));
			controlPanel.add(listScroller, c);
			
			groupNameDialog = new GroupNameDialog(null, "Add group name: ", true);
			addGroup.addActionListener(new AddGroup());
			trackedGroup.addListSelectionListener(new SelectGroup());
			removeGroup.addActionListener(new RemoveGroup());
			
			// Set a tooltip for each list item, rather than a single tooltip for the whole JList
			trackedGroup.addMouseMotionListener(new MouseMotionListener() {
				@Override
				public void mouseDragged(MouseEvent e) {}

				@Override
				public void mouseMoved(MouseEvent e) {
					JList l = (JList) e.getSource();
					ListModel m = l.getModel();
					int index = l.locationToIndex(e.getPoint());
					if (index > -1) {
						String groupName =m.getElementAt(index).toString();
						l.setToolTipText(getGroupSummary(groupName));
						if (groupName.equals(DifarParameters.DefaultGroup))
							l.setToolTipText(null);
					}
				}
			});
			
	}
	
	public String getGroupSummary(String groupName){
		return difarControl.getTrackedGroupProcess().getTrackedGroups().getGroupSummary(groupName);
	}
	
	public String getName() {
		return "Difar Group Panel";
	}
	public Component getComponent() {
		return controlPanel;
	}
	
	public int difarNotification(DIFARMessage difarMessage) {
		switch(difarMessage.message) {
		case DIFARMessage.DemuxComplete:
			difarMessage.difarDataUnit.setTempGroup((String) trackedGroup.getSelectedValue());
			break;
		}
		return 0;
	}

	public String getCurrentlySelectedGroup(){
		return (String) trackedGroup.getSelectedValue();
	}

	public void setCurrentlySelectedGroup(String groupName) {
		trackedGroup.setSelectedValue(groupName, true);
	}
	
	public boolean isTrackedGroupSelectable(String groupName){
		String originalSelection = (String) trackedGroup.getSelectedValue();
		trackedGroup.setSelectedValue(groupName, false);
		String selectedGroup = (String) trackedGroup.getSelectedValue();
		trackedGroup.setSelectedValue(originalSelection, false);
		if (!selectedGroup.equals(groupName)) {
			return false;
		}
		return true;
		
	}
	
	
	class SelectGroup implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (difarControl.getCurrentDemuxedUnit() != null){
				difarControl.getCurrentDemuxedUnit().setTempGroup((String) trackedGroup.getSelectedValue());
			}
		
		}
	}
	
	class AddGroup implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			groupNameDialog.showDialog();
			updateGroupsCombo();
			trackedGroup.setSelectedIndex(difarControl.getDifarParameters().getTrackedGroupList().size()-1);
		}
	}

	class RemoveGroup implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			difarControl.getDifarParameters().removeTrackedGroup((String) trackedGroup.getSelectedValue());
			updateGroupsCombo();
		}
	}
	
	void updateGroupsCombo(){
		Object item = trackedGroup.getSelectedValue();
		String groupName;
		trackedGroup.removeAll();
		if (difarControl.getDifarParameters().getTrackedGroupList() == null) return;
		listModel = new DefaultListModel<>();
		for (int i = 0; i< difarControl.getDifarParameters().getTrackedGroupList().size(); i++){
			groupName = difarControl.getDifarParameters().getTrackedGroupList().get(i);
			listModel.addElement(groupName);
		}
		trackedGroup.setModel(listModel);
		trackedGroup.setSelectedValue(item,true);
	}
	
	private class GroupNameDialog extends PamDialog {

		JTextField textField;
		
		public GroupNameDialog(Window parentFrame, String title,
				boolean hasDefault) {
			super(parentFrame, title, hasDefault);
			PamPanel mainPanel = new PamPanel();
			textField = new JTextField(80);
			textField.setEditable(true);
			mainPanel.add(textField);
			setDialogComponent(mainPanel);

		}

		public void showDialog(){
			textField.setText("");
			setVisible(true);
		}
		@Override
		public boolean getParams() {
			String groupName = textField.getText();
			if (groupName == null)
				return false;
			difarControl.getDifarParameters().addTrackedGroup(groupName);
			listModel.addElement(groupName);
			trackedGroup.setSelectedValue(groupName, true);
			return true;
		}

		@Override
		public void cancelButtonPressed() {

		}

		@Override
		public void restoreDefaultSettings() {

			
		}
		
	}
	
//	/** 

//	 * @author Brian Miller
//	 *
//	 */
//	class TTList extends JList<String>
//	{
//	   TTList (String [] data, String [] tips)
//	   {
//	      // Initialize the JList layer of the TTList object.
//	      super ();
//
//
//
//	      // If the TTList displays a ToolTip, we cannot assign individual
//	      // ToolTips to TTList items.  Therefore, "" is passed to
//	      // setToolTipText to cancel the default ToolTip.
//
//	      setToolTipText ("");
//	   }
//
//	   public TTList(DefaultListModel<String> listModel) {
//		   super(listModel);
//		   // Assign a new ListModel to the TTList.
//		   setListData (createLinks(listModel.toArray()));
//		   setToolTipText("");
//		// TODO Auto-generated constructor stub
//	}
//
//	public String getToolTipText (MouseEvent e)
//	   {
//	      // Convert the mouse coordinates where the left mouse button was
//	      // pressed to a TTList item index.
//
//	      int index = locationToIndex (e.getPoint ());
//
//	      // If the left mouse button was clicked and the mouse cursor was
//	      // not over a list item, index is set to -1.
//
//	      if (index > -1)
//	      {
//	          // Extract the ListModel.
//	          ListModel lm = (ListModel) getModel();
//
//	          // Get the ToolTipLink associated with the TTList item
//	          // index.
//	          ToolTipLink link = (ToolTipLink) lm.getElementAt(index);
//
//	          // Return the ToolTipLink's ToolTip text.
//
//	          return link.getToolTipText ();
//	      }
//	      else
//	          return null;
//	   }
//
//	   private Object [] createLinks (String [] data)
//	   {
//	      // Create an array of references to ToolTipLink objects -- one
//	      // reference per TTList item.
//
//	      ToolTipLink [] links = new ToolTipLink [data.length];
//
//	      // For each TTList item, create a ToolTipLink object and store
//	      // its reference in the previously created array.
//
//	      for (int i = 0; i < data.length; i++)
//	           links [i] = new ToolTipLink (data [i], tips [i]);
//
//	      return links;
//	   }
//
//	   // The following class maintains an association between a TTList
//	   // item name and its associated tipText.
//	   private class ToolTipLink
//	   {
//	      private String name;
//	      private String tipText;
//
//	      public ToolTipLink (String name)
//	      {
//	        this.name = name;
//	        this.tipText = difarControl.getTrackedGroupProcess().getTrackedGroups().getCombinedGroupSummary(name);
//	      }
//
//	      public String getToolTipText ()
//	      {
//	         return tipText;
//	      }
//
//	      // toString is called behind the scenes by JList's UI to extract
//	      // the TTList name for display.
//
//	      public String toString ()
//	      {
//	         return name;
//	      }
//	   }
//	}
	
}
