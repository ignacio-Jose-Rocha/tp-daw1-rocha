package ar.edu.undec.integrador.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_espera")
public class ListaEspera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long actividadId;
    @Column(nullable = false)
    private Long socioId;
    @Column(nullable = false)
    private LocalDateTime fechaAnotacion = LocalDateTime.now();

    public ListaEspera() {}
    public ListaEspera(Long actividadId, Long socioId) {
        this.actividadId = actividadId;
        this.socioId = socioId;
    }

    public Long getId() { return id; }
    public Long getActividadId() { return actividadId; }
    public Long getSocioId() { return socioId; }
    public LocalDateTime getFechaAnotacion() { return fechaAnotacion; }
}
