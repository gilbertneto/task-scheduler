package com.project.user.taskscheduler.business.mapper;


import com.project.user.taskscheduler.business.dto.TasksDTO;
import com.project.user.taskscheduler.infrastructure.entity.TasksEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface TaskUpdateConverter {

    void updateTarefas(TasksDTO dto, @MappingTarget TasksEntity entity);

}
