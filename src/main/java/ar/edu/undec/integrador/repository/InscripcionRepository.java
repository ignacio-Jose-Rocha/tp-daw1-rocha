package ar.edu.undec.integrador.repository;

import ar.edu.undec.integrador.domain.EstadoInscripcion;
import ar.edu.undec.integrador.domain.Inscripcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    long countByActividadIdAndEstadoNot(Long actividadId, EstadoInscripcion estado);

    boolean existsByActividadIdAndSocioIdAndEstadoNot(Long actividadId, Long socioId, EstadoInscripcion estado);

    List<Inscripcion> findByActividadIdOrderByIdAsc(Long actividadId);

    boolean existsByActividadId(Long actividadId);

    boolean existsBySocioId(Long socioId);

    long countBySocioId(Long socioId);

    long countBySocioIdAndEstado(Long socioId, EstadoInscripcion estado);

    @Query("select i.actividadId, count(i) from Inscripcion i where i.estado = :estado group by i.actividadId")
    List<Object[]> contarPorActividad(@Param("estado") EstadoInscripcion estado);
}
