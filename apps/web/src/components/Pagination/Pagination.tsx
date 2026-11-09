import type {ReactNode} from "react";
import {Link} from "wouter";
import {classNames, formatCount} from "@/utils";
import {Icon} from "@/components/Icon";
import styles from "./Pagination.module.css";

const scrollToTop = () => window.scrollTo(0, 0);

interface PageLinkProps {
    href: string | undefined;
    children: ReactNode;
}

const PageLink = ({href, children}: PageLinkProps) => {
    if (!href) {
        return <span className={classNames(styles.link, styles.disabled)} aria-hidden="true">{children}</span>;
    }
    return <Link href={href} className={styles.link} onClick={scrollToTop}>{children}</Link>;
};

interface PaginationProps {
    page: number;
    pageSize: number;
    totalElements: number;
    getPageHref: (page: number) => string;
}

export const Pagination = ({page, pageSize, totalElements, getPageHref}: PaginationProps) => {
    const pageCount = Math.ceil(totalElements / pageSize);
    if (pageCount <= 1) {
        return null;
    }

    return (
        <nav className={styles.pagination} aria-label="Stranice rezultata">
            <PageLink href={page > 0 ? getPageHref(page - 1) : undefined}>
                <Icon name="chevronLeft"/>
                Prethodna
            </PageLink>
            <p className={classNames(styles.status, "tabular-nums")}>
                Stranica {formatCount(page + 1)} od {formatCount(pageCount)}
            </p>
            <PageLink href={page < pageCount - 1 ? getPageHref(page + 1) : undefined}>
                Sljedeća
                <Icon name="chevronRight"/>
            </PageLink>
        </nav>
    );
};
