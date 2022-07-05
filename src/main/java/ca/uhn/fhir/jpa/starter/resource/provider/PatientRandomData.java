package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.OrganizationResourceProvider;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import ca.uhn.fhir.jpa.rp.r4.PractitionerResourceProvider;

@Configuration
public class PatientRandomData {
	public static int count = 1;
	public static int extractCount = 0;
	@Autowired
	PatientResourceProvider patientResourceProvider;

	@Autowired
	OrganizationResourceProvider organizationResourceProvider;

	@Autowired
	PractitionerResourceProvider practitionerResourceProvider;

	// Create a client
	FhirContext ctx = FhirContext.forR4();
	IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir");
	StringBuilder str = new StringBuilder();

	long totalExtractionTime = 0;
	long totalTransformTime = 0;
	int readCount = 0;
	long sizeOfResource = 0;

	//------------------------------------EXTENDED OPERATIONS-------------------------------------------------------------------

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	@Operation(name = "$retrievingBundle", idempotent = true)
	public Bundle retrievingBundle() {
		//resultsPatient.getEntry().get(0);
		Bundle resultsPatient = client
			.search()
			.forResource(Patient.class)
			.returnBundle(Bundle.class)
			.execute();
		System.out.println("SIZE: " + resultsPatient.getEntry().size());

		return resultsPatient;
	}

