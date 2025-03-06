package dev.loveeev.astraTowny.data.load.objects;

import dev.loveeev.astratowny.objects.Resident;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResidentRankCache {
    private Resident resident;
    private String rankName;
    private Boolean type;
}
