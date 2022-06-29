package ca.uhn.fhir.jpa.starter.resource.provider;


import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.Operation;
import org.hl7.fhir.r4.model.Basic;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class CustomResource extends Basic {

	@Child(
		name = "csvText",
		type = {StringType.class},
		order = 0,
		min = 0,
		max = -1,
		modifier = true,
		summary = true
	)
	@Description(
		shortDefinition = "Business identifier",
		formalDefinition = "Identifier assigned to the resource for business purposes, outside the context of FHIR."
	)
	protected StringType csvText;

}