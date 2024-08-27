package PamModel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;

@SuppressWarnings("serial")
public class AboutPluginDisplay extends PamDialog {

	private CommonPluginInterface plugin;
	private static AboutPluginDisplay singleInstance;
	private JLabel name;
	private JLabel ver;
	private JLabel developer;
	private JLabel email;
	private JLabel devOn;
	private JLabel testOn;
	private JTextArea aboutText;

	private AboutPluginDisplay(Frame parentFrame, CommonPluginInterface plugin) {
		
		super(parentFrame, "About " + plugin.getDefaultName(), true);
		
		JPanel p = new JPanel(new GridBagLayout());
		p.setAlignmentY(LEFT_ALIGNMENT);
		p.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,0,0,0);  //top padding
		c.gridx=0;
		c.gridy=0;
		p.add(new JLabel("Plugin Name: "),c);
		c.gridx=1;
		c.gridy=0;
		p.add(name=new JLabel(plugin.getDefaultName()),c);
		c.gridx=0;
		c.gridy=1;
		p.add(new JLabel("Version: "),c);
		c.gridx=1;
		c.gridy=1;
		p.add(ver=new JLabel(plugin.getVersion()),c);
		c.gridx=0;
		c.gridy=2;
		p.add(new JLabel("Developer: "),c);
		c.gridx=1;
		c.gridy=2;
		p.add(developer=new JLabel(plugin.getDeveloperName()),c);
		c.gridx=0;
		c.gridy=3;
		p.add(new JLabel("Contact Information:      "),c);
		c.gridx=1;
		c.gridy=3;
		p.add(email=new JLabel(plugin.getContactEmail()),c);
		c.gridx=0;
		c.gridy=4;
		p.add(new JLabel("Developed on PAMGuard Version:      "),c);
		c.gridx=1;
		c.gridy=4;
		p.add(devOn=new JLabel(plugin.getPamVerDevelopedOn()),c);
		c.gridx=0;
		c.gridy=5;
		p.add(new JLabel("Tested on PAMGuard Version: "),c);
		c.gridx=1;
		c.gridy=5;
		p.add(testOn=new JLabel(plugin.getPamVerTestedOn()),c);
		c.gridx=0;
		c.gridy=6;
		c.insets = new Insets(10,0,0,0);  //top padding
		p.add(new JLabel("Details:"),c);
		c.gridx=0;
		c.gridy=7;
		c.gridwidth=2;
		c.insets = new Insets(2,0,0,0);  //top padding
		aboutText = new JTextArea(plugin.getAboutText());
		JScrollPane scrollPane = new JScrollPane(aboutText);
		aboutText.setEditable(false);
		aboutText.setRows(10);
		aboutText.setLineWrap(true);
		aboutText.setWrapStyleWord(true);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		p.add(scrollPane,c);
				
		setDialogComponent(p);
		this.getOkButton().setVisible(false);
		this.getDefaultButton().setVisible(false);
		this.getCancelButton().setText("Close");
	}

	public static void showDialog(Frame parentFrame, CommonPluginInterface plugin) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new AboutPluginDisplay(parentFrame, plugin);
//		}
//		singleInstance.plugin = plugin;
//		singleInstance.setParams();
		singleInstance.setVisible(true);
	}

	
	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	public void setParams() {
		name.setText(plugin.getDefaultName());
		ver.setText(plugin.getVersion());
		developer.setText(plugin.getDeveloperName());
		email.setText(plugin.getContactEmail());
		devOn.setText(plugin.getPamVerDevelopedOn());
		testOn.setText(plugin.getPamVerTestedOn());
		aboutText.setText(plugin.getAboutText());
	}

}
