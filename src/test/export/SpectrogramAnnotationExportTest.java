package test.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;
import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
import annotation.calcs.measurement.SoundMeasurement;
import annotation.calcs.measurement.SoundMeasurementAnnotation;
import annotation.calcs.measurement.SoundMeasurementAnnotationType;
import annotation.calcs.snr.SNRAnnotation;
import annotation.calcs.spl.SPLAnnotation;
import annotation.calcs.wav.WavAnnotation;
import annotation.handler.AnnotationChoices;
import annotation.string.StringAnnotation;
import annotationMark.MarkDataUnit;
import export.MLExport.MLDetectionsManager;
import export.RExport.RExportManager;
import quickAnnotation.QuickAnnotationModule;
import test.helper.PamControllerTestHelper;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Array;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

/**
 * Check that quick spectrogram annotations export to .mat and .RData with all of their
 * annotations - note, label, SNR, SPL, wav clip and sound measurements - including
 * annotations which are missing from some of the data units.
 */
public class SpectrogramAnnotationExportTest {

	private static final SoundMeasurement[] MEASUREMENTS = { SoundMeasurement.PEAK_FREQUENCY,
			SoundMeasurement.FREQ_5, SoundMeasurement.CENTER_TIME };

	/**
	 * Make a module with the default annotations plus a few sound measurements switched
	 * on, and two marks: the first with every annotation, the second with only some.
	 */
	private List<PamDataUnit> makeMarks(QuickAnnotationModule module) {
		AnnotationChoices choices = module.getAnnotationChoices();
		for (DataAnnotationType<?> anType : module.getAnnotationHandler().getAvailableAnnotationTypes()) {
			boolean use = !(anType instanceof SoundMeasurementAnnotationType) || isTestMeasurement(anType);
			choices.setAnnotionOption(anType.getAnnotationName(), anType.getAnnotationOptions(), use);
		}

		MarkDataUnit full = new MarkDataUnit(1700000000000L, 1, 1500);
		full.setParentDataBlock(module.getAnnotationDataBlock());
		full.setFrequency(new double[] { 1000, 3000 });
		StringAnnotation note = new StringAnnotation(module.getStringAnnotationType());
		note.setString("a note");
		full.addDataAnnotation(note);
		StringAnnotation label = new StringAnnotation(module.getLabelAnnotationType());
		label.setString("whistle");
		full.addDataAnnotation(label);
		SNRAnnotation snr = new SNRAnnotation(module.getSnrAnnotationType());
		snr.setSnr(12.5);
		full.addDataAnnotation(snr);
		SPLAnnotation spl = new SPLAnnotation(module.getSPLAnnotationType());
		spl.setRms(100);
		full.addDataAnnotation(spl);
		WavAnnotation wav = new WavAnnotation(module.getWavAnnotationType());
		wav.setWavFolderName("/clips");
		wav.setWavPrefix("whistle");
		wav.setExportedWavFileName("/clips/whistle_1.wav");
		full.addDataAnnotation(wav);
		for (SoundMeasurementAnnotationType type : module.getMeasurementGroup().getAnnotationTypes()) {
			if (isTestMeasurement(type)) {
				SoundMeasurementAnnotation an = new SoundMeasurementAnnotation(type);
				an.setValue(type.getMeasurement().ordinal() + 0.5);
				full.addDataAnnotation(an);
			}
		}

		// the second mark has no measurements, SNR or wav clip.
		MarkDataUnit partial = new MarkDataUnit(1700000010000L, 1, 800);
		partial.setParentDataBlock(module.getAnnotationDataBlock());
		partial.setFrequency(new double[] { 500, 2000 });
		StringAnnotation label2 = new StringAnnotation(module.getLabelAnnotationType());
		label2.setString("click");
		partial.addDataAnnotation(label2);

		List<PamDataUnit> units = new ArrayList<>();
		units.add(full);
		units.add(partial);
		return units;
	}

