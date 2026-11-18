// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** One fish that was landed. eaten = how many times a bigger fish bit it on the way up (3 = only bones). */
public record CatchRecord(Species species, int sizeCm, int value, boolean newSpecies, boolean record, String clock, int eaten) {

    public CatchRecord(Species species, int sizeCm, int value, boolean newSpecies, boolean record, String clock) {
        this(species, sizeCm, value, newSpecies, record, clock, 0);
    }
}
