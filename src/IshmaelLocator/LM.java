package IshmaelLocator;


/** LM: The Levenberg-Marquardt algorithm for least-squares minimization
*<p>
*   The purpose of LM is to minimize the sum of the squares of m nonlinear
*   functions (dependent variables) in n independent variables by a
*   modification of the Levenberg-Marquardt algorithm. The user must
*   provide a function which calculates the functions and, optionally,
*   a function (jacFcn) to calculate the Jacobian. If useJac is false,
*   the Jacobian is calculated by a forward-difference approximation; this
*   is significantly slower, so define jacFcn if possible.
*<p>
*   To use LM, make a subclass of it that defines fcn and, if
*   desired, jacFcn. You can also declare other class variables to be 
*   used in calculating fcn and jacFcn.  Create an instance of it 
*   (initializing your class vars as needed), then call calc().  
*   Alternatively, you can call calc_simple(), which makes reasonable
*   guesses for many of the inputs to calc.  The first argument 
*   to these, useJac, indicates whether you've defined a jacFcn.  
*   After it returns, examine the output variables LM.info, LM.x, and 
*   any other output variables you like.
*<p>
*   The calling statement is
*<p>
*	    calc(useJac,
*          m,n,x,fvec,ftol,xtol,gtol,maxfev,epsfcn,diag,mode,factor,
*          nprint)
*<p>  
*   or
*<p>
*       calc_simple(useJac, m, n, tol, x[]) 
*<p>
*   where
*<p>
*	fcn (in subclass) is the name of the user-supplied function which
*     calculates the functions.  fcn should be written as follows:
*<p>
*   	    boolean fcn(double x[], double y[], boolean printFlag)
*     	    {
*               //Calculate the functions at x and place this result
*               //vector in y.  Return true to continue, false to stop.
*           }
*<p>
*         fcn should calculate y = fcn(x), where x is a vector of length
*         n and y is a vector of length m.  printFlag indicates whether
*         this call is being made for debug printing purposes only.
*         fcn should return true if processing should continue, false
*         to stop the Levenberg-Marquardt iteration.
*<p>
*   jacFcn
*         If calc() is called with useJac true, then jacFcn gets called
*         to calculate the Jacobian.  Its arguments are the same as fcn,
*         except that output vector y is replaced with output matrix jac,
*         a matrix of size m x n.  Your Jacobian function should place
*         the result in jac, and return true to continue the processing
*         or false to stop it.
*<p>
*         If useJac is false, then the Jacobian is estimated by the
*         (slower) forward-difference method.
*<p>
*	m is a positive integer input variable set to the number
*	  of functions.
*<p>
*	n is a positive integer input variable set to the number
*	  of variables. n must not exceed m.
*<p>
*	x (input and output) is an array of length n. On input x must
*         contain an initial estimate of the solution vector. On output
*         x contains the final estimate of the solution vector.
*<p>
*	fvec is an output array of length m which contains
*	  the functions evaluated at the output x.
*<p>
*	ftol is a nonnegative input variable. Termination
*	  occurs when both the actual and predicted relative
*	  reductions in the sum of squares are at most ftol.
*	  Therefore, ftol measures the relative error desired
*	  in the sum of squares.
*<p>
*	xtol is a nonnegative input variable. Termination
*	  occurs when the relative error between two consecutive
*	  iterates is at most xtol. Therefore, xtol measures the
*	  relative error desired in the approximate solution.
*<p>
*	gtol is a nonnegative input variable. Termination
*	  occurs when the cosine of the angle between fvec and
*	  any column of the Jacobian is at most gtol in absolute
*	  value. Therefore, gtol measures the orthogonality
*	  desired between the function vector and the columns
*	  of the Jacobian.
*<p>
*	maxfev is a positive integer input variable. Termination
*	  occurs when the number of calls to fcn is at least
*	  maxfev by the end of an iteration.
*<p>
*	epsfcn is an input variable used in determining a
*	  suitable step length for the forward-difference
*         approximation. If haveJac is true, epsfcn is ignored. This
*	  approximation assumes that the relative errors in the
*	  functions are of the order of epsfcn. If epsfcn is less
*	  than the machine precision, it is assumed that the relative
*	  errors in the functions are of the order of the machine
*	  precision.
*<p>
*	diag is an input array of length n. If mode = 1 (see
*	  below), diag is internally set. If mode = 2, diag
*	  must contain positive entries that serve as
*	  multiplicative scale factors for the variables.
*<p>
*	mode is an integer input variable. If mode = 1, the
*	  variables will be scaled internally. If mode = 2,
*	  the scaling is specified by the input diag. Other
*	  values of mode are equivalent to mode = 1.
*<p>
*	factor is a positive input variable used in determining the
*	  initial step bound. This bound is set to the product of
*	  factor and the Euclidean norm of diag*x if nonzero, or else
*	  to factor itself. in most cases factor should lie in the
*	  interval (.1,100.). 100. is a generally recommended value.
*<p>
*	nprint is an integer input variable that enables controlled
*	  printing of iterates if it is positive. In this case,
*	  fcn is called with printFlag = true at the beginning of the first
*	  iteration and every nprint iterations thereafter and
*	  immediately prior to return, with x and fvec available
*	  for printing. If nprint is not positive, no special calls
*	  of fcn with printFlag = true are made.
*<p>
*<p>
*  Output variables:
*	info is an integer output variable specifying why calc()
*         returned:
*<p>
*     info = -1 User function terminated execution by returning false.
*           See description of fcn above.
*<p>
*	  info = 0  Improper input parameters.
*<p>
*	  info = 1  Both actual and predicted relative reductions
*		    in the sum of squares are at most ftol.
*<p>
*	  info = 2  Relative error between two consecutive iterates
*		    is at most xtol.
*<p>
*	  info = 3  Conditions for info = 1 and info = 2 both hold.
*<p>
*	  info = 4  The cosine of the angle between fvec and any
*		    column of the Jacobian is at most gtol in
*		    absolute value.
*<p>
*	  info = 5  Number of calls to fcn has reached or
*		    exceeded maxfev.
*<p>
*	  info = 6  ftol is too small. No further reduction in
*		    the sum of squares is possible.
*<p>
*	  info = 7  xtol is too small. No further improvement in
*		    the approximate solution x is possible.
*<p>
*	  info = 8  gtol is too small. fvec is orthogonal to the
*		    columns of the Jacobian to machine precision.
*<p>
*     info = 9  Out of memory. Doesn't happen in the Java
*           version, since an exceptino gets thrown.
*<p>
*	nfev is an integer output variable set to the number of
*	  non-printing function calls, i.e., calls to fcn with
*         printFlag=false.
*<p>
*	njev is an integer output variable set to the number of
*	  Jacobians calculated, i.e., calls to jacFcn.
*<p>
*	fjac is an output m by n array.
*         The upper n by n submatrix of fjac contains an upper
*         triangular matrix r with diagonal elements of nonincreasing
*         magnitude such that
*<p>
*		 T     T	   T
*		p *(jac *jac)*p = r *r,
*<p>
*	  where p is a permutation matrix and jac is the final
*	  calculated Jacobian. Column j of p is column ipvt(j)
*	  (see below) of the identity matrix. The lower trapezoidal
*	  part of fjac contains information generated during
*	  the computation of r.
*<p>
*	ipvt is an integer output array of length n. ipvt
*	  defines a permutation matrix p such that jac*p = q*r,
*	  where jac is the final calculated Jacobian, q is
*	  orthogonal (not stored), and r is upper triangular
*	  with diagonal elements of nonincreasing magnitude.
*	  Column j of p is column ipvt(j) of the identity matrix.
*<p>
*	qtf is an output array of length n which contains
*	  the first n elements of the vector (q transpose)*fvec.
*<p>
*	wa1, wa2, and wa3 are work arrays of length n.
*<p>
*	wa4 is a work array of length m.
*<p>
*     Functions called:
*<p>
*<p>user-supplied ............. fcn, optionally jacFcn
*<p>minpack-supplied (below)... enorm,fdjac2,lmpar,qrfac
*<p>
*     Argonne National Laboratory. Minpack Project. March 1980.
*     Burton S. Garbow, Kenneth E. Hillstrom, Jorge J. More
*/

