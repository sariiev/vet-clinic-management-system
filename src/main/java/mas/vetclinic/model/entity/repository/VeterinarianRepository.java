package mas.vetclinic.model.entity.repository;

import mas.vetclinic.model.entity.person.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {

}
