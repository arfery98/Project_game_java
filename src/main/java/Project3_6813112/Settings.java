// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Everything chosen on the setup screen. */
// [PRESENT] OOP RECORD: immutable settings passed from the setup screen to the game
// ไทย: OOP record: ค่าตั้งต้นที่ส่งจากหน้า Setup ไปเกม แก้ไขไม่ได้ (immutable)
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
