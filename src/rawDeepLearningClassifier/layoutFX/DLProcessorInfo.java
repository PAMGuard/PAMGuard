package rawDeepLearningClassifier.layoutFX;

import ai.djl.Device;
import ai.djl.engine.Engine;
import ai.djl.engine.StandardCapabilities;
import javafx.scene.paint.Color;

/**
 * Works out which physical processor the deep learning engine will actually use
 * to run a model and provides human friendly names, descriptions, icons and
 * accent colours for display in the user interface.
 * <p>
 * The deep learning models are loaded by the underlying <a
 * href="https://djl.ai">Deep Java Library (DJL)</a> engine. On most platforms
 * the default device is a CUDA capable graphics card (GPU) if one is available,
 * otherwise the computer's main processor (CPU). On Apple Silicon Macs PyTorch
 * models are loaded onto the Apple GPU using Metal Performance Shaders (MPS),
 * which is much faster than the CPU for large models; TensorFlow models run on
 * the CPU there because the bundled TensorFlow library has no Metal support. The
 * Apple Neural Engine is not used by either engine.
 *
 * @author Jamie Macaulay
 */
public class DLProcessorInfo {

	/**
	 * The broad category of processor the deep learning will run on.
	 */
	public enum ProcessorType {
		/** A CUDA capable graphics card. */
		GPU,
		/** The GPU of an Apple Silicon chip, via Metal Performance Shaders (MPS). */
		MPS,
		/** The computer's main processor. */
		CPU,
		/** The CPU cores of an Apple Silicon (M-series) chip. */
		APPLE_SILICON,
		/** The processor could not be determined. */
		UNKNOWN
	}

	/**
	 * The DJL device type string for Apple's Metal Performance Shaders (MPS). DJL
	 * does not define this as a {@link Device.Type} constant but the PyTorch engine
	 * recognises it (mapping it to PyTorch device number 13).
	 */
	private static final String MPS_DEVICE_TYPE = "mps";

	private ProcessorType processorType = ProcessorType.UNKNOWN;

	private String friendlyName = "Unknown processor";

	private String detail = "";

	private String iconString = "mdi2c-chip";

	private Color accentColor = Color.web("#BDBDBD");

	private String engineName = "";

	private String engineVersion = "";

	private String deviceString = "";

	private int gpuCount = 0;

	private DLProcessorInfo() {
	}

