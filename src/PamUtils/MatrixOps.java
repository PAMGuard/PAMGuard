package PamUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import PamView.ColourArray;
import fftManager.Complex;

/**
 * Matrix operations for arrays of Complex data
 * Really need to write these for ComplexArray rather than Complex[] since
 * it's a lot faster. 
 * Widely used in the DIFAR module. 
 * @author dg50
 *
 */
public class MatrixOps {

	/**Check the array is rectangular, => all sub arrays are same length.
	 * 
	 * @param a A 2D Complex Array
	 * @return false if null,<p>
	 * true if 0-length or is rectangular
	 */
	public static boolean checkRectangular(Complex[][] a) {

		if (a==null) return false;
		if (a.length==0){
			//			System.out.println("Rec but null or 0 length");
			return true;
		}
		int J=a[0].length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				//				System.out.println("A row x is not same length as length of A[0]:"+x);
				return false;
			}
		}
		return true;
	}

	public static Complex[][] complexMatrixCross(Complex[][] a,Complex[][] b){
		int I=a.length;
		int J=b.length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				System.out.println("A row x is not same length as length of B:"+x);
				return null;
			}
		}

		int K=b[0].length;
		for(int y=0;y<a.length;y++){
			if (b[y].length!=K){
				System.out.println("A row y is not same length as length of y[0]:"+y);
				return null;
			}
		}

		Complex[][] c=Complex.allocateComplexArray(I, K);
		//				new Complex[I][K];

		for (int i=0;i<I;i++){
			for (int k=0;k<K;k++){
				//				c[i][k]=new Complex(0, 0);
				for (int j=0;j<J;j++){
					if (c[i][k]==null||a[i][j]==null||b[j][k]==null){
						System.out.println("i"+i+"j"+j+"k"+k);
					}

					c[i][k]=c[i][k].plus(a[i][j].times(b[j][k]));
				}
			}
		}

		return c;
	}

	public static Complex[][] transposeMatrix(Complex[][] a){
		int I=a.length;
		int J=a[0].length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				System.out.println("A row x is not same length as length of A[0]:"+x);
				return null;
			}
		}

		Complex[][] b = new Complex[J][I];

		for (int i=0;i<I;i++){
			for (int j=0;j<J;j++){
				b[j][i]=a[i][j].clone();
			}
		}
		return b;
	}

	public static Complex[][] scalarMultMatrix(Complex[][] a,Complex scalar){
		int I=a.length;
		int J=a[0].length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				System.out.println("A row x is not same length as length of A[0]:"+x);
				return null;
			}
		}

		Complex[][] b = new Complex[I][J];

		for (int i=0;i<I;i++){
			for (int j=0;j<J;j++){
				b[i][j]=a[i][j].clone().times(scalar);
			}
		}
		return b;
	}

	/**debug tool
	 * <br>
	 * print a complex matrix (with no null entries) to std out
	 * @param a
	 */
	public static void printMatrix(Complex[][] a){
		int I=a.length;
		int J=a[0].length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				System.out.println("A row x is not same length as length of A[0]:"+x);
			}
		}


		for (int i=0;i<I;i++){
			for (int j=0;j<a[i].length;j++){
				System.out.print("\t"+a[i][j] );
			}
			System.out.println();
		}
	}

	/**
	 * 
	 * @param a Complex 2D array
	 * @return the real parts in a double 2d array
	 */
	public static double[][] getRealMatrix(Complex[][] a){
		int I=a.length;
		if (a == null || a.length == 0) {
			return null;
		}
		int J=a[0].length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				System.out.println("A row x is not same length as length of A[0]:"+x);
				return null;
			}
		}

		double[][] b = new double[I][J];

		for (int i=0;i<I;i++){
			for (int j=0;j<J;j++){
				b[i][j]=a[i][j].real;
			}
		}
		return b;
	}

	/**
	 * 
	 * @param 3x3 matrix
	 * @return determinant
	 */
	public static Complex det33Matrix(Complex[][] A){

		Complex a = A[0][0];
		Complex b = A[0][1];
		Complex c = A[0][2];
		Complex d = A[1][0];
		Complex e = A[1][1];
		Complex f = A[1][2];
		Complex g = A[2][0];
		Complex h = A[2][1];
		Complex i = A[2][2];

		Complex aei=a.times(e.times(i));
		Complex bfg=b.times(f.times(g));
		Complex cdh=c.times(d.times(h));

		Complex ceg=c.times(e.times(g));
		Complex bdi=b.times(d.times(i));
		Complex afh=a.times(f.times(h));

		return aei.plus(bfg).plus(cdh).minus(ceg).minus(bdi).minus(afh);
	}

	public static Complex detNnMatrix(Complex[][] A){
		if (!(checkRectangular(A)&&A.length==A[0].length)){
			System.out.println("not N x N matrix");
			return null;
		}
		int n=A.length;
		Complex det = new Complex(0,0);
		switch (n) {
		case 0:
			return null;
		case 1:
			return A[0][0];
		case 2:
			Complex a = A[0][0];
			Complex b = A[0][1];
			Complex c = A[1][0];
			Complex d = A[1][1];
			return a.times(d).minus(b.times(c));
		default:
			for (int i=0;i<n;i++){
				det=det.plus(A[0][i].times(detNnMatrix(MatrixOps.getRolledSubMatrix(A,i))));
			}
			return det;
		}
	}

	/**
	 * 
	 * @param A a complex matrix
	 * @param i
	 * @return a sq matrix with len/height 1 less than A
	 * for i=0 the bottom right corner
	 * for i =A.lenght bottom left 
	 * for other i wraps around the  
	 */
	private static Complex[][]  getRolledSubMatrix(Complex[][] A,int i){

		Complex[][] B =Complex.allocateComplexArray(A.length-1, A.length-1);

		for (int j=0;j<B.length;j++){
			for (int k=0;k<B[0].length;k++){
				B[j][k]=A[j+1][(k+1+i)%A.length];
			}
		}
		return B;
	}

	public static Complex[][] inverse33Matrix(Complex[][] A){

		Complex scale = det33Matrix(A);
		scale= MatrixOps.recipComp(scale);
		return scalarMultMatrix(transposeMatrix(A), scale);

	}

	public static Complex[][] inverseNnMatrix(Complex[][] A){

		Complex scale = detNnMatrix(A);
		if (scale==null){
			System.out.println("not square");
			return null;
		}
		scale= MatrixOps.recipComp(scale);
		return scalarMultMatrix(transposeMatrix(A), scale);

	}

	public static Complex recipComp(Complex z){
		double x = z.real;
		double y = z.imag;

		return new Complex(x/(x*x+y*y),-y/(x*x+y*y));


	}

	/**
	 * Make a new matrix with abs values of the original. 
	 * @param matrix
	 * @return
	 */
	public static double[][] getAbsMatrix(double[][] matrix) {
		if (matrix == null) {
			return null;
		}
		if (matrix.length == 0) {
			return matrix.clone();
		}
		double[][] newMatrix = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				newMatrix[i][j] = Math.abs(matrix[i][j]);
			}
		}
		return newMatrix;
	}

	public static BufferedImage createImage(double[][]surfaceData, ColourArray colorArray, boolean logScale, double intensityScaleFactor) {
		if (surfaceData == null) {
			return null;
		}
		BufferedImage surfaceImage = new BufferedImage(surfaceData.length, surfaceData[0].length, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = surfaceImage.getRaster();
		double val;
		int nColours = colorArray.getNumbColours();
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		//double intensityScaleFactor = 2;
		double intensityFloor;
		/*
		 * i = loop over angles, j over frequency bins
		 */
		for (int i = 0; i < surfaceData.length; i++) {
			for (int j = 0; j < surfaceData[i].length; j++) {
				val =  surfaceData[i][j];
				max = Math.max(max, val);
				min = Math.min(min, val);
			}
			//			System.out.println();
		}
		if (min>max)return null;
		
		intensityFloor = max/intensityScaleFactor; 

		int colInd;
		if (logScale) {
			/*
			 * Scale the image so that values less that 1/intensityScaleFactor 
			 * of the maximum are all the lowest color on the colormap
			 */
			min = Math.log10(Math.max(min, intensityFloor));
			max = Math.log10(max);
			//				min = max-2;
			for (int i = 0; i < surfaceData.length; i++) {
				for (int j = 0; j < surfaceData[i].length; j++) {
					val = Math.log10(surfaceData[i][j]);
					colInd = (int) ((val-min) / (max-min) * (nColours-1));
					//				System.out.print(val + ", ");
					colInd = Math.max(0, Math.min(nColours-1, colInd));
					raster.setPixel(i, surfaceData[i].length-1-j, colorArray.getIntColourArray(colInd));
				}
				//			System.out.println();
			}
		}
		else{
			for (int i = 0; i < surfaceData.length; i++) {
				for (int j = 0; j < surfaceData[i].length; j++) {
					val = surfaceData[i][j];
					val = (int) ((val-min) / (max-min) * (nColours-1));
					//				System.out.print(val + ", ");
					val = Math.max(0, Math.min(nColours-1, val));
					raster.setPixel(i, surfaceData[i].length-1-j, colorArray.getIntColourArray(new Double(val).intValue()));
				}
				//			System.out.println();
			}
		}

		return surfaceImage;
	}

	/*
	public static void main(String[] args){
		Complex[][] Sxm = new Complex[3][3];

	       	Sxm[0][0]= new Complex(1,0);
	       	Sxm[0][1]= new Complex(0,0);
	       	Sxm[0][2]= new Complex(6,0);

	       	Sxm[1][0]= new Complex(5,0);
	       	Sxm[1][1]= new Complex(1,0);
	       	Sxm[1][2]= new Complex(0,0);

	       	Sxm[2][0]= new Complex(0,0);
	       	Sxm[2][1]= new Complex(3,0);
	       	Sxm[2][2]= new Complex(1,0);


	       	Complex[][] Sxm2 = new Complex[3][3];

	       	Sxm2[0][0]= new Complex(1,0);
	       	Sxm2[0][1]= new Complex(0,0);
	       	Sxm2[0][2]= new Complex(0,0);

	       	Sxm2[1][0]= new Complex(0,0);
	       	Sxm2[1][1]= new Complex(1,0);
	       	Sxm2[1][2]= new Complex(0,0);

	       	Sxm2[2][0]= new Complex(0,0);
	       	Sxm2[2][1]= new Complex(0,0);
	       	Sxm2[2][2]= new Complex(1,0);

	       	double a=Double.NEGATIVE_INFINITY%360;
	       	System.out.println(a);
	       	System.out.println(det33Matrix(Sxm));
	       	System.out.println(detNnMatrix(Sxm));
	       	System.out.println(det33Matrix(Sxm2));
	       	System.out.println(detNnMatrix(Sxm2));
	       	printMatrix(Sxm);
	       	System.out.println();

	       	for (int i=0;i<Sxm.length;i++){
				printMatrix((getRolledSubMatrix(Sxm,i)));
				System.out.println();
			}

	}
	 */

	public static boolean checkRectangular(double[][] a) {
		if (a.length==0){
			System.out.println("Rec but null");
			return true;
		}

		int J=a[0].length;
		for(int x=0;x<a.length;x++){
			if (a[x].length!=J){
				System.out.println("A row x is not same length as length of A[0]:"+x);
				return false;
			}
		}
		return true;
	}

	public static boolean applyMask(Complex [] ary, double[] windowFunction){
		if (ary.length!=windowFunction.length){
			System.out.println("apply window function failed!");
			return false;
		}
		for (int i=0;i<ary.length;i++){
			ary[i]=ary[i].times(windowFunction[i]);
		}
		return true;
	}

	public static boolean applyMask(double [] ary, double[] windowFunction){
		if (ary.length!=windowFunction.length){
			System.out.println("apply window function failed!");
			return false;
		}
		for (int i=0;i<ary.length;i++){
			ary[i]=ary[i]*windowFunction[i];
		}
		return true;
	}
	

}
