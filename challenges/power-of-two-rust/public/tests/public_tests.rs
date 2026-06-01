use challenge::is_power_of_two;

#[test]
fn public_cases() {
    assert_eq!(is_power_of_two(1), true);
    assert_eq!(is_power_of_two(3), false);
}
