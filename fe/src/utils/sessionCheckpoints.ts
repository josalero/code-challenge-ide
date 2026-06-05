import type { IntegrityEditorSurface, IntegrityEventType } from "./monacoClipboardGuard";

export type WireMark = {
  k: number;
  at: string;
  s?: number;
  n?: number;
  d?: number;
};

const EVENT_KIND: Record<IntegrityEventType, number> = {
  COPY: 1,
  PASTE: 2,
  CUT: 3,
  TAB_HIDDEN: 4,
  TAB_VISIBLE: 5,
  WINDOW_BLUR: 6,
  WINDOW_FOCUS: 7,
  LARGE_EDIT: 8,
};

const SURFACE_CODE: Record<IntegrityEditorSurface, number> = {
  SOLUTION: 1,
  CUSTOM_TESTS: 2,
};

export function toWireCheckpoint(event: {
  eventType: IntegrityEventType;
  editorSurface?: IntegrityEditorSurface;
  charCount?: number;
  awayMs?: number;
  occurredAt: string;
}): WireMark {
  const mark: WireMark = {
    k: EVENT_KIND[event.eventType],
    at: event.occurredAt,
  };
  if (event.editorSurface) {
    mark.s = SURFACE_CODE[event.editorSurface];
  }
  if (event.charCount != null) {
    mark.n = event.charCount;
  }
  if (event.awayMs != null) {
    mark.d = event.awayMs;
  }
  return mark;
}
