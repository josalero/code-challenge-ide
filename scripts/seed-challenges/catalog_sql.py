"""
SQL catalog — inspired by PostgreSQL Exercises / SQLBolt-style drills (original statements & tests).

Schema: departments + employees (mini HR dataset).
"""

# Shared seed data (PostgreSQL)
EMPLOYEES_SCHEMA = """
CREATE TABLE departments (
  id SERIAL PRIMARY KEY,
  department VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE employees (
  id SERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  department_id INT NOT NULL REFERENCES departments(id),
  salary NUMERIC(12, 2) NOT NULL
);

INSERT INTO departments (id, department) VALUES
  (1, 'Sales'),
  (2, 'Engineering'),
  (3, 'HR');

INSERT INTO employees (id, name, department_id, salary) VALUES
  (1, 'Alice Chen', 2, 120000.00),
  (2, 'Bob Martinez', 1, 75000.00),
  (3, 'Carol White', 2, 105000.00),
  (4, 'Dan Kim', 3, 68000.00),
  (5, 'Eve Lopez', 1, 82000.00),
  (6, 'Frank Ng', 2, 99000.00);
"""

SQL_HEADER = "from ctl_sql import assert_query_result, assert_scalar, assert_row_count\n\n"


def _test(name: str, body: str) -> tuple[str, str]:
    return (name, SQL_HEADER + f"def {name}() -> None:\n    {body.strip()}\n\n")


