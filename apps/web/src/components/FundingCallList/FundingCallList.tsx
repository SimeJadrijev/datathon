import type {ReactNode} from "react";
import styles from "./FundingCallList.module.css";

interface FundingCallListProps {
    children: ReactNode;
}

// Frame for FundingCallRow items. Pass <li> elements as children.
export const FundingCallList = ({children}: FundingCallListProps) => {
    return <ol className={styles.list}>{children}</ol>;
};
