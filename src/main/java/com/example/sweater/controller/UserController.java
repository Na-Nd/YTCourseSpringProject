package com.example.sweater.controller;

import com.example.sweater.domain.Role;
import com.example.sweater.domain.User;
import com.example.sweater.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')") // Эта аннотация будет проверять для метода ниже наличие у пользователя прав ADMIN
    @GetMapping
    public String userList(Model model){
        model.addAttribute("users", userService.findAll());
        return "userList";
    }

    @PreAuthorize("hasAuthority('ADMIN')") // Эта аннотация будет проверять для метода ниже наличие у пользователя прав ADMIN
    @GetMapping("{user}")
    public String userEditForm(@PathVariable User user, Model model){ // @PathVariable - означает, что параметр user будет извлечён из URL и преобразован в объект типа User. URL = "/user/{user}"
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "userEdit";
    }

    @PreAuthorize("hasAuthority('ADMIN')") // Эта аннотация будет проверять для метода ниже наличие у пользователя прав ADMIN
    @PostMapping
    public String userSave(
            @RequestParam String username,
            @RequestParam Map<String, String> form,
            @RequestParam("userId") User user
    )
    {
        userService.saveUser(user, username, form);
        return "redirect:/user";
    }

    @GetMapping("profile")
    public String getProfile(Model model, @AuthenticationPrincipal User user){ // @AuthenticationPrincipal будет ожидать пользователя из контекста, чтобы не получать его из БД
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        return "profile";
    }

    @PostMapping("profile")
    public String updateProfile(@AuthenticationPrincipal User user, @RequestParam String password, @RequestParam String email){ // Метод ожидает помимо пользователя еще и параметры пароли и почты
        userService.updateProfile(user, password, email);

        return "redirect:/user/profile";
    }

    @GetMapping("subscribe/{user}")
    public String subscribe( @AuthenticationPrincipal User currentUser, @PathVariable User user){
        userService.subscribe(currentUser, user);
        return "redirect:/user-messages/" + user.getId();
    }

    @GetMapping("unsubscribe/{user}")
    public String unsubscribe( @AuthenticationPrincipal User currentUser, @PathVariable User user){
        userService.unsubscribe(currentUser, user);
        return "redirect:/user-messages/" + user.getId();
    }

    @GetMapping("{type}/{user}/list")
    public String userList(Model model, @PathVariable User user, @PathVariable String type){
        model.addAttribute("userChannel", user);
        model.addAttribute("type", type);

        if("subscriptions".equals(type)){
            model.addAttribute("users", user.getSubscribtions());
        }else{
            model.addAttribute("users", user.getSubscribers());
        }

        return "subscriptions";
    }

}

