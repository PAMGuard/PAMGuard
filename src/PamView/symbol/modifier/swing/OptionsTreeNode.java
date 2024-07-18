package PamView.symbol.modifier.swing;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreeNode;

import PamView.dialog.GenericSwingDialog;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.modifier.SymbolModifier;

public class OptionsTreeNode implements TreeNode {

	private ModifierTreeNode parent;
	private SymbolModifier modifier;
	private PamDialogPanel optionsPanel;
	protected JButton optionsButton;
	private int leafIndex;

	public OptionsTreeNode(ModifierTreeNode parent, SymbolModifier modifier, PamDialogPanel optionsPanel, int leafIndex) {
		this.parent = parent;
		this.modifier = modifier;
		this.optionsPanel = optionsPanel;
		this.leafIndex = leafIndex;
		optionsButton = new JButton("more ...");
		Insets insets = optionsButton.getInsets();
		if (insets == null) insets = new Insets(0,0,0,0);
		insets.bottom = insets.top = insets.left = 1;
		optionsButton.setBorder(new EmptyBorder(insets));
		optionsButton.setToolTipText("More symbol options");
		optionsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptions();
			}
		});
	}

	private void showOptions() {
		optionsPanel.setParams();
		boolean ok = GenericSwingDialog.showDialog(null, "Options", optionsPanel);
		if (ok) {
			optionsPanel.getParams();
		}
	}
	
	@Override
	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public int getIndex(TreeNode node) {
		return leafIndex;
	}

	@Override
	public boolean getAllowsChildren() {
		return false;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public Enumeration<? extends TreeNode> children() {
		return null;
	}

}
