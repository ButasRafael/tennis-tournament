package org.example.tennistournament.export;

import org.example.tennistournament.model.TennisMatch;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExportService {

    private ExportStrategy strategy;

    public void setStrategy(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public String exportMatches(List<TennisMatch> matches) {
        if (strategy == null) {
            throw new RuntimeException("No export strategy selected");
        }
        return strategy.export(matches);
    }
}
