package likelihoodDetectionModule;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

/**
 * This class provides a custom renderer for a JTree that displays a
 * check box before the standard open/closed subtree icon, and a text string
 * for the node content. In the context of the TreeTable for the Likelihood
 * detector, the top-level node that this cell renderer is used for does not
 * display any node value information.
 * The CheckRenderer intercepts mouse click events destined for the JTree, and
 * if they are in the appropriate geometry, handles them as it could a check box.
 * Otherwise, the events are forwarded to the JTree.
 */

public class CheckRenderer extends JPanel implements TreeCellRenderer {
  
  /** The Constant serialVersionUID. */
  static final long serialVersionUID = 24323;
  
  /** The show checkbox. */
  private boolean showCheckbox;
  
  /** The check box. */
  private JCheckBox checkBox;
  
  /** The label. */
  private TreeLabel label;
  
  /** The model. */
  @SuppressWarnings("unused")
  private TreeModel model;
  
  /** The node name. */
  @SuppressWarnings("unused")
  private String nodeName;
  
  /**
   * Instantiates a new check renderer.
   * 
   * @param model the model
   */
  public CheckRenderer( TreeModel model ) {
    setLayout( null );
    this.showCheckbox = true;
    this.model = model;
    checkBox = new JCheckBox();
    label = new TreeLabel();
    checkBox.setBackground( UIManager.getColor( "Tree.textBackground" ) );
    label.setBackground( UIManager.getColor( "Tree.textForeground" ) );
  }
  
  /* (non-Javadoc)
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
   */
  @Override
public Component getTreeCellRendererComponent( JTree tree, Object value, boolean isSelected,
      boolean isExpanded, boolean isLeaf, int row, boolean hasFocus ) {

    String stringValue = tree.convertValueToText( value, isSelected, isExpanded, isLeaf, row, hasFocus );
    this.setEnabled( tree.isEnabled() );
    this.removeAll();
    
    if ( value.getClass() == TargetNode.class ) {
    	
    	// Java does something weird when it first renders the dialog's tree table. It
    	// calls getTreeCellRendererComponent() with a bunch of strange values that
    	// are erroneous. This results in the wrong listeners being added to the
    	// wrong checkboxes. It does a second pass for some reason, and on the second pass
    	// the parameters are correct. The best was I could come up with to compensate
    	// for this behaviour is to simply remove all item listeners from a checkbox
    	// before the proper one is selected and bound for it.
    	
   		ItemListener[] listeners = this.checkBox.getItemListeners();
    	for ( ItemListener a : listeners ) {
   	    	this.checkBox.removeItemListener(a);	
    	}
    	
      //this.checkBox.removeItemListener( (ItemListener)value );
      this.showCheckbox = true;
      this.add( checkBox );
      checkBox.setSelected( ((TargetNode)value).getState() == TargetConfiguration.State.Active );
      if ( ((TargetNode)value).getState() == TargetConfiguration.State.Error ) {
    	checkBox.setBackground( Color.pink );  
      }
      else {
    	checkBox.setBackground( Color.white );  
      }
      
      this.add( label );
      nodeName = stringValue;
      this.checkBox.addItemListener( (ItemListener)value );
    }
    else {
      this.showCheckbox = false;
      this.add( label );
    }
    
    label.setFont( tree.getFont() );
    label.setText( stringValue );
    label.setSelected( isSelected );
    label.setFocus( hasFocus );
    
    return this;
  }

  /**
   * Checks for check box.
   * 
   * @return true, if successful
   */
  public boolean hasCheckBox() {
    return this.showCheckbox;  
  }
  
  /**
   * Toggle checkbox.
   */
  public void toggleCheckbox() {
    
  }
  
  /**
   * Gets the label text.
   * 
   * @return the label text
   */
  public String getLabelText() {
    return this.label.getText();  
  }
  
