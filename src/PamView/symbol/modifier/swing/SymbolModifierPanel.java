package PamView.symbol.modifier.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;

import PamView.dialog.DialogScrollPane;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import PamguardMVC.debug.Debug;

/**
 * Panel to include in a dialog with options to select and activate a variety of symbol modifiers. 
 * @author Dougl
 *
 */
public class SymbolModifierPanel implements PamDialogPanel {

	private StandardSymbolChooser symbolChooser;
	
	private JPanel mainPanel;
	
	private JTree tree;

	private ArrayList<SymbolModifier> modifiers;

	private SymbolTreeModel treeModel;

	public SymbolModifierPanel(StandardSymbolChooser symbolChooser) {
		super();
		this.symbolChooser = symbolChooser;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Modifiers"));
		modifiers = symbolChooser.getSymbolModifiers();
		
		tree = new JTree(treeModel = new SymbolTreeModel(symbolChooser, symbolChooser.getSymbolOptions().getModifierOrder(symbolChooser)));
		tree.setDragEnabled(true);
		tree.setCellRenderer(new SymbolTreeRenderer(tree));
		tree.setAutoscrolls(true);
		tree.setShowsRootHandles(true);
		tree.setEditable(true);
		tree.addMouseListener(new TreeMouse());
		tree.setToolTipText("Right click on symbol modifiers for options and to change their order");
		JScrollPane scrollPane = new DialogScrollPane(tree, 30);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		
	}
	
	protected void expandTree() {
		/*
		 * It seems that just getting the number of rows and then expanding them doesn't work, since the
		 * number of rows increases with each call to expand row ! So have to go recursive, or easier still
		 * just do them in reverse order. 
		 * Lots of other solutions at 
		 * https://stackoverflow.com/questions/15210979/how-do-i-auto-expand-a-jtree-when-setting-a-new-treemodel
		 */
		int n = tree.getRowCount();
		for (int i = n-1; i >= 0; i--) {
			tree.expandRow(i);
		}
//		expandAllNodes(0, tree.getRowCount());
	}
	
//	/**
//	 * Recursive expansion of tree nodes. 
//	 * @param startingIndex
//	 * @param rowCount
//	 */
//	private void expandAllNodes(int startingIndex, int rowCount){
//	    for(int i=startingIndex;i<rowCount;++i){
//	        tree.expandRow(i);
//	    }
//
//	    if(tree.getRowCount()!=rowCount){
//	        expandAllNodes(rowCount, tree.getRowCount());
//	    }
//	}
	
	private class TreeMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		private void showPopup(MouseEvent e) {
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				return;
			}
			Object node = path.getLastPathComponent();
			if (node instanceof SymbolTreeRoot) {
				// any options we want on the main Default Symbol text 
			}
			if (node instanceof ModifierTreeNode) {
				// it's one of the modifier roots. 
				showPopup(e, (ModifierTreeNode) node);
			}
			
		}

		private void showPopup(MouseEvent e, ModifierTreeNode node) {
			if (modifiers.size() < 2) {
				return;
			}
			int nodeInd1 = modifiers.indexOf(node.getModifier());
			int nodeInd = treeModel.reverseOrder(nodeInd1);
//			System.out.printf("Index %d, Name %s\n", nodeInd, node.getModifier().getName());
			JPopupMenu popMenu = new JPopupMenu();
			SymbolModifier modifier = node.getModifier();
			if (modifier != null) {
				JMenuItem optsItem = modifier.getModifierOptionsMenu();
				if (optsItem != null) {
					popMenu.add(optsItem);
					popMenu.addSeparator();
				}
			}
			if (nodeInd > 0) {
				JMenuItem moveUp = new JMenuItem("Move earlier");
				moveUp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveNode(nodeInd, nodeInd-1);
					}
				});
				popMenu.add(moveUp);
			}
			if (nodeInd < modifiers.size()-1) {
				JMenuItem moveDn = new JMenuItem("Move later");
				moveDn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						moveNode(nodeInd, nodeInd+1);
					}
				});
				popMenu.add(moveDn);
				
			}
			popMenu.show(e.getComponent(), e.getX(), e.getY());
		}

		protected void moveNode(int nodeInd, int newInd) {
			treeModel.swapNodes(nodeInd, newInd);
//			treeModel.removeTreeModelListener(l);
//			tree.expandRow(nodeInd);
			int[] ord = treeModel.nodeOrder;
			getParams();
			treeModel = new SymbolTreeModel(symbolChooser, ord);
			setParams();
//			treeModel.nodeOrder = ord;
			tree.setModel(treeModel);
			expandTree();
		}
		
		
	}
	
	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		StandardSymbolOptions params = symbolChooser.getSymbolOptions();
		ArrayList<ModifierTreeNode> modNodes = treeModel.getRoot().modifierNodes;
		for (ModifierTreeNode node : modNodes) {
//			SymbolModifierParams modParams = params.getModifierParams(node.getModifier().getName());
			SymbolModifierParams modParams = node.getModifier().getSymbolModifierParams(); 

			node.setModBitmap(modParams.modBitMap);
		}
		expandTree();
	}

	@Override
	public boolean getParams() {
		StandardSymbolOptions params = symbolChooser.getSymbolOptions();
		params.setModifierOrder(treeModel.nodeOrder);
		// and work through every node to get it's checkbox selection
		ArrayList<ModifierTreeNode> modNodes = treeModel.getRoot().modifierNodes;
		for (ModifierTreeNode node : modNodes) {
//			SymbolModifierParams modParams = params.getModifierParams(node.getModifier().getName());
			SymbolModifierParams modParams = node.getModifier().getSymbolModifierParams(); 
			modParams.modBitMap = node.getModBitmap();
		}
		
		return true;
	}
	
}
