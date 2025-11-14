package com.epimorphics.lda.configs;

/**
 * A layer is pushed-down reader state: the content of the file
 * that is being included, the position in the content the
 * reader is at, and the pathname of the included file.
 */
public class Layer {
    String content;
    int contentPosition;
    String filePath;
    int lineCount;

    /**
     * Initialise a new layer with given content and
     * file path and a contentPosition of 0, ie beginning
     * of content.
     */
    Layer(String content, String filePath) {
        this.content = content;
        this.contentPosition = 0;
        this.filePath = filePath;
        this.lineCount = 0;
    }
}
