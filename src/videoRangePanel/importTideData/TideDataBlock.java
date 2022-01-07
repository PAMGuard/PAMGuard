package videoRangePanel.importTideData;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import videoRangePanel.VRControl;

/**
 * Vasic data block for tide data
 * @author Jamie Macaulay
 *
 */
public class TideDataBlock extends PamDataBlock {

	public TideDataBlock(String name, PamProcess parentProcess) {
		super(TideDataUnit.class, "Tide Data" ,parentProcess, 0xFFFFFFFF);
	}
	

	public TideDataBlock(String name) {
		super(TideDataUnit.class, "Tide Data" ,null,  0xFFFFFFFF);
	}
	
	/**
	 * The first dataunits before and after timeMillis
	 * @param timeMillis
	 * @return 
	 */
	//TODO- interpolate the location data (lat/long). 
	public TideDataUnit findInterpTideData(long timeMillis){
		
//		System.out.println("Find tide interp data: " +  PamCalendar.formatDateTime2(timeMillis) + " " + this.getUnitsCount());
//		
//		System.out.println("Find tide interp data: first: " +  PamCalendar.formatDateTime2(this.getFirstUnit().getTimeMilliseconds()) 
//		+ " last: "+ PamCalendar.formatDateTime2(this.getLastUnitMillis()));


		
		TideDataUnit dataUnit1=(TideDataUnit) getPreceedingUnit(timeMillis);
		TideDataUnit dataUnit2=(TideDataUnit) this.getNextUnit(timeMillis, 0);
		
		if (dataUnit1==null || dataUnit2==null) return null; 
		
		//now interpolate the results
		double fraction=(timeMillis-dataUnit1.getTimeMilliseconds())/(dataUnit2.getTimeMilliseconds()-dataUnit1.getTimeMilliseconds());
		double levelInterp=dataUnit1.getLevel()+(fraction*(dataUnit2.getLevel()-dataUnit1.getLevel()));
		double speedInterp=dataUnit1.getSpeed()+(fraction*(dataUnit2.getSpeed()-dataUnit1.getSpeed()));
		double angleInterp=dataUnit1.getAngle()+(fraction*(dataUnit2.getAngle()-dataUnit1.getAngle()));
			
		TideDataUnit interpUnit=new TideDataUnit(timeMillis, levelInterp,speedInterp, angleInterp,dataUnit1.getLocation());
		
		return interpUnit;
			
	}
	
}

	
