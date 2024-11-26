package PamModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamController;

/**
 * Some extra functions for SMRU development team
 * @author dg50
 *
 */
public class SMRUDevFunctions {
	
	private static SMRUDevFunctions smruDevFunctions;

	private SMRUDevFunctions() {
		// TODO Auto-generated constructor stub
	}

	public static SMRUDevFunctions getDevFuncs() {
		if (smruDevFunctions == null) {
			smruDevFunctions = new SMRUDevFunctions();
		}
		return smruDevFunctions;
	}
	
	public void addFileMenuFuncs(JMenu fileMenu) {
		if (SMRUEnable.isDevEnable() == false) {
			return;
		}
		JMenuItem expModules = new JMenuItem("Dump modules table");
		expModules.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dumpModulesTable();
			}

		});
		fileMenu.add(expModules);
	}

	/**
	 * Dump a list of modules into a markdown formatted file. 
	 */
	private void dumpModulesTable() {
		PamModel model = PamController.getInstance().getModelInterface();
		ArrayList<PamModuleInfo> moduleList = PamModuleInfo.getModuleList();
		/**
		 * Don't sort. Need to keep groups together and in order, but accept they may not be well ordered. . 
		 */
		ArrayList<ArrayList<PamModuleInfo>> groupedInfos = new ArrayList<>();
		for (PamModuleInfo aM : moduleList) {
			ArrayList<PamModuleInfo> currGroup = null;
			for (int i = 0; i < groupedInfos.size(); i++) {
				if (groupedInfos.get(i).get(0).getModulesMenuGroup() == aM.getModulesMenuGroup()) {
					currGroup = groupedInfos.get(i);
					currGroup.add(aM);
					break;
				}
			}
			if (currGroup == null) {
				currGroup = new ArrayList<>();
				currGroup.add(aM);
				groupedInfos.add(currGroup);
			}
		}
		for (int g = 0; g < groupedInfos.size(); g++) {
			 ArrayList<PamModuleInfo> group = groupedInfos.get(g);
			PamModuleInfo firstMod = group.get(0);
			ModulesMenuGroup modGroup = firstMod.getModulesMenuGroup();
			System.out.println(" ");
			if (modGroup != null) {
				System.out.println("## " + modGroup.getMenuName());
			}
			System.out.println(" ");
			System.out.println("| Module | Number | Function |");
			System.out.println("|------|---|-------|");
			for (int m = 0; m < group.size(); m++) {
				 PamModuleInfo aM = group.get(m);
				 if (aM.getDefaultName().startsWith("NMEA")) {
					 int r = 5+6;
				 }
				 int minN = aM.getMinNumber();
				 int maxN = aM.getMaxNumber();
				 String instances = "Any";
				 if (minN == maxN && minN > 0) {
					 instances = "Exactly " + minN;
				 }
				 else if (minN == 0 && maxN > 0) {
					 instances = "Up to " + maxN;
				 }
				 else if (minN > 0 && maxN == 0) {
					 instances = "At least " + maxN;
				 }
				 
				System.out.printf("| %s | %s | %s |\n", aM.getDefaultName(), instances, aM.getToolTipText());
			}
		}
		
	}


}
