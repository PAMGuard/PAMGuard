package annotationMark.spectrogram;

import java.util.ArrayList;

import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

/**
 * Provides {@link SpectrogramAnnotationTable} displays, i.e. a table of the annotations
 * and their measurements, to the user display system.
 *
 * @author Jamie Macaulay
 */
public class SpectrogramAnnotationTableProvider implements UserDisplayProvider {

	private SpectrogramAnnotationModule annotationModule;

	private ArrayList<SpectrogramAnnotationTable> tables = new ArrayList<>();

	public SpectrogramAnnotationTableProvider(SpectrogramAnnotationModule annotationModule) {
		this.annotationModule = annotationModule;
	}

	@Override
	public String getName() {
		return annotationModule.getUnitName() + " table";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		SpectrogramAnnotationTable table = new SpectrogramAnnotationTable(annotationModule);
		tables.add(table);
		return table;
	}

	@Override
	public Class getComponentClass() {
		return SpectrogramAnnotationTable.class;
	}

	@Override
	public int getMaxDisplays() {
		return 0;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		tables.remove(component);
	}

	/**
	 * Tell the tables the annotation settings have changed so they can update their columns.
	 */
	public void settingsChanged() {
		for (SpectrogramAnnotationTable table : tables) {
			table.settingsChanged();
		}
	}

}
