package com.ashokleyland.wms.service;

import com.ashokleyland.wms.model.Engine;
import com.ashokleyland.wms.repository.EngineRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements FR-06 (Bulk Excel Order Processing) / POST /api/v1/retrieval/upload-excel.
 * Reads engine numbers from the first column of a supervisor-uploaded .xlsx/.csv
 * dispatch order and validates each against the engines table.
 */
@Service
@RequiredArgsConstructor
public class BulkOrderService {

    private final EngineRepository engineRepository;

    public Map<String, Object> processUpload(MultipartFile file) throws IOException {
        List<String> requestedEngineNumbers = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) { // skip header row
                    firstRow = false;
                    continue;
                }
                Cell cell = row.getCell(0);
                if (cell == null) continue;
                String value = cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                        ? String.valueOf((long) cell.getNumericCellValue())
                        : cell.getStringCellValue();
                if (value != null && !value.isBlank()) {
                    requestedEngineNumbers.add(value.trim());
                }
            }
        }

        List<String> found = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (String engineNumber : requestedEngineNumbers) {
            java.util.Optional<Engine> engine = engineRepository.findByEngineNumber(engineNumber);
            if (engine.isPresent()) {
                found.add(engineNumber);
            } else {
                notFound.add(engineNumber);
            }
        }

        return Map.of(
                "filename", file.getOriginalFilename(),
                "total_rows", requestedEngineNumbers.size(),
                "valid_engines", found,
                "invalid_engines", notFound
        );
    }
}
