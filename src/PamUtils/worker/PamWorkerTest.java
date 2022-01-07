package PamUtils.worker;

public class PamWorkerTest {

	public static void main(String[] args) {
		new PamWorkerTest().run();
	}
	
	public void run() {
		new PamWorker<Integer>(new WorkerJob(), null,1, "test worker");
		
	}
	
	

	private class WorkerJob implements PamWorkWrapper<Integer> {

		private static final int TARGET = 20;
		
		@Override
		public Integer runBackgroundTask(PamWorker<Integer> pamWorker) {
			for (int i = 0; i < TARGET; i++) {
				String txt = String.format("Processed %d of %d = %d%%", i, TARGET, i*100/TARGET);
				pamWorker.update(new PamWorkProgressMessage(i*100/TARGET, txt));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return TARGET;
		}

		@Override
		public void taskFinished(Integer result) {
			System.out.println("Task finished with result " + result);
			
		}
		
	}
}
