"use client"

import * as ResizablePrimitive from "react-resizable-panels"

import { cn } from "@/lib/utils"

const RESIZE_HIT_TARGET = { fine: 10, coarse: 28 } as const

function ResizablePanelGroup({
  className,
  direction,
  orientation,
  resizeTargetMinimumSize,
  ...props
}: ResizablePrimitive.GroupProps & { direction?: "horizontal" | "vertical" }) {
  return (
    <ResizablePrimitive.Group
      data-slot="resizable-panel-group"
      orientation={orientation ?? direction ?? "horizontal"}
      resizeTargetMinimumSize={resizeTargetMinimumSize ?? RESIZE_HIT_TARGET}
      className={cn(
        "flex h-full w-full min-h-0 min-w-0 data-[orientation=vertical]:flex-col",
        className
      )}
      {...props}
    />
  )
}

function ResizablePanel({
  className,
  ...props
}: ResizablePrimitive.PanelProps) {
  return (
    <ResizablePrimitive.Panel
      data-slot="resizable-panel"
      className={cn(
        "flex min-h-0 min-w-0 flex-col overflow-hidden",
        className
      )}
      {...props}
    />
  )
}

function ResizableHandle({
  withHandle,
  className,
  ...props
}: ResizablePrimitive.SeparatorProps & {
  withHandle?: boolean
}) {
  return (
    <ResizablePrimitive.Separator
      data-slot="resizable-handle"
      className={cn(
        "relative z-20 flex w-2 shrink-0 items-center justify-center bg-slate-600/70 ring-offset-background after:absolute after:inset-y-0 after:left-1/2 after:w-3 after:-translate-x-1/2 hover:bg-emerald-500/35 focus-visible:ring-1 focus-visible:ring-ring focus-visible:outline-hidden data-[separator=active]:bg-emerald-500/50 aria-[orientation=horizontal]:h-2 aria-[orientation=horizontal]:w-full aria-[orientation=horizontal]:after:left-0 aria-[orientation=horizontal]:after:h-3 aria-[orientation=horizontal]:after:w-full aria-[orientation=horizontal]:after:translate-x-0 aria-[orientation=horizontal]:after:-translate-y-1/2 [&[aria-orientation=horizontal]>div]:rotate-90",
        className
      )}
      {...props}
    >
      {withHandle && (
        <div className="pointer-events-none z-10 flex h-8 w-1 shrink-0 rounded-full bg-slate-400/90 shadow-sm" />
      )}
    </ResizablePrimitive.Separator>
  )
}

export { ResizableHandle, ResizablePanel, ResizablePanelGroup }
