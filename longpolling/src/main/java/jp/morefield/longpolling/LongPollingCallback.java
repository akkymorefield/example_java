package jp.morefield.longpolling;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Implement polling utility.
 * Using OkHttp3
 */
public abstract class LongPollingCallback implements Callback {

	private final int mutextimeout = 1;

	protected Semaphore mutex;

	public LongPollingCallback() throws InterruptedException {
		super();
		mutex = new Semaphore(1);
		mutex.acquire();
	}

	/**
	 * Implement onFailure include timeout.
	 */
	public void onFailure(Call arg0, IOException arg1) {
		mutex.release();
		logErr(arg0,arg1);
	}

	/**
	 * Implement onResponse
	 */
	public void onResponse(Call arg0, Response arg1) throws IOException {
		mutex.release();
	}

	/**
	 * Logging error.
	 * @param e
	 */
	abstract void logErr(Exception e);
	abstract void logErr(Call arg0,Exception e);

	/**
	 * wait for complete.
	 */
	public void waitProcess() {
		try {
			while(!mutex.tryAcquire(mutextimeout, TimeUnit.MILLISECONDS));
		} catch (InterruptedException ie) {
			logErr(ie);
		}
	}

	/**
	 * Abort process
	 */
	public void abort() {
		mutex.release();
	}
}
