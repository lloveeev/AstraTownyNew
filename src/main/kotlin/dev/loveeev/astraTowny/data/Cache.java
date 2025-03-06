package dev.loveeev.astraTowny.data;

import dev.loveeev.astratowny.data.load.objects.LoadRankCache;
import dev.loveeev.astratowny.data.load.objects.LoadResidentCache;
import dev.loveeev.astratowny.data.load.objects.LoadTownCache;
import dev.loveeev.astratowny.data.load.objects.ResidentRankCache;
import dev.loveeev.astratowny.objects.townblocks.TownBlocks;
import dev.loveeev.astratowny.objects.Nation;
import dev.loveeev.astratowny.objects.Resident;
import dev.loveeev.astratowny.objects.Rank;
import dev.loveeev.astratowny.objects.Town;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Cache {
    private static Cache instance;

    private List<LoadRankCache> loadRankCaches;
    private List<LoadTownCache> loadCaches;
    private List<LoadResidentCache> residentCaches;
    private List<ResidentRankCache> residentRankCaches;

    public Cache(){
        instance = this;
        this.loadRankCaches = new ArrayList<>();
        this.loadCaches = new ArrayList<>();
        this.residentCaches = new ArrayList<>();
        this.residentRankCaches = new ArrayList<>();
    }
    public static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }
}
