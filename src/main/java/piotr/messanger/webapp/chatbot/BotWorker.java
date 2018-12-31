package piotr.messanger.webapp.chatbot;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import piotr.messanger.webapp.model.ClientRequest;
import piotr.messanger.webapp.model.MessageDto;
import piotr.messanger.webapp.service.MailingService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class BotWorker implements Runnable {

    private SimpMessageSendingOperations messagingTemplate;
    private BlockingQueue<ClientRequest> requests;
    private MailingService mailingService;
    private Map<String, LocalDateTime> mailCooldown;

    public BotWorker(BlockingQueue<ClientRequest> requests,
                     SimpMessageSendingOperations messagingTemplate,
                     MailingService mailingService) {
        this.requests = requests;
        this.messagingTemplate = messagingTemplate;
        this.mailingService = mailingService;
        mailCooldown = new HashMap<>();
    }

    @Override
    public void run() {

        try {
            while (true) {
                ClientRequest request = requests.take();
                String[] words = request.getContent().split(" ");
                TimeUnit.MILLISECONDS.sleep(800);

                String response = "You wrote: '" + request.getContent() + "'";
                if (words[0].equals("!hello")) {
                    response = "Hello " + request.getUser() + ", my friend!";
                } else if (words[0].equals("!email")) {

                    response = mailingService.handleSendMailRequest(words, request);
                }

                MessageDto msg = new MessageDto("NightBot",
                        LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli(),
                        response);
                messagingTemplate.convertAndSend("/conv/priv/" + request.getUser(), msg);

            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

    }


    public boolean addRequest(ClientRequest request) {
        return  requests.offer(request);
    }
}
