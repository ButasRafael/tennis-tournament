package org.example.tennistournament.export;

import org.example.tennistournament.model.TennisMatch;
import java.util.List;

public interface ExportStrategy {
    String export(List<TennisMatch> matches);
}
