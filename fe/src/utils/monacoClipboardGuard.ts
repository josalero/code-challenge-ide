import type { editor as MonacoEditor } from "monaco-editor";

export type IntegrityEventType =
  | "COPY"
  | "PASTE"
  | "CUT"
  | "TAB_HIDDEN"
  | "TAB_VISIBLE"
  | "WINDOW_BLUR"
  | "WINDOW_FOCUS"
  | "LARGE_EDIT";

export type IntegrityEditorSurface = "SOLUTION" | "CUSTOM_TESTS";

export type IntegrityEventPayload = {
  eventType: IntegrityEventType;
  editorSurface?: IntegrityEditorSurface;
  charCount?: number;
  awayMs?: number;
};

/** @deprecated use IntegrityEventPayload */
export type ClipboardViolationPayload = IntegrityEventPayload & {
  editorSurface: IntegrityEditorSurface;
};

export const LARGE_EDIT_CHAR_THRESHOLD = 150;

export function attachMonacoClipboardGuard(
  editor: MonacoEditor.IStandaloneCodeEditor,
  options: {
    enabled: boolean;
    editorSurface: IntegrityEditorSurface;
    onViolation: (payload: IntegrityEventPayload) => void;
  },
): { dispose: () => void } {
  if (!options.enabled) {
    return { dispose: () => {} };
  }

  const domNode = editor.getDomNode();
  if (!domNode) {
    return { dispose: () => {} };
  }

  const record = (
    event: ClipboardEvent,
    eventType: Extract<IntegrityEventType, "COPY" | "PASTE" | "CUT">,
  ) => {
    const charCount =
      eventType === "PASTE"
        ? event.clipboardData?.getData("text/plain")?.length ?? undefined
        : undefined;
    options.onViolation({
      eventType,
      editorSurface: options.editorSurface,
      charCount,
    });
  };

  const onCopy = (event: ClipboardEvent) => record(event, "COPY");
  const onCut = (event: ClipboardEvent) => record(event, "CUT");
  const onPaste = (event: ClipboardEvent) => record(event, "PASTE");

  domNode.addEventListener("copy", onCopy, true);
  domNode.addEventListener("cut", onCut, true);
  domNode.addEventListener("paste", onPaste, true);

  return {
    dispose: () => {
      domNode.removeEventListener("copy", onCopy, true);
      domNode.removeEventListener("cut", onCut, true);
      domNode.removeEventListener("paste", onPaste, true);
    },
  };
}

export function attachMonacoLargeEditGuard(
  editor: MonacoEditor.IStandaloneCodeEditor,
  options: {
    enabled: boolean;
    editorSurface: IntegrityEditorSurface;
    onLargeEdit: (payload: IntegrityEventPayload) => void;
  },
): { dispose: () => void } {
  if (!options.enabled) {
    return { dispose: () => {} };
  }

  const subscription = editor.onDidChangeModelContent((event) => {
    let added = 0;
    for (const change of event.changes) {
      added += change.text.length;
    }
    if (added >= LARGE_EDIT_CHAR_THRESHOLD) {
      options.onLargeEdit({
        eventType: "LARGE_EDIT",
        editorSurface: options.editorSurface,
        charCount: added,
      });
    }
  });

  return { dispose: () => subscription.dispose() };
}