public abstract class LM {
	abstract boolean fcn(double x[], double y[], boolean printFlag);
	boolean jacFcn(double x[], double jac[][], boolean printFlag) {
		//Don't call this!  Write your own jacFcn, or set useJac=false.
		return false;       
	}
		
	//Return values from calc(...) [x is also an input value]:
	int info;			//reason for terminating the iteration
	double x[];			//the main result; on input, the initial guess
	double fvec[];		//fcn(x)
	int nfev, njev;		//number of fcn and jacFcn evaluations
	int ipvt[];			//pivot values
	double fjac[][];	//see above
	double qtf[];		//q' * fvec   (where q' is q transpose)  		

	//Other local variables:
	int iflag;          //used in several methods
	static final double DWARF = 1e-38;
	static final double MACHEP = Double.MIN_VALUE;

	/** Run the Levenberg-Marquardt method using reasonable guesses
	 * for many of the input parameters.
	 * 
	 * <p>see See the comment for the class LM for inputs and outputs.
	 */
	public void calc_simple(boolean useJac, int m, int n, double tol, 
			double x[]) 
	{
		double ftol    = tol;
		double xtol    = tol * tol;
		double gtol    = 0.0;      
		int    maxfev  = 200 * (n + 1);
		double epsfcn  = 0.0;
		int    mode    = 1;			//let calc calculate diag[]
		double factor  = 100.0;
		int    nprint  = 0;
		double diag[]  = new double[n];

		calc(useJac, m, n, x,
				ftol, xtol, gtol, maxfev, epsfcn, 
				diag, mode, factor, nprint);
		if (info == 8)
			info = 4;
	}
	
