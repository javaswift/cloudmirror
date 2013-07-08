/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.javaswift.cloudmirror;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Main.
 * @author E.Hooijmeijer
 *
 */
public class Main {

    public static void main(String[] args) {
        JCommander jcm = new JCommander();
        MigrateArguments migrate = new MigrateArguments();
        jcm.addCommand("migrate", migrate);
        ListArguments list = new ListArguments();
        jcm.addCommand("list", list);

        try {
            jcm.parse(args);

            if (jcm.getParsedCommand().equals("list")) {
                new ContainerListing(list).run();
            } else if (jcm.getParsedCommand().equals("migrate")) {
                new CloudMirror(migrate).run();
            } else {
                jcm.usage();
            }
            //
        } catch (ParameterException ex) {
            System.out.println(ex.getMessage());
            jcm.usage();
        }

    }
}
