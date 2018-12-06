package piotr.messanger.webapp.database;

import org.springframework.stereotype.Component;
import piotr.messanger.webapp.database.document.Conversation;
import piotr.messanger.webapp.database.document.Message;
import piotr.messanger.webapp.database.service.ConvService;
import piotr.messanger.webapp.database.service.MessageService;
import piotr.messanger.webapp.model.MessageDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class MessagesDatabase {

    private MessageService messageService;
    private ConvService convService;

    public MessagesDatabase(MessageService messageService, ConvService convService) {
        this.messageService = messageService;
        this.convService = convService;
    }

    public MessageDto archiveMessage(String sender, String receiver, String content, boolean unread) {

        LocalDateTime dateTime = LocalDateTime.now(ZoneOffset.UTC);

        Message message = Message.builder().author(sender).time(dateTime).content(content).build();

        Conversation conversation = convService.getConversationId(sender, receiver);
        if (unread) {
            conversation.setUnread(conversation.getUnread() + 1);
            convService.updateUnread(conversation);
        }

        messageService.insertMessage(conversation.getId(), message);
        return new MessageDto(
                sender,
                dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                content);
    }

    public List<MessageDto> getArchivedMessages(String requestAuthor, String partner, long oldestMessage) {
        Conversation conversation = convService.getConvOrDefault(requestAuthor, partner);
        if (conversation == null) {
            return Collections.emptyList();
        }
        LinkedList<MessageDto> archived = new LinkedList<>();

        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(oldestMessage), ZoneOffset.UTC);
        messageService.readArchivedMessages(conversation.getId(), time).forEach(msg -> archived.add(
                new MessageDto(msg.getAuthor(),
                        msg.getTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                        msg.getContent())
        ));

        if (conversation.getUnread() > 0 &&
                !archived.isEmpty() &&
                !archived.getLast().getAuthor().equals(requestAuthor)) {
            conversation.setUnread(0);
            convService.updateUnread(conversation);
        }
        return archived;
    }

    public MessageDto getLastMessageOfConv(String convId) {
        Message dbMessage = messageService.getLastMessage(convId);
        return new MessageDto(
                dbMessage.getAuthor(),
                dbMessage.getTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                dbMessage.getContent());
    }
}
