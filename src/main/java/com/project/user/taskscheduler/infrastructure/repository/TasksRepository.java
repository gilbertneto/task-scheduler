package com.project.user.taskscheduler.infrastructure.repository;

import com.project.user.taskscheduler.infrastructure.entity.TasksEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TasksRepository extends MongoRepository<TasksEntity, String> {

}
