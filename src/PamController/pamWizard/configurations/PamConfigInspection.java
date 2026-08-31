package PamController.pamWizard.configurations;

import java.util.ArrayList;
import java.util.List;

import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;

/**
 * What was learned about a configuration by reading its psfx file, without
 * applying anything. The import wizard uses this to tell the user what a
 * configuration will create, to decide whether to ask for storage paths, and to
 * work out whether a decimator is needed.
 * <p>
 * Reading a psfx is cheap: the settings of each module are only deserialised on
 * demand, so inspecting one costs little more than reading the module list and
 * the sound acquisition settings.
 *
 * @author Jamie Macaulay
 */
public class PamConfigInspection {

	private final PamConfigDescription config;

	private PamSettingsGroup settingsGroup;

	private List<UsedModuleInfo> modules = new ArrayList<>();

	private String acquisitionUnitName;

	private String acquisitionUnitType;

	private Double psfxSampleRate;

	private Integer psfxChannels;

	private String existingDecimatorName;

	private boolean hasBinaryStore;

	private boolean hasDatabase;

	private String error;

	public PamConfigInspection(PamConfigDescription config) {
		this.config = config;
	}

	/**
	 * The configuration that was inspected.
	 * @return the configuration description.
	 */
	public PamConfigDescription getConfig() {
		return config;
	}

	/**
	 * The settings read from the psfx file. This is the object which is modified and
	 * then handed to the PAM controller to build the configuration.
	 *
	 * @return the settings group, or null if the psfx could not be read.
	 */
	public PamSettingsGroup getSettingsGroup() {
		return settingsGroup;
	}

	public void setSettingsGroup(PamSettingsGroup settingsGroup) {
		this.settingsGroup = settingsGroup;
	}

	/**
	 * The modules the configuration will create.
	 * @return the module list, never null.
	 */
	public List<UsedModuleInfo> getModules() {
		return modules;
	}

	public void setModules(List<UsedModuleInfo> modules) {
		this.modules = (modules == null) ? new ArrayList<>() : modules;
	}

	/**
	 * Whether the configuration contains a module of the given class.
	 *
	 * @param className the module class name.
	 * @return true if the configuration contains that module.
	 */
	public boolean hasModule(String className) {
		return findModule(className) != null;
	}

	/**
	 * Find a module by class name.
	 *
	 * @param className the module class name.
	 * @return the module info, or null if the configuration has no such module.
	 */
	public UsedModuleInfo findModule(String className) {
		for (UsedModuleInfo module : modules) {
			if (module != null && className.equals(module.className)) {
				return module;
			}
		}
		return null;
	}

	/**
	 * The unit name of the sound acquisition module in the psfx. Needed to work out
	 * the name of its raw data block, which is what other modules reference as their
	 * data source.
	 *
	 * @return the acquisition unit name, or null if the psfx has no acquisition module.
	 */
	public String getAcquisitionUnitName() {
		return acquisitionUnitName;
	}

	public void setAcquisitionUnitName(String acquisitionUnitName) {
		this.acquisitionUnitName = acquisitionUnitName;
	}

	/**
	 * The unit type of the sound acquisition module in the psfx.
	 * @return the acquisition unit type, or null.
	 */
	public String getAcquisitionUnitType() {
		return acquisitionUnitType;
	}

	public void setAcquisitionUnitType(String acquisitionUnitType) {
		this.acquisitionUnitType = acquisitionUnitType;
	}

	/**
	 * The sample rate stored in the psfx file's own acquisition settings, i.e. the
	 * rate the configuration was built and tested at.
	 *
	 * @return the psfx sample rate in Hz, or null if it could not be read.
	 */
	public Double getPsfxSampleRate() {
		return psfxSampleRate;
	}

	public void setPsfxSampleRate(Double psfxSampleRate) {
		this.psfxSampleRate = psfxSampleRate;
	}

	/**
	 * The channel count stored in the psfx file's own acquisition settings.
	 * @return the psfx channel count, or null if it could not be read.
	 */
	public Integer getPsfxChannels() {
		return psfxChannels;
	}

	public void setPsfxChannels(Integer psfxChannels) {
		this.psfxChannels = psfxChannels;
	}

	/**
	 * The unit name of a decimator already present in the psfx. When there is one it
	 * is retuned to the target sample rate rather than a new decimator being
	 * inserted, which avoids having to rewire everything downstream of it.
	 *
	 * @return the decimator unit name, or null if the psfx has no decimator.
	 */
	public String getExistingDecimatorName() {
		return existingDecimatorName;
	}

	public void setExistingDecimatorName(String existingDecimatorName) {
		this.existingDecimatorName = existingDecimatorName;
	}

	/**
	 * Whether the configuration contains a binary store, and so needs a folder.
	 * @return true if a binary store is present.
	 */
	public boolean hasBinaryStore() {
		return hasBinaryStore;
	}

	public void setHasBinaryStore(boolean hasBinaryStore) {
		this.hasBinaryStore = hasBinaryStore;
	}

	/**
	 * Whether the configuration contains a database, and so needs a database file.
	 * @return true if a database is present.
	 */
	public boolean hasDatabase() {
		return hasDatabase;
	}

	public void setHasDatabase(boolean hasDatabase) {
		this.hasDatabase = hasDatabase;
	}

	/**
	 * Whether the user needs to be asked where data should be stored.
	 * @return true if the configuration has a binary store or a database.
	 */
	public boolean needsStoragePaths() {
		return hasBinaryStore || hasDatabase;
	}

	/**
	 * Why the configuration could not be inspected.
	 * @return the error message, or null if inspection succeeded.
	 */
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Whether the configuration can be applied.
	 * @return true if the psfx was read successfully.
	 */
	public boolean isValid() {
		return error == null && settingsGroup != null;
	}

	/**
	 * The sample rate the configuration's detectors are designed for. Taken from the
	 * JSON descriptor if it declares one, otherwise from the psfx file's own
	 * acquisition settings.
	 *
	 * @return the target sample rate in Hz, or null if neither is available.
	 */
	public Double getTargetSampleRate() {
		if (config != null && config.getTargetSampleRate() != null) {
			return config.getTargetSampleRate();
		}
		return psfxSampleRate;
	}

	/**
	 * Work out whether data at the given sample rate need decimating for this
	 * configuration. Decimation is only ever downwards - data cannot usefully be
	 * upsampled - so this is false when the imported data are already at or below
	 * the target rate.
	 *
	 * @param importedSampleRate the sample rate of the imported files, in Hz.
	 * @return true if a decimator is needed.
	 */
	public boolean needsDecimation(double importedSampleRate) {
		Double target = getTargetSampleRate();
		if (target == null || target <= 0 || importedSampleRate <= 0) {
			return false;
		}
		return importedSampleRate > target;
	}
}
