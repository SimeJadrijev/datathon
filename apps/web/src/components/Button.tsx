import type {ButtonHTMLAttributes, ReactNode} from "react";
import {Link} from "wouter";
import {classNames} from "@/utils";
import styles from "./Button.module.css";

type ButtonVariant = "primary" | "secondary";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: ButtonVariant;
}

export const Button = ({variant = "primary", type = "button", className, ...props}: ButtonProps) => {
    return (
        <button
            type={type}
            className={classNames(styles.button, styles[variant], className)}
            {...props}
        />
    );
};

interface ButtonLinkProps {
    href: string;
    children: ReactNode;
    variant?: ButtonVariant;
}

export const ButtonLink = ({href, children, variant = "primary"}: ButtonLinkProps) => {
    return (
        <Link href={href} className={classNames(styles.button, styles[variant])}>
            {children}
        </Link>
    );
};
