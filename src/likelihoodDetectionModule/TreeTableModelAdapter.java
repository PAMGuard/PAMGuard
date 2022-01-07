package likelihoodDetectionModule;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.util.Vector;
import java.util.ArrayList;

/**
 * The class TreeTableModelAdapter is a necessary part of the TreeTable implementation
 * because it serves as a bridge between the JTable that forms part of the widget and
 * the JTree that forms another part (the table cell renderer).
 * 
 * The Swing JTable uses a TableModel class as its backend representation in the
 * MVC paradigm. In this case, the AbstractTableModel interface is extended. However the
 * JTree uses a TreeModel representation, and there is no common hierarchy between
 * the two separate representations. In order for the mouse events (et al) coming
 * from the JTable part of the TreeTable to reach the embedded JTree (and hence the
 * actual model) they need to be routed through a TableModel adapter. The adapter 
 * provides the JTable with the proper backend interface and then translates the
 * methods into JTree ones.
 * 
 * @see TreeTable
 * @see AbstractTableModel
 * @see TableModel
 */
public class TreeTableModelAdapter extends AbstractTableModel {	
	/** The constant serialVersionUID. required for serialization. */
	private static final long serialVersionUID = 7773263l;
	
	/** The tree part of the internals of the TreeTable. Some calls
	 * will be made directly on it. */
	JTree tree;
    
    /** The tree table model that this model wraps. */
    TreeTableModel treeTableModel;

    /**
     * Instantiates a new tree table model adapter.
     * 
     * @param treeTableModel The tree table model for the TreeTable that is
     * being wrapped by the adapter.
     * @param tree The actual JTree implementation that is internal to the 
     * TreeTable.
     */
    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
        this.tree = tree;
        this.treeTableModel = treeTableModel;
        
