package com.project.taskscheduler.business;

import com.project.taskscheduler.business.dto.TasksDTO;
import com.project.taskscheduler.business.mapper.TaskUpdateConverter;
import com.project.taskscheduler.business.mapper.TasksConverter;
import com.project.taskscheduler.infrastructure.entity.TasksEntity;
import com.project.taskscheduler.infrastructure.enums.StatusNotificationEnum;
import com.project.taskscheduler.infrastructure.exceptions.BusinessException;
import com.project.taskscheduler.infrastructure.exceptions.ConflictException;
import com.project.taskscheduler.infrastructure.exceptions.UnauthorizedException;
import com.project.taskscheduler.infrastructure.repository.TasksRepository;
import com.project.taskscheduler.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TasksServiceTest {
    @Mock
    private TasksRepository tasksRepository;

    @Mock
    private TasksConverter tasksConverter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TaskUpdateConverter taskUpdateConverter;

    @InjectMocks
    private TasksService tasksService;

    private static final String TOKEN = "Bearer jwt-token-valido";
    private static final String EMAIL_DONO = "usuario@teste.com";
    private LocalDateTime proximoDiaUtil14h;

    @BeforeEach
    void setUp() {
        proximoDiaUtil14h = LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
                .withHour(14)
                .withMinute(0)
                .withSecond(45)
                .withNano(123);
    }

    @Test
    @DisplayName("gravarTarefa deve normalizar horário para o minuto exato e salvar como PENDENTE")
    void deveGravarTarefaNormalizandoHorario() {
        LocalDateTime horarioEsperado = proximoDiaUtil14h.withSecond(0).withNano(0);
        TasksDTO input = TasksDTO.builder()
                .nomeTarefa("Revisão API")
                .descricao("Alinhamento semanal")
                .dataEvento(proximoDiaUtil14h)
                .build();

        TasksEntity entity = new TasksEntity();
        entity.setId("task-1");
        entity.setDataEvento(horarioEsperado);
        entity.setEmailUsuario(EMAIL_DONO);
        entity.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);

        TasksDTO expectedDto = TasksDTO.builder()
                .id("task-1")
                .dataEvento(horarioEsperado)
                .emailUsuario(EMAIL_DONO)
                .statusNotificationEnum(StatusNotificationEnum.PENDENTE)
                .build();

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.existsByDataEventoAndStatusNotificationEnumIn(eq(horarioEsperado), anyList()))
                .thenReturn(false);
        when(tasksConverter.paraTarefaEntity(any(TasksDTO.class))).thenReturn(entity);
        when(tasksRepository.save(entity)).thenReturn(entity);
        when(tasksConverter.paraTarefaDTO(entity)).thenReturn(expectedDto);

        TasksDTO result = tasksService.gravarTarefa(TOKEN, input);

        assertNotNull(result);
        assertEquals(horarioEsperado, input.getDataEvento());
        assertEquals(StatusNotificationEnum.PENDENTE, result.getStatusNotificationEnum());
        verify(tasksRepository).save(entity);
    }

    @Test
    @DisplayName("gravarTarefa deve lançar BusinessException ao tentar agendar fora do horário comercial")
    void deveBloquearAgendamentoForaDoExpediente() {
        LocalDateTime foraDoHorario = proximoDiaUtil14h.withHour(21).withMinute(0);
        TasksDTO input = TasksDTO.builder()
                .nomeTarefa("Reunião noturna")
                .dataEvento(foraDoHorario)
                .build();

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);

        assertThrows(BusinessException.class, () -> tasksService.gravarTarefa(TOKEN, input));
        verify(tasksRepository, never()).save(any());
    }

    @Test
    @DisplayName("gravarTarefa deve lançar ConflictException quando o horário já estiver reservado")
    void deveBloquearHorarioJaOcupado() {
        LocalDateTime horarioNormalizado = proximoDiaUtil14h.withSecond(0).withNano(0);
        TasksDTO input = TasksDTO.builder()
                .nomeTarefa("Consulta")
                .dataEvento(proximoDiaUtil14h)
                .build();

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.existsByDataEventoAndStatusNotificationEnumIn(eq(horarioNormalizado), anyList()))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> tasksService.gravarTarefa(TOKEN, input));
        verify(tasksRepository, never()).save(any());
    }

    @Test
    @DisplayName("gravarTarefa deve converter DuplicateKeyException do MongoDB em ConflictException")
    void deveConverterDuplicateKeyEmConflictException() {
        LocalDateTime horarioNormalizado = proximoDiaUtil14h.withSecond(0).withNano(0);
        TasksDTO input = TasksDTO.builder()
                .nomeTarefa("Consulta simultânea")
                .dataEvento(proximoDiaUtil14h)
                .build();
        TasksEntity entity = new TasksEntity();

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.existsByDataEventoAndStatusNotificationEnumIn(eq(horarioNormalizado), anyList()))
                .thenReturn(false);
        when(tasksConverter.paraTarefaEntity(any(TasksDTO.class))).thenReturn(entity);
        when(tasksRepository.save(entity)).thenThrow(new DuplicateKeyException("idx_unique_horario_ativo"));

        assertThrows(ConflictException.class, () -> tasksService.gravarTarefa(TOKEN, input));
    }

    @Test
    @DisplayName("cancelarTarefaPorId deve alterar status para CANCELADO sem excluir do banco")
    void deveRealizarCancelamentoLogicoComSucesso() throws UnauthorizedException {
        TasksEntity entity = new TasksEntity();
        entity.setId("task-1");
        entity.setEmailUsuario(EMAIL_DONO);
        entity.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);
        entity.setDataEvento(LocalDateTime.now().plusHours(5));

        TasksDTO canceledDto = TasksDTO.builder()
                .id("task-1")
                .statusNotificationEnum(StatusNotificationEnum.CANCELADO)
                .build();

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.findById("task-1")).thenReturn(Optional.of(entity));
        when(tasksRepository.save(entity)).thenReturn(entity);
        when(tasksConverter.paraTarefaDTO(entity)).thenReturn(canceledDto);

        TasksDTO result = tasksService.cancelarTarefaPorId("task-1", TOKEN);

        assertEquals(StatusNotificationEnum.CANCELADO, result.getStatusNotificationEnum());
        verify(tasksRepository, never()).deleteById(anyString());
        verify(tasksRepository).save(entity);
    }

    @Test
    @DisplayName("cancelarTarefaPorId deve lançar UnauthorizedException quando outro usuário tentar cancelar")
    void deveBloquearCancelamentoPorOutroUsuario() {
        TasksEntity entity = new TasksEntity();
        entity.setId("task-1");
        entity.setEmailUsuario("outro@teste.com");
        entity.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);
        entity.setDataEvento(LocalDateTime.now().plusHours(5));

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.findById("task-1")).thenReturn(Optional.of(entity));

        assertThrows(UnauthorizedException.class, () -> tasksService.cancelarTarefaPorId("task-1", TOKEN));
        verify(tasksRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelarTarefaPorId deve lançar BusinessException com menos de 2h de antecedência")
    void deveBloquearCancelamentoForaDaJanelaMinima() {
        TasksEntity entity = new TasksEntity();
        entity.setId("task-1");
        entity.setEmailUsuario(EMAIL_DONO);
        entity.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);
        entity.setDataEvento(LocalDateTime.now().plusMinutes(45));

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.findById("task-1")).thenReturn(Optional.of(entity));

        assertThrows(BusinessException.class, () -> tasksService.cancelarTarefaPorId("task-1", TOKEN));
        verify(tasksRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTarefas deve lançar ConflictException quando OptimisticLockingFailureException ocorrer")
    void deveTratarConcorrenciaOtimistaNoUpdate() {
        TasksEntity entity = new TasksEntity();
        entity.setId("task-1");
        entity.setEmailUsuario(EMAIL_DONO);
        entity.setStatusNotificationEnum(StatusNotificationEnum.PENDENTE);
        entity.setDataEvento(proximoDiaUtil14h.withSecond(0).withNano(0));

        TasksDTO updateDto = TasksDTO.builder()
                .nomeTarefa("Título atualizado")
                .build();

        when(jwtUtil.extrairEmailToken("jwt-token-valido")).thenReturn(EMAIL_DONO);
        when(tasksRepository.findById("task-1")).thenReturn(Optional.of(entity));
        when(tasksRepository.save(entity)).thenThrow(new OptimisticLockingFailureException("Versão desatualizada"));

        assertThrows(ConflictException.class, () -> tasksService.updateTarefas(updateDto, "task-1", TOKEN));
    }
}
