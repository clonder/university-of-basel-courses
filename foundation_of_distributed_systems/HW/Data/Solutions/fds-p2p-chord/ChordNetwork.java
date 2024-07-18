package ch.unibas.dmi.dbis.fds.p2p.chord.impl;

import ch.unibas.dmi.dbis.fds.p2p.chord.api.IdentifierCircle;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.data.Identifier;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.math.CircularInterval;
import ch.unibas.dmi.dbis.fds.p2p.chord.api.math.HashFunction;

/**
 * Reference implementation of the {@link ChordNetwork} as described in [1].
 *
 * @author Loris Sauter
 */
public class ChordNetwork implements ch.unibas.dmi.dbis.fds.p2p.chord.api.ChordNetwork {

  /** Number of bits to use for the chord ring (value of <strong>m</strong> in the paper). */
  private final int nbits;

  /** Whether the {@link ChordNetwork} exhibits dynamic behaviour (e.g. uses stabilize / fix-fingers). */
  private final boolean dynamic;

  /** The {@link HashFunction} used to hash keys. */
  private final HashFunction function;

  /** */
  protected final IdentifierCircle<Identifier> circle;

  /**
   * Constructor for {@link ChordNetwork}.
   *
   * @param nbits Number of bits to use for the chord ring (value of <strong>m</strong> in the paper).
   */
  public ChordNetwork(int nbits) {
    this(nbits,false);
  }

  /**
   * Constructor for {@link ChordNetwork}.
   *
   * @param nbits Number of bits to use for the chord ring (value of <strong>m</strong> in the paper).
   * @param dynamic  Whether the {@link ChordNetwork} should exhibit dynamic behaviour (e.g. uses stabilize / fix-fingers)
   */
  public ChordNetwork(int nbits, boolean dynamic){
    this.nbits = nbits;
    this.function = new HashFunction(nbits);
    this.dynamic = dynamic;
    this.circle = new ch.unibas.dmi.dbis.fds.p2p.chord.api.data.IdentifierCircle(nbits);
  }

  /**
   * Getter for the {@link IdentifierCircle} that is underpinning the {@link ch.unibas.dmi.dbis.fds.p2p.chord.api.ChordNetwork}.
   *
   * @return {@link IdentifierCircle<Identifier>}
   */
  public IdentifierCircle<Identifier> getIdentifierCircle() {
    return circle;
  }

  /**
   * Getter for the {@link HashFunction} that is underpinning the {@link ch.unibas.dmi.dbis.fds.p2p.chord.api.ChordNetwork}.
   *
   * @return {@link HashFunction} reference.
   */
  public HashFunction getHashFunction() {
    return this.function;
  }

  /**
   * Number of bits used to construct the chord ring.
   *
   * @return Number of bits.
   */
  @Override
  public int getNbits() {
    return nbits;
  }

  /**
   * Indicates whether or not this {@link ch.unibas.dmi.dbis.fds.p2p.chord.api.ChordNetwork} uses dynamic mode:
   *
   * - static mode: NodeJoins call full setup, not stabilization.
   * - dynamic mode: Joins
   *
   * @return True if dynamic, false otherwise.
   */
  @Override
  public boolean isDynamic() {
    return dynamic;
  }

  /**
   * Convenience method to create a new {@link ChordPeer} from this {@link ChordNetwork}. The newly created peer
   * is NOT part of the {@link ChordNetwork} yet! It merely creates it with all the required intrinsics.
   *
   * @param index The index (i.e. position) of the {@link ChordPeer}.
   * @return The newly created {@link ChordPeer}.
   */
  public ChordPeer createChordPeer(int index){
    final CircularInterval<Integer> interval = CircularInterval.createClosed(0, (int)Math.pow(2, size()));
    if(!interval.contains(index)){
      throw new IllegalArgumentException(String.format("The requested index %d is not part of the interval %s.", index, interval.toString()));
    }
    final Identifier id = this.circle.getIdentifierAt(index);
    return new ChordPeer(id, this);
  }
}
