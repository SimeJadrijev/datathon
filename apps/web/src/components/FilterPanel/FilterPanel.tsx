import {type ReactNode, useId, useState} from "react";
import {classNames} from "@/utils";
import {Button} from "@/components/Button";
import {Icon} from "@/components/Icon";
import styles from "./FilterPanel.module.css";

interface FilterPanelProps {
    children: ReactNode;
    activeCount: number;
    search?: ReactNode;
    label?: string;
}

export const FilterPanel = ({children, activeCount, search, label = "Filtri"}: FilterPanelProps) => {
    const panelId = useId();
    const [isOpen, setIsOpen] = useState(false);

    return (
        <section className={styles.wrapper} aria-label={label}>
            {search && <div className={styles.search}>{search}</div>}
            <Button
                variant="secondary"
                className={styles.toggle}
                aria-expanded={isOpen}
                aria-controls={panelId}
                onClick={() => setIsOpen((wasOpen) => !wasOpen)}
            >
                <Icon name={isOpen ? "close" : "filter"}/>
                {isOpen ? "Sakrij filtre" : "Filtri"}
                {activeCount > 0 && (
                    <span className={styles.toggleCount}>
                        {activeCount}
                        <span className="visually-hidden"> aktivno</span>
                    </span>
                )}
            </Button>
            <div id={panelId} className={classNames(styles.panel, isOpen && styles.panelOpen)}>
                {children}
            </div>
        </section>
    );
};
