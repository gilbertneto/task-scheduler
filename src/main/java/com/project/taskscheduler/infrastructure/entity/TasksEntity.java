package com.project.taskscheduler.infrastructure.entity;


import com.project.taskscheduler.infrastructure.enums.StatusNotificationEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Document(collection = "tarefa")
@CompoundIndex(
        name = "idx_unique_horario_ativo",
        def = "{'dataEvento': 1}",
        unique = true,
        partialFilter = "{'statusNotificationEnum': {'$in': ['PENDENTE', 'NOTIFICADO']}}"
)

public class TasksEntity {

    @Id
    private String id;
    private String nomeTarefa;
    private String descricao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataEvento;
    private String emailUsuario;
    private LocalDateTime dataAlteracao;
    private StatusNotificationEnum statusNotificationEnum;

    @Version
    private Long version;

}
