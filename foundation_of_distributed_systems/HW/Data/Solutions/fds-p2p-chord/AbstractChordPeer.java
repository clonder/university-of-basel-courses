package ch.unibas.dmi.dbis.fds.p2p.chord.api;

import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.Identifier;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircle;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircularInterval;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public abstract class AbstractChordPeer implements ChordNode {
    /** Pointer to this {@link AbstractChordPeer}'s predecessor. */
    private ChordNode predecessor = null;

    /** The {@link ChordNetwork} this {@link AbstractChordPeer} belongs to. */
    private final ChordNetwork network;

    /** The {@link Identifier} if this {@link AbstractChordPeer}. */
    private final Identifier identifier;

    /** HashMap used for data storage. */
    private final Map<String, String> storage = new ConcurrentHashMap<>();

    /** The {@link NodeStatus} of this {@link AbstractChordPeer}. */
    private volatile NodeStatus status = NodeStatus.OFFLINE;

    /** Reference to the internal {@link FingerTable}. */
    protected ChordFingerTable fingerTable;

    /**
     *
     * @param identifier
     * @param network
     */
    protected AbstractChordPeer(Identifier identifier, ChordNetwork network) {
        this.identifier = identifier;
        this.network = network;
        this.fingerTable = new ChordFingerTable();
    }

    /**
     * Getter for the {@link ChordNetwork} this {@link AbstractChordPeer} belongs to.
     *
     * @return {@link ChordNetwork} instance.
     */
    public ChordNetwork getNetwork() {
        return network;
    }

    /**
     * Saves a piece of data identified by the provided key in the local storage.
     *
     * @param origin the node calling the method (purely for logging purposes). Null if it's the client (i.e., not a node in the network)
     * @param key    of data item
     * @param value  of data item
     */
    public void store(Node origin, String key, String value) {
        final Node node = this.lookupNodeForItem(key);
        if (node == this) {
            this.storage.put(key,value);
        } else {
            node.store(this, key, value);
        }
    }

    /**
     * Retrieves a piece of data identified by the provided key from the local storage.
     *
     * @param origin Origin of the request. Null if the query comes from client otherwise the first peer in the network
     * @param key    Key of data item
     * @return value of data item identified by the key.
     */
    public Optional<String> lookup(Node origin, String key) {
        final Node node = this.lookupNodeForItem(key);
        if (node == this) {
            return Optional.ofNullable(this.storage.get(key));
        } else {
            return node.lookup(this, key);
        }
    }

    /**
     * Removes a piece of data identified by the provided key from the local storage. The data deleted is returned by this method.
     *
     * @param origin Origin of the request. Null if the query comes from client otherwise the first peer in the network
     * @param key    Key of data item
     * @return Value of data item identified by the key.
     */
    public Optional<String> delete(Node origin, String key) {
        final Node node = this.lookupNodeForItem(key);
        if (node == this) {
            return Optional.ofNullable(this.storage.remove(key));
        } else {
            return node.delete(this, key);
        }
    }

    /**
     * Returns a set of keys held by this {@link AbstractChordPeer}.
     *
     * @return Set of keys held by this {@link AbstractChordPeer}
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(this.storage.keySet());
    }

    /**
     * Dumps the storage and returns a map containing all data items. This is not a network primitive. Instead,
     * this is a functionality used for internal (e.g. displaying or debugging) purposes.
     *
     * @return The data stored by this {@link AbstractChordPeer}.
     */
    public Map<String,String> dump() {
        return Collections.unmodifiableMap(this.storage);
    }

    /**
     * Performs a lookup for where the data with the provided key should be stored.
     *
     * @return Node in which to store the data with the provided key.
     */
    protected abstract ChordNode lookupNodeForItem(String key);

    @Override
    public ChordNode predecessor() {
        return this.predecessor;
    }

    @Override
    public void setPredecessor(ChordNode predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    public final Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public final FingerTable getFingerTable() {
        return fingerTable;
    }

    /**
     * Called when this {@link ChordNode} joins the network. Internally it asks the network if it is simulated.
     * If so, then this will call {@link #joinOnly(ChordNode)} otherwise an invocation is forwarded to
     * {@link #joinAndUpdate(ChordNode)}
     *
     * @param nprime
     */
    @Override
    public synchronized final void join(Node nprime) {
        if (this.status == NodeStatus.ONLINE || this.status == NodeStatus.JOINING) throw new IllegalStateException("This node has already or is currently joining a network.");
        if (nprime == null || nprime instanceof ChordNode) {
            this.status = NodeStatus.JOINING;
            if (this.network.isDynamic()) {
                joinOnly((ChordNode)nprime);
            } else {
                joinAndUpdate((ChordNode)nprime);
            }
            this.status = NodeStatus.ONLINE;
        }
    }

    /**
     * Sets the status of this {@link AbstractChordPeer} to offline. That's it - the network should handle the rest!
     */
    public synchronized final void leave() {
        if (this.status == NodeStatus.OFFLINE) throw new IllegalStateException("This node is currently part of a network.");
        this.status = NodeStatus.OFFLINE;
    }

    /**
     * Returns the {@link NodeStatus} of this {@link AbstractChordPeer}.
     *
     * @return {@link NodeStatus} of this {@link AbstractChordPeer}
     */
    public synchronized NodeStatus status() {
        return this.status;
    }

    /**
     *
     */
    public class ChordFingerTable implements ch.unibas.dmi.dbis.fds.p2p.chord.api.FingerTable {
        private final ChordNode[] nodes;
        private final List<IdentifierCircularInterval> intervals;
        private final ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircle circle;

        /**
         *
         */
        public ChordFingerTable() {
            this.nodes = new ChordNode[size()];
            this.intervals = new ArrayList<>(size());
            this.circle = new IdentifierCircle(AbstractChordPeer.this.network.getNbits());
            preComputeIntervals();
        }

        private void preComputeIntervals() {
            for (int i = 0; i < size(); i++) {
                Identifier leftBound = circle.getIdentifierAt(start(i + 1));
                Identifier rightBound = circle.getIdentifierAt(start(i + 2));
                intervals.add(IdentifierCircularInterval.createRightOpen(leftBound, rightBound));
            }
        }

        @Override
        public int size() {
            return AbstractChordPeer.this.network.getNbits();
        }

        /**
         * Must be one-based k
         */
        @Override
        public int start(int k) {
            return (AbstractChordPeer.this.getIdentifier().getIndex() + (int) Math.pow(2, k - 1)) % (int) Math.pow(2, size());
        }

        /**
         *
         * @return
         */
        public synchronized ChordNode successor() {
            return nodes[0];
        }

        /**
         * One based k
         */
        @Override
        public synchronized Optional<ChordNode> node(int k) {
            return Optional.ofNullable(nodes[k - 1]);
        }

        /**
         * One based k
         */
        @Override
        public IdentifierCircularInterval interval(int k) {
            return intervals.get(k - 1);
        }

        /**
         *
         * @param k
         * @param node
         */
        public synchronized void setNode(int k, ChordNode node) {
            if(node == null){
                throw new IllegalArgumentException("Cannot set a finger to null");
            }
            nodes[k - 1] = node;
        }
    }
}
