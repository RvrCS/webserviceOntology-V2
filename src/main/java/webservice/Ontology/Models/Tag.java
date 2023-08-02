package webservice.Ontology.Models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Tag {

    public Tag(String artifactTag) {
        this.artifactTag = artifactTag;
    }

    private String artifactTag;

    private String timestamp;

    private String url;

}
