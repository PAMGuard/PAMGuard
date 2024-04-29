package group3dlocaliser;

import java.util.List;

import Array.ArrayManager;
import Localiser.algorithms.locErrors.LocaliserError;
import Localiser.detectionGroupLocaliser.GroupLocResult;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.AbstractLocalisation;
import PamUtils.CPUMonitor;
import PamView.GroupedDataSource;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.crossedbearing.CrossedBearingGroupLocaliser;
import group3dlocaliser.grouper.DetectionGroupMonitor;
import group3dlocaliser.grouper.DetectionGroupedSet;
import group3dlocaliser.grouper.DetectionGrouper;
import group3dlocaliser.logging.Group3DLogging;
import group3dlocaliser.swinggraphics.Group3DOverlayDraw;
import group3dlocaliser.swinggraphics.Group3DSymbolManager;

public class Group3DProcess extends PamProcess implements DetectionGroupMonitor {

	private Group3DLocaliserControl group3DControl;

	private LocaliserAlgorithm3D localiserAlgorithm3D;

	private DetectionGrouper detectionGrouper;

	private Group3DDataBlock group3dDataBlock;

	private Group3DLogging group3dLogging;

	private CPUMonitor cpuMonitor = new CPUMonitor();

	/**
	 * Process for 3D localisation based on multiple hydrophone clusters.
	 * <p>
	 * (It is possible to have a single hydrophone in a cluster, so really this is
	 * just a new, cleaner, implementation of a 3D localiser).
	 * 
	 * @param group3DControl
	 */
	public Group3DProcess(Group3DLocaliserControl group3DControl) {
		super(group3DControl, null);
		this.group3DControl = group3DControl;
		localiserAlgorithm3D = group3DControl.getAlgorithmProviders().get(0);
		detectionGrouper = new DetectionGrouper(this);
		group3dDataBlock = new Group3DDataBlock(group3DControl.getUnitName() + " Localisations", this, 0,
				group3DControl);
		addOutputDataBlock(group3dDataBlock);
		group3dDataBlock.setOverlayDraw(new Group3DOverlayDraw(group3dDataBlock));
		group3dDataBlock.setPamSymbolManager(new Group3DSymbolManager(group3DControl, group3dDataBlock,
				Group3DOverlayDraw.defaultSymbol.getSymbolData()));
		group3dLogging = new Group3DLogging(group3DControl, group3dDataBlock);
		group3dDataBlock.SetLogging(group3dLogging);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		Group3DParams grid3dParams = group3DControl.getGrid3dParams();
		PamDataBlock sourceBlock = PamController.getInstance().getDataBlockByLongName(grid3dParams.getSourceName());
		setParentDataBlock(sourceBlock);
		int arrayShape = ArrayManager.getArrayManager().getCurrentArray().getArrayShape();

		LocaliserAlgorithm3D algorithmProvider = group3DControl.findAlgorithm(grid3dParams.getAlgorithmName());
		if (algorithmProvider == null) {
			return;
		}
		localiserAlgorithm3D = algorithmProvider;

		group3DControl.getG3DOfflineTask().setParentDataBlock(sourceBlock);

		if (sourceBlock != null) {
			if (localiserAlgorithm3D != null) {
				localiserAlgorithm3D.prepare(sourceBlock);
				group3dLogging.setLocalisationAddon(localiserAlgorithm3D.getSQLLoggingAddon(arrayShape));
			}
			GroupedSourceParameters groupedSourceParams = null;
			if (sourceBlock instanceof GroupedDataSource) {
				groupedSourceParams = ((GroupedDataSource) sourceBlock).getGroupSourceParameters();
			} else {
				groupedSourceParams = null;// grid3dParams.getGroupedSourceParams();
			}
			if (groupedSourceParams == null) {
				return;
			}
			detectionGrouper.setGroupedSourceParameters(groupedSourceParams, sourceBlock.getSampleRate());
			detectionGrouper.setDataSelector(sourceBlock.getDataSelector(group3DControl.getDataSelectorName(), false));
			int channelMap = groupedSourceParams.getChanOrSeqBitmap();
			int phoneMap = sourceBlock.getHydrophoneMap();
			int newShape = ArrayManager.getArrayManager().getArrayType(phoneMap);

		}
		detectionGrouper.setDetectionGrouperParams(group3DControl.getGrid3dParams().getGrouperParams());

	}

	@Override
	public void pamStart() {
		cpuMonitor.reset();
	}

