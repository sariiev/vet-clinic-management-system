package mas.vetclinic.repository;

import mas.vetclinic.model.entity.pet.Species;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeciesRepository extends JpaRepository<Species, Long> {

}
