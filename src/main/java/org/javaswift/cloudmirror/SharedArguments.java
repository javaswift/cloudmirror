package org.javaswift.cloudmirror;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

/**
 * Parameters shared over the differend commands.
 * 
 * @author theo
 */
public class SharedArguments {

    @Parameter(names = { "-src" }, description = "The cloud to copy from. Takes 4 arguments: [AuthURL] [Tenant] [Username] [Password]", arity = 4, required = true)
    private List<String> sourceCredentials = new ArrayList<String>();

    @Parameter(names = { "-dst" }, description = "The cloud to copy to. Takes 4 arguments: [AuthURL] [Tenant] [Username] [Password]", arity = 4, required = true)
    private List<String> destCredentials = new ArrayList<String>();

    @Parameter(names = "--help", help = true)
    private boolean help;

    /**
     * @return the sourceCredentials
     */
    public List<String> getSourceCredentials() {
        return sourceCredentials;
    }

    /**
     * @return the destCredentials
     */
    public List<String> getDestCredentials() {
        return destCredentials;
    }

}
