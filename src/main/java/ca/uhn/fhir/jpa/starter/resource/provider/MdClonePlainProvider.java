package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleBuilder;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MdClonePlainProvider {

	private final Logger logger = LoggerFactory.getLogger(MdClonePlainProvider.class);
	private FhirContext myFhirContext = FhirContext.forR4();

	// Create a client
	IGenericClient client = FhirContext.forR4().newRestfulGenericClient("http://localhost:8080/fhir");

	@Operation(name = "$use_case_1", idempotent = true)
	public Bundle bundleDifferentResources(@OperationParam(name = "family_name") StringType familyName) {

		logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + " called");


	/*	BundleBuilder builder = new BundleBuilder(myFhirContext);

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

		Bundle resultBundle = new Bundle();

		// Read a Patient
		if(familyName != null) {

			logger.info(familyName.getValue());
			Bundle resultsPatient = client
				.search()
				.forResource(Patient.class)
				.where(Patient.NAME.matches().value(familyName.getValue()))
				.returnBundle(Bundle.class)
				.execute();

			Bundle.BundleEntryComponent patient = resultsPatient.getEntry().get(0);

			resultBundle.addEntry(patient);
		}

		// Read a Observation
		Bundle resultsObservation = client
			.search()
			.forResource(Observation.class)
			.returnBundle(Bundle.class)
			.execute();

		Bundle.BundleEntryComponent observation = resultsObservation.getEntry().get(0);

		resultBundle.addEntry(observation);

		return resultBundle;
	}
}
