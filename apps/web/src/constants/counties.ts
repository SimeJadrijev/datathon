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
