package example.long_polling_example;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class SampleControllerTest extends SampleController {

	private MockWebServer mockWebServer;

	@Autowired
	SampleController controller;
	
	@Before
    public void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
    }

	final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            if (request == null || request.getPath() == null) {
                return new MockResponse().setResponseCode(400);
            }
            switch (request.getPath()) {
                case "/1":
                	Thread.sleep(1000);
                	return new MockResponse().setBody("Hello world!").setResponseCode(200);
                case "/2":
                    return new MockResponse().setResponseCode(301);
                case "/3":
                    return new MockResponse().setResponseCode(403);
                default:
                    return new MockResponse().setResponseCode(404);
            }
        }
    };

	@Test
	public void test() {
		
	}

}
