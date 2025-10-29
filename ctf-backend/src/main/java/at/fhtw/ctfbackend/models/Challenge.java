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
    private String fileUrl;

    public Challenge(String id, String title, String description,
                     String category, String difficulty, Integer points,
                     String fileUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.fileUrl = fileUrl;
    }
}