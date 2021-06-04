package com.atomatus.linq;

import junit.framework.TestCase;

public class AnalyzerTestOfWholeSaleCustomersData extends TestCase {

    public void testClustering() {
        //see below the full article using same sample.
        //https://pauloesampaio.medium.com/entendendo-k-means-agrupando-dados-e-tirando-camisas-e90ae3157c17

        //todo implement method to clustering (K-Means)
        //to find optimal number of clusters
        //https://medium.com/pizzadedados/kmeans-e-metodo-do-cotovelo-94ded9fdf3a9
        try(Analyzer a = Analyzer.load("https://archive.ics.uci.edu/ml/machine-learning-databases/00292/Wholesale%20customers%20data.csv")) {
            a.head();
        }
    }

}
