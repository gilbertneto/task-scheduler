package com.project.taskscheduler.business;

import com.project.taskscheduler.business.dto.TasksDTO;
import com.project.taskscheduler.business.mapper.TaskUpdateConverter;
import com.project.taskscheduler.business.mapper.TasksConverter;
import com.project.taskscheduler.infrastructure.entity.TasksEntity;
import com.project.taskscheduler.infrastructure.enums.StatusNotificationEnum;
import com.project.taskscheduler.infrastructure.exceptions.BusinessException;
import com.project.taskscheduler.infrastructure.exceptions.ConflictException;
import com.project.taskscheduler.infrastructure.exceptions.ResourceNotFoundException;
import com.project.taskscheduler.infrastructure.exceptions.UnauthorizedException;
import com.project.taskscheduler.infrastructure.repository.TasksRepository;
import com.project.taskscheduler.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor

public class TasksService {

    private final TasksRepository tasksRepository;
    private final TasksConverter tasksConverter;
    private final JwtUtil jwtUtil;
    private final TaskUpdateConverter taskUpdateConverter;

    private static final LocalTime HORARIO_ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime HORARIO_FECHAMENTO = LocalTime.of(18, 0);
    private static final long ANTECEDENCIA_MINIMA = 2L;

    private static final List<StatusNotificationEnum> STATUS_QUE_OCUPAM_HORARIO = List.of(
            StatusNotificationEnum.PENDENTE,
            StatusNotificationEnum.NOTIFICADO
    );

    public TasksDTO gravarTarefa(String token, TasksDTO dto) {
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        LocalDateTime horarioNormalizado = normalizarHorario(dto.getDataEvento());

        validarRegrasDeHorario(horarioNormalizado);

        if (tasksRepository.existsByDataEventoAndStatusNotificationEnumIn(
                horarioNormalizado, STATUS_QUE_OCUPAM_HORARIO)) {
            throw new ConflictException("O horário " + horarioNormalizado + " já se encontra reservado.");
        }

        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);
        dto.setEmailUsuario(email);
        dto.setDataEvento(horarioNormalizado);

        TasksEntity entity = tasksConverter.paraTarefaEntity(dto);

