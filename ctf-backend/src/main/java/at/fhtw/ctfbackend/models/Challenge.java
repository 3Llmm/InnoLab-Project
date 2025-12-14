package at.fhtw.ctfbackend.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Challenge {
    private String id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer points;
    private String downloadUrl;
    private String originalFilename;

    // New fields for instance-based challenges
    private Boolean solved = false;
    private Boolean requiresInstance = false;
    private String dockerImageName;

    // New fields for file storage
    private String challengeFolderPath;
    private String dockerFilesJson;

    // Updated constructor with all fields
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

    // Optional: Add a full constructor if needed
    public Challenge(String id, String title, String description,
                     String category, String difficulty, Integer points,
                     String downloadUrl, String originalFilename,
                     Boolean requiresInstance, String dockerImageName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.downloadUrl = downloadUrl;
        this.originalFilename = originalFilename;
        this.requiresInstance = requiresInstance != null ? requiresInstance : false;
        this.dockerImageName = dockerImageName;
    }

    // Additional constructor with all fields
    public Challenge(String id, String title, String description,
                     String category, String difficulty, Integer points,
                     String downloadUrl, String originalFilename,
                     Boolean requiresInstance, String dockerImageName,
                     String challengeFolderPath,
                     String dockerFilesJson) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.downloadUrl = downloadUrl;
        this.originalFilename = originalFilename;
        this.requiresInstance = requiresInstance != null ? requiresInstance : false;
        this.dockerImageName = dockerImageName;
        this.challengeFolderPath = challengeFolderPath;
        this.dockerFilesJson = dockerFilesJson;
    }
}