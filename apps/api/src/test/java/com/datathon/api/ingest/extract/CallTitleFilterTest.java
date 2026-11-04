package com.datathon.api.ingest.extract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CallTitleFilterTest {

    private static final CallTitleFilter FILTER = new CallTitleFilter(new FundingFilter());

    private static final List<String> CALLS_IN_MINISTRY_NEWS = List.of(
            "Objavljen javni poziv za potpore za izradu ili izmjenu specifikacija proizvoda s oznakama",
            "Javni poziv za financiranje aktivnosti partnerske mreže organizacija za podršku i pomoć žrtvama",
            "Natječaj za dodjelu stipendija „Korak u život“",
            "Započelo podnošenje zahtjeva iz Programa potpore za očuvanje proizvodnje mlijeka na području",
            "Otvorene prijave za Datathon Portala otvorenih podataka: nagradni fond od 12.000 eura",
            "Poziv na dodjelu bespovratnih sredstava \"Opremanje javne kulturne infrastrukture\" (Rok: 31. ožujka 2027.)",
            "Objavljen novi Javni poziv za provedbu intervencije 78.a.01. „Krizna plaćanja poljoprivrednicima“",
            "Interreg Italija – Hrvatska – otvoren Poziv za hrvatske i talijanske poduzetnike u okviru projekta",
            "Objavljen je Javni poziv za dodjelu potpora male vrijednosti otočnim poslodavcima",
            "Objavljen Treći Natječaj za provedbu intervencije 73.10. Potpora za ulaganja u primarnu poljoprivrednu proizvodnju",
            "Program transnacionalne suradnje Euro-MED - osmi poziv na dostavu projektnih prijedloga",
            "Objavljena dokumentacija za 1. poziv za mikro, male i srednje poduzetnike u okviru projekta",
            "Javni poziv za predlaganje javnih potreba u kulturi Republike Hrvatske za 2027. godinu",
            "Objavljen je Poziv za dodjelu financijskih sredstava organizacijama civilnoga društva za programe",
            "Prijave na natječaje za Programe ugradnje dizala i uređenja pročelja otvorene do 31. kolovoza");

    private static final List<String> OTHER_MINISTRY_NEWS = List.of(
            "Podsjećamo: Prijave na javni natječaj za pričuvne policijske službenike i dalje su moguće!",
            "Još jedan neuspješan natječaj za proširenje skladišta plina Chiren",
            "2,5 milijuna eura za financiranje projekata lokalne infrastrukture i ruralnog razvoja na p",
            "Javni natječaj za popunu radnog mjesta - Državni arhiv u Bjelovaru",
            "Još jedan javni poziv uspješno je zaključen: Za 66 projekata usmjerenih hrvatskom iseljeništvu",
            "Veselin Đuho, Branka Batinić i Ivan Gudelj dobitnici Državne nagrade za sport „Franjo Bučar“",
            "Javni poziv za predlaganje predsjednika i članova Hrvatskoga arhivskog vijeća (Rok: 12. studenoga 2026.)",
            "Europska komisija objavila poziv za članstvo u Savezu EU-a za industrijske lance vrijednosti",
            "Priznavanje više sile uslijed suše, Vlajčić: Suša je uzela urod, nema smisla da uzme i potpore",
            "magazin.hrt.hr - Dodijeljene nagrade \"Vicko Andrić\"",
            "Hrvatski sustav vaučera među najboljima u Europi: osvojeno 3. mjesto na European Public Sector Award",
            "Poziv na selekcijski postupak kandidatima/kandidatkinjama za upis u Program srednjoškolskog obrazovanja",
            "Ostvaren preduvjet za otvaranje povoljnih zajmova s otpisom dijela glavnice: Financijski instrument",
            "Vlada RH: Donesena Odlika o davanju suglasnosti u svrhu dodjele bespovratnih sredstava iznosa",
            "Poziv za prijavu izlagača za 12. Sajam zimnice, ekoloških i autohtonih proizvoda",
            "Jedan račun u nagradnoj igri Porezne uprave donio je 10.000 eura sretnoj dobitnici iz Dubrovnika",
            "Poziv za prijavu kandidata/kandidatkinja za dodjelu Nagrade za humanitarni rad za 2025. godinu",
            "Gotovo 400 tisuća eura za realizaciju 152 programa međunarodne kulturne suradnje na drugom krugu",
            "Na javni natječaj za ugradnju dizala pristiglo 36 prijava očekuje ih se još",
            "Otvoren poziv za nove članove Dunavskog vijeća mladih",
            "Hrvatski projekt među osam odabranih za bespovratna sredstva Europskih dana baštine",
            "Zbog širenja afričke svinjske kuge donesene dodatne mjere za područja visokog rizika");

    @Test
    void newsThatAnnounceACallAreKept() {
        assertThat(CALLS_IN_MINISTRY_NEWS).allMatch(FILTER::isCallInNews);
    }

    @Test
    void newsAboutAwardsJobsResultsAndVisitsAreDropped() {
        assertThat(OTHER_MINISTRY_NEWS).noneMatch(FILTER::isCallInNews);
    }

    @Test
    void listsOfCallsUseTheLighterFilter() {
        assertThat(FILTER.isCall("Natječaj za dodjelu stipendija studentima s područja Grada Bjelovara")).isTrue();
        assertThat(FILTER.isCall("Javni natječaj za prijam u službu na neodređeno vrijeme")).isFalse();
        assertThat(FILTER.isCall("JAVNI NATJEČAJ ZA PRODAJU građevinskog zemljišta u Čakovcu")).isFalse();
        assertThat(FILTER.isCall("Poziv na prethodnu provjeru znanja i sposobnosti – viši savjetnik za investicije")).isFalse();
        assertThat(FILTER.isCall("Javni natječaj – Referent za poslove ozakonjenja nezakonito izgrađenih zgrada")).isFalse();
        assertThat(FILTER.isCall("Javni poziv za predlaganje članova Kulturnog vijeća Vukovarsko-srijemske županije")).isFalse();
        assertThat(FILTER.isCall("Javni poziv za podnošenje poticaja za dodjelu javnih priznanja Grada Osijeka")).isFalse();
        assertThat(FILTER.isCall("Javni poziv za predlaganje programa javnih potreba u kulturi Grada Siska za 2027. godinu")).isTrue();
        assertThat(FILTER.isCall("Javni poziv za sufinanciranje kamata na stambene kredite zdravstvenih radnika")).isTrue();
    }

    @Test
    void ministryInTheTitleIsNotNews() {
        assertThat(FILTER.isCall("Javni poziv Ministarstva turizma i sporta za dodjelu potpora")).isTrue();
        assertThat(FILTER.isCall("Ministar posjetio korisnike potpora")).isFalse();
    }

    @Test
    void subsidiesForTransportPurchasesAndPaymentsAreCalls() {
        assertThat(FILTER.isCall("Javni poziv za sufinanciranje troškova prijevoza studenata")).isTrue();
        assertThat(FILTER.isCall("Javni poziv za isplatu jednokratne potpore za novorođeno dijete")).isTrue();
        assertThat(FILTER.isCall("Javni poziv za sufinanciranje kupnje električnih bicikala")).isTrue();
    }

    @Test
    void decisionsWorkshopsAndNoticesAreDropped() {
        assertThat(List.of(
                "Objavljene odluke iz Programa potpore za izostanak prihoda od svinjogojske proizvodnje",
                "Isplaćena potpora iz Programa potpore za poljoprivredne sektore",
                "Radionice za potencijalne korisnike intervencije 78.02. Potpora za pružanje savjetodavnih usluga",
                "Otkazana radionica u Gospiću planirana za 16. listopada 2025. za natječaje za intervencije",
                "Izmjenama plana objave natječaja povećana alokacija u pojedinim intervencijama",
                "Objavljeni novi natječaji za zapošljavanje",
                "Obavijest o dostavi odluka za Nacionalne potpore",
                "Dvadeset i osma Odluka o financiranju u sklopu Poziva NPOO.C3.1.R1-I2.01",
                "Popis dobitnika državne stipendije u STEM područjima znanosti",
                "Natječaj za upis studenata na doktorske studije Sveučilišta u Zadru",
                "Erasmus+ kviz okupio međunarodne studente u Studentskom klubu"))
                .noneMatch(FILTER::isCall);
    }
}
