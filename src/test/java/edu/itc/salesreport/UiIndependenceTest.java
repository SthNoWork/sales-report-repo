package edu.itc.salesreport;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UiIndependenceTest {

    private static final List<String> FORBIDDEN_PREFIXES = List.of(
            "import javafx.",
            "import java.awt.",
            "import javax.swing."
    );

    private static final List<String> TOOLKIT_FREE_PACKAGES = List.of(
            "src/main/java/edu/itc/salesreport/model",
            "src/main/java/edu/itc/salesreport/ingest",
            "src/main/java/edu/itc/salesreport/engine",
            "src/main/java/edu/itc/salesreport/ui",
            "src/main/java/edu/itc/salesreport/ui/mvp",
            "src/main/java/edu/itc/salesreport/ui/console"
    );

    @Test
    void corePackagesDoNotImportUiToolkits() throws IOException {
        for (String pkgPath : TOOLKIT_FREE_PACKAGES) {
            Path dir = Path.of(pkgPath);
            if (!Files.exists(dir)) {
                continue;
            }
            try (var stream = Files.list(dir)) {
                List<Path> javaFiles = stream.filter(p -> p.toString().endsWith(".java")).toList();
                for (Path file : javaFiles) {
                    List<String> lines = Files.readAllLines(file);
                    for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
                        String trimmed = lines.get(lineNum).strip();
                        for (String forbidden : FORBIDDEN_PREFIXES) {
                            boolean hasForbiddenImport = trimmed.startsWith(forbidden);
                            assertTrue(!hasForbiddenImport,
                                    "Forbidden UI toolkit import in %s:%d: %s".formatted(
                                            file, lineNum + 1, trimmed));
                        }
                    }
                }
            }
        }
    }
}
