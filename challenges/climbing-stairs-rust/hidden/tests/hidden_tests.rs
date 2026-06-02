use challenge::climb_stairs;

#[test]
fn hidden_cases() {
    assert_eq!(climb_stairs(10), 89);
    assert_eq!(climb_stairs(1), 1);
}
