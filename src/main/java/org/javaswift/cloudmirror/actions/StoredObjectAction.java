/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.javaswift.cloudmirror.actions;

import org.javaswift.joss.model.StoredObject;

/**
 * StoredObjectAction.
 * @author E.Hooijmeijer
 *
 */
public class StoredObjectAction {

    private final StoredObject src;
    private final StoredObject dst;
    private final Action action;

    public StoredObjectAction(StoredObject src, StoredObject dst, Action action) {
        this.src = src;
        this.dst = dst;
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public StoredObject getDst() {
        return dst;
    }

    public StoredObject getSrc() {
        return src;
    }

}
