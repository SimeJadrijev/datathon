import {useState} from "react";

// While a query shows the previous results (keepPreviousData), returns the value that belongs to them,
// so a sentence like "12 zatvorenih natječaja" never pairs the new filter with the old count.
export const useSettledValue = <T>(value: T, data: unknown, isPlaceholderData: boolean) => {
    const [settled, setSettled] = useState({value, data});

    if (!isPlaceholderData && settled.data !== data) {
        setSettled({value, data});
    }

    return isPlaceholderData ? settled.value : value;
};
