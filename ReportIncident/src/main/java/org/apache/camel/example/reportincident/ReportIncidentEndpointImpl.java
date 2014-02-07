package org.apache.camel.example.reportincident;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.component.log.LogComponent;
import org.apache.camel.impl.DefaultCamelContext;
 
/**
 * The webservice we have implemented.
 */

public class ReportIncidentEndpointImpl implements ReportIncidentEndpoint {
 
	private CamelContext  camel;
	private ProducerTemplate template;
	
	//Constructor to Initialize the came context
	public ReportIncidentEndpointImpl() throws Exception {
        // create the camel context that is the "heart" of Camel
        camel = new DefaultCamelContext();
 
        // get the ProducerTemplate thst is a Spring'ish xxxTemplate based producer for very
        // easy sending exchanges to Camel.
        template = camel.createProducerTemplate();
        
     // append the routes to the context
        camel.addRoutes(new ReportIncidentRoutes());
        
        // add the event driven consumer that will listen for mail files and process them
        //addMailSendConsumer();
 
        // start Camel
        camel.start();
    }
	
	//Implementation in low level,how to add components like file and log and logging the logs in them
    public OutputReportIncident reportIncident_general_style_lowlevelCoding(InputReportIncident parameters)  {
        System.out.println("Hello ReportIncidentEndpointImpl is called from " + parameters.getGivenName());
        String name = parameters.getGivenName() + " " + parameters.getFamilyName();
        
        try{
        camel = new DefaultCamelContext();
        // add the log component
        camel.addComponent("log", new LogComponent());
        // add the file component
        camel.addComponent("file", new FileComponent());
        // start Camel
        camel.start();
        // let Camel do something with the name
        sendToCamel(name);
        sendToCamelFile(parameters.getIncidentId(), name);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        OutputReportIncident out = new OutputReportIncident();
        out.setCode("OK");
        return out;
    }
    private void sendToCamel(String name){
    	   try {
    	        // get the log component
    	        Component component = camel.getComponent("log");
    	 
    	        // create an endpoint and configure it.
    	        // Notice the URI parameters this is a common pratice in Camel to configure
    	        // endpoints based on URI.
    	        // com.mycompany.part2 = the log category used. Will log at INFO level as default
    	        Endpoint endpoint = component.createEndpoint("log:com.mycompany.part2");
    	 
    	        // create an Exchange that we want to send to the endpoint
    	        Exchange exchange = endpoint.createExchange();
    	        // set the in message payload (=body) with the name parameter
    	        exchange.getIn().setBody(name);
    	 
    	        // now we want to send the exchange to this endpoint and we then need a producer
    	        // for this, so we create and start the producer.
    	        Producer producer = endpoint.createProducer();
    	        producer.start();
    	        // process the exchange will send the exchange to the log component, that will process
    	        // the exchange and yes log the payload
    	        producer.process(exchange);
    	 
    	        // stop the producer, we want to be nice and cleanup
    	        producer.stop();
    	 
    	 
    	 
    	 
    	    } catch (Exception e) {
    	        // we ignore any exceptions and just rethrow as runtime
    	        throw new RuntimeException(e);
    	 
    	    }
    	
    	
    }
    
    private void sendToCamelFile(String incidentId, String name) {
        try {
        	            
            // get the file component
            Component component = camel.getComponent("file");
     
            // create an endpoint and configure it.
            // Notice the URI parameters this is a common pratice in Camel to configure
            // endpoints based on URI.
            // file://target instructs the base folder to output the files. We put in the target folder
            // then its actumatically cleaned by mvn clean
            Endpoint endpoint = component.createEndpoint("file://target");
            
            
            /*
             *In the file example above the configuration was URI based. 
             What if you want 100% java setter based style, well this is of course also possible. 
             We just need to cast to the component specific endpoint and then we have all the setters available:
            // create the file endpoint, we cast to FileEndpoint because then we can do
			// 100% java settter based configuration instead of the URI sting based
			// must pass in an empty string, or part of the URI configuration if wanted 
			FileEndpoint endpoint = (FileEndpoint)component.createEndpoint("");
			endpoint.setFile(new File("target/subfolder"));
			endpoint.setAutoCreate(true);
             */
            
            
     
            // create an Exchange that we want to send to the endpoint
            Exchange exchange = endpoint.createExchange();
            // set the in message payload (=body) with the name parameter
            exchange.getIn().setBody(name);
     
            // now a special header is set to instruct the file component what the output filename
            // should be
            exchange.getIn().setHeader(FileComponent.HEADER_FILE_NAME, "incident-" + incidentId + ".txt");
     
            // now we want to send the exchange to this endpoint and we then need a producer
            // for this, so we create and start the producer.
            Producer producer = endpoint.createProducer();
            producer.start();
            // process the exchange will send the exchange to the file component, that will process
            // the exchange and yes write the payload to the given filename
            producer.process(exchange);
     
            // stop the producer, we want to be nice and cleanup
            producer.stop();
        } catch (Exception e) {
            // we ignore any exceptions and just rethrow as runtime
            throw new RuntimeException(e);
        }
    }
    
    private void generateEmailBodyAndStoreAsFile(InputReportIncident parameters) {
        // generate the mail body using velocity template
        // notice that we just pass in our POJO (= InputReportIncident) that we
        // got from Apache CXF to Velocity.
        Object response = template.sendBody("velocity:MailBody.vm", parameters);
        // Note: the response is a String and can be cast to String if needed
     
        // store the mail in a file
        String filename = "mail-incident-" + parameters.getIncidentId() + ".txt";
        template.sendBodyAndHeader("file://target/subfolder", response, FileComponent.HEADER_FILE_NAME, filename);
    }
	
    //Once received request from web service,preparing the mail body using velocity,saving in a file at destination folder.
    //Configured consumer for looking the folder and send the mail if any file exist in the destination folder
    public OutputReportIncident reportIncident(InputReportIncident parameters) {
		
    	/* Without using the routers*/
    	
    	// transform the request into a mail body
        //Object mailBody = template.sendBody("velocity:MailBody.vm", parameters);
        // store the mail body in a file
        //String filename = "mail-incident-" + parameters.getIncidentId() + ".txt";
        //template.sendBodyAndHeader("file://test/subfolder", mailBody, FileComponent.HEADER_FILE_NAME, filename);
 
        
        /*With using routers*/
        //Object mailBody1= camel.createProducerTemplate().sendBodyAndHeader("direct:start", parameters,FileComponent.HEADER_FILE_NAME,"incident.txt");
        Object mailBody1= camel.createProducerTemplate().sendBodyAndHeader("direct:start", parameters,FileComponent.HEADER_FILE_NAME,"incident.txt");
        System.out.println("Body:" + mailBody1);
        
        
        
        // return an OK reply
        OutputReportIncident out = new OutputReportIncident();
        out.setCode("OK");
        return out;
	}
    private void addMailSendConsumer() throws Exception {
        // Grab the endpoint where we should consume. Option - the first poll starts after 2 seconds
        Endpoint endpint = camel.getEndpoint("file://test/subfolder?consumer.initialDelay=2000");
     
        // create the event driven consumer
        // the Processor is the code what should happen when there is an event
        // (think it as the onMessage method)
        Consumer consumer = endpint.createConsumer(new Processor() {
            public void process(Exchange exchange) throws Exception {
                // get the mail body as a String
                String mailBody = exchange.getIn().getBody(String.class);
     
                // okay now we are read to send it as an email
                System.out.println("Sending email..." + mailBody);
                 sendEmail(mailBody);
                System.out.println("Email sent");
            }
        });
     
        // star the consumer, it will listen for files
        consumer.start();
    }
    
    private void sendEmail(String body) {
        // send the email to your mail server
        String url = "smtps://smtp.gmail.com:465?username=satya.gooogle@gmail.com&password=satyagoogle&to=SP00119063@techmahindra.com";
        template.sendBodyAndHeader(url, body, "subject", "New incident reported");
    }
    
    // Using the Routers for the above concepts
}