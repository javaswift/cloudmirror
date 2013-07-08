/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.javaswift.cloudmirror;

import java.util.ArrayList;
import java.util.List;

import org.javaswift.cloudmirror.actions.Action;
import org.javaswift.cloudmirror.actions.ContainerAction;
import org.javaswift.cloudmirror.actions.StoredObjectAction;

import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CloudMirror.
 * @author E.Hooijmeijer
 *
 * Stappen:
 * - uitlezen containers, stored objects, MD5 hashes, bytes van source tenant
 * - uitlezen containers, stored objects, MD5 hashes van target tenant
 * - uitrekenen migratie plan (te migreren bytes) tbv progress indicator
 * i) als target container niet bestaat -> migreren
 * ii) als target object niet bestaat -> migreren
 * iii) als target object een andere MD5 hash heeft -> migreren
 * - voor ieder te migreren object
 * i) check of de target container bestaat -> aanmaken target container
 * ii) upload het source object naar het target object (streaming)
 * 
 * Gedurende het proces wordt een SLF4J log geschreven waarin het volgende wordt bijgehouden:
 * - het percentage van de voortgang
 * - downloads of uploads die verkeerd zijn gegaan
 */
public class CloudMirror extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CloudMirror.class);

    private List<String> containerNamesToProcess;

    public CloudMirror(MigrateArguments args) {
        super(args.getSharedArguments(), args.isDryRun(), args.getTotal());
        this.containerNamesToProcess = args.getContanerNamesToProcess();
    }

    public void run() {
        for (ContainerAction action : diffContainers(src.listContainers(containerNamesToProcess), dst.listContainers())) {
            switch (action.getAction()) {
            case SKIP:
                break;
            case SYNC:
                sync(action);
                break;
            case COPY_SRC_TO_DST:
                sync(create(action));
                break;
            case DELETE_DST:
                delete(action);
                break;
            }
        }
        dst.report();
    }

    /**
     * @param action
     */
    private void delete(ContainerAction action) {
        dst.deleteContainer(action.getDst());
    }

    /**
     * @param action
     */
    private ContainerAction create(ContainerAction action) {
        return new ContainerAction(action, dst.createContainer(action.getSrc()));
    }

    private void sync(ContainerAction sourceAction) {
        try {
            List<StoredObjectAction> actions = diffStoredObjects(src.listStoredObjects(sourceAction.getSrc()), dst.listStoredObjects(sourceAction.getDst()));
            for (StoredObjectAction action : actions) {
                switch (action.getAction()) {
                case SKIP:
                    break;
                case COPY_SRC_TO_DST:
                    copy(action.getSrc(), sourceAction.getDst());
                    break;
                case DELETE_DST:
                    delete(action.getDst());
                    break;
                }
            }
        } catch (RuntimeException ex) {
            LOG.error("Failed to synchronize " + sourceAction.getSrc().getName(), ex);
        }
    }

    private void delete(StoredObject so) {
        dst.deleteStoredObject(so);

    }

    private void copy(StoredObject from, Container dstContainer) {
        dst.copyFrom(from, dstContainer);
    }

    //
    // DIFF
    //

    private List<ContainerAction> diffContainers(List<Container> src, List<Container> dst) {
        List<ContainerAction> results = new ArrayList<ContainerAction>();
        for (Container srcContainer : src) {
            boolean found = false;
            for (Container dstContainer : dst) {
                if (equalsName(srcContainer, dstContainer)) {
                    found = true;
                    Action action = Action.SKIP;
                    if (!equalsProps(srcContainer, dstContainer) || hasObjectDifference(srcContainer, dstContainer)) {
                        action = Action.SYNC;
                        results.add(new ContainerAction(srcContainer, dstContainer, action));
                    } else {
                        results.add(new ContainerAction(srcContainer, dstContainer, Action.SKIP));
                    }
                    LOG.info("Found dst container with name \"{}\". Action={} (src: bytes={} count={}, dst: bytes={} count={})"
                            , srcContainer.getName(), action
                            , srcContainer.getBytesUsed(), srcContainer.getCount()
                            , dstContainer.getBytesUsed(), dstContainer.getCount());
                }
            }
            if (!found) {
                results.add(new ContainerAction(srcContainer, null, Action.COPY_SRC_TO_DST));
                LOG.info("New dst container with name \"{}\". Action={} (src: bytes={} count={})"
                        , srcContainer.getName(), Action.COPY_SRC_TO_DST, srcContainer.getBytesUsed(), srcContainer.getCount());
            }
        }
        return results;
    }

    private List<StoredObjectAction> diffStoredObjects(List<StoredObject> srcObjects, List<StoredObject> dstObjects) {
        List<StoredObjectAction> results = new ArrayList<StoredObjectAction>();
        for (StoredObject srcObject : srcObjects) {
            boolean found = false;
            for (StoredObject dstObject : dstObjects) {
                if (equalsName(srcObject, dstObject)) {
                    found = true;
                    if (equalsProps(srcObject, dstObject)) {
                        results.add(new StoredObjectAction(srcObject, dstObject, Action.SKIP));
                    } else {
                        results.add(new StoredObjectAction(srcObject, dstObject, Action.SYNC));
                    }
                }
            }
            if (!found) {
                results.add(new StoredObjectAction(srcObject, null, Action.COPY_SRC_TO_DST));
            }
        }
        return results;
    }

}
