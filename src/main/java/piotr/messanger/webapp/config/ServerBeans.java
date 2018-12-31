package piotr.messanger.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import piotr.messanger.webapp.model.ClientRequest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Configuration
public class ServerBeans {

    @Bean
    public BlockingQueue<ClientRequest> getBlockingQueue() {
        return new ArrayBlockingQueue<>(100);
    }

    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("You are receiving this email on request of user %s"
                + ".\nMessage:\n%s" + "\n\nNightBot, Messanger-Webapp");
        return message;
    }
}
