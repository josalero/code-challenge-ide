import { Injectable } from "@angular/core";

@Injectable({ providedIn: "root" })
export class DoubleService {
  double(n: number): number {
    throw new Error("TODO");
  }
}
