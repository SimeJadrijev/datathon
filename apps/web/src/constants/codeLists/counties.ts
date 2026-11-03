// Values match FundingCall.counties in the seed data (short county names), in Croatian alphabetical order.
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

// FundingCall.counties holds "ALL" for calls open to the whole country.
export const ALL_COUNTIES = "ALL";

const COUNTY_SUFFIX = /\s+županija$/i;

// Short names get "županija"; full names ("Istarska županija"), Zagreb and places abroad stay as they are.
export const formatCounty = (county: string) => {
    if (county === ALL_COUNTIES) {
        return "Cijela Hrvatska";
    }
    if (!COUNTIES.includes(county) || county === "Grad Zagreb") {
        return county;
    }
    return `${county} županija`;
};

// The Court Register gives full names ("Istarska županija"); the form uses the short ones.
export const findCounty = (name: string | null | undefined) => {
    if (!name) {
        return undefined;
    }
    const shortName = name.trim().replace(COUNTY_SUFFIX, "").toLocaleLowerCase("hr-HR");
    return COUNTIES.find((county) => county.toLocaleLowerCase("hr-HR") === shortName);
};
