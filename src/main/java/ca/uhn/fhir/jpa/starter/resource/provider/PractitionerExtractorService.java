package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.rp.r4.PractitionerResourceProvider;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.StringType;

public class PractitionerExtractorService {

	public static int extractCount = 0;
	//private static StringBuilder str = new StringBuilder();

	//Practitioner object
	PractitionerResourceProvider practitionerResourceProvider;

	//Getters to retrieve Practitioner information

	//	str.append("COUNT, IDENTIFIER, ACTIVE, NAME, TELECOM, GENDER, BIRTH DATE, DECEASED, ADDRESS, " +
	//		"MARITAL STATUS, MULTIPLE BIRTH, PRACTITIONER, ORGANIZATION, EOL\n");
	//Convert the data into CSV
	public static String practitionerToCSV(Practitioner practitioner) {
		StringBuilder str = new StringBuilder();
		//count of Practitioner
		str.append(++extractCount);
		str.append(",");
		//identifier
		str.append((practitioner.getIdentifier() != null && practitioner.getIdentifier().size() > 0) ? practitioner.getIdentifier().get(0) : null);
		str.append(",");
		// activity status
		str.append(practitioner.getActive());
		str.append(",");
		// practitioner name
		str.append((practitioner.getName() != null) ? practitioner.getName().get(0) : null);
		str.append(",");
		//telecom list
		str.append((practitioner.getTelecom() != null && practitioner.getTelecom().size() > 0) ? practitioner.getTelecom().get(0) : null);
		str.append(",");

		//Deceased status- to add

		// list of address
		str.append((practitioner.getAddress().size() > 0 && practitioner.getAddress() != null) ? practitioner.getAddress().get(0) : null);
		str.append(",");
		//gender
		str.append((practitioner.getGender() != null) ? practitioner.getGender() : null);
		str.append(",");
		//date of birth
		str.append((practitioner.getBirthDate() != null) ? practitioner.getBirthDate() : null);
		str.append(",");
		//list of communication
		str.append((practitioner.getCommunication() != null) ? practitioner.getCommunication().get(0) : null);
		str.append(",");

		str.append(" -1 ");
		str.append("\n ");
		return str.toString();
	}
}
