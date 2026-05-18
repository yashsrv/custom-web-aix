package com.yashsrv.customweb;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;

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

  public CustomWeb(ComponentContainer container) {
    super(container.$form());
  }

	@SimpleProperty(description = "")
	public void BaseUrl(String baseUrl) {
		if (!baseUrl.isEmpty()) {
			this.baseUrl = baseUrl;
		} else {
			ErrorOccured("BaseUrl", "", "The BaseUrl cannot be empty.");
		}
	}

	@SimpleProperty(description = "")
	public String BaseUrl() {
		return baseUrl;
	}

	@SimpleProperty(description = "")
	public void RequestHeaders(YailDictionary requestHeaders) {
		if (requestHeaders.isEmpty()) {
			this.requestDict = requestHeaders;
		} else {
			ErrorOccured("RequestHeaders", "", "The RequestHeaders size cannot be 0.");
		}
	}

	@SimpleProperty(description = "")
	public YailDictionary RequestHeaders() {
		return requestDict;
	}

  @SimpleFunction(description = "")
	public void Get(String tag, String endpoint) {
		performRequest(tag, endpoint, "GET", requestDict, null);
	}

	private void performRequest(String tag, String endpoint, String method, 
			YailDictionary requestDict, RequestBody body) {

		String url = baseUrl + endpoint;
		Headers requestHeaders = dictToHeaders(requestDict);
		Request request = new Request.Builder()
			.url(url)
			.method(method, body)
			.headers(requestHeaders)
			.build();

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

					OnResponse(tag, method, statusCode, responseDict, responseBody);
				} catch (IOException e) {
					ErrorOccured(tag, method, e.getMessage());
				}
			}

			@Override
			public void onFailure(Call call, IOException e) {
				ErrorOccured(tag, method, e.getMessage());
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
			final String tag, final String method, final int statusCode, 
			final YailDictionary responseHeaders, final String responseBody) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(
						CustomWeb.this, "OnResponse", tag, method, statusCode, responseHeaders, responseBody);
			}
		});
	}

	@SimpleEvent(description = "")
	public void ErrorOccured(final String tag, final String method, final String errorMessage) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(CustomWeb.this, "ErrorOccured", tag, method, errorMessage);
			}
		});
	}
	
}
