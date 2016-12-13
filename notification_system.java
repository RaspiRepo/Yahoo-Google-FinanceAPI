/*----------------------------------------------------------------------------
COPYRIGHT (c) 2016, RaspiRepo,
Mounatin View, California, USA.

ALL RIGHTS RESERVED.
-----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------
    notification_system.java
                   : Class to handle and support all notification related 
                     features

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/




//package org.RaspiRepo.googlefinance;

import java.util.Properties;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;



public class notification_system
/*----------------------------------------------------------------------------
    notification_system
                   : Class to handle and support all notification related 
                     features.

    Written by     : RaspiRepo
    Date           : Aug 30, 2016
------------------------------------------------------------------------------*/
{
    Session session     = null;
    MimeMessage message = null;


    public notification_system ()
    /*----------------------------------------------------------------------------
        init_smtp      : Initilized email SMTP componenets

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------------*/
    {
        Properties props = System.getProperties();

        props.put("mail.smtp.starttls.enable", true); 
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");

        //setup session
        session = Session.getInstance(props, null);
        message = new MimeMessage(session);
        session.setDebug(false);
    }


    public void send_email (String from_addr,
                            String to_email,
                            String subject,
                            String msg_report)
    /*----------------------------------------------------------------------------
        init_smtp      : Initilized email SMTP componenets

        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------------*/
    {
        // Create the email addresses involved
        try {
           InternetAddress inet_from = new InternetAddress(from_addr);

            message.setSubject(subject);
            message.setFrom(inet_from);
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to_email));

            // Create a multi-part to combine the parts
            Multipart multipart = new MimeMultipart("alternative");

            // Create the html part
            BodyPart messageBodyPart = new MimeBodyPart();

            String htmlMessage = msg_report;
            messageBodyPart.setContent(htmlMessage, "text/html");

            // Add html part to multi part
            multipart.addBodyPart(messageBodyPart);

            // Associate multi-part with message
            message.setContent(multipart);

            // Send message
            Transport transport = session.getTransport();
            transport.connect(market_const.smtp_server, market_const.smtp_port,
                              market_const.username, market_const.password);

            transport.sendMessage(message, message.getAllRecipients());

        } catch (AddressException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessagingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void main (String[] args)
    /*------------------------------------------------------------------------
        main           : Test code to build Stock symbols from file
        
        Written by     : RaspiRepo
        Date           : Aug 30, 2016
    ------------------------------------------------------------------------*/
    {
        notification_system notify = new notification_system ();

        String from_email  = "from@gmail.com";
        String to          = "to@gmail.com";
        String subject     = "Alert from T101";
        String message     = "Box met high target of $14.40\nMon Sep 12 2016  13:35:15 ET";

        notify.send_email(from_email, to, subject, message);
    }
}