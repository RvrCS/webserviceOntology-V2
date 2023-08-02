package webservice.Ontology.Utils;


import org.apache.jena.rdf.model.RDFNode;
import org.springframework.stereotype.Component;

@Component
public class FormatOntologyString {
    public String getString(RDFNode value) {
        if(value == null)
            return "";

        String stringValue = value.toString();

        if (stringValue.contains("^^")) {
            return value.toString().split("\\^")[0];
        } else if (stringValue.contains("#")) {
            return value.toString().split("#")[1];
        } else {
            return stringValue;
        }
//        if(stringValue.contains("^^"))
//            return value.toString().split("\\^")[0];
//        else
//            return value.toString().split("#")[1];
    }
}
