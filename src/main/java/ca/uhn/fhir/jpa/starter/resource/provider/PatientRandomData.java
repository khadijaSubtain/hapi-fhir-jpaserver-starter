package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.AdministrativeGender;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Configuration
public class PatientRandomData {
	public static int count = 10;
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
		int i = 0;
		for ( ; i < count; i++) {
			if(i % 100 == 0) {
				System.out.println("Current Count: " + i);
			}
				randomPatient = this.createRandomPatient();
				dao.create(randomPatient);

		}
		theServletResponse.setContentType("text/plain");
		theServletResponse.getWriter().write(i+1 + " patients inserted");
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
		str.append( "COUNT, IDENTIFIER, ACTIVE, NAME, TELECOM, GENDER, BIRTH DATE, DECEASED, ADDRESS, " +
			"MARITAL STATUS, MULTIPLE BIRTH \n");

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
		str.append(", ");

		//identifier
		str.append(var.getIdentifier().get(0).getValue());
		str.append(",");

		//active
		str.append(var.getActive() ? "true" : "false");
		str.append(",");

		//family name
		str.append(var.getName().get(0).getFamily());
		str.append(" - ");
		//givenName
		str.append(var.getName().get(0).getGiven().get(0).getValue());
		str.append(", ");

		//telecom
		str.append(var.getTelecom().get(0).getValue());
		str.append(", ");

		//Gender
		str.append(var.getGender().toCode());
		str.append(", ");

		//Date of Birth
		str.append(var.getBirthDate().getYear() +"-" +	var.getBirthDate().getMonth() +
			"-"+ var.getBirthDate().getDate());
		str.append(", ");

		//deceased
		str.append((var.getDeceasedBooleanType().getValue() ? "true" : "false"));
		str.append(", ");

		//address
		str.append(var.getAddress().get(0).getLine().get(0).getValue());
		str.append(", ");

		//married
		str.append(var.getMaritalStatus().getTextElement());
		str.append(", ");

		//multipleBirth
		str.append((var.getMultipleBirthBooleanType().getValue() ? "true" : "false"));
		str.append(", ");

		//end of line
		str.append("-1 ");
		str.append("\n ");

	/*	str.append(", PRACTITIONER: ");
		//practitioner family name
		str.append(var.getGeneralPractitioner().get(0));
	 */

	}

	//------------------------------------CREATING PATIENT-------------------------------------------------------------

	//Insert Patient to Bundle
	public Patient createRandomPatient() {

		IFhirResourceDao dao = patientResourceProvider.getDao();
		Patient patient = new Patient();

		//inserting random value for identifier
		List<Identifier> identifierList = new ArrayList<>();
		identifierList.add(new Identifier().setValue(this.generateRandomIdentifier()));
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
		BooleanType multipleBirth= new BooleanType();
		patient.setMultipleBirth(multipleBirth.setValue(this.multipleBirth()));

		//Active
		patient.setActive(this.isActive());

		//Inserting Practitioner
		/*
		Reference reference = new Reference("http://fhir.hl7.org/svc/StructureDefinition/c8973a22-2b5b-4e76-9c66-00639c99e61b");
		reference.setType("http://fhir.hl7.org/svc/StructureDefinition/c8973a22-2b5b-4e76-9c66-00639c99e61b");


		List<Reference> referenceList = new ArrayList<>();
		referenceList.add(reference);
		patient.setGeneralPractitioner(referenceList);
		*/

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
	public String generateRandomIdentifier(){
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
		String gender="";
		switch (genderCode){
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
