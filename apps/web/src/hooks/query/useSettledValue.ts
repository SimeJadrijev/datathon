import {useState} from "react";

export const useSettledValue = <T>(value: T, data: unknown, isPlaceholderData: boolean) => {
    const [settled, setSettled] = useState({value, data});

    if (!isPlaceholderData && settled.data !== data) {
        setSettled({value, data});
    }

    return isPlaceholderData ? settled.value : value;
};
