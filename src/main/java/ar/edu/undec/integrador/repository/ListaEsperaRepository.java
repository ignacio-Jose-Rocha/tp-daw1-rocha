package ar.edu.undec.integrador.repository;

import ar.edu.undec.integrador.domain.ListaEspera;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {

    List<ListaEspera> findByActividadIdOrderByIdAsc(Long actividadId);

    Optional<ListaEspera> findFirstByActividadIdOrderByIdAsc(Long actividadId);

    boolean existsByActividadIdAndSocioId(Long actividadId, Long socioId);

    long countByActividadIdAndIdLessThanEqual(Long actividadId, Long id);

    void deleteByActividadId(Long actividadId);

    void deleteBySocioId(Long socioId);
}
