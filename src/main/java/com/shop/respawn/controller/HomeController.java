package com.shop.respawn.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {

    @RequestMapping({"", "/"})
    public String home() {
        return "home";
    }

    @RequestMapping({"", "/login"})
    public String login() {
        return "login";
    }

    @RequestMapping({"", "/join"})
    public String join() {
        return "join";
    }

}
