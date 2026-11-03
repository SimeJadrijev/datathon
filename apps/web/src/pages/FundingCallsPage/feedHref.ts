import type {FundingCallFilter} from "@/types";

const FEED_PATH = "/api/funding-calls/feed.atom";

export const getFeedHref = ({q, status, targetGroup, area, county, source, type}: FundingCallFilter) => {
    const params = new URLSearchParams();
    const values = {q, status, targetGroup, area, county, source, type};
    for (const [name, value] of Object.entries(values)) {
        if (value) {
            params.set(name, value);
        }
    }
    const query = params.toString();
    return query ? `${FEED_PATH}?${query}` : FEED_PATH;
};
