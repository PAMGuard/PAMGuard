package PamView.symbol.modifier.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.tree.TreeNode;

import PamView.symbol.modifier.SymbolModType;

public class ChoiceTreeNode implements TreeNode, ActionListener {
	
	protected ModifierTreeNode parent;;
	protected int selectionBit;
	private int index;
	protected JCheckBox checkBox;

	public ChoiceTreeNode(ModifierTreeNode parent, int selectionBit, int index) {
		super();
		this.parent = parent;
		this.selectionBit = selectionBit;
		this.index = index;
		checkBox = new JCheckBox(getName());
		checkBox.addActionListener(this);
		checkBox.setEnabled(true);
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return index;
	}

	@Override
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return SymbolModType.getName(selectionBit);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		parent.getModifier().setSelectionBit(selectionBit, checkBox.isSelected());
//		Debug.out.println("Chckbox " + checkBox.getText() + " selected " + checkBox.isSelected());
	}

}
