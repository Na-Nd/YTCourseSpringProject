package com.example.sweater.service;

import com.example.sweater.domain.Role;
import com.example.sweater.domain.User;
import com.example.sweater.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${hostname}")
    private String hostname;

    @Override
    //  Этот метод принимает имя пользователя в качестве параметра и возвращает объект UserDetails, представляющий сведения о пользователе.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);

        if(user == null){
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }
    /*Если пользователь с заданным именем найден, он возвращается из метода loadUserByUsername().
    Если пользователя не существует, то выбрасывается исключение UsernameNotFoundException,
     которое говорит Spring Security о том, что пользователь не найден.*/


    public boolean addUser(User user){
        User userFromDB = userRepo.findByUsername(user.getUsername());

        if(userFromDB != null){
            return false; // Если пользователь найден в БД, то вернуть false, что означает, что такого пользователя добавить нельзя
        }

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setActivationCode(UUID.randomUUID().toString()); // Будем считать, что почта подтверждения, когда пользователь перешел по ссылке с UUID
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);

        sendMessage(user);

        return true; // Если пользователь добавлен и всё ок, возвращает true
    }

    private void sendMessage(User user) {
        if(!StringUtils.isEmpty(user.getEmail())){ // Для непустой строки Email выполняем отправвку сообщений
            String message = String.format("Hello, %s! \n" + "Welcome to Sweater. Please, visit next link: http://%s/activate/%s", user.getUsername(), hostname, user.getActivationCode());
            mailSender.send(user.getEmail(), "Activation code", message);
        }
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if(user == null){
            return false;
        }

        user.setActivationCode(null);

        userRepo.save(user);

        return true;
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public void saveUser(User user, String username, Map<String, String> form) {
        // Передаём мапу, чтобы получить список полей, который передаётся в этой форме. Т.к. количество полей переменное (может быть 1 пользователь, а может быть дохуя), они все будут попадать в форму, но на сервер будут приходить только те, которые отмечены флажком (если в форме роль присутствует, значит в UI для нее установлен флажок, но помимо ролей есть и другие поля в этой форме(token, id), которые нам не нужны) => в форме будет разное кол-во полей
        user.setUsername(username);

        Set<String> roles = Arrays.stream(Role.values()).map(Role::name).collect(Collectors.toSet()); // 1. Используется Stream API для создания потока из всех значений перечисления Role. 2.Затем для каждого значения в потоке вызывается метод name, который возвращает имя роли. 3.Полученный поток имен ролей затем преобразуется в Set с помощью Collectors.toSet().

        user.getRoles().clear(); // Чистим роли пользователя

        for(String key : form.keySet()){
            if(roles.contains(key)){ // Берем роли и проверяем что они содержат ключ key
                user.getRoles().add(Role.valueOf(key)); // Пользователю в список роле добавляем роль, которую получаем через valueOf()
            }
        }

        userRepo.save(user);
    }

    public void updateProfile(User user, String password, String email) {
        String userEmail = user.getEmail();

        // Проверяем, изменился ли Email:
        boolean isEmailChanged = (email != null && !email.equals(userEmail)) || (userEmail != null && !userEmail.equals(email));

        if(isEmailChanged){
            user.setEmail(email);

            // Если обновили Email пользователя, нужно отправить ему новый код активации. Чтобы отправить код, его нужно сгенерировать
            if (!StringUtils.isEmpty(email)){ // Если пользователь установил новый Email
                user.setActivationCode(UUID.randomUUID().toString());
            }
        }

        if(!StringUtils.isEmpty(password)){ // Если пользователь установил новый пароль (если строка пароля не пустая)
            user.setPassword(password);
        }

        // Сохраняем пользователя
        userRepo.save(user);

        // После сохраенния отправляем код активации
        if(isEmailChanged){ // Код отправляем только тогда, когда пароль был изменен
            sendMessage(user);
        }

    }

    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);
        userRepo.save(user);
    }

    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);
        userRepo.save(user);
    }
}
