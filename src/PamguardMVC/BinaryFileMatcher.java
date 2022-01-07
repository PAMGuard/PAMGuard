package PamguardMVC;

import binaryFileStorage.DataUnitFileInformation;

/**
 * Match data based on binary file information.
 * <p>Must supply two parameters. First is binary file name
 * second is click number in file. 
 * @author Doug Gillespie
 *
 */
public class BinaryFileMatcher implements DataUnitMatcher {

	@Override
	public int match(PamDataUnit dataUnit, Object... criteria) {
		if (criteria == null || criteria.length < 2) {
			return 1;
		}
		String fileName = (String) criteria[0];
		if (fileName == null) {
			return 1;
		}
		int clickNumber = (Integer) criteria[1];
		DataUnitFileInformation fileInfo = dataUnit.getDataUnitFileInformation();
		if (fileInfo == null) {
			return 1;
		}
		int comp = fileInfo.getShortFileName(fileName.length()).compareTo(fileName);
		if (comp != 0) {
			return comp;
		}
		return Long.signum(clickNumber-fileInfo.getIndexInFile());
//		return (fileInfo.getShortFileName(fileName.length()).equals(fileName) &&
//				clickNumber == fileInfo.getIndexInFile());
	}

}
