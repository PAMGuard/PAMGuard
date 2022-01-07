package PamView;

public class FullScreen {

	private static boolean goFullScreen = false;

	/**
	 * @return the goFullScreen
	 */
	public static boolean isGoFullScreen() {
		return goFullScreen;
	}

	/**
	 * @param goFullScreen the goFullScreen to set
	 */
	public static void setGoFullScreen(boolean goFullScreen) {
		FullScreen.goFullScreen = goFullScreen;
	}

}