	/** See the long comment for the LM class for details.
	 */
	public void calc(boolean useJac, int m, int n, double x[], 
			double ftol, double xtol, double gtol, int maxfev, double epsfcn, 
			double diag[], int mode, double factor, int nprint)
	{
	  //Set up output variables.
	  info = 0;
	  this.x = x;				//initial guess; gets modified
	  fvec = new double[m];
	  nfev = njev = 0;
	  ipvt = new int[n];
	  fjac = new double[m][n];
	  qtf = new double[n];

	  //Create work variables.
	  double[] wa1 = new double[n];
	  double[] wa2 = new double[n];
	  double[] wa3 = new double[n];
	  double[] wa4 = new double[m];		//note m not n
	  
	  int i,iter,j,l;           //had ij, jj
	  double actred,delta = 0.0,dirder,fnorm,fnorm1,gnorm;
	  double par,pnorm,prered,ratio;
	  double sum,temp,temp1,temp2,temp3,xnorm = 0.0;

	  iflag = 1;     /* default value; gets set to -1 upon user fcn termination */

	  //The original Fortran (and C) code had 'goto L300' statements.
	  //Here we replace them with 'throw new L300()' in a try/catch block.
	  class L300 extends Exception { static final long serialVersionUID = 0; };
	  
	  try {
	  /* Check the input parameters for errors.
	   */
	  info = 0;
	  if (n <= 0 || m < n || ftol < 0.0 || xtol < 0.0 || gtol < 0.0 || maxfev <= 0
	      || factor <= 0.0)
	    throw new L300();

	  if (mode == 2)
	    for (j=0; j<n; j++)
	      if (diag[j] <= 0.0)
	        throw new L300();

	  /* Evaluate the function at the starting point and calculate its norm.
	   */
	  nfev = 1;
	  if (!fcn(x,fvec,false)) {
	    iflag = -1;
	    throw new L300();
	  }
	  fnorm = enorm(m,fvec);

	  /* Initialize Levenberg-Marquardt parameter and iteration counter.
	   */
	  par = 0.0;
	  iter = 1;

	  /************************************* Beginning of the outer loop. */
	  while (true) {                 //used to be labeled 'L30:'

	  /* Calculate the Jacobian matrix.
	   */
	  if (useJac) {
	    njev += 1;
	    if (!jacFcn(x,fjac,false)) {
	      iflag = -1;
	      throw new L300();
	    }
	  } else {
	    fdjac2(m,n,x,fvec,epsfcn,wa4);      //modifies fjac,iflag 
	    nfev += n;
	    if (iflag < 0)
	      throw new L300();
	  }

	  //If requested, call fcn to enable printing of iterates.
	  if (nprint > 0) {
	    iflag = 0;
	    if (((iter - 1) % nprint) == 0) {
	      if (!fcn(x,fvec,true)) {
	        iflag = -1;
	        throw new L300();
	      }
	      xnorm = enorm(m,fvec);
	      //printf("Iteration %3i:    chi-square = %.15g\n", iter, xnorm * xnorm);
	    }
	  }

	  /* Compute the QR factorization of the Jacobian.
	   */
	  qrfac(m,n,fjac,1,1,ipvt,n,wa1,wa2,wa3);

	  /* On the first iteration and if mode is 1, scale according to the norms of
	   * the columns of the initial Jacobian.
	   */
	  if (iter == 1) {
	    if (mode != 2) {
	      for (j=0; j<n; j++) {
	        diag[j] = wa2[j];
	        if (wa2[j] == 0.0)
	          diag[j] = 1.0;
	      }
	    }

	    /* On the first iteration, calculate the norm of the scaled x and
	     * initialize the step bound delta.
	     */
	    for (j=0; j<n; j++)
	      wa3[j] = diag[j] * x[j];

	    xnorm = enorm(n,wa3);
	    delta = factor*xnorm;
	    if (delta == 0.0)
	      delta = factor;
	  }

	  /* Form (q transpose)*fvec and store the first n components in qtf.
	   */
	  for (i=0; i<m; i++)
	    wa4[i] = fvec[i];
	  for (j=0; j<n; j++) {
	    temp3 = fjac[j][j];
	    if (temp3 != 0.0) {
	      sum = 0.0;
	      for (i=j; i<m; i++) {
	        sum += fjac[i][j] * wa4[i];
	      }
	      temp = -sum / temp3;
	      for (i=j; i<m; i++) {
	        wa4[i] += fjac[i][j] * temp;
	      }
	    }
	    fjac[j][j] = wa1[j];
	    qtf[j] = wa4[j];
	  }

	  /* Compute the norm of the scaled gradient.
	   */
	  gnorm = 0.0;
	  if (fnorm != 0.0) {
	    for (j=0; j<n; j++) {
	      l = ipvt[j];
	      if (wa2[l] != 0.0) {
	        sum = 0.0;
	        for (i=0; i<=j; i++) {
	          sum += fjac[i][j]*(qtf[i]/fnorm);
	        }
	        gnorm = Math.max(gnorm,Math.abs(sum/wa2[l]));
	      }
	    }
	  }

	  /* Test for convergence of the gradient norm.
	   */
	  if (gnorm <= gtol)
	    info = 4;
	  if (info != 0)
	    throw new L300();

	  /* Rescale if necessary.
	   */
	  if (mode != 2) {
	    for (j=0; j<n; j++)
	      diag[j] = Math.max(diag[j],wa2[j]);
	  }

	  /******************************************** Beginning of the inner loop. */

	L200: while (true) {					//this used to be labeled 'L200:'

		/* Determine the Levenberg-Marquardt parameter.
		 */
		par = lmpar(n,fjac,m,ipvt,diag,qtf,delta,wa1,wa2,wa3,wa4);

		/* Store the direction p and x + p.  Calculate the norm of p.
		 */
		for (j=0; j<n; j++) {
			wa1[j] = -wa1[j];
			wa2[j] = x[j] + wa1[j];
			wa3[j] = diag[j]*wa1[j];
		}
		pnorm = enorm(n,wa3);

		/* On the first iteration, adjust the initial step bound.
		 */
		if (iter == 1)
			delta = Math.min(delta,pnorm);

		/* Evaluate the function at x + p and calculate its norm.
		 */
		iflag = 1;     
		nfev += 1;
		if (!fcn(wa2,wa4,false)) {
			iflag = -1;
			throw new L300();
		}
		fnorm1 = enorm(m,wa4);

		/* Compute the scaled actual reduction.
		 */
		actred = -1.0;
		if ((0.1*fnorm1) < fnorm) {
			temp = fnorm1/fnorm;
			actred = 1.0 - temp * temp;
		}

		/* Compute the scaled predicted reduction and the scaled directional
		 * derivative.
		 */
		for (j=0; j<n; j++) {
			wa3[j] = 0.0;
			l = ipvt[j];
			temp = wa1[l];
			for (i=0; i<=j; i++) {
				wa3[i] += fjac[i][j]*temp;
			}
		}
		temp1 = enorm(n,wa3)/fnorm;
		temp2 = (Math.sqrt(par)*pnorm)/fnorm;
		prered = temp1*temp1 + (temp2*temp2)/0.5;
		dirder = -(temp1*temp1 + temp2*temp2);

		/* Compute the ratio of the actual to the predicted reduction.
		 */
		ratio = 0.0;
		if (prered != 0.0)
			ratio = actred/prered;

		/* Update the step bound.
		 */
		if (ratio <= 0.25) {
			if (actred >= 0.0)
				temp = 0.5;
			else
				temp = 0.5*dirder/(dirder + 0.5*actred);
			if (((0.1*fnorm1) >= fnorm) || (temp < 0.1))
				temp = 0.1;
			delta = temp*Math.min(delta,pnorm/0.1);
			par = par/temp;
		} else {
			if ((par == 0.0) || (ratio >= 0.75)) {
				delta = pnorm/0.5;
				par = 0.5*par;
			}
		}

		/* Test for successful iteration.
		 */
		if (ratio >= 1.0e-4) {
			/* Successful iteration. Update x, fvec, and their norms.
			 */
			for (j=0; j<n; j++) {
				x[j] = wa2[j];
				wa2[j] = diag[j]*x[j];
			}
			for (i=0; i<m; i++)
				fvec[i] = wa4[i];
			xnorm = enorm(n,wa2);
			fnorm = fnorm1;
			iter += 1;
		}

		/* Tests for convergence.
		 */
		if ((Math.abs(actred) <= ftol) && (prered <= ftol) && (0.5*ratio <= 1.0))
			info = 1;
		if (delta <= xtol*xnorm)
			info = 2;
		if (Math.abs(actred) <= ftol && prered <= ftol && 0.5*ratio <= 1.0 && info == 2)
			info = 3;
		if (info != 0)
			throw new L300();

		/* Tests for termination and stringent tolerances.
		 */
		if (nfev >= maxfev)
			info = 5;
		if ((Math.abs(actred) <= MACHEP) && (prered <= MACHEP) && (0.5*ratio <= 1.0))
			info = 6;
		if (delta <= MACHEP*xnorm)
			info = 7;
		if (gnorm <= MACHEP)
			info = 8;
		if (info != 0)
			throw new L300();

		/******************* End of the inner loop. Repeat if iteration unsuccessful.
		 */
		if (ratio < 1.0e-4)
			continue L200;
	}
	  /***************************************************** End of the outer loop.
	   */
	  }

	  } catch (L300 ex) {};      //this used to be labeled 'L300:'

	  /* Termination, either normal or user imposed.
	   */
	  if (iflag < 0)
	    info = iflag;
	  iflag = 0;
	  if (nprint > 0)
	    fcn(x,fvec,true);

	}          /* end of lm */


