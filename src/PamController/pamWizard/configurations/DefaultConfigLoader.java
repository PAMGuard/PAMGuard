package PamController.pamWizard.configurations;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionParameters;
import PamController.PSFXReadWriter;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingsGroup;
import PamController.UsedModuleInfo;
import PamController.pamWizard.AcquisitionConfigurer;
import PamController.pamWizard.PamFileImport;
import PamController.pamWizard.SoundFileSummary;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.BinaryStoreSettings;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import generalDatabase.DBControlUnit;

/**
 * The standard way of loading a PAMGuard configuration from a psfx file, used
 * for any configuration whose JSON descriptor does not name a different
 * {@code configType}.
 * <p>
 * Applying a configuration is more than just loading its settings, because a
 * configuration is written against whatever data its author happened to have.
 * Four things have to be put right:
 * <ol>
 * <li>the imported data may be at a higher sample rate than the configuration's
 * detectors expect, so a decimator is inserted (or an existing one retuned);</li>
 * <li>sound acquisition has to be pointed at the imported files;</li>
 * <li>the binary store folder and database file have to be moved somewhere that
 * exists on this machine;</li>
 * <li>anything that did not reconnect properly has to be reported rather than
 * left to fail silently at run time.</li>
 * </ol>
 * The decimator work is done by editing the settings <i>before</i> they are
 * loaded, so that the configuration is correct from the moment it is created and
 * stays correct when the user saves it.
 * <p>
 * This class is designed to be extended. An organisation needing something extra
 * to happen - fetching models from a server, say - can subclass it, override
 * {@link #prepare} or {@link #apply}, and register the subclass against its own
 * {@code configType} with {@link PamConfigLoaderFactory}.
 *
 * @author Jamie Macaulay
 */
public class DefaultConfigLoader implements PamConfigLoader {

	public static final String ACQUISITION_CLASS = "Acquisition.AcquisitionControl";
	public static final String DECIMATOR_CLASS = "decimator.DecimatorControl";
	public static final String BINARY_STORE_CLASS = "binaryFileStorage.BinaryStore";
	public static final String DATABASE_CLASS = "generalDatabase.DBControlUnit";

	/**
	 * Unit type of a decimator module, as set by {@code DecimatorControl}.
	 */
	public static final String DECIMATOR_UNIT_TYPE = "Decimator";

	/**
	 * Unit name given to a decimator inserted by the wizard. Deliberately not just
	 * "Decimator": a configuration may already contain one of its own, and two
	 * modules of the same name would collide.
	 */
	public static final String DECIMATOR_UNIT_NAME = "Sample Rate Decimator";

	/**
	 * Order of the anti aliasing filter put in front of an inserted decimator. Six
	 * is what the decimator dialog uses by default.
	 */
	public static final int DECIMATOR_FILTER_ORDER = 6;

	/**
	 * {@inheritDoc}
	 * <p>
	 * Routing by configuration type is done by {@link PamConfigLoaderFactory}, so
	 * all this needs to check is that there is something to load. Deliberately not a
	 * test of {@code configType}: a subclass registered against another type inherits
	 * this method, and would otherwise refuse every configuration it was given.
	 */
	@Override
	public boolean canLoad(PamConfigDescription config) {
		return config != null && config.getPsfxFile() != null && config.getPsfxFile().canRead();
	}

