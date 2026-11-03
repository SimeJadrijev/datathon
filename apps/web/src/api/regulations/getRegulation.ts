import {api} from "@/api/base.ts";
import type {RegulationDetail} from "@/types";

export const getRegulation = (id: number) =>
    api.get<RegulationDetail, RegulationDetail>(`/regulations/${id}`);
