package ar.edu.undec.integrador.controller;

import ar.edu.undec.integrador.dto.Dtos.*;
import ar.edu.undec.integrador.service.InscripcionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService service;

    public InscripcionController(InscripcionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InscripcionResponse> crear(@Valid @RequestBody InscripcionRequest r) {
        InscripcionResponse creada = service.crear(r);
        return ResponseEntity.created(URI.create("/api/inscripciones/" + creada.id())).body(creada);
    }

    @GetMapping
    public List<InscripcionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public InscripcionResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    public InscripcionResponse cambiarEstado(@PathVariable Long id, @RequestBody EstadoRequest r) {
        return service.cambiarEstado(id, r.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        service.borrar(id);
        return ResponseEntity.noContent().build();
    }
}
