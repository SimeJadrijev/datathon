import {api} from "@/api/base.ts";
import type {SourceSummary} from "@/types";

export const getSources = () => api.get<SourceSummary[], SourceSummary[]>("/sources");
