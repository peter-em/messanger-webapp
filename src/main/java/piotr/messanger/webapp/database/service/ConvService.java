package piotr.messanger.webapp.database.service;

import org.springframework.stereotype.Service;
import piotr.messanger.webapp.database.document.Conversation;
import piotr.messanger.webapp.database.repository.ConversationRepository;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class ConvService {

    @Resource
    private ConversationRepository convRepo;

    public Conversation getConversationId(String login1, String login2) {
        SortedSet<String> clients = sortLogins(login1, login2);

        return convRepo.findByFirstAndSecond(clients.first(), clients.last())
                .orElseGet(() -> convRepo.save(new Conversation(clients.first(), clients.last())));
    }

    public Conversation getConvOrDefault(String login1, String login2) {
        SortedSet<String> clients = sortLogins(login1, login2);
        return convRepo.findByFirstAndSecond(clients.first(), clients.last()).orElse(null);
    }

    private SortedSet<String> sortLogins(String login1, String login2) {
        return new TreeSet<>(Arrays.asList(login1, login2));
    }

    public List<Conversation> getUserConversations(String login) {
        return convRepo.findByFirstOrSecond(login, login);
    }

    public void updateUnread(Conversation conversation) {
        convRepo.save(conversation);
    }
}
