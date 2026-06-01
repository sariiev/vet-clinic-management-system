package mas.vetclinic.model.entity.repository;

import mas.vetclinic.model.entity.person.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    List<Shelter> findByNameContainingIgnoreCase(String name);
}
