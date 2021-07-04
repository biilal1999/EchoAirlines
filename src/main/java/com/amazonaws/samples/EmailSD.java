/*package com.amazonaws.samples;

import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;


public class EmailSD {

    private String FROM = "";
    private String TO = "";
    private String CONFIGSET = "ConfigSet";
    private String SUBJECT = "";

    private String HTMLBODY = "<h1>EchoAirlines</h1><p>This email was sent to valid your ticket</p>";
    private String TEXTBODY = "This email was sent to valid your ticket";

    public EmailSD(String TO, String SUBJECT) {
        this.FROM = System.getenv("EMAIL_FROM");
        this.TO = TO;
        this.SUBJECT = SUBJECT;
    }

    public void enviarCorreo(){
        BasicAWSCredentials b = new BasicAWSCredentials("AKIA2PVBNRQQRUKMWDF7", "CtoS+cEX3bqKs4A8pP2+RzvVyNzuCPIE25Y/Rsz6");
        //AWSCredentialsProvider credentials = null;

        /*try{
            //credentials = (AWSCredentialsProvider) b;

        } catch (Exception e){
            throw new AmazonClientException("Cannot load the credentials");
        }*/

        /*try{

            AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(new AWSStaticCredentialsProvider(b));

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM)
                    .withReturnPath(FROM);
                    //.withConfigurationSetName(CONFIGSET);

            client.sendEmail(request);

        } catch (Exception ex){
            System.err.println("Tenemos un problema");
        }
    }

}*/
