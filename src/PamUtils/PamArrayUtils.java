package PamUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

/**
 * Some math and utility functions for arrays and Lists. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PamArrayUtils {


	/**
	 * Calculate the mean of one dimension within a list of points. <i>e.g.</i> the points might be a list of [x y z] co-ordinates in 
	 * which case the dim=0 would return the mean of all x points. 
	 * @param array - a list of points
	 * @param InitialtoIgnorePercentage: ignore the first percentage of results
	 * @param dim - the dimension of the point to calculate the average for
	 * @return the mean of one dimension of the list of the points. 
	 */
	public static double mean(ArrayList<float[]> array, double InitialtoIgnorePercentage, int dim){

		double meanTotal=0;
		int n=0;
		int forStart=(int) Math.round((InitialtoIgnorePercentage)*array.size());

		for (int i=forStart; i<array.size();i++){
			meanTotal+= array.get(i)[dim];
			n++;
		}

		//		 System.out.println("Array size: "+array.size()+ "  n size: "+ n);
		double mean=meanTotal/n;
		return mean;
	}
	

	/**
	 * Calculate the standard deviation of an array of doubles, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array
	 * @param initialtoIgnorePercentage- percentage of initial values to ignore.
	 * @return standard deviation of array. 
	 */
	public static double std(ArrayList<float[]> array, double initialtoIgnorePercentage, int dim){
		double std=0.0;

		int n=0;
		int forStart=(int) Math.round((initialtoIgnorePercentage)*array.size());

		double meanTotal= mean(array,  initialtoIgnorePercentage,  dim);

		//calculate standard deviation
		for (int k=forStart;k<array.size(); k++){
			std+=Math.pow((array.get(k)[dim]-meanTotal),2);
		}

		//standard deviation
		std=Math.sqrt(std/(n-1));

		return std;
	}

	/**
	 * Calculate the mean of an array of double values, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array of float values.
	 * @param InitialtoIgnorePercentage - the percentahe of results to ignore.
	 * @return mean of the array values
	 */
	public static Double mean(ArrayList<? extends Number> array, double InitialtoIgnorePercentage){

		double meanTotal=0;
		int n=0;
		int forStart=(int) Math.round((InitialtoIgnorePercentage)*array.size());

		for (int i=forStart; i<array.size();i++){
			meanTotal+= array.get(i).doubleValue();
			n++;
		}

		//		 System.out.println("Array size: "+array.size()+ "  n size: "+ n);
		double mean=meanTotal/n;
		return mean;
	}

	/**
	 * Calculate the standard deviation of an array of doubles, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array
	 * @param initialtoIgnorePercentage- percentage of initial values to ignore.
	 * @return standard deviation of array. 
	 */
	public static double std(ArrayList<? extends Number> array, double initialtoIgnorePercentage){
		double Std=0.0;
		double meanTotal=0.0;

		double stndDev;
		int n=0;
		int forStart=(int) Math.round((initialtoIgnorePercentage)*array.size());

		//work out the mean
		for (int i=forStart; i<array.size();i++){
			meanTotal+=array.get(i).doubleValue();
			n++;
		}
		meanTotal=meanTotal/n;

		//calculate standard deviation
		for (int k=forStart;k<array.size(); k++){
			Std+=Math.pow((array.get(k).doubleValue()-meanTotal),2);
		}

		//standard deviation
		stndDev=Math.sqrt(Std/(n-1));

		return stndDev;
	}

	/**
	 * Calculate the standard deviation of an array of float values, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array of float values.
	 * @return standard deviation of the array values. 
	 */
	public static double std(ArrayList<? extends Number> array){
		return std(array, 0);
	}

	/**
	 * Find the median of an ArrayList of doubles
	 * @param array the array to calculate the median for
	 * @param ignorePercentage the percentage of initial results on the array to ignore
	 * @return the median of the results whihc are not included in the ignorePercentage
	 */
	public static  double median(ArrayList<? extends Number> array, double ignorePercentage){
		ArrayList<Double> arrayMedian=new ArrayList<Double>();
		int forStart=(int) Math.round((ignorePercentage)*array.size());
		for (int i=forStart; i<array.size();i++){
			arrayMedian.add(array.get(i).doubleValue()); 
		}
		return median(arrayMedian); 
	}


	/**
	 * Find the median of an ArrayList of doubles
	 * @param array
	 * @return median of the array. 
	 */
	public static double median(ArrayList<? extends Number> array){
		int size=array.size();

		//this round about way of sorting is to stop 'sort' from sorting the matrices in the PreMarkovChain part.
		ArrayList<Double> zSort=new ArrayList<Double>();
		for (int i=0; i<array.size(); i++){
			zSort.add(array.get(i).doubleValue());
		}
		Collections.sort(zSort);

		double median=0;
		if (size%2==0){
			double n1=zSort.get(size/2);		
			double n2=zSort.get((size/2)-1);
			median=(n1+n2)/2;
		}
		else{
			median=zSort.get((int) ((size/2)-0.5));
		}
		return median;
	}

	/**
	 * Calculate the median value of an array 
	 * @param numArray - the number array 
	 * @return the median value. 
	 */
	public static double median(double[] numArray) {
		Arrays.sort(numArray);
		double median;
		if (numArray.length % 2 == 0)
			median = ((double)numArray[numArray.length/2] + (double) numArray[numArray.length/2 - 1])/2;
		else
			median = (double) numArray[numArray.length/2];

		return median;
	}


	/**
	 * Calculate the mean of an array of float values, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array of float values.
	 * @param InitialtoIgnorePercentage.
	 * @return mean of the array values.
	 */
	public static double meanf(ArrayList<Float> array, double initialtoIgnorePercentage){
		return mean(array, initialtoIgnorePercentage); 

		//		float MeanTotal=0;
		//		int n=0;
		//		int forStart=(int) Math.round((initialtoIgnorePercentage)*array.size());
		//
		//		for (int i=forStart; i<array.size();i++){
		//			MeanTotal+= array.get(i);
		//			n++;
		//		}
		//		double mean=MeanTotal/n;
		//
		//		return mean;
	}

	/**
	 * Calculate the standard deviation of an array of float values, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array of float values.
	 * @return standard deviation of the array values. 
	 */
	public static double stdf(ArrayList<Float> array){
		return stdf(array, 0);
	}


	/**
	 * Calculate the standard deviation of an array of float values, ignoring an 'initialtoIgnorePercentage' percentage of jumps
	 * @param array of float values.
	 * @param initialtoIgnorePercentage- percentage of initial values to ignore.
	 * @return standard deviation of the array values. 
	 */
	public static double stdf(ArrayList<Float> array, double initialToIgnorePercentage){
		return std(array, initialToIgnorePercentage); 
		//		double Std=0.0;
		//		float meanTotal=(float) 0.0;
		//		int forStart=(int) Math.round((initialToIgnorePercentage)*array.size());
		//
		//		double StandardDeviation;
		//		int n=0;
		//
		//		for (int i=forStart; i<array.size();i++){
		//			meanTotal+=array.get(i);
		//			n++;
		//		}
		//		meanTotal=meanTotal/n;
		//
		//		for (int k=forStart;k<array.size(); k++){
		//			Std+=Math.pow((array.get(k)-meanTotal),2);
		//		}
		//		StandardDeviation=Math.sqrt(Std/(n-1));
		//		return StandardDeviation;
	}

	/**
	 * Calculate the mean for a double[] array
	 * @param data array of doubles
	 * @return the mean of the array
	 */
	public static double mean(double[] data){
		double sum = 0.0;
		for(double a : data)
			sum += a;
		return sum/data.length;
	}


	/**
	 * Calculate the mean for a int[] array
	 * @param data array of integers
	 * @return the mean of the array
	 */
	public static double mean(int[] data) {
		double sum = 0.0;
		for(int a : data)
			sum += a;
		return sum/data.length;
	}

	/**
	 * Calculate the variance for a double[] array
	 * @param data array of doubles
	 * @return the variance
	 */
	public static double varience(double[] data)
	{
		double mean = mean(data);
		double temp = 0;
		for(double a :data)
			temp += (mean-a)*(mean-a);
		return temp/data.length;
	}

	/**
	 * Calculate the standard deviation for a double[] array
	 * @param data array of doubles
	 * @return the standard deviation
	 */
	public static double std(double[] data)
	{
		return Math.sqrt(varience(data));
	}

	/**
	 * Get the standard deviation for a 2D double array
	 * @param data - a 2D array of doubles
	 * @return std for each COLUMN
	 */
	public static double[] std(double[][] data){
		if (data.length<1) return null;
		double[] stdResults=new double[data[0].length]; 
		RealMatrix rm = new Array2DRowRealMatrix(data);	
		for (int i=0; i<data[0].length; i++){
			stdResults[i]=std(rm.getColumn(i)); 
		}
		return stdResults; 
	}


	/**
	 * Transpose a double[][] matrix
	 * @param m - the matrix transdpose
	 * @return the transposed matrix
	 */
	public static double[][] transposeMatrix(double [][] m){
		double[][] temp = new double[m[0].length][m.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[0].length; j++)
				temp[j][i] = m[i][j];
		return temp;
	}


	/**
	 * Sorts and array and returns the index of the sorted elements of the array. 
	 * Handles duplicate values. 
	 * @param array - the array to sort
	 * @return an integer array showing the index of sorted elements from the original input array
	 */
	public static List<Integer> sort(ArrayList<? extends Number> array) {
		TreeMap<Object, List<Integer>> map = new TreeMap<Object, List<Integer>>();
		for(int i = 0; i < array.size(); i++) {
			List<Integer> ind = map.get(array.get(i));
			if(ind == null){
				ind = new ArrayList<Integer>();
				map.put(array.get(i), ind);
			}
			ind.add(i);
		}

		// Now flatten the list
		List<Integer> indices = new ArrayList<Integer>();
		for(List<Integer> arr : map.values()) {
			indices.addAll(arr);
		}

		return indices; 
	}

	/**
	 * Sort one array by the ordering of another array. This is a convenience function which sorts one array and then 
	 * uses the index of that sort to sort another array. The returned array is the sorted arrayToSort. Useful if sorting
	 * one ArrayList by another ArrayList 
	 * @param arrayOrdered - the array to sort by
	 * @param arrayToSort - the array to sort. Must be same size as arrayOrdered.
	 * @return the arrayToSort with elements sorted by the arrayOrdered. 
	 */
	public static <T> ArrayList<T> sort(ArrayList<? extends Number> arrayOrdered, ArrayList<T> arrayToSort) {
		List<Integer> indexSort = sort(arrayOrdered); 

		ArrayList<T> arraySorted = new ArrayList<T>(indexSort.size()); 
		//have to preallocate
		for (int i=0; i<indexSort.size(); i++) {
			arraySorted.add(arrayToSort.get(indexSort.get(i))); 
		}
		return arraySorted;
	}

	/**
	 * Calculate the difference between the minimum and maximum value of an array. 
	 * @param arr - the array to find the maximum value of. 
	 * @return the maximum value in the array
	 */
	public static double minmaxdiff(double[] arr) {
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;

		for(double cur: arr) {
			max = Math.max(max, cur);
			min = Math.min(min, cur);
		}

		return max-min;
	}

	/**
	 * Calculate the minimum and maximum value of an array. 
	 * @param arr - the array to find the maximum value of. 
	 * @return the minimum and maximum value in the array
	 */
	public static double[] minmax(double[] arr) {
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;

		for(double cur: arr) {
			max = Math.max(max, cur);
			min = Math.min(min, cur);
		}

		return new double[] {min, max};
	}


	/**
	 * Calculate the minimum and maximum value of a 2D array. 
	 * @param arr - the array to find the maximum value of. 
	 * @return the minimum and maximum value in the array
	 */
	public static double[] minmax(double[][] arr) {
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;

		for(int i=0; i<arr.length; i++) {
			for(int j=0; j<arr[i].length; j++) {
				max = Math.max(max, arr[i][j]);
				min = Math.min(min, arr[i][j]);
			}
		}

		return new double[] {min, max};
	}

	/**
	 * Calculate the minimum and maximum value of a 2D array. 
	 * @param arr - the array to find the maximum value of. 
	 * @return the minimum and maximum value in the array
	 */
	public static int[] minmax(int[][] arr) {
		int max = Integer.MAX_VALUE;
		int min = Integer.MIN_VALUE;

		for(int i=0; i<arr.length; i++) {
			for(int j=0; j<arr.length; j++) {
				max = Math.max(max, arr[i][j]);
				min = Math.min(min, arr[i][j]);
			}
		}

		return new int[] {min, max};
	}


	/**
	 * Calculate the maximum value in an array 
	 * @param arr - the array to find the maximum value of. 
	 * @return the maximum value in the array
	 */
	public static double max(float[] arr) {
		double max = Double.NEGATIVE_INFINITY;

		for(double cur: arr)
			max = Math.max(max, cur);

		return max;
	}
	
	/**
	 * Calculate the maximum value in an array 
	 * @param arr - the array to find the maximum value of. 
	 * @return the maximum value in the array
	 */
	public static double max(double[] arr) {
		double max = Double.NEGATIVE_INFINITY;

		for(double cur: arr)
			max = Math.max(max, cur);

		return max;
	}

	/**
	 * Calculate the maximum value in an array 
	 * @param arr - the array to find the maximum value of. 
	 * @return the maximum value in the array
	 */
	public static int max(int[] arr) {
		int max = Integer.MIN_VALUE;

		for(int cur: arr)
			max = Math.max(max, cur);

		return max;
	}
	
	/**
	 * Get the index of the maximum value in an array 
	 * @param arr  - the array to find the position of the maximum value. 
	 * m value of. 
	 * @return the index of the maximum value
	 */
	public static int maxPos(double[] arr) {
		double max = Double.NEGATIVE_INFINITY;
		int index = -1; 
		
		int count = 0; 
		for(double cur: arr) {
			if (cur>max) {
				index = count; 
				max=cur;
			}
			count++; 
		}
		

		return index;
	}
	
	/**
	 * Get the minimum index of an array
	 * @param arr  - the array to find the position of the maximum value. 
	 * m value of. 
	 * @return the index of the minimum value
	 */
	public static int minPos(double[] arr) {
		double max = Double.POSITIVE_INFINITY;
		int index = -1; 
		
		int count = 0; 
		for(double cur: arr) {
			if (cur<max) {
				index = count; 
				max=cur;
			}
			count++; 
		}
		return index;
	}

	/**
	 * Get the minimum value in an array 
	 * @param arr  - the array to find the minimu
	 * m value of. 
	 * @return the  minimum value in the array. 
	 */
	public static double min(double[] arr) {
		double min = Double.POSITIVE_INFINITY;

		for(double cur: arr)
			min = Math.min(min, cur);

		return min;
	}
	


	/**
	 * Get the minimum value in an array 
	 * @param arr  - the array to find the minimum value of. 
	 * @return the  minimum value in the array. 
	 */
	public static int min(int[] arr) {
		int min = Integer.MAX_VALUE;

		for(int cur: arr)
			min = Math.min(min, cur);

		return min;
	}


	/**
	 * Normalise an array
	 * @param arr  - the array to normalise
	 * @return normalised copy of the array
	 */
	public static double[] normalise(double[] arr) {
		return normalise(arr, 1); 
	}


	/**
	 * Normalise an array
	 * @param arr  - the array to normalise
	 * @param scaleFactor - multiply the resulting array by a scale factor. 
	 * @return normalised copy of the array
	 */
	public static double[] normalise(double[] arr, double scaleFactor) {
		//		//first find the sum of the square of the wave
		if (arr != null) { 
			int n = arr.length; 
			double sum = 0.0; 

			for (int i = 0; i < n; i++) { 
				sum += arr[i] * arr[i]; 
			} 
			sum = Math.pow(sum, 0.5); 

			double[] normArr=new double[arr.length]; 
			for (int i=0; i<normArr.length; i++) {
				normArr[i]=scaleFactor*arr[i]/sum; 
			}
			return normArr; 
		}
		else return null; 


		//inp/((sum(inp.^2))^0.5);		

		//				double max=PamArrayUtils.max(arr);
		//				double[] normArr = new double[arr.length];
		//				for (int i=0; i<normArr.length; i++) {
		//					normArr[i]=arr[i]/max;
		//				}
		//				return normArr;		

	}

	/**
	 * Flip an array so that it is in the reverse order. Note the array is 
	 * cloned. 
	 * @param flipArray - the waveform to flip
	 * @return the array with elements reversed.
	 */
	public static double[] flip(double[] flipArray) {
		double[] clone=ArrayUtils.clone(flipArray); 
		ArrayUtils.reverse(clone);
		return clone; 
	}
	
	/**
	 * Split an array based on start index and end index. A new array with index 0
	 * as start and the last index at end is created and data from the input array
	 * copied to it.
	 * 
	 * @param arr   - the array to split
	 * @param start - the start index
	 * @param end   - the end index
	 * @return the split array.
	 */
	public static double[][] split(double[][] arr, int start, int end) {
		double[][] newArr = new double[end - start][];
		int n = 0;
		for (int i = start; i < end; i++) {
			newArr[n] = arr[i];
			n++;
		}
		return newArr;
	}


	/**
	 * Sum the elements in an array 
	 * @param  array - the array to sum.
	 * @return the summation of all the elements in the array.
	 */
	public static int sum(int[] array) {
		int sum=0; 
		for (int val:array) {
			sum+=val;
		}
		return sum;
	}


	/**
	 * Sum the elements in an array 
	 * @param  array - the array to sum.
	 * @return the summation of all the elements in the array.
	 */
	public static double sum(double[] array) {
		double sum=0; 
		for (double val:array) {
			sum+=val;
		}
		return sum;
	}


	/**
	 * Sum the elements in a 2D array.
	 * @param  array2 - the array to sum.
	 * @return the summation of all the elements in the array.
	 */
	public static double sum(double[][] array2) {
		double sum=0; 
		double[] array; 
		for (int i=0; i<array2.length; i++) {
			array = array2[i]; 
			for (double val:array) {
				sum+=val;
			}
		}
		return sum;
	}

	/**
	 * Print an array to the console. 
	 * @param array to print
	 */
	public static void printArray(double[] array) {
		for (int i=0; i<array.length; i++) {
			System.out.println(i + ": " + array[i]);
		}
	}
	
	
	/**
	 * Print an array to the console with no index numbers
	 * @param array to print
	 */
	public static void printArrayRaw(double[] array) {
		for (int i=0; i<array.length; i++) {
			System.out.println(array[i]);
		}
	}

	public static void printArray(int[] array) {
		for (int i=0; i<array.length; i++) {
			System.out.println(i + ": " + array[i]);
		}
	}

	/**
	 * Print a 2D double array
	 * @param array - the array 
	 */
	public static void printArray(double[][] array) {
		for (int j=0; j<array.length; j++) {
			System.out.println("");
			for (int i=0; i<array[j].length; i++) {
				System.out.print(array[j][i] + " : ");
			}
		}
		System.out.println("");
	}



	/**
	 * Print a 2D int array
	 * @param array - the array 
	 */
	public static void printArray(int[][] array) {
		for (int j=0; j<array.length; j++) {
			System.out.println("");
			for (int i=0; i<array[j].length; i++) {
				System.out.print(array[j][i] + " : ");
			}
		}
		System.out.println("");
	}
	
	/**
	 * Print a Long array
	 * @param array - the array to print.
	 */
	public static void printArray(Long[] array) {
		for (int i=0; i<array.length; i++) {
			System.out.println(i + ": " + array[i]);
		}
	}


	/**
	 * Convert a list to a primitive double array. 
	 * @param listArray - the list. 
	 * @return the primitive double. 
	 */
	public static double[] list2ArrayD(List<Double> listArray)  {
		double[] array = new double[listArray.size()];
		for (int i=0; i<listArray.size() ;i++) {
			array[i]=listArray.get(i).doubleValue(); 
		}
		return array;
	}

	/**
	 * Check whether there are duplicates within an array
	 * @param the array.
	 * @return true if there are duplicates. 
	 */
	public static boolean unique(double[] array) {
		boolean duplicates=false;
		for (int j=0;j<array.length;j++)
			for (int k=j+1;k<array.length;k++)
				if (k!=j && array[k] == array[j])
					duplicates=true;
		return duplicates;
	}

	/**
	 * Divide each element in an array by a number
	 * @param array - the array
	 * @param divisor - the number 
	 * @return
	 */
	public static double[] divide(double[] array, double max) {
		for (int j=0;j<array.length;j++) {
			array[j]=array[j]/max; 
		}
		return array;
	}



	/**
	 * Convert an array to a  delimited string. 
	 * @param array - the input array to convert to a string
	 * @param decimalplaces - the number of decimal places to save as characters. More means a longer string. 
	 * @param delimitter - the delimiter e.g. ",". 
	 * @return the input array. 
	 */
	public static String array2String(float[] array, int decimalPlaces, String delimiter) {
		String outArray = ""; 
		for (int i=0; i<array.length; i++) {
			outArray+=String.format("%.2" + decimalPlaces + "f", array[i]) ;
			if (i<array.length-1) outArray+=","; 
		}
		return outArray;
	}

	/**
	 * Convert an array to a  delimited string. 
	 * @param array - the input array to convert to a string
	 * @param decimalplaces - the number of decimal places to save as characters. More means a longer string. 
	 * @param delimitter - the delimiter e.g. ",". 
	 * @return the input array. 
	 */
	public static String array2String(double[] array, int decimalPlaces, String delimiter) {
		String outArray = ""; 
		for (int i=0; i<array.length; i++) {
			outArray+=String.format("%.2" + decimalPlaces + "f", array[i]) ;
			if (i<array.length-1) outArray+=","; 
		}
		return outArray;
	}

	/**
	 * Convert an array to a  delimited string. 
	 * @param array - the input array to convert to a string
	 * @param decimalplaces - the number of decimal places to save as characters. More means a longer string. 
	 * @param delimitter - the delimiter e.g. ",". 
	 * @return the input array. 
	 */
	public static String array2String(Number[] array, int decimalPlaces, String delimiter) {
		String outArray = ""; 
		for (int i=0; i<array.length; i++) {
			outArray+=String.format("%.2" + decimalPlaces + "f", array[i]) ;
			if (i<array.length-1) outArray+=","; 
		}
		return outArray;
	}


	/**
	 * Convert a delimited string array to a double array. 
	 * @param array - the input string containing the number array. 
	 * @param delimitter - the delimiter e.g. ",". 
	 * @return the input array.
	 */
	public static double[] string2array(String array, String delimitter) {
		String[] strArray = array.split(delimitter); 
		double[] outArray = new double[strArray.length]; 
		for (int i=0; i<strArray.length; i++) {
			outArray[i]=Double.valueOf(strArray[i]);
		}
		return outArray;
	}

	/**
	 * Convert an array to a comma delimited string. 
	 * @param array - the input array to convert to a string
	 * @param decimalplaces - the number of decimal places to save as characters. More means a longer string. 
	 * @return the input array. 
	 */
	public static String array2String(double[] array, int decimalPlaces) {
		return array2String(array,  decimalPlaces, ","); 
	}

	/**
	 * Convert a comma delimited string array to a double array. 
	 * @param array - the input string containing the number array. 
	 * @return the input array.
	 */
	public static double[] string2array(String array) {
		return string2array(array, ",");
	}

	/**
	 * Check whether there is a single true value in a boolean array. 
	 * @param boolArray - any array of booleans. 
	 * @return true if there is at least one true in the array. 
	 */
	public static boolean isATrue(boolean[] boolArray) {
		for (int i=0; i<boolArray.length; i++) {
			if (boolArray[i]) return true; 
		}
		return false;
	}

	/**
	 * Check whether all elements in an array are false 
	 * @param boolArray - any array of booleans. 
	 * @return true if all elements are false. 
	 */
	public static boolean isAllFalse(boolean[] boolArray) {
		return !isATrue(boolArray); 
	}

	/**
	 * Check whether an array contains a number. 
	 * @param arr - the array. 
	 * @param num - the number to check. 
	 * @return true if the number is contained within the array, 
	 */
	public static boolean contains(int[] arr, int num) {
		for (int i=0; i<arr.length; i++) {
			if (arr[i]== num) return true; 
		}

		return false;
	}

	/**
	 * Convert a float array to a double array. 
	 * @param arrf - the float array
	 * @return a double array containing the same numbers as arrf. 
	 */
	public static double[] float2Double(float[] arrf) {
		double[] arr = new double[arrf.length]; 
		for (int i=0; i<arr.length; i++) {
			arr[i] = (double) arrf[i]; 
		}
		return arr;
	}

	/**
	 * Convert a 2D float array to a 2D double array. 
	 * @param arrf - the float array
	 * @return a double array containing the same numbers as arrf. 
	 */
	public static double[][] float2Double(float[][] arrf) {
		double[][] newArray = new double[arrf.length][];
		for (int i=0; i<arrf.length; i++) {
			newArray[i] = float2Double(arrf[i]); 
		}
		
		return newArray; 
	}
	
	/**
	 * Convert a float array to a double array. 
	 * @param arrd - the double array
	 * @return a double array containing the same numbers as arrf. 
	 */
	public static float[] double2Float(double[] arrd) {
		float[] arr = new float[arrd.length]; 
		for (int i=0; i<arr.length; i++) {
			arr[i] = (float) arrd[i]; 
		}
		return arr;
	}
	
	/**
	 * Convert a 2D float array to a 2D double array. 
	 * @param arrd - the double array
	 * @return a double array containing the same numbers as arrf. 
	 */
	public static float[][] double2Float(double[][] arrd) {
		float[][] newArray = new float[arrd.length][];
		for (int i=0; i<arrd.length; i++) {
			newArray[i] = double2Float(arrd[i]); 
		}
		
		return newArray; 
	}


	
	/**
	 * Check if two int arrays contain the same elements
	 * @param arr1 - the array to compare to. 
	 * @param arr2 - the array to be compared. 
	 */
	public static boolean arrEquals(int[] arr1, int[] arr2) {
		if (arr1.length!=arr2.length) return false; 
		
		for (int i =0 ;i<arr1.length; i++) {
			if (arr1[i]!=arr2[i]) return false; 
		}
		
		return true; 
	}
	
	/**
	 * Check if two double arrays contain the same elements
	 * @param arr1 - the array to compare to. 
	 * @param arr2 - the array to be compared. 
	 */
	public static boolean arrEquals(double[] arr1, double[] arr2) {
		if (arr1.length!=arr2.length) return false; 
		
		for (int i =0 ;i<arr1.length; i++) {
			if (arr1[i]!=arr2[i]) return false; 
		}
		
		return true; 
	}

	/**
	 * Convert primitive long array to Long object array. 
	 * @param arr - primitive long array
	 * @return a Long array 
	 */
	public static Long[] primitive2Object(long[] arr) {
		if (arr==null) return null; 
		
		Long[] arrL = new Long[arr.length];
		for (int i=0; i<arr.length; i++) {
			arrL[i]=arr[i];
		}
		return arrL;
	}
	
	

	/**
	 * Convert primitive double array to Double object array. 
	 * @param arr - primitive double array
	 * @return a Double array 
	 */
	public static Double[] primitive2Object(double[] arr) {
		if (arr==null) return null; 
		
		Double[] arrL = new Double[arr.length];
		for (int i=0; i<arr.length; i++) {
			arrL[i]=arr[i];
		}
		return arrL;
	}
	

	/**
	 * Convert primitive int array to Integer object array. 
	 * @param arr - primitive int array
	 * @return a Ineteger array 
	 */
	public static Integer[] primitive2Object(int[] arr) {
		if (arr==null) return null; 
		
		Integer[] arrL = new Integer[arr.length];
		for (int i=0; i<arr.length; i++) {
			arrL[i]=arr[i];
		}
		return arrL;
	}


	public static long[] object2Primitve(Long[] arr) {
		if (arr==null) return null; 
		
		long[] arrL = new long[arr.length];
		for (int i=0; i<arr.length; i++) {
			arrL[i]=arr[i].longValue();
		}
		return arrL;
	}





	
	
	









}