	/* ---------------------------------lmpar --------------------------------
	*
	*     Given an m by n matrix a, an n by n nonsingular diagonal
	*     matrix d, an m-vector b, and a positive number delta,
	*     the problem is to determine a value for the parameter
	*     par such that if x solves the system
	*
	*	    a*x = b ,	  sqrt(par)*d*x = 0 ,
	*
	*     in the least squares sense, and dxnorm is the Euclidean
	*     norm of d*x, then either par is zero and
	*
	*	    (dxnorm-delta) <= 0.1*delta ,
	*
	*     or par is positive and
	*
	*	    abs(dxnorm-delta) <= 0.1*delta .
	*
	*     This subroutine completes the solution of the problem
	*     if it is provided with the necessary information from the
	*     QR factorization, with column pivoting, of a. That is, if
	*     a*p = q*r, where p is a permutation matrix, q has orthogonal
	*     columns, and r is an upper triangular matrix with diagonal
	*     elements of nonincreasing magnitude, then lmpar expects
	*     the full upper triangle of r, the permutation matrix p,
	*     and the first n components of (q transpose)*b.  On output
	*     lmpar also provides an upper triangular matrix s such that
	*
	*	     t	 t		     t
	*	    p *(a *a + par*d*d)*p = s *s .
	*
	*     s is employed within lmpar and may be of separate interest.
	*
	*     Only a few iterations are generally needed for convergence
	*     of the algorithm. If, however, the limit of 10 iterations
	*     is reached, then the output par will contain the best
	*     value obtained so far.
	*
	*     The calling statement is
	*
	*	lmpar(n,r,ldr,ipvt,diag,qtb,delta,par,x,sdiag,wa1,wa2)
	*
	*     where
	*
	*	n is a positive integer input variable set to the order of r.
	*
	*	r is an n by n array. On input the full upper triangle
	*	  must contain the full upper triangle of the matrix r.
	*	  On output the full upper triangle is unaltered, and the
	*	  strict lower triangle contains the strict upper triangle
	*	  (transposed) of the upper triangular matrix s.
	*
	*	ldr is a positive integer input variable not less than n
	*	  which specifies the leading dimension of the array r.
	*
	*	ipvt is an integer input array of length n which defines the
	*	  permutation matrix p such that a*p = q*r. Column j of p
	*	  is column ipvt(j) of the identity matrix.
	*
	*	diag is an input array of length n which must contain the
	*	  diagonal elements of the matrix d.
	*
	*	qtb is an input array of length n which must contain the first
	*	  n elements of the vector (q transpose)*b.
	*
	*	delta is a positive input variable which specifies an upper
	*	  bound on the Euclidean norm of d*x.
	*
	*	par is a nonnegative variable. On input par contains an
	*	  initial estimate of the Levenberg-Marquardt parameter.
	*	  on output par contains the final estimate.
	*
	*	x is an output array of length n which contains the least
	*	  squares solution of the system a*x = b, sqrt(par)*d*x = 0,
	*	  for the output par.
	*
	*	sdiag is an output array of length n which contains the
	*	  diagonal elements of the upper triangular matrix s.
	*
	*	wa1 and wa2 are work arrays of length n.
	*
	*     Functions called:
	*
	*	minpack-supplied (below)... enorm,qrsolv
	*
	*     Argonne National Laboratory. Minpack Project. March 1980.
	*     Burton S. Garbow, Kenneth E. Hillstrom, Jorge J. More
	*/      
	double lmpar(int n, double r[][], int ldr, int ipvt[], double diag[],
	           double qtb[], double delta, double x[], double sdiag[],
	           double wa1[], double wa2[])
	{
	  int i,iter,j,jm1,jp1,k,l,nsing;             //had ij, jj
	  double dxnorm,fp,gnorm,parc,parl,paru;
	  double sum,temp;
	  double par = 0.0;

	  /* Compute and store in x the gauss-newton direction. If the Jacobian is
	   * rank-deficient, obtain a least squares solution.
	  */
	  nsing = n;
	  //jj = 0;
	  for (j=0; j<n; j++) {
	    wa1[j] = qtb[j];
	    if ((r[j][j] == 0.0) && (nsing == n))
	      nsing = j;
	    if (nsing < n)
	      wa1[j] = 0.0;
	    //jj += ldr+1;
	  }
	  if (nsing >= 1) {
	    for (k=0; k<nsing; k++) {
	      j = nsing - k - 1;
	      wa1[j] = wa1[j]/r[j][j];
	      temp = wa1[j];
	      jm1 = j - 1;
	      if (jm1 >= 0) {
	        //ij = ldr * j;
	        for (i=0; i<=jm1; i++) {
	          //double arij = ABS(r[ij]);
	          //int logrij = log10(arij);
	          //int logtemp = log10(Math.abs(temp));
	          //wa1[i] -= (logrij + logtemp >= -307)
	          //              ? r[ij]*temp : 0;
	          wa1[i] -= r[i][j]*temp;
	          //ij += 1;
	        }
	      }
	    }
	  }

	  for (j=0; j<n; j++) {
	    l = ipvt[j];
	    x[l] = wa1[j];
	  }

	  /* Initialize the iteration counter.  Evaluate the function at the origin,
	   * and test for acceptance of the Gauss-Newton direction.
	   */
	  iter = 0;
	  for (j=0; j<n; j++)
	    wa2[j] = diag[j]*x[j];
	  dxnorm = enorm(n,wa2);
	  fp = dxnorm - delta;
	  if (fp > 0.1*delta) {		//used to be 'if (fp <= 0.1*delta) goto L220'

		  /* If the Jacobian is not rank deficient, the Newton step provides a lower
		   * bound, parl, for the zero of the function. Otherwise set this bound to 0.
		   */
		  parl = 0.0;
		  if (nsing >= n) {
			  for (j=0; j<n; j++) {
				  l = ipvt[j];
				  wa1[j] = diag[l]*(wa2[l]/dxnorm);
			  }
			  //jj = 0;
			  for (j=0; j<n; j++) {
				  sum = 0.0;
				  jm1 = j - 1;
				  if (jm1 >= 0) {
					  //ij = jj;
					  for (i=0; i<=jm1; i++) {
						  sum += r[i][j]*wa1[i];
						  //ij += 1;
					  }
				  }
				  wa1[j] = (wa1[j] - sum)/r[j][j];
				  //jj += ldr;
			  }
			  temp = enorm(n,wa1);
			  parl = ((fp/delta)/temp)/temp;
		  }

		  /* Calculate an upper bound, paru, for the zero of the function.
		   */
		  //jj = 0;
		  for (j=0; j<n; j++) {
			  sum = 0.0;
			  //ij = jj;
			  for (i=0; i<=j; i++) {
				  sum += r[i][j]*qtb[i];
				  //ij += 1;
			  }
			  l = ipvt[j];
			  wa1[j] = sum/diag[l];
			  //jj += ldr;
		  }
		  gnorm = enorm(n,wa1);
		  paru = gnorm/delta;
		  if (paru == 0.0)
			  paru = DWARF / Math.min(delta,0.1);

		  /* If the input par lies outside of the interval (parl,paru), set par to
		   * the closer endpoint.
		   */
		  par = Math.max( par,parl);
		  par = Math.min( par,paru);
		  if (par == 0.0)
			  par = gnorm/dxnorm;

		  /* Beginning of an iteration.
		   */
		  while (true) {          //used to be labeled 'L150:'
			  iter += 1;

			  /* Evaluate the function at the current value of par.
			   */
			  if (par == 0.0)
				  par = Math.max(DWARF, 0.001*paru);
			  temp = Math.sqrt(par);
			  for (j=0; j<n; j++)
				  wa1[j] = temp*diag[j];
			  qrsolv(n,r,ldr,ipvt,wa1,qtb,x,sdiag,wa2);
			  for (j=0; j<n; j++)
				  wa2[j] = diag[j]*x[j];
			  dxnorm = enorm(n,wa2);
			  temp = fp;
			  fp = dxnorm - delta;

			  /* If the function is small enough, accept the current value of par. Also
			   * test for the exceptional cases where parl is zero or the number of
			   * iterations has reached 10.
			   */
			  if (   Math.abs(fp) <= 0.1*delta
					  || ((parl == 0.0) && (fp <= temp) && (temp < 0.0))
					  || iter == 10)
			  {
				  break;           //used to be 'goto L220'
			  }

			  /* Compute the Newton correction.
			   */
			  for (j=0; j<n; j++) {
				  l = ipvt[j];
				  wa1[j] = diag[l]*(wa2[l]/dxnorm);
			  }
			  //jj = 0;
			  for (j=0; j<n; j++) {
				  wa1[j] = wa1[j]/sdiag[j];
				  temp = wa1[j];
				  jp1 = j + 1;
				  if (jp1 < n) {
					  //ij = jp1 + jj;
					  for (i=jp1; i<n; i++) {
						  wa1[i] -= r[i][j]*temp;
						  //ij += 1;
					  }
				  }
				  //jj += ldr;
			  }
			  temp = enorm(n,wa1);
			  parc = ((fp/delta)/temp)/temp;

			  /* Depending on the sign of the function, update parl or paru.
			   */
			  if (fp > 0.0)
				  parl = Math.max(parl, par);
			  if (fp < 0.0)
				  paru = Math.min(paru, par);

			  /* Compute an improved estimate for par.
			   */
			  par = Math.max(parl, par + parc);

			  /* End of an iteration.
			   */
		  }
	  }

	  //used to be labeled 'L220:'
	  
	  /* Termination.
	   */
	  if (iter == 0)
	    par = 0.0;

	  return par;
	}          /* end of lmpar */


