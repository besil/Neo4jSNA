package Utils;

import java.time.Clock;

public class Timer {
	protected static Timer instance;
	public static synchronized Timer timer() {
		if( instance == null )
			instance = new Timer();
		return instance;
	}

	protected final Clock clock;
	public long ts, te;
	public Timer() {
		clock = Clock.systemDefaultZone();
	}
	
	public void start() {
		ts = clock.millis();
		te = 0;
	}
	
	public void stop() {
		te = clock.millis();
	}
	
	public static final String totalTime(long ts, long te) {
		return "Total time: "+ ( te - ts ) / 1000.0 + " s";
	}
}
