import type {ReactNode} from "react";
import styles from "./FundingCallList.module.css";

interface FundingCallListProps {
    children: ReactNode;
}

export const FundingCallList = ({children}: FundingCallListProps) => {
    return <ol className={styles.list}>{children}</ol>;
};
