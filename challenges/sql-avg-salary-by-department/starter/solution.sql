SELECT d.department, ROUND(AVG(e.salary)::numeric, 2) AS avg_salary
FROM employees e
JOIN departments d ON d.id = e.department_id
GROUP BY d.department
ORDER BY d.department;
