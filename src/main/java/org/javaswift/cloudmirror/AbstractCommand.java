package org.javaswift.cloudmirror;

import java.util.List;

import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;

public abstract class AbstractCommand {

    protected final CloudOps src;
    protected final CloudOps dst;

    public abstract void run();

    public AbstractCommand(SharedArguments args) {
        this(args, true, -1);
    }

    public AbstractCommand(SharedArguments args, boolean dryRun, long total) {
        this(connect(args.getSourceCredentials()), connect(args.getDestCredentials()), dryRun, total);
    }

    public AbstractCommand(Account src, Account dst, boolean dryRun, long total) {
        this.src = new CloudOps(src, true, total);
        this.dst = new CloudOps(dst, dryRun, total);
    }

    /**
     * connects to the cloud.
     * @param cred the credentials.
     * @return an account.
     */
    protected static Account connect(List<String> cred) {
        return new AccountFactory()
                .setAuthUrl(cred.get(0))
                .setTenant(cred.get(1))
                .setUsername(cred.get(2))
                .setPassword(cred.get(3))
                .createAccount();

    }

    protected boolean equalsName(Container src, Container dst) {
        return src.getName().equals(dst.getName());
    }

    protected boolean equalsProps(Container src, Container dst) {
        return (src.getBytesUsed() == dst.getBytesUsed()) && (src.getCount() == dst.getCount());
    }

    protected boolean equalsName(StoredObject src, StoredObject dst) {
        return src.getName().equals(dst.getName());
    }

    protected boolean equalsProps(StoredObject src, StoredObject dst) {
        return src.getEtag().equals(dst.getEtag());
    }

    protected boolean hasObjectDifference(Container srcContainer, Container dstContainer) {
        boolean result = false;
        List<StoredObject> srcObjects = src.listStoredObjects(srcContainer);
        List<StoredObject> dstObjects = dst.listStoredObjects(dstContainer);
        for (StoredObject srcObject : srcObjects) {
            if (hasObjectDifference(srcObject, dstObjects)) {
                result = true;
                break;
            }
        }
    
        return result;
    }

    private boolean hasObjectDifference(StoredObject srcObject, List<StoredObject> dstObjects) {
        boolean result = false;
        for (StoredObject dstObject : dstObjects) {
            if (equalsName(srcObject, dstObject)
                    && !equalsProps(srcObject, dstObject)) {
                result = true;
                break;
            }
        }
    
        return result;
    }

}