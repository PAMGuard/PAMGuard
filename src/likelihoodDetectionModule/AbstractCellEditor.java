package likelihoodDetectionModule;

import javax.swing.*;
import javax.swing.event.*;
import java.util.EventObject;

/**
 * The class AbstractCellEditor serves as the base class for the TreeTable's 
 * table cell editor. It provides additional functionality that would be
 * required when the TreeTableCellEditor inherits TableCellEditor. This is
 * broken out to help keep the TreeTableCellEditor code short and to-the-point
 * within the TreeTable class.
 */
public class AbstractCellEditor implements CellEditor {

    /** The listener list. */
    protected EventListenerList listenerList = new EventListenerList();

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() { return null; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    public boolean isCellEditable(EventObject e) { return true; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    public boolean shouldSelectCell(EventObject anEvent) { return false; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    public boolean stopCellEditing() { return true; }
    
    /* (non-Javadoc)
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    public void cancelCellEditing() {}

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  
     * @see EventListenerList
     */
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==CellEditorListener.class) {
                ((CellEditorListener)listeners[i+1]).editingStopped(new ChangeEvent(this));
            }          
        }
    }

    /*
     * Notify all listeners that have registered interest for
     * notification on this event type.  
     * @see EventListenerList
     */
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==CellEditorListener.class) {
                ((CellEditorListener)listeners[i+1]).editingCanceled(new ChangeEvent(this));
            }          
        }
    }
}
