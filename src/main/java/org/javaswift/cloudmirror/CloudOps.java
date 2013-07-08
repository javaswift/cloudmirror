/*
 * (C) 2013 42 bv (www.42.nl). All rights reserved.
 */
package org.javaswift.cloudmirror;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.javaswift.joss.instructions.UploadInstructions;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.PaginationMap;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CloudOps.
 * @author E.Hooijmeijer
 *
 */
public class CloudOps {

    private static final Logger LOG = LoggerFactory.getLogger(CloudOps.class);

    private static final int MAX_PAGE_SIZE = 7500;

    private final boolean dryRun;

    private final Account account;

    private long writeBytesCnt = 0;
    private long updateObjectCnt = 0;
    private long deleteObjectCnt = 0;
    private long createContainerCnt = 0;
    private long deleteContainerCnt = 0;

    private final long total;

    public CloudOps(Account account, boolean dryRun, long total) {
        LOG.info("Initializing " + account.getPublicURL() + " in " + (dryRun ? "ReadOnly" : "Commit") + " mode.");
        this.account = account;
        this.dryRun = dryRun;
        this.total = total;
    }

    public List<Container> listContainers() {
        List<Container> results = new ArrayList<Container>(account.getCount());
        PaginationMap map = account.getPaginationMap(MAX_PAGE_SIZE);
        for (int page = 0; page < map.getNumberOfPages(); page++) {
            results.addAll(account.list(map, page));
        }
        return results;
    }

    public List<Container> listContainers(List<String> containerNames) {
        List<Container> results = new ArrayList<Container>(containerNames.size());
        for (String name : containerNames) {
            Container container = account.getContainer(name);
            if (container != null) {
                results.add(container);
            } else {
                LOG.error("Container with name \"{}\" not found.", name);
            }
        }
        return results;
    }

    public List<StoredObject> listStoredObjects(Container parent) {
        if (parent.exists()) {
            List<StoredObject> results = new ArrayList<StoredObject>(parent.getCount());
            PaginationMap map = parent.getPaginationMap(MAX_PAGE_SIZE);
            for (int page = 0; page < map.getNumberOfPages(); page++) {
                results.addAll(parent.list(map, page));
            }
            return results;
        } else {
            return new ArrayList<StoredObject>();
        }
    }

    /**
     * @param src
     */
    public Container createContainer(Container src) {
        LOG.info("Creating container '" + src.getName() + "'");
        createContainerCnt++;
        if (!dryRun) {
            Container dst = account.getContainer(src.getName());
            dst.create();
            if (src.isPublic()) {
                dst.makePublic();
            } else {
                dst.makePrivate();
            }
            return dst;
        } else {
            return account.getContainer(src.getName());
        }
    }

    /**
     * @param dst
     */
    public void deleteContainer(Container dst) {
        LOG.info("Deleting container '" + dst.getName() + "' including all storedObjects.");
        deleteContainerCnt++;
        if (!dryRun) {
            for (StoredObject so : listStoredObjects(dst)) {
                deleteObjectCnt++;
                so.delete();
            }
            dst.delete();
        } else {
            deleteObjectCnt += dst.getCount();
        }
    }

    /**
     * @param so
     */
    public void deleteStoredObject(StoredObject so) {
        LOG.info("Deleting storedObject '" + so.getName() + "'");
        deleteObjectCnt++;
        if (!dryRun) {
            so.delete();
        }

    }

    /**
     * @param from
     */
    public void copyFrom(StoredObject from, Container dstContainer) {
        InputStream fromStream = null;
        try {
            LOG.info("Copying storedObject '{}' to '{}' ({} bytes)", from.getName(), dstContainer.getName(), from.getContentLength());
            StoredObject dst = dstContainer.getObject(from.getName());
            updateObjectCnt++;
            writeBytesCnt += from.getContentLength();
            if (!dryRun) {
                Map<String, Object> fromMeta = from.getMetadata();
                String fromContentType = from.getContentType();
                fromStream = from.downloadObjectAsInputStream();

                dst.uploadObject(new UploadInstructions(fromStream).setContentType(fromContentType));
                dst.setMetadata(fromMeta);
            }
            if (total > 0) {
                LOG.info(((100.0 * writeBytesCnt) / total) + "%");
            }
        } catch (Exception ex) {
            LOG.error("COPY FAILED and will be ignored: storedObject '{}' to '{}'", new Object[] { from.getName(), dstContainer.getName(), ex });
        } finally {
            IOUtils.closeQuietly(fromStream);
        }
    }

    public void report() {
        LOG.info("---------------------------------------------------------------------");
        LOG.info("Containers:   C:" + createContainerCnt + " D:" + deleteContainerCnt);
        LOG.info("Objects   : C/U:" + updateObjectCnt + " D:" + deleteObjectCnt);
        LOG.info("Bytes     :   W:" + toStr(writeBytesCnt, 0) + " (" + writeBytesCnt + ")");
    }

    /**
     * @param writeBytesCnt2
     * @return
     */
    private String toStr(double b, int ofs) {
        if (b > 1024) {
            return toStr(b / 1024, ofs + 1);
        } else {
            switch (ofs) {
            case 0:
                return b + " bytes";
            case 1:
                return b + " kB";
            case 2:
                return b + " MB";
            case 3:
                return b + " GB";
            case 4:
                return b + " TB";
            case 5:
                return b + " PB";
            default:
                return "Too many bytes.";
            }
        }

    }
}
