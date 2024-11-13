import MockAbortSignal from "./MockAbortSignal";

export default class MockAbortController {
  signal = new MockAbortSignal();

  abort(): void {
    this.signal.dispatchEvent(new Event("abort"));
    this.signal.aborted = true;
  }
}
