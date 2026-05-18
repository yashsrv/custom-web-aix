package com.yashsrv.customweb;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@DesignerComponent(
	version = 1,
	versionName = "1.0",
	description = "Developed by Yash Srivastava using Fast.",
	iconName = "icon.png"
)
public class CustomWeb extends AndroidNonvisibleComponent {

	private final OkHttpClient client = new OkHttpClient();

	// Simple properties.
	private String baseUrl = "";
	private YailDictionary requestDict = new YailDictionary();
	private int callTimeout = 0;

  public CustomWeb(ComponentContainer container) {
    super(container.$form());
  }

	@SimpleProperty(description = "")
	public void BaseUrl(String baseUrl) {
		if (!baseUrl.isEmpty()) {
			this.baseUrl = baseUrl;
		} else {
			ErrorOccured("BaseUrl", "The BaseUrl cannot be empty.");
		}
	}

	@SimpleProperty(description = "")
	public String BaseUrl() {
		return baseUrl;
	}

	@SimpleProperty(description = "")
	public void RequestHeaders(YailDictionary requestHeaders) {
		if (!requestHeaders.isEmpty()) {
			this.requestDict = requestHeaders;
		} else {
			ErrorOccured("RequestHeaders", "The RequestHeaders size cannot be 0.");
		}
	}

	@SimpleProperty(description = "")
	public YailDictionary RequestHeaders() {
		return requestDict;
	}

	@SimpleProperty(description = "")
	public void CallTimeout(int callTimeout) {
		if (callTimeout > 0) {
			this.callTimeout = callTimeout;
		} else {
			ErrorOccured("CallTimeout", "The CallTimeout cannot be negative.");
		}
	}

	@SimpleProperty(description = "")
	public int CallTimeout() {
		return callTimeout;
	}

  @SimpleFunction(description = "")
	public void Get(String tag, String endpoint) {
		performHttpRequest(tag, endpoint, "GET", null);
	}

	@SimpleFunction(description = "")
	public void Post(String tag, String endpoint, YailDictionary body) {
		performHttpRequest(tag, endpoint, "POST", body.toString());
	}

	private void performHttpRequest(String tag, String endpoint, String method, String body) {
		// Configure request.
		final String url = baseUrl + endpoint;
		final Headers requestHeaders = dictToHeaders(requestDict);
		client.newBuilder().callTimeout(callTimeout, TimeUnit.MICROSECONDS).build();
		RequestBody requestBody = body == null
				? null
				: RequestBody.create(MediaType.get("application/json"), body);

		final Request request = new Request.Builder()
				.url(url)
				.method(method, requestBody)
				.headers(requestHeaders)
				.build();

		// Process Call Async.
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, Response response) {
				try {
					int statusCode = response.code();
					YailDictionary responseDict = headersToDict(response.headers());

					String responseBody = "";
					if(response.body() != null) {
						responseBody = response.body().string(); // Prevents NullPointerException.
					}

					OnResponse(tag, statusCode, responseDict, responseBody);
				} catch (IOException e) {
					ErrorOccured(tag, e.getMessage());
				}
			}

			@Override
			public void onFailure(Call call, IOException e) {
				ErrorOccured(tag, e.getMessage());
			}
		});
	}

	private YailDictionary headersToDict(Headers headers) {
		YailDictionary dict = new YailDictionary();
		for (String name: headers.names()) {
			dict.put(name, headers.get(name));
		}
		return dict;
	}

	private Headers dictToHeaders(YailDictionary dict) {
		Headers.Builder headers = new Headers.Builder();
		for (Object key: dict.keySet()) {
			headers.add(key.toString(), dict.get(key).toString());
		}
		return headers.build();
	}

	@SimpleEvent(description = "")
	public void OnResponse(
			final String tag, final int statusCode, 
			final YailDictionary responseHeaders, final String responseBody) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(
						CustomWeb.this, "OnResponse", tag, statusCode, responseHeaders, responseBody);
			}
		});
	}

	@SimpleEvent(description = "")
	public void ErrorOccured(final String tag, final String errorMessage) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(CustomWeb.this, "ErrorOccured", tag, errorMessage);
			}
		});
	}
	
}
