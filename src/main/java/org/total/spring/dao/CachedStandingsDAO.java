package org.total.spring.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.total.spring.entity.Season;
import org.total.spring.entity.Tournament;
import org.total.spring.http.HttpExecutor;
import org.total.spring.util.Constants;
import org.total.spring.util.PasswordManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by pavlo.fandych on 12/1/2016.
 */

@Repository("cachedStandingsDAO")
public class CachedStandingsDAO extends GenericDAO {
    @Autowired
    private HttpExecutor httpExecutor;

    @Autowired
    private PasswordManager passwordManager;

    @Autowired
    private SeasonDAO seasonDAO;

    @Autowired
    private TournamentDAO tournamentDAO;

    public HttpExecutor getHttpExecutor() {
        return httpExecutor;
    }

    public void setHttpExecutor(HttpExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    public PasswordManager getPasswordManager() {
        return passwordManager;
    }

    public void setPasswordManager(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
    }

    public SeasonDAO getSeasonDAO() {
        return seasonDAO;
    }

    public void setSeasonDAO(SeasonDAO seasonDAO) {
        this.seasonDAO = seasonDAO;
    }

    public TournamentDAO getTournamentDAO() {
        return tournamentDAO;
    }

    public void setTournamentDAO(TournamentDAO tournamentDAO) {
        this.tournamentDAO = tournamentDAO;
    }

    public void saveStandings(String seasonCode, String tournamentCode) {
        try {
            Properties credentials = new Properties();
            credentials.load(CachedStandingsDAO.class.getClassLoader()
                    .getResourceAsStream("credentials.properties"));

            String user = credentials.getProperty("user");
            String userpass = credentials.getProperty("userpass");

            if (seasonCode != null
                    && !seasonCode.isEmpty()
                    && tournamentCode != null
                    && !tournamentCode.isEmpty()
                    && user != null
                    && !user.isEmpty()
                    && userpass != null
                    && !userpass.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("?seasonCode=")
                        .append(seasonCode)
                        .append("&tournamentCode=")
                        .append(tournamentCode);

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", Constants.CONTENT_TYPE_APPLICATION_JSON);
                headers.put("Version", "V1");
                headers.put("Authorization", getPasswordManager().encodeBase64(user + ":" + userpass));

                String standingsText = getHttpExecutor().executeGet(Constants.URL_STANDINGS, headers, builder.toString());

                Season season = getSeasonDAO().fetchSeasonBySeasonCode(seasonCode);
                Tournament tournament = getTournamentDAO().fetchTournamentByTournamentCode(tournamentCode);

                if (season != null
                        && season.getSeasonId() > 0
                        && tournament.getTournamentId() > 0
                        && tournament != null
                        && tournament.getTournamentId() > 0
                        && standingsText != null
                        && !standingsText.isEmpty()) {
                    LOGGER.info("SeasonId = " + season.getSeasonId() + "" +
                            " TournamentId = " + tournament.getTournamentId());
                    LOGGER.info("isStandingExists = " +
                            isStandingExists(season.getSeasonId(), tournament.getTournamentId()));

                    if (!isStandingExists(season.getSeasonId(), tournament.getTournamentId())) {
                        LOGGER.info("saving..." + standingsText);
                        getJdbcTemplate()
                                .update(Constants.INSERT_CACHED_STANDINGS,
                                        new Object[]{season.getSeasonId(),
                                                tournament.getTournamentId(),
                                                standingsText});
                    } else {
                        LOGGER.info("updating..." + standingsText);
                        getJdbcTemplate()
                                .update(Constants.UPDATE_CACHED_STANDINGS,
                                        standingsText,
                                        season.getSeasonId(),
                                        tournament.getTournamentId());
                    }
                } else {
                    LOGGER.error("SeasonId = " + season.getSeasonId() + " tournamentId = "
                            + tournament.getTournamentId() + standingsText);
                }
            } else {
                LOGGER.error("Invalid input parameters");
            }
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }

    private boolean isStandingExists(Long seasonId, Long tournamentId) {
        boolean result = false;
        int count = getJdbcTemplate()
                .queryForObject(Constants.COUNT_CACHED_STANDINGS,
                        new Object[]{seasonId,
                                tournamentId}, Integer.class);
        if (count > 0) {
            result = true;
        }
        return result;
    }
}