package effort;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;

public class EffortDataUnit extends PamDataUnit {

	/**
	 * Value for effort end to say that effort is ongoing. 
	 */
	public static final long ONGOINGEFFORT = Long.MAX_VALUE/2;
	
	private PamDataUnit referenceDataUnit;
	private EffortProvider effortProvider;
	

	public EffortDataUnit(EffortProvider effortProvider, PamDataUnit referenceDataUnit, long effortStart, long effortEnd) {
		super(effortStart);
		getBasicData().setEndTime(effortEnd);
		this.effortProvider = effortProvider;
		this.setReferenceDataUnit(referenceDataUnit);
	}

	/**
	 * 
	 * @return Start of effort period. 
	 */
	public long getEffortStart() {
		return getTimeMilliseconds();
	}


	/**
	 * 
	 * @return End of effort period. 
	 */
	public long getEffortEnd() {
		return getEndTimeInMilliseconds();
	}

	/**
	 * Set the effort end time
	 * @param endTime
	 */
	public void setEffortEnd(long endTime) {
		getBasicData().setEndTime(endTime);
	}
	
	/**
	 * 
	 * @param timeMilliseconds
	 * @return time is in effort period
	 */
	public boolean inEffort(long timeMilliseconds) {
		return (timeMilliseconds >= getEffortStart() && timeMilliseconds <= getEffortEnd());
	}
	
	/**
	 * Description of the effort, for tool tips, etc. 
	 * @return
	 */
	public String getEffortDescription() {
		return this.getSummaryString();
	}

	/**
	 * @return the referenceDataUnit
	 */
	public PamDataUnit getReferenceDataUnit() {
		return referenceDataUnit;
	}

	/**
	 * @param referenceDataUnit the referenceDataUnit to set
	 */
	public void setReferenceDataUnit(PamDataUnit referenceDataUnit) {
		this.referenceDataUnit = referenceDataUnit;
	}

	@Override
	public String toString() {
		return String.format("%s %s to %s",effortProvider.getName(), PamCalendar.formatDBDateTime(getEffortStart()), PamCalendar.formatDBDateTime(getEffortEnd()));
	}

	@Override
	public int compareTo(PamDataUnit o) {
		if (o instanceof EffortDataUnit) {
			EffortDataUnit oth = (EffortDataUnit) o;
			return (int) (oth.getEffortStart()-getEffortStart());
		}
		return 0;
	}

}
