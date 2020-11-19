package com.atomatus.linq;

/**
 * Analyzer for CSV file.
 * @author Carlos Matos
 */
final class AnalyzerCSV extends AnalyzerForSepChar {

    public AnalyzerCSV(String filename, char separatorChar) {
        super(filename, separatorChar, new char[]{ ',', ';' });
    }
}
