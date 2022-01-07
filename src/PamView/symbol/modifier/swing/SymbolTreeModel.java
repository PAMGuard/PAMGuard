package PamView.symbol.modifier.swing;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.modifier.SymbolModifier;

public  class SymbolTreeModel implements TreeModel {

	protected ArrayList<SymbolModifier> modifiers;
	private PamSymbolChooser symbolChooser;
	private SymbolTreeRoot treeRoot;
	protected int[] nodeOrder;
	
	public SymbolTreeModel(StandardSymbolChooser symbolChooser, int[] nodeOrder) {
		super();
		this.symbolChooser = symbolChooser;
		this.modifiers = symbolChooser.getSymbolModifiers();
		treeRoot = new SymbolTreeRoot(symbolChooser, modifiers);
		this.nodeOrder = nodeOrder;
	}

	@Override
	public SymbolTreeRoot getRoot() {
		return treeRoot;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof SymbolTreeRoot) {
			return treeRoot.getChildAt(nodeOrder[index]);
		}
		if (parent instanceof ModifierTreeNode) {
			ModifierTreeNode modNode = (ModifierTreeNode) parent;
			return modNode.getChildAt(index);
		}
		return null;
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof SymbolTreeRoot) {
			return treeRoot.getChildCount();
		}
		if (parent instanceof ModifierTreeNode) {
			ModifierTreeNode modNode = (ModifierTreeNode) parent;
			return modNode.getChildCount();
		}
		else {
			return 0;
		}
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof TreeNode) {
			TreeNode tn = (TreeNode) node;
			return tn.isLeaf();
		}
		return true;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
//		if (parent instanceof TreeNode) {
//			TreeNode tn = (TreeNode) parent;
//			return tn.getIndex(chi)
			return 0;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		
	}

	public void swapNodes(int nodeInd, int newInd) {
		int v = nodeOrder[nodeInd];
		nodeOrder[nodeInd] = nodeOrder[newInd];
		nodeOrder[newInd] = v;		
	}

	public int reverseOrder(int nodeInd1) {
		for (int i = 0; i < nodeOrder.length; i++) {
			if (nodeOrder[i] == nodeInd1) {
				return i;
			}
		}
		return nodeInd1;
	}
}
