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
package UserInput;

import java.awt.Color;

import PamController.PamController;
import PamView.PamSymbol;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;

public class UserInputProcess extends PamProcess {

	PamDataBlock<UserInputDataUnit> uiDataBlock;

	public UserInputProcess(UserInputController pamControlledUnit,
			PamDataBlock data) {
		super(pamControlledUnit, data);

		addOutputDataBlock((uiDataBlock = new PamDataBlock<UserInputDataUnit>(UserInputDataUnit.class,
				"User Input Data", this, 0b1111111111111111)));
		uiDataBlock.setOverlayDraw(new UserInputOverlayGraphics(uiDataBlock));
		uiDataBlock.setPamSymbolManager(new StandardSymbolManager(uiDataBlock, 
				new SymbolData(PamSymbol.interpretTextCode("v"),20,20,false,Color.red,Color.black),
				true));
		uiDataBlock.setChannelMap(0b1111111111111111);
		uiDataBlock.setNaturalLifetime(3600*24); // natural lifetime 24 hours. 
		uiDataBlock.setMixedDirection(PamDataBlock.MIX_INTODATABASE);

		// PamModel.getPamModel().setGpsDataBlock(uiDataBlock);

		uiDataBlock.SetLogging(new UserInputLogger(uiDataBlock));
	}

	// PamProcess Overidden Methods
	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 86400*2*1000; // Load previous 2 days of UserInput on start
	}

	/**
	  * Load previous UserInput on startup
	  */	
	@Override
	public void setupProcess() {
		super.setupProcess();

		PamConnection connection = null;
		DBControl dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return;
		}
		connection = dbControl.getConnection();
		if (connection == null){
			return;
		}

		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW){
			uiDataBlock.clearAll();
			uiDataBlock.addObserver(this);
			uiDataBlock.setMixedDirection(PamDataBlock.MIX_OUTOFDATABASE);
			UserInputLogger uiLogging = (UserInputLogger) uiDataBlock.getLogging();
			if (uiLogging != null)
				uiLogging.prepareForMixedMode(connection);
		}
		uiDataBlock.setMixedDirection(PamDataBlock.MIX_INTODATABASE);
		UserInputController uiControl = (UserInputController) this.getPamControlledUnit();
		uiControl.userInputPanel.refillHistory();
	}
}
