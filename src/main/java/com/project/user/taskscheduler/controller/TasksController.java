package com.project.user.taskscheduler.controller;


import com.project.user.taskscheduler.business.TasksService;
import com.project.user.taskscheduler.business.dto.TasksDTO;
import com.project.user.taskscheduler.infrastructure.enums.StatusNotificationEnum;
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
                                                 @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(tasksService.gravarTarefa(token, dto));
    }

    @GetMapping("/eventos")
    public ResponseEntity<List<TasksDTO>> buscaListaTarefaPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal) {

        return ResponseEntity.ok(tasksService.buscaTarefaAgendadaPorPeriodo(dataInicial, dataFinal));
    }

    @GetMapping
    public ResponseEntity<List<TasksDTO>> buscaTarefasPorEmail(@RequestHeader("Authorization") String token) {
        List<TasksDTO> tarefas = tasksService.buscaTarefasPorEmail(token);
        return ResponseEntity.ok(tarefas);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletaTarefaPorId(@RequestParam("id") String id) {
        tasksService.deletaTarefaPorId(id);

        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<TasksDTO> alteraStatusNotificacao(@RequestParam("status") StatusNotificationEnum status,
                                                            @RequestParam("id") String id) {

        return ResponseEntity.ok(tasksService.alteraStatus(status, id));
    }

    @PutMapping
    public ResponseEntity<TasksDTO> updateTarefas(@RequestBody TasksDTO dto, @RequestParam("id") String id){
        return ResponseEntity.ok(tasksService.updateTarefas(dto, id));
    }
}
