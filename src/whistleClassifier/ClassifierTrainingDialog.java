package whistleClassifier;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import PamUtils.FolderChangeListener;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;

public class ClassifierTrainingDialog extends PamDialog {

	private WhistleClassificationParameters whistleClassificationParameters;
	
	private static ClassifierTrainingDialog singleInstance;
	
	private WhistleClassifierControl whistleClassifierControl; 
	
	private SelectFolder selectFolder;
	
	private JCheckBox useFolderNames;
	
	private DataViewPanel dataViewPanel;
	
	public ClassifierTrainingDialog(Window parentFrame, String title) {
		super(parentFrame, title, false);
		
		JPanel p = new JPanel(new BorderLayout());
		JPanel f = new JPanel(new BorderLayout());
		f.setBorder(new TitledBorder("Training data soure"));
		selectFolder = new SelectFolder("Select Folder", 50, true);
		selectFolder.addFolderChangeListener(new FolderChanged());
		f.add(BorderLayout.CENTER, selectFolder.getFolderPanel());
		f.add(BorderLayout.SOUTH, useFolderNames = new JCheckBox("Use folder names as species names" +
				" (this will override the species names in the files)"));
		useFolderNames.addActionListener(new UseFolderNamesChange());
		p.add(BorderLayout.NORTH, f);
		
		dataViewPanel = new DataViewPanel();
		p.add(BorderLayout.CENTER, dataViewPanel);
		
		setDialogComponent(p);
	}
	
	public static WhistleClassificationParameters showDialog(Frame parentFrame, WhistleClassifierControl wc) {
		if (singleInstance == null) {
			singleInstance = new ClassifierTrainingDialog(parentFrame, "Classifier training");
		}
		singleInstance.whistleClassifierControl = wc;
		singleInstance.whistleClassificationParameters = wc.getWhistleClassificationParameters().clone();
		
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return null;
	}
	
	TrainingDataCollection trainingDataCollection;
	private void createTrainingStore() {
		trainingDataCollection = new TrainingDataCollection(whistleClassifierControl);
		trainingDataCollection.loadTrainingData(whistleClassificationParameters.trainingDataFolder, 
				selectFolder.isIncludeSubFolders(), useFolderNames.isSelected());
		trainingDataCollection.dumpStoreContent();
		if (dataViewPanel != null) {
			dataViewPanel.refill();
		}
	}
	
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}
	
	private void setParams() {
		selectFolder.setFolderName(whistleClassificationParameters.trainingDataFolder);
		useFolderNames.setSelected(whistleClassificationParameters.trainingDataFolderAsSpecies);
		createTrainingStore();
	}

	@Override
	public boolean getParams() {
		whistleClassificationParameters.trainingDataFolder = selectFolder.getFolderName(false);
		whistleClassificationParameters.trainingDataFolderAsSpecies = useFolderNames.isSelected();
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Show all the data, listed by species in a treeview. 
	 */
	class DataViewPanel extends JPanel {
		
		private JScrollPane scrollPane;
		
		private JTree dataTree;
		
		private TreeDataModel treeDataModel;
		
		private DefaultMutableTreeNode topNode;
		
		private DataViewPanel() {
			
			 topNode =
		        new DefaultMutableTreeNode("Training Data");
//			 new TreeDataModel()

			dataTree = new JTree(treeDataModel = new TreeDataModel());
			scrollPane = new JScrollPane(dataTree);
			this.setLayout(new BorderLayout());
			setBorder(new TitledBorder("Training data"));
			add(BorderLayout.CENTER, scrollPane);
			setPreferredSize(new Dimension(200,200));
			
			fillData();
		}
		
		protected void refill() {
			dataTree.setModel(treeDataModel = new TreeDataModel());
		}
		
		class TreeDataModel implements TreeModel {
			
//			JCheckBox[] boxes = new JCheckBox[3];

			public TreeDataModel() {
//				for (int i = 0; i < 3; i++) {
//					boxes[i] = new JCheckBox("Box " + i);
//				}
			}

			@Override
			public void addTreeModelListener(TreeModelListener l) {
				
			}

			@Override
			public Object getChild(Object parent, int index) {
				if (parent == null) {
					return topNode;
				}
				else if (parent == topNode){
					if (trainingDataCollection == null) {
						return 0;
					}
					else {
						return trainingDataCollection.getTrainingDataGroup(index);
					}
				}
				else if (parent.getClass() == TrainingDataGroup.class) {
					return ((TrainingDataGroup) parent).getDataSet(index);
				}
				return null;
			}

			@Override
			public int getChildCount(Object parent) {
				if (trainingDataCollection == null) {
					return 0;
				}
				if (parent == topNode) {
					return trainingDataCollection.getNumTrainingGroups();
				}
				else if (parent.getClass() == TrainingDataGroup.class) {
					return ((TrainingDataGroup) parent).getNumDataSets();
				}
				return 0;
			}

			@Override
			public int getIndexOfChild(Object parent, Object child) {
				return 0;
			}

			@Override
			public Object getRoot() {
				return topNode;
			}

			@Override
			public boolean isLeaf(Object node) {
				return (node.getClass() == TrainingDataSet.class);
			}

			@Override
			public void removeTreeModelListener(TreeModelListener l) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void valueForPathChanged(TreePath path, Object newValue) {
				// TODO Auto-generated method stub
				
			}
			
		}
		
		protected void fillData() {
			topNode.add(new JTree.DynamicUtilTreeNode(new JCheckBox("test box"), null));
		}
	}
	
//	class DataTreeModel extends 
	

	class FolderChanged implements FolderChangeListener {
		@Override
		public void folderChanged(String newFolder, boolean subFolders) {
			if (getParams()) {
				createTrainingStore();
			}
		}
	}
	
	class UseFolderNamesChange implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (getParams()) {
				createTrainingStore();
			}
		}
	}
	
}
