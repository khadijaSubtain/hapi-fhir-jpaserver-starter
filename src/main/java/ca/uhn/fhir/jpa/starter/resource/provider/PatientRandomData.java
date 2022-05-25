package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.rp.r4.PatientResourceProvider;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;

import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class PatientRandomData {
	@Autowired
	PatientResourceProvider patientResourceProvider;
	public static int count=10;
	// Create a client
	IGenericClient client = FhirContext.forR4().newRestfulGenericClient("http://localhost:8080/fhir");
	public static void main(String[] args) {

		PatientRandomData patient = new PatientRandomData();

		Patient randomPatient ;
		for (int i = 0; i < count; i++) {
			randomPatient = patient.createRandomPatient();

		}
	}



	//Insert Patient to Bundle
	public Patient createRandomPatient(){

		IFhirResourceDao dao = patientResourceProvider.getDao();
		Patient patient = new Patient();

		HumanName humanName=new HumanName();

		//set family
		humanName.setFamily(this.randomStringGenerator());

		//set given
		List<StringType> givenHumanNameList= new ArrayList<StringType>();
		givenHumanNameList.add(new StringType(this.randomStringGenerator()));
		humanName.setGiven(givenHumanNameList);

		//set patient name
		List<HumanName> humanNameList= new ArrayList<HumanName>();
		humanNameList.add(humanName);
		patient.setName(humanNameList);

		Date date = new Date();
		//date.setValue(this.dateOfBirth());

		patient.setBirthDate(date);
		//patient.setBirthDate(new DateType(date.setValue(this.dateOfBirth())));

		return patient;
	}

	//generate random data for Birthdate
	public String dateOfBirth() {
		GregorianCalendar calander = new GregorianCalendar();
		int year = rangeOfYear(1950, 2020);
		calander.set(calander.YEAR, year);
		int dayOfYear = rangeOfYear(1, calander.getActualMaximum(calander.DAY_OF_YEAR));
		calander.set(calander.DAY_OF_YEAR, dayOfYear);
		return calander.get(calander.YEAR) + "-" + (calander.get(calander.MONTH) + 1) + "-" + calander.get(calander.DAY_OF_MONTH);
	}

	public int rangeOfYear(int start, int end) {
		return start + (int) Math.round(Math.random() * (end - start));
	}

	//generate random phone number
	public long phoneNumber() {
		long phoneNumber = 0;
		return randomNumberOf_fixedLength(10, 9, 0);
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
	public String completeAddress() {

		return "House No: " + this.houseNumber() + ", Street " + this.randomStringGenerator() + ", city: " +
			this.randomStringGenerator() + ", district:" + this.randomStringGenerator() + ", state:" + this.randomStringGenerator() +
			", postal code:" + " H9S 2Y3"
			;
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
