package demo;

import java.io.IOException;
import java.io.OutputStream;

import net.schmizz.sshj.xfer.InMemoryDestFile;

/**
 * www.brainliner.jp
 * 
 * @author Makoto Takemiya<br />
 * 
 * ATR - Computational Neuroscience Laboratories, Department of Neuroinformatics
 *
 * @version 2012/03/22
 */
public class StreamingInMemoryDestFile extends InMemoryDestFile {

    private OutputStream os;
 
    /**
     * @param os
     */
    public StreamingInMemoryDestFile(OutputStream os) {
       super();
       this.os = os;
    }
 
    /*
     * (non-Javadoc)
     * @see net.schmizz.sshj.xfer.LocalDestFile#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
       return os;
    }
 
 }