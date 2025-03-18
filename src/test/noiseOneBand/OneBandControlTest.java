package test.noiseOneBand;

import PamController.PamController;
import PamController.soundMedium.GlobalMedium;
import test.helper.PamControllerTestHelper;
import noiseOneBand.OneBandControl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OneBandControlTest {
	
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        PamControllerTestHelper.InitializePamControllerForTesting();
    }

    @Test
    public void MeasurementnameTest() {
        try {
            assertEquals(4, OneBandControl.NMEASURES);
            assertThrows(Exception.class, () -> OneBandControl.getMeasurementName(4));

            PamController.getInstance().getGlobalMediumManager().setCurrentMedium(GlobalMedium.SoundMedium.Water, false);
            assertEquals("RMS (dB re 1\u00B5Pa)", OneBandControl.getMeasurementName(0));
            assertEquals("0-Peak (dB re 1\u00B5Pa)", OneBandControl.getMeasurementName(1));
            assertEquals("Peak-Peak (dB re 1\u00B5Pa)", OneBandControl.getMeasurementName(2));
            assertEquals("Integrated SEL (dB re 1\u00B5Pa\u00B2s)", OneBandControl.getMeasurementName(3));

            PamController.getInstance().getGlobalMediumManager().setCurrentMedium(GlobalMedium.SoundMedium.Air, false);
            assertEquals("RMS (dB re 20\u00B5Pa)", OneBandControl.getMeasurementName(0));
            assertEquals("0-Peak (dB re 20\u00B5Pa)", OneBandControl.getMeasurementName(1));
            assertEquals("Peak-Peak (dB re 20\u00B5Pa)", OneBandControl.getMeasurementName(2));
            assertEquals("Integrated SEL (dB re 400\u00B5Pa\u00B2s)", OneBandControl.getMeasurementName(3));
        } finally {
            PamController.getInstance().getGlobalMediumManager().setCurrentMedium(GlobalMedium.SoundMedium.Water, false);
        }
    }
}