        // Do not allow any drag and drop.
        tree.setDragEnabled( false );
        
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            // Don't use fireTableRowsInserted() here; the selection model
            // would get updated twice. 
            public void treeExpanded(TreeExpansionEvent event) {  
              fireTableDataChanged(); 
            }
            public void treeCollapsed(TreeExpansionEvent event) {  
              fireTableDataChanged(); 
            }
        });

        // Install a TreeModelListener that can update the table when
        // tree changes. We use delayedFireTableDataChanged as we can
        // not be guaranteed the tree will have finished processing
        // the event before us.
        treeTableModel.addTreeModelListener(new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {
                delayedFireTableDataChanged();
            }

            public void treeNodesInserted(TreeModelEvent e) {
                delayedFireTableDataChanged();
            }

            public void treeNodesRemoved(TreeModelEvent e) {
                delayedFireTableDataChanged();
            }

            public void treeStructureChanged(TreeModelEvent e) {
                delayedFireTableDataChanged();
            }
        });
    }

    /**
     * Gets the target configuration name for associated band node.
     * 
     * @param n the n
     * 
     * @return the target configuration name for associated band node
     */
    public String getTargetConfigurationNameForAssociatedBandNode( AssociatedBandNode n ) {
    	return treeTableModel.getTargetConfigurationNameForAssociatedBandNode( n );	
    }
    
    /**
     * Gets the signal band names.
     * 
     * @param configIdentifier the config identifier
     * 
     * @return the signal band names
     */
    public Vector<String> getSignalBandNames( String configIdentifier ) {
      return treeTableModel.getSignalBandNames( configIdentifier );  
    }
    
    /**
     * Gets the target configuration names.
     * 
     * @return the target configuration names
     */
    public ArrayList<String> getTargetConfigurationNames() {
    	return treeTableModel.targetConfigurationNames();	
    }
    
 	/**
	  * Creates the new target configuration.
	  * 
	  * @param identifier the identifier
	  */
	 public void createNewTargetConfiguration( String identifier ) {
 		treeTableModel.createNewTargetConfiguration(identifier);
 	}
    
 	/**
	  * Adds the target configuration.
	  * 
	  * @param config the config
	  */
	 public void addTargetConfiguration( TargetConfiguration config ) {
 		treeTableModel.addTargetConfiguration( config );	
 	}
 	
 	/**
	  * Delete target configuration.
	  * 
	  * @param identifier the identifier
	  */
	 public void deleteTargetConfiguration( String identifier ) {
 		treeTableModel.deleteTargetConfiguration( identifier );	
 	}
 	
 	/**
	  * Gets the band names.
	  * 
	  * @return the band names
	  */
	 public ArrayList<String> getBandNames() {
 		return treeTableModel.getBandNames();	
 	}
 	
 	/**
	  * Creates the new signal band.
	  * 
	  * @param bandIdentifier the band identifier
	  * @param configurationIdentifier the configuration identifier
	  */
	 public void createNewSignalBand( String bandIdentifier, String configurationIdentifier ) {
 		treeTableModel.createNewSignalBand( bandIdentifier, configurationIdentifier );
 		this.fireTableDataChanged();
 	}
 	
	/**
	 * Creates the new guard band.
	 * 
	 * @param bandIdentifier the band identifier
	 * @param configurationIdentifier the configuration identifier
	 */
	public void createNewGuardBand( String bandIdentifier, String configurationIdentifier ) {
 		treeTableModel.createNewGuardBand( bandIdentifier, configurationIdentifier );
 		this.fireTableDataChanged();
 	}
	
	/**
	 * Delete guard band from.
	 * 
	 * @param identifier the identifier
	 * @param targetConfigurationName the target configuration name
	 */
	public void deleteGuardBandFrom( String identifier, String targetConfigurationName ) {
		treeTableModel.deleteGuardBandFrom( identifier, targetConfigurationName );	
	}
	
	/**
	 * Delete signal band from.
	 * 
	 * @param id the id
	 * @param configName the config name
	 */
	public void deleteSignalBandFrom( String id, String configName ) {
		treeTableModel.deleteSignalBandFrom( id, configName);
	}
	
	/**
	 * Gets the target configuration.
	 * 
	 * @param identifier the identifier
	 * 
	 * @return the target configuration
	 */
	public TargetConfiguration getTargetConfiguration( String identifier ) {
		return treeTableModel.getTargetConfiguration( identifier );
	}
	
	/**
	 * Gets the acquisition settings.
	 * 
	 * @return the acquisition settings
	 */
	public AcquisitionSettings getAcquisitionSettings() {
		return treeTableModel.getAcquisitionSettings();	
	}
	
	public ArrayList<TargetConfiguration> getTargetConfigurations() {
		return treeTableModel.getTargetConfigurations();
	}
	
	/**
	 * Gets the channel map.
	 * 
	 * @return the channel map
	 */
	public int getChannelMap() {
		return treeTableModel.getChannelMap();	
	}
	
	/**
	 * Sets the configuration dialog settings.
	 * 
	 * @param settings the new configuration dialog settings
	 */
	public void setConfigurationDialogSettings( ConfigurationDialogSettings settings ) {
		treeTableModel.setConfigurationDialogSettings( settings );	
	}
	
	/**
	 * Gets the configuration dialog settings.
	 * 
	 * @return the configuration dialog settings
	 */
	public ConfigurationDialogSettings getConfigurationDialogSettings() {
		return treeTableModel.getDialogSettings();	
	}
	
	/**
	 * Sets the configuration dialog expanded state.
	 * 
	 * @param state the new configuration dialog expanded state
	 */
	public void setConfigurationDialogExpandedState( String state ) {
		treeTableModel.setConfigurationDialogExpandedState( state );	
	}
	
    // Wrappers, implementing TableModel interface. 

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return treeTableModel.getColumnCount();
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
	public String getColumnName(int column) {
        return treeTableModel.getColumnName(column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
	public Class getColumnClass(int column) {
        return treeTableModel.getColumnClass(column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return tree.getRowCount();
    }

    /**
     * Node for row.
     * 
     * @param row the row
     * 
     * @return the object
     */
    public Object nodeForRow(int row) {
        TreePath treePath = tree.getPathForRow(row);
        
        if ( treePath == null ) return null;

        return treePath.getLastPathComponent();         
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column) {
        return treeTableModel.getValueAt(nodeForRow(row), column);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
	public boolean isCellEditable(int row, int column) {
         return treeTableModel.isCellEditable(nodeForRow(row), column); 
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
	public void setValueAt(Object value, int row, int column) {
        treeTableModel.setValueAt(value, nodeForRow(row), column);
        this.fireTableStructureChanged();
    }

    /**
     * Invokes fireTableDataChanged after all the pending events have been
     * processed. SwingUtilities.invokeLater is used to handle this.
     */
    protected void delayedFireTableDataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableDataChanged();
            }
        });
    }
}

