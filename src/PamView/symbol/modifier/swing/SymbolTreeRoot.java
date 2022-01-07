package PamView.symbol.modifier.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.modifier.SymbolModifier;

public class SymbolTreeRoot implements TreeNode {

	protected ArrayList<SymbolModifier> modifiers;
	protected StandardSymbolChooser symbolChooser;
	protected ArrayList<ModifierTreeNode> modifierNodes;

	public SymbolTreeRoot(StandardSymbolChooser symbolChooser, ArrayList<SymbolModifier> modifiers) {
		this.symbolChooser = symbolChooser;
		this.modifiers = modifiers;
		modifierNodes = new ArrayList<>();
		for (SymbolModifier modifier : modifiers) {
			ModifierTreeNode modNode = new ModifierTreeNode(this, modifier);
			modifierNodes.add(modNode);
		}
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return modifierNodes.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return modifierNodes.size();
	}

	@Override
	public TreeNode getParent() {
		return null;
	}

	@Override
	public int getIndex(TreeNode node) {
		return 0;
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		// TODO Auto-generated method stub
//		return Collections.enumeration(c)
		return null;
	}

}
