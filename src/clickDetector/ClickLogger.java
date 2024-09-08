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


/**
 *
 * Facilitates data logging to database 
 *
 * @author David J McLaren, Douglas Gillespie, Paul Redmond
 *
 */

package clickDetector;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Databse logging information for Clicks.
 * 
 * @author Doug Gillespie
 *
 */
public class ClickLogger extends PamDetectionLogging {

	ClickControl clickControl;
	
	// These items are use to form the columns within the Table
	PamTableItem clickNumber;
	
	PamTableItem clickSpecies;
	
	public ClickLogger(ClickControl clickControl, PamDataBlock pamDataBlock) {
		
		super(pamDataBlock, SQLLogging.UPDATE_POLICY_OVERWRITE);
		
		this.clickControl = clickControl;

		getTableDefinition().addTableItem(clickNumber = new PamTableItem("ClickNumber", Types.INTEGER));
		getTableDefinition().addTableItem(clickSpecies = new PamTableItem("SpeciesCode", Types.INTEGER));
		
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		super.setTableData(sqlTypes, pamDataUnit);
		
		ClickDetection click = (ClickDetection) pamDataUnit;
		
		clickNumber.setValue(click.clickNumber);
		clickSpecies.setValue((int) click.getClickType());
		
	}

//
//	@Override
//	public void setTableData(PamDataUnit pamDataUnit) {
//
//		ClickDetection click = (ClickDetection) pamDataUnit;
//		
//		dateItem.setValue(PamCalendar.getTimeStamp(click.getTimeMilliseconds()));
//		numItem.setValue(click.clickNumber);
//		channelItem.setValue(click.getChannelBitmap());
//		double angle = 0;
//		if (click.getClickLocalisation() != null) {
//			angle = click.getClickLocalisation().getBearing() * 180 / Math.PI;
//		}
//		angleItem.setValue(angle);
//		
//	}

	


}