package PamController.command;

import PamController.memory.PamMemory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

/**
 * Returns system diagnostics including PAMGuard memory usage,
 * system memory, and CPU usage per core.
 * 
 * @author Jamie Macaulay
 */
public class DiagnosticsCommand extends ExtCommand {

	public DiagnosticsCommand() {
		super("diagnostics", true);
	}

	@Override
	public String execute(String command) {
		return getSystemDiagnostics();
	}

	@Override
	public String getHint() {
		return "Get system diagnostics (memory and CPU usage)";
	}
	
	/**
	 * Get system diagnostics including PAMGuard memory, system memory, and CPU usage.
	 * @return XML string with system diagnostics
	 */
	private String getSystemDiagnostics() {
		StringBuilder sb = new StringBuilder();
		sb.append("<SystemDiagnostics>");
		
		// PAMGuard Memory Usage
		PamMemory pamMem = new PamMemory();
		long usedMB = (pamMem.getTotal() - pamMem.getFree()) / (1024 * 1024);
		long totalMB = pamMem.getTotal() / (1024 * 1024);
		long maxMB = pamMem.getMax() / (1024 * 1024);
		sb.append(String.format("<pamguardMemoryUsedMB>%d</pamguardMemoryUsedMB>", usedMB));
		sb.append(String.format("<pamguardMemoryTotalMB>%d</pamguardMemoryTotalMB>", totalMB));
		sb.append(String.format("<pamguardMemoryMaxMB>%d</pamguardMemoryMaxMB>", maxMB));
		
		// System Memory Usage (from /proc/meminfo on Linux)
		try {
			File meminfo = new File("/proc/meminfo");
			if (meminfo.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(meminfo));
				long totalSystemMB = 0;
				long availableSystemMB = 0;
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("MemTotal:")) {
						totalSystemMB = Long.parseLong(line.split("\\s+")[1]) / 1024;
					} else if (line.startsWith("MemAvailable:")) {
						availableSystemMB = Long.parseLong(line.split("\\s+")[1]) / 1024;
					}
				}
				br.close();
				long usedSystemMB = totalSystemMB - availableSystemMB;
				sb.append(String.format("<systemMemoryUsedMB>%d</systemMemoryUsedMB>", usedSystemMB));
				sb.append(String.format("<systemMemoryTotalMB>%d</systemMemoryTotalMB>", totalSystemMB));
			}
		} catch (Exception e) {
			// If we can't read system memory, use JVM info as fallback
			try {
				OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
				long totalSystemMB = osBean.getTotalPhysicalMemorySize() / (1024 * 1024);
				long freeSystemMB = osBean.getFreePhysicalMemorySize() / (1024 * 1024);
				long usedSystemMB = totalSystemMB - freeSystemMB;
				sb.append(String.format("<systemMemoryUsedMB>%d</systemMemoryUsedMB>", usedSystemMB));
				sb.append(String.format("<systemMemoryTotalMB>%d</systemMemoryTotalMB>", totalSystemMB));
			} catch (Exception e2) {
				// Ignore if system memory is not available
			}
		}
		
		// CPU Usage per core (from /proc/stat on Linux)
		try {
			File stat = new File("/proc/stat");
			if (stat.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(stat));
				String line;
				sb.append("<cpuCores>");
				while ((line = br.readLine()) != null) {
					if (line.startsWith("cpu") && !line.startsWith("cpu ")) {
						// Parse individual CPU cores (cpu0, cpu1, etc.)
						String[] parts = line.split("\\s+");
						String cpuName = parts[0]; // e.g., "cpu0"
						int coreNum = Integer.parseInt(cpuName.substring(3));
						// CPU stats: user nice system idle iowait irq softirq...
						long user = Long.parseLong(parts[1]);
						long nice = Long.parseLong(parts[2]);
						long system = Long.parseLong(parts[3]);
						long idle = Long.parseLong(parts[4]);
						long iowait = parts.length > 5 ? Long.parseLong(parts[5]) : 0;
						
						long total = user + nice + system + idle + iowait;
						long busy = user + nice + system;
						double usage = total > 0 ? (100.0 * busy / total) : 0;
						
						sb.append(String.format("<core index=\"%d\">%.1f</core>", coreNum, usage));
					}
				}
				br.close();
				sb.append("</cpuCores>");
			}
		} catch (Exception e) {
			// If we can't read CPU stats, try JVM method
			try {
				OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
				double systemCpuLoad = osBean.getSystemCpuLoad() * 100;
				int processors = osBean.getAvailableProcessors();
				// Approximate per-core usage (this is a rough estimate)
				sb.append("<cpuCores>");
				for (int i = 0; i < processors; i++) {
					sb.append(String.format("<core index=\"%d\">%.1f</core>", i, systemCpuLoad));
				}
				sb.append("</cpuCores>");
			} catch (Exception e2) {
				// Ignore if CPU stats are not available
			}
		}
		
		sb.append("</SystemDiagnostics>");
		return sb.toString();
	}
}