	private static boolean isTestMeasurement(DataAnnotationType<?> anType) {
		if (!(anType instanceof SoundMeasurementAnnotationType)) {
			return false;
		}
		SoundMeasurement m = ((SoundMeasurementAnnotationType) anType).getMeasurement();
		for (SoundMeasurement t : MEASUREMENTS) {
			if (t == m) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void matExport() throws Exception {
		PamControllerTestHelper.InitializePamControllerForTesting();
		QuickAnnotationModule module = new QuickAnnotationModule("Quick annotation export test");
		List<PamDataUnit> units = makeMarks(module);

		File file = Files.createTempFile("annotationExport", ".mat").toFile();
		file.deleteOnExit();
		MLDetectionsManager mlManager = new MLDetectionsManager();
		assertTrue(mlManager.exportData(file, units, false));
		mlManager.close();

		Mat5File matFile = Mat5.readFromFile(file);
		Struct top = null;
		for (us.hebi.matlab.mat.types.MatFile.Entry entry : matFile.getEntries()) {
			top = (Struct) entry.getValue();
		}
		assertNotNull(top);
		// the marks are in a struct array named after their exporter.
		Struct struct = top.get("annotations");
		assertEquals(2, struct.getNumElements());

		assertEquals("a note", struct.get("note", 0).toString().replace("'", "").trim());
		assertEquals("whistle", struct.get("label", 0).toString().replace("'", "").trim());
		assertEquals("click", struct.get("label", 1).toString().replace("'", "").trim());
		assertEquals(12.5, ((Matrix) struct.get("snr", 0)).getDouble(0), 1e-9);
		assertEquals(100, ((Matrix) ((Struct) struct.get("spl", 0)).get("rms")).getDouble(0), 1e-9);
		Struct wav = (Struct) struct.get("wav", 0);
		assertTrue(wav.get("file").toString().contains("whistle_1.wav"));
		for (SoundMeasurement m : MEASUREMENTS) {
			assertEquals(m.ordinal() + 0.5, ((Matrix) struct.get(m.getColumnName(), 0)).getDouble(0), 1e-9);
			// missing from the second mark - exported as an empty matrix.
			assertEquals(0, ((Array) struct.get(m.getColumnName(), 1)).getNumElements());
		}
		matFile.close();
	}

	@Test
	public void rExport() throws Exception {
		PamControllerTestHelper.InitializePamControllerForTesting();
		QuickAnnotationModule module = new QuickAnnotationModule("Quick annotation R export test");
		List<PamDataUnit> units = makeMarks(module);

		File file = Files.createTempFile("annotationExport", ".RData").toFile();
		file.deleteOnExit();
		RExportManager rManager = new RExportManager(null);
		assertTrue(rManager.exportData(file, units, false));
		rManager.close();

		SEXP data;
		try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(file))) {
			data = new RDataReader(Context.newTopLevelContext(), in).readFile();
		}
		// pairlist(det_..._annotations = list(mark1, mark2), annotations_sR = ...)
		ListVector marks = (ListVector) ((PairList) data).getElementAsSEXP(0);
		assertEquals(2, marks.length());
		ListVector full = (ListVector) marks.getElementAsSEXP(0);
		ListVector partial = (ListVector) marks.getElementAsSEXP(1);

		assertEquals("a note", annotationList(full, "note").getElementAsString(0));
		assertEquals("whistle", annotationList(full, "label").getElementAsString(0));
		assertEquals("click", annotationList(partial, "label").getElementAsString(0));
		assertEquals(12.5, annotationList(full, "snr").getElementAsDouble(0), 1e-9);
		assertEquals(100, annotationList(full, "spl").getElementAsDouble(0), 1e-9);
		ListVector wav = annotationList(full, "wav");
		assertEquals("/clips/whistle_1.wav", wav.getElementAsString(wav.getIndexByName("file")));
		// missing annotations are there, as empty lists.
		assertEquals(0, annotationList(partial, "wav").length());
		for (SoundMeasurement m : MEASUREMENTS) {
			assertEquals(m.ordinal() + 0.5, annotationList(full, m.getColumnName()).getElementAsDouble(0), 1e-9);
			assertEquals(0, annotationList(partial, m.getColumnName()).length());
		}
	}

	private static ListVector annotationList(ListVector mark, String name) {
		int index = mark.getIndexByName(name);
		assertTrue(index >= 0, "R export is missing " + name + " in " + mark.getNames());
		return (ListVector) mark.getElementAsSEXP(index);
	}

}
