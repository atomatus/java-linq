package com.atomatus.linq;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Analyze a spreadsheet file, how like a data table.
 * How like, data frames, to modeling data and generating results.
 * @author Carlos Matos
 */
public abstract class Analyzer extends IterableResultGroup<String, String> implements Closeable {

    protected static final char EMPTY_SEPARATOR_CHAR;

    private String filename;
    private Charset charset;
    private char separatorChar;
    private boolean closed;
    private final boolean localFile;
    private final boolean requestSeparatorChar;

    //region load
    private enum FileType {
        CSV;

        static FileType fromName(String name){
            for (FileType t : values()) {
                if(t.name().equalsIgnoreCase(name)){
                    return t;
                }
            }
            throw new RuntimeException(String.format("File type %1$s is not valid!", name));
        }
    }

    private static FileType getFileTypeFromFilename(String filename){
        int index = filename.lastIndexOf('.');
        if(index == -1) {
            return FileType.CSV; //default file type.
        } else {
            String ext = filename.substring(index + 1);
            return FileType.fromName(ext);
        }
    }

    /**
     * Load spreadsheet (data frame) from file.
     * @param filename data frame file name full path.
     * @return instance of Analyzer.
     */
    public static Analyzer load(String filename) {
        return load(getFileTypeFromFilename(filename), filename, EMPTY_SEPARATOR_CHAR);
    }

    /**
     * Load spreadsheet (data frame) from file.
     * @param filename data frame file name full path.
     * @param separatorChar identify separator char to split values.
     * @return instance of Analyzer.
     */
    public static Analyzer load(String filename, char separatorChar) {
        return load(getFileTypeFromFilename(filename), filename, separatorChar);
    }

    /**
     * Load spreadsheet (data frame) from file.
     * @param type data frame file type.
     * @param filename data frame file name full path.
     * @param separatorChar identify separator char to split values.
     * @return instance of Analyzer.
     */
    private static Analyzer load(FileType type, String filename, char separatorChar) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (type){
            case CSV:
                return new AnalyzerCSV(filename, separatorChar);
            default:
                throw new UnsupportedOperationException();
        }
    }
    //endregion

    static {
        EMPTY_SEPARATOR_CHAR = '\0';
    }

    protected Analyzer(String filename, char separatorChar, boolean requestSeparatorChar) {
        this.localFile              = isLocalFile(filename);
        this.filename               = localFile ? requestFileExists(filename) : filename;
        this.separatorChar          = separatorChar;
        this.requestSeparatorChar   = requestSeparatorChar;
        this.charset                = Charset.defaultCharset();
    }

    protected Analyzer(String filename) {
        this(filename, EMPTY_SEPARATOR_CHAR, false);
    }

    //region filename and separatorChar
    protected boolean isLocalFile() {
        return localFile;
    }

    protected String getFilename() {
        this.requireNonClosed();
        return filename;
    }

    protected Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = Objects.requireNonNull(charset);
    }

    protected char getSeparatorChar() {
        this.requireNonClosed();
        return separatorChar;
    }

    protected void setSeparatorChar(char separatorChar){
        this.separatorChar = separatorChar;
    }

    protected boolean hasNotSeparatorChar() {
        return separatorChar == EMPTY_SEPARATOR_CHAR;
    }

    protected boolean isRequestSeparatorChar() {
        return requestSeparatorChar;
    }

    protected String requestFileExists(String filename) {
        File file = new File(filename);
        if(!file.exists()) {
            throw new RuntimeException(new FileNotFoundException("File not exists!"));
        } else if(!file.canRead()) {
            throw new RuntimeException("No file read permission!");
        } else{
            return filename;
        }
    }

    private boolean isLocalFile(String filename) {
        String aux = Objects.requireNonNull(filename).toLowerCase();
        String[] schemes = new String[]{ "https://", "http://", "file://" };
        for(String s : schemes) {
            if(aux.startsWith(s)) {
                return false;
            }
        }
        return true;
    }
    //endregion

    //region Closeable
    protected final void requireNonClosed(){
        if(closed){
            throw new UnsupportedOperationException("Analyzer was closed and disposed!");
        }
    }

    protected void onClose() { }

    @Override
    public final void close() {
        if(!closed) {
            onClose();
            closed = true;
            separatorChar = EMPTY_SEPARATOR_CHAR;
            filename = null;
        }
    }
    //endregion
}
