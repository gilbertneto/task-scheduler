package com.project.user.taskscheduler.business;

import com.project.user.taskscheduler.business.dto.TasksDTO;
import com.project.user.taskscheduler.business.mapper.TasksConverter;
import com.project.user.taskscheduler.infrastructure.entity.TasksEntity;
import com.project.user.taskscheduler.infrastructure.enums.StatusNotificationEnum;
import com.project.user.taskscheduler.infrastructure.repository.TasksRepository;
import com.project.user.taskscheduler.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class TasksService {

    private final TasksRepository tasksRepository;
    private final TasksConverter tasksConverter;
    private final JwtUtil jwtUtil;


    public TasksDTO gravarTarefa(String token, TasksDTO dto) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificacaoEnum(StatusNotificationEnum.PENDENTE);
        dto.setEmailUsuario(email);
        TasksEntity entity = tasksConverter.paraListaEntity(dto);

        return tasksConverter.paraListaDTO(
        tasksRepository.save(entity));
    }

    public List<TasksDTO> buscaTarefaAgendadaPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return tasksConverter.paraListaTarefaDTO(
                tasksRepository.findByDataEventoBetween(dataInicial, dataFinal));

    }

    public List<TasksDTO> buscaTarefasPorEmail(String token) {

        String email = jwtUtil.extrairEmailToken(token.substring(7));
        List<TasksEntity> listaTarefas = tasksRepository.findByEmailUsuario(email);

        return tasksConverter.paraListaTarefaDTO(listaTarefas);
    }

}
