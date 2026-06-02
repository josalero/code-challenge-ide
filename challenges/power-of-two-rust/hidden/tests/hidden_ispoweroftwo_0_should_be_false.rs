use challenge::is_power_of_two;

#[test]
fn hidden_ispoweroftwo_0_should_be_false() {
    assert_eq!(is_power_of_two(0), false);
}
