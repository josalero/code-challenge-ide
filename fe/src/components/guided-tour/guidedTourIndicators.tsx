export function guidedTourIndicators(currentStep: number, total: number) {
  return (
    <span className="text-xs text-muted-foreground">
      Step {currentStep + 1} of {total}
    </span>
  );
}
