import {Notice} from "@/components";
import type {FundingCallDetail} from "@/types";
import {formatDate, getDaysUntil} from "@/utils";

interface FundingCallStatusNoticeProps {
    fundingCall: FundingCallDetail;
}

export const FundingCallStatusNotice = ({fundingCall}: FundingCallStatusNoticeProps) => {
    const {deadline, status} = fundingCall;

    if (deadline && getDaysUntil(deadline) < 0) {
        return (
            <Notice tone="warning" title="Rok za prijavu je istekao">
                <p>
                    Rok je bio {formatDate(deadline)} Na izvoru provjeri je li poziv produljen ili ponovno raspisan.
                </p>
            </Notice>
        );
    }
    if (status === "CLOSED") {
        return (
            <Notice tone="warning" title="Natječaj je zatvoren">
                <p>Prijave više nisu moguće. Na izvoru provjeri hoće li se poziv ponoviti.</p>
            </Notice>
        );
    }
    if (status === null) {
        return (
            <Notice tone="warning" title="Status nije potvrđen">
                <p>Iz objave nismo mogli pouzdano pročitati je li natječaj otvoren. Provjeri to na izvoru prije prijave.</p>
            </Notice>
        );
    }
    if (status === "ANNOUNCED") {
        return (
            <Notice tone="info" title="Natječaj je najavljen">
                <p>Prijave još nisu otvorene. Rok i uvjeti mogu se promijeniti do službene objave.</p>
            </Notice>
        );
    }
    return null;
};
