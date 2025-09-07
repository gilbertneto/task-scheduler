package com.project.user.taskscheduler.controller;


import com.project.user.taskscheduler.business.TasksService;
import com.project.user.taskscheduler.business.dto.TasksDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor

public class TasksController {

    private final TasksService tasksService;

    @PostMapping

    public ResponseEntity<TasksDTO> gravarTarefa(@RequestBody TasksDTO dto,
                                                 @RequestHeader("Authorization") String token){
        return ResponseEntity.ok(tasksService.gravarTarefa(token, dto));
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<TasksDTO>> buscaListaTarefaPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime dataFinal){

            return ResponseEntity.ok(tasksService.buscaTarefaAgendadaPorPeriodo(dataInicial, dataFinal));
    }

    @GetMapping
    public ResponseEntity<List<TasksDTO>> buscaTarefasPorEmail (@RequestHeader ("Authorization") String token){
        List<TasksDTO> tarefas = tasksService.buscaTarefasPorEmail(token);
        return ResponseEntity.ok(tarefas);
    }



}
