SELECT d.department, MAX(e.salary) AS max_salary
FROM employees e
JOIN departments d ON d.id = e.department_id
GROUP BY d.department
ORDER BY d.department;
