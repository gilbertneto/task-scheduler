package com.project.user.taskscheduler.controller;


import com.project.user.taskscheduler.business.TasksService;
import com.project.user.taskscheduler.business.dto.TasksDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor

public class TasksController {

    private final TasksService tasksService;

    @PostMapping

    public ResponseEntity<TasksDTO> recordTasks(@RequestBody TasksDTO dto,
                                                @RequestHeader("Authorization") String token){
        return ResponseEntity.ok(tasksService.recordTask(token, dto));
    }
}
