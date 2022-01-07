package alarm;


/**
 * BAse class for any alarm data source which is returning 
 * some kind of data measured in dB, which needs 
 * to be added in a different way to normal. 
 * @author Doug Gillespie
 *
 */
public abstract class AlarmDecibelCounter extends AlarmCounter {


	public AlarmDecibelCounter(AlarmControl alarmControl) {
		super(alarmControl);
	}


	@Override
	public double addCount(double currentValue, double countToAdd, int countType) {
		if (countType == AlarmParameters.COUNT_SIMPLE) {
			return currentValue + 1;
		}
		if (currentValue == 0) {
			return countToAdd;
		}
		double e = dBToEnergy(currentValue) + dBToEnergy(countToAdd);
		return energyTodB(e);
	}


	@Override
	public double subtractCount(double currentValue, double countToSubtract,
			int countType) {
		if (countType == AlarmParameters.COUNT_SIMPLE) {
			return currentValue - 1;
		}
		double e = dBToEnergy(currentValue) - dBToEnergy(countToSubtract);
		return energyTodB(e);
	}

	/**
	 * Convert a value in decibels to energy. 
	 * @param db value in dB
	 * @return energy
	 */
	private double dBToEnergy(double db) {
		if (db == Double.NEGATIVE_INFINITY) {
			return 0;
		}
		else {
			return Math.pow(10., db/10.);
		}
	}
	
	/**
	 * Convert an energy value to decibels
	 * @param energy energy value
	 * @return value in dB
	 */
	private double energyTodB(double energy) {
		if (energy <= 0.) {
			return Double.NEGATIVE_INFINITY;
		}
		else {
			return 10.*Math.log10(energy);
		}
	}

}
