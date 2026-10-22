package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class CsvMonthReader {
    private static final Pattern FILE_PATTERN = Pattern.compile("^(PNH|REP|BTB)-\\d{4}-\\d{2}-\\d{2}\\.csv$");
    private final TransactionParser parser;

    public CsvMonthReader(TransactionParser parser) {
        this.parser = parser;
    }

    public MonthData read(Path dataDir, YearMonth month) throws IOException {
        Path dir = dataDir.resolve(month.toString());
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new NoSuchFileException(dir.toString());
        }

        List<Path> files;
        try (var stream = Files.list(dir)) {
            files = stream.filter(p -> FILE_PATTERN.matcher(p.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        List<SaleTransaction> rows = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        for (Path file : files) {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }
                try {
                    rows.add(parser.parse(line));
                } catch (InvalidRowException e) {
                    rejected.add(file.getFileName() + ":" + (i + 1) + " " + e.getMessage());
                }
            }
        }

        return new MonthData(month, rows, rejected);
    }
}
