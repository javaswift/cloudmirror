package org.javaswift.cloudmirror;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * Arguments for the listing of the container differences. 
 * @author theo
 */
@Parameters(commandDescription = "Shows the container names which are evenly distributed over the number of groups (nr of parallel to process contaners).")
public class ListArguments {

    @ParametersDelegate
    private SharedArguments delegate = new SharedArguments();

    @Parameter(names = "-nrgroups", description = "The number of paralelle migrate processes to be used with the migrate option.")
    private int nrGroups = 1;

    /**
     * @return the sourceCredentials
     */
    public List<String> getSourceCredentials() {
        return delegate.getSourceCredentials();
    }

    /**
     * @return the destCredentials
     */
    public List<String> getDestCredentials() {
        return delegate.getDestCredentials();
    }

    /**
     * The number of groups to devide the bytes to process over
     * @return
     */
    public int getNrGroups() {
        return nrGroups;
    }

    public SharedArguments getSharedArguments() {
        return delegate;
    }

}
