package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.EmailAlreadyExistsException;
import com.djeno.lab1.exceptions.UserNotFoundException;
import com.djeno.lab1.exceptions.UsernameAlreadyExistsException;
import com.djeno.lab1.persistence.enums.Role;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.persistence.repositories.UserRepository;
import com.djeno.lab1.persistence.wrappers.UsersWrapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Value("${users.xml.path}")
    private String xmlFilePath;

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    public User save(User user) {
        return repository.save(user);
    }

    /**
     * Создание пользователя
     *
     * @return созданный пользователь
     */
    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            // Заменить на свои исключения
            throw new UsernameAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User savedUser = repository.save(user);

        saveUsersToXml(); // сохраняем всех пользователей в XML

        return savedUser;
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

    }

    /**
     * Получение пользователя по username или email пользователя
     *
     * @return пользователь
     */
    public User getByUsernameOrEmail(String identifier) {
        return repository.findByUsername(identifier)
                .orElseGet(() -> repository.findByEmail(identifier)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")));
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    public UserDetailsService userDetailsService() {
        return this::getByUsernameOrEmail;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    private void saveUsersToXml() {
        try {
            List<User> users = repository.findAll();
            UsersWrapper wrapper = new UsersWrapper(users);

            JAXBContext context = JAXBContext.newInstance(UsersWrapper.class, User.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            marshaller.marshal(wrapper, new File(xmlFilePath));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
