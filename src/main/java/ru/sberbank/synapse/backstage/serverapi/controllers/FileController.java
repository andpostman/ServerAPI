package ru.sberbank.synapse.backstage.serverapi.controllers;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.sberbank.synapse.backstage.serverapi.commons.FileResponse;
import ru.sberbank.synapse.backstage.serverapi.storage.StorageService;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Controller
public class FileController {

    private StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    @ConfigurationProperties(prefix = "spring.thymeleaf")
    public String listAllFiles(Model model){
        model.addAttribute("files",storageService.loadAll().map(
                path -> ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(path.getFileName().toString())
                .toUriString())
                .collect(Collectors.toList()));
        return "listFiles";
    }

    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName){
        Resource resource = storageService.loadAsResource(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/upload-file")
    @ResponseBody
    public FileResponse uploadFile(@RequestParam("file")MultipartFile file){
        String name = storageService.store(file);
        String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(name)
                .toUriString();
        return new FileResponse(name, uri, file.getContentType(), file.getSize());
    }

    @PostMapping("/upload-multiple-files")
    @ResponseBody
    public List<FileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files){
        return Arrays.stream(files)
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }

    @PostMapping("/delete-file")
    @ResponseBody
    public String deleteFile(@PathVariable String fileName, Model model){
        storageService.deleteFileByName(fileName);
        model.addAttribute("deletedFile",fileName);
        return "listAfterDelete";
    }


}
