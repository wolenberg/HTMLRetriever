package com.zuniors.api.htmlretrieve.resources;


import com.zuniors.api.htmlretrieve.dtos.GambisRequestDTO;
import com.zuniors.api.htmlretrieve.dtos.GambisResponseDTO;
import com.zuniors.api.htmlretrieve.services.GambisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/page")
public class GambisResource {

  private final GambisService gambisService;

  @GetMapping("/health")
  public String healthCheck(){
    return "App OK!";
  }

  @PostMapping("/createFile")
  public GambisResponseDTO createFile(@RequestBody GambisRequestDTO gambisRequestDTO) {
    List<File> files = gambisService.generateFileOfUrl(gambisRequestDTO);
    return new GambisResponseDTO(String.format("(%s) %s", files.size(), "Arquivos gerados com sucesso!"));
  }

  @GetMapping("/viewText")
  public String viewText(@RequestParam(name = "i", defaultValue = "0", required = false) String i) throws IOException {
    List<File> files = gambisService.getFilesFolder();
    int index = Integer.parseInt(i);
    File file = files.get(index);
    Path fileName = file.toPath();
    return Files.readString(fileName);
  }
  
  @GetMapping("/download")
  public void download(HttpServletResponse response) throws IOException {
    ZipArchiveOutputStream zipArchiveOutputStream = null;
    List<File> files = gambisService.getFilesFolder();

    Map<String, byte[]> mapNameAndBytesArray = new HashMap<>();
    for (File file : files) {
      mapNameAndBytesArray.put(file.getName(), Files.readAllBytes(file.toPath()));
    }

    try {
      response.setContentType("application/zip");
      zipArchiveOutputStream = new ZipArchiveOutputStream(response.getOutputStream());
      for (Map.Entry<String, byte[]> entry : mapNameAndBytesArray.entrySet()) {
        byte[] dataToWrite = entry.getValue();
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(entry.getKey());
        zipArchiveEntry.setSize(dataToWrite.length);
        zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
        zipArchiveOutputStream.write(dataToWrite);
        zipArchiveOutputStream.closeArchiveEntry();
      }
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
          "Erro ao zipar arquivos. " + ex.getMessage());
    } finally {
      try {

        if (zipArchiveOutputStream != null) {
          zipArchiveOutputStream.flush();
          zipArchiveOutputStream.close();
        }

      } catch (IOException e) {
        throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
            "Erro ao zipar arquivos. " + e.getMessage());
      }
    }
  }

}
