package ca.uhn.fhir.jpa.starter.resource.provider;

import com.apicatalog.jsonld.http.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FhirRestClient {
	private static final String URL_GET_PATIETNT = "http://localhost:8080/fhir/Patient/1000252/_history/1";

	public static void main(String[] args) {
		FhirRestClient client = new FhirRestClient();
		//client.getRequest(URL_GET_PATIETNT);
		client.printResponse(client.getRequest(URL_GET_PATIETNT));
	}

	//---------------------------------------GET REQUEST-------------------------------------------------
	public HttpResponse getRequest(String urlString) {
		HttpResponse response = null;
		try {

			// create HTTP Client
			HttpClient httpClient = HttpClientBuilder.create().build();

			// Create new getRequest with below mentioned URL
			HttpGet getRequest = new HttpGet(urlString);

			// Add additional header to getRequest which accepts application/xml data
			getRequest.addHeader("accept", "*/*");
			//Accept-Encoding header
			getRequest.addHeader("Accept-Encoding", "gzip, deflate, br");
			//connection header
			getRequest.addHeader("Connection", "keep-alive");
			// Execute your request and catch response
			response = httpClient.execute(getRequest);

			// Check for HTTP response code: 200 = success
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

		} catch (
			ClientProtocolException e) {
			e.printStackTrace();
		} catch (
			IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	//-----------------------------------------PRINT RESPONSE----------------------------------------
	public void printResponse(HttpResponse response) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;
			System.out.println("============Output:============");

			// Simply iterate through XML response and show on console.
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void postRequest(String urlString, String jsonString) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(urlString);

			StringEntity input = new StringEntity(jsonString);
			post.setEntity(input);
			HttpResponse response = httpClient.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = " ";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
		} catch (
			ClientProtocolException e) {
			e.printStackTrace();
		} catch (
			IOException e) {
			e.printStackTrace();
		}
	}
}
