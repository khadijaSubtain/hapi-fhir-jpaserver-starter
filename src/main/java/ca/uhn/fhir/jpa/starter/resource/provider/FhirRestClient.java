package ca.uhn.fhir.jpa.starter.resource.provider;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FhirRestClient {

	/* FOR GET URL
	http://localhost:8080/fhir/$retrievingAllRandomPatients
	http://localhost:8080/fhir/$retrievingBundle
   http://localhost:8080/fhir/Patient/1000792/_history/1
	 */
	private static final String URL_GET_PATIETNT = "http://localhost:8080/fhir/$retrievingBundle";

	private static final String POST_REQUEST = "http://192.168.2.41:8080/fhir";
	//"http://localhost:8080/fhir/";
	private static final String EXPUNGING_DATA_URL = "http://localhost:8080/fhir/$expunge";
	private static final String EXPUNGING_SYSTEM_LEVEL_DATA = "/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_system_level_data.json";
	private static final String EXPUNGING_DROP_ALL_DATA = "/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_drop_all_data.json";
	private static final String EXPUNGING_TYPE_LEVEL_DATA = "/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_type_level_data.json";
	private static final String EXPUNGING_INSTANCE_LEVEL_DATA = "/Users/khadijasubtain/Documents/IntelliJ_workspace/FHIR/hapi-fhir-jpaserver-starter/src/main/resources/requests/expunge_instance_level_data.json";
	///Users/khadijasubtain/Downloads/synthea-master/output/fhir/externalResources/
	private static String DIRECTORY_PATH = "/Users/khadijasubtain/Downloads/synthea-master/output/fhir/";
	private static String FILE_NAME = "";
	private static String DESTINATION_PATH_CORRECT_FILES = "/Users/khadijasubtain/Downloads/synthea-master/output/fhir/insertedFiles/";
	private static String DESTINATION_PATH_ERROR_FILES = "/Users/khadijasubtain/Downloads/synthea-master/output/fhir/failedFiles/";
	private static String ERROR_FILE_PATH = "/Users/khadijasubtain/Downloads/synthea-master/output/fhir/errors.txt";
	public int insertCount = 0;

	//-------------------------------------Main method---------------------------------------------

	public static void main(String[] args) {
		FhirRestClient client = new FhirRestClient();
		//client.getRequest(URL_GET_PATIETNT);

		long beforeETLtime = new Date().getTime();
		//client.printResponse(client.getRequest(URL_GET_PATIETNT));
		long afterETLtime = new Date().getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(afterETLtime - beforeETLtime);
		long milliSeconds = TimeUnit.MILLISECONDS.toMillis(afterETLtime - beforeETLtime);
		//System.out.println("Extraction and Loading time: MINUTES "+ seconds/60 +" SECONDS: "+ seconds + " MILLISECONDS: "+ milliSeconds);

		//	System.out.println(client.readFile(FILE_PATH));

		// client.postRequest(POST_REQUEST, client.readFile(FILE_PATH), true);

		client.insertData();

		//	client.expungingData(EXPUNGING_DROP_ALL_DATA);

	}

	//---------------------------------------INSERT ALL DATA-----------------------------------------
	public void insertData() {
		insertCount = 0;
		Set<String> set = this.listFilesUsingJavaIO(DIRECTORY_PATH);
		long timeBeforeInserting = new Date().getTime();

		for (String str : set) {
			if (str.charAt(0) != '.' && !str.contains("errors")) {
				System.out.println("FILE PATH: " + str);
				FILE_NAME = str;
				this.postRequest(POST_REQUEST, this.readFile(DIRECTORY_PATH + str), false);
				moveFile((DIRECTORY_PATH + FILE_NAME), DESTINATION_PATH_CORRECT_FILES + FILE_NAME);
				System.out.println(insertCount + ": Bundles inserted.");
			}
		}

		long timeAfterInserting = new Date().getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeAfterInserting - timeBeforeInserting);
		long milliSeconds = TimeUnit.MILLISECONDS.toMillis(timeAfterInserting - timeBeforeInserting);

		System.out.println("INSERTING TIME: " + insertCount + " inserts is " + (seconds / 60) + " MINUTES, " + seconds + " SECONDS, and " + milliSeconds + " MILLISECONDS.");
	}

	//-----------------------------------------MOVE FILE----------------------------------------
	private static void moveFile(String src, String dest) {
		Path result = null;
		try {
			result = Files.move(Paths.get(src), Paths.get(dest));
		} catch (IOException e) {
			System.out.println("Exception while moving file: " + e.getMessage());
		}
		if (result != null) {
			System.out.println("File moved successfully.");
		} else {
			System.out.println("File movement failed.");
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

			// Add additional header to getRequest which accepts (application/xml/json/txt any kind of data) data
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

	public String printResponse(HttpResponse response) {
		StringBuilder str = new StringBuilder();
		try {
			//reading from a response
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output = "";

			// Simply iterate through json response and show on either console or return it.
			while ((output = br.readLine()) != null) {
				//	System.out.println(output);
				str.append(output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}

//-----------------------------------------POST REQUEST----------------------------------------

	/**
	 * @param urlString
	 * @param jsonFilePath that contains the path to our json file from synthea
	 */
	public void postRequest(String urlString, String jsonFilePath, boolean printResponse) {
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
			String responseText = printResponse(response);

			if (response.getStatusLine().getStatusCode() != 200) {
				moveFile((DIRECTORY_PATH + FILE_NAME), DESTINATION_PATH_ERROR_FILES + FILE_NAME);
				logErrors(responseText);
			}
			insertCount++;
		} catch (
			ClientProtocolException e) {
			e.printStackTrace();
		} catch (
			IOException e) {
			e.printStackTrace();
		}
	}

	//-----------------------------------------ERROR LOG----------------------------------------
	public void logErrors(String responseText) {
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter(ERROR_FILE_PATH, true);
			fWriter.write(FILE_NAME);
			fWriter.write(String.format("														"));
			fWriter.write(String.format("---------------------------------------"));
			fWriter.write(String.format("														"));
			fWriter.write(responseText);
			fWriter.write(String.format("                               			                               			"));
			fWriter.write(String.format("***************************************"));
			fWriter.write(String.format("                                                    			          			"));
			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//-----------------------------------------READ FILE----------------------------------------

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

//-----------------------------------------LIST OF FILES----------------------------------------

	public Set<String> listFilesUsingJavaIO(String dir) {
		return Stream.of(new File(dir).listFiles())
			.filter(file -> !file.isDirectory())
			.map(File::getName)
			.collect(Collectors.toSet());
	}

//-----------------------------------------EXPUNGING DATA----------------------------------------

	public void expungingData(String requestPath) {
		this.postRequest(EXPUNGING_DATA_URL, this.readFile(requestPath), true);
	}
}

