package mas.vetclinic.service;

import mas.vetclinic.model.entity.person.PetOwner;
import mas.vetclinic.repository.PetOwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PetOwnerService {
    private final PetOwnerRepository petOwnerRepository;

    public PetOwnerService(PetOwnerRepository petOwnerRepository) {
        this.petOwnerRepository = petOwnerRepository;
    }

    public PetOwner findById(Long id) {
        return petOwnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
    }
}