	/*------------------------------- qrfac ------------------------------------
	*
	*     This subroutine uses Householder transformations with column
	*     pivoting (optional) to compute a QR factorization of the
	*     m by n matrix a. That is, qrfac determines an orthogonal
	*     matrix q, a permutation matrix p, and an upper trapezoidal
	*     matrix r with diagonal elements of nonincreasing magnitude,
	*     such that a*p = q*r. The Householder transformation for
	*     column k, k = 1,2,...,min(m,n), is of the form
	*
	*			    t
	*	    i - (1/u(k))*u*u
	*
	*     where u has zeros in the first k-1 positions. The form of
	*     this transformation and the method of pivoting first
	*     appeared in the corresponding linpack subroutine.
	*
	*     The calling statement is
	*
	*	  qrfac(m,n,a,lda,pivot,ipvt,lipvt,rdiag,acnorm,wa)
	*
	*     where
	*
	*	m is a positive integer input variable set to the number
	*	  of rows of a.
	*
	*	n is a positive integer input variable set to the number
	*	  of columns of a.
	*
	*	a is an m by n array. On input a contains the matrix for
	*	  which the QR factorization is to be computed. On output
	*	  the strict upper trapezoidal part of a contains the strict
	*	  upper trapezoidal part of r, and the lower trapezoidal
	*	  part of a contains a factored form of q (the non-trivial
	*	  elements of the u vectors described above).
	*
	*	lda is a positive integer input variable not less than m
	*	  which specifies the leading dimension of the array a.
	*
	*	pivot is a logical input variable. If pivot is set true,
	*	  then column pivoting is enforced. If pivot is set false,
	*	  then no column pivoting is done.
	*
	*	ipvt is an integer output array of length lipvt. ipvt
	*	  defines the permutation matrix p such that a*p = q*r.
	*	  Column j of p is column ipvt(j) of the identity matrix.
	*	  If pivot is false, ipvt is not referenced.
	*
	*	lipvt is a positive integer input variable. If pivot is false,
	*	  then lipvt may be as small as 1. If pivot is true, then
	*	  lipvt must be at least n.
	*
	*	rdiag is an output array of length n which contains the
	*	  diagonal elements of r.
	*
	*	acnorm is an output array of length n which contains the
	*	  norms of the corresponding columns of the input matrix a.
	*	  If this information is not needed, then acnorm can coincide
	*	  with rdiag.
	*
	*	wa is a work array of length n. If pivot is false, then wa
	*	  can coincide with rdiag.
	*
	*     Functions called:
	*
	*	minpack-supplied (below) ... enorm
	*
	*     Argonne National Laboratory. Minpack Project. March 1980.
	*     Burton S. Garbow, Kenneth E. Hillstrom, Jorge J. More
	*/  
	void qrfac(int m, int n, double a[][], int lda, int pivot, int ipvt[],
	           int lipvt, double rdiag[], double acnorm[], double wa[])
	{
	  int i,j,jp1,k,kmax,minmn;           //had ij, jj
	  double ajnorm,sum,temp;

	  /* Compute the initial column norms and initialize several arrays.
	   */
	  //ij = 0;
	  for (j=0; j<n; j++) {
	    acnorm[j] = col_enorm(m, a, 0, j);
	    rdiag[j] = acnorm[j];
	    wa[j] = rdiag[j];
	    if (pivot != 0)
	      ipvt[j] = j;
	    //ij += m;
	  }

	  /* Reduce a to r with Householder transformations.
	   */
	  minmn = Math.min(m,n);
	  for (j=0; j<minmn; j++) {
	    if (pivot != 0) {            //used to be 'if (pivot == 0) goto L40

	    	/* Bring the column of largest norm into the pivot position.
	    	 */
	    	kmax = j;
	    	for (k=j; k<n; k++) {
	    		if (rdiag[k] > rdiag[kmax])
	    			kmax = k;
	    	}
	    	if (kmax != j) {              //used to be 'if (kmax == j) goto L40'

	    		//ij = m * j;
	    		//jj = m * kmax;
	    		for (i=0; i<m; i++) {
	    			temp = a[i][j];
	    			a[i][j] = a[i][kmax];
	    			a[i][kmax] = temp;
	    			//ij += 1;
	    			//jj += 1;
	    		}
	    		rdiag[kmax] = rdiag[j];
	    		wa[kmax] = wa[j];
	    		k = ipvt[j];
	    		ipvt[j] = ipvt[kmax];
	    		ipvt[kmax] = k;

	    	}
	    }
	    //used to be labeled 'L40:'

	    /* Compute the Householder transformation to reduce the j-th column of
	     * a to a multiple of the j-th unit vector.
	     */
		//jj = j + m*j;
	    ajnorm = col_enorm(m-j, a, j, j);
	    if (ajnorm != 0.0) {         //used to be 'goto L100' (if ajnorm == 0.0)
	    	if (a[j][j] < 0.0)
	    		ajnorm = -ajnorm;
	    	//ij = jj;
	    	for (i=j; i<m; i++) {
	    		a[i][j] /= ajnorm;
	    		//ij += 1;
	    	}
	    	a[j][j] += 1.0;

	    	/* Apply the transformation to the remaining columns and update the norms.
	    	 */
	    	jp1 = j + 1;
	    	if (jp1 < n) {
	    		for (k=jp1; k<n; k++) {
	    			sum = 0.0;
	    			//ij = j + m*k;
	    			//jj = j + m*j;
	    			for (i=j; i<m; i++) {
	    				sum += a[i][j]*a[i][k];
	    				//ij += 1;
	    				//jj += 1;
	    			}
	    			temp = sum/a[j][j];
	    			//ij = j + m*k;
	    			//jj = j + m*j;
	    			for (i=j; i<m; i++) {
	    				a[i][k] -= temp*a[i][j];
	    				//ij += 1;
	    				//jj += 1;
	    			}
	    			if ((pivot != 0) && (rdiag[k] != 0.0)) {
	    				temp = a[j][k]/rdiag[k];          //error: was a[k][j]
	    				temp = Math.max(0.0, 1.0-temp*temp);
	    				rdiag[k] *= Math.sqrt(temp);
	    				temp = rdiag[k]/wa[k];
	    				if ((0.05*temp*temp) <= MACHEP) {
	    					rdiag[k] = col_enorm(m-j-1, a, jp1, k);
	    					wa[k] = rdiag[k];
	    				}
	    			}
	    		}
	    	}
	    }
	    rdiag[j] = -ajnorm;
	  }
	}          /* end of qrfac */


