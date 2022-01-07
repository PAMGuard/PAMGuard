package classifier;

import java.io.Serializable;
import java.util.Arrays;

import Jama.Matrix;

/**
 * Many classification functions need the mean values of
 * each parameter from a group of training data. 
 * <p> This class provides a set of functions which 
 * will get the means by group, and also return 
 * larger matrixes of the mean values repeated by 
 * a grouping index or repeated for a single group which
 * can be used in classifier training and classifier application
 * @author Doug Gillespie
 * @see Matrix
 *
 */
public class GroupMeans implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private Matrix groupMeans;

	private int[] uniqueGroups;
	
	private int[] groupSize;

	/**
	 * Constructor works out the number of unique groups
	 * in group and then constructs a smaller matrix of 
	 * means of each parameter by group. 
	 * @param data matrix of training data
	 * @param group group indexes
	 */
	public GroupMeans(Matrix data, int[] group) {
		makeGroupMeans(data, group);
	}

	/**
	 * 
	 * @return matrix of means by training group
	 */
	public Matrix getGroupMeans() {
		return groupMeans;
	}

	/**
	 * Get list of unique groups in the data
	 * @return list of unique groups in the data
	 */
	public int[] getUniqueGroups() {
		return uniqueGroups;
	}
	
	/**
	 * 
	 * @return the number of entries in each group.
	 */
	public int[] getGroupSize() {
		return groupSize;
	}

	/**
	 * 
	 * @return The number of unique groups in the data
	 */
	public int getNumGroups() {
		if (uniqueGroups == null) {
			return 0;
		}
		return uniqueGroups.length;
	}

	/**
	 * Create a matrix of means, repeated multiple
	 * times (e.g. so it can be subtracted off a set
	 * of training data). 
	 * @param groups grouping to apply. Must contain only the 
	 * same entries as in the group parameter passed to the constructor. 
	 * @return matrix of grouped means. 
	 */
	public Matrix getGroupMeans(int[] groups) {
		if (groupMeans == null) {
			return null;
		}
		int nCol = groupMeans.getColumnDimension(); 
		int[] groupIndex = getGroupIndex(groups);
		
		Matrix groupedMeans = new Matrix(groups.length, nCol);
		for (int iR = 0; iR < groups.length; iR++) {
			for (int iC = 0; iC < nCol; iC++) {
				groupedMeans.set(iR, iC, groupMeans.get(groupIndex[iR], iC));
			}
		}

		return groupedMeans;
	}

	/**
	 * Create a matrix of means for a single group.
	 * @param group index of group
	 * @param n number of rows of data to create
	 * @return matrix with n identical rows of group
	 */
	public Matrix getGroupMeans(int group, int n) {
		if (groupMeans == null) {
			return null;
		}
		int nCol = groupMeans.getColumnDimension(); 
		int groupIndex = getGroupIndex(group);
		Matrix groupedMeans = new Matrix(n, nCol);
		for (int iR = 0; iR < n; iR++) {
			for (int iC = 0; iC < nCol; iC++) {
				groupedMeans.set(iR, iC, groupMeans.get(groupIndex, iC));
			}
		}

		return groupedMeans;
	}

	/**
	 * Takes the raw training data, works out what gorups are present 
	 * in group and works out the means for each group. 
	 * @param data training data Matrix
	 * @param group grouping variable 
	 */
	private void makeGroupMeans(Matrix data, int[] group) {
		int nRow = data.getRowDimension();
		int nCol = data.getColumnDimension();
		uniqueGroups = unique(group);
		int nGroup = uniqueGroups.length;
		groupMeans = new Matrix(nGroup, nCol);
		groupSize = new int[nGroup];

		double tot;
		int n;
		for (int iC = 0; iC < nCol; iC++) {
			for (int iG = 0; iG < uniqueGroups.length; iG++) {
				tot = 0;
				n = 0;
				for (int iR = 0; iR < nRow; iR++) {
					if (group[iR] != uniqueGroups[iG]) {
						continue;
					}
					tot += data.get(iR, iC);
					n++;
				}
				tot /= n;
				groupMeans.set(iG, iC, tot);
				groupSize[iG] = n;
			}
		}
	}

	/**
	 * The group parameter passed to the constructor
	 * can contain grouping values which may not start at 0
	 * and may not be continuous. 
	 * <p>
	 * Therefore, when constructing new means matrixes, it is necessary
	 * to use the index of each group parameter to get the right values 
	 * out of the groupMeans matrix. 
	 * @param iD group parameter
	 * @return group parameter index in unique list. 
	 */
	private int getGroupIndex(int iD) {
		return Arrays.binarySearch(uniqueGroups, iD);
	}
	
	/**
	 * Gets the index of a group id (these will often be the same !)
	 * @param uniqueGroups list of unique groups (sorted)
	 * @param iD group ID to find
	 * @return index in group list. 
	 */
	public static int getGroupIndex(int[] uniqueGroups, int iD) {
		return Arrays.binarySearch(uniqueGroups, iD);
	}

	/**
	 * As getGroupIndex(iD) but for arrays. <p>
	 * The group parameter passed to the constructor
	 * can contain grouping values which may not start at 0
	 * and may not be continuous. 
	 * <p>
	 * Therefore, when constructing new means matrixes, it is necessary
	 * to use the index of each group parameter to get the right values 
	 * out of the groupMeans matrix. 
	 * 
	 * @param iD group parameter
	 * @return group parameter index in unique list. 
	 */
	private int[] getGroupIndex(int[] iD) {
		return getGroupIndex(uniqueGroups, iD);
	}
	
	/**
	 * 
	 * Gets the index of a group id (these will often be the same !)
	 * @param uniqueGroups sorted list of unique groups
	 * @param iD list of group id's to find indexes of
	 * @return list of indexes. 
	 */
	public static int[] getGroupIndex(int[] uniqueGroups, int[] iD) {
		int[] inds = new int[iD.length];
		/*
		 * If the list is short, then search for each one
		 * individually. If it's long, then it's going 
		 * to be quicker to loop through it once for 
		 * each unique value. 
		 */
		if (iD.length < uniqueGroups.length * 2) {
			for (int i = 0; i < iD.length; i++) {
				inds[i] = getGroupIndex(uniqueGroups, iD[i]);
			}
		}
		else {
			for (int i = 0; i < iD.length; i++) {
				inds[i] = -1;
			}
			for (int iG = 0; iG < uniqueGroups.length; iG++) {
				for (int i = 0; i < iD.length; i++) {
					if (iD[i] == uniqueGroups[iG]) {
						inds[i] = uniqueGroups[iG];
					}
				}
			}
		}

		return inds;
	}
	
	

	/**
	 * Get a unique list of items in the training group
	 * <p>
	 * e.g. if group was {1, 9, 3, 1, 4, 9, 4} then the
	 * return value would be {1, 3, 4, 9}
	 * @param groups group data which will contain many instances of few different values. 
	 * @return a shorter array of unique values sorted by size. 
	 */
	static public int[] unique(int[] groups) {
		if (groups == null || groups.length == 0) {
			return null;
		}
		int[] sorted = groups.clone(); 
		Arrays.sort(sorted);
		int elCount = 1;
		// go through once and work out how many different elements there are
		for (int i = 1; i < sorted.length; i++) {
			if (sorted[i] != sorted[i-1]) {
				elCount++;
			}
		}
		// go through again and get those elements. 
		int[] uniqueGroup = new int[elCount];
		uniqueGroup[0] = sorted[0];
		int iU = 0;
		for (int i = 1; i < sorted.length; i++) {
			if (sorted[i] != sorted[i-1]) {
				uniqueGroup[++iU] = sorted[i];
			}
		}
		return uniqueGroup;
	}

	@Override
	public GroupMeans clone() {
		try {
			return (GroupMeans) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
