package AIS;

import jsonStorage.JSONObjectData;

public class AISJsonData extends JSONObjectData{
	
	int MMSI = 0;
	int messageId = 0;
	double lat = 0;
	double lon = 0;
	double SOG = 0;
	double COG = 0;
	String navStatus="";
	String shipName = "";
	String shipType = "";
	int IMO = 0;
	double dimPort=0;
	double dimStar=0;
	double dimBow=0;
	double dimStern=0;
	double draught = 0;
	
	public AISJsonData() {
		super();
	}
	
	
	
}
