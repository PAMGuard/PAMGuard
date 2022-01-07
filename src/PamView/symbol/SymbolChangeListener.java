package PamView.symbol;

/** 
 * Listener called when symbol data changes with a symbol maanger. 
 * 
 */
public interface SymbolChangeListener {
	
	/**
	 * Called whenever a symbol chooser has changed settings. 
	 * @param chooser the chooser which has changed. 
	 */
	public void symbolChanged(StandardSymbolChooser chooser);

}