	@Override
	public PamConfigInspection inspect(PamConfigDescription config) {
		PamConfigInspection inspection = new PamConfigInspection(config);
		if (config == null || config.getPsfxFile() == null) {
			inspection.setError("No settings file for this configuration");
			return inspection;
		}

		PamSettingsGroup settingsGroup = PSFXReadWriter.getInstance().loadFileSettings(config.getPsfxFile());
		if (settingsGroup == null) {
			inspection.setError("Unable to read " + config.getPsfxFile().getName());
			return inspection;
		}
		inspection.setSettingsGroup(settingsGroup);

		List<UsedModuleInfo> modules = findUsedModules(settingsGroup);
		if (modules == null || modules.isEmpty()) {
			inspection.setError("No modules found in " + config.getPsfxFile().getName());
			return inspection;
		}
		inspection.setModules(modules);

		inspection.setHasBinaryStore(inspection.hasModule(BINARY_STORE_CLASS));
		inspection.setHasDatabase(inspection.hasModule(DATABASE_CLASS));

		/*
		 * Only a decimator the JSON explicitly names is treated as the one to retune.
		 * A configuration's decimator is very often part of what it does rather than a
		 * sample rate adapter - the static monitoring configuration decimates 96 kHz
		 * down to 10 kHz to feed a low frequency whistle detector, alongside detectors
		 * running at the full rate. Retuning that would quietly break the configuration,
		 * so an unnamed decimator is left well alone and a new one is inserted instead.
		 */
		inspection.setExistingDecimatorName(config.getDecimatorUnitName());

		// the acquisition module, and the sample rate the configuration was built at.
		UsedModuleInfo acquisition = inspection.findModule(ACQUISITION_CLASS);
		if (acquisition != null) {
			inspection.setAcquisitionUnitName(acquisition.unitName);
			inspection.setAcquisitionUnitType(acquisition.getUnitType());
			AcquisitionParameters params = readAcquisitionParams(settingsGroup, acquisition);
			if (params != null) {
				inspection.setPsfxSampleRate((double) params.getSampleRate());
				inspection.setPsfxChannels(params.getNChannels());
			}
		}

		return inspection;
	}

	@Override
	public boolean apply(PamConfigInspection inspection, PamFileImport importHandler, ConfigApplyContext context) {
		if (inspection == null || !inspection.isValid()) {
			return false;
		}
		if (context == null) {
			context = new ConfigApplyContext();
		}

		PamSettingsGroup settingsGroup = inspection.getSettingsGroup();

		// 1. give subclasses a chance to do their own work first.
		if (!prepare(inspection, importHandler, context)) {
			return false;
		}

		// 2. edit the settings before they are loaded, so the configuration is right
		// from the start and stays right when it is saved.
		DecimatorPlan decimatorPlan = planDecimation(inspection, importHandler);
		if (decimatorPlan != null) {
			applyDecimatorPlan(decimatorPlan, inspection, context);
		}

		// 3. create the modules and load their settings.
		PamController pamController = PamController.getInstance();
		pamController.loadOldSettings(settingsGroup, true);

		// 4. put right everything that is specific to this machine and these files.
		configureAcquisition(importHandler, context);
		configureBinaryStore(context);
		configureDatabase(context);

		pamController.notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);

		// 5. tell the user about anything that did not reconnect.
		checkDataSources(context);

