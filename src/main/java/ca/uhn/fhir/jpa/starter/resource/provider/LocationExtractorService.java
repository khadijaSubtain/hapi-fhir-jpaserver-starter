package ca.uhn.fhir.jpa.starter.resource.provider;

import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Practitioner;

public class LocationExtractorService {
	//getter to retrieve information of Location

	public static int extractCount = 0;

	public static String locationToCSV(Location location) {
		StringBuilder str = new StringBuilder();
		str.append(++extractCount);
		str.append(",");

		//identifier
		str.append((location.getIdentifier() != null && location.getIdentifier().size() > 0) ? location.getIdentifier().get(0) : null);
		str.append(",");

		//status
		str.append((location.getStatus() != null)? location.getStatus() : null);
		str.append(",");

		//operationalStatus
		str.append((location.getOperationalStatus() != null)? location.getOperationalStatus() : null);
		str.append(",");





		return str.toString();
	}
}
