package mas.vetclinic.repository;

import mas.vetclinic.model.entity.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    boolean existsByChipNumberEquals(String chipNumber);
    @Query("SELECT p FROM Pet p JOIN FETCH p.species WHERE p.registrationNumber = :query")
    Optional<Pet> findByRegistrationNumberFetchingSpecies(@Param("query") String query);

    @Query("SELECT p FROM Pet p JOIN FETCH p.species WHERE p.chipNumber = :query")
    Optional<Pet> findByChipNumberFetchingSpecies(@Param("query") String query);

    @Query("SELECT p FROM Pet p JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Pet> findByIdFetchingOwner(@Param("id") Long id);
}
