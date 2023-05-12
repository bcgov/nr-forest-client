/**
 * This file will have control over fetching of everything
 */
export const conversionFn = (code: any) => { 
                              return { 
                                value: { value: code.code, text: code.name }, 
                                text: code.name,
                                legalType: code.legalType
                              } 
};
