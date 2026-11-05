package ar.edu.undec.integrador.controller;

import ar.edu.undec.integrador.dto.Dtos.*;
import ar.edu.undec.integrador.service.ActividadService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService service;

    public ActividadController(ActividadService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ActividadResponse> crear(@Valid @RequestBody ActividadRequest r) {
        ActividadResponse creada = service.crear(r);
        return ResponseEntity.created(URI.create("/api/actividades/" + creada.id())).body(creada);
    }

    @PostMapping("/semanales")
    public ResponseEntity<List<ActividadResponse>> semanales(@Valid @RequestBody SemanalRequest r) {
        List<ActividadResponse> creadas = service.crearSemanales(r);
        return ResponseEntity.created(URI.create("/api/actividades/" + creadas.get(0).id())).body(creadas);
    }

    @GetMapping
    public List<ActividadResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.listar(fecha);
    }

    @GetMapping("/mas-concurridas")
    public List<ActividadRankingResponse> masConcurridas(@RequestParam(defaultValue = "5") int top) {
        return service.masConcurridas(top);
    }

    @GetMapping("/{id}")
    public ActividadResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public ActividadResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActividadRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/inscripciones")
    public List<InscripcionResponse> inscripciones(@PathVariable Long id) {
        return service.inscripciones(id);
    }

    @PostMapping("/{id}/lista-espera")
    public ResponseEntity<ListaEsperaResponse> anotarEnEspera(@PathVariable Long id,
                                                              @Valid @RequestBody ListaEsperaRequest r) {
        ListaEsperaResponse e = service.anotarEnEspera(id, r.socioId());
        return ResponseEntity.created(URI.create("/api/actividades/" + id + "/lista-espera/" + e.id())).body(e);
    }

    @GetMapping("/{id}/lista-espera")
    public List<ListaEsperaResponse> listaEspera(@PathVariable Long id) {
        return service.listaEspera(id);
    }

    @GetMapping("/{id}/lista-espera/{entradaId}")
    public ListaEsperaResponse entradaEspera(@PathVariable Long id, @PathVariable Long entradaId) {
        return service.entradaEspera(id, entradaId);
    }
}
