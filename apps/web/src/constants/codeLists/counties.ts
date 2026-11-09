export const COUNTIES = [
    "Bjelovarsko-bilogorska",
    "Brodsko-posavska",
    "Dubrovačko-neretvanska",
    "Grad Zagreb",
    "Istarska",
    "Karlovačka",
    "Koprivničko-križevačka",
    "Krapinsko-zagorska",
    "Ličko-senjska",
    "Međimurska",
    "Osječko-baranjska",
    "Požeško-slavonska",
    "Primorsko-goranska",
    "Sisačko-moslavačka",
    "Splitsko-dalmatinska",
    "Šibensko-kninska",
    "Varaždinska",
    "Virovitičko-podravska",
    "Vukovarsko-srijemska",
    "Zadarska",
    "Zagrebačka",
];

export const ALL_COUNTIES = "ALL";

const COUNTY_SUFFIX = /\s+županija$/i;

export const formatCounty = (county: string) => {
    if (county === ALL_COUNTIES) {
        return "Cijela Hrvatska";
    }
    if (!COUNTIES.includes(county) || county === "Grad Zagreb") {
        return county;
    }
    return `${county} županija`;
};

export const findCounty = (name: string | null | undefined) => {
    if (!name) {
        return undefined;
    }
    const shortName = name.trim().replace(COUNTY_SUFFIX, "").toLocaleLowerCase("hr-HR");
    return COUNTIES.find((county) => county.toLocaleLowerCase("hr-HR") === shortName);
};
