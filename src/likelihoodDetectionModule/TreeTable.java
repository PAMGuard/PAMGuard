package likelihoodDetectionModule;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Graphics;
import java.text.DecimalFormat;
import javax.swing.text.NumberFormatter;
import javax.swing.JFormattedTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import javax.swing.event.CellEditorListener;
import java.util.StringTokenizer;
import javax.swing.event.TreeExpansionListener;

/**
 * The TreeTable class provides a widget that is not native to the standard
 * Swing UI library - the Tree Table. The realization of a tree-table widget
 * is a JTable with a JTree inside of it. To the user, it looks like a multi-
 * columned JTree.
 *  <br>
 *  <br>
 *  Due to the complexity of the Swing interfaces, the TreeTable is not a 
 *  straightforward implementation. It requires a JTable to have a custom
 *  cell renderer that is, in fact, a JTree. The JTree itself also has 
 *  custom renderers to handle the display of the checkboxed nodes that
 *  typically are available on tree-table implementations. All of this needs
 *  to be backed with a back-end model representation of the likelihood
 *  detection module parameters, which completes the Swing's MVC 
 *  implementation. 
 *  <br><br>
 *  The TreeTable class is not meant to be a general-purpose component. In
 *  many ways it is written to be very specific to the job at hand. A more
 *  reusable TreeTable implementation would have been outside the effort
 *  available at the time of writing.
 */ 

public class TreeTable extends JTable {
	
	/** The Constant serialVersionUID required to make the TreeTable serializable
	 * which was necessary due to the inheritance hierarchy.
	 */
	static final long serialVersionUID = 3247234;

	/** The tree table's cell custom cell renderer. */
	protected TreeTableCellRenderer tree;
	
	/** The tree table's back-end model adapter. 
	 * @see TreeTableModelAdapter
	 */
	private TreeTableModelAdapter model;
	
	/** The custom tree table cell editor.
	 */
	private TreeTableCellEditor treeTableCellEditor;
	
	/** The popup menu that is displayed when the user
	 * right-clicks inside the tree table. */
	private JPopupMenu popupMenu;
	
	/** The number formatter used within the tree table to
	 * format the real numbers. */
	private NumberFormatter numberFormatter;
	
	/**
	 * Instantiates a new tree table.
	 * 
	 * @param treeTableModel the tree table model
	 * @see TreeTableModel
	 */
	public TreeTable(TreeTableModel treeTableModel) {
		super();

		this.setBackground( Color.white );
		
		// This is required for the tree table to automatically
		// fill the entire JPanel that it is placed in. Regardless
		// of what layout is used.
		this.setFillsViewportHeight( true );
		
		// Configure the formatter for the display of real numbers.
		DecimalFormat format = new DecimalFormat( "#######0.0####" );
		format.setMaximumFractionDigits( 5 );
		format.setMinimumFractionDigits( 1 );
		format.setMaximumIntegerDigits( 5 );
		format.setMinimumIntegerDigits( 1 );

		numberFormatter = new NumberFormatter( format  );
		
		// The formatter should not allow the user to enter an
		// invalid value.
		numberFormatter.setAllowsInvalid( false );

		// Create cell renderer for the tree table.
		tree = new TreeTableCellRenderer(treeTableModel);
		
		// An expansion listener will be required to store the expansion
		// state of the tree between views and between program executions
		// via the LikelihoodDetectionParameters object.
		tree.addTreeExpansionListener( new TreeTableExpansionListener() );
		for ( int i = tree.getRowCount(); i > 0; --i ) tree.expandRow(i);

		// The JTable uses a DefaultTableModel as the back-end representation
		// of the data. However, since the data is actually being rendered in
		// the cells using a JTree, a model adapter is used to wrap the
		// model and convert requests from the JTable DefaultTableModel
		// interface into a JTree DefaultTreeModel representation.
		model = new TreeTableModelAdapter( treeTableModel, tree );

		// Install the tableModel representing the visible rows in the tree table. 
		super.setModel( model );

		// Force the JTable and JTree to share their row selection models. 
		ListToTreeSelectionModelWrapper selectionWrapper = new 
		ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());

		// Install the the tree table cell renderer as the default renderer.
		setDefaultRenderer(TreeTableModel.class, tree); 

		// Create the tree table cell editor, then install it as the
		// default renderer for the tree table.
		treeTableCellEditor = new TreeTableCellEditor();
		setDefaultEditor(TreeTableModel.class, treeTableCellEditor );

		// Do not allow the user to reorder the columns. This would be 
		// very bad.
		this.getTableHeader().setReorderingAllowed( false );

		// Don't show a grid and don't have any spacing between the cells
		// of the tree table. It looks crappy.
		setShowGrid( false );
		setIntercellSpacing( new Dimension(0, 0) );    

		// Make sure that the height of the trees row to match that of the table.
		if (tree.getRowHeight() < 1) {
			// Apparently, the metal theme looks better when this is done.
			setRowHeight(18);
		}
		else {
			setRowHeight( tree.getRowHeight() + 5 );
		} 

