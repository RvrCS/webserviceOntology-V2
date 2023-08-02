package webservice.Ontology.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class TagTimestamp {


    private String tag;

    private String timestamp;

    public TagTimestamp(){
        this.tag = "";
        this.timestamp = "";
    }

}
