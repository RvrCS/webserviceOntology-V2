package webservice.Ontology.Repos;

import org.springframework.stereotype.Repository;
import webservice.Ontology.Models.Programmer;

import java.util.List;

@Repository
public interface ProgrammerRepository {
    List<Programmer> findProgrammers();
    void createProgrammer(Programmer programmer);
}
