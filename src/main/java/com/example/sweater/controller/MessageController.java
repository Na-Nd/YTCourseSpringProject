package com.example.sweater.controller;

import com.example.sweater.domain.Message;
import com.example.sweater.domain.User;
import com.example.sweater.domain.dto.MessageDto;
import com.example.sweater.repos.MessageRepo;
import com.example.sweater.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MessageController {
    @Autowired // Получив бин-компонент messageRepo будем использовать его для обработки данных
    private MessageRepo messageRepo;

    @Autowired
    private MessageService messageService;

    @Value("${upload.path}") // Ищет в property папку upload.puth и вставляет в эту переменную
    private String uploadPath;

    @GetMapping("/")
    public String greeting(Map<String, Object> model) {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String filter, Model model, @PageableDefault(sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable, @AuthenticationPrincipal User user){ // required=false, т.к. мы не всегда будем передавать этот параметр. defaultValue - дефолтное значение, нужно чтобы всё работало нормально, даже если фильтр не указан
        Page<MessageDto> page = messageService.messageList(pageable, filter, user);

        model.addAttribute("page", page);
        model.addAttribute("url", "/main");
        model.addAttribute("filter", filter);
        return "main";
    }

    @PostMapping("/main")
    public String add(
            @AuthenticationPrincipal User user,
            @Valid Message message, // Аннотация @Valid сообщает Spring MVC о необходимости провести валидацию этого объекта перед его использованием в методе.
            BindingResult bindingResult, // BindingResult - это список аргументов и сообщения ошибко валидации. ОН ДОЛЖЕНИ ВСЕГДА ИДТИ ПЕРЕД АРГУМЕНТОМ MODEL
            Model model,
            @RequestParam("file")MultipartFile file //@RequestParam означает, что это параметр запроса GET или POST. Если POST-запрос, аннотация берет параметры из формы (.mustache, .html, ...). Если GET, то берет из URL
    ) throws IOException {

        message.setAuthor(user);

        // Обрабатываем ошибки валидации, которые могут возникнуть в результате применения аннотации @Valid к объекту в Spring MVC контроллере.
        if(bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errorsMap);
            model.addAttribute("message", message);
        }
        else { // Если валидация прошла без ошибок, то добавление файла
            saveFile(message, file);
            model.addAttribute("message", null); // Чтобы после добавления сообщения не получать открытую форму, которая содержит сообщение, которое мы только что добавили
            messageRepo.save(message); // Сохраняем его в репозиторий
        }
        Iterable<Message> messages = messageRepo.findAll();
        model.addAttribute("messages", messages); // Взяли из репозитория и положили в модель

        return "main";
    }

    private void saveFile(Message message, MultipartFile file) throws IOException {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) { // Если не существует
                uploadDir.mkdir(); // То создать новую
            }
            String uuidFile = UUID.randomUUID().toString(); // Создаём уникальное имя файла, чтобы обезопасить от коллизий
            String resultFilename = uuidFile + "." + file.getOriginalFilename(); // Имя файла, которое будем класть в message

            file.transferTo(new File(uploadPath + "/" + resultFilename));

            message.setFilename(resultFilename);
        }
    }

    @GetMapping("/user-messages/{author}")
    public String userMessages(@AuthenticationPrincipal User currentUser,
                               @PathVariable User author, Model model,
                               @RequestParam(required = false) Message message,
                               @PageableDefault(sort = { "id" }, direction =  Sort.Direction.DESC) Pageable pageable)
    {
        Page<MessageDto> page = messageService.messageListForUser(pageable, currentUser, author);
        model.addAttribute("userChannel", author);
        model.addAttribute("subscriptionsCount", author.getSubscribtions().size());
        model.addAttribute("subscribersCount", author.getSubscribers().size());
        model.addAttribute("isSubscriber", author.getSubscribers().contains(currentUser));
        model.addAttribute("page", page);
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(author));
        model.addAttribute("url", "/user-messages" + author.getId());
        return "userMessages";
    }

    @PostMapping("/user-messages/{user}")
    public String updateMessage(
            @AuthenticationPrincipal User currentUser, // Эта аннотация позволяет внедрить текущего аутентифицированного пользователя (если таковой есть) в метод контроллера.
            @PathVariable Long user, // Эта аннотация указывает на то, что переменная пути {user} извлекается из URL и передается в метод контроллера в качестве аргумента типа Long
            @RequestParam("id") Message message, //  Эта аннотация используется для извлечения значения параметра запроса с именем id. Значение параметра будет привязано к аргументу message метода контроллера. Тип аргумента указывает на то, что ожидается объект типа Message.
            @RequestParam("text") String text,
            @RequestParam("tag") String tag,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if(message.getAuthor().equals(currentUser)){
            if(!StringUtils.isEmpty(text)){ // Если поле text не пустое, то обновим этот текст сообщения
                message.setText(text);
            }
            if(!StringUtils.isEmpty(tag)){
                message.setTag(tag);
            }
            saveFile(message,file);
            messageRepo.save(message);
        }
        return "redirect:/user-messages/" + user;
        
    }

    @GetMapping("/messages/{message}/like")
    public String like(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Message message,
            RedirectAttributes redirectAttributes, // Передать новой страние те параметры, которые были получены при запросе на этоу страницу
            @RequestHeader(required = false) String referer // Чтобы понять откуда пришли
    ){
        Set<User> likes = message.getLikes();
        if(likes.contains(currentUser)){
            likes.remove(currentUser);
        }
        else {
            likes.add(currentUser);
        }

        // Для извлечения параметров будем использовать UriComponentsBuilder
        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();
        components.getQueryParams().entrySet().forEach(pair -> redirectAttributes.addAttribute(pair.getKey(), pair.getValue()));
        return "redirect:" + components.getPath();
    }

}
