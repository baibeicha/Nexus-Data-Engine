package by.nexus.data.processor.service;

import by.nexus.data.processor.event.ImportRequestEvent;

public interface ImportProcessorService {
    void handleImportRequest(ImportRequestEvent requestEvent);
}
