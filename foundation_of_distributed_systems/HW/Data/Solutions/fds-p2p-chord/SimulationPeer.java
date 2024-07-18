package ch.unibas.dmi.dbis.fds.p2p.simulation;

import ch.unibas.dmi.dbis.fds.p2p.chord.api.ChordNode;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.Node;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.Identifier;
import ch.unibas.dmi.dbis.fds.p2p.chord.impl.ChordPeer;
import ch.unibas.dmi.dbis.fds.p2p.simulation.SimulationEvent.EventType;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This is a {@link ChordPeer} implementation that adds some features with regards to simulation. It internally
 * generates {@link SimulationEvent}s for logging and introduces some randomness in how long RPC messages take to travel.
 *
 * @author Loris Sauter & Ralph Gasser
 */
public class SimulationPeer extends ChordPeer implements ChordNode {

  /** Logger used to log errors. */
  private static final Logger LOGGER = LogManager.getLogger();

  /** */
  private List<SimulationEventListener> eventListeners = new ArrayList<>();

  /** Random number generator used to calculate delays. */
  private final Random random = new Random();

  public SimulationPeer(Identifier identifier, SimulationNetwork network) {
    super(identifier, network);
  }

  @Override
  public ChordNode findSuccessor(ChordNode caller, Identifier id) {
    if (caller != this) delay();
    notifyListeners(new SimulationEvent(EventType.FIND_SUCCESSOR, caller.getIdentifier(), getIdentifier()));
    return super.findSuccessor(caller, id);
  }

  @Override
  public ChordNode findPredecessor(ChordNode caller, Identifier id) {
    if (caller != this) delay();
    notifyListeners(new SimulationEvent(EventType.FIND_PREDECESSOR, caller.getIdentifier(), getIdentifier()));
    return super.findPredecessor(caller, id);
  }

  @Override
  public ChordNode closestPrecedingFinger(ChordNode caller, Identifier id) {
    if (caller != this) delay();
    notifyListeners(new SimulationEvent(EventType.CLOSEST_PRECEDING_FINGER, caller.getIdentifier(), getIdentifier()));
    return super.closestPrecedingFinger(caller, id);
  }

  @Override
  public void store(Node origin, String key, String value) {
    if (origin != null) {
      if (origin != this) delay();
      notifyListeners(new SimulationEvent(EventType.STORE_DATA, ((ChordPeer)origin).getIdentifier(), getIdentifier()));
    } else {
      notifyListeners(new SimulationEvent(EventType.STORE_DATA, getIdentifier()));
    }
    super.store(origin, key, value);
  }

  @Override
  public Optional<String> lookup(Node origin, String key) {
    if (origin != null) {
      if (origin != this) delay();
      notifyListeners(new SimulationEvent(EventType.LOOKUP_DATA, ((ChordPeer)origin).getIdentifier(), getIdentifier()));
    } else {
      notifyListeners(new SimulationEvent(EventType.LOOKUP_DATA, getIdentifier()));
    }
    return super.lookup(origin, key);
  }

  @Override
  public void notify(ChordNode nprime) {
    if (nprime !=this) delay();
    notifyListeners(new SimulationEvent(EventType.NOTIFY, nprime.getIdentifier(), getIdentifier()));
    super.notify(nprime);
  }

  public void addSimulationEventListener(SimulationEventListener listener){
    eventListeners.add(listener);
  }

  private void notifyListeners(SimulationEvent event){
    eventListeners.forEach(l -> l.handle(event));
  }

  /**
   * Simulates a network delay between 5 and 80ms.
   */
  private void delay() {
    try {
      Thread.sleep(Math.abs(random.nextInt(75 + 5)));
    } catch (InterruptedException e) {
      LOGGER.log(Level.ERROR, "The thread was interrupted while simulating a network delay!");
    }
  }
}
