/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package PamguardMVC.blockprocess;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamView.dialog.PamDialog;


/**
 * @author mo55
 *
 */
public class PamBlockParamsPanel {

	private JPanel mainPanel;
	
	private PamBlockParams blockParams;
	
	private JTextField txtBlockLen;
	
	private JComboBox<String> comboMode;
	
	private JLabel blockLbl;

	
	/**
	 * Create the panel.
	 */
	public PamBlockParamsPanel() {
		createPanel();
	}

	/**
	 * 
	 */
	private void createPanel() {
		this.mainPanel = new JPanel();
		mainPanel.setBorder(null);
		GridBagLayout gbl_mainPanel = new GridBagLayout();
		gbl_mainPanel.rowWeights = new double[]{0.0, 0.0,0.0,1.0};
//		gbl_mainPanel.columnWeights = new double[]{1.0,0.0,0.0};
		mainPanel.setLayout(gbl_mainPanel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(15, 10, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainPanel.add(new JLabel("Block Length ", JLabel.RIGHT), gbc);
		gbc.gridx++;
		gbc.insets = new Insets(15, 0, 5, 5);
		gbc.fill = GridBagConstraints.NONE;
		txtBlockLen = new JTextField(12);
		mainPanel.add(txtBlockLen, gbc);
		gbc.gridx++;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(15, 0, 5, 10);
		mainPanel.add(new JLabel(" samples", JLabel.LEFT), gbc);

		// block modes
		comboMode = new JComboBox<>();
		ComboboxToolTipRenderer renderer = new ComboboxToolTipRenderer();
		comboMode.setRenderer(renderer);
		List<String> toolTips = new ArrayList<String>();
		for (BlockMode aMode : BlockMode.values()) {
			comboMode.addItem(aMode.name());
			toolTips.add(aMode.toString());
		}
		renderer.setTooltips(toolTips);
		comboMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BlockMode theMode = BlockMode.valueOf((String) comboMode.getSelectedItem());
				blockLbl.setText(theMode.toString());
			}
		});
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 10, 10, 5);
		gbc.gridx = 0;
		gbc.gridy++;
		mainPanel.add(new JLabel("Block Type ", JLabel.RIGHT), gbc);
		gbc.gridx++;
		gbc.insets = new Insets(5, 0, 10, 10);
		gbc.fill = GridBagConstraints.NONE;
		mainPanel.add(comboMode,gbc);
		
		// label explaining the type of blocking selected
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 10, 10, 10);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth=3;
		blockLbl = new JLabel(BlockMode.NONE.toString());
		mainPanel.add(blockLbl,gbc);
		
		// blank space at the bottom
		gbc.gridx=0;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.VERTICAL;
		mainPanel.add(Box.createVerticalStrut(10),gbc);
		
	}
	
	
	/**
	 * return the panel object 
	 */
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	/**
	 * Set the values in the dialog to match the parameters
	 * 
	 * @param pamBlockParams the parameters to use
	 */
	public void setParams(PamBlockParams pamBlockParams) {
		this.blockParams = pamBlockParams;
		txtBlockLen.setText(String.format("%d", pamBlockParams.blockLengthMillis));
		comboMode.setSelectedItem(pamBlockParams.blockMode.name());
		blockLbl.setText(pamBlockParams.blockMode.toString());
	}

	/**
	 * Take the current parameters from the dialog and put them back into the parameters object
	 * 
	 * @return
	 */
	public PamBlockParams getParams() {
		try {
			Long blockLenVal = Long.valueOf(txtBlockLen.getText());
			if (blockLenVal<0) {
				PamDialog.showWarning(null, "Invalid block length",
						"Block Length must be greater than 0");
				return null;
			}
			blockParams.blockLengthMillis = blockLenVal;
		}
		catch (NumberFormatException e) {
			PamDialog.showWarning(null, "Invalid block length",
					"Block Length must be a number greater than 0");
			return null;
		}
		blockParams.blockMode = BlockMode.valueOf((String) comboMode.getSelectedItem());
		
		return blockParams;
	}
		
	/**
	 * Custom renderer to display tooltips for each of the combobox items
	 * @author mo55
	 *
	 */
	public class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		/** List of tooltips */
		List<String> tooltips;

	    @Override
	    public Component getListCellRendererComponent(JList list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	        JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (-1 < index && null != value && null != tooltips) {
	            list.setToolTipText(tooltips.get(index));
	        }
	        return comp;
	    }

	    public void setTooltips(List<String> tooltips) {
	        this.tooltips = tooltips;
	    }
	}
	

}
