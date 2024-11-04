package test.helper;

import PamController.PamController;
import PamController.PamSettingManager;
import pamguard.GlobalArguments;

import java.io.File;
import java.io.IOException;

/**
 * Helper to initialize a headless {@link PamController} from unit tests.
 * This will only initialize the {@link PamController} once; tests are responsible for cleaning
 * up any changes they make to the configuration.
 */
public class PamControllerTestHelper {
    private static boolean isInitialized = false;

    public static void InitializePamControllerForTesting() throws IOException {
        if (!isInitialized) {
            isInitialized = true;
            File file = File.createTempFile("OneBandControlTest", "psfx");
            file.deleteOnExit();

            GlobalArguments.setParam(GlobalArguments.BATCHFLAG, "true");
            PamSettingManager.remote_psf = file.getAbsolutePath();
            PamController.create(PamController.RUN_NOTHING);
        }
    }
}
