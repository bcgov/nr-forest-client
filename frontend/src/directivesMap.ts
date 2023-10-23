import { masking, shadowPart } from "@/helpers/CustomDirectives";

const directivesMap = {
  masked: masking(".cds--text-input__field-wrapper input"),
  shadow: shadowPart,
};

export default directivesMap;
