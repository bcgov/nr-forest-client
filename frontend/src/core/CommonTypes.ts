import type { Ref } from "vue";

export interface CodeDescrType {
    value: string;
    text: string;
}

export interface ValidationMessageType {
    fieldId: string;
    errorMsg: string;
}

export const isEmpty = (ref: Ref): boolean => {
    const value = ref.value;
    return value === undefined || value === null || value === "";
};