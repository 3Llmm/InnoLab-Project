package at.fhtw.ctfbackend.models;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Category {
    private String id;
    private String name;
    private String summary;
    private String fileUrl;

    public Category(String id, String name, String summary, String fileUrl) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.fileUrl = fileUrl;
    }


}


