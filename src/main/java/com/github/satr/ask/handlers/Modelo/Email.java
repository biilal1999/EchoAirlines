package com.github.satr.ask.handlers.Modelo;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.SQLException;
import java.util.Properties;


public class Email {

    private static final String FROM = System.getenv("EMAIL_FROM");    // ENV
    private static final String FROMNAME = System.getenv("EMAIL_FROM_NAME");    // ENV
    private String TO = "";     // PARAM
    private static final String SMTP_USERNAME = System.getenv("EMAIL_USERNAME");    // ENV
    private static final String SMTP_PASSWORD = System.getenv("EMAIL_PASSWORD");    // ENV
    //private final String CONFIGSET = "ConfigSet";
    private static final String HOST = System.getenv("EMAIL_HOST");      // ENV
    private static final int PORT = Integer.parseInt(System.getenv("EMAIL_PORT"));       // ENV
    private String SUBJECT = "";        // PARAM

    public Email(String TO, String SUBJECT) {
        this.TO = TO;
        this.SUBJECT = SUBJECT;
    }

    public void enviarConfirmacionReserva(String codigo) throws Exception{
        String body = String.join(System.getProperty("line.separator"), "<h2>EL CODIGO DE " + this.SUBJECT +
                " DE LA RESERVA ES " +
                "<span>" + codigo + "</span></h2>");
        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
        msg.setSubject(SUBJECT);
        msg.setContent(body,"text/html");

        // Add a configuration set header. Comment or delete the
        // next line if you are not using a configuration set
        //msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

        // Create a transport.
        Transport transport = session.getTransport();

        // Send the message.
        try
        {
            System.out.println("Sending...");

            // Connect to Amazon SES using the SMTP username and password you specified above.

            try{
                transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            } catch (Exception ex){
                System.err.println("Tenemos un problema con el acceso");
            }

            // Send the email.

            try{
                transport.sendMessage(msg, msg.getAllRecipients());

            } catch (Exception ex){
                System.err.println("Tenemos un problema con el envío");
            }

            System.out.println("Email sent!");
        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        finally
        {
            // Close and terminate the connection.
            transport.close();
        }
    }


}
