package com.atomatus.linq;

import junit.framework.TestCase;

public class AnalyzerTest extends TestCase {

    @SuppressWarnings("SpellCheckingInspection")
    public void testAnalyzer() {
        //Example, analyzing how many patients have obesity.
        // obs.: real data of patients with respiratory conditions between years 2018 and 2019 in Brazil.
        try(Analyzer a = Analyzer.load("https://raw.githubusercontent.com/chcmatos/nanodegree_py_analyze_srag/main/doc/influd18_limpo-final.csv")) {
            a.head();
            int count = a.get("OBESIDADE").asInteger().count(e -> e == 1 /*0 - NAO TEM OBESIDADE, 1 - TEM OBESIDADE, 4 - NAO AVALIADO*/);
            assertEquals(1466, count);
            System.out.printf("Were found %d patients having obesity.\n", count);
        }
    }
}