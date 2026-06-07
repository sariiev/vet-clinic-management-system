package mas.vetclinic.repository;

import mas.vetclinic.model.entity.person.PetOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetOwnerRepository extends JpaRepository<PetOwner, Long> {
    @Query("SELECT o FROM PetOwner o LEFT JOIN FETCH o.pets p LEFT JOIN FETCH p.species WHERE o.id = :id")
    Optional<PetOwner> findByIdFetchingPets(@Param("id") Long id);
}
