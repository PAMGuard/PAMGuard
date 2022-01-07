/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package rocca;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Creates a modal dialog box containing the current Click Events, and allows the user
 * to select which events are analyzed by Rocca.  This code is based on the ListDialog.java
 * example found here:
 * https://docs.oracle.com/javase/tutorial/uiswing/components/list.html
 * serialVersionUID = 21 
 * 2015/05/31
 */
@SuppressWarnings("serial")
public class RoccaClickEventList extends JDialog implements ActionListener {
    /**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private static RoccaClickEventList dialog;
    private static int[] value = null;
    private JList<String> list;
    private JButton cancelButton = new JButton("Cancel");
    private JButton selectAllButton = new JButton("Select All");
    private JButton analyzeButton = new JButton("Analyze Selection");

    /**
     * Set up and show the dialog.  The first Component argument
     * determines which frame the dialog depends on; it should be
     * a component in the dialog's controlling frame. The second
     * Component argument should be null if you want the dialog
     * to come up with its left corner in the center of the screen;
     * otherwise, it should be the component on top of which the
     * dialog should appear.
     */
    public static int[] showDialog(Component frameComp, 
                                    Component locationComp,
                                    String title,
                                    String[] possibleValues) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        value = null;
        dialog = new RoccaClickEventList(frame,
                                locationComp,
                                title,
                                possibleValues);
        dialog.setVisible(true);
        return value;
    }

    private RoccaClickEventList(Frame frame,
                       Component locationComp,
                       String title,
                       String[] data) {
        super(frame, title, true);

        //Create and initialize the buttons.
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        //
        selectAllButton = new JButton("Select All");
        selectAllButton.addActionListener(this);
        //
        analyzeButton = new JButton("Analyze Selection");
        analyzeButton.addActionListener(this);
        getRootPane().setDefaultButton(analyzeButton);

        //main part of the dialog
        list = new JList<String>(data) {

			//Subclass JList to workaround bug 4832765, which can cause the
            //scroll pane to not let the user easily scroll up to the beginning
            //of the list.  An alternative would be to set the unitIncrement
            //of the JScrollBar to a fixed value. You wouldn't get the nice
            //aligned scrolling, but it should work.
            public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };

        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(15);
//        list.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    analyzeButton.doClick(); //emulate button click
//                }
//            }
//        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(selectAllButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(analyzeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPane.add(cancelButton);

        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

        //Initialize values.
        //setValue(initialValue);
        pack();
        setLocationRelativeTo(locationComp);
    }

    //Handle clicks on the buttons.
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource()==analyzeButton) {
        	RoccaClickEventList.value = list.getSelectedIndices();
            RoccaClickEventList.dialog.setVisible(false);
    	} else if (e.getSource()==selectAllButton) {
        	list.setSelectionInterval(0, list.getModel().getSize()-1);
        } else {
            RoccaClickEventList.dialog.setVisible(false);
        }
    }
}