	/*------------------------------ qrsolv -----------------------------------
	*
	*     Given an m by n matrix a, an n by n diagonal matrix d,
	*     and an m-vector b, the problem is to determine an x which
	*     solves the system
	*
	*	    a*x = b ,	  d*x = 0 ,
	*
	*     in the least squares sense.
	*
	*     This subroutine completes the solution of the problem
	*     if it is provided with the necessary information from the
	*     QR factorization, with column pivoting, of a. That is, if
	*     a*p = q*r, where p is a permutation matrix, q has orthogonal
	*     columns, and r is an upper triangular matrix with diagonal
	*     elements of nonincreasing magnitude, then qrsolv expects
	*     the full upper triangle of r, the permutation matrix p,
	*     and the first n components of (q transpose)*b. The system
	*     a*x = b, d*x = 0, is then equivalent to
	*
	*		   t	   t
	*	    r*z = q *b ,  p *d*p*z = 0 ,
	*
	*     where x = p*z. If this system does not have full rank,
	*     then a least squares solution is obtained. On output qrsolv
	*     also provides an upper triangular matrix s such that
	*
	*	     t	 t		 t
	*	    p *(a *a + d*d)*p = s *s .
	*
	*     s is computed within qrsolv and may be of separate interest.
	*
	*     The calling statement is
	*
	*	qrsolv(n,r,ldr,ipvt,diag,qtb,x,sdiag,wa)
	*
	*     where
	*
	*	n is a positive integer input variable set to the order of r.
	*
	*	r is an n by n array. On input the full upper triangle
	*	  must contain the full upper triangle of the matrix r.
	*	  On output the full upper triangle is unaltered, and the
	*	  strict lower triangle contains the strict upper triangle
	*	  (transposed) of the upper triangular matrix s.
	*
	*	ldr is a positive integer input variable not less than n
	*	  which specifies the leading dimension of the array r.
	*
	*	ipvt is an integer input array of length n which defines the
	*	  permutation matrix p such that a*p = q*r. Column j of p
	*	  is column ipvt(j) of the identity matrix.
	*
	*	diag is an input array of length n which must contain the
	*	  diagonal elements of the matrix d.
	*
	*	qtb is an input array of length n which must contain the first
	*	  n elements of the vector (q transpose)*b.
	*
	*	x is an output array of length n which contains the least
	*	  squares solution of the system a*x = b, d*x = 0.
	*
	*	sdiag is an output array of length n which contains the
	*	  diagonal elements of the upper triangular matrix s.
	*
	*	wa is a work array of length n.
	*
	*     Argonne National Laboratory. Minpack Project. March 1980.
	*     Burton S. Garbow, Kenneth E. Hillstrom, Jorge J. More
	*/     
	void qrsolv(int n, double r[][], int ldr, int ipvt[], double diag[],
	            double qtb[], double x[], double sdiag[], double wa[])
	{
	  int i,j,jp1,k,kp1,l,nsing;      //had ij, ik, kk
	  double cos,cotan,qtbpj,sin,sum,tan,temp;

	  /* Copy r and (q transpose)*b to preserve input and initialize s.
	   * In particular, save the diagonal elements of r in x.
	   */
	  //kk = 0;
	  for (j=0; j<n; j++) {
	    //ij = kk;
	    //ik = kk;
	    for (i=j; i<n; i++) {
	      r[i][j] = r[j][i];
	      //ij += 1;
	      //ik += ldr;
	    }
	    x[j] = r[j][j];
	    wa[j] = qtb[j];
	    //kk += ldr+1;
	  }

	  /* Eliminate the diagonal matrix d using a Givens rotation.
	   */
	  for (j=0; j<n; j++) {
	    /* Prepare the row of d to be eliminated, locating the diagonal element
	     * using p from the QR factorization.
	     */
	    l = ipvt[j];
	    if (diag[l] != 0.0) {
	    	//used to be 'goto L90' here (if diag[l] == 0.0)
	    	for (k=j; k<n; k++)
	    		sdiag[k] = 0.0;
	    	sdiag[j] = diag[l];

	    	/* The transformations to eliminate the row of d modify only a single
	    	 * element of (q transpose)*b beyond the first n, which is initially zero.
	    	 */
	    	qtbpj = 0.0;
	    	for (k=j; k<n; k++) {
	    		/* Determine a Givens rotation which eliminates the appropriate element
	    		 * in the current row of d.
	    		 */
	    		if (sdiag[k] == 0.0)
	    			continue;
	    		//kk = k + ldr * k;
	    		if (Math.abs(r[k][k]) < Math.abs(sdiag[k])) {
	    			cotan = r[k][k]/sdiag[k];
	    			sin = 0.5/Math.sqrt(0.25+0.25*cotan*cotan);
	    			cos = sin*cotan;
	    		} else {
	    			tan = sdiag[k]/r[k][k];
	    			cos = 0.5/Math.sqrt(0.25+0.25*tan*tan);
	    			sin = cos*tan;
	    		}

	    		/* Compute the modified diagonal element of r and the modified element
	    		 * of ((q transpose)*b,0).
	    		 */
	    		r[k][k] = cos*r[k][k] + sin*sdiag[k];
	    		temp = cos*wa[k] + sin*qtbpj;
	    		qtbpj = -sin*wa[k] + cos*qtbpj;
	    		wa[k] = temp;

	    		/* Accumulate the tranformation in the row of s.
	    		 */
	    		kp1 = k + 1;
	    		if (n > kp1) {
	    			//ik = kk + 1;
	    			for (i=kp1; i<n; i++) {
	    				temp = cos*r[i][k] + sin*sdiag[i];
	    				sdiag[i] = -sin*r[i][k] + cos*sdiag[i];
	    				r[i][k] = temp;
	    				//ik += 1;
	    			}
	    		}
	    	}       /* end of k loop */
	    }                       //used to be labeled 'L90:'
	  
	    /* Store the diagonal element of s and restore the corresponding diagonal
	     * element of r.
	     */
	    //kk = j + ldr*j;
	    sdiag[j] = r[j][j];
	    r[j][j] = x[j];
	  }         /* end of j loop */

	  /* Solve the triangular system for z. If the system is singular, then obtain
	   * a least squares solution.
	   */
	  nsing = n;
	  for (j=0; j<n; j++) {
	    if ((sdiag[j] == 0.0) && (nsing == n))
	      nsing = j;
	    if (nsing < n)
	      wa[j] = 0.0;
	  }
	  if (nsing >= 1) {
		  for (k=0; k<nsing; k++) {
			  j = nsing - k - 1;
			  sum = 0.0;
			  jp1 = j + 1;
			  if (nsing > jp1) {
				  //ij = jp1 + ldr * j;
				  for (i=jp1; i<nsing; i++) {
					  sum += r[i][j]*wa[i];
					  //ij += 1;
				  }
			  }
			  wa[j] = (wa[j] - sum)/sdiag[j];
		  }
	  }                 //used to be labeled 'L150:'
	

	  /* Permute the components of z back to components of x.
	   */
	  for (j=0; j<n; j++) {
	    l = ipvt[j];
	    x[l] = wa[j];
	  }
	}          /* end of qrsolv */


