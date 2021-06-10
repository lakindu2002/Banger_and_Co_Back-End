package com.lakindu.bangerandcobackend.util.mailsender;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

@Component
public class MailSender {
    @Value("${custom.mail.email}")
    private String cooperateEmailAddress;

    @Value("${custom.mail.password}")
    private String cooperatePassword;

    @Value("${custom.mail.port}")
    private String smtpPortNumber;

    @Value("${custom.mail.auth.enable}")
    private String enableAuth;

    @Value("${custom.mail.ssl.enable}")
    private String enableSSL;

    @Value("${custom.mail.server-name}")
    private String serverName;

    @Value("${custom.mail.mail-type}")
    private String mailType;

    private Session theMailSession;
    private HashMap<String, String> dynamicData;

    @PostConstruct
    public void init() {
        dynamicData = new HashMap<>(); //create a HashMap to apply handle bar values

        //configure mail properties
        Properties propertiesConfig = new Properties();

        propertiesConfig.put("mail.smtp.host", serverName); //denote host name for smtp server
        propertiesConfig.put("mail.smtp.port", smtpPortNumber); //provide smtp port number
        propertiesConfig.put("mail.smtp.auth", enableAuth); //denote auth requirement
        propertiesConfig.put("mail.smtp.ssl.enable", enableSSL); //denote usage of SSL to transmit data

        //create an authenticator to authenticate with SMTP Server.
        Authenticator theAuthenticator = new Authenticator() {
            //create an authenticator to authenticate with SMTP Server.
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cooperateEmailAddress, cooperatePassword);
            }
        };

        // create a session with the provided properties in authenticated state.
        theMailSession = Session.getInstance(propertiesConfig, theAuthenticator);
        theMailSession.setDebug(false);
    }

    public void sendMail(MailSenderHelper theHelper) {
        Message theMessage = new MimeMessage(theMailSession); //create a MimeMessage to send via Email
        try {
            //format a template according to the mail sending type.
            String contentToEmail = setTemplate(theHelper.getTemplateName(), theHelper); //retrieve formatted template

            theMessage.setSentDate(new Date()); //set current date is sent date
            theMessage.setFrom(new InternetAddress(cooperateEmailAddress)); //set the sender
            theMessage.setSubject(theHelper.getSubject()); //set the Subject for the Mail

            //set receiver
            if (theHelper.getTemplateName() == MailTemplateType.INQUIRY_REPLY) {
                //if email is an inquiry reply.
                theMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(theHelper.getTheInquiry().getEmailAddress()));
            } else {
                //if normal email
                theMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(theHelper.getUserToBeInformed().getEmailAddress()));
            }
            theMessage.setFlag(Flags.Flag.FLAGGED, true); //mark the email as an important email
            theMessage.setContent(contentToEmail, mailType); //attach the body with mail type - html

            Transport.send(theMessage); //send the mail via gmail to the user
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private String setTemplate(MailTemplateType theType, MailSenderHelper theHelper) throws IOException {
        TemplateLoader configurer = new ClassPathTemplateLoader(); //load templates from classpath
        configurer.setPrefix("/templates"); //template has "/templates" path as prefix
        configurer.setSuffix(".html"); //templates are of html

        Handlebars handlebars = new Handlebars(configurer); //create a Handlebars class with current loader used to load templates

        switch (theType) {
            case SIGNUP: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());

                Template theTemplate = handlebars.compile("SignUpMail"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case UPDATEACCOUNT: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());

                Template theTemplate = handlebars.compile("AccountUpdate"); //retrieve the template based on required type
                dynamicData.put("updatedTime", new Date().toString());
                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller

                dynamicData.clear(); //clear hashmap contents after formatting template

                return formattedTemplate;
            }
            case INQUIRY_REPLY: {
                //construct the reply data.
                dynamicData.put("firstName", theHelper.getTheInquiry().getFirstName());
                dynamicData.put("lastName", theHelper.getTheInquiry().getLastName());
                dynamicData.put("yourSubject", theHelper.getTheInquiry().getInquirySubject());
                dynamicData.put("yourInquiry", theHelper.getTheInquiry().getMessage());
                dynamicData.put("ourReply", theHelper.getInquiryReply());
                dynamicData.put("submittedDate", new java.sql.Date(theHelper.getTheInquiry().getCreatedAt().getTime()).toString());

                Template theTemplate = handlebars.compile("InquiryReply"); //retrieve the template based on required type
                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller

                dynamicData.clear(); //clear hashmap contents after formatting template

                return formattedTemplate;
            }
            case WHITELIST: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("whitelistedDate", new Date().toString());

                Template theTemplate = handlebars.compile("WhiteListEmail"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            default: {
                return null;
            }
        }

    }
}
