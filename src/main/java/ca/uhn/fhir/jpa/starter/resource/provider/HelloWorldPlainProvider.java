package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringOrListParam;
import org.apache.commons.compress.utils.IOUtils;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Random;

@Configuration
public class HelloWorldPlainProvider {
	public static int wordCount = 1000;

	private final Logger ourLog = LoggerFactory.getLogger(HelloWorldPlainProvider.class);

	@Autowired
	PatientResourceProvider patientResourceProvider;
/*
	public static void main(String[] args) {

		HelloWorldPlainProvider hp = new HelloWorldPlainProvider();

		for (int i = 1; i <= wordCount; i++) {
		//	System.out.println(i+"--"+hp.randomStringGenerator());
		}
	}
*/
	/**
	 * this methods generates a word of random length consiting of random characters
	 *
	 * @returns a String(word) of Random length
	 */
	public String randomStringGenerator() {
		// number decides what should be the size of current word
		//(int) (Math.random()*(max-min)) + min
		int number = (int) ((Math.random() * (10 - 1)) + 1);
		StringBuilder word = new StringBuilder();
		for (int i = 0; i <= number; i++) {
			// randomCharacter decides what current character should be in a word
			char randomCharacter = (char) ((int) ((Math.random() * (122 - 65)) + 65));
			// string builder appends the character to make a string
			word.append(randomCharacter);
		}
		return word.toString();
	}

	//-----------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * extended operation
	 *
	 * @param theServletRequest
	 * @param theServletResponse
	 * @throws IOException
	 */
	@Operation(name = "$returningPlainText", manualResponse = true, manualRequest = true, idempotent = true)
	public void returningStringsOfDynamicLength(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		String contentType = theServletRequest.getContentType();
		byte[] bytes = IOUtils.toByteArray(theServletRequest.getInputStream());
		ourLog.info("Received call with content type {} and {} bytes", contentType, bytes.length);
		StringBuilder stringBuilder = new StringBuilder();
		int i = 1;
		// Builds a String based of "wordCount" words that are comma seperated and indexed
		while (i < wordCount) {
			if (i % 50 == 0) {
				stringBuilder.append("\r\n" + ", ");
			}
			stringBuilder.append(i + "--" + this.randomStringGenerator() + ", ");
			i++;
		}
		stringBuilder.append(i + "--" + this.randomStringGenerator());
		// setting the content type
		theServletResponse.setContentType("text/plain");
		//generating a response based on generated string
		theServletResponse.getWriter().write(stringBuilder.toString());
		theServletResponse.getWriter().close();
	}

	//-----------------------------------------------------------------------------------------------------------------------------------------------
	@Operation(name = "$returningTextWithinResource", idempotent = true)
	public CustomResource stringsOfDynamicLength() throws IOException {

		StringBuilder stringBuilder = new StringBuilder();
		int i = 1;
		// Builds a String based of "wordCount" words that are comma seperated and indexed
		while (i < wordCount) {
			if (i % 50 == 0) {
				stringBuilder.append("\r\n" + ", ");
			}
			stringBuilder.append(i + "--" + this.randomStringGenerator() + ", ");
			i++;

		}
		stringBuilder.append(i + "--" + this.randomStringGenerator());
		// setting the content type
		CustomResource cr = new CustomResource();
		StringType st = new StringType(stringBuilder.toString());
		cr.csvText = st;
		return cr;
	}
	//-----------------------------------------------------------------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------------------------------------------------------------


	// generate a string of large random data and return it into csv file


	//Using DAO to retrieve data instead of client
	@Operation(name = "$retrievingDataUsingDAO", idempotent = true)
	public IBundleProvider closureOperation() {
		// Using DAO instead of Client
		IFhirResourceDao dao = patientResourceProvider.getDao();
		//Patient p= new Patient();

		// search method takes search parameters defined in a certain way for complex search criteria
		StringOrListParam strOrList = new StringOrListParam();
		// specifying if its a string or list parameter that we are adding
		//strOrList.addOr(new StringParam("Jameson"));
		// added the StringParam into StringAndListParam
		//	StringAndListParam theName = new StringAndListParam();
		//theName.addValue(strOrList);

		SearchParameterMap paramMap = new SearchParameterMap();
		//added the key: name and value: theName from into map to search the entity in the DAO
	/*	paramMap.add("name", theName);
		Patient p= new Patient();
		Date d= new Date();
		d.setValue("1994-02-03");
		p.setBirthDate(d);
		 dao.

	 */
		IBundleProvider res = dao.search(paramMap);
		return res;
	}

	//returning text
	@Operation(name = "$returningTxt", manualResponse = true, manualRequest = true, idempotent = true)
	public void returningTxt(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		String contentType = theServletRequest.getContentType();
		byte[] bytes = IOUtils.toByteArray(theServletRequest.getInputStream());
		//	ourLog.info("Received call with content type {} and {} bytes", contentType, bytes.length);

		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write("hello, I am echoing the same message I am writing, in an operation that parses the request, and generates a response (by echoing back the request) ");
		//theServletResponse.getWriter().write(message);
		theServletResponse.getWriter().close();
	}

	//returning CSV
	@Operation(name = "$returningCSV", manualResponse = true, manualRequest = true, idempotent = true)
	public void returningCSV(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		String contentType = theServletRequest.getContentType();
		byte[] bytes = IOUtils.toByteArray(theServletRequest.getInputStream());
		//	ourLog.info("Received call with content type {} and {} bytes", contentType, bytes.length);
		theServletResponse.setContentType("text/csv");
		theServletResponse.getWriter().write("hello, I am echoing the same message I am writing, in an operation that parses the request, and generates a response (by echoing back the request) ");
		//theServletResponse.getWriter().write(message);
		theServletResponse.getWriter().close();
	}


}
