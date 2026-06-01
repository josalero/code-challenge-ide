use challenge::is_power_of_two;

#[test]
fn public_ispoweroftwo_3_should_be_false() {
    assert_eq!(is_power_of_two(3), false);
}
