package jp.morefield.longpolling;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Implement of Long Polling
 *
 */
public class LongPollingImpl implements LongPolling {

	private LongPollingCallback callback;

	private String url;

	private int timeout;

	private OkHttpClient client;

	private boolean stopFlag;

	private Call currentCall;

	private Thread thread = new Thread(() -> {
		stopFlag = true;
		while(stopFlag == true) {
			call();
		}
    });

	/**
	 * Start process
	 */
	public void start() {
        thread.start();
	}

	/**
	 * Stop process
	 */
	public void stop() {
		currentCall.cancel();
		stopFlag = false;
		callback.abort();
	}

	/**
	 * set Timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * set url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Set OkHttpClient
	 */
	public void setClient(OkHttpClient client) {
		this.client = client;
	}

	/**
	 * Set Callback Instance
	 */
	public void setCallback(LongPollingCallback callback) {
		this.callback = callback;
	}

	/**
	 *
	 */
	private void call() {
		Request request = new Request.Builder()
		           .url(url)
		           .build();
		currentCall = client.newCall(request);
		currentCall.enqueue(callback);

		// Control loop
		callback.waitProcess();
	}
}
