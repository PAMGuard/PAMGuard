package targetMotionOld;

import Localiser.LocaliserModel;
import PamView.PamSymbol;
import PamguardMVC.PamDataUnit;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
public interface TargetMotionModel<T extends PamDataUnit> extends LocaliserModel<T>{
	
	public PamSymbol getPlotSymbol(int iResult);
	
}
