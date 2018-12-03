package piotr.messanger.webapp.database.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import piotr.messanger.webapp.database.document.Message;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageService {

    @Value("${webapp.constants.archivedSize}")
    private int archivedSize;

    @Resource
    private MongoTemplate msgRepo;


    public void insertMessage(String convId, Message message) {

        msgRepo.save(message, convId);
    }

    public List<Message> readArchivedMessages(String convId, LocalDateTime time) {

        Query query = new Query(Criteria.where("time").lt(time));
        long count = msgRepo.count(query, convId);
        query.skip(count - archivedSize);

        List<Message> list = msgRepo.find(query, Message.class, convId);
        Collections.reverse(list);
        return list;
    }

    public Message getLastMessage(String convId) {

        Query query = new Query();
        long count = msgRepo.count(query, convId);

        return msgRepo.find(query.skip(count - 1), Message.class, convId).get(0);
    }
}
