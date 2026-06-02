use challenge::is_power_of_two;

#[test]
fn hidden_cases() {
    assert_eq!(is_power_of_two(16), true);
    assert_eq!(is_power_of_two(0), false);
}
