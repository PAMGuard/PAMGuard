package PamguardMVC;

public interface DataCreator<Tunit extends PamDataUnit> {

	public Tunit createDataUnit(long timrMilliseconds);
	
	public String getDataName();
	
}
