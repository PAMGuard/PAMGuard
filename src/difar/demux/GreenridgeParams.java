package difar.demux;

import java.io.File;
import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;


public class GreenridgeParams implements Serializable, Cloneable, ManagedParameters {

	/**
	 * About three things to do here ...
	 * 1. from clone() call something to check the temp dir 
	 * 2. write params file with a simple name (no date) so it overwrites. 
	 * 3. also write the appropriate filter files to the temp dir. 
	 */

	public static final long serialVersionUID = 1L;
	
	//44100.0 /* Fs			*/
	//7500.0  /* F_init		*/
	public double F_win				= 100.0;   /* F_win				*/
	public double Fn_acquire		= 30.0 ;   /* Fn_acquire		*/
	public double Zeta_acquire		= 0.5  ;   /* Zeta_acquire		*/
	public double Fn_track			= 3.0  ;   /* Fn_track			*/
	public double Zeta_track		= 0.5  ;   /* Zeta_track		*/
	public double Tint_15			= 1.0  ;   /* Tint_15			*/
	public double Tagc_75			= 0.1  ;   /* Tagc_75			*/
	public double Tagc_15			= 1.0  ;   /* Tagc_15			*/
	public double Tau_lock_75		= 0.1  ;   /* Tau_lock_75		*/
	public double Tau_lock_15		= 1.0  ;   /* Tau_lock_15		*/
	public double Lock_threshold_75= 0.85 ;   /* Lock_threshold_75	*/
	public double Lock_hys_75		= 0.05 ;   /* Lock_hys_75		*/
	public double Lock_threshold_15= 0.85 ;   /* Lock_threshold_15	*/
	public double Lock_hys_15		= 0.05 ;   /* Lock_hys_15		*/
	public String dlpf				= "C:\\analysis\\sonobuoy\\difarBSM\\difarlpf_48.flt";//difar low pass filter	should poss be in temp folder and only have name here
	public String drip				= "C:\\analysis\\sonobuoy\\difarBSM\\difarrip_48.flt";//difar ripple filter		should poss be in temp folder and only have name here
	private String tempDir;
	public String exePath			= "C:\\analysis\\sonobuoy\\difarBSM\\DifarXQall.exe";
	
	public double[] dlpfVals = {

			//---------------------- 1
			 1.1000000000000000e+001,

			//---------------------- 12
			 7.4109217344733472e-004,
			-1.2327826358095514e-003,
			 2.3656379056736800e-003,
			-1.9846531452168804e-003,
			 1.9741023328130004e-003,
			-1.3272575149216110e-003,
			 1.3272575149002994e-003,
			-1.9741023327717080e-003,
			 1.9846531451846908e-003,
			-2.3656379056594996e-003,
			 1.2327826358076782e-003,
			-7.4109217344743184e-004,
			
			//---------------------- 12
			 1.0000000000000000e+000,
			-6.3980887234530128e+000,
			 1.9209759173622168e+001,
			-3.5561667530282612e+001,
			 4.4933181287175312e+001,
			-4.0574895524393096e+001,
			 2.6659488141917892e+001,
			-1.2722429561667424e+001,
			 4.3149754326451264e+000,
			-9.8933769633897792e-001,
			 1.3785466794360482e-001,
			-8.8364123928872640e-003};
	
	public double[] dripVals = {
			
			 4.0000000000000000e+000,
			 
			 3.8041170909652224e-006,
			-1.3153632368433678e-006,
			 4.3730985321618136e-006,
			-1.3153632368436254e-006,
			 3.8041170909648864e-006,
			 
			 1.0000000000000000e+000,
			-3.8187063683611792e+000,
			 5.4719934849906912e+000,
			-3.4873185502254332e+000,
			 8.3404078420216112e-001};
	


	public GreenridgeParams() {
		super();
		tempDir = System.getProperty("java.io.tmpdir"); 
		dlpf	= "difarlpf_48.flt";
		drip	= "difarrip_48.flt";
		exePath	= "C:\\analysis\\sonobuoy\\difarBSM\\DIFAR_demux.exe";
	}

	public String getTempDir() {
		if (tempDir!=null){
			if (new File(tempDir).exists()){
				return tempDir;
			}else{
//				PamDialog.showWarning(null, "Difar Temporary Directory", 
//						"The directorory " +tempDir+" does not exist. This will be set to null and the default "
//						+System.getProperty("java.io.tmpdir")+" will be used");
				
				tempDir=null;
			}
		}
//		return Pamguard folder in userfolder  make Exedemux folder?
		return System.getProperty("java.io.tmpdir");
	}
	
	public void setTempDir(String tdn) {
		if (tdn==null){
			tempDir=null;
			return;
		}
		File td=new File(tdn);
		if (!td.exists()){
			tempDir=null;
		}else{
			tempDir=td.getAbsolutePath();
		}
	}
	
	
	

	@Override
	public GreenridgeParams clone() {
		try {
			GreenridgeParams newParams = (GreenridgeParams) super.clone();
//			 TODO check the tempDir of newParams here - just always set to java temp dir. 
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}


}
