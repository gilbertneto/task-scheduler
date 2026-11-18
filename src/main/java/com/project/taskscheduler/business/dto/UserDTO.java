package com.project.taskscheduler.business.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDTO {

    private String email;
    private String senha;

}
