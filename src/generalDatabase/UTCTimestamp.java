package generalDatabase;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Override standard Timestamp class and stop it making UTC corrections as it writes to database
 * @author dg50
 *
 */
public class UTCTimestamp extends Timestamp {

	private static final long serialVersionUID = 1L;

	public UTCTimestamp(long time) {
		super(time);
		// TODO Auto-generated constructor stub
	}

	@Override
	public long getTime() {
		return super.getTime();
	}

	@Override
	public LocalDateTime toLocalDateTime() {
		return super.toLocalDateTime();
	}

}
