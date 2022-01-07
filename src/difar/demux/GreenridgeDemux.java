package difar.demux;

import java.awt.Component;
import java.awt.Window;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettings;
import difar.DifarControl;
import difar.DifarParameters;

public abstract class GreenridgeDemux extends DifarDemux implements PamSettings {

	protected PamControlledUnit difarControl;

	protected GreenridgeParams demuxParams = new GreenridgeParams();

	public GreenridgeDemux(PamControlledUnit difarControl2) {
		super();
		this.difarControl = difarControl2;
//		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public boolean hasOptions() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean showOptions(Window window, DifarParameters difarParams) {
		// TODO Auto-generated method stub
		return false;
	}

	File createParFile() {
		
		return createParFile(demuxParams, 48000);
	}
	
	File createParFile(GreenridgeParams dP, double sampleRate){
		BufferedWriter writer = null;
		File f;
		try {
			File tDir = new File(dP.getTempDir());
			if (!tDir.exists()) return null;
//			f = File.createTempFile("demuxParams", ".par", tDir);
			f=new File(dP.getTempDir(),"demuxParams.par");
			f.deleteOnExit();
			
			writer = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
			
			int sr = new Double(sampleRate).intValue();
			String a = String.format(
//					"0.1f \n 0.1f " +
					" %d %n %s %n %.1f %n %.1f %n %.1f %n %.1f %n " +
					"%.1f %n %.1f %n %.1f %n %.1f %n %.1f %n %.1f %n " +
					"%.1f %n %.1f %n %.1f %n %.1f %n %s %n %s %n "
					
					, sr //44100.0 /* Fs			*/
					, "7500" //7500.0  /* F_init		*/
					, dP.F_win
					, dP.Fn_acquire
					, dP.Zeta_acquire
					, dP.Fn_track
					, dP.Zeta_track
					, dP.Tint_15
					, dP.Tagc_75
					, dP.Tagc_15
					, dP.Tau_lock_75
					, dP.Tau_lock_15
					, dP.Lock_threshold_75
					, dP.Lock_hys_75
					, dP.Lock_threshold_15
					, dP.Lock_hys_15
					, dP.dlpf
					, dP.drip
					);
				
			
//			System.out.println(f.getAbsolutePath());
			writer.write(a);
			writer.close();
			
			writeFilterFile(dP.dlpf, dP, dP.dlpfVals);
			writeFilterFile(dP.drip, dP, dP.dripVals);
			
			return f;
			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		
	}
	
	private File writeFilterFile(String filename,GreenridgeParams dP, double[] values){
		BufferedWriter writer = null;
		File f;
		try {
			File tDir = new File(dP.getTempDir());
			if (!tDir.exists()) return null;
//			f = File.createTempFile("demuxParams", ".par", tDir);
			f=new File(dP.getTempDir(),filename);
			f.deleteOnExit();
			
			writer = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
			
			NumberFormat formatter = new DecimalFormat("0.0E0");
//			formatter.setRoundingMode(RoundingMode.HALF_UP);
			
			formatter.setMinimumFractionDigits(16);
//			formatter.format();
			
			String a="";
			for (double value:values){
				
				
				String number = formatter.format(new BigDecimal(value));
				
				//Option 1 if C is picky
//				String[] parts = number.split("E");
//				String exp=parts[1];
//				if (exp.startsWith("-")){//negative
//					exp=exp.substring(1, exp.length()-1);
//					
//					while(exp.length()<3){
//						exp="0"+exp;
//					}
//					exp="-"+exp;
//					
//					
//				}else{//positive
//					while(exp.length()<3){
//						exp="0"+exp;
//					}
//					exp="+"+exp;
//				}
//				
//				a+=" "+parts[0]+"e"+exp;
				
				//Option 2 if C is not picky
				a+=" "+new Double(value).toString();
			}
			
//			System.out.println(f.getAbsolutePath());
			writer.write(a);
			writer.close();
			return f;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public Component getDisplayComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUnitName() {
		return difarControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "ExeDemuxSettings";
	}

	@Override
	public Serializable getSettingsReference() {
		return demuxParams;
	}

	@Override
	public long getSettingsVersion() {
		return GreenridgeParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		demuxParams = ((GreenridgeParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
}
