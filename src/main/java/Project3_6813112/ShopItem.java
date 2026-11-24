// EGCO 213 Project 3 - Fish Goes Fishing
// Members: Pitsanun Bomrungchit (6813111), Pakkapong Khongsri (6813112),
//          Rachata Charoenboonphon (6813113), Phiraphat Permthong (6813249)
package Project3_6813112;

/** Anything sold in the tackle shop (baits, rods, reels). */
// [PRESENT] OOP INTERFACE: Rod, Reel, Bait and Part are enums that implement it (polymorphism)
// ไทย: OOP interface: Rod, Reel, Bait, Part เป็น enum ที่ implement ShopItem (polymorphism)
public interface ShopItem {
    String getTitle();
    int getPrice();
    String getDesc();
    String getCategory();
}
