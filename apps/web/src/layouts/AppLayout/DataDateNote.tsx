import {useGetMeta} from "@/hooks";
import {formatDate} from "@/utils";

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
