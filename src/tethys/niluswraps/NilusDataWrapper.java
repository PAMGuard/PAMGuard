package tethys.niluswraps;

import PamguardMVC.PamDataBlock;

/**
 * Wrapper for Nilus data objects. This means Detections and Localization documents which 
 * should have an associated datablock, a deployment wrapper and a data count. 
 * @author dg50
 *
 * @param <T>
 */
public class NilusDataWrapper<T> extends NilusDocumentWrapper<T> {

	public Integer count;
	
	public PDeployment deployment;

	public PamDataBlock dataBlock;

	public NilusDataWrapper(T nilusObject, PamDataBlock dataBlock, PDeployment deployment, Integer count) {
		super(nilusObject);
		this.dataBlock = dataBlock;
		this.deployment = deployment;
		this.count = count;
	}

}
