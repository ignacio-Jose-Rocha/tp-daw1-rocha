package ar.edu.undec.integrador.service;

import ar.edu.undec.integrador.domain.EstadoInscripcion;
import ar.edu.undec.integrador.domain.Inscripcion;
import ar.edu.undec.integrador.domain.Socio;
import ar.edu.undec.integrador.dto.Dtos.*;
import ar.edu.undec.integrador.exception.ApiException;
import ar.edu.undec.integrador.repository.InscripcionRepository;
import ar.edu.undec.integrador.repository.ListaEsperaRepository;
import ar.edu.undec.integrador.repository.SocioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SocioService {

    private final SocioRepository socios;
    private final InscripcionRepository inscripciones;
    private final ListaEsperaRepository espera;

    public SocioService(SocioRepository socios, InscripcionRepository inscripciones, ListaEsperaRepository espera) {
        this.socios = socios;
        this.inscripciones = inscripciones;
        this.espera = espera;
    }

    static SocioResponse aRespuesta(Socio s) {
        return new SocioResponse(s.getId(), s.getNombre(), s.getEmail());
    }

    public Socio buscar(Long id) {
        return socios.findById(id).orElseThrow(() -> ApiException.noEncontrado("No existe el socio " + id));
    }

    @Transactional
    public SocioResponse crear(SocioRequest r) {
        Socio s = new Socio();
        s.setNombre(r.nombre());
        s.setEmail(r.email());
        return aRespuesta(socios.save(s));
    }

    @Transactional(readOnly = true)
    public List<SocioResponse> listar() {
        return socios.findAll().stream().map(SocioService::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public SocioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public SocioResponse actualizar(Long id, SocioRequest r) {
        Socio s = buscar(id);
        s.setNombre(r.nombre());
        s.setEmail(r.email());
        return aRespuesta(socios.save(s));
    }

    @Transactional
    public void borrar(Long id) {
        Socio s = buscar(id);
        if (inscripciones.existsBySocioId(id)) {
            throw ApiException.conflicto("El socio tiene inscripciones y no se puede borrar");
        }
        espera.deleteBySocioId(id);
        socios.delete(s);
    }

    @Transactional(readOnly = true)
    public AsistenciaResponse asistencia(Long id) {
        buscar(id);
        long total = inscripciones.countBySocioId(id);
        long asistio = inscripciones.countBySocioIdAndEstado(id, EstadoInscripcion.ASISTIO);
        double pct = total == 0 ? 0.0 : Math.round(asistio * 10000.0 / total) / 100.0;
        return new AsistenciaResponse(id, total, asistio, pct);
    }
}
