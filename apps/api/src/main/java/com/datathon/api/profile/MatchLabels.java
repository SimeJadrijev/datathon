package com.datathon.api.profile;

import com.datathon.api.fundingcall.Area;
import com.datathon.api.fundingcall.TargetGroup;

final class MatchLabels {

    private MatchLabels() {
    }

    static String of(TargetGroup targetGroup) {
        return switch (targetGroup) {
            case CITIZEN -> "građani";
            case CRAFT_BUSINESS -> "obrtnici";
            case COMPANY -> "tvrtke";
            case ASSOCIATION -> "udruge";
            case LOCAL_GOVERNMENT -> "jedinice lokalne samouprave";
            case STUDENT -> "studenti";
            case FARMER -> "poljoprivrednici";
        };
    }

    static String of(Area area) {
        return switch (area) {
            case ENVIRONMENT -> "okoliš";
            case ENERGY -> "energetika";
            case TRANSPORT -> "promet";
            case AGRICULTURE -> "poljoprivreda";
            case EDUCATION -> "obrazovanje";
            case SCIENCE -> "znanost";
            case CULTURE -> "kultura";
            case SPORT -> "sport";
            case SOCIAL -> "socijala";
            case HEALTH -> "zdravstvo";
            case EMPLOYMENT -> "zapošljavanje";
            case ENTREPRENEURSHIP -> "poduzetništvo";
            case INNOVATION -> "inovacije";
            case DIGITALIZATION -> "digitalizacija";
            case TOURISM -> "turizam";
            case YOUTH -> "mladi";
            case CIVIL_SOCIETY -> "civilno društvo";
        };
    }
}
