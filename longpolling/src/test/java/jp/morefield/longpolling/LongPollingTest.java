package jp.morefield.longpolling;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.test.util.ReflectionTestUtils;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LongPollingTest {
	Semaphore mutex_t = new Semaphore(1);

	// response time setting (milli seconds)
	long resp = 1000;

	final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(final RecordedRequest request) throws InterruptedException {
            if (request == null || request.getPath() == null) {
                return new MockResponse().setResponseCode(400);
            }
            switch (request.getPath()) {
                case "/hello":
                	Thread.sleep(resp);
                    return new MockResponse().setBody("test").setResponseCode(200);
                case "/transferred":
                    return new MockResponse().setResponseCode(301);
                case "/forbidden":
                    return new MockResponse().setResponseCode(403);
                default:
                    return new MockResponse().setResponseCode(404);
            }
        }
    };

    class LongPollingCallbackImpl extends LongPollingCallback {
		public LongPollingCallbackImpl() throws InterruptedException {
			super();
		}
		@Override
		void logErr(Exception e) {
			resultArg0 = null;
			resultArg1 = null;
			resultE = e;
			mutex_t.release();
		}
		@Override
		void logErr(Call arg0, Exception e) {
			resultArg0 = arg0;
			resultArg1 = null;
			resultE = e;
			mutex_t.release();
		}
		@Override
		public void onResponse(Call arg0, Response arg1) throws IOException {
			super.onResponse(arg0, arg1);
			resultArg0 = arg0;
			resultArg1 = arg1;
			resultE = null;
			mutex_t.release();
		}
	}

    private Call resultArg0 = null;
	private Response resultArg1 = null;
	private Exception resultE = null;

	@Test
	public void test1() throws Exception {

		int port = getPort();
		resp = 1000;				// 1秒でレスポンスする

		// setting MockWebServer
		final MockWebServer server = new MockWebServer();
		server.setDispatcher(dispatcher);
		server.start(port);

		// setting test target
		LongPolling longPolling = new LongPollingImpl();
		longPolling.setTimeout(10);
		longPolling.setUrl("http://localhost:"+port+"/hello");
		longPolling.setClient(new OkHttpClient());
		longPolling.setCallback(new LongPollingCallbackImpl());

		mutex_t.acquire();
		longPolling.start();

		mutex_t.acquire();
		mutex_t.release();

		// check result.

		// check onResponse called
		if ( resultArg0 == null) {
			fail();
		}
		if ( !resultArg1.isSuccessful() ) {
			fail();
		}
		if ( !resultArg1.body().string().equals("test")) {
			fail();
		}

		resultArg1.close();

		longPolling.stop();

	}

	/**
	 * When socket timeout.
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception {

		int port = getPort();
		resp = 100000;				// 100秒でレスポンスする

		// setting MockWebServer
		final MockWebServer server = new MockWebServer();
		server.setDispatcher(dispatcher);
		server.start(port);

		// setting test target
		LongPolling longPolling = new LongPollingImpl();
		longPolling.setTimeout(1);
		longPolling.setUrl("http://localhost:"+port+"/hello");	// エラーレスポンス
		longPolling.setClient(new OkHttpClient());
		longPolling.setCallback(new LongPollingCallbackImpl());

		mutex_t.acquire();
		longPolling.start();

		mutex_t.acquire();
		mutex_t.release();

		// check result.

		// check onResponse called
		if ( resultArg0 == null) {
			fail();
		}
		if (  !resultE.getClass().equals(SocketTimeoutException.class) ) {
			fail();
		}
	}

	@Test
	public void test3() throws Exception {
		int port = getPort();
		resp = 100000;				// 100秒でレスポンスする

		// setting MockWebServer
		final MockWebServer server = new MockWebServer();
		server.setDispatcher(dispatcher);
		server.start(port);

		// setting test target
		LongPolling longPolling = new LongPollingImpl();
		longPolling.setTimeout(1);
		longPolling.setUrl("http://localhost:"+port+"/hello");
		longPolling.setClient(new OkHttpClient());
		longPolling.setCallback(new LongPollingCallbackImpl());

		mutex_t.acquire();
		longPolling.start();

		Thread.sleep(500);

		Thread th = (Thread)ReflectionTestUtils.getField(longPolling,"thread");
		th.interrupt();

		mutex_t.acquire();
		mutex_t.release();

		// check result.
		// check onResponse called
		if ( resultArg0 != null) {
			fail();
		}
		if ( resultArg1 != null) {
			fail();
		}
		if (  !resultE.getClass().equals(InterruptedException.class) ) {
			fail();
		}
	}

	// utility method.
	//
	int getPort() throws IOException {
		int port;
		try (Socket socket = new Socket()) {
		    socket.bind(null);
		    port = socket.getLocalPort();
		}
		return port;
	}
}
