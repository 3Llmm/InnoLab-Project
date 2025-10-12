package at.fhtw.ctfbackend.models;

import lombok.Getter;

@Getter
public class Challenge {

    private String id;
    private String name;
    private String description;
//    private String category;
//    private String difficulty;
    private String fileUrl;

    public Challenge(String id, String name, String description, String fileUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fileUrl = fileUrl;
    }
}
