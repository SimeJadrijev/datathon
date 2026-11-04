package com.datathon.api.association;

import com.datathon.api.fundingcall.Area;
import java.util.Set;

public record AssociationRecord(String oib, String name, String seat, String county, String registryNumber, Set<Area> areas) {

    static AssociationRecord from(Association association) {
        return new AssociationRecord(association.getOib(), association.getName(), association.getSeat(),
                association.getCounty(), association.getRegistryNumber(), Set.copyOf(association.getAreas()));
    }

    public Association toEntity() {
        Association association = new Association(oib, name);
        association.setSeat(seat);
        association.setCounty(county);
        association.setRegistryNumber(registryNumber);
        association.getAreas().addAll(areas == null ? Set.of() : areas);
        return association;
    }
}
