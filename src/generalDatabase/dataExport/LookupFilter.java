package generalDatabase.dataExport;

import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

public class LookupFilter extends DataFilter {
	
	private LookupList lookupList;
	
	private PamTableItem tableItem;
	
	private JPopupMenu popupMenu;
	
	private JCheckBoxMenuItem[] menuItems;

	/**
	 * @param lookupList
	 */
	public LookupFilter(DataFilterChangeListener dataFilterChangeListener, LookupList lookupList, PamTableItem tableItem) {
		super(dataFilterChangeListener);
		this.lookupList = lookupList;
		this.tableItem = tableItem;
		popupMenu = new JPopupMenu();
		Vector<LookupItem> lutList = lookupList.getList();
		menuItems = new JCheckBoxMenuItem[lutList.size()];
		ButtonListener bl = new ButtonListener();
		int i = 0;
		for (LookupItem lutItem:lutList) {
			menuItems[i] = new JCheckBoxMenuItem(lutItem.getText(), true);
			menuItems[i].addActionListener(bl);
			popupMenu.add(menuItems[i]);
			i++;
		}
	}

	@Override
	public boolean filterSelectAction(MouseEvent event) {
		popupMenu.show(event.getComponent(), event.getX(), event.getY());
		return true;
	}

	@Override
	public String getFilterClause(SQLTypes sqlTypes) {
		String clause = String.format("RTRIM(%s) IN ( ", tableItem.getName());
		boolean needComma = false;
		Vector<LookupItem> lutList = lookupList.getList();
		LookupItem lutItem;
		for (int i = 0; i < lutList.size(); i++) {
			lutItem = lutList.get(i);
			if (menuItems[i].isSelected()) {
				if (needComma) {
					clause += ",";
				}
				clause += String.format("'%s'", lutItem.getCode());
				needComma = true;
			}
		}
		clause += ")";
		if (needComma == false) {
			// mitigate against empty list, which will crash SQL !
			clause = String.format(" %s IN ('No %s data selected') ", tableItem.getName(), tableItem.getName());
		}
		return clause;
	}

	@Override
	public String getColumnName() {
		return tableItem.getName();
	}
	
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			filterChanged();
		}
	}

}
