package PamModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import PamView.help.PamHelp;

public class DeprecatedModuleDialog extends PamDialog {
	
	private JCheckBox acknowledge;
	
	private JButton helpButton;
	
	private String newModulehelpPoint;

	private DeprecatedModuleDialog(Window parentFrame, String moduleName, String replacementModule,
			String newModuleHelp) {
		super(parentFrame, moduleName, false);
		
		JTextPane msgText = new JTextPane();
		msgText.setPreferredSize(new Dimension(460, 160));
		msgText.setEditable(false);
		msgText.setBorder(new EmptyBorder(20,20,20,0));
		JScrollPane scrollPane = new JScrollPane(msgText);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new EmptyBorder(10, 5, 0, 5));
		p.add(BorderLayout.CENTER, scrollPane);
		
		msgText.setText(String.format("The %s you are creating has been replaced by the " +
				"\n%s and may not be supported in future PAMGUARD releases\n\n" +
				"You are strongly advised to reconfigure PAMGUARD to use the %s instead", 
				moduleName, replacementModule, replacementModule));
		
		JPanel p2 = new JPanel();
		p2.setBorder(new EmptyBorder(20, 0, 20, 0));
		p2.setLayout(new BoxLayout(p2,BoxLayout.Y_AXIS));
		p2.add(acknowledge = new JCheckBox("Don't show this message again"));
		if (newModuleHelp != null) {
			p2.add(helpButton = new JButton(replacementModule + " Help"));
			newModulehelpPoint = newModuleHelp;
			helpButton.addActionListener(new NewModuleHelp());
		}
		p.add(BorderLayout.SOUTH, p2);
		
		getCancelButton().setVisible(false);

		setDialogComponent(p);
	}
	
	public static boolean showDialog(Window owner, String moduleName, String replacementModule, String helpPoint) {
		DeprecatedModuleDialog dmd = new DeprecatedModuleDialog(owner, moduleName, replacementModule, helpPoint);
		dmd.setVisible(true);
		return dmd.acknowledge.isSelected();
	}
	
	class NewModuleHelp implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			PamHelp.getInstance().displayContextSensitiveHelp(newModulehelpPoint);
		}
		
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
