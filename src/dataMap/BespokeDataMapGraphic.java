package dataMap;

import java.awt.Graphics;

import dataMap.DataStreamPanel.DataGraph;

/**
 * Bespoke drawing on datamap. Can be provided by a datablock and will override all normal 
 * drawing of the normal DataGraph in DataStreamPanel
 * @author dg50
 *
 */
public interface BespokeDataMapGraphic {

	 void paint(Graphics g, DataGraph dataGraph);	
	
}
