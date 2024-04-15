package com.example.sweater;

import static org.assertj.core.api.Assertions.assertThat;
import com.example.sweater.controller.MessageController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmokeTest {

    @Autowired
    private MessageController controller;

    @Test // Помечает тестовые методы
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }
}