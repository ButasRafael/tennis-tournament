package org.example.tennistournament.export;

import org.example.tennistournament.model.TennisMatch;
import java.util.List;
import java.util.stream.Collectors;

public class CSVExportStrategy implements ExportStrategy {

    @Override
    public String export(List<TennisMatch> matches) {
        String header = "MatchID,Tournament,Player1,Player2,Referee,Score,MatchDate";
        String body = matches.stream().map(match ->
                match.getId() + "," +
                        (match.getTournament() != null ? match.getTournament().getName() : "") + "," +
                        (match.getPlayer1() != null ? match.getPlayer1().getUsername() : "") + "," +
                        (match.getPlayer2() != null ? match.getPlayer2().getUsername() : "") + "," +
                        (match.getReferee() != null ? match.getReferee().getUsername() : "") + "," +
                        match.getScore() + "," +
                        match.getStartTime()
        ).collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }
}
