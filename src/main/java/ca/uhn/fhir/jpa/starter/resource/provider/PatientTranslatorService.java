package ca.uhn.fhir.jpa.starter.resource.provider;

import org.hl7.fhir.r4.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PatientTranslatorService {

	private static final String WORD_SEPARATOR = ",";

	private static final String SUB_WORD_SEPARATOR = "-";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String NULL = "NULL";
	private static final String END_OF_LINE = "-1";
	private static final String NEXT_LINE = "\n";

	//TODO: Confirm with MDClone about date format
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

	protected static String transformPatientToCSV(Patient patient) {

		StringBuilder str = new StringBuilder();

		//identifier
		//TODO: Multiple values, which to use?
		for (Identifier identifier : patient.getIdentifier()) {
			//identifier.use
			appendCsv(str, identifier.getUse() == null ? NULL : identifier.getUse().toString());

			//identifier.type
			appendCsv(str, identifier.getType().getText());

			//identifier.system
			appendCsv(str, getValueOrDefault(identifier.getSystem()));

			//identifier.value
			appendCsv(str, getValueOrDefault(identifier.getValue()));
		}

		//name
		//TODO: Multiple Names, how to print to csv
		if(patient.getName().size() == 0){
			//use
			appendCsv(str, NULL);
			//family
			appendCsv(str, NULL);
			//given
			appendCsv(str, NULL);
		}
		for (HumanName name : patient.getName()) {
			//use
			appendCsv(str, name.getUse() == null ? NULL : name.getUse().toString());

			//family
			appendCsv(str, getValueOrDefault(name.getFamily()));

			//given
			appendCsv(str, appendWithChar(name.getGiven(), '-'));
		}

		//telecom
		//TODO: Multiple values, which/how to use?
		if(patient.getTelecom().size() == 0){
			//system
			appendCsv(str, NULL);
			//value
			appendCsv(str, NULL);
			//use
			appendCsv(str, NULL);
		}
		else {
			for (ContactPoint contactPoint : patient.getTelecom()) {
				//system
				appendCsv(str, contactPoint.getSystem() == null ? NULL : contactPoint.getSystem().toString());

				//value
				appendCsv(str, getValueOrDefault(contactPoint.getValue()));

				//use
				appendCsv(str, contactPoint.getUse() == null ? NULL : contactPoint.getUse().toString());
			}
		}

		//Gender
		appendCsv(str, patient.getGender() == null ? NULL : patient.getGender().toCode());

		//Date of Birth
		Date birthDate = patient.getBirthDate();
		appendCsv(str, birthDate == null ? NULL : dateFormat.format(birthDate));

		//deceased
		appendCsv(str,
			patient.getDeceased() == null ? NULL : patient.getDeceasedBooleanType().getValue() ? TRUE : FALSE);

		//address
		//TODO: Multiple values returned. What format

		if(patient.getAddress().size() == 0){
			//line
			appendCsv(str, NULL);
			//city
			appendCsv(str, NULL);
			//state
			appendCsv(str, NULL);
			//postalCode
			appendCsv(str, NULL);
			//country
			appendCsv(str, NULL);
		}
		else {
			for (Address address : patient.getAddress()) {
				//line
				appendCsv(str, appendWithChar(address.getLine(), ' '));

				//city
				appendCsv(str, getValueOrDefault(address.getCity()));

				//state
				appendCsv(str, getValueOrDefault(address.getState()));

				//postalCode
				appendCsv(str, getValueOrDefault(address.getPostalCode()));

				//country
				appendCsv(str, getValueOrDefault(address.getCity()));
			}
		}

		//communication
		if(patient.getCommunication().size() == 0)
		{
			//code
			appendCsv(str, NULL);
			//text
			appendCsv(str, NULL);
		}
		else {
			for(Patient.PatientCommunicationComponent component : patient.getCommunication()){

				//language.code
				for(Coding coding : component.getLanguage().getCoding()){
					str.append(coding.toString()).append(SUB_WORD_SEPARATOR);
					str.append(WORD_SEPARATOR);
				}

				//language.text
				appendCsv(str, component.getLanguage().getText());
			}
		}

		//end of line tag
		str.append(END_OF_LINE);

		//nextLine
		str.append(NEXT_LINE);

		return str.toString();
	}

	protected static String createPatientCsvHeaderRow() {
		StringBuilder str = new StringBuilder();

		str.append(IDENTIFIER_USE).append(WORD_SEPARATOR)
			.append(IDENTIFIER_TYPE).append(WORD_SEPARATOR)
			.append(IDENTIFIER_SYSTEM).append(WORD_SEPARATOR)
			.append(IDENTIFIER_VALUE).append(WORD_SEPARATOR)

			.append(NAME_USE).append(WORD_SEPARATOR)

			.append(NAME_FAMILY).append(WORD_SEPARATOR)

			.append(NAME_GIVEN).append(WORD_SEPARATOR)

			.append(TELECOM_SYSTEM).append(WORD_SEPARATOR)
			.append(TELECOM_VALUE).append(WORD_SEPARATOR)
			.append(TELECOM_USE).append(WORD_SEPARATOR)

			.append(GENDER).append(WORD_SEPARATOR)

			.append(BIRTH_DATE).append(WORD_SEPARATOR)

			.append(DECEASED).append(WORD_SEPARATOR)

			.append(ADDRESS_LINE).append(WORD_SEPARATOR)
			.append(ADDRESS_CITY).append(WORD_SEPARATOR)
			.append(ADDRESS_STATE).append(WORD_SEPARATOR)
			.append(ADDRESS_POSTAL_CODE).append(WORD_SEPARATOR)
			.append(ADDRESS_COUNTRY).append(WORD_SEPARATOR)

			.append(COMMUNICATION_LANGUAGE_CODE).append(WORD_SEPARATOR)
			.append(COMMUNICATION_LANGUAGE_TEXT).append(WORD_SEPARATOR);

		return str.toString();

		/*
		return
			IDENTIFIER_USE + WORD_SEPARATOR +
			IDENTIFIER_TYPE + WORD_SEPARATOR +
			IDENTIFIER_SYSTEM + WORD_SEPARATOR +
			IDENTIFIER_VALUE + WORD_SEPARATOR +

			NAME_USE + WORD_SEPARATOR +

			NAME_FAMILY + WORD_SEPARATOR +

			NAME_GIVEN + WORD_SEPARATOR +

			TELECOM_SYSTEM + WORD_SEPARATOR +
			TELECOM_VALUE + WORD_SEPARATOR +
			TELECOM_USE + WORD_SEPARATOR +

			GENDER + WORD_SEPARATOR +

			BIRTH_DATE + WORD_SEPARATOR +

			DECEASED + WORD_SEPARATOR +

			ADDRESS_LINE + WORD_SEPARATOR +
			ADDRESS_CITY + WORD_SEPARATOR +
			ADDRESS_STATE + WORD_SEPARATOR +
			ADDRESS_POSTAL_CODE + WORD_SEPARATOR +
			ADDRESS_COUNTRY + WORD_SEPARATOR +

			COMMUNICATION_LANGUAGE_CODE + WORD_SEPARATOR +
			COMMUNICATION_LANGUAGE_TEXT + WORD_SEPARATOR;
			*/
	}

	private static void appendCsv(StringBuilder str, String value) {
		str.append(value).append(WORD_SEPARATOR);
	}

	private static String appendWithChar(List<StringType> values, char characterSeparator) {
		StringBuilder str = new StringBuilder();
		for (StringType value : values) {
			str.append(value.getValue()).append(characterSeparator);
		}
		return str.toString();
	}

	private static <T> T getValueOrDefault(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}

	private static String getValueOrDefault(String value) {
		return value == null ? NULL : value;
	}

	private static void returnJasonString(){
		JsonParser jsonParser = new JsonParser();
		String jsonString = "{'test1':'value1','test2':{'id':0,'name':'testName'}}";
		JsonObject jsonObject = (JsonObject) jsonParser.parse(jsonString);
	}
}
