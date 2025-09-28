package com.project.user.taskscheduler.infrastructure.security;

import com.project.user.taskscheduler.business.dto.UserDTO;
import com.project.user.taskscheduler.infrastructure.client.UsuarioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl {

    @Autowired
    private UsuarioClient client;


    public UserDetails carregaDadosUsuario(String email, String token){
        UserDTO userDTO = client.buscaUsuarioPorEmail(email, token);
        return User
                .withUsername(userDTO.getEmail()) // Define o nome de usuário como o e-mail
                .password(userDTO.getSenha()) // Define a senha do usuário
                .build();
    }
}
