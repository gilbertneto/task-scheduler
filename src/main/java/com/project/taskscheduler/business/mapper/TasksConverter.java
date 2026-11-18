package com.project.taskscheduler.business.mapper;

import com.project.taskscheduler.business.dto.TasksDTO;
import com.project.taskscheduler.infrastructure.entity.TasksEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TasksConverter {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "dataEvento", target = "dataEvento")
    @Mapping(source = "dataCriacao", target = "dataCriacao")

    TasksEntity paraTarefaEntity(TasksDTO dto);

    TasksDTO paraTarefaDTO(TasksEntity entity);

    List<TasksEntity> paraListaTarefaEntity(List<TasksDTO> dtos);
    List<TasksDTO> paraListaTarefaDTO(List<TasksEntity> entities);

}



