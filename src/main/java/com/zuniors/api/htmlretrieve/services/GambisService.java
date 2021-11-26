package com.zuniors.api.htmlretrieve.services;

import com.zuniors.api.htmlretrieve.dtos.GambisRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GambisService {

  private static final String FILE_FOLDER = "temp";
  private static final String FILE_NAME = "page";
  private static final String PATH_NAME = String.format("%s/%s", FILE_FOLDER, FILE_NAME);

  private void createFile(final String fileName) {
    try {
      File file = new File(fileName);
      if (file.createNewFile()) {
        log.info("Arquivo criado: {}", file.getName());
      } else {
        log.info("Arquivo já existe");
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  private void writeFile(final String fileName, final String fileContent) {
    try (FileWriter fileWriter = new FileWriter(fileName)) {
      fileWriter.write(fileContent);
    } catch (IOException e) {
      log.error(e.getMessage());
    }
  }

  private File generateFile(final String fileContent, final int order) {
    final String pathName = String.format("%s_%s.html", PATH_NAME, order);
    log.info("Arquivo sera criado em: {}", pathName);
    createFile(pathName);
    writeFile(pathName, fileContent);
    return new File(pathName);
  }

  private String getTextOfPage(final String url) {
    log.info("Acessando {}", url);
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(url, String.class);
  }

  public List<File> generateFileOfUrl(GambisRequestDTO gambisRequestDTO) {
    List<File> files = new ArrayList<>();
    for (int i = 0; i < gambisRequestDTO.getLinks().size(); i++) {
      final String response = getTextOfPage(gambisRequestDTO.getLinks().get(i));
      files.add(generateFile(response, i));
    }
    return files;
  }

  public List<File> getFilesFolder() {
    File folder = new File(FILE_FOLDER);
    File[] listOfFiles = folder.listFiles();
    List<File> files = new ArrayList<>();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        files.add(file);
      }
    }
    return files;
  }

}
