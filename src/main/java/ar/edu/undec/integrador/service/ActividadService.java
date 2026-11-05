package ar.edu.undec.integrador.service;

import ar.edu.undec.integrador.domain.Actividad;
import ar.edu.undec.integrador.domain.EstadoInscripcion;
import ar.edu.undec.integrador.domain.ListaEspera;
import ar.edu.undec.integrador.dto.Dtos.*;
import ar.edu.undec.integrador.exception.ApiException;
import ar.edu.undec.integrador.repository.ActividadRepository;
import ar.edu.undec.integrador.repository.InscripcionRepository;
import ar.edu.undec.integrador.repository.ListaEsperaRepository;
import ar.edu.undec.integrador.repository.SocioRepository;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActividadService {

    private final ActividadRepository actividades;
    private final InscripcionRepository inscripciones;
    private final ListaEsperaRepository espera;
    private final SocioRepository socios;
    private final InscripcionService inscripcionService;

    public ActividadService(ActividadRepository actividades, InscripcionRepository inscripciones,
                            ListaEsperaRepository espera, SocioRepository socios,
                            InscripcionService inscripcionService) {
        this.actividades = actividades;
        this.inscripciones = inscripciones;
        this.espera = espera;
        this.socios = socios;
        this.inscripcionService = inscripcionService;
    }

    private ActividadResponse aRespuesta(Actividad a) {
        return new ActividadResponse(a.getId(), a.getNombre(), a.getFechaHora(), a.getCupo(),
                inscripcionService.lugaresDisponibles(a));
    }

    public Actividad buscar(Long id) {
        return actividades.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la actividad " + id));
    }

    @Transactional
    public ActividadResponse crear(ActividadRequest r) {
        Actividad a = new Actividad();
        a.setNombre(r.nombre());
        a.setFechaHora(r.fechaHora());
        a.setCupo(r.cupo());
        return aRespuesta(actividades.save(a));
    }

    @Transactional
    public List<ActividadResponse> crearSemanales(SemanalRequest r) {
        List<ActividadResponse> creadas = new ArrayList<>();
        for (int i = 0; i < r.semanas(); i++) {
            Actividad a = new Actividad();
            a.setNombre(r.nombre());
            a.setFechaHora(r.fechaHora().plusWeeks(i));
            a.setCupo(r.cupo());
            creadas.add(aRespuesta(actividades.save(a)));
        }
        return creadas;
    }

    @Transactional(readOnly = true)
    public List<ActividadResponse> listar(LocalDate fecha) {
        List<Actividad> lista = fecha == null
                ? actividades.findAll().stream().sorted(Comparator.comparing(Actividad::getId)).toList()
                : actividades.findByFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
                        fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        return lista.stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ActividadResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public ActividadResponse actualizar(Long id, ActividadRequest r) {
        Actividad a = actividades.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la actividad " + id));
        long ocupados = inscripciones.countByActividadIdAndEstadoNot(id, EstadoInscripcion.CANCELADA);
        if (r.cupo() < ocupados) {
            throw ApiException.conflicto("El cupo no puede ser menor a las inscripciones vigentes (" + ocupados + ")");
        }
        a.setNombre(r.nombre());
        a.setFechaHora(r.fechaHora());
        a.setCupo(r.cupo());
        actividades.save(a);
        inscripcionService.promoverListaEspera(a);
        return aRespuesta(a);
    }

    @Transactional
    public void borrar(Long id) {
        Actividad a = buscar(id);
        if (inscripciones.existsByActividadId(id)) {
            throw ApiException.conflicto("La actividad tiene inscripciones y no se puede borrar");
        }
        espera.deleteByActividadId(id);
        actividades.delete(a);
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> inscripciones(Long id) {
        buscar(id);
        return inscripcionService.listarPorActividad(id);
    }

    // ---- Lista de espera ----

    private ListaEsperaResponse aRespuesta(ListaEspera e) {
        long posicion = espera.countByActividadIdAndIdLessThanEqual(e.getActividadId(), e.getId());
        return new ListaEsperaResponse(e.getId(), e.getActividadId(), e.getSocioId(), posicion,
                e.getFechaAnotacion());
    }

    @Transactional
    public ListaEsperaResponse anotarEnEspera(Long actividadId, Long socioId) {
        Actividad a = actividades.findByIdForUpdate(actividadId)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la actividad " + actividadId));
        if (!socios.existsById(socioId)) {
            throw ApiException.noEncontrado("No existe el socio " + socioId);
        }
        if (inscripciones.existsByActividadIdAndSocioIdAndEstadoNot(actividadId, socioId,
                EstadoInscripcion.CANCELADA)) {
            throw ApiException.conflicto("El socio ya está inscripto en esta actividad");
        }
        if (espera.existsByActividadIdAndSocioId(actividadId, socioId)) {
            throw ApiException.conflicto("El socio ya está en la lista de espera");
        }
        if (inscripcionService.lugaresDisponibles(a) > 0) {
            throw ApiException.conflicto("Todavía hay lugares disponibles: inscribite directamente");
        }
        return aRespuesta(espera.save(new ListaEspera(actividadId, socioId)));
    }

    @Transactional(readOnly = true)
    public List<ListaEsperaResponse> listaEspera(Long actividadId) {
        buscar(actividadId);
        return espera.findByActividadIdOrderByIdAsc(actividadId).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ListaEsperaResponse entradaEspera(Long actividadId, Long entradaId) {
        buscar(actividadId);
        return espera.findById(entradaId).filter(e -> e.getActividadId().equals(actividadId))
                .map(this::aRespuesta)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la entrada " + entradaId));
    }

    // ---- Ranking ----

    @Transactional(readOnly = true)
    public List<ActividadRankingResponse> masConcurridas(int top) {
        if (top < 1) {
            throw ApiException.invalido("'top' debe ser un entero positivo");
        }
        Map<Long, Long> asistencias = new HashMap<>();
        for (Object[] fila : inscripciones.contarPorActividad(EstadoInscripcion.ASISTIO)) {
            asistencias.put((Long) fila[0], (Long) fila[1]);
        }
        return actividades.findAll().stream()
                .sorted(Comparator.comparingLong((Actividad a) -> asistencias.getOrDefault(a.getId(), 0L))
                        .reversed().thenComparing(Actividad::getId))
                .limit(top)
                .map(a -> new ActividadRankingResponse(a.getId(), a.getNombre(), a.getFechaHora(), a.getCupo(),
                        inscripcionService.lugaresDisponibles(a), asistencias.getOrDefault(a.getId(), 0L)))
                .toList();
    }
}
