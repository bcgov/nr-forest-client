export enum DatePart {
  year,
  month,
  day,
};

export interface DateValidator {
  (value: string): string;
  datePart?: DatePart
};
