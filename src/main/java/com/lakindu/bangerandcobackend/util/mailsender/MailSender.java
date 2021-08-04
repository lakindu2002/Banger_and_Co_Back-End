package com.lakindu.bangerandcobackend.util.mailsender;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.lakindu.bangerandcobackend.entity.AdditionalEquipment;
import com.lakindu.bangerandcobackend.entity.Rental;
import com.lakindu.bangerandcobackend.entity.RentalCustomization;
import com.lakindu.bangerandcobackend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

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

    private Logger LOGGER = Logger.getLogger(MailSender.class.getName());

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

    @Async //execute on secondary thread.
    public void sendMail(MailSenderHelper theHelper) {
        Message theMessage = new MimeMessage(theMailSession); //create a MimeMessage to send via Email
        try {
            //format a template according to the mail sending type.
            //null set because no rental.
            String contentToEmail = setTemplate(theHelper.getTemplateName(), theHelper, null); //retrieve formatted template

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
            LOGGER.info("AN EMAIL WAS SENT");
            LOGGER.info(String.format("THE MAIL OF TYPE: %s WAS SENT", theHelper.getTemplateName().toString().toUpperCase()));
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private String setTemplate(MailTemplateType theType, MailSenderHelper theHelper, Rental rentalMade) throws IOException {
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
            case RENTAL_MADE: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("pickupTime", rentalMade.getPickupTime().toString());
                dynamicData.put("returnDate", rentalMade.getReturnDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
                dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                dynamicData.put("totalCostForRental", String.valueOf(rentalMade.getTotalCost()));

                StringBuilder theBuilder = new StringBuilder();
                if (rentalMade.getRentalCustomizationList() == null || rentalMade.getRentalCustomizationList().size() == 0) {
                    dynamicData.put("equipmentList", "None");
                } else {
                    for (RentalCustomization eachCustomization : rentalMade.getRentalCustomizationList()) {
                        AdditionalEquipment equipment = eachCustomization.getEquipmentAddedToRental();
                        theBuilder.append(equipment.getEquipmentName()).append(", ");
                    }
                    dynamicData.put("equipmentList", theBuilder.toString().trim());
                }

                Template theTemplate = handlebars.compile("RentalMadeCustomer"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case ADMIN_CREATED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("adminUsername", theHelper.getUserToBeInformed().getUsername());

                Template theTemplate = handlebars.compile("AdminAccountCreated"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case ADMIN_DELETED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("deletedDate", new Date().toString());

                Template theTemplate = handlebars.compile("AdminAccountDeleted"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case ACCOUNT_BLACKLISTED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnDate", rentalMade.getReturnDate().format(DateTimeFormatter.ISO_DATE));
                if (rentalMade.getVehicleOnRental() != null) {
                    dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                }

                Template theTemplate = handlebars.compile("CustomerBlacklisted"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case RENTAL_APPROVED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("pickupTime", rentalMade.getPickupTime().toString());
                dynamicData.put("returnDate", rentalMade.getReturnDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
                dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                dynamicData.put("approvedDate", new Date().toString());

                Template theTemplate = handlebars.compile("RentalApprovedCustomer"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case RENTAL_REJECTED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                dynamicData.put("pickupTime", rentalMade.getPickupTime().toString());
                dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("rejectedDate", new Date().toString());
                dynamicData.put("rejectedReason", theHelper.getRentalRejectionReason());

                Template theTemplate = handlebars.compile("RentalRejectedCustomer"); //retrieve the template based on required type
                //the template will be searched for {{}} and the relevant data will be assigned by the apply method.
                //library provided by jknack.

                final String formattedTemplate = theTemplate.apply(dynamicData);//return formatted template to the caller
                dynamicData.clear(); //clear hashmap contents after formatting template
                return formattedTemplate;
            }
            case RENTAL_STARTED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                dynamicData.put("pickupTime", rentalMade.getPickupTime().toString());
                dynamicData.put("startedDate", new Date().toString());
                dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));

                Template rentalStarted = handlebars.compile("RentalStarted");
                String formattedData = rentalStarted.apply(dynamicData);
                dynamicData.clear();
                return formattedData;
            }
            case RENTAL_RETURNED: {
                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                dynamicData.put("pickupTime", rentalMade.getPickupTime().toString());
                dynamicData.put("returnedDate", new Date().toString());
                dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));

                Template theTemplate = handlebars.compile("RentalReturned");
                String formattedData = theTemplate.apply(dynamicData);

                dynamicData.clear();

                return formattedData;
            }
            case LATE_RETURN_CANCELLED_CUSTOMER:
            case LATE_RETURN_CUSTOMER: {
                Template theTemplate = null;
                if (theType == MailTemplateType.LATE_RETURN_CUSTOMER) {
                    theTemplate = handlebars.compile("LateReturnCustomer");
                } else {
                    theTemplate = handlebars.compile("LateReturnCancelledCustomer");
                }

                dynamicData.put("firstName", theHelper.getUserToBeInformed().getFirstName());
                dynamicData.put("lastName", theHelper.getUserToBeInformed().getLastName());
                dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
                dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
                dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
                dynamicData.put("returnDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));

                String formattedData = theTemplate.apply(dynamicData);

                dynamicData.clear();

                return formattedData;
            }
            default: {
                return null;
            }
        }

    }

    @Async
    public void sendRentalMail(MailSenderHelper theHelper, Rental madeRental) throws IOException, MessagingException {
        String formattedTemplate = setTemplate(theHelper.getTemplateName(), theHelper, madeRental);

        MimeMessage theMessage = new MimeMessage(theMailSession);
        theMessage.setSentDate(new Date()); //set current date is sent date
        theMessage.setFrom(new InternetAddress(cooperateEmailAddress)); //set the sender
        theMessage.setSubject(theHelper.getSubject()); //set the Subject for the Mail
        theMessage.setContent(formattedTemplate, mailType);
        theMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(theHelper.getUserToBeInformed().getEmailAddress()));

        Transport.send(theMessage);

        LOGGER.info("AN EMAIL WAS SENT");
        LOGGER.info(String.format("THE MAIL OF TYPE: %s WAS SENT", theHelper.getTemplateName().toString().toUpperCase()));
    }

    @Async
    public void sendBulkRentalEmails(List<String> adminList, String subject, List<User> blacklistedUsers, MailTemplateType theType, Rental madeRental) throws MessagingException, IOException {
        MimeMessage theMessage = new MimeMessage(theMailSession);
        String formattedTemplate = getTemplateForBulkRental(blacklistedUsers, theType, madeRental);

        theMessage.setSentDate(new Date()); //set current date is sent date
        theMessage.setFrom(new InternetAddress(cooperateEmailAddress)); //set the sender
        theMessage.setSubject(subject); //set the Subject for the Mail
        theMessage.setContent(formattedTemplate, mailType);
        theMessage.addRecipients(Message.RecipientType.TO, getAddressList(adminList));

        Transport.send(theMessage);

        LOGGER.info("AN EMAIL WAS SENT");
        LOGGER.info(String.format("THE MAIL OF TYPE: %s WAS SENT", theType.toString().toUpperCase()));
    }

    private String getTemplateForBulkRental(List<User> blackListedUsers, MailTemplateType theType, Rental madeRental) throws IOException {
        TemplateLoader configurer = new ClassPathTemplateLoader(); //load templates from classpath
        configurer.setPrefix("/templates"); //template has "/templates" path as prefix
        configurer.setSuffix(".html"); //templates are of html

        Handlebars handlebars = new Handlebars(configurer); //create a Handlebars class with current loader used to load templates

        if (theType == MailTemplateType.ADMIN_BULK_BLACKLIST) {
            Template theTemplate = handlebars.compile("AdminAccountBlackList");

            StringBuilder customerNames = new StringBuilder();
            for (User eachCustomer : blackListedUsers) {
                customerNames.append(String.format("%s %s (%s), ", eachCustomer.getFirstName(), eachCustomer.getLastName(), eachCustomer.getUsername()));
            }

            dynamicData.put("executedDate", new Date().toString());
            dynamicData.put("size", String.valueOf(blackListedUsers.size()));
            dynamicData.put("customerNames", customerNames.toString());

            String formattedContent = theTemplate.apply(dynamicData);
            dynamicData.clear();

            return formattedContent;
        } else if (theType == MailTemplateType.LATE_RETURN_ADMINS || theType == MailTemplateType.LATE_RETURN_CANCELLED_ADMIN_BULK) {
            Template theTemplate = null;
            if (theType == MailTemplateType.LATE_RETURN_ADMINS) {
                theTemplate = handlebars.compile("LateReturnAdmin");
            } else {
                theTemplate = handlebars.compile("LateReturnCancelledAdmin");
            }

            dynamicData.put("vehicleName", madeRental.getVehicleOnRental().getVehicleName());
            dynamicData.put("returnTime", madeRental.getReturnTime().toString());
            dynamicData.put("pickupDate", madeRental.getPickupDate().format(DateTimeFormatter.ISO_DATE));
            dynamicData.put("returnDate", madeRental.getPickupDate().format(DateTimeFormatter.ISO_DATE));

            String formattedContent = theTemplate.apply(dynamicData);
            dynamicData.clear();

            return formattedContent;
        } else {
            return null;
        }
    }

    private Address[] getAddressList(List<String> adminList) throws AddressException {
        List<Address> theEmailAddresses = new ArrayList<>();
        for (String eachAdmin : adminList) {
            theEmailAddresses.add(new InternetAddress(eachAdmin));
        }
        return theEmailAddresses.toArray(new Address[adminList.size()]);
    }

    @Async
    public void notifyAllAdminsAboutNewRental(List<String> adminList, String subject, Rental madeRental, MailTemplateType adminBulkRentalMade) throws MessagingException, IOException {
        MimeMessage theMessage = new MimeMessage(theMailSession);
        String formattedTemplate = getTemplateForNewRental(madeRental);
        theMessage.setSentDate(new Date()); //set current date is sent date
        theMessage.setFrom(new InternetAddress(cooperateEmailAddress)); //set the sender
        theMessage.setSubject(subject); //set the Subject for the Mail
        theMessage.setContent(formattedTemplate, mailType);
        theMessage.addRecipients(Message.RecipientType.TO, getAddressList(adminList));

        Transport.send(theMessage);

        LOGGER.info("AN EMAIL WAS SENT");
        LOGGER.info(String.format("THE MAIL OF TYPE: %s WAS SENT", adminBulkRentalMade.toString().toUpperCase()));
    }

    private String getTemplateForNewRental(Rental rentalMade) throws IOException {
        TemplateLoader configurer = new ClassPathTemplateLoader(); //load templates from classpath
        configurer.setPrefix("/templates"); //template has "/templates" path as prefix
        configurer.setSuffix(".html"); //templates are of html

        Handlebars handlebars = new Handlebars(configurer); //create a Handlebars class with current loader used to load templates

        Template theTemplate = handlebars.compile("AdminRentalMadeNotification");

        dynamicData.put("pickupDate", rentalMade.getPickupDate().format(DateTimeFormatter.ISO_DATE));
        dynamicData.put("pickupTime", rentalMade.getPickupTime().toString());
        dynamicData.put("returnDate", rentalMade.getReturnDate().format(DateTimeFormatter.ISO_DATE));
        dynamicData.put("returnTime", rentalMade.getReturnTime().toString());
        dynamicData.put("vehicleName", rentalMade.getVehicleOnRental().getVehicleName());
        dynamicData.put("totalCostForRental", String.valueOf(rentalMade.getTotalCost()));

        StringBuilder theBuilder = new StringBuilder();
        if (rentalMade.getRentalCustomizationList() == null || rentalMade.getRentalCustomizationList().size() == 0) {
            dynamicData.put("equipmentList", "None");
        } else {
            for (RentalCustomization eachCustomization : rentalMade.getRentalCustomizationList()) {
                AdditionalEquipment equipment = eachCustomization.getEquipmentAddedToRental();
                theBuilder.append(equipment.getEquipmentName()).append(", ");
            }
            dynamicData.put("equipmentList", theBuilder.toString().trim());
        }
        String formattedContent = theTemplate.apply(dynamicData);
        dynamicData.clear();

        return formattedContent;
    }
}
