import type {ButtonHTMLAttributes, ReactNode} from "react";
import {Link} from "wouter";
import {classNames} from "@/utils";
import styles from "./Button.module.css";

export type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";

export type ButtonSize = "sm" | "md";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: ButtonVariant;
    size?: ButtonSize;
}

export const Button = ({variant = "primary", size = "md", type = "button", className, ...props}: ButtonProps) => {
    return (
        <button
            type={type}
            className={classNames(styles.button, styles[variant], styles[size], className)}
            {...props}
        />
    );
};

interface ButtonLinkProps {
    href: string;
    children: ReactNode;
    variant?: ButtonVariant;
    size?: ButtonSize;
    className?: string;
}

export const ButtonLink = ({href, children, variant = "primary", size = "md", className}: ButtonLinkProps) => {
    return (
        <Link href={href} className={classNames(styles.button, styles[variant], styles[size], className)}>
            {children}
        </Link>
    );
};
