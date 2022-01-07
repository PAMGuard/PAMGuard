/**
 * 
 */
package PamUtils;

import java.util.ArrayList;


/**
 * @author Graham Weatherup
 * @param <E>
 *
 */



public class PairedList<A,B>{
	ArrayList<A> as;
	ArrayList<B> bs;
	
	PairedList(){
		
	}
	
	
	Boolean add(A a,B b){
		boolean A = as.add(a);
		if (A){
			boolean B = bs.add(b);
			if (B){
				return true;
			}else {
				if(a!=as.remove(as.size()-1)){
					return null;
				}else{
					System.out.println("Could not add "+ b +", and could not remove "+a+". The PairedList is now unsyncronised");
					return false;
				}
			}
		}else{
			return false;
		}
	}
	
	void remove(A a){
		boolean B = bs.remove(getB(a));
		boolean A = as.remove(a);
		
//		boolean A = as.add(a);
//		if (A){
//			boolean B = bs.add(b);
//			if (B){
//				return true;
//			}else {
//				if(a!=as.remove(as.size()-1)){
//					return null;
//				}else{
//					System.out.println("Could not add "+ b +", and could not remove "+a+". The PairedList is now unsyncronised");
//					return false;
//				}
//			}
//		}else{
//			return false;
//		}
		
	}
	
	
	B getB(A a){
		return bs.get(as.indexOf(a));
	}
	
	A getA(B b){
		return as.get(bs.indexOf(b));
	}
	
	int indexOfA(A a){
		return as.indexOf(a);
	}
	
	int indexOfB(B a){
		return bs.indexOf(a);
	}
	
}
