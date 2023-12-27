import { useMediaQuery } from "@vueuse/core";

export const isSmallScreen = useMediaQuery("(max-width: 671px)");
export const isMediumScreen = useMediaQuery("(min-width: 672px) and (max-width: 1055px)");
export const isTouchScreen = useMediaQuery("(hover: none)");
