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
