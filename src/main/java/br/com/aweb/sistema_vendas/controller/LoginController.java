package br.com.aweb.sistema_vendas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Tela de login
    @GetMapping("/login")
    public String exibirLogin() {
        return "login";
    }

    // Tela de logout
    @GetMapping("/logout")
    public String exibirLogout() {
        return "logout";
    }

}
