import { Injectable } from "@angular/core";

@Injectable({ providedIn: "root" })
export class SlugifyService {
  slugify(text: string): string {
    throw new Error("TODO");
  }
}
