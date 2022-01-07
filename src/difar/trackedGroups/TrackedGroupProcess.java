package difar.trackedGroups;

import java.util.ArrayList;
import java.util.ListIterator;

import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import difar.DIFARCrossingInfo;
import difar.DIFARTargetMotionInformation;
import difar.DifarControl;
import difar.DifarDataUnit;
import pamScrollSystem.PamScroller;
import pamScrollSystem.ViewLoadObserver;
import targetMotionModule.TargetMotionResult;
import targetMotionModule.algorithms.Simplex2D;
import Array.ArrayManager;
import Array.StreamerDataUnit;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import autecPhones.AutecGraphics;

/**
 * Subscribe to DifarDataBlocks and assign new bearings to groups of whales 
 * @author Brian Miller
 *
 */
public class TrackedGroupProcess extends PamProcess{

	private DifarControl difarControl;

	private TrackedGroupDataBlock trackedGroupData;

	private boolean viewerMode = (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
	private PamScroller viewerScroller;

	private PamDataBlock parentDataBlock;

	public TrackedGroupProcess(DifarControl difarControl,
			PamDataBlock parentDataBlock, String processName) {
		super(difarControl, parentDataBlock, processName);
		this.parentDataBlock = parentDataBlock;
		trackedGroupData = new TrackedGroupDataBlock("DIFAR Tracked Groups", difarControl, this, 0);
		trackedGroupData.SetLogging(new TrackedGroupSqlLogging(difarControl, trackedGroupData));
		trackedGroupData.setNaturalLifetime(3600*8);
		TrackedGroupOverlayGraphics detectionOverlayGraphics= new TrackedGroupOverlayGraphics(difarControl, trackedGroupData);
		trackedGroupData.setOverlayDraw(detectionOverlayGraphics);
		trackedGroupData.setPamSymbolManager(new StandardSymbolManager(trackedGroupData, TrackedGroupOverlayGraphics.defaultSymbol, true));
		addOutputDataBlock(trackedGroupData);

	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
	}

	public TrackedGroupDataBlock getTrackedGroups(){
		return trackedGroupData;
	}


	/**
	 * Check if a group already exists, and if so add the data
	 * to that group. Otherwise create a new group.
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit dataUnit) {
		DifarDataUnit ddu = (DifarDataUnit) dataUnit;
		if (ddu.isVessel()) return;

		StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock()
				.getPreceedingUnit(ddu.getTimeMilliseconds(), ddu.getChannelBitmap());
		if (sdu == null) return;
		
		TrackedGroupDataUnit trackedGroup = trackedGroupData.findDataUnit(
				sdu.getTimeMilliseconds(), ddu.getChannelBitmap(), ddu.getTrackedGroup());

		if (trackedGroup == null) {
			trackedGroupData.addPamData(trackedGroup = new TrackedGroupDataUnit(ddu));

		} else {
			trackedGroup.addData(ddu);
			if (!getPamControlledUnit().isViewer()){
				PamConnection con = DBControlUnit.findConnection();
				if (con != null) {
					TrackedGroupSqlLogging trackedGroupLogging = (TrackedGroupSqlLogging) trackedGroupData.getLogging();
					trackedGroupLogging.updateData(con.getSqlTypes(), trackedGroup);
				}
			}
		}
		TrackedGroupCrossingInfo tgci = null;
		tgci = getDifarRangeInfo(trackedGroup);
	}


	/**
	 * Called just before a difar unit is stored. Looks at other channels that have recently 
	 * had a data unit stored and decides whether or not they are likely to be the same 
	 * vocalisation. If they are, calculate a crossed bearing to them both. If > 2 channels
	 * do some kind of optimised fit. 
	 * @param trackedGroupDataUnit 
	 * @return information about the range (will already have been put into affected units)
	 */
	public TrackedGroupCrossingInfo getDifarRangeInfo(TrackedGroupDataUnit trackedGroupDataUnit) {
		/**
		 * First find a list of other channels that may match by iterating backwards through
		 * the datablocks. 
		 */
		ArrayList<PamDataUnit> detectionList = trackedGroupData.findDataUnits(trackedGroupDataUnit.getGroupName());
		if (detectionList.size() < 2) return null;

		Simplex2D simplex2D = new Simplex2D(null);
		TrackedGroupTargetMotionInformation tmi = new TrackedGroupTargetMotionInformation(this, detectionList);
		simplex2D.setStartPoint(tmi.getMeanPosition());
		long now = System.currentTimeMillis();
		TargetMotionResult[] locResult = simplex2D.runModel(tmi);
		TrackedGroupCrossingInfo crossInfo = null;
		LatLong ll = locResult[0].getLatLong();

		TrackedGroupDataUnit [] tgdu = new TrackedGroupDataUnit[detectionList.size()];
		for (int i = 0; i < detectionList.size(); i++) {
			tgdu[i] = (TrackedGroupDataUnit) detectionList.get(i);
		}
		// check the result is vaguely sensible. 
		if (tmi.getGPSReference().distanceToMiles(ll) < 1000) { 
			crossInfo = new TrackedGroupCrossingInfo(tgdu, locResult[0]);
		}

		for (int i = 0; i < detectionList.size(); i++) {
			if (detectionList.get(i).getLocalisation() == null) continue;
			((TrackedGroupDataUnit) detectionList.get(i)).setCrossing(crossInfo);
		}
		return crossInfo;
	}

}