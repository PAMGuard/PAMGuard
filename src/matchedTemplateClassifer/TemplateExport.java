package matchedTemplateClassifer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Matrix;

/**
 * Export a click/spectrum template to file in the same formats read by
 * {@link ImportTemplateMAT} and {@link ImportTemplateCSV}, so an exported
 * template can be re-imported anywhere templates are loaded.
 *
 * @author Jamie Macaulay
 */
public class TemplateExport {

	/**
	 * Export a template to a .mat file containing a <i>spectrum</i> (or waveform)
	 * 1D array and an <i>sR</i> sample rate scalar - the format read by
	 * {@link ImportTemplateMAT}.
	 * @param file - the file to write to.
	 * @param template - the template to export.
	 * @throws IOException if the file cannot be written.
	 */
	public static void exportTemplateMAT(File file, MatchTemplate template) throws IOException {
		Matrix spectrum = Mat5.newMatrix(new int[] {1, template.waveform.length});
		for (int i = 0; i < template.waveform.length; i++) {
			spectrum.setDouble(0, i, template.waveform[i]);
		}
		Mat5File matFile = Mat5.newMatFile();
		matFile.addArray("spectrum", spectrum);
		matFile.addArray("sR", Mat5.newScalar(template.sR));
		Mat5.writeToFile(matFile, file.getAbsolutePath());
	}

	/**
	 * Export a template to a .csv file where the first row is the template
	 * amplitude values and the second row is the sample rate in samples per
	 * second - the format read by {@link ImportTemplateCSV}.
	 * @param file - the file to write to.
	 * @param template - the template to export.
	 * @throws IOException if the file cannot be written.
	 */
	public static void exportTemplateCSV(File file, MatchTemplate template) throws IOException {
		FileWriter fw = new FileWriter(file);
		try {
			StringBuilder row = new StringBuilder();
			for (int i = 0; i < template.waveform.length; i++) {
				if (i > 0) {
					row.append(',');
				}
				row.append(template.waveform[i]);
			}
			fw.write(row.toString() + "\n");
			fw.write(template.sR + "\n");
			fw.flush();
		}
		finally {
			fw.close();
		}
	}

}
