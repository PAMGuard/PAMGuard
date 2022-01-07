package IshmaelDetector;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.panel.PamPanel;

/**
 * Dialog to change Ishmael display settings on the spectrogram plugin.
 * 
 * @author Jamie Macaulay. 
 *
 */
public class IshDisplayDialog extends PamDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private static IshDisplayDialog singleInstance;

	/**
	 * The display parameters. 
	 */
	private IshDisplayParams ishDisplayParams;


	/**
	 * Check box for auto scaling. 
	 */
	private JCheckBox checkBoxAuto;
	
	/**
	 * Text field for the vertical scale factor. 
	 */
	private JTextField verticalScaleFactor; 

	public IshDisplayDialog(Window parentFrame) {
		super(parentFrame, "Ishmael Display Settings", false);
		
		JPanel g = new JPanel(new GridBagLayout());
		g.setBorder(new TitledBorder("Ishmael Display"));
		
		GridBagConstraints c = new GridBagConstraints(); 
	
		c.gridy = 0; 
		c.gridx = 0; 
		c.gridwidth=2;
		checkBoxAuto = new JCheckBox("Auto Scale"); 
		checkBoxAuto.addActionListener((action)->{
			verticalScaleFactor.setEnabled(!checkBoxAuto.isSelected());
		});
		PamPanel.addComponent(g, checkBoxAuto, c);


		JLabel vertLabel = new JLabel("Vertical Scale Factor"); 
		c.gridwidth=1;
		c.gridy++;
		PamPanel.addComponent(g, vertLabel, c);

		c.gridx++;
		verticalScaleFactor= new JTextField(5); 
		PamPanel.addComponent(g, verticalScaleFactor, c);

		setDialogComponent(g);

	}
	
	/**
	 * Open the dialog. 
	 * @param parentFrame - the parent frame. 
	 * @param currentParams - the current params
	 * @return the updated params from user controls. 
	 */
	public static IshDisplayParams showDialog(Frame parentFrame,
			IshDisplayParams currentParams) {
		
		if (singleInstance==null) {
			singleInstance = new IshDisplayDialog(parentFrame);
		}
		
		singleInstance.ishDisplayParams = currentParams.clone();
		singleInstance.setParams(singleInstance.ishDisplayParams);
		singleInstance.pack();
		singleInstance.setVisible(true);  //causes wait until OK/Cancel clicked
		
		singleInstance.getParams();
		return singleInstance.ishDisplayParams;
	}

	/**
	 * Set the correct params 
	 * @param ishDetParams2
	 */
	private void setParams(IshDisplayParams ishDetParams2) {
		// TODO Auto-generated method stub
		
		verticalScaleFactor.setText(String.valueOf(ishDetParams2.verticalScaleFactor));
		checkBoxAuto.setSelected(ishDetParams2.autoScale);
		
		//enable or disable the vertical scaling factor if auto scaling used. 
		verticalScaleFactor.setEnabled(!checkBoxAuto.isSelected());

	}

	@Override
	public boolean getParams() {
		try {
			ishDisplayParams.verticalScaleFactor = Double.valueOf(verticalScaleFactor.getText()); 
			ishDisplayParams.autoScale = checkBoxAuto.isSelected();  
		
		return true; 
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
