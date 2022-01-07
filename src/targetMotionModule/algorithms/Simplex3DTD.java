package targetMotionModule.algorithms;

import java.awt.Color;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import pamMaths.PamVector;
import targetMotionModule.TargetMotionInformation;
import targetMotionModule.TargetMotionLocaliser;
import targetMotionModule.TargetMotionResult;
import Array.ArrayManager;
import GPS.GpsData;
import Localiser.algorithms.genericLocaliser.simplex.old.Simplex;
import PamUtils.LatLong;
import PamguardMVC.debug.Debug;

public class Simplex3DTD extends AbstractTargetMotionModel {
	
	private TargetMotionLocaliser targetMotionLocaliser;
	protected Simplex simplex;
	private ArrayList<ArrayList<Double>> timeDelays;
	private ArrayList<ArrayList<Double>> timeDelaysErrors;
	private ArrayList<ArrayList<Point3f>> hydrophoneArray;
	private float sampleRate;
	private double speedOfSound;
	private ArrayManager arrayManager;

	public Simplex3DTD(TargetMotionLocaliser targetMotionLocaliser) {
		this. targetMotionLocaliser=targetMotionLocaliser;
		this.arrayManager = ArrayManager.getArrayManager();
		this. simplex=new Simplex();
	}

	@Override
	public String getName() {
		return "3D simplex optimisation TD";
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasParameters() {
		return false;
	}

	@Override
	public boolean parametersDialog() {
		return false;
	}

	@Override
	public TargetMotionResult[] runModel(
			TargetMotionInformation targetMotionInformation) {
		
				if (targetMotionInformation == null) return null;
				if (targetMotionInformation.getCurrentDetections().size() <=1) return null;
				
				
				timeDelays=targetMotionInformation.getTimeDelays();
				timeDelaysErrors=targetMotionInformation.getTimeDelayErrors();
				hydrophoneArray=targetMotionInformation.getHydrophonePos();
				sampleRate=targetMotionInformation.getCurrentDetections().get(0).getParentDataBlock().getSampleRate();
				speedOfSound=arrayManager.getCurrentArray().getSpeedOfSound();
				
				Debug.out.println("timeDelays: "+timeDelays);
				Debug.out.println("hydrophoneArray: "+hydrophoneArray);
				Debug.out.println("sampleRate: "+sampleRate);
				Debug.out.println("timeDelaysErrors: "+timeDelaysErrors);
				Debug.out.println("speedOfSound: "+speedOfSound);
				
				
				simplex.setTimeDelays(timeDelays);
				simplex.setHydrophonePos(hydrophoneArray);
				simplex.setSampleRate(sampleRate);
				simplex.setTimeDelaysErrors(timeDelaysErrors);
				simplex.setSoundSpeed(speedOfSound);
				
				simplex.runAlgorithm();
				
				TargetMotionResult[] tmResults = new TargetMotionResult[1];
				
				LatLong ll = targetMotionInformation.metresToLatLong(new PamVector(simplex.getLocation()[0],simplex.getLocation()[1],simplex.getLocation()[2]));
				
				tmResults[0] = new TargetMotionResult(targetMotionInformation.getTimeMillis(),this, ll, 0, simplex.getChi2());
				tmResults[0].setErrorX(simplex.getLocationErrors()[0]);
				tmResults[0].setErrorY(simplex.getLocationErrors()[1]);
				tmResults[0].setErrorZ(simplex.getLocationErrors()[2]);
				GpsData beamPos=targetMotionInformation.getBeamLatLong(ll);
				tmResults[0].setBeamLatLong(new GpsData(beamPos));
				tmResults[0].setBeamTime(targetMotionInformation.getBeamTime(beamPos));
				tmResults[0].setStartLatLong(targetMotionInformation.getCurrentDetections().get(0).getOriginLatLong(false));
				tmResults[0].setEndLatLong(targetMotionInformation.getCurrentDetections().get(targetMotionInformation.getCurrentDetections().size()-1).getOriginLatLong(false));
				tmResults[0].setPerpendicularDistance(ll.distanceToMetres(tmResults[0].getBeamLatLong()));
				tmResults[0].setPerpendicularDistanceError(Math.sqrt(Math.pow(simplex.getLocationErrors()[0],2)+Math.pow(simplex.getLocationErrors()[1],2)));
				tmResults[0].setChi2(simplex.getChi2());
				tmResults[0].setReferenceHydrophones(targetMotionInformation.getReferenceHydrophones());
				
				return tmResults;
				
	}

	private Color symbolColour = new Color(180,0,215);

	@Override
	Color getSymbolColour() {
		return symbolColour;
	}



}
