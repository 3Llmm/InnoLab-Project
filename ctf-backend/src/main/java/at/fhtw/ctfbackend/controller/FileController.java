package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.services.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String FILE_DIRECTORY = "/app/files";
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        Path file = Paths.get(FILE_DIRECTORY).resolve(filename).normalize();

        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(file.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank()) {
            return fileService.saveFiles();
        } else {
            return fileService.customUpload(body);
        }
    }

}

