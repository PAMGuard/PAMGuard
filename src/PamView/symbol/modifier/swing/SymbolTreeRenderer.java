package PamView.symbol.modifier.swing;

import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import PamView.PamSymbol;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModifier;

public class SymbolTreeRenderer extends DefaultTreeCellRenderer {

	private static final double DEF_HEIGHT = 12;
	private static final double DEF_WIDTH = 12;

    private Color selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
    private Color selectionForeground = UIManager.getColor("Tree.selectionForeground");
    private Color selectionBackground = UIManager.getColor("Tree.selectionBackground");
    private Color textForeground = UIManager.getColor("Tree.textForeground");
    private Color textBackground = UIManager.getColor("Tree.textBackground");

	public SymbolTreeRenderer(JTree tree) {
		super();
		tree.setCellEditor(new CheckBoxNodeEditor(tree));
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		if (value instanceof ChoiceTreeNode) {
			return checkboxChoice(tree, (ChoiceTreeNode) value, sel, expanded, leaf, row, hasFocus);
		}
		
		if (value instanceof OptionsTreeNode) {
			return ((OptionsTreeNode) value).optionsButton;
		}
				
		Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (value instanceof ModifierTreeNode) {
			setModifierStuff((ModifierTreeNode) value, component);
		}
		if (value instanceof SymbolTreeRoot) {
			setRootStuff((SymbolTreeRoot) value, component);
		}

		return component;
	}

	private Component checkboxChoice(JTree tree, ChoiceTreeNode value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		JCheckBox checkbox = value.checkBox;
		if (selected) {
			checkbox.setForeground(selectionForeground);
			checkbox.setBackground(selectionBackground);
		} else {
			checkbox.setForeground(textForeground);
			checkbox.setBackground(textBackground);
		}
		checkbox.setEnabled(true);
//		checkbox.setSelected(!checkbox.isSelected());
		return checkbox;
//		return new JCheckBox("boo");
	}
	
	class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

//		  CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

		  ChangeEvent changeEvent = null;

		  JTree tree;
		  
//		  private ChoiceTreeNode checkBoxNode;
		  

		  public CheckBoxNodeEditor(JTree tree) {
		    this.tree = tree;
//		    this.checkBoxNode = checkBoxNode;
		  }
		  
		  public boolean isCellEditable(EventObject event) {
//			  Debug.out.println("Mouse event " +event);
			  return true;
		  }

		@Override
		public Object getCellEditorValue() {
			return null;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
				boolean leaf, int row) {
			return getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, leaf);
		}
	}

	private void setRootStuff(SymbolTreeRoot value, Component component) {
		StandardSymbolChooser symbolChooser = value.symbolChooser;
		try {
			setIcon(getDefSymbol(symbolChooser));
		}
		catch (Exception e) {
			
		}
		setText("Default Symbol");
		setToolTipText(symbolChooser.getDisplayName());
		
	}
	
	private PamSymbol getDefSymbol(StandardSymbolChooser symbolChooser) {
		try {
			PamSymbol symb = symbolChooser.getPamSymbol(null, null);
			if (symb != null) {
				return symb;
			}
		}
		catch (Exception e) {
			
		}
		try {
			PamSymbol symb = new PamSymbol(symbolChooser.getDefaultSymbol());
			if (symb != null) {
				return symb;
			}
		}
		catch (Exception e) {
			
		}
		return new PamSymbol();
		
	}

	private void setModifierStuff(ModifierTreeNode value, Component component) {
		SymbolModifier modifier = value.getModifier();
		setToolTipText(modifier.getToolTipText());
		try {
			SymbolData symData = modifier.getSymbolData(null, null);
			PamSymbol symbol = new PamSymbol(symData);
			symbol.setHeight(DEF_HEIGHT);
			symbol.setWidth(DEF_WIDTH);
			setIcon(symbol);
		}
		catch (Exception e) {
//			return;
		}
		
//		setToolTipText(modifier.getName());
		setText(modifier.getName());
		
	}

}
