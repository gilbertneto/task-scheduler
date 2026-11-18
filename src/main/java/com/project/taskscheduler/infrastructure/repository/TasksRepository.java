package com.project.taskscheduler.infrastructure.repository;

import com.project.taskscheduler.infrastructure.entity.TasksEntity;
import com.project.taskscheduler.infrastructure.enums.StatusNotificationEnum;
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

    boolean existsByDataEventoAndStatusNotificationEnumIn(
            LocalDateTime dataEvento,
            List<StatusNotificationEnum> status);

    boolean existsByDataEventoAndStatusNotificationEnumInAndIdNot(
            LocalDateTime dataEvento,
            List<StatusNotificationEnum> status,
            String id
    );

}

