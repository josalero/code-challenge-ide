use challenge::is_palindrome;

#[test]
fn public_ispalindrome_race_a_car_should_be_false() {
    assert_eq!(is_palindrome("race a car"), false);
}
