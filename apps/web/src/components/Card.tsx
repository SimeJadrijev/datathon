import type {ReactNode} from "react";
import {classNames} from "@/utils";
import styles from "./Card.module.css";

interface CardProps {
    children: ReactNode;
    as?: "article" | "section" | "div";
    className?: string;
}

export const Card = ({children, as: Element = "div", className}: CardProps) => {
    return <Element className={classNames(styles.card, className)}>{children}</Element>;
};
