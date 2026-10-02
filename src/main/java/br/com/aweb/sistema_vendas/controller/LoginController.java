package br.com.aweb.sistema_vendas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // Exibe a tela de login; o POST de /login é processado pelo Spring Security
    @GetMapping("/login")
    public String exibirLogin() {
        return "login";
    }

}
