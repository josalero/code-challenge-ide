/** Types for the `vscode` npm alias (@codingame/monaco-vscode-api). */
declare module "vscode" {
  export class Uri {
    static file(path: string): Uri;
    static parse(value: string): Uri;
    readonly scheme: string;
    readonly path: string;
    toString(): string;
  }
}
