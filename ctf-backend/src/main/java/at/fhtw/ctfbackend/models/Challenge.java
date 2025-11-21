package at.fhtw.ctfbackend.models;

import lombok.Getter;

@Getter
public class Challenge {
    private String id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer points;
    private String downloadUrl;
    private String originalFilename;

    public Challenge(String id, String title, String description,
                     String category, String difficulty, Integer points,
                     String downloadUrl, String originalFilename) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.downloadUrl = downloadUrl;
        this.originalFilename = originalFilename;
    }
}