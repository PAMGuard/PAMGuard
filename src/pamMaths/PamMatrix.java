package pamMaths;

import Jama.Matrix;


/**
 * Some functions to compliment those of the Jamma 
 * Matrix class
 * @author Doug Gillespie
 * @see Matrix
 */
public class PamMatrix {

	public static String matrixToString(Matrix m, String format, String delimiter, String[] colHeads, String[] rowHeads, boolean fixedWidth) {
		if (m == null) {
			return "null matrix";
		}
		String str = new String();
		int minWidth = 0;
		if (fixedWidth) {
			minWidth = 4; 
		}
		else {
			if (colHeads != null) {
				for (int i = 0; i < colHeads.length; i++) {
					minWidth = Math.max(minWidth, colHeads[i].length());
				}
			}
			minWidth += 1+delimiter.length();
		}
		if (colHeads != null) {
			if (rowHeads != null) {
				str += delimiter;
			}
			for (int i = 0; i < colHeads.length; i++) {
				str += createString(colHeads[i], delimiter, minWidth, i==colHeads.length-1);
			}
			str += "\r\n";
		}
		int nR = m.getRowDimension();
		int nC = m.getColumnDimension();
		for (int iR = 0; iR < nR; iR++) {
			if (rowHeads != null && rowHeads.length > iR) {
				str += createString(rowHeads[iR], delimiter, minWidth, false);
			}
			for (int iC = 0; iC < nC; iC++) {
				str += createString(format, m.get(iR, iC), delimiter, minWidth, iC == nC-1);
			}
			str += "\r\n";
		}
		return str;
	}

//	private static String createString(String subString, String delimiter, int minWidth) {
//		return createString(subString, delimiter, minWidth, false);
//	}
	private static String createString(String subString, String delimiter, int minWidth, boolean isLast) {
		String str = new String(subString);
		if (!isLast) {
			if (delimiter != null) {
				str += delimiter;
			}
			while (str.length() < minWidth) {
				str += " ";
			}
		}
		return str;
	}
	
	private static String createString(String format, double number, String delimiter, int minWidth, boolean isLast) {
		boolean isInteger = format.contains("d");
		String str;
		if (isInteger) {
			str = String.format(format, (int) number);
		}
		else {
			str = String.format(format, number);
		}
		return createString(str, delimiter, minWidth, isLast);
	}
}