	@Override
	public void pamStop() {
		
		///need this here to close the group
		this.detectionGrouper.closeMotherGroup();
		String prf = String.format("%s %s ", group3DControl.getUnitName(), localiserAlgorithm3D.getName());
		System.out.println(cpuMonitor.getSummary(prf));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			prepareProcess();
		}
		super.notifyModelChanged(changeType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#masterClockUpdate(long, long)
	 */
	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		if (detectionGrouper == null)
			return;
		detectionGrouper.masterClockUpdate(timeMilliseconds, sampleNumber);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable,
	 * PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit pamDataUnit) {
		if (detectionGrouper == null)
			return;
//		long t1 = System.currentTimeMillis();
//		System.out.printf("New %s in Group3Dprocess", o.toString());
		detectionGrouper.newData(pamDataUnit);
//		long t2 = System.currentTimeMillis();
//
//		System.out.printf(" done in %d millis\n", t2-t1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see PamguardMVC.PamProcess#updateData(PamguardMVC.PamObservable,
	 * PamguardMVC.PamDataUnit)
	 */
	@Override
	public void updateData(PamObservable o, PamDataUnit pamDataUnit) {
//		super.updateData(o, pamDataUnit);
//		if (detectionGrouper == null) return;
//		detectionGrouper.newData(pamDataUnit);
	}

	@Override
	public void newGroupedDataSet(DetectionGroupedSet detectionGroupedSet1) {
		
		DetectionGroupedSet detectionGroupedSet = this.localiserAlgorithm3D.preFilterLoc(detectionGroupedSet1); 
		
		int nGroups = detectionGroupedSet.getNumGroups();
		AbstractLocalisation abstractLocalisation;
		GroupLocalisation groupLocalisation;
		AbstractLocalisation bestLocalisation = null;
		GroupLocResult bestResult = null;
		int bestSet = -1;

		boolean logAll = false;

		// will have to make a data unit for each group now...
		Group3DDataUnit[] group3dDataUnits = new Group3DDataUnit[nGroups];

//		System.out.println("Enter newGRoupedDataSet with groups: " + detectionGroupedSet.getNumGroups());
//		if (detectionGroupedSet.hasUID(14045004731L)) {
//			Debug.out.println(" found it");
//		}

		for (int i = 0; i < nGroups; i++) {
			List<PamDataUnit> groupSet = detectionGroupedSet.getGroup(i);
			group3dDataUnits[i] = new Group3DDataUnit(detectionGroupedSet.getGroup(i));
//			if (groupSet.size() == 3) {
//				System.out.println("Group size: " + groupSet.size());
//			}

			cpuMonitor.start();
			// groupLocalisation =
			// localiserAlgorithm3D.process(detectionGroupedSet.getGroup(i));
			abstractLocalisation = localiserAlgorithm3D.runModel(group3dDataUnits[i], null, false);
			if (abstractLocalisation == null) {
				System.out.println("Group 3D process null localisation from " + localiserAlgorithm3D.getName());
				continue;
			}
			// log all outputs ..
			if (logAll) {
				Group3DDataUnit newDataUnit = group3dDataUnits[i];
				newDataUnit.setLocalisation(abstractLocalisation);
				group3dDataBlock.addPamData(newDataUnit);
				if (group3DControl.isViewer()) {
					// call explicityly since it won't happen in normal mode.
					logViewerData(newDataUnit);
				}
			}
		
			System.out.println("Ran localisation " + i + " " + localiserAlgorithm3D.getName() + "  got: " + abstractLocalisation.getLatLong(0) + "  " + abstractLocalisation.getHeight(0) + " Error: " +  abstractLocalisation.getLocError(0));

			if (abstractLocalisation instanceof GroupLocalisation) {
				groupLocalisation = (GroupLocalisation) abstractLocalisation;
				
				cpuMonitor.stop();
				if (groupLocalisation != null) {
					groupLocalisation.sortLocResults();
					GroupLocResult locResult = groupLocalisation.getGroupLocaResult(0);
					
					if (locResult == null) {
						continue;
					}
					if (bestResult == null) {
						bestResult = locResult;
						bestLocalisation = groupLocalisation;
						bestSet = i;
					} else if (isBetter(groupSet, detectionGroupedSet.getGroup(bestSet), locResult, bestResult)) {
						bestResult = locResult;
						bestLocalisation = groupLocalisation;
						bestSet = i;
					}
//					else if (groupSet.size() > detectionGroupedSet.getGroup(bestSet).size()) {
//						//					Take the result with the most detections.
//						bestResult = locResult;
//						bestLocalisation = groupLocalisation;
//						bestSet = i;
//					}
//					else if (groupSet.size() == detectionGroupedSet.getGroup(bestSet).size() &&
//							locResult.compareTo(bestResult) < 0) {
//						//					Take the result with the lowest Chi2 / AIC.
//						bestResult = locResult;
//						bestLocalisation = groupLocalisation;
//						bestSet = i;
//					}
				}
			} else if (bestLocalisation==null) {
				//note it is important here to make sure bestLoclaisation is null. If we have an array which has a linear
				//compenent than a set of time delays of only the linear system may return a linear loclaisation in which case, without a
				// null check, this will always override a group loclaisation. 
				bestLocalisation = abstractLocalisation;
				bestSet = i;
			}
			
			
		}
		if (bestLocalisation != null && logAll == false) {
			// best make and output a data unit !
			// List<PamDataUnit> bestGroup = detectionGroupedSet.getGroup(bestSet);
			// Group3DDataUnit newDataUnit = new
			// Group3DDataUnit(bestGroup.get(0).getBasicData().clone());
			// for (int i = 0; i < bestGroup.size(); i++) {
			// newDataUnit.addSubDetection(bestGroup.get(i));
			// }
			Group3DDataUnit newDataUnit = group3dDataUnits[bestSet];
			newDataUnit.setLocalisation(bestLocalisation);
			
			System.out.println("Set click localisation: " + bestSet + "  " + bestLocalisation.getRange(0) + "  " + bestLocalisation.getLatLong(0));
			group3dDataBlock.addPamData(newDataUnit);
			if (group3DControl.isViewer()) {
				// call explicityly since it won't happen in normal mode.
				logViewerData(newDataUnit);
			}
		}
	}

