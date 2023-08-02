package webservice.Ontology.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Video {

        private String artifactName;
        private String artifactLocation;
        private String artifactFormat;
        private List<Tag> artifactTags;
        private String isMadeBy;
        private String hasUsedIn;
        private String hasTaggedBy;
        private String isUsedBy;

    public Video(){
        this.artifactName = "";
        this.artifactLocation = "";
        this.artifactFormat = "";
        this.artifactTags = new ArrayList<Tag>();
        this.isMadeBy = "";
        this.hasUsedIn = "";
        this.hasTaggedBy = "";
        this.isUsedBy = "";
    }

    public void setArtifactTag(Tag tag){
        if(artifactTags == null){
            this.artifactTags = new ArrayList<>();
        }
            this.artifactTags.add(tag);
    }
}
