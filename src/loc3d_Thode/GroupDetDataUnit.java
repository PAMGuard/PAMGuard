package loc3d_Thode;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class GroupDetDataUnit extends SuperDetection implements PamDetection {
	/* This class wraps a feature set around a PamDetection, useful
	 * for situations where a group of detections have to be collected into a single
	 * unit, based on a common feature.  These common features can be used to
	 * associate detections between channels or stations as well.
	 * 
	 * A station is defined as a collection of channels where the relationship
	 * between detections on these channels is not ambiguous.  In most cases this means 
	 * a situation where the elements are spatially close enough that there is no
	 * ambiguity about the relative arrival time of a particular call event at the multiple stations.
	 * For example, each subarray in a towed array will be considered a separate station.
	 * 
	 */
	private float score;
	//private double tds[];			//time in msec relative to the anchorDetection
	private double tdd[];  //time in msec relative to other stations
	private int Nstations,Nfeatures; //number of stations, number of features associated with at station.

	private double feature[][]; //Rows are station numbers, columns are particular features
	private double rawdata[];
	
	public GroupDetDataUnit(long timeMilliseconds, int channelBitmap, long peakSample, int Nstations, int Nfeatures) {
		super(timeMilliseconds, channelBitmap, peakSample, 0);
		//tds=new double[Nstations];
		feature= new double[Nstations][Nfeatures];
		tdd=new double[Nstations];
		this.rawdata=null;
		for (int i=0; i<Nstations; i++){
			tdd[i]=0;
			for (int j=0; j<Nfeatures; j++){
				//tds[i]=0;
				feature[i][j]=0;
			}
		}
			
		this.Nstations=Nstations;
		this.Nfeatures=Nfeatures;
		
		
	}

	public int getNstations() {
		return Nstations;
	}

	public int getNfeatures() {
		return Nstations;
	}
	
	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public double getTdd(int i) {
		return tdd[i];
	}

	public double[] getTdd() {
		return tdd;
	}

	public void setTdd(double tdd, int i) {
		this.tdd[i] = tdd;
	}

	public void setTdd(double[] tdd) {
		this.tdd = tdd;
	}

	public double[][] getFeature() {
		return feature;
	}

	public void setFeature(double[][] feature) {
		this.feature = feature;
	}
	public double getFeature(int i, int j) {
		return feature[i][j];
	}

	public void setFeature(double feature,int i, int j) {
		this.feature[i][j] = feature;
	}

	public double[] getRawdata() {
		return rawdata;
	}

	public void setRawdata(double[] rawdata) {
		this.rawdata = rawdata;
	}
	


}

//class DetInfo {
//IshDetection ishDetection;

//public IshDetection getIshDetection() {
//return ishDetection;
//}
//public void setIshDetection(IshDetection ishDetection) {
//this.ishDetection = ishDetection;
//this.tds = ishDetection.peakTime - 
//(anchorDetection==null ? -999999 : anchorDetection.peakTime);
//}
//}
///* End of DetInfo subclass.*/