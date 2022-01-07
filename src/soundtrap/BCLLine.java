package soundtrap;

/**
 * Unpacked contents of a line of BCL data. Should be in format
 * "rtime","mticks","report","state","nl","thr","scnt"
 * @author Douglas Gillespie
 *
 */
public class BCLLine {
	
	private String rawLine;
	private Integer rtime, mticks;
	private String report; 
	private Integer state, nl; 
	private Integer thr, scnt; // only used on last E off record. 

	private BCLLine(String bclLine, Integer rtime, Integer mticks, String report, Integer state,
			Integer nl, Integer thr, Integer scnt) {
		this.rawLine = bclLine;
		this.rtime = rtime;
		this.mticks = mticks;
		this.report = report;
		this.state = state;
		this.thr = thr;
		this.scnt = scnt;
	}

	/**
	 * Create a line of BCL data. Returns null if critical information missing. 
	 * @param bclLine line read from bcl file. 
	 * @return unpacked data or null if it's not a valid line. 
	 */
	public static BCLLine makeBCLLine(String bclLine) {
		String[] subStr = bclLine.split(",");
		if (subStr.length < 4) {
			return null;
		}
		Integer rtime = null, mticks = null;
		String report = null; 
		Integer state = null, nl = null;  
		Integer thr = null, scnt = null;
		try {
			rtime = Integer.valueOf(subStr[0]);
			mticks = Integer.valueOf(subStr[1]);
			report = subStr[2].trim();
			state = Integer.valueOf(subStr[3]);
			if (subStr.length >= 5) {
				nl = Integer.valueOf(subStr[4]);
			}
			if (subStr.length >= 6) {
				thr = Integer.valueOf(subStr[5]);
			}
			if (subStr.length >= 7) {
				scnt = Integer.valueOf(subStr[6]);
			}
		}
		catch (NumberFormatException e) {
			// no need to do anything here - it's expected to fail on 
			// many records since they have two incomplete fields at the end. 
		}
		catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return new BCLLine(bclLine, rtime, mticks, report, state, nl, thr, scnt);
	}

	/**
	 * @return the rawLine
	 */
	public String getRawLine() {
		return rawLine;
	}

	/**
	 * @return the rtime
	 */
	public Integer getRtime() {
		return rtime;
	}

	/**
	 * @return the mticks
	 */
	public Integer getMticks() {
		return mticks;
	}

	/**
	 * @return the report
	 */
	public String getReport() {
		return report;
	}

	/**
	 * @return the state
	 */
	public Integer getState() {
		return state;
	}

	/**
	 * @return the nl
	 */
	public Integer getNl() {
		return nl;
	}

	/**
	 * @return the thr
	 */
	public Integer getThr() {
		return thr;
	}

	/**
	 * @return the scnt
	 */
	public Integer getScnt() {
		return scnt;
	}
	
	/**
	 * Get the time as Java like milliseconds. 
	 * @return
	 */
	public long getMilliseconds() {
		return (long) rtime * 1000 + (long) mticks / 1000;
	}
	
	/**
	 * Get the abs time in microseconds. 
	 * @return the abs time in microseconds. 
	 */
	public long getMicroSeconds() {
		return (long) rtime * 1000000L + (long) mticks;
	}
}
