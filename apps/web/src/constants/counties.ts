// Values match FundingCall.counties in the seed data (short county names).
export const COUNTIES = [
    "Grad Zagreb",
    "Zagrebačka",
    "Krapinsko-zagorska",
    "Sisačko-moslavačka",
    "Karlovačka",
    "Varaždinska",
    "Koprivničko-križevačka",
    "Bjelovarsko-bilogorska",
    "Primorsko-goranska",
    "Ličko-senjska",
    "Virovitičko-podravska",
    "Požeško-slavonska",
    "Brodsko-posavska",
    "Zadarska",
    "Osječko-baranjska",
    "Šibensko-kninska",
    "Vukovarsko-srijemska",
    "Splitsko-dalmatinska",
    "Istarska",
    "Dubrovačko-neretvanska",
    "Međimurska",
];

export const formatCounty = (county: string) =>
    county === "Grad Zagreb" ? county : `${county} županija`;

const COUNTY_SUFFIX = /\s+županija$/i;

// The Court Register gives full names ("Istarska županija"); the form uses the short ones.
export const findCounty = (name: string | null | undefined) => {
    if (!name) {
        return undefined;
    }
    const shortName = name.trim().replace(COUNTY_SUFFIX, "").toLocaleLowerCase("hr-HR");
    return COUNTIES.find((county) => county.toLocaleLowerCase("hr-HR") === shortName);
};
