package ca.uhn.fhir.jpa.starter.resource.provider;

import com.apicatalog.jsonld.http.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FhirRestClient {

	private static final String URL_GET_PATIETNT = "http://localhost:8080/fhir/Patient/1000792/_history/1";

	private static final String POST_REQUEST = "http://localhost:8080/fhir/";
	private static final String DIRECTORY_PATH = "/Users/khadijasubtain/Desktop/Synthea data/fhir";

	private static final String EXPUNGING_DATA_URL = "http://localhost:8080/fhir/$expunge";
	private static final String EXPUNGING_SYSTEM_LEVEL_DATA= "/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_system_level_data.json";
	private static final String EXPUNGING_DROP_ALL_DATA="/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_drop_all_data.json";
	private static final String EXPUNGING_TYPE_LEVEL_DATA="/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_type_level_data.json";
	private static final String EXPUNGING_INSTANCE_LEVEL_DATA="/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_instance_level_data.json";

	public int insertCount = 0;

	//-------------------------------------Main method---------------------------------------------

	public static void main(String[] args) {
		FhirRestClient client = new FhirRestClient();
		// client.getRequest(URL_GET_PATIETNT);
		//	client.printResponse(client.getRequest(URL_GET_PATIETNT));
		//	System.out.println(client.readFile(FILE_PATH));
		// client.postRequest(POST_REQUEST, client.readFile(FILE_PATH));
		//client.insertData();
		//client.expungingData(EXPUNGING_DROP_ALL_DATA);

	}
	//---------------------------------------INSERT ALL DATA-----------------------------------------
	public void insertData(){
		insertCount = 0;
		Set<String> set = this.listFilesUsingJavaIO(DIRECTORY_PATH);
		for(String str : set){
			this.postRequest(POST_REQUEST, this.readFile(DIRECTORY_PATH + str));
			System.out.println(insertCount + ": Bundles inserted.");
		}
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
			getRequest.addHeader("Accept", "*/*");
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
			//reading from a response
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output;

			// Simply iterate through XML response and show on console.
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param urlString
	 * @param jsonFilePath that contains the path to our json file from synthea
	 */
	public void postRequest(String urlString, String jsonFilePath) {
		try {
			//create a client
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(urlString);
			//setting headers values
			post.addHeader("Content-Type", "application/json");
			post.addHeader("Accept", "*/*");
			post.addHeader("Accept-Encoding", "gzip, deflate, br");
			post.addHeader("Connection", "keep-alive");
			//setting entity
			StringEntity input = new StringEntity(jsonFilePath);
			post.setEntity(input);
			HttpResponse response = httpClient.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = " ";
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			} else {
				printResponse(response);
				insertCount++;
			}
		} catch (
			ClientProtocolException e) {
			e.printStackTrace();
		} catch (
			IOException e) {
			e.printStackTrace();
		}
	}

	public String readFile(String filePath) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			File myObj = new File(filePath);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				stringBuilder.append(data);
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public Set<String> listFilesUsingJavaIO(String dir) {
		return Stream.of(new File(dir).listFiles())
			.filter(file -> !file.isDirectory())
			.map(File::getName)
			.collect(Collectors.toSet());
	}

	public void expungingData(String requestPath){
			this.postRequest(EXPUNGING_DATA_URL, this.readFile(requestPath));
	}
}

