import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "truncate", standalone: true })
export class TruncatePipe implements PipeTransform {
  transform(value: string, limit: number): string {
    throw new Error("TODO");
  }
}
