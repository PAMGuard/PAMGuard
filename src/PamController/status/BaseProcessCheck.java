package PamController.status;


import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

/**
 * Basic checks of one or more processes in a module. The module
 * will have to add one of these process checks explicitly for 
 * every process it wan'ts to check, checks can include the
 * input being connected, the output being connected, data 
 * arriving at the input and data arriving at the output
 * @author Doug
 *
 */
public class BaseProcessCheck implements ProcessCheck {
	
	/**
	 * Process this applies to
	 */
	private PamProcess pamProcess;
	
	private Class inputClass; // can be null if no input checks
	
	private IOCounter inputCounter, outputCounter;
	
	private boolean neverIdle;

	/**
	 * Class to check a PAMProcess is doing it's stuff
	 * @param pamProcess process reference
	 * @param inputClass input class (can be null if there is no input)
	 * @param d minimum rate of input data
	 * @param e minimum rate of output data
	 */
	public BaseProcessCheck(PamProcess pamProcess, Class inputClass, double d, double e) {
		super();
		this.pamProcess = pamProcess;
		this.inputClass = inputClass;
		inputCounter = new IOCounter(d);
		outputCounter = new IOCounter(e);
		pamProcess.setProcessCheck(this);
	}

	@Override
	public void newInput(PamObservable obs, PamDataUnit data) {
		inputCounter.addOne();
	}

	@Override
	public void newOutput(PamObservable obs, PamDataUnit data) {
		outputCounter.addOne();
	}

	/**
	 * Set and averaging for the input counter. <br>This must me 
	 * greater than or equal to 1. A value of 1 means no averaging, 
	 * bigger values will be the number of measurements counts 
	 * are averaged over using a simple decaying average calculation
	 * @param averaging
	 */
	public void setInputAveraging(double averaging) {
		inputCounter.averaging = 1./averaging;
	}
	
	/**
	 * Set and averaging for the output counter. <br>This must me 
	 * greater than or equal to 1. A value of 1 means no averaging, 
	 * bigger values will be the number of measurements counts 
	 * are averaged over using a simple decaying average calculation
	 * @param averaging
	 */
	public void setOutputAveraging(double averaging) {
		outputCounter.averaging = 1./averaging;
	}
	
	public void reset() {
		inputCounter.count = 0;
		outputCounter.count = 0;
	}
	
	@Override
	public ModuleStatus getStatus() {
		if (inputClass != null) {
			if (pamProcess.getParentDataBlock() == null) {
				return new ModuleStatus(ModuleStatus.STATUS_ERROR, "No input data connection");
			}
		}
		int pamStatus = PamController.getInstance().getPamStatus();
		int runMode = PamController.getInstance().getRunMode();
		if (runMode == PamController.RUN_PAMVIEW) {
			return new ModuleStatus(ModuleStatus.STATUS_OK);
		}
		ModuleStatus moduleStatus = null;
		if (neverIdle) {
			return getRunningStatus();
		}
		else if (pamStatus == PamController.PAM_IDLE) {
			return getIdleStatus();
		}
		else if (pamStatus == PamController.PAM_STALLED) {
			return new ModuleStatus(ModuleStatus.STATUS_ERROR, "System has stalled");
		}
		else {
			return getRunningStatus();
		}
	}
	
	/**
	 * Get a status when the system is idle. Generally this is just a warning, 
	 * but it might get overridden for modules that are meant to operate 
	 * even when PAM is idle. 
	 * @return A status for when the system is idel. 
	 */
	public ModuleStatus getIdleStatus() {
		return new ModuleStatus(ModuleStatus.STATUS_WARNING, "System is idle");
	}
	
	/**
	 * 
	 * @return a status for if the idle stats was null
	 */
	public ModuleStatus getRunningStatus() {
		long now = PamCalendar.getTimeInMillis();
		double rate = inputCounter.getRateAndSet(now);
		if (rate < inputCounter.minRate) {
			if (rate == 0) {
			return new ModuleStatus(ModuleStatus.STATUS_ERROR, 
					"No input data arriving in "  + pamProcess.getProcessName());
			}
			else {
				return new ModuleStatus(ModuleStatus.STATUS_WARNING, 
						String.format("Slow data rate of %3.1f/s arriving in %s", 
								rate, pamProcess.getProcessName()));
			}
		}
		rate = outputCounter.getRateAndSet(now);
		if (rate < outputCounter.minRate) {
			if (rate == 0) {
			return new ModuleStatus(ModuleStatus.STATUS_ERROR, 
					"No output data sent from "  + pamProcess.getProcessName());
			}
			else {
				return new ModuleStatus(ModuleStatus.STATUS_WARNING, 
						String.format("Slow data rate of %3.1f/s from %s", 
								rate, pamProcess.getProcessName()));
			}
		}
		return new ModuleStatus(ModuleStatus.STATUS_OK);
	}
	
	public boolean isNeverIdle() {
		return neverIdle;
	}

	public void setNeverIdle(boolean neverIdle) {
		this.neverIdle = neverIdle;
	}

	public IOCounter getInputCounter() {
		return inputCounter;
	}

	public IOCounter getOutputCounter() {
		return outputCounter;
	}

	public class IOCounter {
		double count;
		double averaging = 1.0;
		private long lastGet;
		private double minRate;
		private double lastReport;
		private double maxRate = -1;
		
		/**
		 * Create a counter and set it's minimum acceptable data rate
		 * @param minRate minimum data rate
		 */
		public IOCounter(double minRate) {
			this.minRate = minRate;
		}

		/**
		 * Create a counter and set it's minimum and maximum acceptable data rate
		 * @param minRate minimum data rate
		 * @param maxRate maximum data rate
		 */
		public IOCounter(double minRate, double maxRate) {
			super();
			this.minRate = minRate;
			this.setMaxRate(maxRate);
		}

		private void addOne() {
			count += averaging;
		}
		
		public double getRateAndSet(long timeMillis) {
			if (timeMillis - lastGet < 1000) {
				return lastReport;
			}
			double r = count;
			r /= ((timeMillis-lastGet)/1000.);
			lastGet = timeMillis;
			count *= (1.-averaging);
			lastReport = r;
			return r;
		}
		
		public void reset(long timeMillis) {
			count = 0;
			lastGet = timeMillis;
		}

		public double getMinRate() {
			return minRate;
		}

		public void setMinRate(double minRate) {
			this.minRate = minRate;
		}

		/**
		 * @return the maxRate
		 */
		public double getMaxRate() {
			return maxRate;
		}

		/**
		 * @param maxRate the maxRate to set
		 */
		public void setMaxRate(double maxRate) {
			this.maxRate = maxRate;
		}
		
		
	}
	

}