		return true;
	}

	/**
	 * Hook for subclasses to do work before the configuration is built - downloading
	 * models, contacting a server, checking a licence and so on. The default does
	 * nothing.
	 *
	 * @param inspection    what was found in the psfx.
	 * @param importHandler the imported files.
	 * @param context       the user's choices, and where warnings are reported.
	 * @return true to carry on, false to abandon loading.
	 */
	protected boolean prepare(PamConfigInspection inspection, PamFileImport importHandler, ConfigApplyContext context) {
		return true;
	}

	/*
	 * ------------------------------------------------------------------
	 * Inspection helpers
	 * ------------------------------------------------------------------
	 */

	/**
	 * Get the list of modules a settings group will create.
	 * <p>
	 * The list is held in the PAM controller's own settings record within the group.
	 * It is found here by the static unit type and name rather than via
	 * {@link PamSettingsGroup#getUsedModuleInfo()}, so that a configuration can also
	 * be inspected when there is no live controller - which is what the unit tests
	 * do.
	 *
	 * @param settingsGroup the settings read from a psfx file.
	 * @return the module list, or null if the group has no controller record.
	 */
	@SuppressWarnings("unchecked")
	public static List<UsedModuleInfo> findUsedModules(PamSettingsGroup settingsGroup) {
		if (settingsGroup == null) {
			return null;
		}
		PamControlledUnitSettings controllerSettings = findControllerSettings(settingsGroup);
		if (controllerSettings == null) {
			return null;
		}
		Object settings = controllerSettings.getSettings();
		if (settings instanceof ArrayList) {
			return (ArrayList<UsedModuleInfo>) settings;
		}
		return null;
	}

	/**
	 * Find the PAM controller's own settings record within a settings group - the
	 * record which holds the list of modules.
	 *
	 * @param settingsGroup the settings read from a psfx file.
	 * @return the controller settings, or null.
	 */
	public static PamControlledUnitSettings findControllerSettings(PamSettingsGroup settingsGroup) {
		for (PamControlledUnitSettings aSet : settingsGroup.getUnitSettings()) {
			if (PamController.unitName.equals(aSet.getUnitName())
					&& PamController.unitType.equals(aSet.getUnitType())) {
				return aSet;
			}
		}
		return null;
	}

	/**
	 * Read the sound acquisition settings out of a settings group.
	 *
	 * @param settingsGroup the settings read from a psfx file.
	 * @param acquisition   the acquisition module info.
	 * @return the acquisition parameters, or null if they could not be read.
	 */
	private AcquisitionParameters readAcquisitionParams(PamSettingsGroup settingsGroup, UsedModuleInfo acquisition) {
		PamControlledUnitSettings settings =
				settingsGroup.findUnitSettings(acquisition.getUnitType(), acquisition.unitName);
		if (settings == null) {
			return null;
		}
		try {
			Object params = settings.getSettings();
			if (params instanceof AcquisitionParameters) {
				return (AcquisitionParameters) params;
			}
		}
		catch (Throwable e) {
			System.out.println("Unable to read acquisition settings: " + e.getMessage());
		}
		return null;
	}

	/*
	 * ------------------------------------------------------------------
	 * Decimation
	 * ------------------------------------------------------------------
	 */

	/**
	 * What needs doing about the sample rate: nothing, retune an existing decimator,
	 * or insert a new one.
	 */
	protected static class DecimatorPlan {

		/** Sample rate to decimate down to, in Hz. */
		final float targetSampleRate;

		/** Sample rate of the imported files, in Hz. */
		final float sourceSampleRate;

		/** Channel bitmap to decimate. */
		final int channelMap;

		/** Unit name of an existing decimator to retune, or null to insert a new one. */
		final String existingUnitName;

		DecimatorPlan(float targetSampleRate, float sourceSampleRate, int channelMap, String existingUnitName) {
			this.targetSampleRate = targetSampleRate;
			this.sourceSampleRate = sourceSampleRate;
			this.channelMap = channelMap;
			this.existingUnitName = existingUnitName;
		}

		boolean isInsert() {
			return existingUnitName == null;
		}
	}

	/**
	 * Decide what to do about the sample rate.
	 *
	 * @param inspection    what was found in the psfx.
	 * @param importHandler the imported files.
	 * @return the plan, or null if no decimation is needed.
	 */
	protected DecimatorPlan planDecimation(PamConfigInspection inspection, PamFileImport importHandler) {
		/*
		 * Written out rather than as a conditional expression on purpose. The Eclipse
		 * compiler this project builds with mis-generates the stack map frame for
		 * "importHandler == null ? null : importHandler.getSoundSummary()", writing the
		 * merged type as "PamController/pamWizard" instead of
		 * "PamController/pamWizard/SoundFileSummary". The class then fails verification
		 * at load time with NoClassDefFoundError: PamController/pamWizard. javac
		 * compiles the same expression correctly, so this only shows up in a Maven build.
		 */
		SoundFileSummary summary = null;
		if (importHandler != null) {
			summary = importHandler.getSoundSummary();
		}
		if (summary == null || !summary.isValid()) {
			return null;
		}
		float sourceRate = summary.getMinSampleRate();
		if (!inspection.needsDecimation(sourceRate)) {
			return null;
		}
		Double target = inspection.getTargetSampleRate();
		int channelMap = (1 << summary.getMinChannels()) - 1;
		return new DecimatorPlan(target.floatValue(), sourceRate, channelMap, inspection.getExistingDecimatorName());
	}

	/**
	 * Carry out a decimation plan by editing the settings group.
	 *
	 * @param plan       what to do.
	 * @param inspection what was found in the psfx.
	 * @param context    where warnings are reported.
	 */
	protected void applyDecimatorPlan(DecimatorPlan plan, PamConfigInspection inspection, ConfigApplyContext context) {
		if (plan.isInsert()) {
			insertDecimator(plan, inspection, context);
		}
		else {
			retuneDecimator(plan, inspection, context);
		}
	}

	/**
	 * Retune a decimator which is already part of the configuration. Nothing needs
	 * rewiring, since whatever was reading the decimator's output still is.
	 */
	protected void retuneDecimator(DecimatorPlan plan, PamConfigInspection inspection, ConfigApplyContext context) {
		PamSettingsGroup settingsGroup = inspection.getSettingsGroup();
		PamControlledUnitSettings settings =
				settingsGroup.findUnitSettings(DECIMATOR_UNIT_TYPE, plan.existingUnitName);
		if (settings == null) {
			context.addWarning("Could not find the decimator settings to set the sample rate - "
					+ "check the Decimator module before processing.");
			return;
		}
		Object params = settings.getSettings();
		if (!(params instanceof DecimatorParams)) {
			context.addWarning("Unexpected decimator settings - check the Decimator module before processing.");
			return;
		}
		DecimatorParams decimatorParams = (DecimatorParams) params;
		decimatorParams.newSampleRate = plan.targetSampleRate;
		if (decimatorParams.filterParams != null) {
			decimatorParams.filterParams.lowPassFreq = plan.targetSampleRate / 2;
		}
		decimatorParams.interpolation = interpolationFor(plan);
		settings.setSettings(decimatorParams);
		settingsGroup.replaceSettings(settings);

		System.out.println(String.format("PamConfig: decimator \"%s\" set to %s (source %s)",
				plan.existingUnitName, SoundFileSummary.formatRate(plan.targetSampleRate),
				SoundFileSummary.formatRate(plan.sourceSampleRate)));
	}

	/**
	 * Add a decimator to the configuration between sound acquisition and everything
	 * that was reading from it.
	 * <p>
	 * Modules listed in {@link PamConfigDescription#getKeepRawSourceModules()} are
	 * left reading the full rate acquisition data. This matters: a SoundTrap click
	 * detector extracts stored clicks from the raw stream, and decimating its input
	 * would destroy the clicks it exists to find.
	 */
	protected void insertDecimator(DecimatorPlan plan, PamConfigInspection inspection, ConfigApplyContext context) {
		PamSettingsGroup settingsGroup = inspection.getSettingsGroup();
		String acquisitionName = inspection.getAcquisitionUnitName();
		if (acquisitionName == null) {
			context.addWarning("The configuration has no Sound Acquisition module, so no decimator was added. "
					+ "Data will be processed at their original sample rate.");
			return;
		}

		// names the other modules will be using for the acquisition raw data.
		String decimatorName = uniqueDecimatorName(inspection);
		String acqDataName = rawDataName(acquisitionName);
		String acqLongName = longName(acquisitionName, acqDataName);
		String decDataName = decimatorDataName(decimatorName);
		String decLongName = longName(decimatorName, decDataName);

		// repoint everything that was reading the acquisition data, except the modules
		// that have to stay on full rate data.
		List<String> keepRaw = inspection.getConfig().getKeepRawSourceModules();
		SourceNameRewriter rewriter = new SourceNameRewriter(
				new String[] { acqLongName, acqDataName },
				new String[] { decLongName, decDataName });

		int repointed = 0;
		for (UsedModuleInfo module : inspection.getModules()) {
			if (module == null || ACQUISITION_CLASS.equals(module.className)) {
				continue;
			}
			if (keepRaw.contains(module.className)) {
				System.out.println(String.format("PamConfig: leaving \"%s\" on full rate data", module.unitName));
				continue;
			}
			for (PamControlledUnitSettings settings : settingsGroup.findSettingsForName(module.unitName)) {
				Object params;
				try {
					params = settings.getSettings();
				}
				catch (Throwable e) {
					continue;
				}
				if (rewriter.rewrite(params) > 0) {
					settings.setSettings(params);
					settingsGroup.replaceSettings(settings);
					repointed++;
				}
			}
		}

		// build the decimator itself and add it to the configuration.
		DecimatorParams decimatorParams = new DecimatorParams(plan.targetSampleRate, DECIMATOR_FILTER_ORDER);
		decimatorParams.rawDataSource = acqLongName;
		decimatorParams.channelMap = plan.channelMap;
		decimatorParams.interpolation = interpolationFor(plan);

		settingsGroup.addSettings(new PamControlledUnitSettings(DECIMATOR_UNIT_TYPE, decimatorName,
				DECIMATOR_CLASS, DecimatorParams.serialVersionUID, (Serializable) decimatorParams));

		if (!addUsedModule(settingsGroup, inspection,
				new UsedModuleInfo(DECIMATOR_CLASS, DECIMATOR_UNIT_TYPE, decimatorName))) {
			context.addWarning("Could not add the Decimator to the module list - "
					+ "data will be processed at their original sample rate.");
			return;
		}

		System.out.println(String.format("PamConfig: inserted decimator \"%s\" %s -> %s, repointed %d module(s)",
				decimatorName, SoundFileSummary.formatRate(plan.sourceSampleRate),
				SoundFileSummary.formatRate(plan.targetSampleRate), repointed));
	}

	/**
	 * A unit name for an inserted decimator which is not already used by a module in
	 * the configuration.
	 *
	 * @param inspection what was found in the psfx.
	 * @return an unused unit name.
	 */
	private String uniqueDecimatorName(PamConfigInspection inspection) {
		String name = DECIMATOR_UNIT_NAME;
		for (int i = 2; isNameUsed(inspection, name); i++) {
			name = DECIMATOR_UNIT_NAME + " " + i;
		}
		return name;
	}

	/**
	 * Whether a module of the given unit name is already in the configuration.
	 */
	private boolean isNameUsed(PamConfigInspection inspection, String unitName) {
		for (UsedModuleInfo module : inspection.getModules()) {
			if (module != null && unitName.equalsIgnoreCase(module.unitName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a module to the settings group's module list, immediately after sound
	 * acquisition so that it is created in a sensible order.
	 *
	 * @return true if the module was added.
	 */
	private boolean addUsedModule(PamSettingsGroup settingsGroup, PamConfigInspection inspection,
			UsedModuleInfo newModule) {
		List<UsedModuleInfo> modules = inspection.getModules();
		if (modules == null) {
			return false;
		}
		int insertAt = modules.size();
		for (int i = 0; i < modules.size(); i++) {
			if (ACQUISITION_CLASS.equals(modules.get(i).className)) {
				insertAt = i + 1;
				break;
			}
		}
		modules.add(insertAt, newModule);

		// write the modified list back into the controller's settings record.
		PamControlledUnitSettings controllerSettings = findControllerSettings(settingsGroup);
		if (controllerSettings == null) {
			return false;
		}
		controllerSettings.setSettings(modules);
		settingsGroup.replaceSettings(controllerSettings);
		return true;
	}

	/**
	 * Whether the decimator needs to interpolate. A whole number ratio between the
	 * source and output rates can be decimated exactly; anything else has to be
	 * interpolated.
	 *
	 * @return 0 for no interpolation, 1 for linear.
	 */
	private int interpolationFor(DecimatorPlan plan) {
		if (plan.targetSampleRate <= 0) {
			return 0;
		}
		double ratio = plan.sourceSampleRate / plan.targetSampleRate;
		return (ratio == Math.rint(ratio)) ? 0 : 1;
	}

	/**
	 * The name an acquisition module gives its raw data block. Must match
	 * {@code AcquisitionProcess}, which names it "Raw input data from &lt;unit name&gt;".
	 *
	 * @param acquisitionUnitName the acquisition module's unit name.
	 * @return the raw data block name.
	 */
	public static String rawDataName(String acquisitionUnitName) {
		return String.format("Raw input data from %s", acquisitionUnitName);
	}

	/**
	 * The name a decimator gives its output data block. Must match
	 * {@code DecimatorProcessW}, which names it "&lt;unit name&gt; Data".
	 *
	 * @param decimatorUnitName the decimator's unit name.
	 * @return the decimated data block name.
	 */
	public static String decimatorDataName(String decimatorUnitName) {
		return decimatorUnitName + " Data";
	}

	/**
	 * The long form of a data block name, which is how most modules record their
	 * source. Must match {@code PamDataBlock.getLongDataName()}.
	 *
	 * @param unitName the owning module's unit name.
	 * @param dataName the data block name.
	 * @return the long data name.
	 */
	public static String longName(String unitName, String dataName) {
		return unitName + ", " + dataName;
	}

	/*
	 * ------------------------------------------------------------------
	 * Post load configuration
	 * ------------------------------------------------------------------
	 */

	/**
	 * Point the newly created sound acquisition module at the imported files.
	 */
	protected void configureAcquisition(PamFileImport importHandler, ConfigApplyContext context) {
		if (importHandler == null) {
			return;
		}
		PamControlledUnit unit = PamController.getInstance().findControlledUnit(AcquisitionControl.class, null);
		if (!(unit instanceof AcquisitionControl)) {
			context.addWarning("No Sound Acquisition module was created, so the imported files were not attached.");
			return;
		}
		AcquisitionConfigurer.configure((AcquisitionControl) unit, importHandler, false);
	}

	/**
	 * Move the binary store to the folder the user chose.
	 */
	protected void configureBinaryStore(ConfigApplyContext context) {
		File binaryFolder = context.getBinaryFolder();
		if (binaryFolder == null) {
			return;
		}
		BinaryStore binaryStore = BinaryStore.findBinaryStoreControl();
		if (binaryStore == null) {
			return;
		}
		if (!binaryFolder.exists() && !binaryFolder.mkdirs()) {
			context.addWarning("Could not create the binary storage folder " + binaryFolder.getAbsolutePath());
			return;
		}
		BinaryStoreSettings settings = binaryStore.getBinaryStoreSettings().clone();
		settings.setStoreLocation(binaryFolder.getAbsolutePath());
		settings.datedSubFolders = true;
		binaryStore.setBinaryStoreSettings(settings);
	}

	/**
	 * Point the database at the file the user chose, creating it if it does not
	 * already exist.
	 */
	protected void configureDatabase(ConfigApplyContext context) {
		File databaseFile = context.getDatabaseFile();
		if (databaseFile == null) {
			return;
		}
		DBControlUnit database = DBControlUnit.findDatabaseControl();
		if (database == null) {
			return;
		}
		File parent = databaseFile.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			context.addWarning("Could not create the folder for the database " + databaseFile.getAbsolutePath());
			return;
		}
		/*
		 * Sqlite is database system 0, and passing the name as the forced name makes
		 * DBControl create the file if it is not already there.
		 */
		if (!database.selectSystem(0, true, databaseFile.getAbsolutePath())) {
			context.addWarning("Could not open the database " + databaseFile.getAbsolutePath()
					+ " - set it from the Database module before processing.");
		}
	}

	/**
	 * Check that every process found the data it is supposed to read, and report any
	 * that did not.
	 * <p>
	 * A module can end up unconnected if it recorded its source in a form that could
	 * not be repointed when the decimator was inserted - some older modules store an
	 * index rather than a name. Saying so is much better than letting it fail
	 * silently once processing starts.
	 */
	protected void checkDataSources(ConfigApplyContext context) {
		PamController pamController = PamController.getInstance();
		List<String> unconnected = new ArrayList<>();

		for (int i = 0; i < pamController.getNumControlledUnits(); i++) {
			PamControlledUnit unit = pamController.getControlledUnit(i);
			if (unit == null) {
				continue;
			}
			for (int p = 0; p < unit.getNumPamProcesses(); p++) {
				PamProcess process = unit.getPamProcess(p);
				if (process == null || process.getParentDataBlock() != null) {
					continue;
				}
				/*
				 * Acquisition and the other data sources legitimately have no parent, so only
				 * complain about processes which say they can take an input.
				 */
				if (!needsSource(process)) {
					continue;
				}
				unconnected.add(unit.getUnitName());
				break;
			}
		}

		for (String unitName : unconnected) {
			context.addWarning(String.format("The \"%s\" module has no input data selected. "
					+ "Open its settings and choose a data source before processing.", unitName));
		}
	}

	/**
	 * Whether a process declares that it needs an input data block. Processes that
	 * generate data (acquisition, and the various file readers) do not.
	 */
	private boolean needsSource(PamProcess process) {
		ArrayList<Class<? extends PamDataUnit>> compatible = process.getCompatibleDataUnits();
		return compatible != null && !compatible.isEmpty();
	}

	/**
	 * Find the raw data block a decimator should read from - the output of the sound
	 * acquisition module. Exposed for use by subclasses.
	 *
	 * @return the acquisition raw data block, or null if there is not one.
	 */
	protected PamRawDataBlock findAcquisitionRawData() {
		return PamController.getInstance().getRawDataBlock(0);
	}

	/**
	 * Find a decimator module by unit name. Exposed for use by subclasses.
	 *
	 * @param unitName the decimator's unit name.
	 * @return the decimator, or null.
	 */
	protected DecimatorControl findDecimator(String unitName) {
		PamControlledUnit unit = PamController.getInstance().findControlledUnit(DecimatorControl.class, unitName);
		return (unit instanceof DecimatorControl) ? (DecimatorControl) unit : null;
	}
}
