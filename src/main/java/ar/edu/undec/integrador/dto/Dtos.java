package ar.edu.undec.integrador.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class Dtos {

    private Dtos() {}

    public record ActividadRequest(
            @NotBlank(message = "no debe estar vacío") String nombre,
            @NotNull(message = "es obligatoria") @Future(message = "debe ser una fecha futura") LocalDateTime fechaHora,
            @NotNull(message = "es obligatorio") @Positive(message = "debe ser un entero positivo") Integer cupo) {}

    public record SemanalRequest(
            @NotBlank(message = "no debe estar vacío") String nombre,
            @NotNull(message = "es obligatoria") @Future(message = "debe ser una fecha futura") LocalDateTime fechaHora,
            @NotNull(message = "es obligatorio") @Positive(message = "debe ser un entero positivo") Integer cupo,
            @NotNull(message = "es obligatorio") @Min(value = 1, message = "debe ser al menos 1")
            @Max(value = 104, message = "no puede superar 104") Integer semanas) {}

    public record ActividadResponse(Long id, String nombre, LocalDateTime fechaHora, Integer cupo,
                                    int lugaresDisponibles) {}

    public record ActividadRankingResponse(Long id, String nombre, LocalDateTime fechaHora, Integer cupo,
                                           int lugaresDisponibles, long asistencias) {}

    public record SocioRequest(
            @NotBlank(message = "no debe estar vacío") String nombre,
            @Email(message = "debe ser un email válido") String email) {}

    public record SocioResponse(Long id, String nombre, String email) {}

    public record InscripcionRequest(
            @NotNull(message = "es obligatorio") Long actividadId,
            @NotNull(message = "es obligatorio") Long socioId) {}

    public record InscripcionResponse(Long id, Long actividadId, Long socioId, String estado,
                                      LocalDateTime fechaInscripcion) {}

    public record EstadoRequest(String estado) {}

    public record ListaEsperaRequest(@NotNull(message = "es obligatorio") Long socioId) {}

    public record ListaEsperaResponse(Long id, Long actividadId, Long socioId, long posicion,
                                      LocalDateTime fechaAnotacion) {}

    public record AsistenciaResponse(Long socioId, long inscripcionesTotales, long asistencias,
                                     double porcentaje) {}
}
