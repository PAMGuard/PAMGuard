/**
 * 
 */
package Map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import GPS.GpsData;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.MapContourValues;

/**
 * @author David
 *
 */
public class MapContourGraphics {
	
	
	MapContourValues contourLatLongNew = new MapContourValues();
	MapContourValues contourLatLongOld = new MapContourValues();
	Coordinate3d screenXYold = new Coordinate3d();
	Coordinate3d screenXYnew = new Coordinate3d();
	LatLong mapUpperLeft = new LatLong();
	LatLong mapLowerRight = new LatLong();
	Coordinate3d screenUpperLeft = new Coordinate3d(0.0,0.0,0.0);
	Coordinate3d screenLowerRight = new Coordinate3d();
	LatLong panelTopLeftLL;

	LatLong panelTopRightLL;

	LatLong panelLowerLeftLL;

	LatLong panelLowerRightLL;

	
	ArrayList<MapContourValues> mapContourPoints = new ArrayList<MapContourValues>();
	double minLat=0, maxLat=0, minLong=0, maxLong=0;
	int numPoints;
	
	
	public MapContourGraphics() {
		super();
		panelTopLeftLL = new LatLong();
		panelTopRightLL = new LatLong();
		panelLowerLeftLL = new LatLong();
		panelLowerRightLL = new LatLong();
	}
	
	
	public void DrawMapContours(Graphics2D g2, MapRectProjector rectProj) {
		
		GpsData gpsData;
		
		contourLatLongOld = mapContourPoints.get(0);
		screenXYold=rectProj.getCoord3d(contourLatLongOld.y,contourLatLongOld.x,0.0);
		screenLowerRight.x=rectProj.panelWidth;
		screenLowerRight.y=rectProj.panelHeight;
		mapUpperLeft  = rectProj.panel2LL(new Coordinate3d(0.0,0.0,0.0));
		mapLowerRight = rectProj.panel2LL(screenLowerRight);
		Color currentColor;
		currentColor = g2.getColor();
		
		
		panelTopLeftLL = rectProj.panel2LL(new Coordinate3d(0.0, 0.0, 0.0));
		panelTopRightLL = rectProj.panel2LL(new Coordinate3d(
				rectProj.panelWidth, 0.0, 0.0));
		panelLowerLeftLL = rectProj.panel2LL(new Coordinate3d(0.0,
				rectProj.panelHeight, 0.0));
		panelLowerRightLL = rectProj.panel2LL(new Coordinate3d(
				rectProj.panelWidth, rectProj.panelHeight, 0.0));

		minLat = panelTopLeftLL.getLatitude();
		if (panelTopRightLL.getLatitude() < minLat)
			minLat = panelTopRightLL.getLatitude();
		if (panelLowerLeftLL.getLatitude() < minLat)
			minLat = panelLowerLeftLL.getLatitude();
		if (panelLowerRightLL.getLatitude() < minLat)
			minLat = panelLowerRightLL.getLatitude();
		minLong = panelTopLeftLL.getLongitude();
		if (panelTopRightLL.getLongitude() < minLong)
			minLong = panelTopRightLL.getLongitude();
		if (panelLowerLeftLL.getLongitude() < minLong)
			minLong = panelLowerLeftLL.getLongitude();
		if (panelLowerRightLL.getLongitude() < minLong)
			minLong = panelLowerRightLL.getLongitude();
		maxLat = panelTopLeftLL.getLatitude();
		maxLong = panelTopLeftLL.getLongitude();
		// longDegreesDisplayed = maxLong-minLong;
		if (panelTopRightLL.getLatitude() > maxLat)
			maxLat = panelTopRightLL.getLatitude();
		if (panelLowerLeftLL.getLatitude() > maxLat)
			maxLat = panelLowerLeftLL.getLatitude();
		if (panelLowerRightLL.getLatitude() > maxLat)
			maxLat = panelLowerRightLL.getLatitude();

		if (panelTopRightLL.getLongitude() > maxLong)
			maxLong = panelTopRightLL.getLongitude();
		if (panelLowerLeftLL.getLongitude() > maxLong)
			maxLong = panelLowerLeftLL.getLongitude();
		if (panelLowerRightLL.getLongitude() > maxLong)
			maxLong = panelLowerRightLL.getLongitude();
		
		for (int i =0; i<mapContourPoints.size()-2; i++){
			
			
			contourLatLongNew = mapContourPoints.get(i);
			screenXYnew=rectProj.getCoord3d(contourLatLongNew.y,contourLatLongNew.x,0.0);
			//	System.out.println(contourLatLongNew.x + "  " + contourLatLongNew.y);

			
			//if(contourLatLongNew.x<4 && contourLatLongNew.x>-4 && contourLatLongNew.y>32 && contourLatLongNew.y<44){
			//if(contourLatLongNew.x<mapLowerRight.longDegs && contourLatLongNew.x>mapUpperLeft.longDegs && contourLatLongNew.y>mapLowerRight.latDegs && contourLatLongNew.y<mapUpperLeft.latDegs){
				
			if(contourLatLongNew.x<maxLong+.5 && contourLatLongNew.x>minLong-.5 && contourLatLongNew.y>minLat-.5 && contourLatLongNew.y<maxLat+.5){
			
			if((int)contourLatLongOld.z==0 & contourLatLongOld.blockNum==contourLatLongNew.blockNum){
					
					(g2).setColor(new Color(100, 50, 0));
					(g2).drawLine((int)(screenXYold.x),(int)(screenXYold.y),(int) (screenXYnew.x), (int)(screenXYnew.y));
				}
				
				if((int)contourLatLongOld.z==100 & contourLatLongOld.blockNum==contourLatLongNew.blockNum){
					(g2).setColor(new Color(150,150,255));
					(g2).drawLine((int)(screenXYold.x),(int)(screenXYold.y),(int) (screenXYnew.x), (int)(screenXYnew.y));
				}
				
				if((int)contourLatLongOld.z==1000 & contourLatLongOld.blockNum==contourLatLongNew.blockNum){
					(g2).setColor(new Color(100,100,255));
					(g2).drawLine((int)(screenXYold.x),(int)(screenXYold.y),(int) (screenXYnew.x), (int)(screenXYnew.y));
				}
				
				contourLatLongOld=contourLatLongNew;		
				screenXYold=screenXYnew;
				numPoints=i;
				
			}
			else {
				contourLatLongOld = mapContourPoints.get(i+1);
				screenXYold=rectProj.getCoord3d(contourLatLongOld.y,contourLatLongOld.x,0.0);			
			}

		}
		(g2).setColor(currentColor);
		
	}
	
	
	
	public void setMapContourPoints(ArrayList<MapContourValues> mapContourPoints) {
		this.mapContourPoints = mapContourPoints;
	}
}