		// Create the popup menu and install a TreeMouseListener on the
		// TreeTable to pick up clicks from the user.
		popupMenu = new JPopupMenu();
		this.addMouseListener( new TreeMouseListener() );
	}
    
	/* (non-Javadoc)
	 * @see javax.swing.JTable#setValueAt(java.lang.Object, int, int)
	 * This method is called automatically by the TreeTable when the user finishes
	 * editing a cell and the view commits the value back to the model. 
	 */
	@Override
	public void setValueAt( Object value, int row, int column ) {
		
		// Swing will reset the sizes of the columns after this method is
		// finished, so the column sizes are preserved here and then
		// restored after the call to super.setValueAt()
		
		int width0 = -1;
		int width1 = -1;
		if ( this.columnModel.getColumnCount() == 2 ) {
			width0 = this.columnModel.getColumn(0).getWidth();
			width1 = this.columnModel.getColumn(1).getWidth();
		}
		
		super.setValueAt(value, row, column);	
		
		if ( width0 > -1 ) {
			this.columnModel.getColumn(0).setPreferredWidth(width0);
			this.columnModel.getColumn(1).setPreferredWidth(width1);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JTable#columnMarginChanged(javax.swing.event.ChangeEvent)
	 * This method is called automatically from the default JTableHeader when a column
	 * size (width) is changed. 
	 */
	@Override
	public void columnMarginChanged( ChangeEvent e ) {
		super.columnMarginChanged( e );
		
		// Preserve the user's resize values withing the back-end model. This way
		// they can be gathered up by the controlling unit and preserved for future
		// reference.
		if ( e.getSource().getClass() == DefaultTableColumnModel.class ) {
			DefaultTableColumnModel columnModel = (DefaultTableColumnModel)e.getSource();
			ConfigurationDialogSettings settings = new ConfigurationDialogSettings();
			settings.expandedState = (model).getConfigurationDialogSettings().expandedState;
			settings.firstColumnWidth = columnModel.getColumn( 0 ).getWidth();
			settings.secondColumnWidth = columnModel.getColumn( 1 ).getWidth();	
			(model).setConfigurationDialogSettings( settings );
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
	 * This method is called automatically.
	 */
	@Override
	public Component prepareRenderer( TableCellRenderer renderer, int row, int col ) {
		Component c = super.prepareRenderer( renderer, row, col );
		return c;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
	 * This method is called automatically by the TreeTable implementation when the
	 * mouse remains unmoved over a table cell for a configured amount of time. The
	 * TreeTable checks the position of the mouse relative to the tree renderer's
	 * contents, and if necessary, returns a tool tip to be displayed.
	 * 
	 * Note the tool tip is not display while editing a table cell.
	 * 
	 */
	@Override
	public String getToolTipText( MouseEvent e ) {
		// A return value of null tells the underlying Swing libraries that
		// no tool tip should be displayed.
		String tip = null;
		
		// Get the point at which the mouse is.
		java.awt.Point p = e.getPoint();
		
		// Determine which row and column of the table the mouse is on.
		int row = rowAtPoint( p );
		int col = columnAtPoint( p );
		
		// Convert the column index returned to a column value that is 
		// relative to the backend model.
		int realcol = convertColumnIndexToModel( col );
		
		// Based on the row, get the model object that it represents.
		Object obj = model.nodeForRow( row );
		
		// If there was no object in the row, then it means the mouse is
		// over an area of the tree table that doesn't have anything in it.
		if ( obj == null ) return null; 
		
		// The only entity in the first column of the tree table that has a
		// tool tip is the checkbox node (target node), and even then a tip is
		// only displayed when a certain error condition exists.
		if ( realcol == 0 ) {
			if ( obj.getClass() == TargetNode.class ) {
				TargetNode node = (TargetNode)obj;
				if ( node.isError() && node.numberSignalBands() == 0 ) {
					tip = "Target configuration must have at least one signal band defined.";	
					return tip;
				}
			}
			else {
				return tip;
			}
		}

		// Note that there is no check for column 1 here, since there is only ever two columns
		// in the table - if it wasn't 0 (above) then it's obviously 1.
		
		// For the text in the tool tip, a decimal (point) formatter is required.
		DecimalFormat df = new DecimalFormat( "#########0.0###" );
		df.setDecimalSeparatorAlwaysShown( true );
		
		// Perform a run-time comparison of the object (node) type and determine which part of
		// the model it represents. For an appropriate part of the model, display a tool tip.
		
		if ( obj.getClass() == FrequencyResolutionNode.class ) {
			FrequencyResolutionNode node = (FrequencyResolutionNode)obj;
			tip = "Value between " + new Double( df.format( node.getLimits()[0] ) ).doubleValue() + " and " + new Double( df.format( node.getLimits()[1] ) ).doubleValue();
		}
		else if ( obj.getClass() == TimeResolutionNode.class ) {
			TimeResolutionNode node = (TimeResolutionNode)obj;
			tip = "Value between " + df.format( node.getLimits()[0] ) + " and " + df.format( node.getLimits()[1] );
		}
		else if ( obj.getClass() == GuardBandThresholdNode.class ) {
			GuardBandThresholdNode node = (GuardBandThresholdNode)obj;
			tip = "Value between " + df.format( node.getLimits()[0] ) + " and " + df.format(node.getLimits()[1]);
		}
		else if ( obj.getClass() == SnrThresholdNode.class ) {
			SnrThresholdNode node = (SnrThresholdNode) obj;
			tip = "Value between " + df.format( node.getLimits()[0] ) + " and " + df.format( node.getLimits()[1] );
		}
		else if ( obj.getClass() == StartFrequencyNode.class ) {
			StartFrequencyNode node = (StartFrequencyNode)obj;
			tip = "Value between " + df.format( node.getLimits()[0] ) + " and End Frequency (" + df.format( node.getLimits()[1] ) + ")";
		}
		else if ( obj.getClass() == EndFrequencyNode.class ) {
			EndFrequencyNode node = (EndFrequencyNode)obj;
			tip = "Value between Start Frequency (" + df.format( node.getLimits()[0] ) + ") and " + df.format( node.getLimits()[1] );
		}
		else if ( obj.getClass() == BackgroundNode.class ) {
			BackgroundNode node = (BackgroundNode)obj;
			SignalNode snode = (SignalNode)model.nodeForRow( row+1 );
			tip = "Value between Signal Window (" + df.format( snode.getSignalSecods() ) + ") and " + df.format( node.getLimits()[1] );
		}
		else if ( obj.getClass() == SignalNode.class ) {
			SignalNode node = (SignalNode)obj;
			tip = "Value greater than " + df.format( node.getLimits()[0] ) + " and less than or equal to Noise Window (" + df.format( node.getLimits()[1] ) + ")";
		}
		else if ( obj.getClass() == SecondsBetweenDetectionsNode.class ) {
			SecondsBetweenDetectionsNode node = (SecondsBetweenDetectionsNode)obj;
			tip = "Value between " + df.format( node.getLimits()[0] ) + " and " + df.format( node.getLimits()[1] );
		}
		
		return tip;
	}
	
	/**
	 * Overridden to send a message to the super class then and forward the 
	 * call to the internal tree renderer.
	 * Since the tree is not actually in the component hierarchy of the JTable (TreeTable) it will
	 * never receive this unless we forward it in this manner.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if ( tree != null ) tree.updateUI();

		// Use the tree's default foreground and background colors in the table. 
		LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font" );
	}

	/* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
	 * paint the editor. The UI currently uses different techniques to 
	 * paint the renderers and editors and overriding setBounds() below 
	 * is not the right thing to do for an editor. Returning -1 for the 
	 * editing row in this case, ensures the editor is never painted. 
	 */
	/* (non-Javadoc)
	 * @see javax.swing.JTable#getEditingRow()
	 */
	@Override
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 :
			editingRow;  
	}

	/**
	 * Overridden to pass the new rowHeight to the tree.
	 * 
	 * @param rowHeight the row height
	 */
	@Override
	public void setRowHeight(int rowHeight) { 
		super.setRowHeight(rowHeight); 
		if (tree != null && tree.getRowHeight() != rowHeight) {
			tree.setRowHeight(getRowHeight()); 
		}
	}

	/**
	 * Gets the tree.
	 * 
	 * @return the tree
	 */
	public JTree getTree() {
		return tree;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#getCellRenderer(int, int)
	 * 
	 * The implementation of this method checks which type of node
	 * is represented by the row and column for which the JTable (TreeTable)
	 * has requested an editor for and decides whether any custom formatting
	 * is necessary.
	 */
	@Override
	public TableCellRenderer getCellRenderer( int row, int column ) {
		// It isn't necessary to return a customer renderer for any row and
		// column that is outside the area shown by the internal JTree. This
		// should never be requested, but just in case we check that the
		// renderer returned is not a default one.
		TableCellRenderer r = super.getCellRenderer( row, column );
		if ( r.getClass() == TreeTableCellRenderer.class ) return r;
		
		// Nothing is editable in column one.
		if ( column == 0 ) return r;
		
		// Get the backend model object that is represented by the row in
		// the tree table.
		Object obj = model.nodeForRow(row);
		
		// Change the background colour of the editor to white.
		DefaultTableCellRenderer	d = (DefaultTableCellRenderer)r;
		d.setBackground( Color.white );
		
		// Now, based on the type of node it is, check to see whether there is any
		// special formatting needed for the row. This is typically a red background
		// for entities that have errors or a yellow background for those that
		// have warning-type conditions.
		
		if ( obj.getClass() == FrequencyResolutionNode.class ) {
			if ( ((FrequencyResolutionNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == TimeResolutionNode.class ) {
			if ( ((TimeResolutionNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == GuardBandThresholdNode.class ) {
			if ( ((GuardBandThresholdNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == SnrThresholdNode.class ) {
			if ( ((SnrThresholdNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == StartFrequencyNode.class ) {
			if ( ((StartFrequencyNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == EndFrequencyNode.class ) {
			if ( ((EndFrequencyNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == BackgroundNode.class ) {
			if ( ((BackgroundNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
			else if ( ((BackgroundNode)obj).overWarnLimit() ) {
				d.setBackground( Color.yellow );	
			}
		}
		else if ( obj.getClass() == SignalNode.class ) {
			if ( ((SignalNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
			else if ( ((SignalNode)obj).overWarnLimit() ) {
				d.setBackground( Color.yellow );	
			}
		}
		else if ( obj.getClass() == SecondsBetweenDetectionsNode.class ) {
			if ( ((SecondsBetweenDetectionsNode)obj).isError() ) {
				d.setBackground( Color.pink );	
			}
		}
		else if ( obj.getClass() == AssociatedBandNode.class ) {
			if ( ((AssociatedBandNode)obj).getBandName().equals( "None" ) ) {
				d.setBackground( Color.yellow );
			}
		}
		
		return r;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#getCellEditor(int, int)
	 * 
	 * When the user double-clicks on a cell in the TreeTable, this method is automatically
	 * invoked. It will decide which custom Swing editor widget should be displayed in
	 * the cell based on the type of backend node that the row and column represent.
	 */
	@Override
	public TableCellEditor getCellEditor( int row, int column ) {
		
		// Nothing in column one is editable, so just return the editor for the
		// internal JTree (which doesn't allow editing.
		if ( column == 0 ) {
			return treeTableCellEditor;
		}

		// Get the backend object for this row.
		Object node = model.nodeForRow( row );

		// Determine the appropriate custom editor to return. If nothing is 
		// applicable, then return the internal JTree editor, which will do nothing.
		
		if ( node.getClass() == AlgorithmNode.class ) {
			return new AlgorithmComboBoxEditor( TargetConfiguration.getAlgorithmNames() );        
		}
		else if ( node.getClass() == AssociatedBandNode.class ) {
			AssociatedBandNode n = (AssociatedBandNode)node;
			String configName = model.getTargetConfigurationNameForAssociatedBandNode( n );
			Vector<String> names = model.getSignalBandNames( configName );
			names.add( "None" );
			return new AssociatedSignalBandEditor( names ); 
		}
		else if ( node.getClass() == FrequencyResolutionNode.class ) {
			FrequencyResolutionNode fnode = (FrequencyResolutionNode)node;
			return new DoubleEditor( fnode.getLimits()[0], fnode.getLimits()[1] );  
		}
		else if ( node.getClass() == TimeResolutionNode.class ) {
			TimeResolutionNode tnode = (TimeResolutionNode)node;
			DoubleEditor d = new DoubleEditor( tnode.getLimits()[0], tnode.getLimits()[1] );
			return d;
		}
		else if ( node.getClass() == SecondsBetweenDetectionsNode.class ) {
			SecondsBetweenDetectionsNode snode = (SecondsBetweenDetectionsNode)node;
			return new DoubleEditor( snode.getLimits()[0], snode.getLimits()[1] );
		}
		else if ( node.getClass() == GuardBandThresholdNode.class ) {
			GuardBandThresholdNode gnode = (GuardBandThresholdNode)node;
			return new DoubleEditor( gnode.getLimits()[0], gnode.getLimits()[1] ); 
		}
		else if ( node.getClass() == SnrThresholdNode.class ) {
			SnrThresholdNode snode = (SnrThresholdNode)node;
			return new DoubleEditor( snode.getLimits()[0], snode.getLimits()[1] ); 
		}
		else if ( node.getClass() == StartFrequencyNode.class ) {
			StartFrequencyNode fnode = (StartFrequencyNode)node;
			return new DoubleEditor( fnode.getLimits()[0], fnode.getLimits()[1] ); 
		}
		else if ( node.getClass() == EndFrequencyNode.class ) {
			EndFrequencyNode fnode = (EndFrequencyNode)node;
			return new DoubleEditor( fnode.getLimits()[0], fnode.getLimits()[1] ); 
		}
		else if ( node.getClass() == BackgroundNode.class ) {
			BackgroundNode bnode = (BackgroundNode)node;
			return new DoubleEditor( bnode.getLimits()[0], bnode.getLimits()[1] ); 
		}
		else if ( node.getClass() == SignalNode.class ) {
			SignalNode snode = (SignalNode)node;
			return new DoubleEditor( snode.getLimits()[0], snode.getLimits()[1] ); 
		}

		return treeTableCellEditor;

	}

	/**
	 * The Class AlgorithmComboBoxEditor is used by the TreeTable as the custom
	 * editor for the normalization algorithm rows.
	 */
	public class AlgorithmComboBoxEditor extends DefaultCellEditor {
		
		/** The Constant serialVersionUID. */
		static final long serialVersionUID = 23946;
		
		/**
		 * Instantiates a new algorithm combo box editor.
		 * 
		 * @param items the items
		 */
		public AlgorithmComboBoxEditor( String[] items ) {
			super( new JComboBox( items ) ); 
		}
	}

	/**
	 * The Class AssociatedSignalBandEditor is used by the TreeTable as the custom
	 * editor for the associated signal band row within a guard band.
	 */
	public class AssociatedSignalBandEditor extends DefaultCellEditor {
		
		/** The Constant serialVersionUID. */
		static final long serialVersionUID = 2903234;
		
		/**
		 * Instantiates a new associated signal band editor.
		 * 
		 * @param names the names
		 */
		public AssociatedSignalBandEditor( Vector<String> names ) {
			super( new JComboBox( names ) );
		}
	}

	/**
	 * The listener interface for receiving doubleCellEditor events.
	 * The class that is interested in processing a doubleCellEditor
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addDoubleCellEditorListener<code> method. When
	 * the doubleCellEditor event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 */
	public class DoubleCellEditorListener implements CellEditorListener {
		
		/* (non-Javadoc)
		 * @see javax.swing.event.CellEditorListener#editingStopped(javax.swing.event.ChangeEvent)
		 */
		public void editingStopped( ChangeEvent e ) {
			model.fireTableStructureChanged();
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.CellEditorListener#editingCanceled(javax.swing.event.ChangeEvent)
		 */
		public void editingCanceled( ChangeEvent e ) {
			tree.updateUI();	
		}
	}
	
	/**
	 * The Class DoubleEditor is used by the TreeTable for editing any value that has
	 * a real number representation. It allows the implementation to supply a min and
	 * max value for the number, and uses a custom JFormattedTextField component
	 * internally to do the validation of the user input.
	 */
	public class DoubleEditor extends DefaultCellEditor {
		
		/** The Constant serialVersionUID. */
		static final long serialVersionUID = 23476690;
		
		/** The min. */
		private double min;
		
		/** The max. */
		private double max;

		/** The component. */
		Component component;

		/**
		 * Instantiates a new double editor.
		 * 
		 * @param min the min
		 * @param max the max
		 */
		public DoubleEditor( double min, double max ) {
			super( new JFormattedTextField( numberFormatter ) );
			component = super.getComponent();
			this.min = min;
			this.max = max;
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected,
				int row, int column ) {
			((JFormattedTextField)component).setText( (String)value );
			return component;  
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#stopCellEditing()
		 */
		@Override
		public boolean stopCellEditing() {	
			double value = Double.parseDouble( (String)getCellEditorValue() );
			boolean shouldStop = ( value >= min && value <= max );
			if ( shouldStop ) {	
				return super.stopCellEditing();
			}

			((JFormattedTextField)component).setBorder( BorderFactory.createLineBorder(Color.red, ((JFormattedTextField)component).getInsets().left));
			
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return ((JFormattedTextField)component).getText(); 
		}
	}

	/**
	 * The class TreeTableCellRenderer is the main guts of the TreeTable's functionality
	 * that renderers the JTree within the first column of the JTable. 
	 * 
	 * @see JTree
	 * @see TableCellRenderer
	 */
	public class TreeTableCellRenderer extends JTree implements TableCellRenderer {
		
		/** The Constant serialVersionUID. */
		static final long serialVersionUID = 272362234;
		
		/** Last table/tree row asked to renderer. This is used to keep track
		 * of widget height and next-point-of-insert.
		 */
		protected int visibleRow;

		/**
		 * Instantiates a new tree table cell renderer.
		 * The actual model that will be used when this is instantiated is a 
		 * TreeTableModel.
		 * 
		 * @see TreeTableModel
		 * @param model the model
		 */
		public TreeTableCellRenderer(TreeModel model) {
			super( model ); 
			
			// The cells in this JTree need to be rendered in a custom manner, because 
			// Swing does not allow for checkboxes as nodes in a JTree by default. This
			// sets the default renderer for the JTree cells as a CheckRenderer.
			this.setCellRenderer( new CheckRenderer( model ) );
		}

		/**
		 * updateUI is overridden to set the colors of the Tree's renderer
		 * to match that of the table.
		 */
		@Override
		public void updateUI() {
			super.updateUI();
			// Make the tree's cell renderer use the table's cell selection
			// colors. 
			TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer) {
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr); 
				// For 1.1 uncomment this, 1.2 has a bug that will cause an
				// exception to be thrown if the border selection color is
				// null.
				// dtcr.setBorderSelectionColor(null);
				dtcr.setTextSelectionColor(UIManager.getColor
						("Table.selectionForeground"));
				dtcr.setBackgroundSelectionColor(UIManager.getColor
						("Table.selectionBackground"));
			}
		}

		/**
		 * Sets the row height of the tree, and forwards the row height to
		 * the table.
		 * 
		 * @param rowHeight the row height
		 */
		@Override
		public void setRowHeight(int rowHeight) { 
			if (rowHeight > 0) {
				super.setRowHeight(rowHeight); 
				if (TreeTable.this != null &&
						TreeTable.this.getRowHeight() != rowHeight) {
					TreeTable.this.setRowHeight(getRowHeight()); 
				}
			}
		}

		/**
		 * This is overridden to set the height to match that of the JTable.
		 * 
		 */
		@Override
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, TreeTable.this.getHeight());
		}

		/**
		 * Sublcassed to translate the graphics such that the last visible
		 * row will be drawn at 0,0.
		 * 
		 * @param g the g
		 */
		@Override
		public void paint(Graphics g) {
			g.translate(0, -visibleRow * getRowHeight());
			super.paint(g);
		}

		/**
		 * This returns the renderer to be used by the JTree view to draw
		 * the cells. For the TreeTable implementation, this will be a 
		 * CheckRenderer.
		 * 
		 * @see CheckRenderer
		 * 
		 * @param table the table
		 * @param value the value that will be renderered in the cell
		 * @param isSelected the is selected flag
		 * @param hasFocus the has focus flag
		 * @param row the row
		 * @param column the column
		 * 
		 * @return the table cell renderer component
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row, int column) {
			if(isSelected)
				setBackground(table.getSelectionBackground());
			else
				setBackground(table.getBackground());

			visibleRow = row;
			
			return this;
		}
		
		/**
		 * Sets the expanded state for a tree path to true.
		 * 
		 * @param p the new expanded
		 */
		void setExpanded( TreePath p ) {
			this.setExpandedState( p, true );
		}
	}


	/**
	 * TreeTableCellEditor implementation. Component returned is the
	 * JTree.
	 */
	public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent( JTable table,
				Object value, boolean isSelected, int row, int column ) {
			return tree;
		}

		// Intercept the mouse event. All keyboard events are passed on to
		// the table by returning false. By returning false, the policy that
		// the tree will never be editable is enforced. Mouse events in the
		// tree area are passed on to the tree.
		/* (non-Javadoc)
		 * @see likelihoodDetectionModule.AbstractCellEditor#isCellEditable(java.util.EventObject)
		 */
		@Override
		public boolean isCellEditable( EventObject e ) {
			if ( e instanceof MouseEvent ) {
				for ( int counter = getColumnCount() - 1; counter >= 0; counter--) {
					if ( getColumnClass(counter) == TreeTableModel.class ) {
						MouseEvent me = (MouseEvent)e;
						if ( me.getClickCount() > 1 ) break;
						MouseEvent newME = new MouseEvent( tree, me.getID(),
								me.getWhen(), me.getModifiers(),
								me.getX() - getCellRect(0, counter, true).x,
								me.getY(), me.getClickCount(),
								me.isPopupTrigger() );
						tree.dispatchEvent( newME );
						break;
					}
				}
			}
			return false;
		}
	}


	/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	 * to listen for changes in the ListSelectionModel it maintains. Once
	 * a change in the ListSelectionModel happens, the paths are updated
	 * in the DefaultTreeSelectionModel.
	 */
	class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel { 
		
		/** The Constant serialVersionUID. */
		static final long serialVersionUID = 66351412;
		
		/** Set to true when we are updating the ListSelectionModel. */
		protected boolean         updatingListSelectionModel;

		/**
		 * Instantiates a new list to tree selection model wrapper.
		 */
		public ListToTreeSelectionModelWrapper() {
			super();
			getListSelectionModel().addListSelectionListener
			(createListSelectionListener());
		}

		/**
		 * Returns the list selection model. ListToTreeSelectionModelWrapper
		 * listens for changes to this model and updates the selected paths
		 * accordingly.
		 * 
		 * @return the list selection model
		 */
		ListSelectionModel getListSelectionModel() {
			return listSelectionModel; 
		}

		/**
		 * This is overridden to set <code>updatingListSelectionModel</code>
		 * and message super. This is the only place DefaultTreeSelectionModel
		 * alters the ListSelectionModel.
		 */
		@Override
		public void resetRowSelection() {
			if(!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					super.resetRowSelection();
				}
				finally {
					updatingListSelectionModel = false;
				}
			}
			// Notice how we don't message super if
			// updatingListSelectionModel is true. If
			// updatingListSelectionModel is true, it implies the
			// ListSelectionModel has already been updated and the
			// paths are the only thing that needs to be updated.
		}

		/**
		 * Creates and returns an instance of ListSelectionHandler.
		 * 
		 * @return the list selection listener
		 */
		protected ListSelectionListener createListSelectionListener() {
			return new ListSelectionHandler();
		}

		/**
		 * If <code>updatingListSelectionModel</code> is false, this will
		 * reset the selected paths from the selected rows in the list
		 * selection model.
		 */
		protected void updateSelectedPathsFromSelectedRows() {
			if(!updatingListSelectionModel) {
				updatingListSelectionModel = true;
				try {
					// This is way expensive, ListSelectionModel needs an
					// enumerator for iterating.
					int        min = listSelectionModel.getMinSelectionIndex();
					int        max = listSelectionModel.getMaxSelectionIndex();

					clearSelection();
					if(min != -1 && max != -1) {
						for(int counter = min; counter <= max; counter++) {
							if(listSelectionModel.isSelectedIndex(counter)) {
								TreePath     selPath = tree.getPathForRow
								(counter);

								if(selPath != null) {
									addSelectionPath(selPath);
								}
							}
						}
					}
				}
				finally {
					updatingListSelectionModel = false;
				}
			}
		}

		/**
		 * Class responsible for calling updateSelectedPathsFromSelectedRows
		 * when the selection of the list changse.
		 */
		class ListSelectionHandler implements ListSelectionListener {
			
			/* (non-Javadoc)
			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
			 */
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedPathsFromSelectedRows();
			}
		}
	}

	/**
	 * The listener interface for receiving treeTableExpansion events.
	 * The class that is interested in processing a treeTableExpansion
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTreeTableExpansionListener<code> method. When
	 * the treeTableExpansion event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see TreeTableExpansionEvent
	 */
	class TreeTableExpansionListener implements TreeExpansionListener {
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
		 */
		public void treeCollapsed( TreeExpansionEvent e ) {
			String state = preserveExpanded();
			model.setConfigurationDialogExpandedState(state);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
		 */
		public void treeExpanded( TreeExpansionEvent e ) {
			String state = preserveExpanded();
			model.setConfigurationDialogExpandedState(state);
		}
	}
	
	/**
	 * The listener interface for receiving treeMouse events.
	 * The class that is interested in processing a treeMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTreeMouseListener<code> method. When
	 * the treeMouse event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see TreeMouseEvent
	 */
	class TreeMouseListener extends MouseAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked( MouseEvent event ) { 

			if ( event.getClickCount() > 1 ) {
				//super.mouseClicked( event );
				return;
			}

			// Ignore mouse clicks from the right button. The popup menu
			// will be handled via mousePressed().
			if ( SwingUtilities.isRightMouseButton( event ) ) return;

			int x = event.getX();
			int y = event.getY();
			int row = tree.getRowForLocation( x, y );
			TreePath treePath = tree.getPathForRow( row );

			if ( treePath == null ) {
				super.mouseClicked( event );
				return;
			}

			Node node = (Node)treePath.getLastPathComponent();

			// Need to determine if the click should be forwarded to the
			// super class or if it is over a 
			if ( node != null && treePath.getLastPathComponent().getClass() == TargetNode.class ) {
				if ( ((TargetNode)node).getState() == TargetConfiguration.State.Active ) {
					TargetNode n = (TargetNode) node;
					((TargetNode)node).setState( TargetConfiguration.State.Inactive );  
				}
				else if ( ((TargetNode)node).getState() == TargetConfiguration.State.Inactive ) {
					TargetNode n = (TargetNode) node;
					((TargetNode)node).setState( TargetConfiguration.State.Active );
				}
				else {
					// It is error state do nothing.
				}

				tree.updateUI();

			}
			else {
				super.mouseClicked( event ); 
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed( MouseEvent event ) {
			if ( SwingUtilities.isRightMouseButton( event ) ) {
				JTable source = (JTable)event.getSource();
				int row = source.rowAtPoint( event.getPoint() );
				
				// Don't do anything if the click was not on a valid row.
				if ( row < 0 ) return;
				
				source.setRowSelectionInterval( row, row );
			}

			showPopup( event );    
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased( MouseEvent event ) {
			showPopup( event );
		}

		/**
		 * Show popup.
		 * 
		 * @param event the event
		 */
		private void showPopup( MouseEvent event ) {
			if ( event.isPopupTrigger() ) {

				// Clear the menu.
				popupMenu.removeAll();

				JTable source = (JTable)event.getSource();
				int row = source.rowAtPoint( event.getPoint() );
				//int column = source.columnAtPoint( event.getPoint() );

				Object node = ((TreeTableModelAdapter)source.getModel()).nodeForRow(row);
				
				Class nodeClass = null;
				if (node != null) {
					nodeClass = node.getClass();
				}

				if ( nodeClass == TargetNode.class ) {
					// When on this row, you can delete.
					JMenuItem deleteItem = new JMenuItem( "Delete Configuration" );
					popupMenu.add( deleteItem );
					deleteItem.addActionListener( new DeleteTargetConfigurationAction() );
					JMenuItem exportItem = new JMenuItem( "Export Configuration" );
					popupMenu.add( exportItem );
					exportItem.addActionListener( new ExportConfigurationAction() );
				}

				if ( nodeClass == DetectionBandsNode.class ) {
					JMenuItem newItem = new JMenuItem( "Add Signal Band" );
					popupMenu.add( newItem );
					newItem.addActionListener( new AddSignalBandAction() );
					JMenuItem newItem2 = new JMenuItem( "Add Guard Band" );
					popupMenu.add( newItem2 );
					newItem2.addActionListener( new AddGuardBandAction() );
				}
				else if ( nodeClass == GuardBandNode.class ) {
					JMenuItem deleteItem = new JMenuItem( "Delete Band" ); 
					popupMenu.add( deleteItem );
					deleteItem.addActionListener( new DeleteGuardBandAction() );
				}
				else if ( nodeClass == SignalBandNode.class ) {
					JMenuItem deleteItem = new JMenuItem( "Delete Band" ); 
					popupMenu.add( deleteItem );
					deleteItem.addActionListener( new DeleteSignalBandAction() );					
				}
				else {
					JMenuItem addItem = new JMenuItem( "Add Configuration" );
					popupMenu.add( addItem );
					addItem.addActionListener( new AddTargetConfigurationAction() );
					JMenuItem importItem = new JMenuItem( "Import Configuration" );
					popupMenu.add( importItem );
					importItem.addActionListener( new ImportConfigurationAction() );

				}

				popupMenu.show( event.getComponent(), event.getX(), event.getY() ); 
			}
		}
	}

	/**
	 * The Class AddTargetConfigurationAction.
	 */
	private class AddTargetConfigurationAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
			// Loop over the model and make a list of the existing identifiers.
			ArrayList<String> existingIdentifiers = model.getTargetConfigurationNames();

			TargetConfigurationNameDialog d = new TargetConfigurationNameDialog( existingIdentifiers );
			d.setLocationRelativeTo(null);
			String newIdentifier = d.showDialog();

			if ( newIdentifier != null ) {
				String state = preserveExpanded();
				model.createNewTargetConfiguration( newIdentifier );
				restoreExpanded( state );
			}
		}
	}

	/**
	 * The Class DeleteTargetConfigurationAction.
	 */
	private class DeleteTargetConfigurationAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
			// Get the currently selected target configuration name.
			int row = getSelectedRow();
			String name = (String)getValueAt( row, 0 );
			if ( name != null ) {
				
				String state = preserveExpanded();
				
				model.deleteTargetConfiguration( name );
				
				restoreExpanded( state );
			}
		}
	}

	/**
	 * The Class AddSignalBandAction.
	 */
	private class AddSignalBandAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {

			// Find the name of the target configuration that is selected.
			int row = getSelectedRow();
			String configName = (String)getValueAt( row - 5 , 0 );

			TargetConfiguration config = model.getTargetConfiguration( configName );
			
			// Loop over the model and make a list of the existing identifiers.
			ArrayList<SignalBand> bands = config.getSignalBands();
			ArrayList<String> existingIdentifiers = new ArrayList<String>();
			for ( SignalBand b : bands ) {
				existingIdentifiers.add( b.identifier );	
			}
			
			NewBandNameDialog d = new NewBandNameDialog( NewBandNameDialog.BandType.Signal, existingIdentifiers );
			d.setLocationRelativeTo( null );
			String newIdentifier = d.showDialog();

			// Preserve the tree's expanded state;
			String state = preserveExpanded();

			if ( newIdentifier != null ) {
				model.createNewSignalBand( newIdentifier, configName );
			}
			
			// Restore the tree's expanded state.
			restoreExpanded( state );
		}
	}

	/**
	 * The Class AddGuardBandAction.
	 */
	private class AddGuardBandAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
			// Find the name of the target configuration that is selected.
			int row = getSelectedRow();
			String configName = (String)getValueAt( row - 5 , 0 );
			
			TargetConfiguration config = model.getTargetConfiguration( configName );
			
			// Loop over the model and make a list of the existing identifiers.
			ArrayList<GuardBand> bands = config.getGuardBands();
			ArrayList<String> existingIdentifiers = new ArrayList<String>();
			for ( GuardBand b : bands ) {
				existingIdentifiers.add( b.identifier );	
			}

			NewBandNameDialog d = new NewBandNameDialog( NewBandNameDialog.BandType.Guard, existingIdentifiers );
			d.setLocationRelativeTo(null);
			String newIdentifier = d.showDialog();

			String state = preserveExpanded();
			
			if ( newIdentifier != null ) {
				model.createNewGuardBand( newIdentifier, configName );
			}
			
			restoreExpanded( state );
		}
	}

	/**
	 * The Class DeleteGuardBandAction.
	 */
	private class DeleteGuardBandAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
			// Get the currently selected target configuration name.
			int row = getSelectedRow();
			String name = (String)getValueAt( row, 0 );
			name = name.replace(" (Guard Band)", "" );
			
			GuardBandNode n = (GuardBandNode)model.nodeForRow(row);
			String configName = n.getConfigName();
			
			String state = preserveExpanded();
			
			model.deleteGuardBandFrom( name, configName );
			
			restoreExpanded( state );
		}
	}

	/**
	 * The Class DeleteSignalBandAction.
	 */
	private class DeleteSignalBandAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
			// Get the currently selected target configuration name.
			int row = getSelectedRow();
			String name = (String)getValueAt( row, 0 );
			name = name.replace(" (Signal Band)", "" );
			
			SignalBandNode n = (SignalBandNode)model.nodeForRow(row);
			String configName = n.getConfigName();
			String state = preserveExpanded();
			model.deleteSignalBandFrom( name, configName );
			restoreExpanded( state );
		}
	}
	
	/**
	 * The Class ImportConfigurationAction.
	 */
	private class ImportConfigurationAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
			JDialog.setDefaultLookAndFeelDecorated( true );
			chooser.addChoosableFileFilter( new ConfigurationFileFilter() );
			int retcode = chooser.showOpenDialog( null );
			if ( retcode == JFileChooser.APPROVE_OPTION ) {
				File file = chooser.getSelectedFile();
				String fileName = new String( file.getAbsolutePath() );

				// If an extension wasn't supplied, add one.
				int pos = fileName.lastIndexOf( '.' );
				if ( pos == -1 ) {
					// No extension, add it.
					fileName += ".tcf";
				}

				TargetConfigurationImporter importer = new TargetConfigurationImporter( fileName, model.getAcquisitionSettings(), model.getChannelMap() );
				TargetConfiguration config = importer.doImport();
				if ( config == null ) {
					JOptionPane.showMessageDialog( null, "Unable to import configuration.", "Import error", JOptionPane.ERROR_MESSAGE );	
					return;
				}
				
				// Make sure that there isn't a configuration already with that identifier.
				ArrayList<String> configNames = model.getTargetConfigurationNames();
				for ( String name : configNames ) {
					if ( name.equals( config.getIdentifier() ) ) {
						TargetConfigurationNameDialog d = new TargetConfigurationNameDialog( configNames );
						String newIdentifier = d.showDialog();	
						if ( newIdentifier == null || newIdentifier.isEmpty() ) {
							JOptionPane.showMessageDialog( null, "Unable to import configuration - needs unique name.", "Import error", JOptionPane.ERROR_MESSAGE );	
							return;
						}
						else {
							config.setIdentifier( newIdentifier );	
						}
					}
				}
				
				model.addTargetConfiguration(config);
			}	
		}
	}

	/**
	 * The Class ExportConfigurationAction.
	 */
	private class ExportConfigurationAction implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
			int row = getSelectedRow();
			String name = (String)getValueAt(row,0);
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
			JDialog.setDefaultLookAndFeelDecorated( true );
			chooser.addChoosableFileFilter( new ConfigurationFileFilter() );
			int retcode = chooser.showSaveDialog( null );
			if ( retcode == JFileChooser.APPROVE_OPTION ) {
				File file = chooser.getSelectedFile();
				String fileName = new String( file.getAbsolutePath() );

				// If an extension wasn't supplied, add one.
				int pos = fileName.lastIndexOf( '.' );
				if ( pos == -1 ) {
					// No extension, add it.
					fileName += ".tcf";
				}

				//TargetConfiguration config = model.getTargetConfiguration( name );
				ArrayList<TargetConfiguration> configs = model.getTargetConfigurations();
				
		 		for ( int i = 0; i < configs.size(); ++i ) {
		  			if ( configs.get(i).getIdentifier().equals( name )) {
		  		
		  				TargetConfigurationExporter exporter = new TargetConfigurationExporter( fileName );
						boolean ok = exporter.doExport( configs.get(i) );
						if ( !ok ) {
							JOptionPane.showMessageDialog( null, "Unable to export configuration.", "Export error", JOptionPane.ERROR_MESSAGE );	
						}	
		  			}
		  		}
	
			}
		}
	}

	/**
	 * The Class ConfigurationFileFilter is used by the import and export target configuration
	 * dialogs to filter out only the files that are saved with the 'tcf' extension.
	 */
	class ConfigurationFileFilter extends FileFilter {
		
		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept( File f ) {
			if ( f.isDirectory() ) {
				return true;
			}

			int pos = f.getName().lastIndexOf( '.' );
			if ( pos >= 0 ) {
				String ext = f.getName().substring( pos + 1 );
				if ( ext.equals( "tcf" ) ) return true;
				else return false;
			}
			else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		@Override
		public String getDescription() {
			return "Target Configuration Files";
		}
	}

    /**
     * This method is called to preserve the expanded state of the internal
     * JTree. It is preserved by capturing the expanded states of the rows,
     * in order using string values of 0 or 1. This string can then be stored
     * with the persistent settings and restored using restoreExpanded()
     * 
     * @see restoreExpanded
     * 
     * @return A comma-delimited string representing the expanded states.
     */
    private String preserveExpanded() {
		TreePath rowPath = tree.getPathForRow( 0 );
		StringBuffer buf = new StringBuffer();
		int rowCount = tree.getRowCount();
		for ( int i = 0; i < rowCount; i++ ) {
			TreePath path = tree.getPathForRow( i );
			if ( i == 0 || isDescendant( path, rowPath ) ) {
					if ( tree.isExpanded( path ) ) {
						buf.append( "," + String.valueOf( i ) );	
					}
			}
			else break;
		}
		return buf.toString();
    }

    /**
     * This method accepts a comma-delimited string returned from the
     * preserveExpanded() method and attempts to change the internal JTree's
     * expanded state to match it.
     * 
     * @see preserveExpanded
     * 
     * @param s the s
     */
    public void restoreExpanded( String s ) {
    	if ( s == null || s.isEmpty() ) return;
    	StringTokenizer stok = new StringTokenizer( s, "," );
    	while ( stok.hasMoreTokens() ) {
    		int token = Integer.parseInt( stok.nextToken() );
    		tree.expandRow( token );
    	}
    }
    
    /**
     * A helper method used by preserveExpanded() to recursively moved
     * down the internal JTree representation of the model.
     *
     * @see preserveExpanded
     * 
     * @return true, if is descendant
     */
    private static boolean isDescendant( TreePath p1, TreePath p2 ) {
    	int count1 = p1.getPathCount();
    	int count2 = p2.getPathCount();
    	if ( count1 <= count2 ) return false;
    	while ( count1 != count2 ) {
    		p1 = p1.getParentPath();
    		count1--;
    	}
    	
    	return p1.equals( p2 );
    }
}