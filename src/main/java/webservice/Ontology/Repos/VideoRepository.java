package webservice.Ontology.Repos;

import org.springframework.stereotype.Repository;
import webservice.Ontology.DTOs.VideoTaggedDTO;
import webservice.Ontology.Models.Tag;
import webservice.Ontology.Models.Video;

import java.util.List;

@Repository
public interface VideoRepository {

    List<VideoTaggedDTO> findVideos();

    void createVideo(Video video);

    void insertTag(Tag tag);

    List<VideoTaggedDTO> findVideosByTag(String tag);

}
