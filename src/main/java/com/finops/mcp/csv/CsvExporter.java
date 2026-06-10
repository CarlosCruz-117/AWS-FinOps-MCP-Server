package com.finops.mcp.csv;

import com.finops.mcp.model.CostRecord;
import org.apache.commons.csv.*;

import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class CsvExporter {

    public static final String OUTPUT_COST_REPORT_CSV = "output/cost-report.csv";
    public static final String DIRECTORY_NAME = "output";

    public String export(List<CostRecord> records) {
        try {
            Files.createDirectories(Path.of(DIRECTORY_NAME));
            String file = OUTPUT_COST_REPORT_CSV;

            // 1. Configuramos el formato usando el Builder (API moderna)
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader("service", "usageType", "region", "cost")
                    .build();

            // 2. Pasamos el formato ya construido al CSVPrinter
            try (CSVPrinter printer = new CSVPrinter(new FileWriter(file), format)) {
                for (CostRecord r : records) {
                    printer.printRecord(r.service(), r.usageType(), r.region(), r.cost());
                }
            }

            return file;

        } catch (Exception e) {
            throw new RuntimeException("CSV export failed", e);
        }
    }
}
