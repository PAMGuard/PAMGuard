package PamModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamController;
import PamUtils.PamCalendar;

/**
 * Some extra functions for SMRU development team
 * @author dg50
 *
 */
public class SMRUDevFunctions {
	
	private static SMRUDevFunctions smruDevFunctions;
	
	private String helpRoot = null;

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
		
		// possible links into module names in table. 
		// for a dump to go into the main help file, use helpRoot = "../../../";
		//helpRoot = "../../../";
		// for no output, set null. 
		helpRoot = null;
		// for ooutput into main PAMGuard pages, it's probably
		
		PamModel model = PamController.getInstance().getModelInterface();
		ArrayList<PamModuleInfo> moduleList = PamModuleInfo.getModuleList();
		/**
		 * Don't sort. Need to keep groups together and in order, but accept they may not be well ordered. . 
		 */
		ArrayList<ArrayList<PamModuleInfo>> groupedInfos = new ArrayList<>();
		for (PamModuleInfo aM : moduleList) {
			if (aM.isHidden()) {
				continue;
			}
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

		System.out.println(" ");
		System.out.printf("Last updated %s\n\n", PamCalendar.formatDate(System.currentTimeMillis()));
		for (int g = 0; g < groupedInfos.size(); g++) {
			 ArrayList<PamModuleInfo> group = groupedInfos.get(g);
			PamModuleInfo firstMod = group.get(0);
			ModulesMenuGroup modGroup = firstMod.getModulesMenuGroup();
			String groupName = modGroup.getMenuName();
			String linkName = "_" + groupName;
			linkName = linkName.replace(" ", "_");
//			System.out.printf("[%s](#%s) (%d modules)\n", groupName, linkName, group.size());
			System.out.printf("[%s](#%s)", groupName, linkName, group.size());
			if (g < groupedInfos.size()-1) {
				System.out.printf(",\n");
			}
			else {
				System.out.printf("\n\n");
			}
			
		}
		int n = 0;
		for (int g = 0; g < groupedInfos.size(); g++) {
			 ArrayList<PamModuleInfo> group = groupedInfos.get(g);
			 n += group.size();
			PamModuleInfo firstMod = group.get(0);
			ModulesMenuGroup modGroup = firstMod.getModulesMenuGroup();
			System.out.println(" ");
			if (modGroup != null) {
				String groupName = modGroup.getMenuName();
				String linkName = "_" + groupName;
				linkName = linkName.replace(" ", "_");
				System.out.printf("<a name=\"%s\"></a>\n", linkName);
				System.out.printf("\n## %s (%d modules)\n", modGroup.getMenuName(), group.size());
			}
			System.out.println(" ");
			System.out.println("| Module | Number | Function |");
			System.out.println("|------|---|-------|");
			for (int m = 0; m < group.size(); m++) {
				 PamModuleInfo aM = group.get(m);
				 
				 String name = aM.getDefaultName();
				 String hp = aM.getHelpPoint();
				 if (helpRoot != null && hp != null) {
					 name = String.format("[%s](%s%s)", name, helpRoot, hp);
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
				 
				System.out.printf("| %s | %s | %s |\n", name, instances, aM.getToolTipText());
			}
		}
		
		System.out.printf("\n\n\nTotal modules exported = %d\n", n);
	}


}
