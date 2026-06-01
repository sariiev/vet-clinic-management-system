package mas.vetclinic.model.entity.repository;

import mas.vetclinic.model.entity.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    Optional<Pet> findByRegistrationNumber(String registrationNumber);
    Optional<Pet> findByChipNumber(String chipNumber);
    List<Pet> findByNameContainingIgnoreCase(String name);
}
