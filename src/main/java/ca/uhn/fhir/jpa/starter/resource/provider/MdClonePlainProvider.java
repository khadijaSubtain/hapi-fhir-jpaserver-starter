package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleBuilder;

import org.apache.commons.compress.utils.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class MdClonePlainProvider {

	private final Logger logger = LoggerFactory.getLogger(MdClonePlainProvider.class);
	private FhirContext myFhirContext = FhirContext.forR4();

	// Create a client
	IGenericClient client = FhirContext.forR4().newRestfulGenericClient("http://localhost:8080/fhir");


		@Operation(name = "$use_case_1", idempotent = true)
		public Bundle bundleDifferentResources(@OperationParam(name = "family_name") StringType familyName) {

		logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + " called");
		/*
		// An Empty bundle is created to fill in the information of 2 resources patient and observation (extended op. functionality)
		*/
		Bundle resultBundle = new Bundle();

		// Read a Patient
		if(familyName != null) {
			logger.info(familyName.getValue()); // logs all the method calls

			Bundle resultsPatient = client
				.search()
				.forResource(Patient.class)
				.where(Patient.NAME.matches().value(familyName.getValue())) // retrieving familyname from key value pair where KEY: name = "family_name" and VALUE: StringType familyName
				.returnBundle(Bundle.class)
				.execute();
		/*
		/ retreiving the first entry in the for of bundle entry componenet cause bundle can only save BundleEntryComponent type
		*/
			Bundle.BundleEntryComponent patient = resultsPatient.getEntry().get(0);
			resultBundle.addEntry(patient); // adding BundleEntryComponent(Patient) into resultBundle
		}
		// Read a Observation
		Bundle resultsObservation = client
			.search()
			.forResource(Observation.class)
			.returnBundle(Bundle.class)
			.execute();

		/*
		after retrieving all the observation in the form of bundle we'll get the first entry and make a BundleEntryComponent
		and add that BundleEntryComponent called observation into resultBundle our bundle has all the needed etries
		we'll return the bundle
		 */
		Bundle.BundleEntryComponent observation = resultsObservation.getEntry().get(0);

		resultBundle.addEntry(observation);

		return resultBundle;


/*
		//	BundleBuilder builder = new BundleBuilder(myFhirContext);

		// Read a Patient
		Patient patient = client
			.read()
			.resource(Patient.class)
			.withId("103")
			.execute();

		// Read a Observation
		Observation observation = client
			.read()
			.resource(Observation.class)
			.withId("102")
			.execute();

		builder.addTransactionUpdateEntry(patient);

		return (Bundle) builder.getBundle();

*/

		/*
		// Read a Patient
		Bundle results = client
			.search()
			.forResource(Patient.class)
			.where(Patient.NAME.matches().value("Jameson"))
			.returnBundle(Bundle.class)
			.execute();
		return results;
		 */





	}
}
