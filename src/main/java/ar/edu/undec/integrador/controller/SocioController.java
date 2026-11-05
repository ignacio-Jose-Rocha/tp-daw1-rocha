package ar.edu.undec.integrador.controller;

import ar.edu.undec.integrador.dto.Dtos.*;
import ar.edu.undec.integrador.service.SocioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/socios")
public class SocioController {

    private final SocioService service;

    public SocioController(SocioService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SocioResponse> crear(@Valid @RequestBody SocioRequest r) {
        SocioResponse creado = service.crear(r);
        return ResponseEntity.created(URI.create("/api/socios/" + creado.id())).body(creado);
    }

    @GetMapping
    public List<SocioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public SocioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public SocioResponse actualizar(@PathVariable Long id, @Valid @RequestBody SocioRequest r) {
        return service.actualizar(id, r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/asistencia")
    public AsistenciaResponse asistencia(@PathVariable Long id) {
        return service.asistencia(id);
    }
}
