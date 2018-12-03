package piotr.messanger.webapp.database;

import org.springframework.stereotype.Component;
import piotr.messanger.webapp.database.document.Conversation;
import piotr.messanger.webapp.database.document.Message;
import piotr.messanger.webapp.database.service.ConvService;
import piotr.messanger.webapp.database.service.MessageService;
import piotr.messanger.webapp.model.MessageDto;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class MessagesDatabase {

    @Resource
    private MessageService messageService;

    @Resource
    private ConvService convService;

    public MessageDto archiveMessage(String sender, String receiver, String content) {

        LocalDateTime dateTime = LocalDateTime.now(ZoneOffset.UTC);

        Message message = Message.builder().author(sender).time(dateTime).content(content).build();
        addMessage(sender, receiver, message);
        return new MessageDto(
                sender,
                dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
                content);
    }

    public List<MessageDto> getArchivedMessages(String sender, String receiver, long oldestMessage) {
        Conversation conv = convService.getConvOrDefault(sender, receiver);
        if (conv == null) {
            return Collections.emptyList();
        }
        List<MessageDto> archived = new LinkedList<>();

        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(oldestMessage), ZoneOffset.UTC);
        messageService.readArchivedMessages(conv.getId(), time).forEach(msg -> archived.add(
                new MessageDto(msg.getAuthor(),
                        msg.getTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                        msg.getContent())
        ));
        return archived;
    }

    public MessageDto getLastMessageOfConv(String convId) {
        Message dbMessage = messageService.getLastMessage(convId);
        return new MessageDto(
                dbMessage.getAuthor(),
                dbMessage.getTime().toInstant(ZoneOffset.UTC).toEpochMilli(),
                dbMessage.getContent());
    }

    private void addMessage(String sender, String receiver, Message message) {

        String convId = convService.getConversationId(sender, receiver);
        messageService.insertMessage(convId, message);
//        messageService.getLastMessage(convId);
    }
}
