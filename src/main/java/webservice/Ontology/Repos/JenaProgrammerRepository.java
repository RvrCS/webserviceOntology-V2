package webservice.Ontology.Repos;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import webservice.Ontology.Models.Programmer;
import webservice.Ontology.Utils.Constants;
import webservice.Ontology.Utils.FormatOntologyString;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JenaProgrammerRepository implements ProgrammerRepository{

    private RepositoryConfig repositoryConfig;

    private OntModel model;

    private FormatOntologyString formatter;

    public JenaProgrammerRepository(){
        repositoryConfig = RepositoryConfig.getInstance();
        formatter = new FormatOntologyString();
        model = repositoryConfig.getOntModel();
    }
    @Override
    public List<Programmer> findProgrammers() {
        List<Programmer> programmers = new ArrayList<>();

        model.read(Constants.ONTOLOGY_PATH.getValue());

        String queryString = "SELECT ?programmer ?name ?email WHERE {\n" +
                "\t?programmer a <http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#Programmer> .\n" +
                "\t?programmer <http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#userName> ?name .\n" +
                "\t?programmer <http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#userEmail> ?email .\n" +
                "}";

        Query query = QueryFactory.create(queryString);
        try (QueryExecution exec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = exec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                System.out.println("Solution: "+solution.toString());
                Programmer programmer = new Programmer();
                programmer.setName(formatter.getString(solution.get("name")));
                programmer.setEmail(formatter.getString(solution.get("email")));
                programmers.add(programmer);
            }

        }catch (Exception e){
            System.out.println("Error: "+e.getMessage());
        }
        return programmers;
    }

    @Override
    public void createProgrammer(Programmer programmer) {
        // Cargar la ontología existente desde el archivo
        try {
            InputStream in = FileManager.get().open(Constants.ONTOLOGY_PATH.getValue());
            model.read(in, null);
        } catch (Exception e) {
            System.out.println("Error al cargar la ontología: " + e.getMessage());
        }

        // Crear un nuevo individuo de Programador y agregar sus propiedades
        String ns = Constants.ONTOLOGY_NAMESPACE.getValue();
        Individual programmerIndividual = model.createIndividual(ns + programmer.getName(), model.getResource(ns + "Programmer"));
        programmerIndividual.addProperty(model.getProperty(ns + "userName"), programmer.getName());
        programmerIndividual.addProperty(model.getProperty(ns + "userEmail"), programmer.getEmail());

        // Guardar la ontología de vuelta en el archivo
        try {
            FileOutputStream out = new FileOutputStream(Constants.ONTOLOGY_PATH.getValue());
            model.write(out, "RDF/XML-ABBREV");
            out.close();
        } catch (Exception e) {
            System.out.println("Error al guardar la ontología: " + e.getMessage());
        }
    }
}
