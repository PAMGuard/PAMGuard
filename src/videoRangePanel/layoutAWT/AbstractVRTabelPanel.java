package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

/**
 * Use this for various tables we will be making in this dailog box
 * @author Jamie Macaulay
 *
 */
class AbstractVRTabelPanel extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JTable list;
	protected JButton deleteButton, addButton, editbutton;
	protected JScrollPane scrollPane;
	protected JPanel buttonPanel;

	public AbstractVRTabelPanel(){
		super();
	}
	
	public void createPanel(AbstractTableModel table){
//		setBorder(new TitledBorder("Calibration"));
		setLayout(new BorderLayout());
		list = new JTable(table);
		list.setRowSelectionAllowed(true);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(list);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(1, 130));
		add(BorderLayout.CENTER, scrollPane);
			
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(addButton = new JButton("Add"));
		buttonPanel.add(editbutton = new JButton("Edit"));
		buttonPanel.add(deleteButton = new JButton("Delete"));
		this.add(BorderLayout.SOUTH, buttonPanel);
	}
	
}