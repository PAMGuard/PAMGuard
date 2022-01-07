package PamModel;

import PamUtils.SystemTiming;

public class PamProfiler {

		boolean loadFailed = false;
		
		static PamProfiler pamProfiler;   
		
		private static final String SILIB = "PamTiming";
		//private static final String SILIB = "lib/PamTiming";
		private static final String SILIB2 = "PamTiming";
		
		//public static native long getProcessCPUTime ();
		
		private PamProfiler () {
			try  {
				System.loadLibrary(SILIB);
			}
			catch (UnsatisfiedLinkError e)
	        {
				try  {
					System.loadLibrary(SILIB2);
				}
				catch (UnsatisfiedLinkError e1)
		        {
		            loadFailed = true;
		        }

	            loadFailed = true;
	        }
			
		}
		
		static public PamProfiler getInstance() {
			if (pamProfiler == null) {
				pamProfiler = new PamProfiler();
			}
			return pamProfiler;
		}
		public class CPUUsageSnapshot
	    {
	        CPUUsageSnapshot (long time, long CPUTime)
	        {
	            m_time = time;
	            m_CPUTime = CPUTime;
	        }
	        
	        public final long m_time, m_CPUTime;
	        
	    } // end of nested class
	    
	    public CPUUsageSnapshot makeCPUUsageSnapshot ()
	    {
	    	if (loadFailed) return null;
	        return new CPUUsageSnapshot (System.currentTimeMillis (), SystemTiming.getProcessCPUTime ());
	    }
	    
	    public static double getProcessCPUUsage (CPUUsageSnapshot start, CPUUsageSnapshot end)
	    {
	    	// CPUTImes are in 100ns intervals, so divide by 10000 to get to milliseconds
	    	// and then multiply up by 100 to get percent - so divide y 100 overall
	    	if (start == null || end == null) return -1;
	    	if (end.m_time == start.m_time) return -2;
	        return ((double)(end.m_CPUTime - start.m_CPUTime) / 100) / (end.m_time - start.m_time);
	    }
	    
	    public static double getProcessCPUUsage (CPUUsageSnapshot start, CPUUsageSnapshot end, long lastTime)
	    {
	    	// CPUTImes are in 100ns intervals, so divide by 10000 to get to milliseconds
	    	// and then multiply up by 100 to get percent - so divide y 100 overall
	    	if (start == null || end == null) return -1;
	    	if (lastTime == end.m_time) return -2;
	        return ((double)(end.m_CPUTime - start.m_CPUTime) / 100) / (end.m_time - lastTime);
	    }

		
	}

