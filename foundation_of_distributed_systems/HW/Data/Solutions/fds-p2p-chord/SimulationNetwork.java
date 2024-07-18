package ch.unibas.dmi.dbis.fds.p2p.simulation;

import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.Identifier;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.math.CircularInterval;
import ch.unibas.dmi.dbis.fds.p2p.chord.impl.ChordNetwork;
import ch.unibas.dmi.dbis.fds.p2p.chord.impl.ChordPeer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This is a {@link ChordNetwork} implementation that adds some features with regards to simulation. It internally keeps
 * track of a list of {@link SimulationPeer}s (i.e. simulated {@link ChordPeer}s) and provides different type of access
 * to them, which are all required for simulation.
 *
 * @author Loris Sauter
 */
public class SimulationNetwork extends ChordNetwork implements SimulationEventListener {
  /** */
  private final SimulationEventListener eventHandler;

  /** Internal list of {@link SimulationPeer}s. An actual network does not have that! */
  private final List<SimulationPeer> peers;

  /** Reference to ranomd number generator. */
  private final Random random = new Random();

  /**
   * Constructor for {@link SimulationNetwork}.
   *
   * @param nbits Number of bits to create the chord ring. Equals the value <strong>m</strong> in the paper.
   * @param dynamic Whether or not a dynamic {@link ChordNetwork} should be simulated.
   * @param listener A listener for {@link SimulationEvent}.
   */
  public SimulationNetwork(int nbits, boolean dynamic, SimulationEventListener listener){
    super(nbits, dynamic);
    this.eventHandler = listener;
    this.peers = new ArrayList<>();
  }

  /**
   * Looks up and returns a {@link SimulationPeer} based on its number (not equal to its index in {@link SimulationNetwork#peers}.
   * This method is strictly for simulation purposes only!
   *
   * @param number Number of the {@link SimulationPeer}
   * @return Optional {@link SimulationPeer}.
   */
  public synchronized Optional<SimulationPeer> getSimulationPeer(int number){
    for(SimulationPeer p : peers){
      if(p.getIdentifier().getIndex() == number){
        return Optional.of(p);
      }
    }
    return Optional.empty();
  }

  /**
   * Randomly returns one of the actively simulated {@link SimulationPeer}s.
   *
   * @return {@link SimulationPeer}
   */
  public synchronized final SimulationPeer getRandomPeer() {
    return this.peers.get(random.nextInt(this.peers.size()));
  }

  /**
   * Convenience method to create a new {@link SimulationPeer} from this {@link SimulationPeer}. The newly created peer
   * is NOT part of the {@link SimulationPeer} yet! It merely creates it with all the required intrinsics.
   *
   * @param index The index (i.e. position) of the {@link SimulationPeer}.
   * @return The newly created {@link SimulationPeer}.
   */
  public synchronized SimulationPeer createChordPeer(int index){
    final CircularInterval<Integer> interval = CircularInterval.createClosed(0, (int)Math.pow(2, size()));
    if(!interval.contains(index)){
      throw new IllegalArgumentException(String.format("The requested index %d is not part of the interval %s.", index, interval.toString()));
    }
    final Identifier id = this.circle.getIdentifierAt(index);
    final SimulationPeer sp = new SimulationPeer(id, this);
    sp.addSimulationEventListener(this);
    this.peers.add(sp);
    return sp;
  }

  /**
   * Removes a {@link SimulationPeer} from the {@link SimulationNetwork}. This method is only for simulation purposes!
   *
   * @param number Number of the {@link SimulationPeer} (not equal to the index in {@link SimulationNetwork#peers}).
   */
  public synchronized void remove(int number){
    getSimulationPeer(number).ifPresent(p -> {
      this.peers.remove(p);
      p.leave();
    });
  }

  /**
   * Forwards a {@link SimulationEvent} to the registered {@link SimulationEventListener}.
   *
   * @param event {@link SimulationEvent}
   */
  @Override
  public void handle(SimulationEvent event) {
    if(this.eventHandler != null) this.eventHandler.handle(event);
  }
}
