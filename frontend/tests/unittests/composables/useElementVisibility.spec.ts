/* eslint-disable n/no-callback-literal */
import { beforeEach, describe, expect, it, vi } from "vitest";
import useElementVisibility from "@/composables/useElementVisibility";
import { useIntersectionObserver } from "@vueuse/core";

// Based on @vueuse/core

vi.mock("@vueuse/core", async () => ({
  useIntersectionObserver: vi.fn(() => ({
    stop: vi.fn(),
  })),
}));

describe("useElementVisibility", () => {
  let el: HTMLDivElement;

  beforeEach(() => {
    el = document.createElement("div");
  });

  describe("when internally using useIntersectionObserver", async () => {
    it("should call useIntersectionObserver internally", () => {
      expect(useIntersectionObserver).toHaveBeenCalledTimes(0);
      useElementVisibility(el);
      expect(useIntersectionObserver).toHaveBeenCalledTimes(1);
    });

    it("passes the given element to useIntersectionObserver", () => {
      useElementVisibility(el);
      expect(vi.mocked(useIntersectionObserver).mock.lastCall?.[0]).toBe(el);
    });

    it("passes a callback to useIntersectionObserver that sets visibility to false only when isIntersecting is false", async () => {
      const { elementIsVisibleRefPromise } = useElementVisibility(el);
      const callback = vi.mocked(useIntersectionObserver).mock.lastCall?.[1];
      const callMockCallbackWithIsIntersectingValue = (isIntersecting: boolean) =>
        callback?.(
          [{ isIntersecting, time: 1 } as IntersectionObserverEntry],
          {} as IntersectionObserver,
        );

      // It should be false if the callback gets an isIntersecting = false
      callMockCallbackWithIsIntersectingValue(false);

      const isVisible = await elementIsVisibleRefPromise;

      expect(isVisible.value).toBe(false);

      // But it should become true if the callback gets an isIntersecting = true
      callMockCallbackWithIsIntersectingValue(true);
      expect(isVisible.value).toBe(true);

      // And it should become false again if isIntersecting = false
      callMockCallbackWithIsIntersectingValue(false);
      expect(isVisible.value).toBe(false);
    });

    it("uses the latest version of isIntersecting when multiple intersection entries are given", async () => {
      const { elementIsVisibleRefPromise } = useElementVisibility(el);
      const callback = vi.mocked(useIntersectionObserver).mock.lastCall?.[1];
      const callMockCallbackWithIsIntersectingValues = (
        ...entries: { isIntersecting: boolean; time: number }[]
      ) => {
        callback?.(entries as IntersectionObserverEntry[], {} as IntersectionObserver);
      };

      // It should take the latest value of isIntersecting
      callMockCallbackWithIsIntersectingValues(
        { isIntersecting: false, time: 1 },
        { isIntersecting: false, time: 2 },
        { isIntersecting: true, time: 3 },
      );

      const isVisible = await elementIsVisibleRefPromise;

      expect(isVisible.value).toBe(true);

      // It should take the latest even when entries are out of order
      callMockCallbackWithIsIntersectingValues(
        { isIntersecting: true, time: 1 },
        { isIntersecting: false, time: 3 },
        { isIntersecting: true, time: 2 },
      );

      expect(isVisible.value).toBe(false);
    });

    it("passes the given window to useIntersectionObserver", () => {
      const mockWindow = {} as Window;

      useElementVisibility(el, { window: mockWindow });
      expect(vi.mocked(useIntersectionObserver).mock.lastCall?.[2]?.window).toBe(mockWindow);
    });

    it("uses the given scrollTarget as the root element in useIntersectionObserver", () => {
      const mockScrollTarget = document.createElement("div");

      useElementVisibility(el, { scrollTarget: mockScrollTarget });
      expect(vi.mocked(useIntersectionObserver).mock.lastCall?.[2]?.root).toBe(mockScrollTarget);
    });
  });
});
