package PamView.symbol.modifier.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import PamUtils.PamUtils;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.modifier.SymbolModifier;

public class ModifierTreeNode implements TreeNode {
	private SymbolModifier modifier;
	private SymbolTreeRoot rootNode;
	private ArrayList<TreeNode> choiceNodes;

	public ModifierTreeNode(SymbolTreeRoot rootNode, SymbolModifier modifier) {
		super();
		this.rootNode = rootNode;
		this.modifier = modifier;
		int modBits = modifier.getModifyableBits();
		int nMod = Integer.bitCount(modBits);
		choiceNodes = new ArrayList<>();

		int leafIndex = 0;

		for (int i = 0; i < nMod; i++) {
			choiceNodes.add(new ChoiceTreeNode(this, 1<<PamUtils.getNthChannel(i, modBits), leafIndex++));
		}

		PamDialogPanel optionsPanel = modifier.getDialogPanel();
		if (optionsPanel != null) {
			choiceNodes.add(new OptionsTreeNode(this, modifier, optionsPanel, leafIndex++));
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
		for (TreeNode tN : choiceNodes) {
			if (tN instanceof ChoiceTreeNode) {
				ChoiceTreeNode cN = (ChoiceTreeNode) tN;
				cN.checkBox.setSelected((cN.selectionBit & modBitMap) != 0);
			}
		}
	}

	public int getModBitmap() {
		int mp = 0;
		for (TreeNode tN : choiceNodes) {
			if (tN instanceof ChoiceTreeNode) {
				ChoiceTreeNode cN = (ChoiceTreeNode) tN;
				if (cN.checkBox.isSelected()) {
					mp |= cN.selectionBit;
				}
			}
		}
		return mp;
	}

}
