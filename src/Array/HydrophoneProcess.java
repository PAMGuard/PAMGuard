package Array;

import pamScrollSystem.AbstractScrollManager;
import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class HydrophoneProcess extends PamProcess{
	
		private StreamerDataBlock streamerDataBlock;
		private boolean arrayDataSaved;
		private ArrayManager arrayManager;
		private HydrophoneDataBlock hydrophoneDataBlock;
		private HydrophoneSQLLogging hydrophoneSQLlogging;
	
		public HydrophoneProcess(ArrayManager arrayManager) {
			super(arrayManager, null);
			this.arrayManager = arrayManager;
			
			addOutputDataBlock(streamerDataBlock = new StreamerDataBlock(this));
			streamerDataBlock.setOverlayDraw(new StreamerOverlayGraphics());
			streamerDataBlock.setPamSymbolManager(new StandardSymbolManager(streamerDataBlock, StreamerOverlayGraphics.streamerSymbol, true));
			streamerDataBlock.SetLogging(new StreamerLogging(this, streamerDataBlock));

			hydrophoneDataBlock= new HydrophoneDataBlock("Hydrophone Data",	this, 0xFFFFFFFF);
			hydrophoneSQLlogging=new HydrophoneSQLLogging(hydrophoneDataBlock);
			hydrophoneDataBlock.SetLogging(hydrophoneSQLlogging);
			addOutputDataBlock(hydrophoneDataBlock);
			
			streamerDataBlock.setMixedDirection(PamDataBlock.MIX_OUTOFDATABASE);
			hydrophoneDataBlock.setMixedDirection(PamDataBlock.MIX_OUTOFDATABASE);
		}

		@Override
		public void destroyProcess(){
			for (int i=0; i<super.outputDataBlocks.size(); i++){
				AbstractScrollManager.getScrollManager().removeFromSpecialDatablock(outputDataBlocks.get(i));
			}
			super.destroyProcess();
		}
		
		@Override
		public void addOutputDataBlock(PamDataBlock outputDataBlock){
			AbstractScrollManager.getScrollManager().addToSpecialDatablock(outputDataBlock, 60000, 0);
			super.addOutputDataBlock(outputDataBlock);
		}
		
		@Override
		public void pamStart() {
			/*
			 * Not necessary to call this since the streamers will create a data unit, which get's 
			 * saved to the database as soon as initialisation is complete. 
			 */
			if (!arrayDataSaved) {
//				saveArrayData();
			}
		}

		@Override
		public void pamStop() {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @return the streamerDataBlock
		 */
		protected StreamerDataBlock getStreamerDataBlock() {
			return streamerDataBlock;
		}

		/**
		 * Save all the array data to the database. This get's called when 
		 * the array manager dialog has been called or when PAMGuard starts to ensure
		 * that there is a database record of how the hydrophones were arranged. <p>
		 * This may cause trouble in offline analysis if you want to make changes to the phone layout 
		 * (for example changing a hydrophone separation).  
		 */
		public void createArrayData() {
//			DBControlUnit dbControl = DBControlUnit.findDatabaseControl();
//			DBProcess dbProcess = null;
//			if (dbControl != null) {
//				dbProcess = dbControl.getDbProcess();
//			}
			
			long timeNow = PamCalendar.getTimeInMillis();
			
			createDefaultStreamerUnits(timeNow);
			createDefaultHydrophoneUnits(timeNow);

			arrayDataSaved = true;
		}
		public int createDefaultStreamerUnits(long timeNow) {

			PamArray currentArray = arrayManager.getCurrentArray();
			if (currentArray == null) {
				return 0;
			}
			int n = currentArray.getNumStreamers();
			Streamer s;
			StreamerDataUnit sdu;
			StreamerDataBlock sdb = arrayManager.getStreamerDatabBlock();
			for (int i = 0; i < n; i++) {
				if (sdb.getPreceedingUnit(timeNow,1<<i)==null){
					s = currentArray.getStreamer(i);
					sdu = new StreamerDataUnit(timeNow, s);
					if (getPamControlledUnit().isViewer()) {
						// give it a face index to stop it saving every time viewere is opened. 
						sdu.setDatabaseIndex(-1);
					}
					streamerDataBlock.addPamData(sdu);
				}
			}
			return n;
		}
		public int createDefaultHydrophoneUnits(long timeNow) {
			PamArray currentArray = arrayManager.getCurrentArray();
			if (currentArray == null) {
				return 0;
			}
			int n = currentArray.getHydrophoneCount();
			Hydrophone h;
			HydrophoneDataUnit hdu;
			for (int i = 0; i < n; i++) {
				h = currentArray.getHydrophone(i);
				hdu = new HydrophoneDataUnit(timeNow, h);
				if (getPamControlledUnit().isViewer()) {
					// give it a face index to stop it saving every time viewere is opened. 
					hdu.setDatabaseIndex(-1);
				}
				hydrophoneDataBlock.addPamData(hdu);
			}

			return n;
		}

		/**
		 * @return the hydrophoneDataBlock
		 */
		protected HydrophoneDataBlock getHydrophoneDataBlock() {
			return hydrophoneDataBlock;
		}

		/**
		 * @return the hydrophoneSQLlogging
		 */
		protected HydrophoneSQLLogging getHydrophoneSQLlogging() {
			return hydrophoneSQLlogging;
		}

}
