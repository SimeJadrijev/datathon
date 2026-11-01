import {api} from "@/api/base.ts";
import type {Meta} from "@/types";

export const getMeta = () => api.get<Meta, Meta>("/meta");