	/*-------------------------------- enorm -----------------------------------
	 *
	 *     Given an n-vector x, this function calculates the
	 *     Euclidean norm of x.
	 *
	 *     The Euclidean norm is computed by accumulating the sum of
	 *     squares in three different sums. The sums of squares for the
	 *     small and large components are scaled so that no overflows
	 *     occur. Non-destructive underflows are permitted. Underflows
	 *     and overflows do not occur in the computation of the unscaled
	 *     sum of squares for the intermediate components.
	 *     The definitions of small, intermediate and large components
	 *     depend on two constants, rdwarf and rgiant. The main
	 *     restrictions on these constants are that rdwarf**2 not
	 *     underflow and rgiant**2 not overflow. The constants
	 *     given here are suitable for every known computer.
	 *
	 *     The function statement is
	 *
	 *	double enorm(n,x)
	 *
	 *     where
	 *
	 *	n is a positive integer input variable.
	 *
	 *	x is an input array of length n.
	 *
	 *     Argonne National Laboratory. Minpack Project. March 1980.
	 *     Burton S. Garbow, Kenneth E. Hillstrom, Jorge J. More
	 */    
	static final double rdwarf = 3.834e-20;
	static final double rgiant = 1.304e19;
	double enorm(int n, double x[])
	{
		int i;
		double xabs;
		double ans;
		double temp;

		double s1 = 0.0;
		double s2 = 0.0;
		double s3 = 0.0;
		double x1max = 0.0;
		double x3max = 0.0;
		double floatn = n;
		double agiant = rgiant/floatn;

		for (i=0; i<n; i++) {
			xabs = Math.abs(x[i]);
			if ((xabs > rdwarf) && (xabs < agiant)) {
				//Sum for intermediate components.
				s2 += xabs*xabs;
			} else if (xabs > rdwarf) {
				//Sum for large components.
				if (xabs > x1max) {
					temp = x1max/xabs;
					s1 = 1.0 + s1*temp*temp;
					x1max = xabs;
				} else {
					temp = xabs/x1max;
					s1 += temp*temp;
				}
			} else {
				//Sum for small components.
				if (xabs > x3max) {
					temp = x3max/xabs;
					s3 = 1.0 + s3*temp*temp;
					x3max = xabs;
				} else {
					if (xabs != 0.0) {
						temp = xabs/x3max;
						s3 += temp*temp;
					}
				}
			}
		}

		//Calculation of norm.
		if (s1 != 0.0) {
			temp = s1 + (s2/x1max)/x1max;
			ans = x1max * Math.sqrt(temp);
			return ans;
		}
		if (s2 != 0.0) {
			if (s2 >= x3max)
				temp = s2*(1.0+(x3max/s2)*(x3max*s3));
			else
				temp = x3max*((s2/x3max)+(x3max*s3));
			ans = Math.sqrt(temp);
		} else {
			ans = x3max*Math.sqrt(s3);
		}
		return ans;
	}          //end of enorm

