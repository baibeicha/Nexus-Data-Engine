package by.nexus.data.processor.service;

import by.nexus.data.processor.event.ImportRequestEvent;

import java.io.File;

public interface DataExtractionService {
    File extractAndConvert(ImportRequestEvent event) throws Exception;
}
