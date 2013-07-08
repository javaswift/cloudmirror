/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.javaswift.cloudmirror;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * Arguments.
 * @author E.Hooijmeijer
 *
 */
@Parameters(commandDescription = "Migrates the src and dst object stores.")
public class MigrateArguments {

    @ParametersDelegate
    private SharedArguments delegate = new SharedArguments();

    @Parameter(names = "-dry", description = "Do not perform any actions, just log what would be done.")
    private boolean dryRun = false;

    @Parameter(names = "-total", description = "Total bytes value for progress indication.")
    private long total = -1;

    @Parameter(names = "-cont", description = "A comma separated list of container names to process (see the \"list\" command).", variableArity = true, required = true)
    private List<String> contanerNamesToProcess = new ArrayList<String>();

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
     * @return the dryRun
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * @return
     */
    public long getTotal() {
        return total;
    }

    public SharedArguments getSharedArguments() {
        return delegate;
    }

    public List<String> getContanerNamesToProcess() {
        return contanerNamesToProcess;
    }

}
