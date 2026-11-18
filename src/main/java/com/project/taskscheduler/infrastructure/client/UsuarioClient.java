package com.project.taskscheduler.infrastructure.client;

import com.project.taskscheduler.business.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "usuario", url = "${user.url}")
public interface UsuarioClient {

    @GetMapping("/usuario")
    UserDTO buscaUsuarioPorEmail(@RequestParam("email") String email,
                                 @RequestHeader("Authorization") String token);

}
