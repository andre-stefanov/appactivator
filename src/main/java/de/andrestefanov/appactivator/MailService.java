package de.andrestefanov.appactivator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;

import static de.andrestefanov.appactivator.Application.VALID_EMAIL_ADDRESS_REGEX;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    private final Configuration freeMarkerConfig;

    private final MailConfig mailConfig;

    @Autowired
    public MailService(JavaMailSender mailSender, @Qualifier("freeMarkerConfiguration") Configuration freeMarkerConfig, MailConfig mailConfig) {
        this.mailSender = mailSender;
        this.freeMarkerConfig = freeMarkerConfig;
        this.mailConfig = mailConfig;
    }

    void prepareAndSendValidation(String recipient, String token) throws Exception {
        if (!validateEmailAddress(recipient)) {
            throw new SecurityException("not allowed for " + recipient);
        }

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(mailConfig.getFrom(), mailConfig.getName());
            messageHelper.setTo(recipient);
            messageHelper.setSubject(mailConfig.getSubject());

            Template template = freeMarkerConfig.getTemplate("email.ftl");

            String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, new EmailModel(token));

            messageHelper.setText(text, true);
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            throw new Exception(e);
        }
    }

    private boolean validateEmailAddress(String emailAddress) {
        return (!StringUtils.isEmpty(emailAddress) &&
                VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress).find() &&
                emailAddress.endsWith(mailConfig.getSuffix())) ||
                (!StringUtils.isEmpty(mailConfig.getDebug()) && mailConfig.getDebug().equals(emailAddress));
    }

    public final static class EmailModel {

        private String token;

        EmailModel(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

}
