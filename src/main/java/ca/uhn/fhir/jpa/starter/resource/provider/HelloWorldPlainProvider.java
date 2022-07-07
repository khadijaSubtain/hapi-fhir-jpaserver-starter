package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.StringOrListParam;
import org.apache.commons.compress.utils.IOUtils;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class HelloWorldPlainProvider {
	public static int wordCount = 1;
	IGenericClient client = FhirContext.forR4().newRestfulGenericClient("http://localhost:8080/fhir");
	private final Logger ourLog = LoggerFactory.getLogger(HelloWorldPlainProvider.class);

	@Autowired
	PatientResourceProvider patientResourceProvider;

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
//---------------------------------------------------------------------------------------------------------------

	//--------------------------------$insertPatient BY CREATING NEW PATIENT -------------------------------------------------------------------
/*
	@Operation(name = "$insertPatient", manualResponse = true, manualRequest = true, idempotent = true)
	public void insertingPatient(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

		IFhirResourceDao dao = patientResourceProvider.getDao();

		Patient randomPatient;

		for (int i = 0; i < wordCount; i++) {
			randomPatient = this.createRandomPatient();
			System.out.println(i + "hello");
			dao.create(randomPatient);

		}
		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write(wordCount + " patients inserted");
		theServletResponse.getWriter().close();
	}


	//------------------------$retrievingAllRandomPatients FROM DATABASE USING DAO-------------------------------------------------------------------

	@Operation(name = "$retrievingAllRandomPatients", idempotent = true)
	public Bundle bundleDifferentResources() {
		FhirContext.forR4().getRestfulClientFactory().setSocketTimeout(20000 * 1000);
	//	IGenericClient client = FhirContext.forR4().newRestfulGenericClient("http://localhost:8080/fhir");

		return client
			.search()
			.forResource(Patient.class)
			.returnBundle(Bundle.class)
			.execute();

	}

	public Patient createRandomPatient() {

		IFhirResourceDao dao = patientResourceProvider.getDao();
		Patient patient = new Patient();

		HumanName humanName = new HumanName();
		//set family
		humanName.setFamily(this.randomStringGenerator());

		//set given
		List<StringType> givenHumanNameList = new ArrayList<StringType>();
		givenHumanNameList.add(new StringType(this.randomStringGenerator()));
		humanName.setGiven(givenHumanNameList);

		//set patient name
		List<HumanName> humanNameList = new ArrayList<HumanName>();
		humanNameList.add(humanName);
		patient.setName(humanNameList);

		//patient.setBirthDate(new Date(this.dateOfBirth());

		//patient.setActive(this.isActive());

		//	patient.setDeceased(new BooleanType(this.isDeceased()));


		return patient;
	}
*/
	//-----------------------------------------------------------------------------------------------------------------------------------------------
	//---------------------------------$returningPlainText--WITH RANDOMLY GENERATED STRING--------------------------------------------------------

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

		//------------------------------$returningTextWithinResource FOR RANDOMLY GENERATED TEXT---------------------------------------------------------------------------------
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

//---------------------------------------$retrievingDataUsingDAO--------------------------------------------------------------------------------------------------------


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
	//---------------------------------------$returningTxt--------------------------------------------------------------------------------------------------------

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
//---------------------------------------$returningCSV--------------------------------------------------------------------------------------------------------

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
