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
package PamView;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.symbol.PamSymbolChooser;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Used by PamDataBlocks to draw PamDataUnits on display objects (maps,
 *         spectrograms, etc)
 *         <p>
 *         If a PanelOverlayDraw object is instantiated for a PamObservable,
 *         then any display (usually an observer of that PamObservable) will
 *         call the PamObservable function PamObservable.DrawDataUnit(...),
 *         parsing it a Graphics2d handle, a PamDataUnit, and a concrete
 *         instance of a GeneralProjector. The DrawDataUnit function in
 *         PanelOverlayDraw may then use the Projector to convert data in the
 *         PamDatablock into screen coordinates and then draw them (in any way
 *         it likes) on the graphics handle.
 *         <p>
 *         If the Observable may be drawn on multiple types of display, then
 *         DrawDataUnit should check the ParameterTypes and ParameterUnits
 *         required by the Projector and then draw whatever is appropriate for
 *         that Projector (e.g. The Whistle DataBlock draws contours on the
 *         display that has TIME and FREQUENCY as ParameterTypes and a single
 *         triangular symbol on the display that has LATITUDE and LONGITUDE as
 *         it's parameter types).
 *         
 *         @see GeneralProjector
 */
public abstract class PanelOverlayDraw {

	private PamSymbol defaultSymbol;

	public PanelOverlayDraw(PamSymbol defaultSymbol) {
		super();
		this.defaultSymbol = defaultSymbol;
	}

	/**
	 * 
	 * @param g
	 *            Graphics handle to draw on
	 * @param pamDataUnit
	 *            PamDataUnit to draw
	 * @param generalProjector
	 *            Projector to use when drawing.
	 * @return A rectangle surrouding whatever has just been drawn.
	 */
	abstract public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector);

	/**
	 * @param generalProjector
	 * @return Returns whether or not the object can be drawn using the given
	 *         projector. The implementation of CanDraw should examine the
	 *         parameter types in GeneralProjector and check that it will know
	 *         how to prvide those parameters before returning true, or false
	 *         otherwise.
	 */
	public boolean canDraw(GeneralProjector generalProjector) {
		return canDraw(generalProjector.getParameterTypes(), generalProjector.getParameterUnits());
	}
	
	/**
	 * Extra function so that a datablock can do entirely it's own thing (introduced for drawing gemini
	 * data overlays where we only want to draw on thing and one thing only. 
	 * @param g
	 * @param pamDataBlock
	 * @param projector
	 * @return true if normal drawing should proceed after this, otherwise return false and drawing will stop
	 */
	public boolean preDrawAnything(Graphics g, PamDataBlock pamDataBlock, GeneralProjector projector) {
		return true;
	}
	/**
	 * 
	 * @param parameterTypes
	 * @param parameterUnits
	 * @return Returns whether or not the object can be drawn using the given
	 *         projector. The implementation of CanDraw should examine the
	 *         parameter types in GeneralProjector and check that it will know
	 *         how to prvide those parameters before returning true, or false
	 *         otherwise.
	 */
	abstract public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits);
	
	/**
	 * provide a graphics component (probably a JPanel) that can be incorporated 
	 * into a key panel for maps, and anything else that uses overlay graphics. 
	 * <p>
	 * Since multiple keys may be created in various displays, new ones should 
	 * normally be created each time this is called. 
	 * <p>
	 * The GeneralProjector reference is passed as a parameter since the 
	 * type of symbol or shape drawn on a particular plot may depend on the
	 * type of coordinate system. For example, whistles are drawn as a contour
	 * on the spectrogram display, but as a point (PamSymbol) on the map.
	 * <p>
	 * It is possible that some PanelOverlayDraw implementations will 
	 * be rather complicated and the keys consequently quite large. The
	 * extendedKey parameter may therefore be used to draw a full or a cut 
	 * down version of the key. It is expected that most implementations
	 * will ignore this parameter !
	 * 
	 * @param generalProjector Reference to a GeneralProjector responsible 
	 * for drawing with the PAnelOverlayDraw implementation
	 * @param keyType Draw a more complicated key, giving more detail
	 * @return java.awt compnent (usually a JPanel).
	 * 
	 * @see whistleDetector.WhistleGraphics
	 * @see PamSymbol
	 */
	abstract public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType);
	
	/**
	 * Provides text for popup hover windows on displays. 
	 * @param generalProjector Projector associated with the display requiring the text
	 * @param dataUnit Data unit the mouse hovered over
	 * @return Text to display
	 */
	abstract public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide);
	
	/**
	 * 
	 * @param generalProjector projector
	 * @return true if the drawing methods have options relevant to this
	 * projection which can be shown in a dialog of some sort
	 * (see showOptions)
	 */
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return false;
	}
	
	/**
	 * Show an options dialog for a particular projector. 
	 * @param parentWindow parent window or frame
	 * @param generalProjector projector
	 * @return
	 */
	public boolean showOptions(Window parentWindow, GeneralProjector generalProjector) {
		return false;
	}
	
	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector projector) {
		PamSymbolChooser symbolChooser = projector.getPamSymbolChooser();
		if (symbolChooser == null) {
			return getDefaultSymbol();
		}
		else {
			return symbolChooser.getPamSymbol(projector, pamDataUnit);
		}
	}

	public PamSymbol getDefaultSymbol() {
		return defaultSymbol;
	}

	/**
	 * @param defaultSymbol the defaultSymbol to set
	 */
	public void setDefaultSymbol(PamSymbol defaultSymbol) {
		this.defaultSymbol = defaultSymbol;
	}
}
