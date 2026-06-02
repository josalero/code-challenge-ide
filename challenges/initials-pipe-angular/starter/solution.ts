import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "initials", standalone: true })
export class InitialsPipe implements PipeTransform {
  transform(fullName: string): string {
    throw new Error("TODO");
  }
}
