"""Helpers for SQL challenge pytest modules (copied into /tmp/workspace)."""

from __future__ import annotations

import os
from decimal import Decimal
from pathlib import Path

import psycopg2

WORKSPACE = Path("/tmp/workspace")
CHALLENGE_MOUNT = Path("/challenge")

_conn = None


def _connect():
    global _conn
    if _conn is None or _conn.closed:
        _conn = psycopg2.connect(
            host=os.environ.get("CTL_PG_HOST", "127.0.0.1"),
            port=int(os.environ.get("CTL_PG_PORT", "5432")),
            dbname=os.environ.get("CTL_PG_DB", "postgres"),
            user=os.environ.get("CTL_PG_USER", "postgres"),
            password=os.environ.get("CTL_PG_PASSWORD", ""),
        )
        _conn.autocommit = True
    return _conn


def _schema_path() -> Path:
    for candidate in (WORKSPACE / "setup" / "schema.sql", CHALLENGE_MOUNT / "setup" / "schema.sql"):
        if candidate.is_file():
            return candidate
    raise FileNotFoundError("setup/schema.sql not found in workspace or challenge mount")


def reset_database() -> None:
    schema_sql = _schema_path().read_text(encoding="utf-8")
    conn = _connect()
    with conn.cursor() as cur:
        cur.execute("DROP SCHEMA IF EXISTS public CASCADE;")
        cur.execute("CREATE SCHEMA public;")
        cur.execute(schema_sql)


def read_solution_sql() -> str:
    path = WORKSPACE / "solution.sql"
    if not path.is_file():
        raise FileNotFoundError("solution.sql missing in workspace")
    text = path.read_text(encoding="utf-8").strip()
    if not text:
        raise ValueError("solution.sql is empty")
    return text.rstrip(";")


def _normalize_cell(value):
    if isinstance(value, Decimal):
        return float(value)
    return value


def fetch_rows(sql: str) -> list[tuple]:
    conn = _connect()
    with conn.cursor() as cur:
        cur.execute(sql)
        if cur.description is None:
            return []
        return [tuple(_normalize_cell(v) for v in row) for row in cur.fetchall()]


def assert_query_result(
    expected: list[tuple],
    *,
    ordered: bool = True,
    message: str | None = None,
) -> None:
    reset_database()
    user_sql = read_solution_sql()
    actual = fetch_rows(user_sql)
    exp = [tuple(row) for row in expected]
    if ordered:
        assert actual == exp, message or f"expected {exp!r}, got {actual!r}"
    else:
        assert sorted(actual) == sorted(exp), message or f"expected {exp!r}, got {actual!r}"


def assert_row_count(expected: int, message: str | None = None) -> None:
    reset_database()
    rows = fetch_rows(read_solution_sql())
    assert len(rows) == expected, message or f"expected {expected} rows, got {len(rows)}"


def assert_scalar(expected, message: str | None = None) -> None:
    reset_database()
    actual_rows = fetch_rows(read_solution_sql())
    assert len(actual_rows) == 1 and len(actual_rows[0]) == 1, (
        message or f"expected one scalar cell, got {actual_rows!r}"
    )
    assert actual_rows[0][0] == expected, message or f"expected {expected!r}, got {actual_rows[0][0]!r}"