SQL_CHALLENGES: list[dict] = [
    {
        "slug": "sql-select-all-employees",
        "title": "List all employees",
        "difficulty": "easy",
        "description": "Return every employee's id and name, ordered by id ascending.",
        "language": "sql",
        "runtime": "17",
        "starter": "SELECT id, name\nFROM employees\nORDER BY id;",
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_row_count", "assert_row_count(6)"),
        ],
        "hidden_tests": [
            _test(
                "test_first_and_last",
                "assert_query_result([(1, 'Alice Chen'), (6, 'Frank Ng')], ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_row_count", "description": "Returns 6 rows"},
        ],
    },
    {
        "slug": "sql-filter-sales",
        "title": "Employees in Sales",
        "difficulty": "easy",
        "description": "Return id and name of employees in the Sales department (department_id = 1).",
        "language": "sql",
        "runtime": "17",
        "starter": "SELECT id, name\nFROM employees\nWHERE department_id = 1\nORDER BY id;",
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_sales_count", "assert_row_count(2)"),
        ],
        "hidden_tests": [
            _test(
                "test_sales_names",
                "assert_query_result([(2, 'Bob Martinez'), (5, 'Eve Lopez')], ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_sales_count", "description": "Two employees work in Sales"},
        ],
    },
    {
        "slug": "sql-count-engineering",
        "title": "Count Engineering headcount",
        "difficulty": "easy",
        "description": "Return a single number: how many employees are in Engineering (department_id = 2).",
        "language": "sql",
        "runtime": "17",
        "starter": "SELECT COUNT(*) AS engineering_count\nFROM employees\nWHERE department_id = 2;",
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_count", "assert_scalar(3)"),
        ],
        "hidden_tests": [
            _test("test_still_scalar", "assert_scalar(3)"),
        ],
        "public_tests_meta": [
            {"name": "test_count", "description": "Engineering has three employees"},
        ],
    },
    {
        "slug": "sql-join-department-names",
        "title": "Employee with department name",
        "difficulty": "medium",
        "description": "Return employee name and department name for everyone in Engineering, ordered by employee name.",
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT e.name, d.department\n"
            "FROM employees e\n"
            "JOIN departments d ON d.id = e.department_id\n"
            "WHERE e.department_id = 2\n"
            "ORDER BY e.name;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_engineering_join", "assert_row_count(3)"),
        ],
        "hidden_tests": [
            _test(
                "test_no_sales",
                "assert_query_result("
                "[('Alice Chen', 'Engineering'), ('Carol White', 'Engineering'), ('Frank Ng', 'Engineering')], "
                "ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {
                "name": "test_engineering_join",
                "description": "Three Engineering rows with department label",
            },
        ],
    },
    {
        "slug": "sql-avg-salary-by-department",
        "title": "Average salary by department",
        "difficulty": "medium",
        "description": "Return department name and average salary (rounded to 2 decimals), ordered by department name.",
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT d.department, ROUND(AVG(e.salary)::numeric, 2) AS avg_salary\n"
            "FROM employees e\n"
            "JOIN departments d ON d.id = e.department_id\n"
            "GROUP BY d.department\n"
            "ORDER BY d.department;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_three_departments", "assert_row_count(3)"),
        ],
        "hidden_tests": [
            _test(
                "test_avg_values",
                "assert_query_result("
                "[('Engineering', 108000.00), ('HR', 68000.00), ('Sales', 78500.00)], "
                "ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_three_departments", "description": "One row per department"},
        ],
    },
    {
        "slug": "sql-top-salaries",
        "title": "Top three salaries",
        "difficulty": "medium",
        "description": "Return the names of the three highest-paid employees, highest first.",
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT name\n"
            "FROM employees\n"
            "ORDER BY salary DESC\n"
            "LIMIT 3;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_limit", "assert_row_count(3)"),
        ],
        "hidden_tests": [
            _test(
                "test_top_names",
                "assert_query_result("
                "[('Alice Chen',), ('Carol White',), ('Frank Ng',)], ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_limit", "description": "Exactly three rows returned"},
        ],
    },
    {
        "slug": "sql-salary-over-100k",
        "title": "Salaries over 100k",
        "difficulty": "easy",
        "description": "Return id and name of employees earning more than 100000, ordered by salary descending.",
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT id, name\n"
            "FROM employees\n"
            "WHERE salary > 100000\n"
            "ORDER BY salary DESC;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_count", "assert_row_count(2)"),
        ],
        "hidden_tests": [
            _test(
                "test_rows",
                "assert_query_result([(1, 'Alice Chen'), (3, 'Carol White')], ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_count", "description": "Two employees earn over 100k"},
        ],
    },
    {
        "slug": "sql-departments-without-employees",
        "title": "Empty departments",
        "difficulty": "hard",
        "description": "Return department names that have zero employees assigned.",
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT d.department\n"
            "FROM departments d\n"
            "LEFT JOIN employees e ON e.department_id = d.id\n"
            "WHERE e.id IS NULL\n"
            "ORDER BY d.department;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_none_empty", "assert_row_count(0)"),
        ],
        "hidden_tests": [
            _test("test_still_empty", "assert_row_count(0)"),
        ],
        "public_tests_meta": [
            {"name": "test_none_empty", "description": "All departments have staff in this dataset"},
        ],
    },
    {
        "slug": "sql-highest-paid-per-department",
        "title": "Highest paid in each department",
        "difficulty": "hard",
        "description": (
            "Return department name and the maximum salary in that department, "
            "ordered by department name."
        ),
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT d.department, MAX(e.salary) AS max_salary\n"
            "FROM employees e\n"
            "JOIN departments d ON d.id = e.department_id\n"
            "GROUP BY d.department\n"
            "ORDER BY d.department;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_dept_count", "assert_row_count(3)"),
        ],
        "hidden_tests": [
            _test(
                "test_max_values",
                "assert_query_result("
                "[('Engineering', 120000.00), ('HR', 68000.00), ('Sales', 82000.00)], "
                "ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_dept_count", "description": "One max salary per department"},
        ],
    },
    {
        "slug": "sql-distinct-salary-tiers",
        "title": "Distinct salary brackets",
        "difficulty": "medium",
        "description": (
            "Return distinct salaries that are strictly greater than 80000, "
            "ordered descending."
        ),
        "language": "sql",
        "runtime": "17",
        "starter": (
            "SELECT DISTINCT salary\n"
            "FROM employees\n"
            "WHERE salary > 80000\n"
            "ORDER BY salary DESC;"
        ),
        "schema": EMPLOYEES_SCHEMA,
        "public_tests": [
            _test("test_distinct_count", "assert_row_count(4)"),
        ],
        "hidden_tests": [
            _test(
                "test_values",
                "assert_query_result("
                "[(120000.00,), (105000.00,), (99000.00,), (82000.00,)], ordered=True)",
            ),
        ],
        "public_tests_meta": [
            {"name": "test_distinct_count", "description": "Four distinct salaries above 80k"},
        ],
    },
]
