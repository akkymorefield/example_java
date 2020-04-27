package example.long_polling_example;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kotlin.random.Random;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
// duplicate class name.
//import okhttp3.ResponseBody;

@Controller
@RequestMapping("/sample")
public class SampleController {

	@Value("${server.port}")
	private String port;

    private int counter = 0;

    @RequestMapping("/")
    @ResponseBody
    public synchronized String sample() throws InterruptedException {
    	counter ++;
    	int r = Random.Default.nextBoolean() ? 5000 : -5000;
    	Thread.sleep(10000 + r);
    	String result = "example"+counter;
    	System.out.println(result);
		return result;

    }

    @PostConstruct
    public void startClient() {
    	// Client
    	new Thread(() -> {
    		try {
				OkHttpClient.Builder cb = new OkHttpClient.Builder();
				cb.callTimeout(10, TimeUnit.SECONDS);
				OkHttpClient client = cb.build();

				HttpUrl url = HttpUrl.parse("http://localhost:"+port+"/sample/");
				Builder builder = new Request.Builder();
				builder = builder.url(url);
				Request request = builder.build();

				// long polling example.
		    	while(true) {
		    		Response response = null;
		    		try {
		    			response = client.newCall(request).execute();
		    			okhttp3.ResponseBody responseBody = response.body();
		    			byte[] responseByte = responseBody.bytes();
		    			if(response.code() == 200) {

		    				System.out.println("recived:" + new String(responseByte));
		    			}
		    		} catch (InterruptedIOException iioe) {
	    				System.out.println("Request timed out");
		    			continue;
		    		}
		    	}
    		} catch (IOException ioe) {
    			// Not implemented because it's example.
    			ioe.printStackTrace();
    		}
    	}).start();

    }
}
