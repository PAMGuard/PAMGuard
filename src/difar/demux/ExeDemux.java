package difar.demux;

import java.awt.Component;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTextArea;

import wavFiles.WavFile;

//import com.sun.media.sound.WaveFileReader;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import difar.DifarControl;
import difar.DifarParameters;
import difar.BScan;

//public class ExeDemux extends GreenridgeDemux {
//	
//<<<<<<< .mine
//	private ExeDemuxParams exeDemuxParams = new ExeDemuxParams();
//
//=======
//>>>>>>> .r1472
//	private Process difarProcess; //not pam process process for running demux app
//	
//	private DifarOutputGUI difarOutputGUI;
//	
//	
//	
//	public ExeDemux(DifarControl difarControl) {
//		super(difarControl);
//		this.difarControl = difarControl;
////		PamSettingManager.getInstance().registerSettings(this);
//	}
//
//	
//	
//	@Override
//	public boolean configDemux(DifarParameters difarParams, double sampleRate) {
//		
//		
//		
//		File par = createParFile(exeDemuxParams, sampleRate);
//		if (par==null||!par.exists()||par.length()==0L){
////			System.out.printf("par %s exists %s size %L",par,par.exists(),par.length());
//			return false;
//		}
//		
//		
//		
//		
//		return true;
//
//	}
//	
//<<<<<<< .mine
//	File createParFile(ExeDemuxParams dP, double sampleRate){
//		BufferedWriter writer = null;
//		File f;
//		try {
//			File tDir = new File(dP.getTempDir());
//			if (!tDir.exists()) return null;
//			f = File.createTempFile("demuxParams", ".par", tDir);
//			f.deleteOnExit();
//			
//			writer = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
//			
//			int sr = new Double(sampleRate).intValue();
//			String a = String.format(
////					"0.1f \n 0.1f " +
//					" %d %n %s %n %.1f %n %.1f %n %.1f %n %.1f %n " +
//					"%.1f %n %.1f %n %.1f %n %.1f %n %.1f %n %.1f %n " +
//					"%.1f %n %.1f %n %.1f %n %.1f %n %s %n %s %n "
//					
//					, sr //44100.0 /* Fs			*/
//					, "7500" //7500.0  /* F_init		*/
//					, dP.F_win
//					, dP.Fn_acquire
//					, dP.Zeta_acquire
//					, dP.Fn_track
//					, dP.Zeta_track
//					, dP.Tint_15
//					, dP.Tagc_75
//					, dP.Tagc_15
//					, dP.Tau_lock_75
//					, dP.Tau_lock_15
//					, dP.Lock_threshold_75
//					, dP.Lock_hys_75
//					, dP.Lock_threshold_15
//					, dP.Lock_hys_15
//					, dP.dlpf
//					, dP.drip
//					);
//				
//			
//			System.out.println(f.getAbsolutePath());
//			writer.write(a);
//			writer.close();
//			return f;
//			
//		}catch(Exception e){
//			e.printStackTrace();
//			return null;
//		}
//		
//		
//	}
//	
//=======
//>>>>>>> .r1472
////	void printEr(String... strs){
////		String time=PamCalendar.formatDateTime(System.currentTimeMillis());
////		String fullStr="";
////		for ( String string:strs){
////			fullStr+=", "+string;
////		}
////		
////		System.out.println("["+time+"] "+fullStr);
////	}
////	
////	void printEr(String format,Object... strs){
////		String time=PamCalendar.formatDateTime(System.currentTimeMillis());
////		String fullStr="";
////		for ( Object string:strs){
////			fullStr+=", "+string.toString();
////		}
////		
////		System.out.println("["+time+"] "+fullStr);
////	}
//
//	@Override
//	public DifarResult processClip(double[] difarClip, double sampleRate) {
//		
//		
//		double[][] rawData={difarClip};
//		
////		write clip to wav
//		
//		
//		
//		
//		String fileName = PamCalendar.formatFileDateTime(PamCalendar.getTimeInMillis())+"_DIFAR_IN.wav";
//////		String fileName = "C:\\data\\userClips\\EV_20120120_082236.wav";
////		
//		/**
//		 * inputFile
//		 */
//		String fullName=exeDemuxParams.getTempDir()+fileName;
//		
//		WavFile wavFile = new WavFile(fullName, "w");
//		/* create a new AudioFormat object describing the wav file */
//		AudioFormat af = new AudioFormat((float) sampleRate,
//				16,
//				1,
//				true,
//				false);
//		wavFile.writeSingleChannel(af, difarClip);
////		wavFile.write((float) sampleRate, rawData.length, rawData);
//		
////		-----------------------------
////		String
//		fullName="C:\\data\\userClips\\EV_20120120_082237pg.wav";
////		-------------------------------
//		
//		
//		
//		
//		
////		start Difar Exe with clip
//		
//		String outNm = 
////				"_DIFAR_OUT";
//				PamCalendar.formatFileDateTime(PamCalendar.getTimeInMillis())+"_DIFAR_OUT";
//		
//		File inputFn = new File(fullName);
//		File outputFn = new File(outNm);
//		String[] exts={".ew",".ns",".om"};
//		
//		
////		long noOfSamples = 0;
////		try {
////			noOfSamples = new WaveFileReader().getAudioFileFormat(inputFn).getFrameLength();
////		} catch (UnsupportedAudioFileException e) {
////			e.printStackTrace();
////			return null;
////		} catch (IOException e) {
////			e.printStackTrace();
////			return null;
////		}
//		
//		WavFile wf = new WavFile(fullName, "r");
//		
//		
//		long noOfSamples =
////				difarClip.length;
//				wf.getWavHeader().getDataSize();
//				
//		
////		int decimationFactor = 10;
//		int decimationFactor = 1;
//		
//		System.out.printf("process %s samples %n",noOfSamples);
//		long start = System.currentTimeMillis();
//		if (!startDifarExe(inputFn, outputFn, noOfSamples, decimationFactor)){
//			System.out.println("Difar exe failed");
//			return null;
//		}
//
//		int a = (int) Math.ceil(noOfSamples/decimationFactor);
//		noOfSamples=(long)a;
//		
//		System.out.println("Done"+PamCalendar.formatTime(System.currentTimeMillis()-start));
//		
//		File ew = new File(outputFn.getAbsoluteFile()+exts[0]);
//		File ns = new File(outputFn.getAbsoluteFile()+exts[1]);
//		File om = new File(outputFn.getAbsoluteFile()+exts[2]);
//		
////		Used for testing exe output vs dll output from matlab
////		File ew = new File("E:\\difarMatlabOutput\\omni.dat");
////		File ns = new File("E:\\difarMatlabOutput\\ns.dat");
////		File om = new File("E:\\difarMatlabOutput\\ew.dat");
//		
//		//decimate to 250hz - this may come from species id or from spectro/detection limits
//		
////		decimationFactor=4800/250;
//		decimationFactor=1;
//		noOfSamples=noOfSamples/decimationFactor;
//		sampleRate=sampleRate/decimationFactor;
//		
//		
//		
//		File[] outs = {ew,ns,om};
//		FileInputStream[] fisS = new FileInputStream[3];
//		
//		if (noOfSamples>Integer.MAX_VALUE){
//			System.out.println("input file too many samples to make output double array");
//			return null;
//		}
//		
//		
//		/**
//		 * 0-ew, 1-ns, 2-om
//		 */
//		double [][] outputArray=new double [3][(int)noOfSamples];
//		
////		forEachFile:
//		for (int i=0;i<outs.length;i++){
//			File out = outs[i];
//			if ((out==null)){
//				System.out.println(i+"th file is null");
//				return null;
//			}
//			if (!out.exists()){
//				//not exists 
//				System.out.println(out.getName()+" does not exist");
//				return null;
//			}
//			
//			long expectedLength = (noOfSamples/2)*16;//leng = bytes  (16 bits per sample => 2bytes per sample? samples*2)
//			
////			if ((out.length()!=expectedLength)){
//////				not correct length
////				System.out.println(out.getName()+" is not expected length: ac:"+out.length()+" exp:"+expectedLength);
////				return null;
////			}
//			
//			
//			try {
//				fisS[i]=new FileInputStream(out);
//			} catch (FileNotFoundException e) {
//				System.out.println("Cannot create reader for "+out.getName());
//				e.printStackTrace();
//				return null;
//			}
//		}
//		
//		
//		
//		
//		
//		byte[] buffer = new byte[2];
//		
////		boolean reachedEnd = false;
//		int j=0;
//		while (j<noOfSamples){
//			
//			for (int k=0;k<3;k++){
//				
//				try {
//					fisS[k].read(buffer);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//				//TODO Check endian-ness!!
//				
//				short s = ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN).getShort();
////				System.out.println(s);
//				outputArray[k][j]=(double)s;
//			}
//			j++;
//		}
//		
////		delete wav,ew,ns,om files
//		
////		inputFn.delete();
//		
//		System.out.println(inputFn.getAbsolutePath());
//		
//		/* create a new AudioFormat object describing the wav file */
//		AudioFormat af2 = new AudioFormat((float) sampleRate,
//				16,
//				1,
//				true,
//				false);
//		
//		
//		WavFile wavFileX = new WavFile(fullName+".ew", "w");
//		System.out.println("filewrtt:"+fullName+".ew"+wavFileX.writeSingleChannel(af2, outputArray[0]));
//		
//		WavFile wavFileY = new WavFile(fullName+".ns", "w");
//		System.out.println("filewrtt:"+fullName+".ns"+wavFileY.writeSingleChannel(af2, outputArray[1]));
//		
//		WavFile wavFileZ = new WavFile(fullName+".om", "w");
//		System.out.println("filewrtt:"+fullName+".om"+wavFileZ.writeSingleChannel(af2, outputArray[2]));
//		
//		for (File oot:outs){
//			oot.delete();
//		}
//		
//		
//		
//		System.out.println("Processed clip - output ready for next process");
//		
////		return outputArray;
//		return null;
//	}
//	
//	private boolean startDifarExe(File inputFn, File outputFn, long noOfSamples, double decimationFactor) {
//        if (exeDemuxParams.exePath == null||!new File(exeDemuxParams.exePath).exists()) {
//        	System.out.println("exe null or doesn't exist");
//            return false;
//        }
//
////		C:\Users\gw\Workspaces\Matclipse\difarSpace>DIFAR_demux.exe
////		DIFAR_demux - sonobuoy demultiplexer. v. 1.4
////		1998-2004 Greeneridge Sciences, Inc.
////		
////		Usage is: DIFAR_demux <parameter_fn> <input_fn> <output_fn> <no_of_samples> <decimation_factor>
//
//        double sampleRate = 48000;//getSampleRate(inputFn);
//
//
//		
//        
//
////		  Usage is: DIFAR_demux <parameter_fn> <input_fn> <output_fn> <no_of_samples> <decimation_factor>
////        version for DIFAR_demux.exe which only produces simulated data
////        String processArgs[] = new String[6];
////        processArgs[0] = demuxParams.exePath;
////        processArgs[1] = createParFile(demuxParams, sampleRate).getAbsolutePath();
////        processArgs[2] = inputFn.getAbsolutePath();
////        processArgs[3] = outputFn.getAbsolutePath();
////        processArgs[4] = String.format("%d",noOfSamples);
////        processArgs[5] = String.format("%f", decimationFactor);
//        
////        DifarXQall <input> <Omni_Out> <North/South_Out> <East/West_Out> [<Flags_Out>]
//        String processArgs[] = new String[5];
//      processArgs[0] = exeDemuxParams.exePath;
//      processArgs[1] = inputFn.getAbsolutePath();
//      processArgs[2] = outputFn.getAbsolutePath()+".om";
//      processArgs[3] = outputFn.getAbsolutePath()+".ns";
//      processArgs[4] = outputFn.getAbsolutePath()+".ew";
//        
//        
//        
//        
//        for (String processArg : processArgs){
//        	System.out.print(processArg+ " ");
//        }
//        System.out.println("");
//        
//        ProcessBuilder pb = new ProcessBuilder(processArgs);
//        try {
////        	difarOutputGUI = new DifarOutputGUI(difarControl.getGuiFrame(), inputFn.getName()+": Demultiplexing...", false);
//            difarProcess = pb.start();
////            Thread reader = difarOutputGUI.captureOutput(difarProcess);
////            
////            difarOutputGUI.setVisible(true);
//            
//            
//            
//            
//            difarProcess.waitFor();
//            System.out.println("DIFAR acknowleged finished");
////            difarProcess.
////            reader=null;
////            difarOutputGUI.dispose();
//            
//            
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            return false;
//        }
//        return true;
//   }
//	
//	
//	
//	class DifarOutputGUI extends PamDialog{
//
//		InputStream inputStream;
//		
//		int MAX_LINES = 8;
//		JTextArea textArea;
//		
//		public DifarOutputGUI(Window parentFrame, String title,
//				boolean hasDefault) {
//			super(parentFrame, title, hasDefault);
//			textArea= new JTextArea();
//			setDialogComponent(textArea);
//			setSize(300, 300);
//		}
//		
//		public Thread captureOutput(Process buoyNetProcess) {
//			
//			
//	          textArea.setText("Capture text from buoynet process ");
//	          InputStream is = buoyNetProcess.getInputStream();
//	          
//	          
//	          InputStreamReader isr = new InputStreamReader(is);
//	          BufferedReader bufferedReader = new BufferedReader(isr);
//	          ReadThread readThread = new ReadThread(bufferedReader);
//	          Thread t = new Thread(readThread);
//	          t.start();
//	          return t;
//	          
//	    }
//
//		@Override
//		public boolean getParams() {
//			return true;
//		}
//
//		@Override
//		public void cancelButtonPressed() {
//			difarProcess.destroy();
//		}
//
//		@Override
//		public void restoreDefaultSettings() {
//			
//		}
//		
//		public JTextArea getTextArea() {
//			return textArea;
//		}
//		
//		public int getMAX_LINES() {
//			return MAX_LINES;
//		}
//		
//	}
//
//	class ReadThread implements Runnable {
//
//        private BufferedReader bufferedReader;
//        int totalLines = 0;
//        
//        public ReadThread(BufferedReader bufferedReader) {
//            super();
//            this.bufferedReader = bufferedReader;
//        }
//
//        @Override
//        public void run() {
//        	JTextArea textArea = ((DifarOutputGUI)getDisplayComponent()).getTextArea();
//            while (true) {
//                 try {
//                      String str = bufferedReader.readLine();
//                      totalLines++;
//                      String currText = textArea.getText();
//                      currText += "\n" + str;
//                      while (totalLines > ((DifarOutputGUI)getDisplayComponent()).getMAX_LINES()) {
//                           int lEnd = currText.indexOf("\n");
//                           if (lEnd < 0) {
//                                break;
//                           }
//                           currText = currText.substring(lEnd+1);
//                           totalLines--;
//                      }
//                      textArea.setText(currText);
//                      textArea.setCaretPosition(currText.length()-1);
//                      
//                 } catch (IOException e) {
//                      System.out.println(e.getMessage());
//                 }
//            }
//        }
//        
//   }
//
//
//
//	@Override
//	public boolean hasOptions() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//	@Override
//	public boolean showOptions(Window window, DifarParameters difarParams) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public Component getDisplayComponent() {
//		return difarOutputGUI;
//	}
//
//<<<<<<< .mine
//	@Override
//	public String getUnitName() {
//		return difarControl.getUnitName();
//	}
//
//	@Override
//	public String getUnitType() {
//		return "ExeDemuxSettings";
//	}
//
//	@Override
//	public Serializable getSettingsReference() {
//		return exeDemuxParams;
//	}
//
//	@Override
//	public long getSettingsVersion() {
//		return ExeDemuxParams.serialVersionUID;
//	}
//
//	@Override
//	public boolean restoreSettings(
//			PamControlledUnitSettings pamControlledUnitSettings) {
//		exeDemuxParams = ((ExeDemuxParams) pamControlledUnitSettings.getSettings()).clone();
//		return true;
//	}
//	
//=======
//>>>>>>> .r1472
//	
//	
//	
//
//}
