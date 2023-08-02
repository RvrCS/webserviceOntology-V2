package webservice.Ontology.Utils;

public enum Constants {

    ONTOLOGY_PATH("classpath:ontology/OntologiaVideos.owl"),
    ONTOLOGY_NAMESPACE("http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#");

    private String value;

    private Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
