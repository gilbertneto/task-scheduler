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

@Service
@RequiredArgsConstructor

public class TasksService {

    private final TasksRepository tasksRepository;
    private final TasksConverter tasksConverter;
    private final JwtUtil jwtUtil;


    public TasksDTO recordTask(String token, TasksDTO dto) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificaoEnum(StatusNotificationEnum.PENDENTE);
        dto.setEmailUsuairio(email);
        TasksEntity entity = tasksConverter.paraTarefaEntity(dto);

        return tasksConverter.paraTarefaDTO(
        tasksRepository.save(entity));
    }

}