	/**
	 * Work out the processor that the deep learning engine will currently use to
	 * run models. This queries the default DJL engine and device.
	 *
	 * @return a populated {@link DLProcessorInfo}. Never null - if the engine
	 *         cannot be queried the processor type is {@link ProcessorType#UNKNOWN}.
	 */
	public static DLProcessorInfo getCurrentProcessorInfo() {
		DLProcessorInfo info = new DLProcessorInfo();
		try {
			Engine engine = Engine.getInstance();
			info.engineName = engine.getEngineName();
			info.engineVersion = engine.getVersion();

			Device device = engine.defaultDevice();
			info.deviceString = device.toString();
			String deviceType = device.getDeviceType();

			try {
				info.gpuCount = engine.getGpuCount();
			} catch (Throwable e) {
				info.gpuCount = 0;
			}

			if (device.isGpu()) {
				info.processorType = ProcessorType.GPU;
				info.iconString = "mdi2e-expansion-card";
				info.accentColor = Color.web("#66BB6A");
				info.friendlyName = "Graphics card (GPU)";
				boolean cuda = false;
				try {
					cuda = engine.hasCapability(StandardCapabilities.CUDA);
				} catch (Throwable e) {
					// capability check not supported - leave as false.
				}
				info.detail = cuda ? "Hardware accelerated on an NVIDIA CUDA graphics card."
						: "Hardware accelerated on the graphics card.";
				if (info.gpuCount > 1) {
					info.detail += " " + info.gpuCount + " GPUs detected.";
				}
			} else if (MPS_DEVICE_TYPE.equals(deviceType)) {
				info.processorType = ProcessorType.MPS;
				info.iconString = "mdi2a-apple";
				info.accentColor = Color.web("#CE93D8");
				info.friendlyName = "Apple GPU (Metal / MPS)";
				info.detail = "Hardware accelerated on the Apple Silicon GPU using "
						+ "Metal Performance Shaders (MPS).";
			} else if (isAppleSilicon()) {
				//DJL's default device on Apple Silicon is the CPU, but PyTorch models are
				//explicitly loaded onto the Apple GPU (MPS) - see jdl4pam's model loaders.
				//TensorFlow has no Metal support so those models run on the CPU.
				boolean pyTorch = info.engineName != null && info.engineName.toLowerCase().contains("pytorch");
				if (pyTorch) {
					info.processorType = ProcessorType.MPS;
					info.iconString = "mdi2a-apple";
					info.accentColor = Color.web("#CE93D8");
					info.friendlyName = "Apple GPU (Metal / MPS)";
					info.detail = "PyTorch models run on the Apple Silicon GPU using Metal "
							+ "Performance Shaders (MPS), with an automatic fall back to the CPU "
							+ "if a model cannot run on the GPU.";
				} else {
					info.processorType = ProcessorType.APPLE_SILICON;
					info.iconString = "mdi2a-apple";
					info.accentColor = Color.web("#E0E0E0");
					info.friendlyName = "Apple Silicon CPU";
					info.detail = "Running on the CPU cores of the Apple Silicon chip. This "
							+ "engine (e.g. TensorFlow) has no Apple GPU support. The Apple "
							+ "Neural Engine is not used.";
				}
			} else {
				info.processorType = ProcessorType.CPU;
				info.iconString = "mdi2c-cpu-64-bit";
				info.accentColor = Color.web("#42A5F5");
				info.friendlyName = "CPU";
				info.detail = "Running on the computer's main processor (CPU).";
			}
		} catch (Throwable e) {
			// the DJL engine may not be loaded / available.
			info.processorType = ProcessorType.UNKNOWN;
			info.iconString = "mdi2h-help-circle-outline";
			info.friendlyName = "Unknown processor";
			info.detail = "Could not determine which processor the deep learning engine will use.";
		}
		return info;
	}

	/**
	 * Is the application running on an Apple Silicon (arm64) Mac?
	 *
	 * @return true if running on an Apple Silicon Mac.
	 */
	private static boolean isAppleSilicon() {
		try {
			String os = System.getProperty("os.name", "").toLowerCase();
			String arch = System.getProperty("os.arch", "").toLowerCase();
			return os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm"));
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * @return the broad category of processor the model will run on.
	 */
	public ProcessorType getProcessorType() {
		return processorType;
	}

	/**
	 * @return a short, human friendly name for the processor e.g. "CPU" or
	 *         "Graphics card (GPU)".
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * @return a longer description of what the processor is doing.
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @return the Ikonli icon string representing the processor.
	 */
	public String getIconString() {
		return iconString;
	}

	/**
	 * @return an accent colour for the processor icon that reads on a dark
	 *         background.
	 */
	public Color getAccentColor() {
		return accentColor;
	}

	/**
	 * @return a description of the deep learning engine and version e.g.
	 *         "PyTorch 2.5.1".
	 */
	public String getEngineDescription() {
		if (engineName == null || engineName.isEmpty()) {
			return "Unknown engine";
		}
		if (engineVersion == null || engineVersion.isEmpty()) {
			return engineName;
		}
		return engineName + " " + engineVersion;
	}

	/**
	 * @return the raw DJL device string e.g. "cpu()" or "gpu(0)".
	 */
	public String getDeviceString() {
		return deviceString;
	}

	/**
	 * @return the number of CUDA GPUs detected.
	 */
	public int getGpuCount() {
		return gpuCount;
	}
}
