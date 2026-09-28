package test.pamWizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import PamController.pamWizard.configurations.SourceNameRewriter;

/**
 * Tests for the reflective repointing of module data sources, which is what lets
 * a decimator be inserted between sound acquisition and the modules that follow
 * it.
 */
public class SourceNameRewriterTest {

	private static final String ACQ_LONG = "Sound Acquisition, Raw input data from Sound Acquisition";
	private static final String ACQ_SHORT = "Raw input data from Sound Acquisition";
	private static final String DEC_LONG = "Sample Rate Decimator, Sample Rate Decimator Data";
	private static final String DEC_SHORT = "Sample Rate Decimator Data";

	private SourceNameRewriter newRewriter() {
		return new SourceNameRewriter(
				new String[] { ACQ_LONG, ACQ_SHORT },
				new String[] { DEC_LONG, DEC_SHORT });
	}

	/** Stands in for a module's settings class. */
	static class Params {
		public String rawDataSource;
		public String unrelated = "leave me alone";
		public int channelMap = 3;
		Nested nested;
		String[] extraSources;
		List<Params> children;
		Params cycle;
	}

	static class Nested {
		String source;
	}

	@Test
	public void replacesTheLongDataName() {
		Params params = new Params();
		params.rawDataSource = ACQ_LONG;

		assertEquals(1, newRewriter().rewrite(params));
		assertEquals(DEC_LONG, params.rawDataSource);
		assertEquals("leave me alone", params.unrelated);
	}

	@Test
	public void replacesTheShortDataName() {
		// the decimator in the static monitoring configuration stores the short form,
		// so both have to be handled.
		Params params = new Params();
		params.rawDataSource = ACQ_SHORT;

		assertEquals(1, newRewriter().rewrite(params));
		assertEquals(DEC_SHORT, params.rawDataSource);
	}

	@Test
	public void matchesExactlyAndNothingElse() {
		Params params = new Params();
		params.rawDataSource = "Some Other Module, " + ACQ_SHORT + " copy";

		assertEquals(0, newRewriter().rewrite(params));
		assertEquals("Some Other Module, " + ACQ_SHORT + " copy", params.rawDataSource);
	}

	@Test
	public void walksIntoNestedObjects() {
		Params params = new Params();
		params.nested = new Nested();
		params.nested.source = ACQ_LONG;

		assertEquals(1, newRewriter().rewrite(params));
		assertEquals(DEC_LONG, params.nested.source);
	}

	@Test
	public void walksIntoArrays() {
		Params params = new Params();
		params.extraSources = new String[] { ACQ_LONG, "something else", ACQ_SHORT };

		assertEquals(2, newRewriter().rewrite(params));
		assertEquals(DEC_LONG, params.extraSources[0]);
		assertEquals("something else", params.extraSources[1]);
		assertEquals(DEC_SHORT, params.extraSources[2]);
	}

	@Test
	public void walksIntoCollections() {
		Params params = new Params();
		Params child = new Params();
		child.rawDataSource = ACQ_LONG;
		params.children = new ArrayList<>();
		params.children.add(child);

		assertEquals(1, newRewriter().rewrite(params));
		assertEquals(DEC_LONG, child.rawDataSource);
	}

	@Test
	public void survivesACycle() {
		Params params = new Params();
		params.rawDataSource = ACQ_LONG;
		params.cycle = params;

		assertEquals(1, newRewriter().rewrite(params));
		assertEquals(DEC_LONG, params.rawDataSource);
	}

	@Test
	public void handlesNull() {
		assertEquals(0, newRewriter().rewrite(null));

		Params params = new Params();
		assertEquals(0, newRewriter().rewrite(params));
		assertNull(params.rawDataSource);
	}
}
