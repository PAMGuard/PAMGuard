package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Murty's algorithm for the k lowest-cost assignments of a square cost matrix.
 * <p>
 * Given a square cost matrix it returns up to {@code k} distinct complete
 * assignments (one column per row) in non-decreasing total-cost order, using the
 * {@link HungarianAlgorithm} as the single-best-assignment subroutine. Each
 * returned assignment is a {@code rowToCol} array (as
 * {@link HungarianAlgorithm#solve(double[][])}).
 * <p>
 * This is used by the N-scan multi-hypothesis tracker to enumerate the best few
 * global track/detection assignments per frame, rather than committing to only
 * the single best (which is what a greedy nearest-neighbour tracker does).
 *
 * @author Jamie Macaulay
 */
public class MurtyKBest {

	/**
	 * A cost at or above which an edge is treated as forbidden - both the caller's
	 * gated-out pairs and the edges Murty removes when partitioning use this. Kept
	 * well below {@link HungarianAlgorithm}'s internal infinity so the solver still
	 * treats the row as assignable elsewhere.
	 */
	public static final double FORBIDDEN = 1e7;

	private MurtyKBest() {
	}

	/**
	 * The k lowest-cost complete assignments of a square cost matrix.
	 *
	 * @param cost - a square cost matrix; may contain {@link #FORBIDDEN} for
	 *               disallowed cells.
	 * @param k    - the maximum number of assignments to return.
	 * @return up to k assignments in non-decreasing cost order; empty if none is
	 *         feasible.
	 */
	public static List<int[]> kBest(double[][] cost, int k) {
		List<int[]> result = new ArrayList<>();
		int n = cost.length;
		if (n == 0 || k <= 0) {
			return result;
		}

		PriorityQueue<Node> queue = new PriorityQueue<>();
		Node root = solve(cost, new int[0][0], new boolean[n][n]);
		if (root != null) {
			queue.add(root);
		}

		while (!queue.isEmpty() && result.size() < k) {
			Node best = queue.poll();
			result.add(best.assignment);

			// Partition the remaining solution space around best.assignment. Edges that
			// are already forced in this node stay fixed in every child; the remaining
			// "free" edges (on rows that are not forced) are peeled off one at a time in
			// row order: child i forbids the i-th free edge and forces the free edges
			// already peeled (0..i-1), on top of this node's own forced/forbidden set.
			boolean[] forcedRow = new boolean[n];
			List<int[]> forced = new ArrayList<>();
			for (int[] e : best.forced) {
				forced.add(e);
				forcedRow[e[0]] = true;
			}
			for (int r = 0; r < n; r++) {
				if (forcedRow[r]) {
					continue; // only free edges are peeled
				}
				int c = best.assignment[r];

				boolean[][] forbidden = deepCopy(best.forbidden);
				forbidden[r][c] = true; // forbid this free edge in the child

				Node child = solve(cost, forced.toArray(new int[0][]), forbidden);
				if (child != null) {
					queue.add(child);
				}

				// fix this edge for all subsequent children (and hence for their subtrees).
				forced.add(new int[] { r, c });
			}
		}
		return result;
	}

	/**
	 * Solve a constrained assignment subproblem: forbidden cells are made
	 * unassignable, and each forced (row,col) edge is imposed by making that row and
	 * column pick only that cell.
	 *
	 * @return the resulting node, or null if the subproblem has no feasible finite
	 *         assignment.
	 */
	private static Node solve(double[][] cost, int[][] forced, boolean[][] forbidden) {
		int n = cost.length;
		double[][] work = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				work[i][j] = forbidden[i][j] ? FORBIDDEN : cost[i][j];
			}
		}
		// impose forced edges: row r and column c may only use cell (r,c).
		for (int[] e : forced) {
			int r = e[0];
			int c = e[1];
			for (int j = 0; j < n; j++) {
				work[r][j] = (j == c) ? cost[r][c] : FORBIDDEN;
			}
			for (int i = 0; i < n; i++) {
				work[i][c] = (i == r) ? cost[r][c] : FORBIDDEN;
			}
		}

		int[] assignment = HungarianAlgorithm.solve(work);
		if (assignment.length != n) {
			return null;
		}
		double total = 0;
		for (int r = 0; r < n; r++) {
			int c = assignment[r];
			// Feasibility must be judged against the *constrained* matrix: if the solver
			// was forced (no alternative) into a forbidden or forced-out cell, work[r][c]
			// is FORBIDDEN even though the original cost is finite. Gate on work, sum the
			// real cost.
			if (c < 0 || work[r][c] >= FORBIDDEN) {
				return null; // infeasible under these constraints.
			}
			total += cost[r][c];
		}
		return new Node(assignment, total, forced, forbidden);
	}

	private static boolean[][] deepCopy(boolean[][] a) {
		boolean[][] b = new boolean[a.length][];
		for (int i = 0; i < a.length; i++) {
			b[i] = Arrays.copyOf(a[i], a[i].length);
		}
		return b;
	}

	/** A node in Murty's search: one solved (constrained) assignment. */
	private static final class Node implements Comparable<Node> {
		private final int[] assignment;
		private final double cost;
		private final int[][] forced;
		private final boolean[][] forbidden;

		Node(int[] assignment, double cost, int[][] forced, boolean[][] forbidden) {
			this.assignment = assignment;
			this.cost = cost;
			this.forced = forced;
			this.forbidden = forbidden;
		}

		@Override
		public int compareTo(Node o) {
			return Double.compare(cost, o.cost);
		}
	}

}
