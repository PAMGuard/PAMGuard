package PamguardMVC;

import java.util.ArrayList;

public class ThreadedObserverRepository {
	private ArrayList<ThreadedObserver> activeThreadedObservers;
	
	private static ThreadedObserverRepository currentRepository;
	
	public static void createRepository() {
		currentRepository = new ThreadedObserverRepository();
	}
	
	private ThreadedObserverRepository() {
		activeThreadedObservers = new ArrayList<ThreadedObserver>();
	}
	
	public static ThreadedObserverRepository getInstance() {
		return currentRepository;
	}
	
	public void addObserver(ThreadedObserver newObserver) {
		activeThreadedObservers.add(newObserver);
	}
	
	public void destroyAllObservers() {
		for(int i=activeThreadedObservers.size()-1;i!=0;i--) {
			activeThreadedObservers.remove(i).terminateThread();
		}
	}
	
}
