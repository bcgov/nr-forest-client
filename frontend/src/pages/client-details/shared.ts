import type { IndexedRelatedClient, SaveEvent } from "@/dto/CommonTypesDto";

export interface SaveableComponent {
  setSaving: (value: boolean) => void;
  lockEditing: () => void;
}

/**
 * The number of titled columns displayed in the Client relationships table for users with
 * permission to edit.
 * Note: non-titled, empty columns, which are used for spacing, layout purposes only, are not
 * included in the count.
 */
export const CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT = 6;

export interface OperationOptions {
  preserveRawPatch?: boolean;
  saveableComponent?: SaveableComponent;
}

export type OperateRelatedClient = (
  payload: SaveEvent<IndexedRelatedClient>,
  rawOptions?: OperationOptions,
) => void;

export type GoToTab = (tabName: string) => void;
