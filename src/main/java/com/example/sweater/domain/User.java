package com.example.sweater.domain;

import jakarta.persistence.*;
import com.example.sweater.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "usr") // usr - потому что PostgreSQL выёбывается много, и User там уже есть. На постгресе больше нихуя не делаем
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usr_SEQ")
    @SequenceGenerator(name = "usr_SEQ", sequenceName = "usr_SEQ", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    // Поле для подтверждения пороля. Его в БД пихать не надо, используем аннотацию @Transient
    private boolean active; // Признак активности

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER) // Благодаря ElementCollection не нужно самостоятельно формировать таблицу для хранения Enum-типа
    // fetch - параметр, определяющий как данные значения (Roles) будут подгружаться относительно основной сущности (User).
    // Когда загружаем User'a его роли хранятся в отдельной таблице, и их можно подгружать либо жадным способом (EAGER) либо ленивым (LAZY).
    // EAGER - hibernate сразу же при запросе пользователя будет подгружать все его роли. LAZY - hibernate подгрузит роли только когда пользователь явно обратится к этому полю.
    // EAGER - хорош когда мало данных, это ускорит работу, но потребит больше памяти. LAZY - хорош, когда много записей хранится (например класс школа, которая содержит сотни учеников; учеников всех сразу подгружать не нужно, они нужны по мере необходимости)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id")) // Аннотация описывает что данное поле будет храниться в отдельной таблице, для которой мы не описывали маппинг.
    // Это ( ^выше^ ) позволяет создать табличку для набора ролей ( ниже  /  ), она будет называться user_role, она будет соединяться с текущей табличкой через user_id
    @Enumerated(EnumType.STRING) // Хранить Enum как строку              /
    private Set<Role> roles;                         //             <<<</

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Message> messages;


    // Хранение подписок
    @ManyToMany
    @JoinTable(
            name="user_subscriptions",
            joinColumns = {@JoinColumn(name = "subscriber_id")},
            inverseJoinColumns = {@JoinColumn(name = "channel_id")}
    )
    private Set<User> subscribtions  = new HashSet<>();

    // Хранение подписчиков
    @ManyToMany
    @JoinTable( // Чтобы обеспечить связь ManyToMany между таблицами
            name="user_subscriptions",
            joinColumns = {@JoinColumn(name = "channel_id")},
            inverseJoinColumns = {@JoinColumn(name = "subscriber_id")}
    )
    private Set<User> subscribers  = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Email(message = "Email is not correct")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    private String activationCode;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isAdmin(){
        return roles.contains(Role.ADMIN);
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public Set<User> getSubscribtions() {
        return subscribtions;
    }

    public void setSubscribtions(Set<User> subscribtions) {
        this.subscribtions = subscribtions;
    }

    public Set<User> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Set<User> subscribers) {
        this.subscribers = subscribers;
    }
}

