package metadata.deployment;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Largely the content of the Tethys QualityAssurance schema
 * @author dg50
 *
 */
public class QAData  implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	private String objectives;
	
	private String qaAbstract;
	
	private String method;
	
	private String responsibleName;
	private String responsibleOrganisation;
	private String responsiblePosition;
	private String responsiblePhone;
	private String responsibleAddress;
	private String responsibleEmail;
	
	public QAData() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
