/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.javaswift.cloudmirror.actions;

import org.javaswift.joss.model.Container;

/**
 * ContainerAction.
 * @author E.Hooijmeijer
 *
 */
public class ContainerAction {

    private final Container src;
    private final Container dst;
    private final Action action;

    public ContainerAction(Container src, Container dst, Action action) {
        this.src = src;
        this.dst = dst;
        this.action = action;
    }

    /**
     * @param action2
     * @param createContainer
     */
    public ContainerAction(ContainerAction a, Container dst) {
        this.src = a.getSrc();
        this.action = a.getAction();
        this.dst = dst;
    }

    /**
     * @return the src
     */
    public Container getSrc() {
        return src;
    }

    /**
     * @return the dst
     */
    public Container getDst() {
        return dst;
    }

    /**
     * @return the action
     */
    public Action getAction() {
        return action;
    }

}