	@Operation(name = "$insertPatient", manualResponse = true, manualRequest = true, idempotent = true)
	public void insertingPatient(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

		IFhirResourceDao dao = patientResourceProvider.getDao();

		Patient randomPatient;
		int i = 0;
		long timeBeforeInserting = new Date().getTime();
		for (; i < count; i++) {
			if (i % 100 == 0) {
				System.out.println("Current Count: " + i);
			}
			randomPatient = this.createRandomPatient();
			dao.create(randomPatient);
		}

		long timeAfterInserting = new Date().getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeAfterInserting - timeBeforeInserting);
		long milliSeconds = TimeUnit.MILLISECONDS.toMillis(timeAfterInserting - timeBeforeInserting);
		System.out.print("INSERTED: " + count + " patients in " + milliSeconds + " milliSeconds, " + seconds + " seconds, and " + (seconds / 60) + " minutes. ");

		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write(i + " patients inserted");
		theServletResponse.getWriter().close();
	}

	@Operation(name = "$retrievingAllRandomPatients", manualResponse = true, manualRequest = true, idempotent = true)
	public void bundleDifferentResources(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

		System.out.println("Called bundleDifferentResources: " + Thread.currentThread());

		resetCounters();

		//Set timeout and create new client based on timeout
		ctx.getRestfulClientFactory().setSocketTimeout(12000 * 1000);
		IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir");

		System.out.println("Created client and set timeout. Loading first page.");

		//EXTRACT: Loading First Page
		long timeBeforeLoadingFirstPage = new Date().getTime();

		Bundle resultingBundle = client
			.search()
			.forResource(Patient.class)
			.returnBundle(Bundle.class)
			.execute();

		long timeAfterLoadingFirstPage = new Date().getTime();

		readCount += resultingBundle.getEntry().size();

		/*if(resultingBundle.getEntry() != null) {
			sizeOfResource = InstrumentationAgent.getObjectSize((resultingBundle.getEntry().get(0)));
		}*/

		long extractionTimeFirstPage = TimeUnit.MILLISECONDS.toMillis(timeAfterLoadingFirstPage - timeBeforeLoadingFirstPage);

		totalExtractionTime += extractionTimeFirstPage;

		System.out.println();
		System.out.println("EXTRACT: First extraction of bundle size " + resultingBundle.getEntry().size() +
			" took: " + round((extractionTimeFirstPage / 1000.0), 2) + " seconds, " + round((extractionTimeFirstPage / 60000.0), 2) + " minutes. ");

		//TRANSFORM: Transform bundle entries to string in CSV format
		transformPageToCSV(BundleUtil.toListOfResources(ctx, resultingBundle));

		//EXTRACT: Load the subsequent pages
		while (resultingBundle.getLink(IBaseBundle.LINK_NEXT) != null) {

			long timeBeforeLoadingPage = new Date().getTime();

			resultingBundle = client
				.loadPage()
				.next(resultingBundle)
				.execute();

			long timeAfterLoadingPage = new Date().getTime();
			//getting the size of records
			readCount += resultingBundle.getEntry().size();
			// consecutive time taken to load first and subsequent pages
			long extractionTimeNextPage = TimeUnit.MILLISECONDS.toMillis(timeAfterLoadingPage - timeBeforeLoadingPage);
			totalExtractionTime += extractionTimeNextPage;
/*
			System.out.println();
			System.out.println("EXTRACT: Next Page extraction of bundle size " + resultingBundle.getEntry().size() +
			" took: " + round((extractionTimeNextPage/1000.0), 2) + " seconds, " + round((extractionTimeNextPage / 60000.0), 2) + " minutes. ");
*/
			//TRANSFORM: Transform bundle entries to string in CSV format
			transformPageToCSV(BundleUtil.toListOfResources(ctx, resultingBundle));
/*
			System.out.println("EXTRACTED TOTAL: " + readCount + " patients in " + round((totalExtractionTime/1000.0), 2)  + " seconds, " + round((totalExtractionTime / 60000.0), 2) + " minutes. ");

			System.out.println("TRANSFORMED TOTAL: " + readCount + " patients in " + totalTransformTime + " milliSeconds, " +
				round((totalTransformTime / 1000.0), 2) + " seconds, " + round((totalTransformTime / 60000.0), 2) + " minutes.\r\n");

 */

		}

		System.out.println("EXTRACTED TOTAL: " + readCount + " patients in " + round((totalExtractionTime / 1000.0), 2) + " seconds, " + round((totalExtractionTime / 60000.0), 2) + " minutes. ");

		System.out.println("TRANSFORMED TOTAL: " + readCount + " patients in " + totalTransformTime + " milliSeconds, " +
			round((totalTransformTime / 1000.0), 2) + " seconds, " + round((totalTransformTime / 60000.0), 2) + " minutes.\r\n");

		//System.out.println("Total Resources size created " +  (sizeOfResource * readCount)/1000000 + "MB");

		System.out.println("Loaded " + readCount + " patients!");
		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write(str.toString());
		theServletResponse.getWriter().close();
	}

	private void resetCounters() {
		sizeOfResource = 0;
		totalExtractionTime = 0;
		totalTransformTime = 0;
		readCount = 0;
		extractCount = 0;
		str = new StringBuilder();
		str.append( "COUNT, IDENTIFIER, ACTIVE, NAME, TELECOM, GENDER, BIRTH DATE, DECEASED, ADDRESS, " +
			"MARITAL STATUS, MULTIPLE BIRTH, PRACTITIONER, ORGANIZATION, EOL\n");
	}


	//TRANSFORM
	private void transformPageToCSV(List<IBaseResource> patientsList) {
		long timeBeforeLoading = new Date().getTime();

		for (IBaseResource var : patientsList) {
			//EXTRACT
			transformPatientToCSV((Patient) var);
		}

		long timeAfterLoading = new Date().getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeAfterLoading - timeBeforeLoading);
		long milliSeconds = TimeUnit.MILLISECONDS.toMillis(timeAfterLoading - timeBeforeLoading);

		totalTransformTime += milliSeconds;

		//System.out.println("TRANSFORM: Transformed: " + patientsList.size() + " patients in " + milliSeconds + " milliSeconds, " +
		//seconds + " seconds, " + round((seconds / 60.0), 2) + " minutes. \r\n");
	}

	private void transformPatientToCSV(Patient var) {
		str.append(++extractCount);
		str.append(", ");

		//identifier
		if (var.getIdentifier() != null && var.getIdentifier().size() > 0) {
			str.append(var.getIdentifier().get(0).getValue());
			str.append(",");
		}

		//active
		if (var != null) {
			str.append(var.getActive() ? "true" : "false");
			str.append(",");
		}

		//family name
		if (var != null && var.getName().size() > 0 && var.getName().get(0).getFamily() != null) {
			str.append(var.getName().get(0).getFamily());
			str.append(" - ");
		}
		//givenName
		if (var != null && var.getName().size() > 0 && var.getName().get(0).getGiven() != null) {
			str.append(var.getName().get(0).getGiven().get(0).getValue());
			str.append(", ");
		}

		//telecom
		if (var != null && var.getTelecom().size() > 0 && var.getTelecom() != null) {
			str.append(var.getTelecom().get(0).getValue());
			str.append(", ");
		}

		//Gender
		if (var != null && var.getGender() != null) {
			str.append(var.getGender().toCode());
			str.append(", ");
		}

		//Date of Birth
		if (var != null && var.getBirthDate() != null) {
			str.append(var.getBirthDate().getYear() + "-" + var.getBirthDate().getMonth() +
				"-" + var.getBirthDate().getDate());
			str.append(", ");
		}

		//deceased
		if (var != null && var.getDeceasedBooleanType() != null  ) {
			//	str.append((var.getDeceasedBooleanType().getValue() ? "true" : "false"));
			str.append(", ");
		}

		//address
		if (var != null && var.getAddress().size() > 0 && var.getAddress() != null) {
			str.append(var.getAddress().get(0).getLine().get(0).getValue());
			str.append(", ");
		}

		//married
		if (var != null && var.getMaritalStatus() != null) {
			str.append(var.getMaritalStatus().getTextElement());
			str.append(", ");
		}

		//multipleBirth
		if (var != null && var.getMultipleBirthBooleanType() != null) {
			str.append((var.getMultipleBirthBooleanType().getValue() ? "true" : "false"));
			str.append(", ");
		}

		//practitioner family name
		if (var != null && var.getGeneralPractitioner().size() > 0 && var.getGeneralPractitioner() != null) {
			str.append(var.getGeneralPractitioner().get(0).getReference());
			str.append(", ");
		}
		//Observation
		if (var != null && var.getManagingOrganization() != null) {
			str.append(var.getManagingOrganization().getReference());
			str.append(", ");
		}
		//end of line
		str.append(" -1 ");
		str.append("\n ");


	}

	//------------------------------------CREATING PATIENT-------------------------------------------------------------

	@Operation(name = "$insertOrganization", manualResponse = true, manualRequest = true, idempotent = true)
	public void insertingOrganization(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		IFhirResourceDao dao = organizationResourceProvider.getDao();

		dao.create(createRandomOrganization());

		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write("Organization inserted");
		theServletResponse.getWriter().close();
	}
	@Operation(name = "$insertPractitioner", manualResponse = true, manualRequest = true, idempotent = true)
	public void insertingPractitioner(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		IFhirResourceDao dao = practitionerResourceProvider.getDao();

		dao.create(createRandomPractitioner());

		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write("Practitioner inserted");
		theServletResponse.getWriter().close();
	}

	@Operation(name = "$retrievingPractitioners", idempotent = true)
	public Bundle retrievingPractitioners()  {
		return client
			.search()
			.forResource(Practitioner.class)
			.returnBundle(Bundle.class)
			.execute();
	}

	public Practitioner createRandomPractitioner(){
		Practitioner practitioner = new Practitioner();
		return  practitioner;
	}

	@Operation(name = "$retrievingOrganizations", idempotent = true)
	public Bundle retrievingOrganizations() {
		return client
			.search()
			.forResource(Organization.class)
			.returnBundle(Bundle.class)
			.execute();
	}

	public Organization createRandomOrganization() {
		Organization org = new Organization();
		return org;
	}

	//Insert Patient to Bundle
	public Patient createRandomPatient() {

		Patient patient = new Patient();

		//inserting random value for identifier
		List<Identifier> identifierList = new ArrayList<>();
		identifierList.add(new Identifier().setValue(this.generateRandomIdentifier()));
		patient.setIdentifier(identifierList);

		HumanName humanName = new HumanName();
		//set family
		//	humanName.setFamily(this.randomStringGenerator());

		//	humanName.setFamily("Harry");

		//set given
		List<StringType> givenHumanNameList = new ArrayList<StringType>();
		givenHumanNameList.add(new StringType(this.randomStringGenerator()));
		humanName.setGiven(givenHumanNameList);

		//inserting patient name
		List<HumanName> humanNameList = new ArrayList<HumanName>();
		humanNameList.add(humanName);
		patient.setName(humanNameList);

		//Date OF Birth -----------------------------
		patient.setBirthDate(this.dateOfBirth());

		//inserting the address of the patient
		List<Address> theAddress = new ArrayList<>();
		theAddress.add(new Address().addLine(this.completeAddress()));
		patient.setAddress(theAddress);

		//inserting the activity status of patient
		patient.setActive(this.isActive());

		//inserting random value for deceased
		patient.setDeceased(new BooleanType(this.isDeceased()));

		//Marital status
		patient.setMaritalStatus(new CodeableConcept().setTextElement(new StringType(this.randomStringGenerator())));

		//Telecom

		List<ContactPoint> contactPointList = new ArrayList<>();
		contactPointList.add(new ContactPoint().setValue(this.phoneNumber()));
		patient.setTelecom(contactPointList);

		patient.setGender(Enumerations.AdministrativeGender.fromCode(this.generateRandomGender()));

		//multiple birth
		BooleanType multipleBirth = new BooleanType();
		patient.setMultipleBirth(multipleBirth.setValue(this.multipleBirth()));

		//Active
		patient.setActive(this.isActive());

		//Inserting Practitioner

		//Reference reference = new Reference("http://fhir.hl7.org/svc/StructureDefinition/c8973a22-2b5b-4e76-9c66-00639c99e61b");
		//reference.setType("http://fhir.hl7.org/svc/StructureDefinition/c8973a22-2b5b-4e76-9c66-00639c99e61b");

		//Reference reference = new Reference("GeneralPractitioner/12345");
	//	reference.set("Practitioner");
		Reference reference = new Reference("Practitioner/52");
		List<Reference> referenceList = new ArrayList<>();
		referenceList.add(reference);
		//patient.setGeneralPractitioner(referenceList);
		patient.setManagingOrganization(new Reference("Organization/1000102"));

		patient.setGeneralPractitioner(referenceList);
		//Inserting Organization
		patient.setManagingOrganization(new Reference("Organization/1"));
		return patient;
	}
	//----------------------------------CREATING RANDOM DATA-------------------------------------------------------------
	/*
	public static void main(String[] args){
		PatientRandomData pt= new PatientRandomData();
		System.out.print(pt.dateOfBirth().getYear() +"-" +
			pt.dateOfBirth().getMonth() + "-"+ pt.dateOfBirth().getDate());

	}

	 */

	//generate random identifier
	public String generateRandomIdentifier() {
		return (this.randomStringGenerator());
	}

	//martial status
	public boolean multipleBirth() {
		return new Random().nextBoolean();
	}
	//multiple birth
	//generate random data for Birthdate

	public Date dateOfBirth() {

		GregorianCalendar calander = new GregorianCalendar();
		int year = rangeOfYear(1950, 2020);
		calander.set(calander.YEAR, year);
		int dayOfYear = rangeOfYear(1, calander.getActualMaximum(calander.DAY_OF_YEAR));
		calander.set(calander.DAY_OF_YEAR, dayOfYear);
		//return calander.get(calander.YEAR) + "-" + (calander.get(calander.MONTH) + 1) + "-" + calander.get(calander.DAY_OF_MONTH);
		Date dateObj = new Date();
		dateObj.setYear(calander.get(calander.YEAR));
		dateObj.setMonth((calander.get(calander.MONTH) + 1));
		dateObj.setDate(calander.get(calander.DAY_OF_MONTH));
		return dateObj;
	}

	public int rangeOfYear(int start, int end) {
		return start + (int) Math.round(Math.random() * (end - start));
	}

	//generate random phone number
	public String phoneNumber() {
		long phoneNumber = 0;
		return "" + randomNumberOf_fixedLength(10, 9, 0);
	}

	public int randomNumberGenerator(int max, int min) {
		//to make upper and lower bound inclusive, adding 1
		return (int) ((Math.random() * (max - min + 1)) + min);
	}

	public long randomNumberOf_fixedLength(int length, int max, int min) {
		long number = 0;
		for (int i = 0; i <= length; i++) {
			number *= 10;
			number += randomNumberGenerator(max, min);
		}
		return number;
	}

	//generate random address
	public java.lang.String completeAddress() {

		return "House No: " + this.houseNumber() + " - Street: " + this.randomStringGenerator() + " - city: " +
			this.randomStringGenerator() + " - district:" + this.randomStringGenerator() +
			" - state:" + this.randomStringGenerator() + " - postal code:" + " H9S 2Y3";
	}

	public int houseNumber() {
		int size = randomNumberGenerator(5, 1);
		int houseNumber = 0;
		while (size >= 1) {
			houseNumber *= 10;
			houseNumber += randomNumberGenerator(9, 0);
			size--;
		}
		return houseNumber;
	}
	//make postal code


	//generate gender -- curently keeping male and female for synthetic data
	public String generateRandomGender() {
		int genderCode = this.randomNumberGenerator(4, 1);
		String gender = "";
		switch (genderCode) {
			case 1:
				gender = "male";
				break;
			case 2:
				gender = "female";
				break;
			case 3:
				gender = "other";
				break;
			case 4:
				gender = "unknown";
				break;

		}
		return gender;
	}

	// deceased
	public boolean isDeceased() {
		return new Random().nextBoolean();
	}

	//marital data- codeable (value set)
	public boolean isMarried() {
		return new Random().nextBoolean();
	}

	//active
	public boolean isActive() {
		return new Random().nextBoolean();
	}
	//generate random data for name --DONE (randomStringGenerator)
	//generate practitioner -- DONE (randomStringGenerator)
	// search the string to fill in the data

	// insert the data and return the bundle


	/**
	 * this methods generates a word of random length consiting of random characters
	 *
	 * @returns a String(word) of Random length
	 */
	public String randomStringGenerator() {
		// number decides what should be the size of current word
		int number = randomNumberGenerator(10, 1);
		StringBuilder word = new StringBuilder();
		for (int i = 0; i <= number; i++) {
			// randomCharacter decides what current character should be in a word
			char randomCharacter = (char) ((this.randomNumberGenerator(122, 65)));
			// string builder appends the character to make a string
			word.append(randomCharacter);
		}
		return word.toString();
	}
}
