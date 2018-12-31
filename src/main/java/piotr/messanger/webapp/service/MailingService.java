package piotr.messanger.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.model.ClientRequest;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class MailingService {

    private static final String TEMPLATE = "You are receiving this email on request of user %s"
            + ".\nMessage:\n%s" + "\n\nNightBot, Messanger-Webapp";

    @Resource
    private JavaMailSender mailSender;
    @Resource
    private MessageSource messages;


    public String handleSendMailRequest(String[] words, ClientRequest request) {
        if (words.length > 2) {
            SimpleMailMessage mail = new SimpleMailMessage();

            StringBuilder builder = new StringBuilder();
            for (int i = 2; i < words.length; ++i) {
                builder.append(words[i]).append(' ');
            }
            mailSender.send(constructEmail(words[1], "Message from " + request.getUser(),
                    String.format(TEMPLATE, request.getUser(), builder.toString().trim())));

//            mail.setTo(words[1]);
//            mail.setSubject("Message from " + request.getUser());
//            String content = request.getContent().substring(words[0].length() + words[1].length() + 2);
//            mail.setText("You are receiving this email on request of user "
//                    + request.getUser() + ".\nMessage:\n" + content
//                    + "\n\nNightBot, Messanger-Webapp");
//            mailSender.send(mail);
            return  "Your message was sent to " + words[1];
        }

        return  "Not enough arguments. Example usage:\n'!email email@example.com Message content'";
    }

    public void sendResetTokenEmail(String contextPath, String token, User user) {

        String url = contextPath + "/changePassword?id=" + user.getId() + "&token=" + token;
        String message = messages.getMessage("message.resetPassword", null, null);
        mailSender.send(constructEmail(user.getEmail(),
                "Reset Your Password",
                message + "<br/><br/>" + "<a href=\"" + url + "\">" + url + "</a>"));

    }

    private MimeMessage constructEmail(String to, String subject, String text) {
        MimeMessage mail = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
        } catch (MessagingException ex) {
            log.error(ex.getMessage());
        }
        return mail;
    }
}
