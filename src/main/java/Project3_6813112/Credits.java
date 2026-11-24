// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Group member names & IDs. Edit this one place - every screen reads from here. */
// [PRESENT] REQUIRED: member names + IDs (shown on the title screen and in the credits)
// ไทย: ชื่อและรหัสสมาชิกทุกคน (แสดงบนหน้าจอ title และหน้า credits)
public final class Credits {

    public static final String[] MEMBERS = {
        "Pitsanun Bomrungchit  (6813111)",
        "Pakkapong Khongsri  (6813112)",
        "Rachata Charoenboonphon  (6813113)",
        "Phiraphat Permthong  (6813249)"
    };

    /** The group representative (the package is named after this ID). */
    public static final String REPRESENTATIVE = "6813112";

    private Credits() { }

    public static String oneLine() {
        return String.join("   |   ", MEMBERS);
    }

    /** First line of the member list (two names) and second line (two names), for a centred label. */
    public static String line1() { return MEMBERS[0] + "   |   " + MEMBERS[1]; }
    public static String line2() { return MEMBERS[2] + "   |   " + MEMBERS[3]; }

    /** The members on two lines (HTML), so the whole list fits in a narrow label. */
    public static String twoLines() {
        return MEMBERS[0] + "   |   " + MEMBERS[1] + "<br>" + MEMBERS[2] + "   |   " + MEMBERS[3];
    }
}
