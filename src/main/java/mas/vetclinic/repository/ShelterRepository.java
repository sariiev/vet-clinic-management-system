package mas.vetclinic.repository;

import mas.vetclinic.model.entity.person.Shelter;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    boolean existsByPhoneNumbersContaining(String phoneNumber);
    Optional<Shelter> findByPhoneNumbersContaining(String phoneNumber);
    List<Shelter> findByNameContainingIgnoreCase(String name, Limit limit);
}
