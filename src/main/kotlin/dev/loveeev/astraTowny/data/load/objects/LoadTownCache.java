package dev.loveeev.astraTowny.data.load.objects;

import dev.loveeev.astratowny.objects.Town;

public class LoadTownCache {
    private Town town;

    public String getNation() {
        return nation;
    }
    private String nation;
    public void setTown(Town town) {
        this.town = town;
    }

    public LoadTownCache(Town town, String nation){
        this.town = town;
        this.nation = nation;
    }
    public void setNation(String nation) {
        this.nation = nation;
    }



    public Town getTown() {
        return town;
    }

}
