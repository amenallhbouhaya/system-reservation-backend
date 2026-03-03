package cnstn.system_de_reservation_cnstn.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create upload dir: " + root, e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) throw new RuntimeException("Empty file");

            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            original = original.replaceAll("\\s+", "_");

            String filename = UUID.randomUUID() + "_" + original;
            Path target = root.resolve(filename).normalize();

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename; // نخزّنو هذا في Document.chemin
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = root.resolve(filename).normalize();
            Resource res = new UrlResource(file.toUri());
            if (!res.exists()) throw new RuntimeException("File not found");
            return res;
        } catch (Exception e) {
            throw new RuntimeException("File not found", e);
        }
    }
}