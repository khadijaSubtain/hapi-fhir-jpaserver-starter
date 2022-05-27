package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.Code;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class PatientRandomData {
	public static int count = 1;
	public static int extractCount = 0;
	@Autowired
	PatientResourceProvider patientResourceProvider;
	// Create a client
	FhirContext ctx = FhirContext.forR4();
	IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/fhir");


	//------------------------------------EXTENDED OPERATIONS-------------------------------------------------------------------

	@Operation(name = "$retrievingBundle", idempotent = true)
	public Bundle retrievingBundle()  {
		return client
				.search()
				.forResource(Patient.class)
				.returnBundle(Bundle.class)
				.execute();
	}

	@Operation(name = "$insertPatient", manualResponse = true, manualRequest = true, idempotent = true)
	public void insertingPatient(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {

		IFhirResourceDao dao = patientResourceProvider.getDao();

		Patient randomPatient;

		for (int i = 0; i <= count; i++) {
			randomPatient = this.createRandomPatient();
			dao.create(randomPatient);
		}
		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write(count + " patients inserted");
		theServletResponse.getWriter().close();
	}

	@Operation(name = "$retrievingAllRandomPatients", manualResponse = true, manualRequest = true, idempotent = true)
	public void bundleDifferentResources(HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) throws IOException {
		System.out.println("checking connection" + Thread.currentThread().toString());

		List<IBaseResource> patients = new ArrayList<>();

		Bundle resultingBundle = client
			.search()
			.forResource(Patient.class)
			.returnBundle(Bundle.class)
			.execute();

		patients.addAll(BundleUtil.toListOfResources(ctx, resultingBundle));

		long timeBeforeLoading = new Date().getTime();

		// Load the subsequent pages -EXTRACT

		while (resultingBundle.getLink(IBaseBundle.LINK_NEXT) != null ) {
			resultingBundle = client
				.loadPage()
				.next(resultingBundle)
				.execute();
			patients.addAll(BundleUtil.toListOfResources(ctx, resultingBundle));
		}
		long timeAfterLoading = new Date().getTime();
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timeAfterLoading - timeBeforeLoading);
		long milliSeconds = TimeUnit.MILLISECONDS.toMillis(timeAfterLoading - timeBeforeLoading);

		System.out.print("EXTRACTED: " + patients.size() + " patients in " + milliSeconds + " milliSeconds, " + seconds + " seconds, " + (seconds / 60) + " minutes. ");


		//TRANSFORM
		timeBeforeLoading = new Date().getTime();
		StringBuilder str = new StringBuilder();

		for (IBaseResource var : patients) {
			//EXTRACT
			transformPatientToCSV(str, (Patient) var);
		}

		timeAfterLoading = new Date().getTime();
		seconds = TimeUnit.MILLISECONDS.toSeconds(timeAfterLoading - timeBeforeLoading);
		milliSeconds = TimeUnit.MILLISECONDS.toMillis(timeAfterLoading - timeBeforeLoading);

		System.out.print("TRANSFORMED: " + patients.size() + " patients in " + milliSeconds + " milliSeconds, " + seconds + " seconds, " + (seconds / 60) + " minutes. ");

		// LOAD
		System.out.println("Loaded " + patients.size() + " patients!");
		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write(str.toString());
		theServletResponse.getWriter().close();
	}

	private void transformPatientToCSV(StringBuilder str, Patient var) {
		str.append(++extractCount);
		str.append(", ID: ");
		//identifier
		str.append(var.getIdentifier().get(0).getId());
		str.append(", NAME: ");
		//family name
		str.append(var.getName().get(0).getFamily());
		str.append(", ");
		//givenName
		str.append(var.getName().get(0).getGiven().get(0).getValue());
		str.append(", PRACTITIONER: ");
		//practitioner family name
		str.append(var.getGeneralPractitioner().get(0));
		str.append(", ADDRESS: ");
		//address
		str.append(var.getAddress().get(0).getLine().get(0).getValue());
		str.append(", TELECOM: ");
		//telecom
		str.append(var.getTelecom().get(0).getValue());
		str.append(", MARITAL STATUS: ");
		//married
		str.append(var.getMaritalStatus().getTextElement());
		//multipleBirth
		str.append((var.getMultipleBirthBooleanType().getValue() ? "true" : "false"));
		str.append(", ACTIVE: ");
		//active
		str.append(var.getActive() ? "true" : "false");
		str.append(", DECEASED:");
		//deceased
		str.append((var.getDeceasedBooleanType().getValue() ? "true" : "false"));
		str.append(", ");
		str.append("-1 ");
	}

	//------------------------------------CREATING PATIENT-------------------------------------------------------------

	//Insert Patient to Bundle
	public Patient createRandomPatient() {

		IFhirResourceDao dao = patientResourceProvider.getDao();
		Patient patient = new Patient();
		//inserting random value for identifier
		List<Identifier> identifierList = new ArrayList<>();
		Identifier id = new Identifier();
		id.setValue(this.identifier());
		identifierList.add(id);
		patient.setIdentifier(identifierList);

		HumanName humanName = new HumanName();
		//set family
		humanName.setFamily(this.randomStringGenerator());

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
		Address address = new Address();
		address.addLine(this.completeAddress());
		List<Address> theAddress = new ArrayList<>();
		theAddress.add(address);
		patient.setAddress(theAddress);

		//inserting the activity status of patient
		patient.setActive(this.isActive());

		//inserting random value for deceased
		patient.setDeceased(new BooleanType(this.isDeceased()));

		//Marital status
		CodeableConcept codeableConcept = new CodeableConcept();
		codeableConcept.setTextElement(new StringType(this.randomStringGenerator()));
		patient.setMaritalStatus(codeableConcept);

		//Telecome
		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setValue(this.phoneNumber());
		List<ContactPoint> contactPointList = new ArrayList<>();
		contactPointList.add(contactPoint);
		patient.setTelecom(contactPointList);

		//multiple birth
		BooleanType multipleBirth= new BooleanType();
		patient.setMultipleBirth(multipleBirth.setValue(this.multipleBirth()));
		patient.setActive(this.isActive());

		//Inserting Practitioner
		List<Reference> referenceList = new ArrayList<>();
		referenceList.add(new Reference().setReference(this.randomStringGenerator()));
		patient.setGeneralPractitioner(referenceList);


		return patient;
	}
	//----------------------------------CREATING RANDOM DATA-------------------------------------------------------------
	//generate random identifier
	public String identifier(){
		return (this.randomStringGenerator());
	}
	//martial status
	public boolean multipleBirth(){
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
		return ""+ randomNumberOf_fixedLength(10, 9, 0);
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

		return "House No: " + this.houseNumber() + ", Street " + this.randomStringGenerator() + ", city: " +
			this.randomStringGenerator() + ", district:" + this.randomStringGenerator() + ", state:" + this.randomStringGenerator() +
			", postal code:" + " H9S 2Y3";
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
	public char gender() {
		return ((this.randomNumberGenerator(1, 0)) == 1) ? 'M' : 'F';

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
