package ar.edu.undec.integrador.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones")
public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long actividadId;
    @Column(nullable = false)
    private Long socioId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado = EstadoInscripcion.CONFIRMADA;
    @Column(nullable = false)
    private LocalDateTime fechaInscripcion = LocalDateTime.now();

    public Inscripcion() {}
    public Inscripcion(Long actividadId, Long socioId) {
        this.actividadId = actividadId;
        this.socioId = socioId;
    }

    public Long getId() { return id; }
    public Long getActividadId() { return actividadId; }
    public Long getSocioId() { return socioId; }
    public EstadoInscripcion getEstado() { return estado; }
    public void setEstado(EstadoInscripcion estado) { this.estado = estado; }
    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
}
