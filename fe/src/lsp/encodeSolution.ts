/** Base64url-encodes solution source for the LSP WebSocket handshake query param. */
export function encodeSolutionForLsp(code: string): string {
  const bytes = new TextEncoder().encode(code);
  let binary = "";
  bytes.forEach((b) => {
    binary += String.fromCharCode(b);
  });
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
