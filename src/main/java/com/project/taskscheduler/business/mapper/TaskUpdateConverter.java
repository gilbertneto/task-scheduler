package com.project.taskscheduler.business.mapper;


import com.project.taskscheduler.business.dto.TasksDTO;
import com.project.taskscheduler.infrastructure.entity.TasksEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface TaskUpdateConverter {

    void updateTarefas(TasksDTO dto, @MappingTarget TasksEntity entity);

}
