package com.project.user.taskscheduler.business.mapper;

import com.project.user.taskscheduler.business.dto.TasksDTO;
import com.project.user.taskscheduler.infrastructure.entity.TasksEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TasksConverter {

    TasksEntity paraTarefaEntity(TasksDTO dto);

    TasksDTO paraTarefaDTO(TasksEntity entity);

}



