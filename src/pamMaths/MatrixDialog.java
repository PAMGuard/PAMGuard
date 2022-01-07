package pamMaths;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import Jama.Matrix;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog for showing the content of a matrix. 
 * 
 * @author Doug Gillespie
 *
 */
public class MatrixDialog extends PamDialog {
	
	private JPanel matrixPanel;
	
	private JLabel titleLabel;
	
	private String[] rowHeadings;
	
	private String[] colHeadings;
	
	private Matrix matrix;

	private MatrixDialog(Window parentFrame, String title) {
		super(parentFrame, title, false);

		matrixPanel = new JPanel();
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.NORTH, titleLabel = new JLabel(title));
		p.add(BorderLayout.CENTER, matrixPanel);
		
		setDialogComponent(p);
	}
	
	public static Matrix showDialog(Frame frame, String title, Matrix matrix, 
			String[] rowHeadings, String[] columnHeadings) {
		
		MatrixDialog md = new MatrixDialog(frame, title);

		md.setHeadings(rowHeadings, columnHeadings);
		md.setMatrix(matrix);
		md.setVisible(true);
		return null;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}
	private void setHeadings(String[] rowHeadings, String[] colHeadings) {
		this.rowHeadings = rowHeadings;
		this.colHeadings = colHeadings;
	}
	private void setMatrix(Matrix matrix) {
		this.matrix = matrix;
		layoutMatrix();
	}
	
	private void layoutMatrix() {
		
		matrixPanel.removeAll();
		int nRow = matrix.getRowDimension();
		int nCol = matrix.getColumnDimension();
		int firstCol = 0, firstRow = 0;
		matrixPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		if (rowHeadings != null) {
			firstCol = 1;
		}
		if (colHeadings != null) {
			firstRow = 1;
		}
		if (rowHeadings != null) {
			for (int i = 0; i < rowHeadings.length; i++) {
				c.gridx = 0;
				c.gridy = i + firstRow;
				addComponent(matrixPanel, new MatrixLabel(rowHeadings[i]).getComponent(), c);
				c.gridy++;
			}
		}
		if (colHeadings != null) {
			for (int i = 0; i < colHeadings.length; i++) {
				c.gridy = 0;
				c.gridx = i + firstCol;
				addComponent(matrixPanel, new MatrixLabel(colHeadings[i]).getComponent(), c);
				c.gridx++;
			}
		}
		for (int iR = 0; iR < nRow; iR++) {
			for (int iC = 0; iC < nCol; iC++) {
				c.gridx = iC + firstCol;
				c.gridy = iR + firstRow;
				addComponent(matrixPanel, new MatrixComponent(iR, iC).getComponent(), c);
			}
		}
		pack();
	}
	
	class MatrixLabel {
		Component c;
		
		String text;

		public MatrixLabel(String text) {
			super();
			this.text = text;
			JLabel l = new JLabel(text);
			l.setHorizontalAlignment(SwingConstants.CENTER);
			c = l;
		}
		
		Component getComponent() {
			return c;
		}
	}

	class MatrixComponent {
		
		int iR, iC;
		
		JTextField label;

		public MatrixComponent(int ir, int ic) {
			super();
			iR = ir;
			iC = ic;
			double val;
			label = new JTextField(5);
			label.setText(String.format("%3.1f", val = matrix.get(iR, iC)));
			label.setEnabled(false);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			if (ir == ic) {
				label.setBackground(Color.WHITE);
			}
			else if (val > 20) {
				label.setBackground(Color.ORANGE);
			}
			else if (val > 10) {
				label.setBackground(Color.CYAN);
			}
		}
		
		public Component getComponent() {
			return label;
		}
		
		
	}
	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
