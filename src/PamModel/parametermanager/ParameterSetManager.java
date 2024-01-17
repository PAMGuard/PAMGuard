package PamModel.parametermanager;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import PamModel.parametermanager.swing.ManagedParameterDialog;
import generalDatabase.parameterstore.ParameterDatabaseStore;

/**
 * Just about everything giving overall control of some managed parameters. 
 * May be a bit too specific on first cut and need to be abstracted. 
 * Testing on 'Deployment' data. 
 * @author dg50
 *
 * @param <T>
 */
public class ParameterSetManager<T extends ManagedParameters> {
	
	private T managedParams;
	private String name;

	public ParameterSetManager(T defaultParams, String name) {
		setManagedParams(defaultParams);
		this.name = name;
//		if (managedParams == null) {
//			managedParams = new T();
//		}
	}

	/**
	 * @return the managedParams
	 */
	public T getManagedParams() {
		return managedParams;
	}

	/**
	 * @param managedParams the managedParams to set
	 */
	public void setManagedParams(T managedParams) {
		this.managedParams = managedParams;
	}
	
	public JMenuItem getMenuItem(Window parent) {
		if (managedParams == null) {
			return null;
		}
		JMenuItem menuItem = new JMenuItem(name + " ...");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog(parent);
			}
		});
		
		return menuItem;
	}

	protected void showDialog(Window parent) {
		ManagedParameterDialog<T> dialog = new ManagedParameterDialog<T>(parent, name, managedParams);
		T newParams = dialog.showDialog(parent, name, managedParams);

		if (newParams != null) {
		ParameterDatabaseStore paramDatabase = new ParameterDatabaseStore("MetaData");
		paramDatabase.saveParameterSet(newParams);
		}
	}

}
