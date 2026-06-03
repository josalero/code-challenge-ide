SELECT d.department
FROM departments d
LEFT JOIN employees e ON e.department_id = d.id
WHERE e.id IS NULL
ORDER BY d.department;
