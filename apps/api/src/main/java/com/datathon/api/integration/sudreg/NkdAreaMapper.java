package com.datathon.api.integration.sudreg;

import com.datathon.api.fundingcall.Area;
import java.util.ArrayList;
import java.util.List;

public final class NkdAreaMapper {

    private static final List<Area> WITHOUT_ACTIVITY_CODE = List.of(Area.ENTREPRENEURSHIP, Area.EMPLOYMENT);

    private record Division(int first, int last, List<Area> areas) {

        boolean contains(int division) {
            return division >= first && division <= last;
        }
    }

    private static final List<Division> DIVISIONS = List.of(
            new Division(1, 3, List.of(Area.AGRICULTURE)),
            new Division(5, 9, List.of(Area.ENTREPRENEURSHIP)),
            new Division(10, 33, List.of(Area.ENTREPRENEURSHIP, Area.INNOVATION)),
            new Division(35, 35, List.of(Area.ENERGY)),
            new Division(36, 39, List.of(Area.ENVIRONMENT)),
            new Division(41, 43, List.of(Area.ENTREPRENEURSHIP, Area.ENERGY)),
            new Division(45, 47, List.of(Area.ENTREPRENEURSHIP)),
            new Division(49, 53, List.of(Area.TRANSPORT)),
            new Division(55, 56, List.of(Area.TOURISM)),
            new Division(58, 63, List.of(Area.DIGITALIZATION, Area.INNOVATION)),
            new Division(64, 71, List.of(Area.ENTREPRENEURSHIP)),
            new Division(72, 72, List.of(Area.SCIENCE, Area.INNOVATION)),
            new Division(73, 82, List.of(Area.ENTREPRENEURSHIP)),
            new Division(85, 85, List.of(Area.EDUCATION)),
            new Division(86, 86, List.of(Area.HEALTH)),
            new Division(87, 88, List.of(Area.SOCIAL)),
            new Division(90, 91, List.of(Area.CULTURE)),
            new Division(93, 93, List.of(Area.SPORT)),
            new Division(94, 94, List.of(Area.CIVIL_SOCIETY)));

    private NkdAreaMapper() {
    }

    /** Employment is always added, because every company can apply for hiring subsidies. */
    public static List<Area> forActivityCode(String activityCode) {
        if (activityCode == null) {
            return WITHOUT_ACTIVITY_CODE;
        }
        int division = Integer.parseInt(activityCode.substring(0, 2));
        List<Area> areas = new ArrayList<>(DIVISIONS.stream()
                .filter(candidate -> candidate.contains(division))
                .findFirst()
                .map(Division::areas)
                .orElse(List.of(Area.ENTREPRENEURSHIP)));
        areas.add(Area.EMPLOYMENT);
        return areas;
    }
}
