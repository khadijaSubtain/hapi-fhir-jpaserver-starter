package ca.uhn.fhir.jpa.starter.resource.provider;

import org.hl7.fhir.r4.model.*;

public class PatientTranslatorService {

	private static final String IDENTIFIER_USE = "identifier.use";
	private static final String IDENTIFIER_TYPE = "identifier.type";
	private static final String IDENTIFIER_SYSTEM = "identifier.system";
	private static final String IDENTIFIER_VALUE = "identifier.value";

	private static final String NAME_USE = "name.use";
	private static final String NAME_FAMILY = "name.family";
	private static final String NAME_GIVEN = "name.given";

	private static final String TELECOM_SYSTEM = "telecom.system";
	private static final String TELECOM_VALUE = "telecom.value";
	private static final String TELECOM_USE = "telecom.use";

	private static final String GENDER = "gender";

	private static final String BIRTH_DATE = "birthDate";

	private static final String DECEASED = "deceased";

	private static final String ADDRESS_LINE = "address.line";
	private static final String ADDRESS_CITY = "address.city";
	private static final String ADDRESS_STATE = "address.state";
	private static final String ADDRESS_POSTAL_CODE = "address.postalCode";
	private static final String ADDRESS_COUNTRY = "address.country";

	private static final String COMMUNICATION_LANGUAGE_CODE = "communication.language.code";
	private static final String COMMUNICATION_LANGUAGE_TEXT = "communication.language.text";

	protected static void transformPatientToCSV(Patient patient) {

		StringBuilder str = new StringBuilder();

		str.append(",");

		//identifier
		//TODO: Multiple values, which to use?
		for(Identifier identifier : patient.getIdentifier())
		{
			//identifier.use
			str.append(identifier.getUse().toString());
			str.append(",");

			//identifier.type
			str.append(identifier.getType().toString());
			str.append(",");

			//identifier.system
			str.append(identifier.getSystem());
			str.append(",");

			//identifier.value
			str.append(identifier.getValue());
			str.append(",");
		}

		//name
		for(HumanName name : patient.getName())
		{
			//use
			str.append(name.getUse().toString());
			str.append(",");

			//family
			str.append(name.getFamily());
			str.append(",");

			//given
			for(StringType givenNames : name.getGiven()){
				givenNames.getValue();
				str.append("; ");
			}
		}

		//telecom
		//TODO: Multiple values, which/how to use?
		for(ContactPoint contactPoint : patient.getTelecom()){

			//system
			str.append(contactPoint.getSystem().toString());
			str.append(",");

			//value
			str.append(contactPoint.getValue());
			str.append(",");

			//use
			str.append(contactPoint.getUse().toString());
			str.append(",");
		}
		

		//Gender
		if (patient != null && patient.getGender() != null) {
			str.append(patient.getGender().toCode());
		}
		str.append(",");

		//Date of Birth
		if (patient != null && patient.getBirthDate() != null) {
			str.append(patient.getBirthDate().getYear() + "-" + patient.getBirthDate().getMonth() +
				"-" + patient.getBirthDate().getDate());
		}
		str.append(",");

		//deceased
		if (patient != null && patient.getDeceasedBooleanType() != null) {
			//	str.append((var.getDeceasedBooleanType().getValue() ? "true" : "false"));
		}
		str.append(",");

		//address
		if (patient.getAddress().size() > 0 && patient.getAddress() != null) {
			str.append(patient.getAddress().get(0).getLine().get(0).getValue());
		}
		str.append(",");

		//married
		if (patient != null && patient.getMaritalStatus() != null) {
			str.append(patient.getMaritalStatus().getTextElement());
		}
		str.append(",");

		//multipleBirth
		if (patient != null && patient.getMultipleBirthBooleanType() != null) {
			str.append((patient.getMultipleBirthBooleanType().getValue() ? "true" : "false"));
		}
		str.append(",");

		//practitioner family name
		if (patient != null && patient.getGeneralPractitioner().size() > 0 && patient.getGeneralPractitioner() != null) {
			str.append(patient.getGeneralPractitioner().get(0).getReference());
		}
		str.append(",");

		//Observation
		if (patient != null && patient.getManagingOrganization() != null) {
			str.append(patient.getManagingOrganization().getReference());
		}
		str.append(",");

		//end of line
		str.append(" -1 ");
		str.append("\n ");

	}

	protected String createPatientCsvHeaderRow(){
		StringBuilder str = new StringBuilder();

		str.append(IDENTIFIER_USE);
		str.append(",");
		str.append(IDENTIFIER_TYPE);
		str.append(",");
		str.append(IDENTIFIER_SYSTEM);
		str.append(",");
		str.append(IDENTIFIER_VALUE);
		str.append(",");

		str.append(NAME_USE);
		str.append(",");
		str.append(NAME_FAMILY);
		str.append(",");
		str.append(NAME_GIVEN);
		str.append(",");

		str.append(TELECOM_SYSTEM);
		str.append(",");
		str.append(TELECOM_VALUE);
		str.append(",");
		str.append(TELECOM_USE);
		str.append(",");

		str.append(GENDER);
		str.append(",");

		str.append(BIRTH_DATE);
		str.append(",");

		str.append(DECEASED);
		str.append(",");

		str.append(ADDRESS_LINE);
		str.append(",");
		str.append(ADDRESS_CITY);
		str.append(",");
		str.append(ADDRESS_STATE);
		str.append(",");
		str.append(ADDRESS_POSTAL_CODE);
		str.append(",");
		str.append(ADDRESS_COUNTRY);
		str.append(",");

		str.append(COMMUNICATION_LANGUAGE_CODE);
		str.append(",");
		str.append(COMMUNICATION_LANGUAGE_TEXT);
		str.append(",");

		return str.toString();
	}
}
