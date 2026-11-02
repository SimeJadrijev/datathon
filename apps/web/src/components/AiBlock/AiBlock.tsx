import type {ReactNode} from "react";
import {classNames} from "@/utils";
import styles from "./AiBlock.module.css";

interface AiBlockProps {
    label: string;
    note: ReactNode;
    children: ReactNode;
    className?: string;
}

// Frame for text a model wrote: dashed AI border, its own label and one sentence that says what to check.
export const AiBlock = ({label, note, children, className}: AiBlockProps) => {
    return (
        <div className={classNames(styles.block, className)}>
            <p className={styles.label}>{label}</p>
            <div className={styles.body}>{children}</div>
            <p className={styles.note}>{note}</p>
        </div>
    );
};
