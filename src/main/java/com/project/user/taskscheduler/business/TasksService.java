package com.project.user.taskscheduler.business;

import com.project.user.taskscheduler.business.dto.TasksDTO;
import com.project.user.taskscheduler.business.mapper.TaskUpdateConverter;
import com.project.user.taskscheduler.business.mapper.TasksConverter;
import com.project.user.taskscheduler.infrastructure.entity.TasksEntity;
import com.project.user.taskscheduler.infrastructure.enums.StatusNotificationEnum;
import com.project.user.taskscheduler.infrastructure.exceptions.ResourceNotFoundException;
import com.project.user.taskscheduler.infrastructure.repository.TasksRepository;
import com.project.user.taskscheduler.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class TasksService {

    private final TasksRepository tasksRepository;
    private final TasksConverter tasksConverter;
    private final JwtUtil jwtUtil;
    private final TaskUpdateConverter taskUpdateConverter;


    public TasksDTO gravarTarefa(String token, TasksDTO dto) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);
        dto.setEmailUsuario(email);
        TasksEntity entity = tasksConverter.paraTarefaEntity(dto);

        return tasksConverter.paraTarefaDTO(
                tasksRepository.save(entity));
    }

    public List<TasksDTO> buscaTarefaAgendadaPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return tasksConverter.paraListaTarefaDTO(
                tasksRepository.findByDataEventoBetweenAndStatusNotificationEnum(dataInicial, dataFinal,
                        StatusNotificationEnum.PENDENTE));

    }

    public List<TasksDTO> buscaTarefasPorEmail(String token) {

        String email = jwtUtil.extrairEmailToken(token.substring(7));
        List<TasksEntity> listaTarefas = tasksRepository.findByEmailUsuario(email);

        return tasksConverter.paraListaTarefaDTO(listaTarefas);
    }

    public void deletaTarefaPorId(String id) {
        try {
            tasksRepository.deleteById(id);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao deletar tarefa por ID, ID inexistente " + id,
                    e.getCause());

        }
    }

    public TasksDTO alteraStatus(StatusNotificationEnum status, String id) {
        try {
            TasksEntity entity = tasksRepository.findById(id).
                    orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada " + id));
            entity.setStatusNotificationEnum(status);
            return tasksConverter.paraTarefaDTO(tasksRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao alterar o status da tarefa "
                    + e.getCause());
        }
    }

    public TasksDTO updateTarefas(TasksDTO dto, String id) {
        try {
            TasksEntity entity = tasksRepository.findById(id).
                    orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada " + id));
        taskUpdateConverter.updateTarefas(dto, entity);
        return tasksConverter.paraTarefaDTO(tasksRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao alterar o status da tarefa "
                    + e.getCause());
        }
    }
}