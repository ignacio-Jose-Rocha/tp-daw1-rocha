package ar.edu.undec.integrador.service;

import ar.edu.undec.integrador.domain.Actividad;
import ar.edu.undec.integrador.domain.EstadoInscripcion;
import ar.edu.undec.integrador.domain.Inscripcion;
import ar.edu.undec.integrador.domain.ListaEspera;
import ar.edu.undec.integrador.dto.Dtos.*;
import ar.edu.undec.integrador.exception.ApiException;
import ar.edu.undec.integrador.repository.ActividadRepository;
import ar.edu.undec.integrador.repository.InscripcionRepository;
import ar.edu.undec.integrador.repository.ListaEsperaRepository;
import ar.edu.undec.integrador.repository.SocioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripciones;
    private final ActividadRepository actividades;
    private final SocioRepository socios;
    private final ListaEsperaRepository espera;

    public InscripcionService(InscripcionRepository inscripciones, ActividadRepository actividades,
                              SocioRepository socios, ListaEsperaRepository espera) {
        this.inscripciones = inscripciones;
        this.actividades = actividades;
        this.socios = socios;
        this.espera = espera;
    }

    static InscripcionResponse aRespuesta(Inscripcion i) {
        return new InscripcionResponse(i.getId(), i.getActividadId(), i.getSocioId(),
                i.getEstado().name(), i.getFechaInscripcion());
    }

    /** Lugares libres = cupo - inscripciones no canceladas (se calcula, no se guarda). */
    public int lugaresDisponibles(Actividad a) {
        long ocupados = inscripciones.countByActividadIdAndEstadoNot(a.getId(), EstadoInscripcion.CANCELADA);
        return (int) Math.max(0, a.getCupo() - ocupados);
    }

    private boolean tieneInscripcionActiva(Long actividadId, Long socioId) {
        return inscripciones.existsByActividadIdAndSocioIdAndEstadoNot(
                actividadId, socioId, EstadoInscripcion.CANCELADA);
    }

    private Inscripcion buscar(Long id) {
        return inscripciones.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la inscripción " + id));
    }

    @Transactional
    public InscripcionResponse crear(InscripcionRequest r) {
        Actividad a = actividades.findByIdForUpdate(r.actividadId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe la actividad " + r.actividadId()));
        if (!socios.existsById(r.socioId())) {
            throw ApiException.noEncontrado("No existe el socio " + r.socioId());
        }
        if (tieneInscripcionActiva(a.getId(), r.socioId())) {
            throw ApiException.conflicto("El socio ya está inscripto en esta actividad");
        }
        if (lugaresDisponibles(a) <= 0) {
            throw ApiException.conflicto("No hay lugar: el cupo de la actividad está completo");
        }
        return aRespuesta(inscripciones.save(new Inscripcion(a.getId(), r.socioId())));
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listar() {
        return inscripciones.findAll().stream().map(InscripcionService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public InscripcionResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> listarPorActividad(Long actividadId) {
        return inscripciones.findByActividadIdOrderByIdAsc(actividadId).stream()
                .map(InscripcionService::aRespuesta).toList();
    }

    @Transactional
    public InscripcionResponse cambiarEstado(Long id, String estadoTexto) {
        if (estadoTexto == null || estadoTexto.isBlank()) {
            throw ApiException.invalido("El campo 'estado' es obligatorio");
        }
        EstadoInscripcion nuevo;
        try {
            nuevo = EstadoInscripcion.valueOf(estadoTexto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalido("Estado inválido: " + estadoTexto);
        }
        Inscripcion i = buscar(id);
        Actividad a = actividades.findByIdForUpdate(i.getActividadId())
                .orElseThrow(() -> ApiException.noEncontrado("No existe la actividad " + i.getActividadId()));
        if (i.getEstado() != EstadoInscripcion.CONFIRMADA || nuevo == EstadoInscripcion.CONFIRMADA) {
            throw ApiException.conflicto("Transición no permitida: " + i.getEstado() + " → " + nuevo);
        }
        if (nuevo == EstadoInscripcion.CANCELADA && LocalDateTime.now().plusHours(2).isAfter(a.getFechaHora())) {
            throw ApiException.conflicto("No se puede cancelar con menos de 2 horas de anticipación");
        }
        i.setEstado(nuevo);
        inscripciones.save(i);
        if (nuevo == EstadoInscripcion.CANCELADA) {
            promoverListaEspera(a);
        }
        return aRespuesta(i);
    }

    @Transactional
    public void borrar(Long id) {
        Inscripcion i = buscar(id);
        Actividad a = actividades.findByIdForUpdate(i.getActividadId()).orElse(null);
        inscripciones.delete(i);
        inscripciones.flush();
        if (a != null) {
            promoverListaEspera(a);
        }
    }

    /** Mientras haya lugar, inscribe al primero de la lista de espera. */
    @Transactional
    public void promoverListaEspera(Actividad a) {
        while (lugaresDisponibles(a) > 0) {
            Optional<ListaEspera> primero = espera.findFirstByActividadIdOrderByIdAsc(a.getId());
            if (primero.isEmpty()) {
                return;
            }
            ListaEspera e = primero.get();
            espera.delete(e);
            espera.flush();
            if (socios.existsById(e.getSocioId()) && !tieneInscripcionActiva(a.getId(), e.getSocioId())) {
                inscripciones.saveAndFlush(new Inscripcion(a.getId(), e.getSocioId()));
            }
        }
    }
}
