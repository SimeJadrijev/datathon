const OIB_PATTERN = /^\d{11}$/;

export const isValidOib = (value: string) => {
    if (!OIB_PATTERN.test(value)) {
        return false;
    }

    const digits = [...value].map(Number);
    let remainder = 10;
    for (const digit of digits.slice(0, 10)) {
        remainder = (remainder + digit) % 10 || 10;
        remainder = (remainder * 2) % 11;
    }
    const controlDigit = (11 - remainder) % 10;
    return controlDigit === digits[10];
};
