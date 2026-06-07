package mas.vetclinic.service;

import mas.vetclinic.model.entity.pet.Species;
import mas.vetclinic.repository.SpeciesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeciesService {
    private final SpeciesRepository speciesRepository;

    public SpeciesService(SpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    public List<Species> getAllSpecies() {
        return speciesRepository.findAll();
    }
}
