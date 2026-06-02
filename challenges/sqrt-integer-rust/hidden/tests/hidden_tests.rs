use challenge::my_sqrt;

#[test]
fn hidden_cases() {
    assert_eq!(my_sqrt(10), 3);
    assert_eq!(my_sqrt(2147483647), 46340);
}
