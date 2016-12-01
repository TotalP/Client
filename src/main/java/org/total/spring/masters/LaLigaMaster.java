package org.total.spring.masters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.total.spring.dao.ResultDAO;
import org.total.spring.entity.Result;
import org.total.spring.finder.DataFinder;

import java.util.TreeSet;

/**
 * Created by pavlo.fandych on 11/29/2016.
 */

@Component("laLigaMaster")
public class LaLigaMaster implements Master {
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
        TreeSet<Result> savedResults = getResultDAO().results();
        for (Result item : getDataFinderSpainPrimera().findResults()) {
            if (!savedResults.contains(item)) {
                getResultDAO().insertResult(item);
            }
        }
    }
}