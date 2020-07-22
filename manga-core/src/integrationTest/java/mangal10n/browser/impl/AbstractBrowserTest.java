package mangal10n.browser.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import mangal10n.browser.Browser;
import mangal10n.browser.Request;
import mangal10n.browser.Response;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public abstract class AbstractBrowserTest {

	protected Gson gson;
	protected Browser browser;

	@Before
	public void setUp() {
		gson = new Gson();
		browser = createBrowser();
	}

	@Test
	public void testGetWithoutParams() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/get")
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());
			assertTrue(jsonObject.getAsJsonObject("args").keySet().isEmpty());
		}
	}

	@Test
	public void testGetWithParams() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/get")
				.addQueryParameter("someKey", "someValue")
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());

			jsonObject = jsonObject.getAsJsonObject("args");
			assertFalse(jsonObject.keySet().isEmpty());
			assertTrue(jsonObject.has("someKey"));
			assertEquals("someValue", jsonObject.get("someKey").getAsString());
		}
	}

	@Test
	public void testGetAppendHeader() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/get")
				.addHeader("SomeKey", "SomeValue")
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());

			assertTrue(jsonObject.has("headers"));

			jsonObject = jsonObject.getAsJsonObject("headers");
			//"Somekey" - не опечатка! Возможно особенность httpbin.org. Нужно учитывать.
			assertTrue(jsonObject.has("Somekey"));
			assertEquals("SomeValue", jsonObject.get("Somekey").getAsString());
		}
	}

	@Test
	public void testPostWithoutData() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/post")
				.post()
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());

			assertEquals("", jsonObject.get("data").getAsString());
			assertEquals(0, jsonObject.getAsJsonObject("headers").get("Content-Length").getAsInt());
		}
	}

	@Test
	public void testPostWithData() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/post")
				.post("SomeString")
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());

			assertEquals("SomeString", jsonObject.get("data").getAsString());
			assertEquals(10, jsonObject.getAsJsonObject("headers").get("Content-Length").getAsInt());
		}
	}

	@Test
	public void testMultipart() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/post")
				.addMultipartData("mp-key1", "mp-value1")
				.addMultipartData("mp-key2", "mp-value2")
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());
			assertFalse(jsonObject.getAsJsonObject("form").keySet().isEmpty());

			JsonObject form = jsonObject.getAsJsonObject("form");
			assertTrue(form.has("mp-key1"));
			assertEquals("mp-value1", form.get("mp-key1").getAsString());
			assertTrue(form.has("mp-key2"));
			assertEquals("mp-value2", form.get("mp-key2").getAsString());

			JsonObject headers = jsonObject.getAsJsonObject("headers");
			assertTrue(headers.get("Content-Type").getAsString().startsWith("multipart/form-data; boundary="));
		}
	}

	@Test
	public void testSendFile() throws IOException {
		final Request request = browser.requestBuilder()
				.url("https://httpbin.org/post")
				.addMultipartData("file", "new file.txt",
						"text/plain", IOUtils.resourceToByteArray("/testfile.txt"))
				.build();

		try (Response response = request.execute()) {
			JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
//			System.out.println(jsonObject.toString());
			assertFalse(jsonObject.getAsJsonObject("files").keySet().isEmpty());

			JsonObject files = jsonObject.getAsJsonObject("files");
			assertTrue(files.has("file"));
			assertEquals(IOUtils.resourceToString("/testfile.txt", StandardCharsets.UTF_8),
					files.get("file").getAsString());

			JsonObject headers = jsonObject.getAsJsonObject("headers");
			assertTrue(headers.get("Content-Type").getAsString().startsWith("multipart/form-data; boundary="));
		}
	}

	protected abstract Browser createBrowser();
}
