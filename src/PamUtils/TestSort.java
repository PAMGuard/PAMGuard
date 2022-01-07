package PamUtils;

public class TestSort {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int[] data = { 9, 3, 5, 1, 6, 7, -2, 8, 2, 1, 7, -9};
		int[] sortInds = PamUtils.getSortedInds(data);
		for (int i = 0; i < data.length; i++) {
			System.out.println(data[i] + "    Index "+ sortInds[i] + " sorted = " + data[sortInds[i]]);
		}

	}

}
