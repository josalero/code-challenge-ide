package com.codetraininglab.operations.application;

import java.util.Map;
import java.util.Optional;

/**
 * Minimal passing solutions for pool smoke warm — exercises runner infrastructure without
 * starter stubs or hidden tests (aligned with {@code scripts/smoke-runners.sh}).
 */
public final class RunnerWarmSolutions {

  private static final Map<String, String> BY_SLUG =
      Map.ofEntries(
          Map.entry(
              "reverse-string",
              """
              package com.challenge;

              public class Solution {
                public static String reverse(String input) {
                  if (input == null) {
                    return null;
                  }
                  return new StringBuilder(input).reverse().toString();
                }
              }
              """),
          Map.entry(
              "armstrong-number",
              """
              def is_armstrong(n: int) -> bool:
                  if n < 0:
                      return False
                  digits = str(n)
                  power = len(digits)
                  return sum(int(d) ** power for d in digits) == n
              """),
          Map.entry(
              "anagram-check-go",
              """
              package solution

              func IsAnagram(s, t string) bool {
                if len(s) != len(t) {
                  return false
                }
                count := make(map[rune]int)
                for _, c := range s {
                  count[c]++
                }
                for _, c := range t {
                  count[c]--
                  if count[c] < 0 {
                    return false
                  }
                }
                return true
              }
              """),
          Map.entry(
              "anagram-check-node",
              """
              function isAnagram(s, t) {
                if (s.length !== t.length) return false;
                const a = s.split("").sort().join("");
                const b = t.split("").sort().join("");
                return a === b;
              }
              module.exports = { isAnagram };
              """),
          Map.entry(
              "anagram-check-typescript",
              """
              export function isAnagram(s: string, t: string): boolean {
                if (s.length !== t.length) return false;
                const a = [...s].sort().join("");
                const b = [...t].sort().join("");
                return a === b;
              }
              """),
          Map.entry(
              "anagram-check-csharp",
              """
              namespace Challenge;

              public static class Solution {
                public static bool IsAnagram(string s, string t) {
                  if (s.Length != t.Length) return false;
                  var a = s.ToCharArray();
                  var b = t.ToCharArray();
                  System.Array.Sort(a);
                  System.Array.Sort(b);
                  return new string(a) == new string(b);
                }
              }
              """),
          Map.entry(
              "anagram-check-rust",
              """
              pub fn is_anagram(s: &str, t: &str) -> bool {
                  let mut a: Vec<char> = s.chars().collect();
                  let mut b: Vec<char> = t.chars().collect();
                  a.sort();
                  b.sort();
                  a == b
              }
              """),
          Map.entry(
              "anagram-check-cpp",
              """
              #include <algorithm>
              #include <string>

              bool is_anagram(const std::string& s, const std::string& t) {
                if (s.size() != t.size()) return false;
                std::string a = s;
                std::string b = t;
                std::sort(a.begin(), a.end());
                std::sort(b.begin(), b.end());
                return a == b;
              }
              """),
          Map.entry(
              "accordion-react",
              """
              import { useState } from "react";

              export type AccordionItem = { title: string; content: string };

              type Props = { items: AccordionItem[] };

              export function Accordion({ items }: Props) {
                const [open, setOpen] = useState<string | null>(null);
                return (
                  <div>
                    {items.map((item) => (
                      <div key={item.title}>
                        <button
                          type="button"
                          onClick={() => setOpen(open === item.title ? null : item.title)}
                        >
                          {item.title}
                        </button>
                        {open === item.title ? <div>{item.content}</div> : null}
                      </div>
                    ))}
                  </div>
                );
              }
              """),
          Map.entry(
              "computed-filter-vue",
              """
              <script setup lang="ts">
              import { computed, ref } from "vue";

              const props = defineProps<{ items: string[] }>();
              const query = ref("");

              const filtered = computed(() => {
                const q = query.value.toLowerCase();
                if (!q) {
                  return props.items;
                }
                return props.items.filter((item) => item.toLowerCase().includes(q));
              });
              </script>

              <template>
                <input v-model="query" aria-label="filter" />
                <ul>
                  <li v-for="item in filtered" :key="item">{{ item }}</li>
                </ul>
              </template>
              """),
          Map.entry(
              "double-service-angular",
              """
              import { Injectable } from "@angular/core";

              @Injectable({ providedIn: "root" })
              export class DoubleService {
                double(n: number): number {
                  return n * 2;
                }
              }
              """));

  private RunnerWarmSolutions() {}

  public static Optional<String> solutionFor(String slug) {
    if (slug == null || slug.isBlank()) {
      return Optional.empty();
    }
    return Optional.ofNullable(BY_SLUG.get(slug.trim()));
  }
}
