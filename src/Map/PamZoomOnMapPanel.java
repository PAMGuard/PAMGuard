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
package Map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import PamController.masterReference.MasterReferencePoint;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;

public class PamZoomOnMapPanel implements PanZoomBehaviour {
	
	MapPanel mp;

	LatLong ll;

	double latAdj;

	double longAdj;

	int zoomAdj = 1;

	double rotationAdj;

	double panStepSize;

	double rotationStep = 0.2;

	boolean mouseButtonDown;

	/**
	 * 
	 */
	public PamZoomOnMapPanel(MapPanel mp) {
		super();
		this.mp = mp; // TODO Auto-generated constructor stub
		mouseButtonDown = false;
		latAdj = 0.0;
		longAdj = 0.0;
		zoomAdj = 0;
		rotationAdj = 0.0;
	}

	Timer t = new Timer(50, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			ll = mp.getMapCentreDegrees();
			mp.setMapCentreCoords(new Coordinate3d((mp.getWidth() / 2.0)
					+ (longAdj * 3), (mp.getHeight() / 2.0) + (latAdj * 3), 0));
//			mp.setMapRangeMetres(mp.getMapRangeMetres()
//					+ (int) (zoomAdj * Math.pow(
//							mp.getMapRangeMetres() / 125.0, 1.25)));
			mp.stepMapZoom(zoomAdj);
			mp.setMapRotationDegrees(mp.getMapRotationDegrees()
					+ (rotationStep * (rotationAdj * 2)));
			mp.repaint();
		}
	});

	@Override
	public void handleMBReleased() {
		t.stop();
		mouseButtonDown = false;
		latAdj = 0.0;
		longAdj = 0.0;
		zoomAdj = 0;
		rotationAdj = 0.0;
	}

	@Override
	public void handleUp() {
		latAdj = -1.0;
		t.start();
	}

	@Override
	public void handleDown() {
		latAdj = 1.0;
		t.start();
	}

	@Override
	public void handleLeft() {

		longAdj = -1.0;
		t.start();
	}

	@Override
	public void handleRight() {
		longAdj = 1.0;
		t.start();
	}

	@Override
	public void handleUpRight() {
		latAdj = -1.0;
		longAdj = 1.0;
		t.start();
	}

	@Override
	public void handleUpLeft() {
		latAdj = -1.0;
		longAdj = -1.0;
		t.start();
	}

	@Override
	public void handleDownLeft() {
		latAdj = 1.0;
		longAdj = -1.0;
		t.start();
	}

	@Override
	public void handleDownRight() {
		latAdj = 1.0;
		longAdj = 1.0;
		t.start();
	}

	@Override
	public void handleZoomIn() {
		zoomAdj = -1; // Math.pow((double)mp.getMapRangeMetres()/125.0,1.05);
		t.start();
	}

	@Override
	public void handleZoomOut() {
		zoomAdj = 1; // Math.pow((double)mp.getMapRangeMetres()/125.0,1.05);
						// //100;

		// int panSpeed = 1;
		// int myZoomAdj = mp.getMapRangeMetres();
		// System.out.println("mp.getMapRangeMetres():
		// "+mp.getMapRangeMetres());
		t.start();
	}

	@Override
	public void handleRotateClockwise() {
		rotationAdj = 1;
		t.start();
	}

	@Override
	public void handleRotateAntiClockwise() {
		rotationAdj = -1;
		t.start();
	}

	@Override
	public void handleNorthUp() {
		if (mp != null) {
			mp.rotateNorthUp(true);
			mp.getRectProj().setMapVerticalRotationDegrees(0);
		}
	}

	@Override
	public void handleHeadUp() {
		if (mp != null) {
			mp.rotateHeadingUp(true);
			mp.getRectProj().setMapVerticalRotationDegrees(0);
		}
	}

	@Override
	public void handleCentreOnShip() {
		mp.setMapCentreDegrees(MasterReferencePoint.getLatLong());
		mp.repaint();
	}

	@Override
	public void handleMeasureWithMouse() {
		mp.mapController.setMouseMoveAction(MapController.MOUSE_MEASURE);		
	}

	@Override
	public void handlePanWithMouse() {
		mp.mapController.setMouseMoveAction(MapController.MOUSE_PAN);			
	}

}
