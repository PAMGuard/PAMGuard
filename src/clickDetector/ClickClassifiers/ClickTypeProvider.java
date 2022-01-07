package clickDetector.ClickClassifiers;

import PamView.symbol.SymbolData;

/**
 * An object which provides info on and can change click types. This provides
 * the possible click types which is used by data map etc to figure out what
 * click types might be available. The class which implements this will be
 * responsible for changing the click type flag in a ClickDetection.
 * <p>
 * Any process which changes clikc types should implement this.
 * <p>
 * Any child of the click control which changes click types should implments
 * ClickTypeProvider.
 * 
 * @author Jamie Macaulay
 *
 */
public interface ClickTypeProvider {

	
	/**
	 * Get a list of species names.
	 * @return list speces names.
	 */
	public String[] getSpeciesList();
	
	/**
	 * Get a list of symbols for each species corresponding to getSpeciesList. 
	 * @return list of species symbols.
	 */
	public SymbolData[] getSymbolsData();
	
    /**
     * Returns a list of the currently-defined click types / species codes.
     * @return int array with the codes
     */
    public int[] getCodeList();
    
    /**
     * Returns the index in the list for the click type.
     * @param code - the click type code
     * @return the index. 
     */
	public int codeToListIndex(int code);
	
	
}
