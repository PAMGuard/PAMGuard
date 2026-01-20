package Acquisition.layoutFX;

import pamViewFX.fxNodes.PamBorderPane;

/**
 * Simple interface that creates a pane of some type. 
 */
public interface PaneFactory {
	
	public PaneFactoryPane createPane();
	
	public static class PaneFactoryPane extends PamBorderPane {
		
		private PaneFactory factoryRef;

		public PaneFactory getFactoryRef() {
			return factoryRef;
		}

		public void setFactoryRef(PaneFactory factoryRef) {
			this.factoryRef = factoryRef;
		}

		public PaneFactoryPane(PaneFactory factoryRef){
			super();
			this.factoryRef=factoryRef;
		}
		
	
	}

	public String getPaneFactoryName();
	

}
