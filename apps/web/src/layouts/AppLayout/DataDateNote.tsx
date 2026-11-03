import {useGetMeta} from "@/hooks";
import {formatDate} from "@/utils";

// The snapshot date protects every page: a call closed after this day is not our mistake. Quiet when meta fails.
export const DataDateNote = () => {
    const {data: meta} = useGetMeta();

    if (!meta?.snapshotDate) {
        return null;
    }
    return (
        <li>
            Stanje podataka na dan{" "}
            <time dateTime={meta.snapshotDate} className="tabular-nums">{formatDate(meta.snapshotDate)}</time>
        </li>
    );
};
