use challenge::reverse_string;

#[test]
fn public_reversestring_hello_should_be_olleh() {
    assert_eq!(reverse_string("hello"), "olleh");
}
