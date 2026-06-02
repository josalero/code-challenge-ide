use challenge::is_palindrome;

#[test]
fn public_ispalindrome_a_man_a_plan_a_can_should_be() {
    assert_eq!(is_palindrome("A man, a plan, a canal: Panama"), true);
}
