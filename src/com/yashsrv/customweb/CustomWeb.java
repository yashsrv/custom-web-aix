package com.yashsrv.customweb;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

import com.yashsrv.customweb.helpers.HttpMethod;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;

@DesignerComponent(
	version = 1,
	versionName = "1.0",
	description = "Developed by Yash Srivastava using Fast.",
	iconName = "icon.png"
)
public class CustomWeb extends AndroidNonvisibleComponent {

	private OkHttpClient client = new OkHttpClient();

	// Simple properties.
	private String baseUrl = "";
	private Headers requestHeaders = Headers.of();
	private int callTimeout = 0;

  public CustomWeb(ComponentContainer container) {
    super(container.$form());
  }

	@SimpleProperty(description = "")
	public void BaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@SimpleProperty(description = "")
	public String BaseUrl() {
		return baseUrl;
	}

	@SimpleProperty(description = "")
	public void RequestHeaders(YailDictionary requestDict) {
		if (!requestDict.isEmpty()) {
			this.requestHeaders = dictToHeaders(requestDict);
		} else {
			this.requestHeaders = Headers.of();
		}
	}

	@SimpleProperty(description = "")
	public YailDictionary RequestHeaders() {
		return headersToDict(requestHeaders);
	}

	@SimpleProperty(description = "")
	public void CallTimeout(int callTimeout) {
		if (callTimeout >= 0) {
			this.callTimeout = callTimeout;
			this.client = client.newBuilder().callTimeout(callTimeout, TimeUnit.MILLISECONDS).build();
		} else {
			this.client = client.newBuilder().callTimeout(0, TimeUnit.MILLISECONDS).build();
		}
	}

	@SimpleProperty(description = "")
	public int CallTimeout() {
		return callTimeout;
	}

  @SimpleFunction(description = "")
	public void MakeHttpRequest(
			String tag, @Options(HttpMethod.class) String method,
			String endpoint, String body) {
		if (Arrays.asList("GET", "DELETE", "HEAD", "OPTIONS").contains(method)) {
			body = null;
		}
		performHttpRequest(tag, endpoint, method, body);
	}

	@SimpleFunction(description = "")
	public YailDictionary JsonToDictionary (String jsonString) {
		try {
			return (YailDictionary) JsonUtil.getObjectFromJson(jsonString, true);
		} catch (JSONException e) {
			ErrorOccurred("JsonToDictionary", e.getMessage());
		}
		return new YailDictionary();
	}

	@SimpleFunction(description = "")
	public String DictionaryToJson (YailDictionary dictionary) {
		return dictionary.toString();
	}

	private void performHttpRequest( 
			String tag, String endpoint, String method, String body) {
		if (baseUrl.isEmpty()) {
			ErrorOccurred(tag, "The BaseUrl cannot be empty.");
			return;
		}

		final Request request;
		try {
			// Configure request.
			final String url = URI.create(baseUrl).resolve(endpoint).toString();
			final RequestBody requestBody = body == null
					? null
					: RequestBody.create((MediaType) null, body);
			request = new Request.Builder()
					.url(url)
					.method(method, requestBody)
					.headers(requestHeaders)
					.build();
		} catch (IllegalArgumentException e) {
			ErrorOccurred(tag, e.getMessage());
			return;
		}

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
					ErrorOccurred(tag, e.getMessage());
				}
			}

			@Override
			public void onFailure(Call call, IOException e) {
				if (e instanceof SocketTimeoutException) {
					CallTimedOut(tag, e.getMessage());
				} else {
					ErrorOccurred(tag, e.getMessage());
				}
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
			String tag, int statusCode, YailDictionary responseHeaders, String responseBody) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(
						CustomWeb.this, "OnResponse", tag, statusCode, responseHeaders, responseBody);
			}
		});
	}

	@SimpleEvent(description = "")
	public void CallTimedOut(String tag, String errorMessage) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(CustomWeb.this, "CallTimedOut", tag, errorMessage);
			}
		});
	}

	@SimpleEvent(description = "")
	public void ErrorOccurred(String tag, String errorMessage) {
		form.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				EventDispatcher.dispatchEvent(CustomWeb.this, "ErrorOccurred", tag, errorMessage);
			}
		});
	}
	
}
