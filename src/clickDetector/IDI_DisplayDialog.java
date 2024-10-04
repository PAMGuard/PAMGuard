package clickDetector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import PamUtils.PamFileChooser;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class IDI_DisplayDialog extends PamDialog  implements ItemListener, ActionListener {

	private static IDI_DisplayDialog singleInstance;
	
	private IDI_DisplayParams idiParams;
	
	private IDI_Display idiDisplay;
	
    /** Scale of Inter-Detection-Interval (IDI) bins in the high-resolution
     * display, in milliseconds */
    private JTextField highResIdiBinScale;

    /** Scale of Inter-Detection-Interval (IDI) bins in the low-resolution
     * display, in milliseconds */
    private JTextField lowResIdiBinScale;

    /** Size of time bins, in milliseconds */
    private JTextField timeBinScale;

    /** maximum IDI bin value in high-resolution display, in milliseconds */
    private JTextField maxHighResBin;

    /** maximum IDI bin value in low-resolution display, in milliseconds */
    private JTextField maxLowResBin;

    /** highest histogram count in the high-resolution colour bar */
    private JTextField maxHighCount;

    /** highest histogram count in the low-resolution colour bar */
    private JTextField maxLowCount;

    /** maximum time bin value, in milliseconds */
    private JTextField maxTimeBin;

    /** checkbox to select output file */
    private JCheckBox outputButton;

    /** textbox displaying the output filename, including path */
    JTextField outputDirTxt;

    /** Jbutton for selecting output file */
    JButton outputDirectoryButton;

	/**
     * Constructor
     *
     * @param parentFrame
     * @param pt
     */
	private IDI_DisplayDialog(Window parentFrame, Point pt) {
		super(parentFrame, "Histogram Display", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        /* high-res histogram parameters */
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("High-Res Histogram"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
        c.insets = new Insets(0, 10, 0, 0);
		c.gridwidth = 1;
		addComponent(p, new JLabel("Bin Scale "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(p, highResIdiBinScale = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" ms/bin"), c);
        c.insets.left = 10;
        c.gridx=0;
        c.gridy++;
		addComponent(p, new JLabel("Max Value  "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(p, maxHighResBin = new JTextField(4), c);
		c.gridx++;
		addComponent(p, new JLabel(" ms"), c);
        c.insets.left = 10;
        c.gridx=0;
        c.gridy++;
		addComponent(p, new JLabel("Max Counts  "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(p, maxHighCount = new JTextField(3), c);
		c.gridx++;
		addComponent(p, new JLabel(" counts"), c);
		mainPanel.add(p);
		
        /* low-res histogram parameters */
		JPanel r = new JPanel();
		r.setBorder(new TitledBorder("Low-Res Histogram"));
		r.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
        c.insets = new Insets(0, 10, 0, 0);
		c.gridwidth = 1;
		addComponent(r, new JLabel("Bin Scale "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(r, lowResIdiBinScale = new JTextField(3), c);
		c.gridx++;
		addComponent(r, new JLabel(" ms/bin"), c);
        c.insets.left = 10;
        c.gridx=0;
        c.gridy++;
		addComponent(r, new JLabel("Max Value  "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(r, maxLowResBin = new JTextField(4), c);
		c.gridx++;
		addComponent(r, new JLabel(" ms"), c);
        c.insets.left = 10;
        c.gridx=0;
        c.gridy++;
		addComponent(r, new JLabel("Max Counts  "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(r, maxLowCount = new JTextField(3), c);
		c.gridx++;
		addComponent(r, new JLabel(" counts"), c);
		mainPanel.add(r);

        /* time parameters */
		JPanel q = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		q.setBorder(new TitledBorder("Time Parameters"));
        c.insets = new Insets(0, 10, 0, 0);
		c.gridwidth = 1;
		addComponent(q, new JLabel("Bin Scale "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(q, timeBinScale = new JTextField(3), c);
		c.gridx++;
		addComponent(q, new JLabel(" s/bin"), c);
        c.insets.left = 10;
        c.gridx=0;
        c.gridy++;
		addComponent(q, new JLabel("Max Value  "), c);
        c.insets.left = 0;
		c.gridx++;
		addComponent(q, maxTimeBin = new JTextField(4), c);
		c.gridx++;
		addComponent(q, new JLabel(" minutes"), c);
		mainPanel.add(q);

        /* output file */
		JPanel s = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		s.setBorder(new TitledBorder("Output Parameters"));
        c.insets = new Insets(0, 10, 0, 0);
		c.gridwidth = 1;
		addComponent(s, outputButton = new JCheckBox("Output to File"), c);
        outputButton.setSelected(false);
        outputButton.addItemListener(this);
        c.gridwidth = 2;
		c.gridy++;
        addComponent(s, outputDirTxt = new JTextField(30), c);
        outputDirTxt.setEditable(true);
        outputDirTxt.setEnabled(false);
        c.gridwidth = 1;
        c.gridx++;
		c.gridy++;
        c.insets.left = 0;
        addComponent(s, outputDirectoryButton = new JButton("Select File"),c);
        outputDirectoryButton.addActionListener(this);
        outputDirectoryButton.setEnabled(false);
		mainPanel.add(s);

		
		setDialogComponent(mainPanel);
		if (pt != null) {
			setLocation(pt);
		}
	}
	
	public static IDI_DisplayParams showDialog(Window frame, Point pt,
			IDI_DisplayParams idiParams) {
		if (singleInstance == null ||  singleInstance.getOwner() != frame) {
			singleInstance = new IDI_DisplayDialog(frame, pt);
		}
		singleInstance.idiParams = idiParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.idiParams;
	}

    /**
     * Set the parameters in the GUI, based on the passed IDI_DisplayParams object
     */
	private void setParams() {
		highResIdiBinScale.setText(String.format("%d", idiParams.getHighResIdiBinScale()));
        maxHighResBin.setText(String.format("%d", idiParams.getMaxHighResBin()));
        maxHighCount.setText(String.format("%d", idiParams.getMaxHighCount()));
        lowResIdiBinScale.setText(String.format("%d", idiParams.getLowResIdiBinScale()));
        maxLowResBin.setText(String.format("%d", idiParams.getMaxLowResBin()));
        maxLowCount.setText(String.format("%d", idiParams.getMaxLowCount()));
        timeBinScale.setText(String.format("%d", idiParams.getTimeBinScale()/1000));
        maxTimeBin.setText(String.format("%d", idiParams.getMaxTimeBin()/60/1000));
        outputButton.setSelected(idiParams.isOutputSaved());
        
        try {
            outputDirTxt.setText(idiParams.getOutputFilename().getAbsolutePath());
//            String dirString = idiParams.getOutputFilename().getAbsolutePath();
//            int dirLen = dirString.length();
//            String dirSubString;
//            if (dirLen > 30) {
//                dirSubString = "..." + dirString.substring(dirString.length()-30);
//            } else {
//                dirSubString = dirString;
//            }
//            outputDirTxt.setText(dirSubString);
        } catch (NullPointerException ex) {
            outputDirTxt.setText("C:\\");
        }
	}

    /**
     * Set the IDI_DisplayParams parameters, based on what the user has entered
     * into the GUI
     *
     * @return
     */
	@Override
	public boolean getParams() {
		try {
            idiParams.setHighResIdiBinScale(Integer.parseInt(highResIdiBinScale.getText()));
            idiParams.setMaxHighResBin(Integer.parseInt(maxHighResBin.getText()));
            idiParams.setMaxHighCount(Integer.parseInt(maxHighCount.getText()));
            idiParams.setLowResIdiBinScale(Integer.parseInt(lowResIdiBinScale.getText()));
            idiParams.setMaxLowResBin(Integer.parseInt(maxLowResBin.getText()));
            idiParams.setMaxLowCount(Integer.parseInt(maxLowCount.getText()));
            idiParams.setTimeBinScale(Integer.parseInt(timeBinScale.getText())*1000);
            idiParams.setMaxTimeBin(Integer.parseInt(maxTimeBin.getText())*60*1000);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid value");
		}

        /* if any of the values are 0 or negative, the product would show that.
         * must cast one of the values to a double first, though, because an
         * int would overflow
         */
        double prod = ((double) idiParams.getHighResIdiBinScale()) *
                idiParams.getMaxHighResBin() * 
                idiParams.getMaxHighCount() *
                idiParams.getLowResIdiBinScale() *
                idiParams.getMaxLowResBin() *
                idiParams.getMaxLowCount() *
                idiParams.getTimeBinScale() *
                idiParams.getMaxTimeBin();
        if (prod<=0) {
			return showWarning("All values must be greater than zero");
		}

        /* try to save the output filename */
        try {
            idiParams.setOutputFilename(new File(outputDirTxt.getText()));
            idiParams.setSaveOutput(outputButton.isSelected());
        }
        catch (Exception e) {
            return showWarning("Error creating save file");
        }

		return true;
	}

    /**
     * Selects/Deselects the checkbox to save the output to file
     * 
     * @param e
     */
    @Override
	public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            outputDirTxt.setEnabled(true);
            outputDirectoryButton.setEnabled(true);
            idiParams.setSaveOutput(true);
        } else {
            outputDirTxt.setEnabled(false);
            outputDirectoryButton.setEnabled(false);
            idiParams.setSaveOutput(false);
        }
    }

    /**
     * Starts the open file dialog
     *
     * @param e
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        String currFile = outputDirTxt.getText();
		JFileChooser fileChooser = new PamFileChooser();
        fileChooser.setDialogTitle("Select output file...");
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new csvFileFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (currFile != null) fileChooser.setSelectedFile(new File(currFile));
		int state = fileChooser.showSaveDialog(outputDirTxt);
		if (state == JFileChooser.APPROVE_OPTION) {
			currFile = fileChooser.getSelectedFile().getAbsolutePath();
            idiParams.setOutputFilename(fileChooser.getSelectedFile());
		}
        outputDirTxt.setText(idiParams.getOutputFilename().getAbsolutePath());
    }

    /**
     * filter for csv files
     */
    private class csvFileFilter extends FileFilter {
        //Accept all directories and csv files.
        @Override
		public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("csv") ||
                    extension.equals("CSV")) {
                        return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }

        //The description of this filter
        @Override
		public String getDescription() {
            return "Comma-Separated File (*.csv)";
        }
    }

	@Override
	public void cancelButtonPressed() {
		idiParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
	}


}