        try {
            TasksEntity salvo = tasksRepository.save(entity);
            return tasksConverter.paraTarefaDTO(salvo);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("O horário " + horarioNormalizado + "já foi reservado!", e.getCause());
        }

    }

    public List<TasksDTO> buscaTarefaAgendadaPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        if (dataInicial == null || dataFinal == null || !dataInicial.isBefore(dataFinal)) {
            throw new BusinessException("O período informado é inválido: a data inicial deve ser anterior à data final.");
        }

        return tasksConverter.paraListaTarefaDTO(
                tasksRepository.findByDataEventoBetweenAndStatusNotificationEnum(dataInicial, dataFinal,
                        StatusNotificationEnum.PENDENTE));

    }

    public List<TasksDTO> buscaTarefasPorEmail(String token) {

        String email = jwtUtil.extrairEmailToken(token.substring(7));
        List<TasksEntity> listaTarefas = tasksRepository.findByEmailUsuario(email);

        return tasksConverter.paraListaTarefaDTO(listaTarefas);
    }

    public TasksDTO cancelarTarefaPorId(String id, String token) throws UnauthorizedException {
        String emailAutenticado = extrairEmailDoToken(token);

        TasksEntity entity = tasksRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com o ID: " + id));

        validarPropriedade(entity, emailAutenticado);

        if (entity.getStatusNotificationEnum() == StatusNotificationEnum.CANCELADO) {
            throw new ConflictException("Este agendamento já se encontra cancelado.");
        }

        if (LocalDateTime.now().plusHours(ANTECEDENCIA_MINIMA).isAfter(entity.getDataEvento())) {
            throw new BusinessException(
                    "O cancelamento só é permitido com no mínimo "
                            + ANTECEDENCIA_MINIMA + " horas de antecedência.");
        }

        entity.setStatusNotificationEnum(StatusNotificationEnum.CANCELADO);
        entity.setDataAlteracao(LocalDateTime.now());

        try {
            return tasksConverter.paraTarefaDTO(tasksRepository.save(entity));
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException(
                    "O agendamento foi modificado simultaneamente por outra requisição. Tente novamente.", e);
        }
    }

    public TasksDTO alteraStatus(StatusNotificationEnum status, String id) {
        TasksEntity entity = tasksRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com o ID: " + id));

        if (entity.getStatusNotificationEnum() == StatusNotificationEnum.CANCELADO) {
            throw new ConflictException("Não é permitido alterar o status de um agendamento já cancelado.");
        }

        entity.setStatusNotificationEnum(status);
        entity.setDataAlteracao(LocalDateTime.now());

        try {
            return tasksConverter.paraTarefaDTO(tasksRepository.save(entity));
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException(
                    "Conflito de atualização simultânea ao alterar o status da tarefa " + id, e);
        }
    }

    public TasksDTO updateTarefas(TasksDTO dto, String id, String token) throws UnauthorizedException {
        String emailAutenticado = extrairEmailDoToken(token);

        TasksEntity entity = tasksRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada: " + id));

        validarPropriedade(entity, emailAutenticado);

        if (entity.getStatusNotificationEnum() == StatusNotificationEnum.CANCELADO) {
            throw new ConflictException("Não é permitido editar ou reagendar uma tarefa cancelada.");
        }

        if (dto.getDataEvento() != null) {
            LocalDateTime novoHorario = normalizarHorario(dto.getDataEvento());
            validarRegrasDeHorario(novoHorario);

            if (!novoHorario.equals(entity.getDataEvento())
                    && tasksRepository.existsByDataEventoAndStatusNotificationEnumInAndIdNot(
                    novoHorario, STATUS_QUE_OCUPAM_HORARIO, id)) {
                throw new ConflictException(
                        "O novo horário " + novoHorario + " já está reservado por outro usuário.");
            }
            dto.setDataEvento(novoHorario);
        }

        taskUpdateConverter.updateTarefas(dto, entity);
        entity.setDataAlteracao(LocalDateTime.now());

        try {
            return tasksConverter.paraTarefaDTO(tasksRepository.save(entity));
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new ConflictException(
                    "Conflito de concorrência: o novo horário escolhido acabou de ser reservado por outro usuário.", e);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException(
                    "Este agendamento foi atualizado simultaneamente em outra sessão. Recarregue os dados e tente novamente.", e);
        }

    }

            //           Metodos auxiliares //
    // ---------------------------------------------------------//

    private LocalDateTime normalizarHorario(LocalDateTime dataEvento) {
        if (dataEvento == null) {
            throw new BusinessException("A data e hora do evento são obrigatórias.");
        }
        return dataEvento.truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Valida se o horário solicitado está no futuro, em dia útil e dentro do expediente.
     */
    private void validarRegrasDeHorario(LocalDateTime dataEvento) {
        if (!dataEvento.isAfter(LocalDateTime.now())) {
            throw new BusinessException("A data do agendamento deve estar no futuro.");
        }

        DayOfWeek diaSemana = dataEvento.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            throw new BusinessException("Agendamentos são permitidos apenas em dias úteis (segunda a sexta-feira).");
        }

        LocalTime horario = dataEvento.toLocalTime();
        if (horario.isBefore(HORARIO_ABERTURA) || horario.isAfter(HORARIO_FECHAMENTO)) {
            throw new BusinessException(
                    "O horário do agendamento deve estar dentro do expediente comercial ("
                            + HORARIO_ABERTURA + " às " + HORARIO_FECHAMENTO + ").");
        }
    }

    /**
     * Verifica se o usuário autenticado no JWT é o proprietário do agendamento.
     */
    private void validarPropriedade(TasksEntity entity, String emailAutenticado) throws UnauthorizedException {
        if (entity.getEmailUsuario() == null || !entity.getEmailUsuario().equalsIgnoreCase(emailAutenticado)) {
            throw new UnauthorizedException("Você não possui permissão para alterar ou cancelar o agendamento de outro usuário.");
        }
    }

    /**
     * Extrai o e-mail do cabeçalho Authorization Bearer JWT.
     */
    private String extrairEmailDoToken(String token) throws UnauthorizedException {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token JWT ausente ou em formato inválido.");
        }
        return jwtUtil.extrairEmailToken(token.substring(7));
    }
}