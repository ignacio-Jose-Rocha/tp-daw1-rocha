package ar.edu.undec.integrador.repository;

import ar.edu.undec.integrador.domain.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocioRepository extends JpaRepository<Socio, Long> {
}
