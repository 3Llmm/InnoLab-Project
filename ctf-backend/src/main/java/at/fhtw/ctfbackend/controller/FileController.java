package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.services.FileService;
import at.fhtw.ctfbackend.services.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String FILE_DIRECTORY = "/app/files";
    private final FileService fileService;
    private final FileStorageService fileStorageService;

    public FileController(FileService fileService, FileStorageService fileStorageService) {

        this.fileService = fileService;
        this.fileStorageService = fileStorageService;
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

    @PostMapping(
            path = "/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadMachineImage(
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty file"));
        }

        try {
            String path = fileStorageService.storeChallengeImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imagePath", path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to save file"));
        }
    }


}

