"""Pytest hooks for SQL challenges — fresh schema per test."""

from ctl_sql import reset_database


def pytest_runtest_setup(item) -> None:
    reset_database()