  /**
   * Point within check box.
   * 
   * @param x the x
   * @param y the y
   * @param nodeName the node name
   * 
   * @return true, if successful
   */
  public boolean pointWithinCheckBox( int x, int y, String nodeName ) {
    if ( ! showCheckbox ) return false;
    int width = checkBox.getPreferredSize().width;
    int height = ( checkBox.getPreferredSize().height < label.getPreferredSize().height ? label.getPreferredSize().height : checkBox.getPreferredSize().height );
    if ( x >=0 && x <= height && y >= 0 && y <= width ) return true;
    return false;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
public Dimension getPreferredSize() {
    if ( this.showCheckbox ) {
    Dimension cd = checkBox.getPreferredSize();
    Dimension ld = label.getPreferredSize();
    return new Dimension( cd.width + ld.width, ( cd.height < ld.height ? ld.height : cd.height ) );
    }
    else {
      return label.getPreferredSize();
    }
  } 

  /* (non-Javadoc)
   * @see java.awt.Container#doLayout()
   */
  @Override
public void doLayout() {
    if ( this.showCheckbox ) {
      Dimension checkDim = checkBox.getPreferredSize();
      Dimension labelDim = label.getPreferredSize();

      int check_y = 0;
      int label_y = 0;

      if ( checkDim.height < labelDim.height ) {
        check_y = (labelDim.height - checkDim.height ) / 2;
      }
      else {
        label_y = ( checkDim.height - labelDim.height ) / 2; 
      }

      checkBox.setLocation( 0, check_y );
      checkBox.setBounds( 0, check_y, checkDim.width, checkDim.height );
      label.setLocation( checkDim.width, label_y );
      label.setBounds( checkDim.width, label_y, labelDim.width, labelDim.height );     
    }
    else {
      Dimension labelDim = label.getPreferredSize();
      label.setLocation( 0, 0 );
      label.setBounds( 0, 0, labelDim.width, labelDim.height );
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#setBackground(java.awt.Color)
   */
  @Override
public void setBackground( Color color ) {
    if ( color instanceof ColorUIResource ) color = null;
    super.setBackground( color );
  }
  
  /**
   * The Class TreeLabel.
   */
  public class TreeLabel extends JLabel {
    
    /** The Constant serialVersionUID. */
    static final long serialVersionUID = 902734;
    
    /** The is selected. */
    boolean isSelected;
    
    /** The has focus. */
    boolean hasFocus;

    /* (non-Javadoc)
     * @see javax.swing.JComponent#setBackground(java.awt.Color)
     */
    @Override
	public void setBackground( Color color ) {
      if ( color instanceof ColorUIResource ) color = null;
      super.setBackground( color );
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
	public void paint( Graphics g ) {
      String str = this.getText();
      if ( str != null ) {
        if ( 0 < str.length() ) {
          if ( isSelected ) {
            g.setColor( UIManager.getColor( "Tree.selectionBackground" ) ); 
          }
          else {
            g.setColor( UIManager.getColor( "Tree.textBackground" ) ); 
          }
          Dimension d = this.getPreferredSize();
          int imageOffset = 0;
          Icon currentIcon = this.getIcon();
          if ( currentIcon != null ) {
            imageOffset = currentIcon.getIconWidth() + Math.max( 0, this.getIconTextGap() - 1 ); 
          }
          g.fillRect( imageOffset, 0, d.width - 1 - imageOffset, d.height );
          if ( hasFocus ) {
            g.setColor( UIManager.getColor( "Tree.selectionBorderColor" ) );
            g.drawRect( imageOffset, 0, d.width - 1 - imageOffset, d.height - 1 );
          }
        }
      }
      
      super.paint( g );
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
	public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      if ( d != null ) {
        d = new Dimension( d.width + 3, d.height ); 
      }
      return d;
    }
    
    /**
     * Sets the selected.
     * 
     * @param isSelected the new selected
     */
    public void setSelected( boolean isSelected ) {
      this.isSelected = isSelected; 
    }
    
    /**
     * Sets the focus.
     * 
     * @param hasFocus the new focus
     */
    public void setFocus( boolean hasFocus ) {
      this.hasFocus = hasFocus;
    }
  }
  
}

