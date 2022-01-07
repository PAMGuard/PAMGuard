package GPS;

import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Simple dialog asking the user to input a date for GGA string input. GGA strings contain a time but no date. If nmea data contains both GGA and RMC strings then the user can select to ignore the GGA strings and just use the RMC data. 
 * @author Jamie Macaulay
 *
 */
public class ImportGGADialog  extends PamDialog{
	
	private	static ImportGGADialog singleInstance; 
	private ImportGPSParams params;
	private Window frame;
	private ImportGPSData importGPSData;
	private JTextField year;
	private JTextField month;
	private JTextField day;
	private JCheckBox checkBox; 


	public ImportGGADialog(Window parentFrame, Point pt) {
		super(parentFrame, "GGA Data Detected", false);
		
		PamGridBagContraints c=new PamGridBagContraints();
		
		JPanel p= new JPanel(new GridBagLayout());
		p.setBorder(new TitledBorder("GGA Data"));
		c.gridx = 0;
		c.gridwidth = 3;
		JLabel text= new JLabel("Select date to use GPGGA");
		addComponent(p,text,c);
		c.gridy=c.gridy+10;
		c.gridx=0;
		c.gridwidth = 1;
		c.gridheight = 1;
		addComponent(p, new JLabel("Year"),c);
		c.gridx=1;
		addComponent(p, year=new JTextField(3),c);
		c.gridy++;
		c.gridx=0;
		addComponent(p, new JLabel("Month"),c);
		c.gridx++;
		addComponent(p, month=new JTextField(3),c);
		c.gridy++;
		c.gridx=0;
		addComponent(p, new JLabel("Day"),c);
		c.gridx++;
		addComponent(p, day=new JTextField(3),c);
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 3;
		addComponent(p,checkBox=new JCheckBox("Use only GPRMC strings") ,c);
		checkBox.addActionListener(new DisableDate());
		//setPreferredSize(new Dimension(200,200));
		//setModalityType(ModalityType.DOCUMENT_MODAL);
		setDialogComponent(p);
		
		
	}
	
	class DisableDate implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			year.setEnabled(!checkBox.isSelected());
			month.setEnabled(!checkBox.isSelected());
			day.setEnabled(!checkBox.isSelected());
		}
		
	}
	
	
	
	public static ImportGPSParams showDialog(Window frame, Point pt, ImportGPSParams params, ImportGPSData importGPSData ){
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new ImportGGADialog(frame, pt);
		}
		singleInstance.params = params.clone();
		singleInstance.frame=frame;
		singleInstance.importGPSData=importGPSData;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	
	public boolean setParams() {
		
		year.setText(String.format("%d",params.year));
		month.setText(String.format("%d",(params.month+1)));
		day.setText(String.format("%d",params.day));
		checkBox.setSelected(!params.useGGA);
		
		return true;
	}
	
	@Override
	public boolean getParams() {
		
		try{
			
			int intYear=Integer.valueOf(year.getText());
			int intMonth=Integer.valueOf(month.getText())-1;
			int intDay=Integer.valueOf(day.getText());
			
			params.year=intYear;
			params.month=intMonth; 
			params.day=intDay;
		}
		catch(Exception e){
			return showWarning("Invalid year, month or day entry. Each field must be filled with an integer");
		}

		params.useGGA=!checkBox.isSelected();
		
		return true;
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
