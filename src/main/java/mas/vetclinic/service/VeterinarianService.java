package mas.vetclinic.service;

import mas.vetclinic.model.entity.person.Person;
import mas.vetclinic.model.entity.person.PersonRole;
import mas.vetclinic.model.entity.person.Veterinarian;
import mas.vetclinic.repository.VeterinarianRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VeterinarianService {
    private final VeterinarianRepository veterinarianRepository;

    public VeterinarianService(VeterinarianRepository veterinarianRepository) {
        this.veterinarianRepository = veterinarianRepository;
    }

    public List<Veterinarian> getAllVeterinarians() {
        return veterinarianRepository.findAll();
    }

    public Optional<Veterinarian> findById(Long id) {
        return veterinarianRepository.findById(id);
    }

    public List<Veterinarian> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return veterinarianRepository.findAll();
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length >= 2) {
            return veterinarianRepository.searchByFullName(parts[0], parts[1]);
        } else {
            return veterinarianRepository.searchBySinglePartOfName(parts[0]);
        }
    }
}
