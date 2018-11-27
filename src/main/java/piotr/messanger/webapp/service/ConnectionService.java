package piotr.messanger.webapp.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ConnectionService {

    // mapping client's sessionID (key) with his email (value)
    private Map<String, String> sessionMap = new ConcurrentHashMap<>();


    public void addSession(String sessionId, String email) {
        sessionMap.put(sessionId, email);
    }

    public void removeSession(String sessionId) {
        sessionMap.remove((sessionId));
    }

    public List<String> getSessionIds(String email) {
        return sessionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(email))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> getUsernames() {
        return sessionMap.values().stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
