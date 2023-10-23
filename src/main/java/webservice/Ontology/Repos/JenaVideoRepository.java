package webservice.Ontology.Repos;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;
import webservice.Ontology.DTOs.VideoTaggedDTO;
import webservice.Ontology.Models.Tag;
import webservice.Ontology.Models.TagTimestamp;
import webservice.Ontology.Models.Video;
import webservice.Ontology.Utils.Constants;
import webservice.Ontology.Utils.FormatOntologyString;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class JenaVideoRepository implements VideoRepository {

    // Constants
    public static final String videoUri = "http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#Videos";
    public static final String locationProperty = "http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#artifactLocation";
    public static final String tagProperty = "http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#artifactTag";

    @Autowired
    private FormatOntologyString formatter;

    @Value("${ontology.owl.file-path}")
    private String ontologyFilePath;

    private OntModel model;

    private OntModel getModel() {
        if (model == null) {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
            try (InputStream in = new FileInputStream(ontologyFilePath)) {
                model.read(in, null);
            } catch (Exception e) {
                throw new RuntimeException("Error while reading ontology: " + e.getMessage());
            }
        }
        return model;
    }

    @Override
    public List<VideoTaggedDTO> findVideos() {
        List<VideoTaggedDTO> videosList = new ArrayList<>();

        OntModel model = getModel();

        String queryString = String.format("SELECT ?artifactLocation ?artifactTag WHERE { " +
                "?video a <%s> . " +
                "?video <%s> ?artifactLocation . " +
                "?video <%s> ?artifactTag . " +
                "}", videoUri, locationProperty, tagProperty);

        Query query = QueryFactory.create(queryString);
        try (QueryExecution exec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = exec.execSelect();
            Map<String, VideoTaggedDTO> videosMap = new HashMap<>();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                RDFNode locationNode = solution.get("artifactLocation");
                RDFNode tagsNode = solution.get("artifactTag");

                if (locationNode != null && locationNode.isLiteral() && tagsNode != null && tagsNode.isLiteral()) {
                    String artifactLocation = ((Literal) locationNode).getString();
                    String artifactTag = ((Literal) tagsNode).getString();

                    String[] tagsWithTimestamp = artifactTag.split("\\/", 2);
                    if (tagsWithTimestamp.length == 2) {

                        String tag = tagsWithTimestamp[0];
                        String timestamp = tagsWithTimestamp[1];

                        VideoTaggedDTO videoTaggedDTO = videosMap.get(artifactLocation);
                        if (videoTaggedDTO == null) {
                            videoTaggedDTO = new VideoTaggedDTO();
                            videoTaggedDTO.setArtifactLocation(artifactLocation);
                            videosMap.put(artifactLocation, videoTaggedDTO);
                        }

                        TagTimestamp tagTimestamp = new TagTimestamp();
                        tagTimestamp.setTag(tag);
                        tagTimestamp.setTimestamp(timestamp);
                        videoTaggedDTO.getArtifactTagsTimestamp().add(tagTimestamp);
                    }
                }
            }
            videosList.addAll(videosMap.values());
            results.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while finding videos: " + e.getMessage(), e);
        }
        return videosList;
    }

    @Override
    public void createVideo(Video video) {

        OntModel model = getModel();

        String ns = Constants.ONTOLOGY_NAMESPACE.getValue();


        String videoUri = ns + video.getArtifactLocation();
        Individual videoIndividual = model.createIndividual(videoUri, model.getResource(ns + "Videos"));


        StmtIterator tagStmts = videoIndividual.listProperties(model.getProperty(ns + "artifactTag"));
        while (tagStmts.hasNext()) {
            Statement stmt = tagStmts.next();
            stmt.remove();
        }

        videoIndividual.addProperty(model.getProperty(ns + "artifactLocation"), video.getArtifactLocation());
        videoIndividual.addProperty(model.getProperty(ns + "artifactFormat"), video.getArtifactFormat());

        for (Tag tag : video.getArtifactTags()) {
            String artifactTagWithTimestamp = tag.getArtifactTag().toLowerCase() + " / " + tag.getTimestamp();
            videoIndividual.addProperty(model.getProperty(ns + "artifactTag"), artifactTagWithTimestamp);
        }

        Individual isMadeByIndividual = model.getIndividual(ns + video.getIsMadeBy());
        if (isMadeByIndividual == null) {
            isMadeByIndividual = model.createIndividual(ns + video.getIsMadeBy(), model.getResource(ns + "Programmer"));
        }
        videoIndividual.addProperty(model.getProperty(ns + "isMadeBy"), isMadeByIndividual);

        Individual isUsedByIndividual = model.getIndividual(ns + video.getIsUsedBy());
        if (isUsedByIndividual == null) {
            isUsedByIndividual = model.createIndividual(ns + video.getIsUsedBy(), model.getResource(ns + "Programmer"));
        }
        videoIndividual.addProperty(model.getProperty(ns + "isUSedBy"), isUsedByIndividual);

        Resource videohasUsedInResource = model.createResource(ns + video.getHasUsedIn());
        videoIndividual.addProperty(model.getProperty(ns + "hasUsedIn"), videohasUsedInResource);
        Resource videohasTaggedByResource = model.createResource(ns + video.getHasTaggedBy());
        videoIndividual.addProperty(model.getProperty(ns + "hasTaggedBy"), videohasTaggedByResource);

        try (FileOutputStream out = new FileOutputStream(ontologyFilePath)){
            model.write(out, "RDF/XML-ABBREV");
            System.out.println("Ontología guardada en: " + ontologyFilePath);
        } catch (Exception e) {
            System.out.println("Error al guardar la ontología: " + e.getMessage());
        }

    }

    @Override
    public void insertTag(Tag tag) {
        String artifactTag = tag.getArtifactTag().toLowerCase();
        String timestamp = tag.getTimestamp().toString();
        String artifactLocation = tag.getUrl();

        OntModel model = getModel();

        String ns = Constants.ONTOLOGY_NAMESPACE.getValue();

        String queryString = "SELECT ?video WHERE { "
                + "?video a <http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#Videos> . "
                + "?video <http://www.semanticweb.org/jose/ontologies/2019/4/untitled-ontology-24#artifactLocation> ?artifactLocation . "
                + "FILTER (?artifactLocation = \"" + artifactLocation + "\")"
                + "}";
        Query query = QueryFactory.create(queryString);
        try (QueryExecution exec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = exec.execSelect();
            if (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Resource videoResource = solution.getResource("video");
                Individual videoIndividual = model.getIndividual(videoResource.getURI());

                videoIndividual.addProperty(model.createProperty(ns + "artifactTag"), (artifactTag + " / " + timestamp));

                try (FileOutputStream out = new FileOutputStream(ontologyFilePath)) {
                    model.write(out, "RDF/XML-ABBREV");
                    System.out.println("Ontología actualizada y guardada en: " + ontologyFilePath);
                } catch (Exception e) {
                    System.out.println("Error al guardar la ontología: " + e.getMessage());
                }

            } else {
                System.out.println("El Video no existe en la ontología.");
            }
            results.close();
        } catch (Exception e) {
            System.out.println("Error al cargar la ontología: " + e.getMessage());
        }
    }

    @Override
    public List<VideoTaggedDTO> findVideosByTag(String tag) {
        List<VideoTaggedDTO> videosList = new ArrayList<>();

        OntModel model = getModel();



        String queryString = String.format("SELECT ?artifactLocation ?artifactTag WHERE { " +
                "?video a <%s> . " +
                "?video <%s> ?artifactLocation . " +
                "?video <%s> ?artifactTag . " +
                "FILTER (CONTAINS(?artifactTag, \"%s\"))" +
                "}", videoUri, locationProperty, tagProperty, tag.toLowerCase());

        Query query = QueryFactory.create(queryString);
        try (QueryExecution exec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = exec.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();

                RDFNode locationNode = solution.get("artifactLocation");
                RDFNode tagsNode = solution.get("artifactTag");

                if (locationNode != null && locationNode.isLiteral() && tagsNode != null && tagsNode.isLiteral()) {
                    String artifactLocation = ((Literal) locationNode).getString();
                    String artifactTag = ((Literal) tagsNode).getString();

                    String[] tagsWithTimestamp = artifactTag.split("/", 2);
                    String tagValue = tagsWithTimestamp[0];
                    String timestamp = tagsWithTimestamp[1];

                    VideoTaggedDTO videoTaggedDTO = new VideoTaggedDTO();
                    videoTaggedDTO.setArtifactLocation(artifactLocation);

                    TagTimestamp tagTimestamp = new TagTimestamp();
                    tagTimestamp.setTag(tagValue.trim());
                    tagTimestamp.setTimestamp(timestamp.trim());
                    videoTaggedDTO.getArtifactTagsTimestamp().add(tagTimestamp);

                    videosList.add(videoTaggedDTO);
                }
            }
            results.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while finding videos by tag: " + e.getMessage(), e);
        }

        return videosList;


    }


}
