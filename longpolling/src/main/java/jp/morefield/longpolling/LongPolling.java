package jp.morefield.longpolling;

import okhttp3.OkHttpClient;

/**
 * Interface of long polling utility.
 * Using OkHttp3
 */
public interface LongPolling {

	/**
	 * Start task
	 */
	public void start();

	/**
	 * Stop task
	 */
	public void stop();

	/**
	 * Set Timeout
	 * @param timeout Set timeout seconds
	 */
	public void setTimeout(int timeout);

	/**
	 * Set client (use OkHttp3)
	 */
	public void setClient(OkHttpClient client);

	/**
	 * Set callback
	 */
	public void setCallback(LongPollingCallback callback);

	/**
	 * Set url to poll
	 * @param url
	 */
	public void setUrl(String url);

}
