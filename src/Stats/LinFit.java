package Stats;


/**
 * 
 * @author Doug Gillespie
 * Simple linear regression. Fitting line y = a + bx
 * Singelton class
 */
public class LinFit {
	
//	private static  LinFit singleInstance;
	
//	private LinFit() {
//		singleInstance = this;
//	}
//	
//	public static LinFit getInstance(){
//		if (singleInstance == null) {
//			return new LinFit();
//		}
//		return singleInstance;
//	}
	
	private final int ITMAX = 100;
	private final double EPS = 3.0e-7;  
	private final double FPMIN = 1.0e-30;
	
	private double a, b, siga, sigb, chi2, q;
	private double[] x, y, sig;
	private int nData;
	private boolean doneErrors;
	double wt, t, sxoss, sx=0., sy=0., st2=0., ss, sigdat;
	int mwt = 0;

	public  LinFit(double[] x, double[] y, int nData)
	{
		doFit(x, y, nData, null);
	}
	public  LinFit(double[] x, double[] y, int nData, double[] sig)
	{
		doFit(x, y, nData, sig);
	}
	private void doFit(double[] x, double[] y, int nData, double[] sig)
	{
		this.x = x;
		this.y = y;
		this.nData = nData;
		this.sig = sig;
		doneErrors = false;
		
		int i;
		if (sig != null) mwt = nData;
		
		b = 0.;
		if (mwt > 0){                 // Accumalative sums
			ss = 0.;
			for (i = 0; i < nData; i++){     // with weights
				wt = 1./SQR(sig[i]);
				ss += wt;
				sx += x[i]*wt;
				sy += y[i]*wt;
			}
		}
		else{
			for (i = 0; i < nData; i++){
				sx += x[i];                    // without weights
				sy += y[i];
			}
			ss = nData;
		}
		sxoss = sx/ss;
		if (mwt>0){
			for (i = 0; i < nData; i++){
				t = (x[i]-sxoss)/sig[i];
				st2 += t*t;
				b += t*y[i]/sig[i];
			}
		}
		else{
			for (i = 0; i < nData; i++){
				t = x[i]-sxoss;
				st2 += t*t;
				b += t*y[i];
			}
		}
		b /= st2;
		a = (sy-sx*b)/ss;
	}
	private void doErrors()
	{
		int i;
		siga = Math.sqrt((1.+sx*sx/(ss*st2))/ss);
		sigb = Math.sqrt(1./st2);
		chi2 = q = 0.;
		if (nData <= 2) return;
		if (mwt == 0){
			for (i = 0; i < nData; i++)
				chi2 += SQR(y[i]-(a)-(b)*x[i]);
			q = 1.;
			sigdat = Math.sqrt((chi2)/(nData-2));
			siga *= sigdat;
			sigb *= sigdat;
		}
		else{
			for (i = 0; i < nData; i++)
				chi2 += SQR((y[i]-a-b*x[i])/sig[i]);
			q = gammq(0.5*(nData-2),0.5*(chi2));
		}
		doneErrors = true;
	}
	
	/**
	 * @return the fit intercept
	 */
	public double getA() {
		if (doneErrors == false) doErrors();
		return a;
	}


	/**
	 * @return the fit gradient
	 */
	public double getB() {
		if (doneErrors == false) doErrors();
		return b;
	}


	public double getChi2() {
		if (doneErrors == false) doErrors();
		return chi2;
	}


	public double getQ() {
		if (doneErrors == false) doErrors();
		return q;
	}


	public double getSiga() {
		if (doneErrors == false) doErrors();
		return siga;
	}


	public double getSigb() {
		if (doneErrors == false) doErrors();
		return sigb;
	}


	double gammq(double a, double x)
	{
		// incomplete gamma function Q(a,x) = 1 - P(a,x)
		Double gamser = 0.;
		Double gammcf = 0.;
		Double gln = 0.;
		
		if (x < 0. || a <= 0.){
//			MessageBox(NULL, "Invalid arguments in Gamma function",
//			"Routine gammq", MB_ICONHAND);
			return 0;
		}
		if (x < (a+1.0)) {
			gser(gamser,a,x,gln);
			return 1.0-gamser;
		} else {
			gcf(gammcf,a,x,gln);
			return gammcf;
		}
	}   
	
	void gser(Double gamser, double a, double x, Double gln)
	{
		int n;
		double sum,del,ap;
		
		gln=gammln(a);
		if (x <= 0.0) {
			//if (x < 0.0) nrerror("x less than 0 in routine gser");
			gamser=0.0;
			return;
		} else {
			ap=a;
			del=sum=1.0/a;
			for (n=1;n<=ITMAX;n++) {
				++ap;
				del *= x/ap;
				sum += del;
				if (Math.abs(del) < Math.abs(sum)*EPS) {
					gamser=sum*Math.exp(-x+a*Math.log(x)-(gln));
					return;
				}
			}
			//nrerror("a too large, ITMAX too small in routine gser");
			return;
		}
	}      
	
	void gcf(Double gammcf, double a, double x, Double gln)
	{
		int i;
		double an,b,c,d,del,h;
		
		gln = gammln(a);
		b=x+1.0-a;
		c=1.0/FPMIN;
		d=1.0/b;
		h=d;
		for (i=1;i<=ITMAX;i++) {
			an = -i*(i-a);
			b += 2.0;
			d=an*d+b;
			if (Math.abs(d) < FPMIN) d=FPMIN;
			c=b+an/c;
			if (Math.abs(c) < FPMIN) c=FPMIN;
			d=1.0/d;
			del=d*c;
			h *= del;
			if (Math.abs(del-1.0) < EPS) break;
		}
		//if (i > ITMAX) nrerror("a too large, ITMAX too small in gcf");
		gammcf=Math.exp(-x+a*Math.log(x)-(gln))*h;
	}   
	double gammln(double xx)
	{
		double x,y,tmp,ser;
		final double[] cof = {76.18009172947146,-86.50532032941677,
				24.01409824083091,-1.231739572450155,
				0.1208650973866179e-2,-0.5395239384953e-5};
		int j;
		
		y=x=xx;
		tmp=x+5.5;
		tmp -= (x+0.5)*Math.log(tmp);
		ser=1.000000000190015;
		for (j=0;j<=5;j++) ser += cof[j]/++y;
		return -tmp+Math.log(2.5066282746310005*ser/x);
	}
	
	double SQR(double f){return f*f;};
}
