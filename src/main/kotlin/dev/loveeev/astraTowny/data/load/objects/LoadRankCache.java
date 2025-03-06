package dev.loveeev.astraTowny.data.load.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
public class LoadRankCache {


    private String name;
    private List<String> perms;
    private String town;
    private String nation;
    private UUID uuid;
    public LoadRankCache(String name, String town, String nation, List<String> perms,UUID uuid){
        this.name = name;
        this.town = town;
        this.nation = nation;
        this.perms = new ArrayList<>();
        this.perms.addAll(perms);
        this.uuid = uuid;
    }
}
