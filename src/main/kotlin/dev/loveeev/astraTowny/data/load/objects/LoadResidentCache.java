package dev.loveeev.astraTowny.data.load.objects;

import dev.loveeev.astratowny.manager.TownManager;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Town;

public class LoadResidentCache {
    private String town;

    private String nation;
    private Resident resident;
    public LoadResidentCache(String town,String nation,Resident resident){
        this.town = town;
        this.nation = nation;
        this.resident = resident;
    }

    public Town getTown() {
        return TownManager.getInstance().getTown(town);
    }


    public Nation getNation() {
        return TownManager.getInstance().getNation(nation);
    }

    public Resident getResident() {
        return resident;
    }


}
