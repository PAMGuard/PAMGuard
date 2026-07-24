package NMEA;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;

public class NMEADialog2 extends PamDialog {
	
	private static final long serialVersionUID = 1L;

	private static NMEADialog2 singleInstance;
	
	private NMEAControl nmeaControl;
	
	private boolean cancelled;
	
	private JComboBox<String> sourceTypes;
	
	private JPanel mainPanel;
	
	private JPanel sPanel;

	private ArrayList<NMEAProvider> providers;

	private PamDialogPanel component;

	private NMEADialog2(Window parentFrame, NMEAControl nmeaControl) {
		super(parentFrame, nmeaControl.getUnitName(), false);
		this.nmeaControl = nmeaControl;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel nPanel = new JPanel(new BorderLayout());
		sPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, nPanel);
		mainPanel.add(BorderLayout.CENTER, sPanel);
		sourceTypes = new JComboBox<String>();
		providers = nmeaControl.getNmeaProviders();
		for (NMEAProvider type : providers) {
			sourceTypes.addItem(type.getName());
		}
		nPanel.setBorder(new TitledBorder("NMEA Source"));
		nPanel.add(BorderLayout.CENTER, sourceTypes);
		
		sourceTypes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newSourceType();
			}
		});
		
		setDialogComponent(mainPanel);
	}
	
	protected void newSourceType() {
		sPanel.removeAll();
		NMEAProvider selProvider = getSelectedProvider();
		if (selProvider == null) {
			return;
		}
		component = selProvider.getDialogPanel(this);
		if (component != null) {
			sPanel.add(BorderLayout.CENTER, component.getDialogComponent());
			component.setParams();
		}
		pack();
	}
	
	private NMEAProvider getSelectedProvider() {
		int ind = sourceTypes.getSelectedIndex();
		if (ind < 0 || ind >= providers.size()) {
			return null;
		}
		NMEAProvider selProvider = providers.get(ind);
		return selProvider;
		
	}

	public static boolean showDialog(Window parent, NMEAControl nmeaControl) {
//		if (singleInstance == null || singleInstance.nmeaControl != nmeaControl) {
			singleInstance = new NMEADialog2(parent, nmeaControl);
//		}
		singleInstance.setParams();
	
		singleInstance.setVisible(true);
		
		return !singleInstance.cancelled;
	}

	private void setParams() {
		
		String provClass = nmeaControl.getNmeaParameters().getProviderClass();
		for (int i = 0; i < providers.size(); i++) {
			if (providers.get(i).getClass().getName().equals(provClass)) {
				sourceTypes.setSelectedIndex(i);
				break;
			}
		}
		
		newSourceType();
	}

	@Override
	public boolean getParams() {
		boolean ok = true;
		if (component != null) {
			ok = component.getParams();
		}
		NMEAProvider prov = getSelectedProvider();
		if (prov != null) {
			nmeaControl.getNmeaParameters().setProviderClass(prov.getClass());
		}
		
		return ok & prov != null;
	}

	@Override
	public void cancelButtonPressed() {
		cancelled = true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
