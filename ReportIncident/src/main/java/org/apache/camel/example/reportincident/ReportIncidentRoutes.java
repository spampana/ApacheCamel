package org.apache.camel.example.reportincident;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.language.bean.BeanLanguage;

public class ReportIncidentRoutes extends RouteBuilder {
	 
    public void configure() throws Exception {
    	// first part from the webservice -> file backup
        from("direct:start")
        	//.setHeader(FileComponent.HEADER_FILE_NAME, BeanLanguage.bean(FilenameGenerator.class, "generateFilename"))
             .to("velocity:MailBody.vm")
             .to("file://target/subfolder");
 
        // second part from the file backup -> send email
        	from("file://target/subfolder")
            // set the subject of the email
            .setHeader("subject", constant("New incident reported"))
            // send the email
            .to("smtps://smtp.gmail.com:465?username=satya.gooogle@gmail.com&password=satyagoogle&to=SP00119063@techmahindra.com");
    }
             
    
 
}