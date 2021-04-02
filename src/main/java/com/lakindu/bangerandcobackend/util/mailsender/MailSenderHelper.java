package com.lakindu.bangerandcobackend.util.mailsender;

import com.lakindu.bangerandcobackend.entity.User;

public class MailSenderHelper {
    //helper class used to maintain email transactions.
    private final User userToBeInformed;
    private final String subject;
    private final MailTemplateType templateName;

    public MailSenderHelper(User userToBeInformed, String subject, MailTemplateType templateName) {
        this.userToBeInformed = userToBeInformed;
        this.subject = subject;
        this.templateName = templateName;
    }

    public User getUserToBeInformed() {
        return userToBeInformed;
    }

    public String getSubject() {
        return subject;
    }

    public MailTemplateType getTemplateName() {
        return templateName;
    }
}
