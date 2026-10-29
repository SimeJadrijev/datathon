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
