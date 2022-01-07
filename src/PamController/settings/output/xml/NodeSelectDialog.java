package PamController.settings.output.xml;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;

public class NodeSelectDialog extends PamDialog {
	
	private int selectionIndex = -1;
	
	private JCheckBox[] buttons;

	private NodeSelectDialog(Window parentFrame, String title, String[] types, String[] names) {
		super(parentFrame, "Import Settings", false);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder(title));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(new JLabel("Multiple settings available"));
		topPanel.add(new JLabel("Select " + title + " to import... "));
		JPanel cenPanel = new JPanel();
		cenPanel.setLayout(new BoxLayout(cenPanel, BoxLayout.Y_AXIS));
		buttons = new JCheckBox[types.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JCheckBox(types[i] + "; " + names[i]);
			bg.add(buttons[i]);
			cenPanel.add(buttons[i]);
		}
		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, cenPanel);
		setDialogComponent(mainPanel);
		
	}

	@Override
	public boolean getParams() {
		selectionIndex = -1;
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].isSelected()) {
				selectionIndex = i;
				break;
			}
		}
		return (selectionIndex >= 0);
	}

	@Override
	public void cancelButtonPressed() {
		selectionIndex = -1;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	public static int showDialog(Window parent, Class objectClass, String[] types, String[] names) {
		NodeSelectDialog nsDialog = new NodeSelectDialog(parent, objectClass.getName(), types, names);
		
		nsDialog.setVisible(true);
		
		return nsDialog.selectionIndex;
		
	}

}
