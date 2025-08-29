export interface SaveableComponent {
  setSaving: (value: boolean) => void;
  lockEditing: () => void;
}
