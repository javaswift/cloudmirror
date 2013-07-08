package org.javaswift.cloudmirror;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.javaswift.joss.model.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

/**
 * Shows all container names with the number of objects in it and the total number of bytes and the difference
 * in bytes between the source and target object store.
 * Based on the number of groups the container names are printed as a list whereby the total number of bytes are
 * equally divided over the number of groups.
 * 
 * @author theo
 */
public class ContainerListing extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerListing.class);

    private int nrOfGroups;

    public ContainerListing(ListArguments args) {
        super(args.getSharedArguments());
        this.nrOfGroups = args.getNrGroups();
    }

    @Override
    public void run() {
        List<Container> srcContaners = src.listContainers();
        List<Container> dstContainers = dst.listContainers();
        Map<String, Long> diff = diffContainersInValueOrdered(srcContaners, dstContainers);
        List<List<String>> groups = createGroups(diff);
        printGroups(groups);
    }

    private Map<String, Long> diffContainersInValueOrdered(List<Container> srcContaners, List<Container> dstContainers) {
        TreeMap<String, Long> result = new ValueComparableMap<String, Long>(Ordering.natural().reverse());

        for (Container srcContainer : srcContaners) {
            boolean found = false;
            long srcBytesUsed = srcContainer.getBytesUsed();
            int srcCount = srcContainer.getCount();
            String srcName = srcContainer.getName();
            for (Container dstContainer : dstContainers) {
                if (equalsName(srcContainer, dstContainer)) {
                    found = true;
                    long dstBytesUsed = dstContainer.getBytesUsed();
                    int dstCount = dstContainer.getCount();
                    long byteDiff = srcBytesUsed - dstBytesUsed;
                    boolean added = false;
                    if (!equalsProps(srcContainer, dstContainer) || hasObjectDifference(srcContainer, dstContainer)) {
                        added = true;
                        result.put(srcName, byteDiff);
                    }
                    LOG.info("Found dst container with name \"{}\".{} (src: bytes={} count={}, dst: bytes={} count={})"
                            , srcName
                            , !added ? "No diffs (container ignored)" : ""
                            , srcBytesUsed, srcCount
                            , dstBytesUsed, dstCount);
                }
            }
            if (!found) {
                result.put(srcName, srcBytesUsed);
                LOG.info("Found NEW container with name \"{}\".(src: bytes={} count={})"
                        , srcName
                        , srcBytesUsed, srcCount);
            }
        }

        return result;
    }

    private List<List<String>> createGroups(Map<String, Long> diff) {
        List<List<String>> result = new ArrayList<List<String>>(getNrOfGroups());
        prefillList(result);

        Set<Entry<String, Long>> entrySet = diff.entrySet();
        int index = 0;
        GroupState state = GroupState.UP;
        for (Entry<String, Long> entry : entrySet) {
            LOG.info("container: {} diff: {}", entry.getKey(), entry.getValue());
            List<String> resultList = result.get(index);
            resultList.add(entry.getKey());

            if (getNrOfGroups() == 1
                    || (state == GroupState.UP && index + 1 == getNrOfGroups())
                    || (state == GroupState.DOWN && index == 0)) {
                state = GroupState.HOLD;
            } else if (state == GroupState.HOLD && index + 1 == getNrOfGroups()) {
                state = GroupState.DOWN;
            } else if (state == GroupState.HOLD && index == 0) {
                state = GroupState.UP;
            }

            switch (state) {
            case UP:
                index++;
                break;
            case DOWN:
                index--;
                break;
            default:
                break;
            }

        }

        return result;
    }

    private void prefillList(List<List<String>> result) {
        for (int index = 0; index < getNrOfGroups(); index++) {
            result.add(new ArrayList<String>());
        }
    }

    private void printGroups(List<List<String>> groups) {
        int groupNr = 1;
        for (List<String> list : groups) {
            String groupsString = org.apache.commons.lang.StringUtils.join(list, ", ");
            LOG.info("Group {}: {}", groupNr++, groupsString);
        }
    }

    public int getNrOfGroups() {
        return nrOfGroups;
    }

    private static class ValueComparableMap<K extends Comparable<K>, V> extends TreeMap<K, V> {
        //A map for doing lookups on the keys for comparison so we don't get infinite loops
        private final Map<K, V> valueMap;

        ValueComparableMap(final Ordering<? super V> partialValueOrdering) {
            this(partialValueOrdering, new HashMap<K, V>());
        }

        private ValueComparableMap(Ordering<? super V> partialValueOrdering,
                HashMap<K, V> valueMap) {
            super(partialValueOrdering.onResultOf(Functions.forMap(valueMap)).compound(Ordering.natural()));
            this.valueMap = valueMap;
        }

        public V put(K k, V v) {
            if (valueMap.containsKey(k)) {
                remove(k);
            }
            valueMap.put(k, v);
            return super.put(k, v);
        }
    }

    private enum GroupState {
        UP, DOWN, HOLD;
    }
}
