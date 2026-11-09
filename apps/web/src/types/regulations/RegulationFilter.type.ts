import type {Area} from "@/types/codeLists";
import type {RegulationType} from "./Regulation.type.ts";

export type RegulationFilter = {
    q?: string;
    type?: RegulationType;
    area?: Area;
    publishedFrom?: string;
    publishedTo?: string;
    page?: number;
    size?: number;
};