	/*-------------------------------- col_enorm -----------------------------------
	 *
	 *     This is like enorm above, but calculates the norm of the n elements
	 *     starting at x[i0][j] and going down the column from there
	 */    
	double col_enorm(int n, double x[][], int i0, int j)
	{
	  int i;
	  double xabs;
	  double ans;
	  double temp;

	  double s1 = 0.0;
	  double s2 = 0.0;
	  double s3 = 0.0;
	  double x1max = 0.0;
	  double x3max = 0.0;
	  double floatn = n;
	  double agiant = rgiant/floatn;

	  for (i=i0; i<i0+n; i++) {
	    xabs = Math.abs(x[i][j]);
	    if ((xabs > rdwarf) && (xabs < agiant)) {
	      //Sum for intermediate components.
	      s2 += xabs*xabs;
	     } else if (xabs > rdwarf) {
	      //Sum for large components.
	      if (xabs > x1max) {
	        temp = x1max/xabs;
	        s1 = 1.0 + s1*temp*temp;
	        x1max = xabs;
	      } else {
	        temp = xabs/x1max;
	        s1 += temp*temp;
	      }
	    } else {
	    	//Sum for small components.
	    	if (xabs > x3max) {
	    		temp = x3max/xabs;
	    		s3 = 1.0 + s3*temp*temp;
	    		x3max = xabs;
	    	} else {
	    		if (xabs != 0.0) {
	    			temp = xabs/x3max;
	    			s3 += temp*temp;
	    		}
	    	}
	    }
	  }

	  //Calculation of norm.
	  if (s1 != 0.0) {
	    temp = s1 + (s2/x1max)/x1max;
	    ans = x1max * Math.sqrt(temp);
	    return ans;
	  }
	  if (s2 != 0.0) {
	    if (s2 >= x3max)
	      temp = s2*(1.0+(x3max/s2)*(x3max*s3));
	    else
	      temp = x3max*((s2/x3max)+(x3max*s3));
	    ans = Math.sqrt(temp);
	  } else {
	    ans = x3max*Math.sqrt(s3);
	  }
	  return ans;
	}          //end of col_enorm

	/*--------------------------------- fdjac2 --------------------------------
	 *
	 *     This subroutine computes a forward-difference approximation
	 *     to the m by n Jacobian matrix associated with a specified 
	 *     problem of m functions in n variables.
	 *
	 *     The calling statement is
	 *
	 *	fdjac2(m,n,x,fvec,epsfcn,wa)
	 *
	 *     where
	 *                                            
	 *	fcn (input) is an abstract function (i.e., you have to supply it in a
	 *   subclass of LM) which calculates the functions.  fcn should be written as follows:
	 *
	 *   	    boolean fcn(double x[], double y[], boolean printFlag)
	 *     	    {
	 *             calculate the functions at x and place this result vector in y
	 *             return true to continue processing
	 *           }
	 *
	 *         fcn should calculate y = fcn(x), where x is a vector of length
	 *         n and y is a vector of length m.  printFlag indicates whether
	 *         this call is being made for debug printing purposes only.
	 *         fcn should return true if processing should continue, false
	 *         to stop the Levenberg-Marquardt iteration.
	 *
	 *	m is a positive integer input variable set to the number
	 *	  of functions.
	 *
	 *	n is a positive integer input variable set to the number
	 *	  of variables. n must not exceed m.
	 *
	 *	x is an input array of length n.
	 *
	 *	fvec is an input array of length m which must contain the
	 *	  functions evaluated at x.
	 *
	 *	fjac is an output m by n array which contains the
	 *	  approximation to the Jacobian matrix evaluated at x.
	 *
	 *	iflag is an integer variable which can be used to terminate
	 *	  the execution of fdjac2. See description of fcn.
	 *
	 *	epsfcn is an input variable used in determining a suitable
	 *	  step length for the forward-difference approximation. This
	 *	  approximation assumes that the relative errors in the
	 *	  functions are of the order of epsfcn. If epsfcn is less
	 *	  than the machine precision, it is assumed that the relative
	 *	  errors in the functions are of the order of the machine
	 *	  precision.
	 *
	 *	wa is a work array of length m.
	 *
	 *     Functions called:
	 *
	 *	user-supplied ...... fcn
	 *
	 *     Argonne National Laboratory. Minpack Project. March 1980.
	 *     Burton S. Garbow, Kenneth E. Hillstrom, Jorge J. More
	 */
	void fdjac2(int m, int n, double x[], double fvec[],
	            double epsfcn, double wa[])
	{
	  //int i,j;
	  double eps,h,temp;

	  temp = Math.max(epsfcn, MACHEP);
	  eps = Math.sqrt(temp);
	  //int ij = 0;
	  for (int j=0; j<n; j++) {
	    temp = x[j];
	    h = eps * Math.abs(temp);
	    if (h == 0.0)
	      h = eps;
	    x[j] = temp + h;
	    if (!fcn(x,wa,false)) {
	      iflag = -1;
	      return;
	    }
	    x[j] = temp;
	    for (int i=0; i<m; i++) {
	      fjac[i][j] = (wa[i] - fvec[i])/h;
	      //ij += 1;
	    }
	  }
	}          /* end of fdjac2 */
}
