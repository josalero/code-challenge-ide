export function difficultyColor(difficulty: string): string {
  switch (difficulty.toLowerCase()) {
    case "easy":
      return "green";
    case "medium":
      return "gold";
    case "hard":
      return "red";
    default:
      return "default";
  }
}
