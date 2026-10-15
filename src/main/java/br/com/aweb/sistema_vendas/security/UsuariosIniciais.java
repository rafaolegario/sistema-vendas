package br.com.aweb.sistema_vendas.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.aweb.sistema_vendas.model.UsuarioRole;
import br.com.aweb.sistema_vendas.service.UsuarioService;

@Component
public class UsuariosIniciais implements CommandLineRunner {

    @Autowired
    UsuarioService usuarioService;

    // Usuários de teste
    @Override
    public void run(String... args) {
        if (!usuarioService.existe("rafael@email.com")) {
            usuarioService.cadastrar("Rafael", "rafael@email.com", "rafael123", UsuarioRole.ADMIN);
        }
        if (!usuarioService.existe("padrao@email.com")) {
            usuarioService.cadastrar("Usuário Padrão", "padrao@email.com", "padrao123", UsuarioRole.PADRAO);
        }
    }

}
