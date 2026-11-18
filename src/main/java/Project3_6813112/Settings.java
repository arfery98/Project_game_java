// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Everything chosen on the setup screen. */
public record Settings(
        String name,
        Location location,
        boolean sound,
        boolean rain,
        boolean dayNight,
        boolean easyReel,
        boolean fishFinder,
        int volume) {
}