	/**
	 * Return true if the group1 and associated result 1 are better than group2 and
	 * result2
	 * 
	 * @param group1
	 * @param group2
	 * @param result1
	 * @param result2
	 * @return true if group 1 is better than group 2. 
	 */
	private boolean isBetter(List<PamDataUnit> group1, List<PamDataUnit> group2, GroupLocResult result1,
			GroupLocResult result2) {
		int nch1 = group1.size();
		double chi1 = result1.getChi2() / nch1;
		int nch2 = group2.size();
		double chi2 = result2.getChi2() / nch2;
		Double errMag1 = null, errMag2 = null;
		LocaliserError err = result1.getLocError();
		
		//if one has location information and the other does not, choose the one with the 
		System.out.println("LAT LONG TEST: " + result1.getLatLong() + "  " + result2.getLatLong()); 
		if (result1.getLatLong()==null && result2.getLatLong()!=null) {
			return false;
		}
		
		if (result2.getLatLong() == null && result1.getLatLong() != null) {
			return true;
		}
		
		if (err != null) {
			errMag1 = err.getErrorMagnitude();
			if (errMag1.isInfinite()) {
				errMag1 = null; // invalid error so pretend it's not even there.
			}
		}
		err = result2.getLocError();
		if (err != null) {
			errMag2 = err.getErrorMagnitude();
			if (errMag2.isInfinite()) {
				errMag2 = null; // invalid error so pretend it's not even there.
			}
		}
		if (errMag1 != null && errMag2 == null) {
			return true; // r2 didn't even get an error estimate.
		} else if (errMag1 == null && errMag2 != null) {
			return false;
		}
		/*
		 * If we get here, then either both have valid errors, or neither do.
		 */

		if (nch1 > nch2) {
			return true;
		}
		if (nch1 < nch2) {
			return false;
		}
//		else if (nch1 > nch1))
//		if (nch1 != nch2) {
//			System.out.printf("Group 1 %d dets chi2=%3.1f, Group2 %d dets chi2=%3.1f\n", nch1, chi1, nch2, chi2);
//		}
		return chi1 < chi2;
	}

//	
//	private boolean isBetterResult(GroupLocResult newResult, GroupLocResult oldResult) {
//		if (oldResult == null) {
//			return true;
//		}
//		newResult.get
//		
//		return false;
//	}

	private void logViewerData(Group3DDataUnit newDataUnit) {
		PamConnection con = DBControlUnit.findConnection();
		if (con == null || con.getConnection() == null) {
			return;
		}
		group3dLogging.logData(con, newDataUnit);

	}

	/**
	 * @return the group3dDataBlock
	 */
	public Group3DDataBlock getGroup3dDataBlock() {
		return group3dDataBlock;
	}

	/**
	 * @return the localiserAlgorithm3D
	 */
	public LocaliserAlgorithm3D getLocaliserAlgorithm3D() {
		return localiserAlgorithm3D;
	}

}
