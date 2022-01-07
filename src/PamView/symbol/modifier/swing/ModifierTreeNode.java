package PamView.symbol.modifier.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import PamUtils.PamUtils;
import PamView.symbol.modifier.SymbolModifier;

public class ModifierTreeNode implements TreeNode {
	private SymbolModifier modifier;
	private SymbolTreeRoot rootNode;
	private ArrayList<ChoiceTreeNode> choiceNodes;

	public ModifierTreeNode(SymbolTreeRoot rootNode, SymbolModifier modifier) {
		super();
		this.rootNode = rootNode;
		this.modifier = modifier;
		int modBits = modifier.getModifyableBits();
		int nMod = Integer.bitCount(modBits);
		choiceNodes = new ArrayList<>();
		for (int i = 0; i < nMod; i++) {
			choiceNodes.add(new ChoiceTreeNode(this, 1<<PamUtils.getNthChannel(i, modBits), i));
		}
		
		
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return choiceNodes.get(childIndex);
//		return 
	}

	@Override
	public int getChildCount() {
		return choiceNodes.size();
	}

	@Override
	public TreeNode getParent() {
		return rootNode;
	}

	@Override
	public int getIndex(TreeNode node) {
		return rootNode.modifiers.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return Collections.enumeration(choiceNodes);
	}

	/**
	 * @return the modifier
	 */
	public SymbolModifier getModifier() {
		return modifier;
	}

	public void setModBitmap(int modBitMap) {
		for (ChoiceTreeNode cN : choiceNodes) {
			cN.checkBox.setSelected((cN.selectionBit & modBitMap) != 0);
		}
	}
	
	public int getModBitmap() {
		int mp = 0;
		for (ChoiceTreeNode cN : choiceNodes) {
			if (cN.checkBox.isSelected()) {
				mp |= cN.selectionBit;
			}
		}
		return mp;
	}

}
