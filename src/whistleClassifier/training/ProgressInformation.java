package whistleClassifier.training;

public interface ProgressInformation {

	public void setText(String txt);

	public void setProgressLimits(int minLim, int maxLim);

	public void setProgress(int progress);
}
