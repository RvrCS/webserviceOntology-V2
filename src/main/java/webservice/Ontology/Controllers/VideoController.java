package webservice.Ontology.Controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webservice.Ontology.DTOs.VideoTaggedDTO;
import webservice.Ontology.Models.Tag;
import webservice.Ontology.Models.Video;
import webservice.Ontology.Services.VideoService;

import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class VideoController {


    private VideoService videoService;

    public VideoController(){
        videoService = new VideoService();
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoTaggedDTO>> getVideos() {

        List<VideoTaggedDTO> videos = videoService.getVideosList();

        return ResponseEntity.ok(videos != null ? videos : Collections.emptyList());
    }

    @GetMapping("/videos/{tag}")
    public ResponseEntity<List<VideoTaggedDTO>> getVideosByTag(@PathVariable String tag){
       List<VideoTaggedDTO> videos = videoService.getVideosListByTag(tag);

       if (videos == null || videos.isEmpty()){
           return ResponseEntity.notFound().build();
       }

       return ResponseEntity.ok(videos);
    }

    @PutMapping("/videos/tag")
    public ResponseEntity<String> insertTag(@RequestBody Tag tag) {

        videoService.insertTag(tag);
        return ResponseEntity.ok("Tag insertado exitosamente");
    }

    @PostMapping("/videos/save")
    public ResponseEntity<Video> saveVideo(@RequestBody Video video){
        videoService.insertVideo(video);
        return ResponseEntity.status(HttpStatus.CREATED).body(video);
    }


}
