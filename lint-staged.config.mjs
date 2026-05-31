/** @type {import('lint-staged').Configuration} */
export default {
  'fe/**/*.{ts,tsx}': (files) => {
    const rel = files.map((f) => f.replace(/^fe\//, ''));
    return [`bash -c 'cd fe && npx eslint --fix ${rel.map((r) => `"${r}"`).join(' ')}'`];
  },
  'be/**/*.java': () => './gradlew :be:compileJava -q --no-daemon',
};
