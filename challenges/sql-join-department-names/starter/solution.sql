SELECT e.name, d.department
FROM employees e
JOIN departments d ON d.id = e.department_id
WHERE e.department_id = 2
ORDER BY e.name;
