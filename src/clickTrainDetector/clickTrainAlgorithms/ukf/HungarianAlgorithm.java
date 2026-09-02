package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.util.Arrays;

/**
 * Kuhn-Munkres (Hungarian) algorithm for the rectangular linear assignment
 * problem. Finds the minimum-cost assignment of rows (e.g. tracks) to columns
 * (e.g. detections).
 * <p>
 * Implemented with the O(n^3) potential method. Rectangular cost matrices are
 * padded to a square with zero-cost dummy cells; padded assignments are returned
 * as "unassigned" (-1). The caller is responsible for rejecting matches whose
 * cost exceeds a gate (forbidden pairs should be given a large cost so the
 * algorithm avoids them in favour of a dummy).
 *
 * @author Jamie Macaulay
 */
public class HungarianAlgorithm {

	private static final double INF = Double.MAX_VALUE / 4;

	/**
	 * Solve the minimum-cost assignment problem.
	 *
	 * @param costMatrix - cost[row][col]; need not be square.
	 * @return an array {@code rowToCol} of length rows, where {@code rowToCol[r]}
	 *         is the column assigned to row r, or -1 if the row is unassigned.
	 */
	public static int[] solve(double[][] costMatrix) {
		int n = costMatrix.length;
		if (n == 0) {
			return new int[0];
		}
		int m = costMatrix[0].length;
		if (m == 0) {
			int[] none = new int[n];
			Arrays.fill(none, -1);
			return none;
		}

		int dim = Math.max(n, m);
		double[][] cost = new double[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				cost[i][j] = (i < n && j < m) ? costMatrix[i][j] : 0.0;
			}
		}

		double[] u = new double[dim + 1];
		double[] v = new double[dim + 1];
		int[] p = new int[dim + 1]; // p[j] = row (1-indexed) matched to column j
		int[] way = new int[dim + 1];

		for (int i = 1; i <= dim; i++) {
			p[0] = i;
			int j0 = 0;
			double[] minv = new double[dim + 1];
			boolean[] used = new boolean[dim + 1];
			Arrays.fill(minv, INF);
			do {
				used[j0] = true;
				int i0 = p[j0];
				int j1 = -1;
				double delta = INF;
				for (int j = 1; j <= dim; j++) {
					if (!used[j]) {
						double cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
						if (cur < minv[j]) {
							minv[j] = cur;
							way[j] = j0;
						}
						if (minv[j] < delta) {
							delta = minv[j];
							j1 = j;
						}
					}
				}
				for (int j = 0; j <= dim; j++) {
					if (used[j]) {
						u[p[j]] += delta;
						v[j] -= delta;
					} else {
						minv[j] -= delta;
					}
				}
				j0 = j1;
			} while (p[j0] != 0);
			do {
				int j1 = way[j0];
				p[j0] = p[j1];
				j0 = j1;
			} while (j0 != 0);
		}

		int[] rowToCol = new int[n];
		Arrays.fill(rowToCol, -1);
		for (int j = 1; j <= dim; j++) {
			int row = p[j];
			if (row >= 1 && row <= n && j <= m) {
				rowToCol[row - 1] = j - 1;
			}
		}
		return rowToCol;
	}

}
