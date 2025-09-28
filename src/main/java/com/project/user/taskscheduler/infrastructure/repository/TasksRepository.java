package com.project.user.taskscheduler.infrastructure.repository;

import com.project.user.taskscheduler.infrastructure.entity.TasksEntity;
import com.project.user.taskscheduler.infrastructure.enums.StatusNotificationEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TasksRepository extends MongoRepository<TasksEntity, String> {
    List<TasksEntity> findByDataEventoBetweenAndStatusNotificationEnum(LocalDateTime dataInicial,
                                                                       LocalDateTime dataFinal,
                                                                       StatusNotificationEnum status);

    List<TasksEntity> findByEmailUsuario(String email);

}

