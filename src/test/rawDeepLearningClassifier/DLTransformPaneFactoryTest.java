package test.rawDeepLearningClassifier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransform.DLTransformType;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.SimpleTransform;
import org.jamdev.jdl4pam.transforms.jsonfile.DLTransformsParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import rawDeepLearningClassifier.dlClassification.archiveModel.ArchiveModelWorker;
import org.jamdev.jdl4pam.genericmodel.GenericModelParams;
import rawDeepLearningClassifier.layoutFX.dlTransfroms.DLImageTransformPane;
import rawDeepLearningClassifier.layoutFX.dlTransfroms.DLTransformPane;
import rawDeepLearningClassifier.layoutFX.dlTransfroms.DataTransformPaneFactory;

/**
 * Test that there is a settings pane for every transform so that loading a
 * model does not fail when its transforms are shown. Previously the transforms
 * of the BioDCASE YOLO model (e.g. spectrogram_scipy and normalize_percentile)
 * had no settings pane and loading the model threw a NullPointerException
 * which left the class names and transforms blank.
 */
public class DLTransformPaneFactoryTest {

	private static final String PDTF = "./src/test/resources/rawDeepLearningClassifier/YOLO/biodcase_task2/biodcase_task2_yolo11s.pdtf";

	@BeforeAll
	public static void startFX() {
		try {
			Platform.startup(() -> {});
		}
		catch (IllegalStateException e) {
			//the toolkit is already running
		}
	}

	/**
	 * Run code on the FX thread and rethrow any error.
	 */
	private static void runFX(Runnable runnable) throws Throwable {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Throwable> error = new AtomicReference<Throwable>();
		Platform.runLater(() -> {
			//exceptions in property listeners (e.g. a spinner given the wrong number type) are not thrown
			//but passed to the uncaught exception handler.
			Thread.UncaughtExceptionHandler handler = Thread.currentThread().getUncaughtExceptionHandler();
			Thread.currentThread().setUncaughtExceptionHandler((thread, e) -> error.compareAndSet(null, e));
			try {
				runnable.run();
			}
			catch (Throwable e) {
				error.compareAndSet(null, e);
			}
			finally {
				Thread.currentThread().setUncaughtExceptionHandler(handler);
				latch.countDown();
			}
		});
		assertTrue(latch.await(60, TimeUnit.SECONDS), "Timed out waiting for the FX thread");
		if (error.get() != null) throw error.get();
	}

	@Test
	public void allTransformPanesTest() throws Throwable {
		runFX(() -> {
			for (DLTransformType type : DLTransformType.values()) {
				DLTransform transform = DLTransformsFactory.makeDLTransform(type, 48000);
				if (transform == null) continue; //the transform cannot be made with default settings
				DLTransformPane pane = DataTransformPaneFactory.getSettingsPane(transform);
				assertNotNull(pane, "No settings pane for " + type);
				pane.setParams(transform);
				assertNotNull(pane.getDLTransform(), "No transform from the settings pane for " + type);
			}
		});
	}

	@Test
	public void bioDCASETransformsTest() throws Throwable {
		String jsonString = DLTransformsParser.readJSONString(new File(Paths.get(PDTF).toAbsolutePath().normalize().toString()));
		GenericModelParams params = new ArchiveModelWorker().makeModelParams(jsonString);
		ArrayList<DLTransform> transforms = DLTransformsFactory.makeDLTransforms(params.dlTransforms);
		assertEquals(7, transforms.size());

		//the parameters before they are shown in the settings panes
		List<Number[]> original = new ArrayList<Number[]>();
		for (DLTransform transform : transforms) {
			original.add(((SimpleTransform) transform).getParams() == null ? null : ((SimpleTransform) transform).getParams().clone());
		}

		runFX(() -> {
			//this is what the model settings pane does when a model is loaded
			DLImageTransformPane transformsPane = new DLImageTransformPane();
			transformsPane.setTransforms(transforms);

			//the parameters are unchanged after a round trip through the settings panes e.g. the
			//window type of the scipy spectrogram, which has no spinner, is kept.
			for (int i = 0; i < transforms.size(); i++) {
				DLTransformPane pane = DataTransformPaneFactory.getSettingsPane(transforms.get(i));
				pane.setParams(transforms.get(i));
				SimpleTransform transform = (SimpleTransform) pane.getDLTransform();
				if (original.get(i) == null) continue;
				double[] expected = new double[original.get(i).length];
				double[] actual = new double[transform.getParams().length];
				for (int j = 0; j < expected.length; j++) expected[j] = original.get(i)[j].doubleValue();
				for (int j = 0; j < actual.length; j++) actual[j] = transform.getParams()[j].doubleValue();
				assertArrayEquals(expected, actual, 1e-9, "Parameters changed for " + transform.getDLTransformType());
			}
		});
	}

}
