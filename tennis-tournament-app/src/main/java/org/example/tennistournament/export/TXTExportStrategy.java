package org.example.tennistournament.export;

import org.example.tennistournament.model.TennisMatch;
import java.util.List;
import java.util.stream.Collectors;

public class TXTExportStrategy implements ExportStrategy {

    @Override
    public String export(List<TennisMatch> matches) {
        return matches.stream().map(match ->
                "Match ID: " + match.getId() +
                        ", Tournament: " + (match.getTournament() != null ? match.getTournament().getName() : "") +
                        ", Player1: " + (match.getPlayer1() != null ? match.getPlayer1().getUsername() : "") +
                        ", Player2: " + (match.getPlayer2() != null ? match.getPlayer2().getUsername() : "") +
                        ", Referee: " + (match.getReferee() != null ? match.getReferee().getUsername() : "") +
                        ", Score: " + match.getScore() +
                        ", Date: " + match.getStartTime()
        ).collect(Collectors.joining("\n"));
    }
}
