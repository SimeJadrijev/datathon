import type {ReactNode} from "react";
import {classNames} from "@/utils";
import styles from "./Badge.module.css";

export type BadgeTone = "neutral" | "accent" | "success" | "warning" | "danger" | "ai";

interface BadgeProps {
    children: ReactNode;
    tone?: BadgeTone;
    title?: string;
}

export const Badge = ({children, tone = "neutral", title}: BadgeProps) => {
    return (
        <span className={classNames(styles.badge, styles[tone])} title={title}>
            {children}
        </span>
    );
};
