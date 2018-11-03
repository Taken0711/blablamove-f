package fr.unice.polytech.al.teamf.webservices;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import fr.unice.polytech.al.teamf.NotifyUser;
import fr.unice.polytech.al.teamf.PullNotifications;
import fr.unice.polytech.al.teamf.entities.Notification;
import fr.unice.polytech.al.teamf.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AutoJsonRpcServiceImpl
public class NotificationServiceImpl implements NotificationService {

    private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PullNotifications pullNotifications;

    @Override
    public List<Notification> pullNotificationForUser(String username) {
        return pullNotifications.pullNotificationForUser(userRepository.findByName(username).get(0));
    }
}
