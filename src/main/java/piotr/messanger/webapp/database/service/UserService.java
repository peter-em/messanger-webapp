package piotr.messanger.webapp.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import piotr.messanger.webapp.database.entity.Role;
import piotr.messanger.webapp.database.entity.User;
import piotr.messanger.webapp.database.repository.RoleRepository;
import piotr.messanger.webapp.database.repository.UserRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service("userService")
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User saveUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setActive(1);
        Role userRole = roleRepository.findByRoleName("USER");
        user.setRoleEntities(new HashSet<>(Collections.singletonList(userRole)));
        return userRepository.save(user);
    }

    public void updateLastActive(User user) {
        user.setLastActive(null);
        userRepository.save(user);
    }

    public List<String> getLogins() {
        return userRepository.getLogins();
    }

}