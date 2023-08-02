package webservice.Ontology.Services;

import org.springframework.stereotype.Service;
import webservice.Ontology.DTOs.VideoTaggedDTO;
import webservice.Ontology.Models.Tag;
import webservice.Ontology.Models.Video;
import webservice.Ontology.Repos.JenaVideoRepository;

import java.util.List;

@Service
public class VideoService {



    private JenaVideoRepository jenaVideoRepository;

    public VideoService(){
        jenaVideoRepository = new JenaVideoRepository();
    }

    public List<VideoTaggedDTO> getVideosList(){

        return jenaVideoRepository.findVideos();
    }

    public void insertVideo(Video video) {

        jenaVideoRepository.createVideo(video);
    }

    public List<VideoTaggedDTO> getVideosListByTag(String tag){
       return jenaVideoRepository.findVideosByTag(tag);
    }

    public void insertTag(Tag tag){
        jenaVideoRepository.insertTag(tag);
    }

}
