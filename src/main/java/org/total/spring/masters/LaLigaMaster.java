package org.total.spring.masters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.total.spring.dao.ResultDAO;
import org.total.spring.entity.Result;
import org.total.spring.entity.enums.SeasonCode;
import org.total.spring.entity.enums.TournamentCode;
import org.total.spring.finder.DataFinder;

import java.util.SortedSet;

/**
 * @author Pavlo.Fandych
 */

@Component("laLigaMaster")
public final class LaLigaMaster implements Master {

    @Autowired
    private DataFinder dataFinderSpainPrimera;

    @Autowired
    private ResultDAO resultDAO;

    public DataFinder getDataFinderSpainPrimera() {
        return dataFinderSpainPrimera;
    }

    public void setDataFinderSpainPrimera(DataFinder dataFinderSpainPrimera) {
        this.dataFinderSpainPrimera = dataFinderSpainPrimera;
    }

    public ResultDAO getResultDAO() {
        return resultDAO;
    }

    public void setResultDAO(ResultDAO resultDAO) {
        this.resultDAO = resultDAO;
    }

    @Override
    public void populateResults() {
        final SortedSet<Result> savedResults = getResultDAO().results();
        for (Result item : getDataFinderSpainPrimera().findResults()) {
            if (!savedResults.contains(item)) {
                getResultDAO().insertResult(item);
            }
        }
    }

    public void populateResults(final SeasonCode seasonCode) {
        final SortedSet<Result> savedResults = getResultDAO()
                .findResultsBySeasonCodeAndTournamentCode(seasonCode, TournamentCode.ESP_PRIMERA);

        getDataFinderSpainPrimera().findResults().stream().filter(result -> !savedResults.contains(result))
                .forEach(getResultDAO()::insertResult);
    }
}