package ca.uhn.fhir.jpa.starter.resource.provider;

import ca.uhn.fhir.rest.annotation.Operation;

//@configuration
public class HelloWorldPlainProvider {
	@Operation(name="$subtain", idempotent = true)
	public void closureOperation(){
		System.out.println("Hello subtain, from khadija");
	}
}
