package org.apache.camel.example.reportincident;

public class FilenameGenerator {
	public String generateFilename(InputReportIncident input) {
        // compute the filename
        return "incident-" + input.getIncidentId() + ".txt";
    }
}
