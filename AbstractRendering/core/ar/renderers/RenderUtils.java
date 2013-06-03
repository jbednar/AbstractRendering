package ar.renderers;

import java.util.concurrent.atomic.AtomicLong;

public class RenderUtils {
	/**Common location for controlling render progress reporting.
	 * Renderers are NOT required to respect this setting, but will
	 * if they use the "recorder" method also in this class. 
	 */
	public static boolean RECORD_PROGRESS = false;
	
	/**Instantiate a progress recorder according to the RECORD_PROGRESS setting.**/
	public static Progress recorder() {
		return RECORD_PROGRESS ? new RenderUtils.Progress.Counter() : new RenderUtils.Progress.NOP();
	}

	
	/**Utility class for recording percent progress through a known task size.**/
	public static interface Progress {
		public long count();
		public void update(long delta);
		public double percent();
		public void reset(long expected);
		
		
		/**Dummy progress recorder.  Always returns -1 for status inquiries.**/
		public static final class NOP implements Progress {
			public long count() {return -1;}
			public void update(long delta) {}
			public void reset(long expected) {}
			public double percent() {return -1;}
		}
		
		/**Thread-safe progress reporter for.**/
		public static final class Counter implements Progress {
			private final AtomicLong counter = new AtomicLong();
			private long expected=1;
			public long count() {return counter.get();}
			public void update(long delta) {counter.addAndGet(delta);}
			public void reset(long expected) {this.expected = expected; counter.set(0);}
			public double percent() {
				if (expected <=0) {return -1;}
				return counter.intValue()/((double) expected);
			} 
		}
	}
}