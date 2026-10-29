import type {ReactNode} from "react";
import {classNames} from "@/utils";
import styles from "./Badge.module.css";

export type BadgeTone = "neutral" | "accent" | "success" | "warning" | "danger" | "ai";

interface BadgeProps {
    children: ReactNode;
    tone?: BadgeTone;
    title?: string;
    className?: string;
}

export const Badge = ({children, tone = "neutral", title, className}: BadgeProps) => {
    return (
        <span className={classNames(styles.badge, styles[tone], className)} title={title}>
            {children}
        </span>
    );
};
